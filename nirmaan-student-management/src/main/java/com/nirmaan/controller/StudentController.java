package com.nirmaan.controller;

import com.nirmaan.dto.*;
import com.nirmaan.entity.StudentQuizAttempt;
import com.nirmaan.security.UserPrincipal;
import com.nirmaan.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'STUDENT')")
@Slf4j
@Tag(name = "Student", description = "Student management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final QuizService quizService;
    private final FeedbackService feedbackService;
    private final NotificationService notificationService;
    private final CourseMaterialService courseMaterialService;

    // Profile Management
    @GetMapping("/profile")
    @Operation(summary = "Get student profile", description = "Retrieve current student's profile information")
    public ResponseEntity<ApiResponse<StudentDto>> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", student));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update student profile", description = "Update student profile information")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDto>> updateProfile(
            @Valid @RequestBody StudentDto studentDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        StudentDto updatedStudent = studentService.updateStudent(student.getId(), studentDto);
        log.info("Student profile updated: {}", updatedStudent.getStudentId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updatedStudent));
    }

    @PostMapping("/profile/change-password")
    @Operation(summary = "Change password", description = "Change student password")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        userService.changePassword(userPrincipal.getUser().getId(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully"));
    }

    // Attendance Management
    @GetMapping("/attendance")
    @Operation(summary = "Get student attendance", description = "Retrieve attendance records for the student")
    public ResponseEntity<ApiResponse<Page<AttendanceDto>>> getAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        Page<AttendanceDto> attendance = attendanceService.getStudentAttendance(student.getId(), pageable, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance retrieved successfully", attendance));
    }

    @PostMapping("/attendance/mark")
    @Operation(summary = "Mark attendance using QR code", description = "Mark daily attendance by scanning QR code")
    @PreAuthorize("hasRole('STUDENT')")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "200", description = "Attendance marked successfully"),
        @SwaggerResponse(responseCode = "400", description = "Invalid QR code or already marked"),
        @SwaggerResponse(responseCode = "404", description = "QR code not found")
    })
    public ResponseEntity<ApiResponse<AttendanceDto>> markAttendance(
            @RequestParam String qrCodeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        AttendanceDto attendance = attendanceService.markAttendance(student.getId(), qrCodeId);
        log.info("Attendance marked for student: {} using QR code: {}", student.getStudentId(), qrCodeId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance marked successfully", attendance));
    }

    @GetMapping("/attendance/summary")
    @Operation(summary = "Get attendance summary", description = "Get attendance summary and statistics")
    public ResponseEntity<ApiResponse<AttendanceSummaryDto>> getAttendanceSummary(
            @RequestParam(required = false) @Parameter(description = "Month (1-12)") Integer month,
            @RequestParam(required = false) @Parameter(description = "Year") Integer year,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        AttendanceSummaryDto summary = attendanceService.getStudentAttendanceSummary(student.getId(), month, year);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance summary retrieved successfully", summary));
    }

    // Quiz Management
    @GetMapping("/quizzes")
    @Operation(summary = "Get available quizzes", description = "Retrieve available quizzes for the student")
    public ResponseEntity<ApiResponse<Page<QuizDto>>> getAvailableQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status, // active, completed, missed
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        Page<QuizDto> quizzes = quizService.getAvailableQuizzesForStudent(student.getId(), pageable, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get quiz details", description = "Get quiz details for attempting")
    public ResponseEntity<ApiResponse<QuizDto>> getQuizDetails(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        QuizDto quiz = quizService.getQuizForStudent(quizId, student.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz details retrieved successfully", quiz));
    }

    @PostMapping("/quiz/{quizId}/start")
    @Operation(summary = "Start quiz attempt", description = "Start a new quiz attempt")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizAttemptDto>> startQuizAttempt(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        QuizAttemptDto attempt = quizService.startQuizAttempt(student.getId(), quizId);
        log.info("Quiz attempt started - Student: {}, Quiz: {}", student.getStudentId(), quizId);
        return new ResponseEntity<>(new ApiResponse<>(true, "Quiz attempt started successfully", attempt), HttpStatus.CREATED);
    }

    @PostMapping("/quiz/{quizId}/submit")
    @Operation(summary = "Submit quiz attempt", description = "Submit quiz answers and get results")
    @PreAuthorize("hasRole('STUDENT')")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "200", description = "Quiz submitted successfully"),
        @SwaggerResponse(responseCode = "400", description = "Invalid submission or quiz already submitted"),
        @SwaggerResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<ApiResponse<StudentQuizAttempt>> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmissionRequest submissionRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        StudentQuizAttempt attempt = quizService.submitQuizAttempt(student.getId(), quizId, submissionRequest.getAnswers());
        log.info("Quiz submitted - Student: {}, Quiz: {}, Score: {}", student.getStudentId(), quizId, attempt.getScore());
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz submitted successfully", attempt));
    }

    @GetMapping("/quiz/{quizId}/result")
    @Operation(summary = "Get quiz result", description = "Get quiz attempt result and analysis")
    public ResponseEntity<ApiResponse<QuizResultDto>> getQuizResult(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        QuizResultDto result = quizService.getQuizResult(student.getId(), quizId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz result retrieved successfully", result));
    }

    @GetMapping("/quiz-attempts")
    @Operation(summary = "Get quiz attempts history", description = "Get all quiz attempts with results")
    public ResponseEntity<ApiResponse<Page<QuizAttemptDto>>> getQuizAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("endTime").descending());
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        Page<QuizAttemptDto> attempts = quizService.getStudentQuizAttempts(student.getId(), pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz attempts retrieved successfully", attempts));
    }

    // Feedback Management
    @PostMapping("/feedback")
    @Operation(summary = "Submit feedback", description = "Submit feedback on courses, trainers, or system")
    @PreAuthorize("hasRole('STUDENT')")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "201", description = "Feedback submitted successfully"),
        @SwaggerResponse(responseCode = "400", description = "Invalid feedback data")
    })
    public ResponseEntity<ApiResponse<FeedbackDto>> submitFeedback(
            @Valid @RequestBody FeedbackDto feedbackDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        FeedbackDto feedback = feedbackService.submitFeedback(feedbackDto, student.getId());
        log.info("Feedback submitted by student: {} for type: {}", student.getStudentId(), feedback.getFeedbackType());
        return new ResponseEntity<>(new ApiResponse<>(true, "Feedback submitted successfully", feedback), HttpStatus.CREATED);
    }

    @GetMapping("/feedback")
    @Operation(summary = "Get student's feedback", description = "Retrieve feedback submitted by the student")
    public ResponseEntity<ApiResponse<Page<FeedbackDto>>> getStudentFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        Page<FeedbackDto> feedback = feedbackService.getStudentFeedback(student.getId(), pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback retrieved successfully", feedback));
    }

    // Course Materials
    @GetMapping("/materials")
    @Operation(summary = "Get course materials", description = "Get all course materials for student's batch")
    public ResponseEntity<ApiResponse<List<CourseMaterialDto>>> getCourseMaterials(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        List<CourseMaterialDto> materials = courseMaterialService.getCourseMaterialsForStudent(student.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Course materials retrieved successfully", materials));
    }

    @GetMapping("/materials/{materialId}/download")
    @Operation(summary = "Download course material", description = "Download a specific course material")
    public ResponseEntity<ApiResponse<String>> downloadCourseMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        String downloadUrl = courseMaterialService.generateDownloadUrl(materialId, student.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Download URL generated successfully", downloadUrl));
    }

    // Notifications
    @GetMapping("/notifications")
    @Operation(summary = "Get notifications", description = "Get all notifications for the student")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean read,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationDto> notifications = notificationService.getUserNotifications(
            userPrincipal.getUser().getId(), pageable, read);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved successfully", notifications));
    }

    @PutMapping("/notifications/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        notificationService.markAsRead(notificationId, userPrincipal.getUser().getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read"));
    }

    @PutMapping("/notifications/mark-all-read")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for the student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> markAllNotificationsAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        notificationService.markAllAsRead(userPrincipal.getUser().getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read"));
    }

    // Dashboard and Analytics
    @GetMapping("/dashboard")
    @Operation(summary = "Get student dashboard", description = "Get student dashboard with summary information")
    public ResponseEntity<ApiResponse<StudentDashboardDto>> getDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        StudentDashboardDto dashboard = studentService.getStudentDashboard(student.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard data retrieved successfully", dashboard));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get learning progress", description = "Get detailed learning progress and statistics")
    public ResponseEntity<ApiResponse<LearningProgressDto>> getLearningProgress(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        LearningProgressDto progress = studentService.getLearningProgress(student.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Learning progress retrieved successfully", progress));
    }

    // Batch and Course Information
    @GetMapping("/batch")
    @Operation(summary = "Get batch information", description = "Get information about student's assigned batch")
    public ResponseEntity<ApiResponse<BatchDto>> getBatchInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        BatchDto batch = batchService.getStudentBatch(student.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch information retrieved successfully", batch));
    }

    @GetMapping("/course")
    @Operation(summary = "Get course information", description = "Get information about student's enrolled course")
    public ResponseEntity<ApiResponse<CourseDto>> getCourseInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        CourseDto course = courseService.getStudentCourse(student.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Course information retrieved successfully", course));
    }

    @GetMapping("/classmates")
    @Operation(summary = "Get classmates", description = "Get list of classmates in the same batch")
    public ResponseEntity<ApiResponse<List<StudentDto>>> getClassmates(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        StudentDto student = studentService.getStudentByUserId(userPrincipal.getUser().getId());
        List<StudentDto> classmates = studentService.getClassmates(student.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Classmates retrieved successfully", classmates));
    }
}
