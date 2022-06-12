package com.gydronium.storedemo.dao;

import com.gydronium.storedemo.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<UserModel, Long> {
}
