package com.nirmaan.controller;

import com.nirmaan.dto.ApiResponse;
import com.nirmaan.dto.FeedbackDto;
import com.nirmaan.enums.FeedbackType;
import com.nirmaan.security.UserPrincipal;
import com.nirmaan.service.FeedbackService;
import com.nirmaan.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final StudentService studentService;

    // ===============================
    // = STUDENT OPERATIONS
    // ===============================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'STUDENT')")
    public ResponseEntity<ApiResponse<FeedbackDto>> submitFeedback(@Valid @RequestBody FeedbackDto feedbackDto, 
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = studentService.getStudentByUserId(userPrincipal.getUser().getId()).getId();
        
        FeedbackDto feedback = feedbackService.submitFeedback(feedbackDto, studentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Feedback submitted successfully", feedback));
    }

    @PostMapping("/course")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'STUDENT')")
    public ResponseEntity<ApiResponse<FeedbackDto>> submitCourseFeedback(
            @RequestParam Long courseId,
            @RequestParam Integer rating,
            @RequestParam String comments,
            @RequestParam(defaultValue = "false") Boolean anonymous,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = studentService.getStudentByUserId(userPrincipal.getUser().getId()).getId();
        
        FeedbackDto feedback = feedbackService.submitCourseFeedback(studentId, courseId, rating, comments, anonymous);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Course feedback submitted successfully", feedback));
    }

    @PostMapping("/trainer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'STUDENT')")
    public ResponseEntity<ApiResponse<FeedbackDto>> submitTrainerFeedback(
            @RequestParam Long trainerId,
            @RequestParam Integer rating,
            @RequestParam String comments,
            @RequestParam(defaultValue = "false") Boolean anonymous,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = studentService.getStudentByUserId(userPrincipal.getUser().getId()).getId();
        
        FeedbackDto feedback = feedbackService.submitTrainerFeedback(studentId, trainerId, rating, comments, anonymous);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Trainer feedback submitted successfully", feedback));
    }

    @PostMapping("/system")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'STUDENT')")
    public ResponseEntity<ApiResponse<FeedbackDto>> submitSystemFeedback(
            @RequestParam Integer rating,
            @RequestParam String comments,
            @RequestParam(defaultValue = "false") Boolean anonymous,
            Authentication authentication) {
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = studentService.getStudentByUserId(userPrincipal.getUser().getId()).getId();
        
        FeedbackDto feedback = feedbackService.submitSystem