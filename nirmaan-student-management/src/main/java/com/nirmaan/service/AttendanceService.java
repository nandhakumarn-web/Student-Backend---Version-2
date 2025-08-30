package com.nirmaan.service;

import com.nirmaan.dto.AttendanceDto;
import com.nirmaan.entity.Attendance;
import com.nirmaan.entity.Student;
import com.nirmaan.entity.QRCode;
import com.nirmaan.enums.AttendanceStatus;
import com.nirmaan.exception.ResourceNotFoundException;
import com.nirmaan.exception.ValidationException;
import com.nirmaan.repository.AttendanceRepository;
import com.nirmaan.repository.StudentRepository;
import com.nirmaan.repository.QRCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

	private final AttendanceRepository attendanceRepository;
	private final StudentRepository studentRepository;
	private final QRCodeRepository qrCodeRepository;

	public AttendanceDto markAttendance(Long studentId, String qrCodeId) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student not found"));

		QRCode qrCode = qrCodeRepository.findByQrCodeId(qrCodeId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid QR Code"));

		if (!qrCode.isActive() || qrCode.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new ValidationException("QR Code has expired");
		}

		// Check if attendance already marked for today
		if (attendanceRepository.findByStudentAndAttendanceDate(student, LocalDate.now()).isPresent()) {
			throw new ValidationException("Attendance already marked for today");
		}

		Attendance attendance = new Attendance();
		attendance.setStudent(student);
		attendance.setBatch(student.getBatch());
		attendance.setAttendanceDate(LocalDate.now());
		attendance.setStatus(AttendanceStatus.PRESENT);
		attendance.setMarkedAt(LocalDateTime.now());
		attendance.setQrCodeId(qrCodeId);

		attendance = attendanceRepository.save(attendance);
		return convertToDto(attendance);
	}

	public List<AttendanceDto> getStudentAttendance(Long studentId) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student not found"));

		return attendanceRepository.findByStudent(student).stream().map(this::convertToDto)
				.collect(Collectors.toList());
	}

	public List<AttendanceDto> getAttendanceByDate(LocalDate date) {
		return attendanceRepository.findByAttendanceDate(date).stream().map(this::convertToDto)
				.collect(Collectors.toList());
	}

	private AttendanceDto convertToDto(Attendance attendance) {
		AttendanceDto dto = new AttendanceDto();
		dto.setId(attendance.getId());
		dto.setStudentId(attendance.getStudent().getId());
		dto.setStudentName(attendance.getStudent().getUser().getFirstName() + " "
				+ attendance.getStudent().getUser().getLastName());
		if (attendance.getBatch() != null) {
			dto.setBatchName(attendance.getBatch().getBatchName());
		}
		dto.setAttendanceDate(attendance.getAttendanceDate());
		dto.setStatus(attendance.getStatus());
		dto.setMarkedAt(attendance.getMarkedAt());
		return dto;
	}
}
