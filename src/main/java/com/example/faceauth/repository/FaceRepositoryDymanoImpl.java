package com.example.faceauth.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
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
		createTable();
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

	/**
	 * {@link #findByAccountIdByEnhanced(String)} がnative版でエラーを吐くので低レベルAPIで対応
	 * 
	 */
	@Override
	public Face findByAccountId(String accountId) {
		HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();

		keyToGet.put("accountId", AttributeValue.builder().s(accountId).build());
		GetItemRequest request = GetItemRequest.builder().key(keyToGet).tableName(tableName).build();

		{
			Map<String, AttributeValue> returnedItem = ddb.getItem(request).item();

			if (returnedItem != null) {
				Set<String> keys = returnedItem.keySet();
				Face ret = new Face();
				for (String attr : keys) {
					switch (attr) {
					case "accountId":
						ret.setAccountId(returnedItem.get("accountId").s());
						break;
					case "faceId":
						ret.setFaceId(returnedItem.get("faceId").s());
						break;
					case "faceImage":
						ret.setFaceImage(returnedItem.get("faceImage").s());
						break;
					case "imageFormat":
						ret.setImageFormat(returnedItem.get("imageFormat").s());
						break;
					case "boundingLeft":
						ret.setBoundingLeft(Double.parseDouble(returnedItem.get("boundingLeft").n()));
						break;
					case "boundingTop":
						ret.setBoundingTop(Double.parseDouble(returnedItem.get("boundingTop").n()));
						break;
					case "boundingWidth":
						ret.setBoundingWidth(Double.parseDouble(returnedItem.get("boundingWidth").n()));
						break;
					case "boundingHeight":
						ret.setBoundingHeight(Double.parseDouble(returnedItem.get("boundingHeight").n()));
						break;
					case "created":
						ret.setCreated(LocalDateTime.parse(returnedItem.get("created").s()));
						break;
					}
				}
				if (ret.getAccountId() == null)
					throw ResourceNotFoundException.builder().message("accountId[" + accountId + "] face not found")
							.build();
				return ret;
			} else {
				log.warn("No item found with the key %s!\n", accountId);
			}
		}
		return null;
	}

	public Face findByAccountIdByEnhanced(String accountId) {
		DynamoDbTable<Face> mappedTable = dynamodbClient.table(tableName, TableSchema.fromBean(Face.class));
		Key key = Key.builder().partitionValue(accountId).build();
		var ret = mappedTable.getItem(r -> r.key(key));
		if (ret == null)
			throw ResourceNotFoundException.builder().message("accountId[" + accountId + "] face not found").build();
		return ret;
	}

	/**
	 * {@link #saveByEnhanced(Face)} がnative版でエラーを吐くので低レベルAPIで対応
	 * 
	 */
	@Override
	public void save(Face face) {
		HashMap<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>();
		itemValues.put("accountId", AttributeValue.builder().s(face.getAccountId()).build());
		itemValues.put("faceId", AttributeValue.builder().s(face.getFaceId()).build());
		itemValues.put("faceImage", AttributeValue.builder().s(face.getFaceImage()).build());
		itemValues.put("imageFormat", AttributeValue.builder().s(face.getImageFormat()).build());
		itemValues.put("created", AttributeValue.builder().s(face.getCreated().toString()).build());
		itemValues.put("boundingLeft", AttributeValue.builder().n(Double.toString(face.getBoundingLeft())).build());
		itemValues.put("boundingTop", AttributeValue.builder().n(Double.toString(face.getBoundingTop())).build());
		itemValues.put("boundingWidth", AttributeValue.builder().n(Double.toString(face.getBoundingWidth())).build());
		itemValues.put("boundingHeight", AttributeValue.builder().n(Double.toString(face.getBoundingHeight())).build());

		PutItemRequest request = PutItemRequest.builder().tableName(tableName).item(itemValues).build();
		ddb.putItem(request);
	}

	public void saveByEnhanced(Face face) {
		DynamoDbTable<Face> mappedTable = dynamodbClient.table(tableName, TableSchema.fromBean(Face.class));
		mappedTable.putItem(face);
	}

	boolean isTableExist() {
		try {
			DescribeTableRequest request = DescribeTableRequest.builder().tableName(tableName).build();
			TableDescription tableDescription = ddb.describeTable(request).table();
			log.info("Dynamodb table does exist. name:{}", tableDescription);
			return true;
		} catch (ResourceNotFoundException rnfe) {
			log.info("Dynamodb table does not exist. name:{}", tableName);
		}
		return false;
	}

	String createTable() {

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
