package com.example.faceauth;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;

public class BlogicResponse {
	private boolean OK;
	private String message;

	@DynamoDbIgnore
	public boolean isOK() {
		return OK;
	}

	public void setOK(boolean oK) {
		OK = oK;
	}

	@DynamoDbIgnore
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
