package com.example.faceauth.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Face {

	private String accountId;

	private String faceId;
	private String faceImage;
	private String imageFormat;
	private double boundingWidth;
	private double boundingHeight;
	private double boundingLeft;
	private double boundingTop;

	private LocalDateTime created;

	@DynamoDbPartitionKey
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

}