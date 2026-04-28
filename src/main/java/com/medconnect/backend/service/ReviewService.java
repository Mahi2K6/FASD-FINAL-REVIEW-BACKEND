package com.medconnect.backend.service;

import com.medconnect.backend.model.Review;

public interface ReviewService {
    Review addReview(Review review, String patientEmail);
}
