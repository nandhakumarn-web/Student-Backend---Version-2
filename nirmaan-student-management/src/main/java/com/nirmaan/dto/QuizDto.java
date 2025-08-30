package com.nirmaan.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.nirmaan.enums.CourseType;

@Data
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String trainerName;
    private CourseType courseType;
    private String batchName;
    private Integer timeLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    private List<QuestionDto> questions;
}
