package com.nirmaan.controller;

import com.nirmaan.dto.*;
import com.nirmaan.service.CourseService;
import com.nirmaan.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course", description = "Course management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    private final CourseService courseService;
    private final BatchService batchService;

    @GetMapping
    @Operation(summary = "Get all courses", description = "Retrieve all courses with pagination")
    public ResponseEntity<ApiResponse<Page<CourseDto>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "courseName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CourseDto> courses = courseService.getAllCourses(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Courses retrieved successfully", courses));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active courses", description = "Retrieve only active courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getActiveCourses() {
        List<CourseDto> courses = courseService.getActiveCourses();
        return ResponseEntity.ok(new ApiResponse<>(true, "Active courses retrieved successfully", courses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Retrieve course details by ID")
    public ResponseEntity<ApiResponse<CourseDto>> getCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course retrieved successfully", course));
    }

    @PostMapping
    @Operation(summary = "Create course", description = "Create a new course")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "201", description = "Course created successfully"),
        @SwaggerResponse(responseCode = "400", description = "Invalid course data"),
        @SwaggerResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto course = courseService.createCourse(courseDto);
        log.info("Course created: {} - {}", course.getCourseType(), course.getCourseName());
        return new ResponseEntity<>(new ApiResponse<>(true, "Course created successfully", course), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update course", description = "Update course details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseDto>> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        CourseDto course = courseService.updateCourse(id, courseDto);
        log.info("Course updated: {} - {}", course.getCourseType(), course.getCourseName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Course updated successfully", course));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete course", description = "Deactivate a course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        log.info("Course deactivated with ID: {}", id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course deleted successfully"));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate course", description = "Activate a deactivated course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateCourse(@PathVariable Long id) {
        courseService.activateCourse(id);
        log.info("Course activated with ID: {}", id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course activated successfully"));
    }

    @GetMapping("/{id}/batches")
    @Operation(summary = "Get course batches", description = "Get all batches for a specific course")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<List<BatchDto>>> getCourseBatches(@PathVariable Long id) {
        List<BatchDto> batches = batchService.getBatchesByCourse(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course batches retrieved successfully", batches));
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "Get course students", description = "Get all students enrolled in a specific course")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<Page<StudentDto>>> getCourseStudents(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("user.firstName"));
        Page<StudentDto> students = studentService.getStudentsByCourse(id, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course students retrieved successfully", students));
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get course statistics", description = "Get statistics for a specific course")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<CourseStatisticsDto>> getCourseStatistics(@PathVariable Long id) {
        CourseStatisticsDto statistics = courseService.getCourseStatistics(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course statistics retrieved successfully", statistics));
    }
}

