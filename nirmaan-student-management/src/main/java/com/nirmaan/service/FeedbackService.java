package com.nirmaan.service;

import com.nirmaan.dto.FeedbackDto;
import com.nirmaan.entity.Feedback;
import com.nirmaan.entity.Student;
import com.nirmaan.entity.Trainer;
import com.nirmaan.entity.Course;
import com.nirmaan.exception.ResourceNotFoundException;
import com.nirmaan.repository.FeedbackRepository;
import com.nirmaan.repository.StudentRepository;
import com.nirmaan.repository.TrainerRepository;
import com.nirmaan.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

	private final FeedbackRepository feedbackRepository;
	private final StudentRepository studentRepository;
	private final TrainerRepository trainerRepository;
	private final CourseRepository courseRepository;

	public FeedbackDto submitFeedback(FeedbackDto feedbackDto, Long studentId) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student not found"));

		Feedback feedback = new Feedback();
		feedback.setStudent(student);
		feedback.setFeedbackType(feedbackDto.getFeedbackType());
		feedback.setRating(feedbackDto.getRating());
		feedback.setComments(feedbackDto.getComments());
		feedback.setAnonymous(feedbackDto.isAnonymous());
		feedback.setSubmittedAt(LocalDateTime.now());

		if (feedbackDto.getTrainerName() != null) {
			// Find trainer by name (simplified - should be by ID in real implementation)
		}

		if (feedbackDto.getCourseName() != null) {
			// Find course by name (simplified - should be by ID in real implementation)
		}

		feedback = feedbackRepository.save(feedback);
		return convertToDto(feedback);
	}

	public List<FeedbackDto> getAllFeedback() {
		return feedbackRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
	}

	public List<FeedbackDto> getFeedbackByTrainer(Long trainerId) {
		Trainer trainer = trainerRepository.findById(trainerId)
				.orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

		return feedbackRepository.findByTrainer(trainer).stream().map(this::convertToDto).collect(Collectors.toList());
	}

	private FeedbackDto convertToDto(Feedback feedback) {
		FeedbackDto dto = new FeedbackDto();
		dto.setId(feedback.getId());
		if (!feedback.isAnonymous()) {
			dto.setStudentName(feedback.getStudent().getUser().getFirstName() + " "
					+ feedback.getStudent().getUser().getLastName());
		}
		if (feedback.getTrainer() != null) {
			dto.setTrainerName(feedback.getTrainer().getUser().getFirstName() + " "
					+ feedback.getTrainer().getUser().getLastName());
		}
		if (feedback.getCourse() != null) {
			dto.setCourseName(feedback.getCourse().getCourseName());
		}
		dto.setFeedbackType(feedback.getFeedbackType());
		dto.setRating(feedback.getRating());
		dto.setComments(feedback.getComments());
		dto.setAnonymous(feedback.isAnonymous());
		dto.setSubmittedAt(feedback.getSubmittedAt());
		return dto;
	}
}
