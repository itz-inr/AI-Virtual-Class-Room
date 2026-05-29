package com.aiclassroom.controller;

import com.aiclassroom.entity.Course;
import com.aiclassroom.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getTeacherCourses(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(courseService.getCoursesByTeacher(teacherId));
    }
    
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.createCourse(course));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable UUID id, @RequestBody Course course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }
}
