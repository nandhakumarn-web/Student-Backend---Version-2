package com.nirmaan.controller;

import com.nirmaan.dto.*;
import com.nirmaan.service.QuizService;
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
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz", description = "Quiz management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/available")
    @Operation(summary = "Get available quizzes", description = "Retrieve available quizzes for students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'STUDENT')")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "200", description = "Available quizzes retrieved successfully"),
        @SwaggerResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Page<QuizDto>>> getAvailableQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String courseType,
            @RequestParam(required = false) Long batchId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<QuizDto> quizzes = quizService.getAvailableQuizzes(pageable, courseType, batchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Available quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/{quizId}")
    @Operation(summary = "Get quiz by ID", description = "Retrieve quiz details by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'STUDENT')")
    public ResponseEntity<ApiResponse<QuizDto>> getQuizById(@PathVariable Long quizId) {
        QuizDto quiz = quizService.getQuizById(quizId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz retrieved successfully", quiz));
    }

    @GetMapping("/trainer/{trainerId}")
    @Operation(summary = "Get quizzes by trainer", description = "Retrieve quizzes created by a specific trainer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<Page<QuizDto>>> getQuizzesByTrainer(
            @PathVariable Long trainerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizDto> quizzes = quizService.getQuizzesByTrainer(trainerId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Trainer quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get quizzes by batch", description = "Retrieve quizzes assigned to a specific batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<List<QuizDto>>> getQuizzesByBatch(@PathVariable Long batchId) {
        List<QuizDto> quizzes = quizService.getQuizzesByBatch(batchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/{quizId}/results")
    @Operation(summary = "Get quiz results", description = "Get results of all students for a specific quiz")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<Page<StudentQuizResultDto>>> getQuizResults(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());
        Page<StudentQuizResultDto> results = quizService.getQuizResults(quizId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz results retrieved successfully", results));
    }

    @GetMapping("/{quizId}/analytics")
    @Operation(summary = "Get quiz analytics", description = "Get detailed analytics for a quiz")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<QuizAnalyticsDto>> getQuizAnalytics(@PathVariable Long quizId) {
        QuizAnalyticsDto analytics = quizService.getQuizAnalytics(quizId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz analytics retrieved successfully", analytics));
    }

    @GetMapping("/reports/performance")
    @Operation(summary = "Get performance report", description = "Generate performance report for quizzes")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<List<PerformanceReportDto>>> getPerformanceReport(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate) {
        
        List<PerformanceReportDto> report = quizService.getPerformanceReport(batchId, courseId, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Performance report generated successfully", report));
    }

    @GetMapping("/statistics/summary")
    @Operation(summary = "Get quiz statistics summary", description = "Get overall quiz statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<QuizStatsSummaryDto>> getQuizStatsSummary(
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false) Long batchId) {
        
        QuizStatsSummaryDto summary = quizService.getQuizStatsSummary(trainerId, batchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz statistics summary retrieved successfully", summary));
    }
}
