package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDetailsImpl {
    @Autowired
    private UserRepository userRepository;
    public User editAdminDetails(User user){
        System.out.println(user.getFname());
        return userRepository.save(user);
    }
}
