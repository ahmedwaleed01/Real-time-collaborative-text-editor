package com.envn8.app.repository;

import com.envn8.app.models.*;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);
  
    Boolean existsByEmail(String email);   
}
