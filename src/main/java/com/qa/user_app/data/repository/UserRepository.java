package com.qa.user_app.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qa.user_app.data.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>{

	
	
}
