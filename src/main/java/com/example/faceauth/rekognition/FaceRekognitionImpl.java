package com.example.faceauth.rekognition;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.DeleteCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.Face;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.FaceRecord;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesRequest;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;
import software.amazon.awssdk.services.rekognition.model.InvalidParameterException;
import software.amazon.awssdk.services.rekognition.model.ListCollectionsRequest;
import software.amazon.awssdk.services.rekognition.model.ListCollectionsResponse;
import software.amazon.awssdk.services.rekognition.model.ListFacesRequest;
import software.amazon.awssdk.services.rekognition.model.ListFacesResponse;
import software.amazon.awssdk.services.rekognition.model.QualityFilter;
import software.amazon.awssdk.services.rekognition.model.Reason;
import software.amazon.awssdk.services.rekognition.model.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.rekognition.model.ResourceNotFoundException;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageRequest;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageResponse;
import software.amazon.awssdk.services.rekognition.model.UnindexedFace;

@Slf4j
@Component
public class FaceRekognitionImpl implements FaceRekognition {
	private RekognitionClient rekClient;

	@PostConstruct
	public void init() {
		rekClient = RekognitionClient.builder().build();
	}

	@Override
	public IndexedFace indexFaces(String collectionId, byte[] faceImage) {
		SdkBytes imagebytes = SdkBytes.fromByteArray(faceImage);
		Image image = Image.builder().bytes(imagebytes).build();
		IndexFacesRequest request = IndexFacesRequest.builder().collectionId(collectionId).image(image).maxFaces(1)
				.qualityFilter(QualityFilter.AUTO).detectionAttributes(Attribute.DEFAULT).build();

		IndexFacesResponse facesResponse = rekClient.indexFaces(request);

		// Display the results.
		List<FaceRecord> faceRecords = facesResponse.faceRecords();
		for (FaceRecord faceRecord : faceRecords) {
			log.info("  登録Face ID: " + faceRecord.face().faceId());
			log.info("  登録Location:" + faceRecord.faceDetail().boundingBox().toString());
		}

		List<UnindexedFace> unindexedFaces = facesResponse.unindexedFaces();
		for (UnindexedFace unindexedFace : unindexedFaces) {
			log.info("  未登録Location:" + unindexedFace.faceDetail().boundingBox().toString());
			for (Reason reason : unindexedFace.reasons()) {
				log.info("未登録Reason:  " + reason);
			}
		}
		IndexedFace ret = null;
		if (faceRecords.size() > 0) {
			var face = faceRecords.get(0).face();
			var boundingBox = faceRecords.get(0).faceDetail().boundingBox();
			ret = IndexedFace.builder().faceId(face.faceId()).boundingHeight(boundingBox.height())
					.boundingWidth(boundingBox.width()).boundingLeft(boundingBox.left()).boundingTop(boundingBox.top())
					.build();
		}
		return ret;
	}

	@Override
	public void createCollection(String collectionId) {
		log.info("コレクション作成: " + collectionId);

		CreateCollectionRequest collectionRequest = CreateCollectionRequest.builder().collectionId(collectionId)
				.build();
		try {
			CreateCollectionResponse collectionResponse = rekClient.createCollection(collectionRequest);
			log.info("CollectionArn : " + collectionResponse.collectionArn());
			log.info("Status code : " + collectionResponse.statusCode().toString());
		} catch (ResourceAlreadyExistsException ignore) {
			//
		}

	}

	@Override
	public void deleteCollection(String collectionId) {
		log.info("コレクション削除: " + collectionId);

		DeleteCollectionRequest deleteCollectionRequest = DeleteCollectionRequest.builder().collectionId(collectionId)
				.build();
		try {
			DeleteCollectionResponse deleteCollectionResponse = rekClient.deleteCollection(deleteCollectionRequest);
		} catch (ResourceNotFoundException ignore) {
			//
		}
	}

	@Override
	public List<String> listAllCollections() {

		ListCollectionsRequest listCollectionsRequest = ListCollectionsRequest.builder().maxResults(10).build();

		ListCollectionsResponse response = rekClient.listCollections(listCollectionsRequest);
		List<String> collectionIds = response.collectionIds();
		for (String resultId : collectionIds) {
			log.info(resultId);
		}
		return collectionIds;
	}

	@Override
	public void listFacesCollection(String collectionId) {

		ListFacesRequest facesRequest = ListFacesRequest.builder().collectionId(collectionId).maxResults(10).build();
		ListFacesResponse facesResponse = rekClient.listFaces(facesRequest);

		// For each face in the collection, print out the confidence level and face id
		// value.
		List<Face> faces = facesResponse.faces();
		for (Face face : faces) {
			log.info("Confidence level there is a face: " + face.confidence());
			log.info("The face Id value is " + face.faceId());
		}
	}

	@Override
	public List<MatchInfo> searchFaceByImage(String collectionId, byte[] sourceImage, Integer maxFaces,
			Float matchThreshold) {
		try {
			SdkBytes sourceBytes = SdkBytes.fromByteArray(sourceImage);
			Image souImage = Image.builder().bytes(sourceBytes).build();

			SearchFacesByImageRequest facesByImageRequest = SearchFacesByImageRequest.builder().image(souImage)
					.maxFaces(maxFaces).faceMatchThreshold(matchThreshold).collectionId(collectionId).build();

			SearchFacesByImageResponse imageResponse = rekClient.searchFacesByImage(facesByImageRequest);

			List<FaceMatch> faceImageMatches = imageResponse.faceMatches();
			List<MatchInfo> ret = new ArrayList<>();
			// 型を入れ直す
			for (FaceMatch face : faceImageMatches) {
				MatchInfo info = MatchInfo.builder().build();
				info.setSimilarity(face.similarity());
				info.setFaceId(face.face().faceId());
				info.setHeight(face.face().boundingBox().height());
				info.setWidth(face.face().boundingBox().width());
				info.setLeft(face.face().boundingBox().left());
				info.setTop(face.face().boundingBox().top());
				ret.add(info);
			}
			return ret;
		} catch (ResourceNotFoundException | InvalidParameterException ignore) {
			log.error("{}", ignore.toString());
		}
		return new ArrayList<>();
	}

	@PreDestroy
	public void destroy() {
		rekClient.close();
	}

}
