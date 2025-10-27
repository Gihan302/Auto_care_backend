package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdDetailsImpl {

    @Autowired
    private AdRepository adRepository;

    public Advertisement saveAdDetails(Advertisement advertisement) {
        return adRepository.save(advertisement);
    }
}
