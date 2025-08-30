package com.nirmaan.controller;

import com.nirmaan.dto.*;
import com.nirmaan.entity.User;
import com.nirmaan.enums.Role;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Admin", description = "Admin management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

 private final UserService userService;
 private final StudentService studentService;
 private final TrainerService trainerService;
 private final CourseService courseService;
 private final AttendanceService attendanceService;
 private final FeedbackService feedbackService;
 private final QuizService quizService;
 private final NotificationService notificationService;

 // User Management
 @PostMapping("/users/register")
 @Operation(summary = "Register new user", description = "Register a new user with specified role")
 @ApiResponses(value = {
     @SwaggerResponse(responseCode = "201", description = "User registered successfully"),
     @SwaggerResponse(responseCode = "400", description = "Invalid input or user already exists"),
     @SwaggerResponse(responseCode = "403", description = "Access denied")
 })
 public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
     log.info("Admin registering new user: {} with role: {}", request.getUsername(), request.getRole());
     User user = userService.registerUser(request);
     return new ResponseEntity<>(new ApiResponse<>(true, "User registered successfully", user), HttpStatus.CREATED);
 }

 @GetMapping("/users")
 @Operation(summary = "Get all users", description = "Retrieve all users with pagination")
 public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size,
         @RequestParam(defaultValue = "id") String sortBy,
         @RequestParam(defaultValue = "asc") String sortDir) {
     
     Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
     Pageable pageable = PageRequest.of(page, size, sort);
     
     Page<User> users = userService.getAllUsers(pageable);
     return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));
 }

 @GetMapping("/users/role/{role}")
 @Operation(summary = "Get users by role", description = "Retrieve users filtered by role")
 public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable Role role) {
     List<User> users = userService.getUsersByRole(role);
     return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));
 }

 @GetMapping("/users/{id}")
 @Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
 public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
     User user = userService.getUserById(id);
     return ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", user));
 }

 @PutMapping("/users/{id}")
 @Operation(summary = "Update user", description = "Update user details")
 public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @Valid @RequestBody User userRequest) {
     User user = userService.updateUser(id, userRequest);
     return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", user));
 }

 @DeleteMapping("/users/{id}")
 @Operation(summary = "Delete user", description = "Soft delete user (set inactive)")
 public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
     userService.deleteUser(id);
     return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully"));
 }

 @PutMapping("/users/{id}/activate")
 @Operation(summary = "Activate user", description = "Activate a deactivated user")
 public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
     userService.activateUser(id);
     return ResponseEntity.ok(new ApiResponse<>(true, "User activated successfully"));
 }

 // Student Management
 @GetMapping("/students")
 @Operation(summary = "Get all students", description = "Retrieve all students with pagination")
 public ResponseEntity<ApiResponse<Page<StudentDto>>> getAllStudents(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size,
         @RequestParam(defaultValue = "id") String sortBy) {
     
     Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
     Page<StudentDto> students = studentService.getAllStudents(pageable);
     return ResponseEntity.ok(new ApiResponse<>(true, "Students retrieved successfully", students));
 }

 @GetMapping("/students/{id}")
 @Operation(summary = "Get student by ID", description = "Retrieve student details by ID")
 public ResponseEntity<ApiResponse<StudentDto>> getStudentById(@PathVariable Long id) {
     StudentDto student = studentService.getStudentById(id);
     return ResponseEntity.ok(new ApiResponse<>(true, "Student retrieved successfully", student));
 }

 @PutMapping("/students/{id}")
 @Operation(summary = "Update student", description = "Update student details")
 public ResponseEntity<ApiResponse<StudentDto>> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentDto studentDto) {
     StudentDto student = studentService.updateStudent(id, studentDto);
     return ResponseEntity.ok(new ApiResponse<>(true, "Student updated successfully", student));
 }

 @PutMapping("/students/{id}/batch/{batchId}")
 @Operation(summary = "Assign student to batch", description = "Assign a student to a specific batch")
 public ResponseEntity<ApiResponse<StudentDto>> assignStudentToBatch(@PathVariable Long id, @PathVariable Long batchId) {
     StudentDto student = studentService.assignStudentToBatch(id, batchId);
     return ResponseEntity.ok(new ApiResponse<>(true, "Student assigned to batch successfully", student));
 }

 // Trainer Management
 @GetMapping("/trainers")
 @Operation(summary = "Get all trainers", description = "Retrieve all trainers with pagination")
 public ResponseEntity<ApiResponse<Page<TrainerDto>>> getAllTrainers(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size) {
     
     Pageable pageable = PageRequest.of(page, size);
     Page<TrainerDto> trainers = trainerService.getAllTrainers(pageable);
     return ResponseEntity.ok(new ApiResponse<>(true, "Trainers retrieved successfully", trainers));
 }

 @GetMapping("/trainers/{id}")
 @Operation(summary = "Get trainer by ID", description = "Retrieve trainer details by ID")
 public ResponseEntity<ApiResponse<TrainerDto>> getTrainerById(@PathVariable Long id) {
     TrainerDto trainer = trainerService.getTrainerById(id);
     return ResponseEntity.ok(new ApiResponse<>(true, "Trainer retrieved successfully", trainer));
 }

 @PutMapping("/trainers/{id}")
 @Operation(summary = "Update trainer", description = "Update trainer details")
 public ResponseEntity<ApiResponse<TrainerDto>> updateTrainer(@PathVariable Long id, @Valid @RequestBody TrainerDto trainerDto) {
     TrainerDto trainer = trainerService.updateTrainer(id, trainerDto);
     return ResponseEntity.ok(new ApiResponse<>(true, "Trainer updated successfully", trainer));
 }

 // Course Management
 @PostMapping("/courses")
 @Operation(summary = "Create course", description = "Create a new course")
 public ResponseEntity<ApiResponse<CourseDto>> createCourse(@Valid @RequestBody CourseDto courseDto) {
     CourseDto course = courseService.createCourse(courseDto);
     return new ResponseEntity<>(new ApiResponse<>(true, "Course created successfully", course), HttpStatus.CREATED);
 }

 @PutMapping("/courses/{id}")
 @Operation(summary = "Update course", description = "Update course details")
 public ResponseEntity<ApiResponse<CourseDto>> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
     CourseDto course = courseService.updateCourse(id, courseDto);
     return ResponseEntity.ok(new ApiResponse<>(true, "Course updated successfully", course));
 }

 @DeleteMapping("/courses/{id}")
 @Operation(summary = "Delete course", description = "Deactivate a course")
 public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
     courseService.deleteCourse(id);
     return ResponseEntity.ok(new ApiResponse<>(true, "Course deleted successfully"));
 }

 // Batch Management
 @PostMapping("/batches")
 @Operation(summary = "Create batch", description = "Create a new batch")
 public ResponseEntity<ApiResponse<BatchDto>> createBatch(@Valid @RequestBody BatchDto batchDto) {
     BatchDto batch = batchService.createBatch(batchDto);
     return new ResponseEntity<>(new ApiResponse<>(true, "Batch created successfully", batch), HttpStatus.CREATED);
 }

 @GetMapping("/batches")
 @Operation(summary = "Get all batches", description = "Retrieve all batches")
 public ResponseEntity<ApiResponse<List<BatchDto>>> getAllBatches() {
     List<BatchDto> batches = batchService.getAllBatches();
     return ResponseEntity.ok(new ApiResponse<>(true, "Batches retrieved successfully", batches));
 }

 @PutMapping("/batches/{id}")
 @Operation(summary = "Update batch", description = "Update batch details")
 public ResponseEntity<ApiResponse<BatchDto>> updateBatch(@PathVariable Long id, @Valid @RequestBody BatchDto batchDto) {
     BatchDto batch = batchService.updateBatch(id, batchDto);
     return ResponseEntity.ok(new ApiResponse<>(true, "Batch updated successfully", batch));
 }

 // Attendance Reports
 @GetMapping("/reports/attendance")
 @Operation(summary = "Get attendance report", description = "Generate attendance report for specified date range")
 public ResponseEntity<ApiResponse<List<AttendanceReportDto>>> getAttendanceReport(
         @RequestParam @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
         @RequestParam @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate,
         @RequestParam(required = false) Long batchId) {
     
     List<AttendanceReportDto> report = attendanceService.getAttendanceReport(startDate, endDate, batchId);
     return ResponseEntity.ok(new ApiResponse<>(true, "Attendance report generated successfully", report));
 }

 @GetMapping("/reports/attendance/summary")
 @Operation(summary = "Get attendance summary", description = "Get attendance summary statistics")
 public ResponseEntity<ApiResponse<AttendanceSummaryDto>> getAttendanceSummary(
         @RequestParam(required = false) Long batchId) {
     
     AttendanceSummaryDto summary = attendanceService.getAttendanceSummary(batchId);
     return ResponseEntity.ok(new ApiResponse<>(true, "Attendance summary retrieved successfully", summary));
 }

 // Performance Reports
 @GetMapping("/reports/performance")
 @Operation(summary = "Get performance report", description = "Generate performance report for students")
 public ResponseEntity<ApiResponse<List<PerformanceReportDto>>> getPerformanceReport(
         @RequestParam(required = false) Long batchId,
         @RequestParam(required = false) Long courseId) {
     
     List<PerformanceReportDto> report = quizService.getPerformanceReport(batchId, courseId);
     return ResponseEntity.ok(new ApiResponse<>(true, "Performance report generated successfully", report));
 }

 // Feedback Management
 @GetMapping("/feedback")
 @Operation(summary = "Get all feedback", description = "Retrieve all feedback with filtering options")
 public ResponseEntity<ApiResponse<Page<FeedbackDto>>> getAllFeedback(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size,
         @RequestParam(required = false) String feedbackType,
         @RequestParam(required = false) Long trainerId) {
     
     Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
     Page<FeedbackDto> feedback = feedbackService.getAllFeedback(pageable, feedbackType, trainerId);
     return ResponseEntity.ok(new ApiResponse<>(true, "Feedback retrieved successfully", feedback));
 }

 @GetMapping("/feedback/analytics")
 @Operation(summary = "Get feedback analytics", description = "Get feedback analytics and statistics")
 public ResponseEntity<ApiResponse<FeedbackAnalyticsDto>> getFeedbackAnalytics() {
     FeedbackAnalyticsDto analytics = feedbackService.getFeedbackAnalytics();
     return ResponseEntity.ok(new ApiResponse<>(true, "Feedback analytics retrieved successfully", analytics));
 }

 // System Analytics
 @GetMapping("/analytics/dashboard")
 @Operation(summary = "Get dashboard analytics", description = "Get comprehensive dashboard analytics")
 public ResponseEntity<ApiResponse<DashboardAnalyticsDto>> getDashboardAnalytics() {
     DashboardAnalyticsDto analytics = analyticsService.getDashboardAnalytics();
     return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard analytics retrieved successfully", analytics));
 }

 @GetMapping("/analytics/user-activity")
 @Operation(summary = "Get user activity", description = "Get user activity statistics")
 public ResponseEntity<ApiResponse<List<UserActivityDto>>> getUserActivity(
         @RequestParam @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
         @RequestParam @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate) {
     
     List<UserActivityDto> activity = analyticsService.getUserActivity(startDate, endDate);
     return ResponseEntity.ok(new ApiResponse<>(true, "User activity retrieved successfully", activity));
 }

 // Notification Management
 @PostMapping("/notifications/broadcast")
 @Operation(summary = "Broadcast notification", description = "Send notification to multiple users")
 public ResponseEntity<ApiResponse<Void>> broadcastNotification(@Valid @RequestBody NotificationRequest request) {
     notificationService.broadcastNotification(request);
     return ResponseEntity.ok(new ApiResponse<>(true, "Notification broadcasted successfully"));
 }

 @PostMapping("/notifications/user/{userId}")
 @Operation(summary = "Send notification to user", description = "Send notification to specific user")
 public ResponseEntity<ApiResponse<Void>> sendNotificationToUser(
         @PathVariable Long userId, 
         @Valid @RequestBody NotificationRequest request) {
     
     notificationService.sendNotificationToUser(userId, request);
     return ResponseEntity.ok(new ApiResponse<>(true, "Notification sent successfully"));
 }

 // System Configuration
 @GetMapping("/config")
 @Operation(summary = "Get system configuration", description = "Retrieve system configuration settings")
 public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemConfig() {
     Map<String, Object> config = configService.getSystemConfig();
     return ResponseEntity.ok(new ApiResponse<>(true, "System configuration retrieved successfully", config));
 }

 @PutMapping("/config")
 @Operation(summary = "Update system configuration", description = "Update system configuration settings")
 public ResponseEntity<ApiResponse<Void>> updateSystemConfig(@RequestBody Map<String, Object> config) {
     configService.updateSystemConfig(config);
     return ResponseEntity.ok(new ApiResponse<>(true, "System configuration updated successfully"));
 }
}
