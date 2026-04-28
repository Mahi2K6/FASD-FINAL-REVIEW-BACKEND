package com.medconnect.backend.controller;

import com.medconnect.backend.model.Review;
import com.medconnect.backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Review review, java.security.Principal principal) {
        System.out.println("Review Save Started");
        System.out.println("Payload: " + review);
        try {
            Review saved = reviewService.addReview(review, principal.getName().trim().toLowerCase());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Review saved successfully",
                    "reviewId", saved.getId()
            ));
        } catch (IllegalArgumentException ex) {
            System.err.println("Validation Error: " + ex.getMessage());
            if (ex.getMessage() != null && ex.getMessage().contains("already submitted")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "error", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            System.err.println("Access Denied: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (com.medconnect.backend.exception.ResourceNotFoundException ex) {
            System.err.println("Not Found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            System.err.println("Internal Server Error: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Internal server error: " + ex.getMessage()));
        }
    }
}
