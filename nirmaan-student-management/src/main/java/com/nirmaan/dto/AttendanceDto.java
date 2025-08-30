package com.nirmaan.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.nirmaan.enums.AttendanceStatus;

@Data
public class AttendanceDto {
	private Long id;
	private Long studentId;
	private String studentName;
	private String batchName;
	private LocalDate attendanceDate;
	private AttendanceStatus status;
	private LocalDateTime markedAt;
}
