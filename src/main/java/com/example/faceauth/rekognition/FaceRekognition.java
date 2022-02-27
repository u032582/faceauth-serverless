package com.example.faceauth.rekognition;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface FaceRekognition {

	@Data
	@Builder
	public static class IndexedFace {
		private String faceId;
		private Float boundingWidth;
		private Float boundingHeight;
		private Float boundingLeft;
		private Float boundingTop;

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MatchInfo {
		private String faceId;
		private Float similarity;
		private Float height;
		private Float width;
		private Float left;
		private Float top;
		private String faceImage;
		private String imageFormat;

	}

	IndexedFace indexFaces(String collectionId, byte[] faceImage);

	void createCollection(String collectionId);

	void deleteCollection(String collectionId);

	List<String> listAllCollections();

	void listFacesCollection(String collectionId);

	List<MatchInfo> searchFaceByImage(String collectionId, byte[] sourceImage, Integer maxFaces, Float matchThreshold);

}