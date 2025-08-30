package com.nirmaan.service;

import com.nirmaan.dto.QuizDto;
import com.nirmaan.dto.QuestionDto;
import com.nirmaan.entity.Quiz;
import com.nirmaan.entity.Question;
import com.nirmaan.entity.Trainer;
import com.nirmaan.entity.Batch;
import com.nirmaan.entity.StudentQuizAttempt;
import com.nirmaan.entity.Student;
import com.nirmaan.exception.ResourceNotFoundException;
import com.nirmaan.exception.ValidationException;
import com.nirmaan.repository.QuizRepository;
import com.nirmaan.repository.QuestionRepository;
import com.nirmaan.repository.TrainerRepository;
import com.nirmaan.repository.BatchRepository;
import com.nirmaan.repository.StudentQuizAttemptRepository;
import com.nirmaan.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

	private final QuizRepository quizRepository;
	private final QuestionRepository questionRepository;
	private final TrainerRepository trainerRepository;
	private final BatchRepository batchRepository;
	private final StudentQuizAttemptRepository studentQuizAttemptRepository;
	private final StudentRepository studentRepository;

	@Transactional
	public QuizDto createQuiz(QuizDto quizDto, Long trainerId) {
		Trainer trainer = trainerRepository.findById(trainerId)
				.orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

		Batch batch = batchRepository
				.findById(quizDto.getBatchName() != null ? Long.parseLong(quizDto.getBatchName()) : 0L).orElse(null);

		Quiz quiz = new Quiz();
		quiz.setTitle(quizDto.getTitle());
		quiz.setDescription(quizDto.getDescription());
		quiz.setTrainer(trainer);
		quiz.setCourseType(quizDto.getCourseType());
		quiz.setBatch(batch);
		quiz.setTimeLimit(quizDto.getTimeLimit());
		quiz.setStartTime(quizDto.getStartTime());
		quiz.setEndTime(quizDto.getEndTime());
		quiz.setActive(quizDto.isActive());

		quiz = quizRepository.save(quiz);

		// Create questions
		if (quizDto.getQuestions() != null) {
			for (QuestionDto questionDto : quizDto.getQuestions()) {
				Question question = new Question();
				question.setQuiz(quiz);
				question.setQuestionText(questionDto.getQuestionText());
				question.setOptionA(questionDto.getOptionA());
				question.setOptionB(questionDto.getOptionB());
				question.setOptionC(questionDto.getOptionC());
				question.setOptionD(questionDto.getOptionD());
				question.setCorrectAnswer(questionDto.getCorrectAnswer());
				question.setMarks(questionDto.getMarks());
				questionRepository.save(question);
			}
		}

		return convertToDto(quiz);
	}

	public List<QuizDto> getQuizzesByTrainer(Long trainerId) {
		Trainer trainer = trainerRepository.findById(trainerId)
				.orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

		return quizRepository.findByTrainer(trainer).stream().map(this::convertToDto).collect(Collectors.toList());
	}

	public List<QuizDto> getAvailableQuizzes() {
		return quizRepository.findByActiveTrue().stream()
				.filter(quiz -> quiz.getStartTime().isBefore(LocalDateTime.now())
						&& quiz.getEndTime().isAfter(LocalDateTime.now()))
				.map(this::convertToDto).collect(Collectors.toList());
	}

	public StudentQuizAttempt submitQuizAttempt(Long studentId, Long quizId, Map<Long, String> answers) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student not found"));

		Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

		if (studentQuizAttemptRepository.findByStudentAndQuiz(student, quiz).isPresent()) {
			throw new ValidationException("Quiz already attempted");
		}

		List<Question> questions = questionRepository.findByQuiz(quiz);
		int correctAnswers = 0;
		int totalQuestions = questions.size();

		for (Question question : questions) {
			String studentAnswer = answers.get(question.getId());
			if (studentAnswer != null && studentAnswer.equals(question.getCorrectAnswer())) {
				correctAnswers++;
			}
		}

		int score = (correctAnswers * 100) / totalQuestions;

		StudentQuizAttempt attempt = new StudentQuizAttempt();
		attempt.setStudent(student);
		attempt.setQuiz(quiz);
		attempt.setStartTime(LocalDateTime.now().minusMinutes(quiz.getTimeLimit()));
		attempt.setEndTime(LocalDateTime.now());
		attempt.setTotalQuestions(totalQuestions);
		attempt.setCorrectAnswers(correctAnswers);
		attempt.setScore(score);
		attempt.setAnswers(answers.toString()); // Convert to JSON in real implementation
		attempt.setCompleted(true);

		return studentQuizAttemptRepository.save(attempt);
	}

	private QuizDto convertToDto(Quiz quiz) {
		QuizDto dto = new QuizDto();
		dto.setId(quiz.getId());
		dto.setTitle(quiz.getTitle());
		dto.setDescription(quiz.getDescription());
		if (quiz.getTrainer() != null) {
			dto.setTrainerName(
					quiz.getTrainer().getUser().getFirstName() + " " + quiz.getTrainer().getUser().getLastName());
		}
		dto.setCourseType(quiz.getCourseType());
		if (quiz.getBatch() != null) {
			dto.setBatchName(quiz.getBatch().getBatchName());
		}
		dto.setTimeLimit(quiz.getTimeLimit());
		dto.setStartTime(quiz.getStartTime());
		dto.setEndTime(quiz.getEndTime());
		dto.setActive(quiz.isActive());

		List<Question> questions = questionRepository.findByQuiz(quiz);
		dto.setQuestions(questions.stream().map(this::convertQuestionToDto).collect(Collectors.toList()));

		return dto;
	}

	private QuestionDto convertQuestionToDto(Question question) {
		QuestionDto dto = new QuestionDto();
		dto.setId(question.getId());
		dto.setQuestionText(question.getQuestionText());
		dto.setOptionA(question.getOptionA());
		dto.setOptionB(question.getOptionB());
		dto.setOptionC(question.getOptionC());
		dto.setOptionD(question.getOptionD());
		// Don't expose correct answer to students
		dto.setMarks(question.getMarks());
		return dto;
	}
}