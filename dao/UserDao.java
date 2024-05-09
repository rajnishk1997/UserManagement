package com.optum.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optum.entity.User;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {
	Optional<User> findByUserName(String userName);

	List<User> findByUserNameContainingIgnoreCaseOrUserFirstNameContainingIgnoreCase(String keyword, String keyword2);

	List<User> findByUserFirstName(String userFirstName);
}
