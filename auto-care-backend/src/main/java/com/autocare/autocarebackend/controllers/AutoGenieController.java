package com.autocare.autocarebackend.controllers;

import com.autocare.autocarebackend.payload.request.AutoGenieRequest;
import com.autocare.autocarebackend.payload.response.AutoGenieResponse;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/autogenie")
public class AutoGenieController {

    @PostMapping("/chat")
    public AutoGenieResponse chat(@RequestBody AutoGenieRequest request) {
        // Simple echo bot for now
        return new AutoGenieResponse("You said: " + request.getQuery());
    }
}
