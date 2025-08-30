package com.nirmaan.dto;

import com.nirmaan.enums.CourseType;

import lombok.Data;

@Data
public class CourseDto {
	private Long id;
	private CourseType courseType;
	private String courseName;
	private String description;
	private Integer durationMonths;
	private String syllabus;
	private boolean active;
}
