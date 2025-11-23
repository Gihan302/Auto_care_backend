package com.autocare.autocarebackend.service;

import com.autocare.autocarebackend.models.AgentApplicationDTO;
import com.autocare.autocarebackend.repository.AgentApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentApplicationService {

    private final AgentApplicationRepository agentApplicationRepository;

    public List<AgentApplicationDTO> getApplicationsForAgent(Long agentId) {
        return agentApplicationRepository.findApplicationsByAgentId(agentId);
    }
}
