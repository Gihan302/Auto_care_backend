package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.AgentApplicationDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class AgentApplicationRepository {

    private static final Logger log = LoggerFactory.getLogger(AgentApplicationRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public List<AgentApplicationDTO> findApplicationsByAgentId(Long agentId) {
        String leasingQuery = """
            SELECT
                la.id,
                la.full_name,
                la.email,
                la.phone,
                la.address,
                la.income,
                la.employment_status,
                la.status,
                la.submitted_at,
                'Leasing' as application_type,
                a.title as ad_title,
                a.id as ad_id
            FROM leasing_applications la
            JOIN leasing_plans lp ON la.plan_id = lp.id
            JOIN advertisement a ON lp.ad_id = a.id
            WHERE a.user_id = ?
        """;

        String insuranceQuery = """
            SELECT
                ia.id,
                ia.full_name,
                ia.email,
                ia.phone,
                ia.address,
                ia.status,
                ia.submitted_at,
                'Insurance' as application_type,
                a.title as ad_title,
                a.id as ad_id
            FROM insurance_applications ia
            JOIN insurance_plans ip ON ia.plan_id = ip.id
            JOIN advertisement a ON ip.ad_id = a.id
            WHERE a.user_id = ?
        """;

        List<AgentApplicationDTO> leasingApps = jdbcTemplate.query(
                leasingQuery,
                new Object[]{agentId},
                (rs, rowNum) -> new AgentApplicationDTO(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("income"),
                        rs.getString("employment_status"),
                        rs.getString("status"),
                        rs.getTimestamp("submitted_at"),
                        rs.getString("application_type"),
                        rs.getString("ad_title"),
                        rs.getLong("ad_id")
                )
        );
        log.info("Found {} leasing applications for agent ID: {}", leasingApps.size(), agentId);


        List<AgentApplicationDTO> insuranceApps = jdbcTemplate.query(
                insuranceQuery,
                new Object[]{agentId},
                (rs, rowNum) -> new AgentApplicationDTO(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        null, // income is not available for insurance applications
                        null, // employment_status is not available for insurance applications
                        rs.getString("status"),
                        rs.getTimestamp("submitted_at"),
                        rs.getString("application_type"),
                        rs.getString("ad_title"),
                        rs.getLong("ad_id")
                )
        );
        log.info("Found {} insurance applications for agent ID: {}", insuranceApps.size(), agentId);

        return Stream.concat(leasingApps.stream(), insuranceApps.stream()).toList();
    }
}
