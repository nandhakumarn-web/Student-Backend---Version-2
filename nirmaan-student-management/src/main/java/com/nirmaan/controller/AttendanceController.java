package com.nirmaan.controller;

import com.nirmaan.dto.*;
import com.nirmaan.service.AttendanceService;
import com.nirmaan.service.QRCodeService;
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
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
@Slf4j
@Tag(name = "Attendance", description = "Attendance management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final QRCodeService qrCodeService;

    @GetMapping("/date/{date}")
    @Operation(summary = "Get attendance by date", description = "Retrieve attendance records for a specific date")
    @ApiResponses(value = {
        @SwaggerResponse(responseCode = "200", description = "Attendance records retrieved successfully"),
        @SwaggerResponse(responseCode = "400", description = "Invalid date format"),
        @SwaggerResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Page<AttendanceDto>>> getAttendanceByDate(
            @PathVariable @Parameter(description = "Attendance date (YYYY-MM-DD)") LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long batchId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("markedAt").descending());
        Page<AttendanceDto> attendance = attendanceService.getAttendanceByDate(date, pageable, batchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance retrieved successfully", attendance));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student attendance", description = "Retrieve attendance records for a specific student")
    public ResponseEntity<ApiResponse<Page<AttendanceDto>>> getStudentAttendance(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        Page<AttendanceDto> attendance = attendanceService.getStudentAttendance(studentId, pageable, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Student attendance retrieved successfully", attendance));
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get batch attendance", description = "Retrieve attendance records for a specific batch")
    public ResponseEntity<ApiResponse<Page<AttendanceDto>>> getBatchAttendance(
            @PathVariable Long batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam(required = false) @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("attendanceDate").descending());
        Page<AttendanceDto> attendance = attendanceService.getBatchAttendance(batchId, pageable, startDate, endDate);
        return ResponseEntity.ok(new ApiResponse<>(true, "Batch attendance retrieved successfully", attendance));
    }

    @GetMapping("/reports/summary")
    @Operation(summary = "Get attendance summary", description = "Get attendance summary statistics")
    public ResponseEntity<ApiResponse<AttendanceSummaryDto>> getAttendanceSummary(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) @Parameter(description = "Month (1-12)") Integer month,
            @RequestParam(required = false) @Parameter(description = "Year") Integer year) {
        
        AttendanceSummaryDto summary = attendanceService.getAttendanceSummary(batchId, month, year);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance summary retrieved successfully", summary));
    }

    @GetMapping("/reports/detailed")
    @Operation(summary = "Get detailed attendance report", description = "Generate detailed attendance report")
    public ResponseEntity<ApiResponse<List<AttendanceReportDto>>> getDetailedAttendanceReport(
            @RequestParam @Parameter(description = "Start date (YYYY-MM-DD)") LocalDate startDate,
            @RequestParam @Parameter(description = "End date (YYYY-MM-DD)") LocalDate endDate,
            @RequestParam(required = false) Long batchId) {
        
        List<AttendanceReportDto> report = attendanceService.getAttendanceReport(startDate, endDate, batchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance report generated successfully", report));
    }

    @GetMapping("/qrcode/{batchId}/today")
    @Operation(summary = "Get today's QR code for batch", description = "Get QR code for today's attendance for a specific batch")
    public ResponseEntity<ApiResponse<QRCodeDto>> getTodayQRCode(@PathVariable Long batchId) {
        QRCodeDto qrCode = qrCodeService.getTodayQRCodeForBatch(batchId, null);
        return ResponseEntity.ok(new ApiResponse<>(true, "QR code retrieved successfully", qrCode));
    }

    @PostMapping("/qrcode/{batchId}/generate")
    @Operation(summary = "Generate QR code for batch", description = "Generate new QR code for attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<QRCodeDto>> generateQRCode(
            @PathVariable Long batchId,
            @RequestParam(required = false, defaultValue = "8") @Parameter(description = "QR code validity in hours") Integer validityHours) {
        
        QRCodeDto qrCode = qrCodeService.generateQRCode(batchId, null, validityHours);
        return ResponseEntity.ok(new ApiResponse<>(true, "QR code generated successfully", qrCode));
    }

    @GetMapping("/statistics/monthly")
    @Operation(summary = "Get monthly attendance statistics", description = "Get attendance statistics for a specific month")
    public ResponseEntity<ApiResponse<MonthlyAttendanceStatsDto>> getMonthlyStatistics(
            @RequestParam @Parameter(description = "Year") Integer year,
            @RequestParam @Parameter(description = "Month (1-12)") Integer month,
            @RequestParam(required = false) Long batchId) {
        
        MonthlyAttendanceStatsDto stats = attendanceService.getMonthlyStatistics(year, month, batchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Monthly statistics retrieved successfully", stats));
    }

    @GetMapping("/defaulters")
    @Operation(summary = "Get attendance defaulters", description = "Get list of students with poor attendance")
    public ResponseEntity<ApiResponse<List<AttendanceDefaulterDto>>> getAttendanceDefaulters(
            @RequestParam(required = false, defaultValue = "75") @Parameter(description = "Minimum attendance percentage") Double minPercentage,
            @RequestParam(required = false) Long batchId) {
        
        List<AttendanceDefaulterDto> defaulters = attendanceService.getAttendanceDefaulters(minPercentage, batchId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Attendance defaulters retrieved successfully", defaulters));
    }
}
