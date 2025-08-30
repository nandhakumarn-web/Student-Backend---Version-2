package com.nirmaan.repository;

import com.nirmaan.entity.Feedback;
import com.nirmaan.entity.Student;
import com.nirmaan.entity.Trainer;
import com.nirmaan.entity.Course;
import com.nirmaan.enums.FeedbackType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
	List<Feedback> findByStudent(Student student);

	List<Feedback> findByTrainer(Trainer trainer);

	List<Feedback> findByCourse(Course course);

	List<Feedback> findByFeedbackType(FeedbackType feedbackType);
}
