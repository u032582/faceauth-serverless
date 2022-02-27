package com.example.faceauth.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Face {

	private Long id;

	private String faceId;

	private Account account;

	private String faceImage;
	private String imageFormat;
	private Float boundingWidth;
	private Float boundingHeight;
	private Float boundingLeft;
	private Float boundingTop;
	private Date created;

}