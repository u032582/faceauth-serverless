package com.example.faceauth.repository;

import com.example.faceauth.entity.Face;

public interface FaceRepository {
	void deleteByAccountId(String accountId);

	Face findByAccountId(String accountId);

	void save(Face face);

}