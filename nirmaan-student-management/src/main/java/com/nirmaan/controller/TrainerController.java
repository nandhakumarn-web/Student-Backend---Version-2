package com.nirmaan.controller;

import com.nirmaan.dto.ApiResponse;
import com.nirmaan.dto.QuizDto;
import com.nirmaan.dto.TrainerDto;
import com.nirmaan.dto.AttendanceDto;
import com.nirmaan.dto.FeedbackDto;
import com.nirmaan.security.UserPrincipal;
import com.nirmaan.service.QuizService;
import com.nirmaan.service.AttendanceService;
import com.nirmaan.service.FeedbackService;
import com.nirmaan.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
public class TrainerController {

    private final QuizService quizService;
    private final AttendanceService attendanceService;
    private final FeedbackService feedbackService;
    private final TrainerService trainerService;

    // Quiz Management
    @PostMapping("/quiz")
    public ResponseEntity<ApiResponse<QuizDto>> createQuiz(@Valid @RequestBody QuizDto quizDto, 
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long trainerId = trainerService.getTrainerByUserId(userPrincipal.getUser().getId()).getId();
        
        QuizDto createdQuiz = quizService.createQuiz(quizDto, trainerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Quiz created successfully", createdQuiz));
    }

    @GetMapping("/quizzes")
    public ResponseEntity<ApiResponse<List<QuizDto>>> getMyQuizzes(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long trainerId = trainerService.getTrainerByUserId(userPrincipal.getUser().getId()).getId();
        
        List<QuizDto> quizzes = quizService.getQuizzesByTrainer(trainerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quizzes retrieved successfully", quizzes));
    }

    // Attendance Management
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getAttendanceByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceDto> attendance = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance retrieved successfully", attendance));
    }

    // Feedback Management
    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getMyFeedback(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long trainerId = trainerService.getTrainerByUserId(userPrincipal.getUser().getId()).getId();
        
        List<FeedbackDto> feedback = feedbackService.getFeedbackByTrainer(trainerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback retrieved successfully", feedback));
    }

    // Profile Management
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<TrainerDto>> getProfile(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", trainer));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<TrainerDto>> updateProfile(@Valid @RequestBody TrainerDto trainerDto, 
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long trainerId = trainerService.getTrainerByUserId(userPrincipal.getUser().getId()).getId();
        
        TrainerDto updatedTrainer = trainerService.updateTrainer(trainerId, trainerDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updatedTrainer));
    }
}