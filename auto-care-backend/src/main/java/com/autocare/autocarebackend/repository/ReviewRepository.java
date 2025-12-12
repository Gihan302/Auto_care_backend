package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.CarReview;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<CarReview, Long> {

    // Get all approved reviews (flag = 1)
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 ORDER BY r.createdAt DESC")
    List<CarReview> getApprovedReviews();

    // Get all pending reviews (flag = 0) for admin approval
    @Query("SELECT r FROM CarReview r WHERE r.flag = 0 ORDER BY r.createdAt DESC")
    List<CarReview> getPendingReviews();

    // Get reviews by user
    List<CarReview> findByUser(User user);

    // Get reviews by specific car (make and model)
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 AND LOWER(r.carMake) = LOWER(:make) AND LOWER(r.carModel) = LOWER(:model) ORDER BY r.createdAt DESC")
    List<CarReview> findByCarMakeAndModelApproved(@Param("make") String make, @Param("model") String model);

    // Search reviews by car make or model
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 AND (LOWER(r.carMake) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.carModel) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY r.createdAt DESC")
    List<CarReview> searchReviews(@Param("search") String search);

    // Get reviews with minimum rating
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 AND r.overallRating >= :rating ORDER BY r.createdAt DESC")
    List<CarReview> findByMinimumRating(@Param("rating") Double rating);

    // Get verified owner reviews only
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 AND r.verifiedOwner = true ORDER BY r.createdAt DESC")
    List<CarReview> findVerifiedOwnerReviews();

    // Get most helpful reviews
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 ORDER BY r.helpfulCount DESC, r.createdAt DESC")
    List<CarReview> findMostHelpfulReviews();

    // Get highest rated reviews
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 ORDER BY r.overallRating DESC, r.createdAt DESC")
    List<CarReview> findHighestRatedReviews();

    // Get lowest rated reviews
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 ORDER BY r.overallRating ASC, r.createdAt DESC")
    List<CarReview> findLowestRatedReviews();

    // Count total approved reviews
    @Query("SELECT COUNT(r) FROM CarReview r WHERE r.flag = 1")
    Long countApprovedReviews();

    // Count reviews by user
    @Query("SELECT COUNT(r) FROM CarReview r WHERE r.user = :user")
    Long countByUser(@Param("user") User user);

    // Count pending reviews by user
    @Query("SELECT COUNT(r) FROM CarReview r WHERE r.user = :user AND r.flag = 0")
    Long countPendingByUser(@Param("user") User user);

    // Count approved reviews by user
    @Query("SELECT COUNT(r) FROM CarReview r WHERE r.user = :user AND r.flag = 1")
    Long countApprovedByUser(@Param("user") User user);

    // Get average rating for all approved reviews
    @Query("SELECT AVG(r.overallRating) FROM CarReview r WHERE r.flag = 1")
    Double getAverageRating();

    // Count verified owners percentage
    @Query("SELECT COUNT(r) FROM CarReview r WHERE r.flag = 1 AND r.verifiedOwner = true")
    Long countVerifiedOwners();

    // Count reviews created this month
    @Query("SELECT COUNT(r) FROM CarReview r WHERE r.flag = 1 AND MONTH(r.createdAt) = MONTH(CURRENT_DATE) AND YEAR(r.createdAt) = YEAR(CURRENT_DATE)")
    Long countReviewsThisMonth();

    // Get reviews by car make
    @Query("SELECT r FROM CarReview r WHERE r.flag = 1 AND LOWER(r.carMake) = LOWER(:make) ORDER BY r.createdAt DESC")
    List<CarReview> findByCarMake(@Param("make") String make);

    // Get rejected reviews (flag = -1)
    @Query("SELECT r FROM CarReview r WHERE r.flag = -1 ORDER BY r.createdAt DESC")
    List<CarReview> getRejectedReviews();
    // Get unique car makes
    @Query("SELECT DISTINCT r.carMake FROM CarReview r WHERE r.flag = 1 ORDER BY r.carMake")
    List<String> findDistinctCarMakes();
}