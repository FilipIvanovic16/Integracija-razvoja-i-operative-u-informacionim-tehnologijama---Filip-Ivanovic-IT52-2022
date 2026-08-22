package com.chronoshop.auth.repository;

import com.chronoshop.auth.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Page<User> findByEmailContainingIgnoreCaseOrLastNameContainingIgnoreCase(
      String email, String lastName, Pageable pageable);
}
