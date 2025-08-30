package com.nirmaan.repository;

import com.nirmaan.entity.Quiz;
import com.nirmaan.entity.Trainer;
import com.nirmaan.entity.Batch;
import com.nirmaan.enums.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
	List<Quiz> findByTrainer(Trainer trainer);

	List<Quiz> findByBatch(Batch batch);

	List<Quiz> findByCourseType(CourseType courseType);

	List<Quiz> findByActiveTrue();
}
