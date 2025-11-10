package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.Advertisement;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Advertisement, Long> {

    @Override
    Optional<Advertisement> findById(Long id);

    boolean existsById(Long id);

    @Override
    List<Advertisement> findAll();

    List<Advertisement> findByUser(User user);

    List<Advertisement> findAllByUser(User user);

    // Get all approved advertisements (flag = 1)
    @Query(value = "SELECT u FROM Advertisement u WHERE u.flag = 1")
    List<Advertisement> getConfirmAd();

    // Get all pending advertisements (flag = 0)
    @Query(value = "SELECT u FROM Advertisement u WHERE u.flag = 0")
    List<Advertisement> getPendingAd();

    // Count remaining/pending ads for a user (flag = 0)
    @Query(value = "SELECT COUNT(a.id) FROM Advertisement a WHERE a.user = :user AND a.flag = 0")
    Long rcount(@Param("user") User user);

    // Count posted/approved ads for a user (flag = 1)
    @Query(value = "SELECT COUNT(a.id) FROM Advertisement a WHERE a.user = :user AND a.flag = 1")
    Long pcount(@Param("user") User user);

    // Get ads not in IPlan for a user
    @Query(value = "SELECT a FROM Advertisement a WHERE a.user.id = :uid AND a.id NOT IN (SELECT i.advertisement.id FROM IPlan i WHERE i.user.id = :uid)")
    List<Advertisement> getIPendingAd(@Param("uid") Long uid);

    // Get ads in IPlan for a user
    @Query(value = "SELECT a FROM Advertisement a WHERE a.user.id = :uid AND a.id IN (SELECT i.advertisement.id FROM IPlan i WHERE i.user.id = :uid)")
    List<Advertisement> getIConfrimAd(@Param("uid") Long uid);

    // Get ads not in LPlan for a user
    @Query(value = "SELECT a FROM Advertisement a WHERE a.user.id = :uid AND a.id NOT IN (SELECT l.advertisement.id FROM LPlan l WHERE l.user.id = :uid)")
    List<Advertisement> getLPendingAd(@Param("uid") Long uid);

    // Get ads in LPlan for a user
    @Query(value = "SELECT a FROM Advertisement a WHERE a.user.id = :uid AND a.id IN (SELECT l.advertisement.id FROM LPlan l WHERE l.user.id = :uid)")
    List<Advertisement> getLConfrimAd(@Param("uid") Long uid);

    // 1. Count vehicles by manufacturer
    @Query("SELECT a.manufacturer, COUNT(a) FROM Advertisement a WHERE a.flag = 1 GROUP BY a.manufacturer")
    List<Object[]> countByManufacturer();

    // 2. Count vehicles by type
    @Query("SELECT a.v_type, COUNT(a) FROM Advertisement a WHERE a.flag = 1 GROUP BY a.v_type")
    List<Object[]> countByVehicleType();

    // 3. Count vehicles by fuel type
    @Query("SELECT a.fuel_type, COUNT(a) FROM Advertisement a WHERE a.flag = 1 GROUP BY a.fuel_type")
    List<Object[]> countByFuelType();

    // 4. Count vehicles by condition
    @Query("SELECT a.v_condition, COUNT(a) FROM Advertisement a WHERE a.flag = 1 GROUP BY a.v_condition")
    List<Object[]> countByCondition();

    // 5. Get listings created in last N days
    @Query("SELECT a FROM Advertisement a WHERE a.datetime >= :startDate ORDER BY a.datetime DESC")
    List<Advertisement> findRecentListings(@Param("startDate") Date startDate);

    // 6. Count by status flags
    @Query("SELECT " +
            "SUM(CASE WHEN a.lStatus = 1 THEN 1 ELSE 0 END) as withLeasing, " +
            "SUM(CASE WHEN a.iStatus = 1 THEN 1 ELSE 0 END) as withInsurance, " +
            "SUM(CASE WHEN a.lStatus = 1 AND a.iStatus = 1 THEN 1 ELSE 0 END) as withBoth " +
            "FROM Advertisement a WHERE a.flag = 1")
    Object[] countByStatus();

    // 7. Monthly statistics
    @Query("SELECT " +
            "YEAR(a.datetime) as year, " +
            "MONTH(a.datetime) as month, " +
            "COUNT(a) as count, " +
            "AVG(CAST(a.price AS double)) as avgPrice " +
            "FROM Advertisement a " +
            "WHERE a.datetime >= :startDate " +
            "GROUP BY YEAR(a.datetime), MONTH(a.datetime) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyStatistics(@Param("startDate") Date startDate);

    // 8. Top manufacturers with average price
    @Query("SELECT a.manufacturer, COUNT(a), AVG(CAST(a.price AS double)) " +
            "FROM Advertisement a " +
            "WHERE a.flag = 1 AND a.manufacturer IS NOT NULL " +
            "GROUP BY a.manufacturer " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> getTopManufacturersWithPrice();
}