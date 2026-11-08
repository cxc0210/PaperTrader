package com.andre.papertrader.repository;

import com.andre.papertrader.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
