package com.example.faceauth;

import java.time.LocalDateTime;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.faceauth.entity.Face;
import com.example.faceauth.rekognition.FaceRekognition;
import com.example.faceauth.rekognition.FaceRekognition.MatchInfo;
import com.example.faceauth.repository.FaceRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Blogic {
	@Autowired
	private FaceRekognition faceApi;
	@Autowired
	private FaceRepository faceRepository;

	@Value("${app.truncateOnBoot}")
	private boolean truncateOnBoot;

	@PostConstruct
	public void init() {
		log.info("truncateOnBoot={}", truncateOnBoot);
		if (truncateOnBoot) {
			var list = faceApi.listAllCollections();
			for (String collectionId : list) {
				faceApi.deleteCollection(collectionId);
				faceRepository.deleteByAccountId(collectionId);
			}
		}
	}

	public Face getFace(Face face) {
		try {
			log.info("getface called. face={}", face);
			var ret = faceRepository.findByAccountId(face.getAccountId());
			ret.setOK(true);
			return ret;
		} catch (Exception e) {
			log.error("", e);
			var ret = Face.builder().build();
			ret.setOK(false);
			ret.setErrorDetail(e.toString());
			return ret;
		}
	}

	public Face registFace(Face face) {
		log.info("registface called. {}", face);
		try {
			var accountId = face.getAccountId();
			// コレクション削除
			faceApi.deleteCollection(accountId);
			faceRepository.deleteByAccountId(accountId);
			// コレクション作成
			faceApi.createCollection(face.getAccountId());
			// 顔登録
			byte[] bytes = Base64.getDecoder().decode(face.getFaceImage().getBytes());
			var indexedFace = faceApi.indexFaces(face.getAccountId(), bytes);
			face.setAccountId(accountId);
			face.setFaceId(indexedFace.getFaceId());
			face.setBoundingHeight(indexedFace.getBoundingHeight());
			face.setBoundingWidth(indexedFace.getBoundingWidth());
			face.setBoundingLeft(indexedFace.getBoundingLeft());
			face.setBoundingTop(indexedFace.getBoundingTop());
			face.setCreated(LocalDateTime.now());
			faceRepository.save(face);
			face.setOK(true);
			return face;
		} catch (Exception e) {
			log.error("", e);
			var ret = Face.builder().build();
			ret.setOK(false);
			ret.setErrorDetail(e.toString());
			return ret;
		}
	}

	@Data
	public static class SearchRequest {
		private String accountId;
		private String image;
		private Integer maxFaces;
		private Float threshold;
	}

	public BlogicResponse login(Face req) {
		var ret = new BlogicResponse();
		try {
			byte[] bytes = Base64.getDecoder().decode(req.getFaceImage().getBytes());
			// 99.0%以上の一致率の顔を取得
			var faces = faceApi.searchFaceByImage(req.getAccountId(), bytes, 1, 99.0f);
			for (MatchInfo matchInfo : faces) {
				log.info("{}", matchInfo);
			}
			ret.setOK(faces.size() > 0);
		} catch (Exception e) {
			log.error("", e);
			ret.setOK(false);
			ret.setErrorDetail(e.toString());
		}
		return ret;
	}
}
