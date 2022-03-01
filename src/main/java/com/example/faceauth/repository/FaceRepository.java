package com.example.faceauth.repository;

import java.util.List;

import com.example.faceauth.entity.Face;

public interface FaceRepository {
	void deleteByAccountId(String accountId);

	List<Face> findByAccountId(String accountId);

	void save(Face face);

}