package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.CarReview;
import com.autocare.autocarebackend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewDetailsImpl {

    @Autowired
    private ReviewRepository reviewRepository;

    // Save review (automatically sets flag = 0 for Pending)
    public CarReview saveReview(CarReview review) {
        return reviewRepository.save(review);
    }

    // Get all approved reviews (flag = 1)
    public List<CarReview> getApprovedReviews() {
        return reviewRepository.getApprovedReviews();
    }

    // Get all pending reviews (flag = 0) for admin
    public List<CarReview> getPendingReviews() {
        return reviewRepository.getPendingReviews();
    }

    // Get review by ID
    public Optional<CarReview> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    // Update review
    public CarReview updateReview(CarReview review) {
        return reviewRepository.save(review);
    }

    // Delete review
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    // Approve review (admin) - Sets flag = 1
    public CarReview approveReview(Long id) {
        Optional<CarReview> opt = reviewRepository.findById(id);
        if (opt.isPresent()) {
            CarReview review = opt.get();
            review.setFlag(1); // Approved
            return reviewRepository.save(review);
        }
        return null;
    }

    // Reject review (admin) - Sets flag = -1 (Rejected)
    public CarReview rejectReview(Long id) {
        Optional<CarReview> opt = reviewRepository.findById(id);
        if (opt.isPresent()) {
            CarReview review = opt.get();
            review.setFlag(-1); // Mark as rejected (not deleted)
            return reviewRepository.save(review);
        }
        return null;
    }

    // Increment helpful count
    public CarReview incrementHelpfulCount(Long id) {
        Optional<CarReview> opt = reviewRepository.findById(id);
        if (opt.isPresent()) {
            CarReview review = opt.get();
            review.incrementHelpfulCount();
            return reviewRepository.save(review);
        }
        return null;
    }

    // Increment view count
    public CarReview incrementViewCount(Long id) {
        Optional<CarReview> opt = reviewRepository.findById(id);
        if (opt.isPresent()) {
            CarReview review = opt.get();
            review.incrementViewCount();
            return reviewRepository.save(review);
        }
        return null;
    }
}