package com.nirmaan.controller;

import com.nirmaan.dto.*;
import com.nirmaan.security.UserPrincipal;
import com.nirmaan.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
@Slf4j
@Tag(name = "Trainer", description = "Trainer management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrainerController {

    private final QuizService quizService;
    private final AttendanceService attendanceService;
    private final FeedbackService feedbackService;
    private final TrainerService trainerService;
    private final StudentService studentService;
    private final QRCodeService qrCodeService;
    private final NotificationService notificationService;

    // Profile Management
    @GetMapping("/profile")
    @Operation(summary = "Get trainer profile", description = "Retrieve trainer profile information")
    public ResponseEntity<com.nirmaan.dto.ApiResponse<TrainerDto>> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", trainer));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update trainer profile", description = "Update trainer profile information")
    public ResponseEntity<com.nirmaan.dto.ApiResponse<TrainerDto>> updateProfile(
            @Valid @RequestBody TrainerDto trainerDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto currentTrainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        TrainerDto updatedTrainer = trainerService.updateTrainer(currentTrainer.getId(), trainerDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updatedTrainer));
    }

    // Batch Management
    @GetMapping("/batches")
    @Operation(summary = "Get trainer's batches", description = "Retrieve batches assigned to the trainer")
    public ResponseEntity<ApiResponse<List<BatchDto>>> getTrainerBatches(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        List<BatchDto> batches = batchService.getBatchesByTrainer(trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Batches retrieved successfully", batches));
    }

    @GetMapping("/batches/{batchId}/students")
    @Operation(summary = "Get batch students", description = "Retrieve students in a specific batch")
    public ResponseEntity<ApiResponse<List<StudentDto>>> getBatchStudents(
            @PathVariable Long batchId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // Verify trainer has access to this batch
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        List<StudentDto> students = studentService.getStudentsByBatch(batchId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch students retrieved successfully", students));
    }

    // Quiz Management
    @PostMapping("/quiz")
    @Operation(summary = "Create quiz", description = "Create a new quiz for students")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "201", description = "Quiz created successfully"),
        @SwaggerResponse(responseCode = "400", description = "Invalid quiz data"),
        @SwaggerResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<QuizDto>> createQuiz(
            @Valid @RequestBody QuizDto quizDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        QuizDto quiz = quizService.createQuiz(quizDto, trainer.getId());
        log.info("Quiz created by trainer: {} with title: {}", trainer.getTrainerId(), quiz.getTitle());
        return new ResponseEntity<>(new ApiResponse<>(true, "Quiz created successfully", quiz), HttpStatus.CREATED);
    }

    @GetMapping("/quizzes")
    @Operation(summary = "Get trainer's quizzes", description = "Retrieve all quizzes created by the trainer")
    public ResponseEntity<ApiResponse<Page<QuizDto>>> getTrainerQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        Page<QuizDto> quizzes = quizService.getQuizzesByTrainer(trainer.getId(), pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get quiz details", description = "Retrieve detailed quiz information including questions")
    public ResponseEntity<ApiResponse<QuizDto>> getQuizDetails(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        QuizDto quiz = quizService.getQuizById(quizId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz details retrieved successfully", quiz));
    }

    @PutMapping("/quiz/{quizId}")
    @Operation(summary = "Update quiz", description = "Update quiz details and questions")
    public ResponseEntity<ApiResponse<QuizDto>> updateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizDto quizDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        QuizDto updatedQuiz = quizService.updateQuiz(quizId, quizDto, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz updated successfully", updatedQuiz));
    }

    @DeleteMapping("/quiz/{quizId}")
    @Operation(summary = "Delete quiz", description = "Deactivate a quiz")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        quizService.deleteQuiz(quizId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz deleted successfully"));
    }

    @PostMapping("/quiz/{quizId}/activate")
    @Operation(summary = "Activate quiz", description = "Activate a quiz for students to attempt")
    public ResponseEntity<ApiResponse<Void>> activateQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        quizService.activateQuiz(quizId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz activated successfully"));
    }

    @PostMapping("/quiz/{quizId}/deactivate")
    @Operation(summary = "Deactivate quiz", description = "Deactivate a quiz to prevent further attempts")
    public ResponseEntity<ApiResponse<Void>> deactivateQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        quizService.deactivateQuiz(quizId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz deactivated successfully"));
    }

    // Quiz Results and Analytics
    @GetMapping("/quiz/{quizId}/results")
    @Operation(summary = "Get quiz results", description = "Get results of all students for a specific quiz")
    public ResponseEntity<ApiResponse<List<StudentQuizResultDto>>> getQuizResults(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        List<StudentQuizResultDto> results = quizService.getQuizResults(quizId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz results retrieved successfully", results));
    }

    @GetMapping("/quiz/{quizId}/analytics")
    @Operation(summary = "Get quiz analytics", description = "Get detailed analytics for a quiz")
    public ResponseEntity<ApiResponse<QuizAnalyticsDto>> getQuizAnalytics(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        QuizAnalyticsDto analytics = quizService.getQuizAnalytics(quizId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz analytics retrieved successfully", analytics));
    }

    // Attendance Management
    @GetMapping("/attendance")
    @Operation(summary = "Get attendance by date", description = "Retrieve attendance records for a specific date")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getAttendanceByDate(
            @RequestParam @Parameter(description = "Attendance date (YYYY-MM-DD)") LocalDate date,
            @RequestParam(required = false) Long batchId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        List<AttendanceDto> attendance = attendanceService.getAttendanceByDateAndTrainer(date, trainer.getId(), batchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance retrieved successfully", attendance));
    }

    @GetMapping("/attendance/batch/{batchId}")
    @Operation(summary = "Get batch attendance", description = "Get attendance records for a specific batch")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getBatchAttendance(
            @PathVariable Long batchId,
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        List<AttendanceDto> attendance = attendanceService.getBatchAttendance(batchId, startDate, endDate, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch attendance retrieved successfully", attendance));
    }

    @GetMapping("/attendance/student/{studentId}")
    @Operation(summary = "Get student attendance", description = "Get attendance records for a specific student")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> getStudentAttendance(
            @PathVariable Long studentId,
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        List<AttendanceDto> attendance = attendanceService.getStudentAttendanceByTrainer(studentId, startDate, endDate, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Student attendance retrieved successfully", attendance));
    }

    @PostMapping("/attendance/manual")
    @Operation(summary = "Mark manual attendance", description = "Manually mark attendance for students")
    public ResponseEntity<ApiResponse<List<AttendanceDto>>> markManualAttendance(
            @Valid @RequestBody ManualAttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        List<AttendanceDto> attendance = attendanceService.markManualAttendance(request, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Manual attendance marked successfully", attendance));
    }

    @GetMapping("/attendance/summary")
    @Operation(summary = "Get attendance summary", description = "Get attendance summary for trainer's batches")
    public ResponseEntity<ApiResponse<AttendanceSummaryDto>> getAttendanceSummary(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) @Parameter(description = "Month (1-12)") Integer month,
            @RequestParam(required = false) @Parameter(description = "Year") Integer year,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        AttendanceSummaryDto summary = attendanceService.getAttendanceSummaryByTrainer(trainer.getId(), batchId, month, year);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance summary retrieved successfully", summary));
    }

    // QR Code Management
    @GetMapping("/qrcode/today/{batchId}")
    @Operation(summary = "Get today's QR code", description = "Get QR code for today's attendance for a specific batch")
    public ResponseEntity<ApiResponse<QRCodeDto>> getTodayQRCode(
            @PathVariable Long batchId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        QRCodeDto qrCode = qrCodeService.getTodayQRCodeForBatch(batchId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "QR code retrieved successfully", qrCode));
    }

    @PostMapping("/qrcode/generate/{batchId}")
    @Operation(summary = "Generate QR code", description = "Generate new QR code for attendance")
    public ResponseEntity<ApiResponse<QRCodeDto>> generateQRCode(
            @PathVariable Long batchId,
            @RequestParam(required = false) @Parameter(description = "QR code validity in hours") Integer validityHours,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        QRCodeDto qrCode = qrCodeService.generateQRCode(batchId, trainer.getId(), validityHours);
        return new ResponseEntity<>(new ApiResponse<>(true, "QR code generated successfully", qrCode), HttpStatus.CREATED);
    }

    // Feedback Management
    @GetMapping("/feedback")
    @Operation(summary = "Get trainer feedback", description = "Retrieve feedback submitted for the trainer")
    public ResponseEntity<ApiResponse<Page<FeedbackDto>>> getTrainerFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) Integer rating,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        Page<FeedbackDto> feedback = feedbackService.getFeedbackByTrainer(trainer.getId(), pageable, feedbackType, rating);
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback retrieved successfully", feedback));
    }

    @GetMapping("/feedback/summary")
    @Operation(summary = "Get feedback summary", description = "Get feedback summary and statistics")
    public ResponseEntity<ApiResponse<FeedbackSummaryDto>> getFeedbackSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        FeedbackSummaryDto summary = feedbackService.getFeedbackSummaryByTrainer(trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback summary retrieved successfully", summary));
    }

    // Performance Analytics
    @GetMapping("/analytics/performance")
    @Operation(summary = "Get performance analytics", description = "Get student performance analytics for trainer's batches")
    public ResponseEntity<ApiResponse<PerformanceAnalyticsDto>> getPerformanceAnalytics(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        PerformanceAnalyticsDto analytics = quizService.getPerformanceAnalyticsByTrainer(trainer.getId(), batchId, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Performance analytics retrieved successfully", analytics));
    }

    @GetMapping("/analytics/student/{studentId}")
    @Operation(summary = "Get student analytics", description = "Get detailed analytics for a specific student")
    public ResponseEntity<ApiResponse<StudentAnalyticsDto>> getStudentAnalytics(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        StudentAnalyticsDto analytics = studentService.getStudentAnalytics(studentId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Student analytics retrieved successfully", analytics));
    }

    // Notifications
    @PostMapping("/notifications/batch/{batchId}")
    @Operation(summary = "Send batch notification", description = "Send notification to all students in a batch")
    public ResponseEntity<ApiResponse<Void>> sendBatchNotification(
            @PathVariable Long batchId,
            @Valid @RequestBody NotificationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        notificationService.sendBatchNotification(batchId, request, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch notification sent successfully"));
    }

    @PostMapping("/notifications/student/{studentId}")
    @Operation(summary = "Send student notification", description = "Send notification to a specific student")
    public ResponseEntity<ApiResponse<Void>> sendStudentNotification(
            @PathVariable Long studentId,
            @Valid @RequestBody NotificationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        notificationService.sendStudentNotification(studentId, request, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Student notification sent successfully"));
    }

    // Course Materials
    @PostMapping("/materials/upload")
    @Operation(summary = "Upload course material", description = "Upload course material for students")
    public ResponseEntity<ApiResponse<CourseMaterialDto>> uploadCourseMaterial(
            @RequestParam Long batchId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        CourseMaterialDto material = courseMaterialService.uploadMaterial(batchId, file, title, description, trainer.getId());
        return new ResponseEntity<>(new ApiResponse<>(true, "Course material uploaded successfully", material), HttpStatus.CREATED);
    }

    @GetMapping("/materials/batch/{batchId}")
    @Operation(summary = "Get batch materials", description = "Get all course materials for a specific batch")
    public ResponseEntity<ApiResponse<List<CourseMaterialDto>>> getBatchMaterials(
            @PathVariable Long batchId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        List<CourseMaterialDto> materials = courseMaterialService.getBatchMaterials(batchId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch materials retrieved successfully", materials));
    }

    @DeleteMapping("/materials/{materialId}")
    @Operation(summary = "Delete course material", description = "Delete a course material")
    public ResponseEntity<ApiResponse<Void>> deleteCourseMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        TrainerDto trainer = trainerService.getTrainerByUserId(userPrincipal.getUser().getId());
        courseMaterialService.deleteMaterial(materialId, trainer.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Course material deleted successfully"));
    }
}