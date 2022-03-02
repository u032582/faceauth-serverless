package com.example.faceauth;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;

public class BlogicResponse {
	private boolean OK = true;
	private String errorDetail;

	@DynamoDbIgnore
	public boolean isOK() {
		return OK;
	}

	public void setOK(boolean oK) {
		OK = oK;
	}

	@DynamoDbIgnore
	public String getErrorDetail() {
		return errorDetail;
	}

	public void setErrorDetail(String message) {
		this.errorDetail = message;
	}

}
