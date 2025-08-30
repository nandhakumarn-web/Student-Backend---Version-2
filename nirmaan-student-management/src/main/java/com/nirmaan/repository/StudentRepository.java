package com.nirmaan.repository;

import com.nirmaan.entity.Student;
import com.nirmaan.entity.Batch;
import com.nirmaan.enums.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
	Optional<Student> findByStudentId(String studentId);

	Optional<Student> findByUserId(Long userId);

	List<Student> findByBatch(Batch batch);

	List<Student> findByEnrolledCourse(CourseType courseType);

	boolean existsByStudentId(String studentId);
}
