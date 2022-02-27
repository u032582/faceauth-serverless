package com.example.faceauth.function;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.faceauth.AccountRepository;
import com.example.faceauth.FaceRepository;
import com.example.faceauth.entity.Account;
import com.example.faceauth.entity.Face;
import com.example.faceauth.rekognition.FaceRekognition;
import com.example.faceauth.rekognition.FaceRekognition.MatchInfo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Blogic {
//	@Autowired
	private FaceRekognition faceApi;
//	@Autowired
	private FaceRepository faceRepository;
//	@Autowired
	private AccountRepository accountRepository;

	@Value("${app.truncateOnBoot}")
	private boolean truncateOnBoot;

	@PostConstruct
	public void init() {
		log.info("truncateOnBoot={}", truncateOnBoot);
		if (truncateOnBoot) {
			var list = faceApi.listAllCollections();
			for (String collectionId : list) {
				faceApi.deleteCollection(collectionId);
			}
			// faceRepository.deleteAll();
		}
	}

//	@DeleteMapping("/accounts")
	public void deleteAccount(String id) {
//		faceRepository.deleteByAccountId(accountId);
//		accountRepository.deleteByAccountId(accountId);
	}

//	@GetMapping("/getfaces")
	public List<Face> getFace(String accountId) {
		return null;// faceRepository.findByAccountId(accountId);
	}

//	@PostMapping("/registface")
	public Face registFace(Face face) {

		var accountId = face.getAccount().getAccountId();
		// コレクション削除
		faceApi.deleteCollection(accountId);
		// faceRepository.deleteByAccountId(accountId);
		// コレクション作成
		Account account = null;
		// accountRepository.save(Account.builder().accountId(accountId).build());
		faceApi.createCollection(face.getAccount().getAccountId());
		// 顔登録
		byte[] bytes = Base64.getDecoder().decode(face.getFaceImage().getBytes());
		var indexedFace = faceApi.indexFaces(face.getAccount().getAccountId(), bytes);
		face.setAccount(account);
		face.setFaceId(indexedFace.getFaceId());
		face.setBoundingHeight(indexedFace.getBoundingHeight());
		face.setBoundingWidth(indexedFace.getBoundingWidth());
		face.setBoundingLeft(indexedFace.getBoundingLeft());
		face.setBoundingTop(indexedFace.getBoundingTop());
		face.setCreated(toDate(LocalDateTime.now()));
		// faceRepository.save(face);
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
		byte[] bytes = Base64.getDecoder().decode(req.getFaceImage().getBytes());
		var faces = faceApi.searchFaceByImage(req.getAccount().getAccountId(), bytes, 1, 99.0f);
		for (MatchInfo matchInfo : faces) {
			log.info("{}", matchInfo);
		}
		return faces.size() > 0;
	}

	static Date toDate(LocalDateTime localDateTime) {
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zone);
		Instant instant = zonedDateTime.toInstant();
		return Date.from(instant);
	}
}
