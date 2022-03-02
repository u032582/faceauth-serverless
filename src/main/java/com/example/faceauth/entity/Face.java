package com.example.faceauth.entity;

import java.time.LocalDateTime;

import com.example.faceauth.BlogicResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@DynamoDbBean
public class Face extends BlogicResponse {

	private String accountId;

	private String faceId;
	private String faceImage;
	private String imageFormat;
	private double boundingWidth;
	private double boundingHeight;
	private double boundingLeft;
	private double boundingTop;

	@JsonIgnore
	// spring cloud functionで入出力型にLocalDateTimeが含まれるとうまくいかなかったので、
	// とりえず Ignore で回避する。
	// daynamodbはDate型に対応しておらずLocalDateTimeを使う必要がある
	private LocalDateTime created;

	@DynamoDbPartitionKey
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

}