package com.example.faceauth.repository;

import com.example.faceauth.entity.Account;

public interface AccountRepository {
	void deleteByAccountId(String accountId);

	void save(Account account);
}
