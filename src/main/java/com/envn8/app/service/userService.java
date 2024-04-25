package com.envn8.app.service;

import com.envn8.app.models.User;
import com.envn8.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class userService {

    private final UserRepository userRepository;

    @Autowired
    public userService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }   

    // public User getUserByEmail(String email) {
    //     return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    // }
}