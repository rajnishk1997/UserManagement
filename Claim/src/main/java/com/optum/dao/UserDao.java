package com.optum.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optum.entity.User;

@Repository
public interface UserDao extends JpaRepository<User, String> {
	Optional<User> findByUserName(String userName);
}
