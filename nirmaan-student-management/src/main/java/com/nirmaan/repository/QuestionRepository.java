package com.nirmaan.repository;

import com.nirmaan.entity.Question;
import com.nirmaan.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
	List<Question> findByQuiz(Quiz quiz);
}
