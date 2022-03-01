package com.example.faceauth.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.faceauth.entity.Face;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Slf4j
@Component
public class FaceRepositoryDymanoImpl implements FaceRepository, InitializingBean, DisposableBean {

	@Value("${app.aws.region}")
	private String region;
	@Value("${app.aws.dynamodbTable}")
	private String tableName;
	private DynamoDbClient ddb;
	private DynamoDbEnhancedClient dynamodbClient;

	@Override
	public void afterPropertiesSet() throws Exception {
		ddb = DynamoDbClient.builder().region(Region.of(region)).build();
		dynamodbClient = DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
	}

	@Override
	public void destroy() throws Exception {
		ddb.close();
	}

	@Override
	public void deleteByAccountId(String accountId) {
		HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();
		keyToGet.put("accountId", AttributeValue.builder().s(accountId).build());
		DeleteItemRequest deleteReq = DeleteItemRequest.builder().tableName(tableName).key(keyToGet).build();
		ddb.deleteItem(deleteReq);
	}

	@Override
	public List<Face> findByAccountId(String accountId) {
		DynamoDbTable<Face> mappedTable = dynamodbClient.table(tableName, TableSchema.fromBean(Face.class));
		Key key = Key.builder().partitionValue(accountId).build();
		Face result = mappedTable.getItem(r -> r.key(key));
		List<Face> ret = new ArrayList<>();
		if (result != null)
			ret.add(result);
		return ret;
	}

	@Override
	public void save(Face face) {
		DynamoDbTable<Face> mappedTable = dynamodbClient.table(tableName, TableSchema.fromBean(Face.class));
		mappedTable.putItem(face);
	}

	public boolean isTableExist() {
		try {
			DescribeTableRequest request = DescribeTableRequest.builder().tableName(tableName).build();
			TableDescription tableDescription = ddb.describeTable(request).table();
			log.info("Dynamodb table does exist. name:{}", tableName);
			return true;
		} catch (ResourceNotFoundException rnfe) {
			log.info("Dynamodb table does not exist. name:{}", tableName);
		}
		return false;
	}

	public String createTable() {

		if (isTableExist())
			return "";

		DynamoDbWaiter dbWaiter = ddb.waiter();
		CreateTableRequest request = CreateTableRequest.builder()
				.attributeDefinitions(AttributeDefinition.builder().attributeName("accountId")
						.attributeType(ScalarAttributeType.S).build())
				.keySchema(KeySchemaElement.builder().attributeName("accountId").keyType(KeyType.HASH).build())
				.billingMode(BillingMode.PAY_PER_REQUEST)
//				.provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(new Long(10))
//						.writeCapacityUnits(new Long(10)).build())
				.tableName(tableName).build();

		String newTable = "";

		CreateTableResponse response = ddb.createTable(request);
		DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName(tableName).build();

		// Wait until the Amazon DynamoDB table is created
		log.info("テーブル作成中...");
		WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
		waiterResponse.matched().response().ifPresent(System.out::println);
		newTable = response.tableDescription().tableName();
		return newTable;

	}
}
