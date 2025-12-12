package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.AgentApplicationDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class AgentApplicationRepository {

    private static final Logger log = LoggerFactory.getLogger(AgentApplicationRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public Page<AgentApplicationDTO> findApplicationsByAgentId(Long agentId, String status, String search, int page, String[] sort) {
        log.info("Executing findApplicationsByAgentId for agent: {}", agentId);

        // 1. Setup Pagination and Sorting
        String sortField = sort[0];
        String sortDirection = sort[1];

        String sqlSortField = sortField.equals("submittedAt") ? "submitted_at" : sortField;

        Sort sortable = Sort.by(Sort.Direction.fromString(sortDirection), sqlSortField);
        Pageable pageable = PageRequest.of(page, 10, sortable);

        List<Object> filterParams = new ArrayList<>();
        filterParams.add(agentId);

        if (StringUtils.hasText(status)) {
            filterParams.add(status);
        }

        if (StringUtils.hasText(search)) {
            String searchPattern = "%" + search + "%";
            filterParams.add(searchPattern);
            filterParams.add(searchPattern);
        }

        // --- LEASING APPLICATIONS QUERY ---
        // FIX: Added la.cover_letter to the SELECT list
        StringBuilder leasingQuery = new StringBuilder("""
            SELECT
                la.id,
                la.full_name,
                la.email,
                la.phone,
                la.address,
                la.income,
                la.employment_status,
                la.cover_letter,    -- <--- NEW FIELD ADDED TO QUERY
                la.status,
                la.submitted_at,
                'Leasing' as application_type,
                a.title as ad_title,
                a.id as ad_id,
                lp.plan_name,
                la.credit_score
            FROM leasing_applications la
            JOIN leasing_plans lp ON la.plan_id = lp.id
            JOIN advertisement a ON lp.ad_id = a.id
            WHERE a.user_id = ?
        """);

        StringBuilder countLeasingQuery = new StringBuilder("""
            SELECT count(*)
            FROM leasing_applications la
            JOIN leasing_plans lp ON la.plan_id = lp.id
            JOIN advertisement a ON lp.ad_id = a.id
            WHERE a.user_id = ?
        """);

        // ... (Status and Search filters remain the same)
        if (StringUtils.hasText(status)) {
            leasingQuery.append(" AND la.status = ?");
            countLeasingQuery.append(" AND la.status = ?");
        }

        if (StringUtils.hasText(search)) {
            leasingQuery.append(" AND (LOWER(la.full_name) LIKE LOWER(?) OR LOWER(la.email) LIKE LOWER(?))");
            countLeasingQuery.append(" AND (LOWER(la.full_name) LIKE LOWER(?) OR LOWER(la.email) LIKE LOWER(?))");
        }

        List<Object> leasingDataParams = new ArrayList<>(filterParams);

        leasingQuery.append(" ORDER BY ").append(sqlSortField).append(" ").append(sortDirection);
        leasingQuery.append(" LIMIT ? OFFSET ?");
        leasingDataParams.add(pageable.getPageSize());
        leasingDataParams.add(pageable.getOffset());

        // Row Mapper is now correct since the query and DTO constructor align
        List<AgentApplicationDTO> leasingApps = jdbcTemplate.query(
                leasingQuery.toString(),
                leasingDataParams.toArray(),
                (rs, rowNum) -> new AgentApplicationDTO(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("income"),
                        rs.getString("employment_status"),
                        rs.getString("cover_letter"), // Now retrieved from the query
                        rs.getString("status"),
                        rs.getTimestamp("submitted_at"),
                        rs.getString("application_type"),
                        rs.getString("ad_title"),
                        rs.getLong("ad_id"),
                        rs.getString("plan_name"),
                        rs.getString("credit_score")
                )
        );
        log.info("Found {} leasing applications for agent ID: {}", leasingApps.size(), agentId);

        // --- INSURANCE APPLICATIONS QUERY ---

        // ... (Insurance Query remains the same)
        StringBuilder insuranceQuery = new StringBuilder("""
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
                a.id as ad_id,
                ip.plan_name
            FROM insurance_applications ia
            JOIN insurance_plans ip ON ia.plan_id = ip.id
            JOIN advertisement a ON ip.ad_id = a.id
            WHERE a.user_id = ?
        """);

        StringBuilder countInsuranceQuery = new StringBuilder("""
            SELECT count(*)
            FROM insurance_applications ia
            JOIN insurance_plans ip ON ia.plan_id = ip.id
            JOIN advertisement a ON ip.ad_id = a.id
            WHERE a.user_id = ?
        """);

        // ... (Status and Search filters remain the same)
        if (StringUtils.hasText(status)) {
            insuranceQuery.append(" AND ia.status = ?");
            countInsuranceQuery.append(" AND ia.status = ?");
        }

        if (StringUtils.hasText(search)) {
            insuranceQuery.append(" AND (LOWER(ia.full_name) LIKE LOWER(?) OR LOWER(ia.email) LIKE LOWER(?))");
            countInsuranceQuery.append(" AND (LOWER(ia.full_name) LIKE LOWER(?) OR LOWER(ia.email) LIKE LOWER(?))");
        }

        List<Object> insuranceDataParams = new ArrayList<>(filterParams);

        insuranceQuery.append(" ORDER BY ").append(sqlSortField).append(" ").append(sortDirection);
        insuranceQuery.append(" LIMIT ? OFFSET ?");
        insuranceDataParams.add(pageable.getPageSize());
        insuranceDataParams.add(pageable.getOffset());

        // FIX: Added 'null' for income, employmentStatus, coverLetter, and creditScore
        // to match the 15-argument DTO constructor.
        List<AgentApplicationDTO> insuranceApps = jdbcTemplate.query(
                insuranceQuery.toString(),
                insuranceDataParams.toArray(),
                (rs, rowNum) -> new AgentApplicationDTO(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        null, // income (index 6 - not in insurance query)
                        null, // employmentStatus (index 7 - not in insurance query)
                        null, // coverLetter (index 8 - NOT IN INSURANCE QUERY)
                        rs.getString("status"),
                        rs.getTimestamp("submitted_at"),
                        rs.getString("application_type"),
                        rs.getString("ad_title"),
                        rs.getLong("ad_id"),
                        rs.getString("plan_name"),
                        null // creditScore (index 15 - not in insurance query)
                )
        );
        log.info("Found {} insurance applications for agent ID: {}", insuranceApps.size(), agentId);

        // ... (Count and return logic remains the same)
        Integer totalLeasing = jdbcTemplate.queryForObject(countLeasingQuery.toString(), Integer.class, filterParams.toArray());
        Integer totalInsurance = jdbcTemplate.queryForObject(countInsuranceQuery.toString(), Integer.class, filterParams.toArray());

        long total = (totalLeasing != null ? totalLeasing : 0) + (totalInsurance != null ? totalInsurance : 0);

        List<AgentApplicationDTO> allApps = Stream.concat(leasingApps.stream(), insuranceApps.stream()).toList();

        return new PageImpl<>(allApps, pageable, total);
    }
}