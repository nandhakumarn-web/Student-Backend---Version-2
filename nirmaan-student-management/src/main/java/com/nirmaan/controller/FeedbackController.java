package com.nirmaan.controller;

import com.nirmaan.dto.*;
import com.nirmaan.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feedback", description = "Feedback management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    @Operation(summary = "Get all feedback", description = "Retrieve all feedback with filtering options")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "200", description = "Feedback retrieved successfully"),
        @SwaggerResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Page<FeedbackDto>>> getAllFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer rating) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<FeedbackDto> feedback = feedbackService.getAllFeedback(pageable, feedbackType, trainerId, courseId, rating);
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback retrieved successfully", feedback));
    }

    @GetMapping("/trainer/{trainerId}")
    @Operation(summary = "Get feedback by trainer", description = "Retrieve feedback for a specific trainer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<Page<FeedbackDto>>> getFeedbackByTrainer(
            @PathVariable Long trainerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) Integer rating) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<FeedbackDto> feedback = feedbackService.getFeedbackByTrainer(trainerId, pageable, feedbackType, rating);
        return ResponseEntity.ok(new ApiResponse<>(true, "Trainer feedback retrieved successfully", feedback));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get feedback by course", description = "Retrieve feedback for a specific course")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<Page<FeedbackDto>>> getFeedbackByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<FeedbackDto> feedback = feedbackService.getFeedbackByCourse(courseId, pageable, rating);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course feedback retrieved successfully", feedback));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get feedback analytics", description = "Get feedback analytics and statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackAnalyticsDto>> getFeedbackAnalytics(
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate) {
        
        FeedbackAnalyticsDto analytics = feedbackService.getFeedbackAnalytics(startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback analytics retrieved successfully", analytics));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get feedback summary", description = "Get feedback summary statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<FeedbackSummaryDto>> getFeedbackSummary(
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false) Long courseId) {
        
        FeedbackSummaryDto summary = feedbackService.getFeedbackSummary(trainerId, courseId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback summary retrieved successfully", summary));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get feedback trends", description = "Get feedback trends over time")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<List<FeedbackTrendDto>>> getFeedbackTrends(
            @RequestParam @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false) Long courseId) {
        
        List<FeedbackTrendDto> trends = feedbackService.getFeedbackTrends(startDate, endDate, trainerId, courseId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback trends retrieved successfully", trends));
    }

    @GetMapping("/ratings/distribution")
    @Operation(summary = "Get rating distribution", description = "Get distribution of ratings")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<RatingDistributionDto>> getRatingDistribution(
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false) Long courseId) {
        
        RatingDistributionDto distribution = feedbackService.getRatingDistribution(feedbackType, trainerId, courseId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rating distribution retrieved successfully", distribution));
    }
}
