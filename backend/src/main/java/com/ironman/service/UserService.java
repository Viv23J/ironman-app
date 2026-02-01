package com.ironman.service;

import com.ironman.model.User;
import com.ironman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        log.info("Fetching all users from database");
        return userRepository.findAll();
    }

    public long getUserCount() {
        long count = userRepository.count();
        log.info("Total users in database: {}", count);
        return count;
    }
}