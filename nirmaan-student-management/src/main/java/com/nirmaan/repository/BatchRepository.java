package com.nirmaan.repository;

import com.nirmaan.entity.Batch;
import com.nirmaan.entity.Trainer;
import com.nirmaan.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
	List<Batch> findByTrainer(Trainer trainer);

	List<Batch> findByCourse(Course course);

	List<Batch> findByActiveTrue();
}
