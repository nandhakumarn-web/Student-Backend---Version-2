package com.nirmaan.repository;

import com.nirmaan.entity.Course;
import com.nirmaan.enums.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
	Optional<Course> findByCourseType(CourseType courseType);

	List<Course> findByActiveTrue();
}