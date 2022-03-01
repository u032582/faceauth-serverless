package com.example.faceauth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.faceauth.entity.Account;
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

//	@DeleteMapping("/accounts")
	public void deleteAccount(Account account) {
		faceApi.deleteCollection(account.getAccountId());
		faceRepository.deleteByAccountId(account.getAccountId());
	}

//	@GetMapping("/getfaces")
	public List<Face> getFace(Account account) {
		try {
			log.info("getfaces called.");
			return faceRepository.findByAccountId(account.getAccountId());
		} catch (Exception e) {
			log.error("", e);
		}
		return List.of();
	}

//	@PostMapping("/registface")
	public Face registFace(Face face) {
		log.info("registface called.");
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
		return face;
	}

	@Data
	public static class SearchRequest {
		private String accountId;
		private String image;
		private Integer maxFaces;
		private Float threshold;
	}

//	@PostMapping("/login")
	public boolean login(Face req) {
		try {
			byte[] bytes = Base64.getDecoder().decode(req.getFaceImage().getBytes());
			var faces = faceApi.searchFaceByImage(req.getAccountId(), bytes, 1, 99.0f);
			for (MatchInfo matchInfo : faces) {
				log.info("{}", matchInfo);
			}
			return faces.size() > 0;
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}

	private static Date toDate(LocalDateTime localDateTime) {
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zone);
		Instant instant = zonedDateTime.toInstant();
		return Date.from(instant);
	}
}
