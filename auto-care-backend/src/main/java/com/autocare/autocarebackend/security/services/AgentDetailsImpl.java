package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.User;
import com.autocare.autocarebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class AgentDetailsImpl {
    @Autowired
    private UserRepository userRepository;

    public User editNormalUserEditProfile(User user){
        System.out.println(user.getFname());
        return userRepository.save(user);
    }
}
