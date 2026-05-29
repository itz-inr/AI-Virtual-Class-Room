package com.aiclassroom.service;

import com.aiclassroom.entity.Course;
import com.aiclassroom.entity.User;
import com.aiclassroom.repository.CourseRepository;
import com.aiclassroom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final com.aiclassroom.repository.StaffAssignmentRepository staffAssignmentRepository;
    private final com.aiclassroom.repository.SubjectRepository subjectRepository;
    private final com.aiclassroom.repository.AcademicClassRepository academicClassRepository;

    public List<Course> getAllCourses() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && User.Role.STUDENT.equals(user.getRole())) {
            if (user.getAcademicClass() == null) {
                return List.of(); // No class mapped, see nothing
            }
            return courseRepository.findByAcademicClass_IdAndActiveTrue(user.getAcademicClass().getId());
        }
        return courseRepository.findByActiveTrue();
    }

    public List<Course> getCoursesByTeacher(UUID teacherId) {
        return courseRepository.findByTeacher_Id(teacherId);
    }

    public Course getCourseById(UUID id) {
        if (id == null)
            throw new RuntimeException("Course ID cannot be null");
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public Course createCourse(Course course) {
        if (course.getTeacher() == null || course.getTeacher().getId() == null) {
            throw new RuntimeException("Teacher ID is required");
        }
        if (course.getSubject() == null || course.getSubject().getId() == null) {
            throw new RuntimeException("Subject mapping is required");
        }
        if (course.getAcademicClass() == null || course.getAcademicClass().getId() == null) {
            throw new RuntimeException("Class mapping is required");
        }

        UUID teacherId = course.getTeacher().getId();
        if (teacherId == null)
            throw new RuntimeException("Teacher ID is null");
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // VALIDATE MAPPING: Staff must be assigned to this Subject and Class
        boolean isMapped = staffAssignmentRepository.existsByStaff_IdAndSubject_IdAndAcademicClass_Id(
                teacher.getId(), course.getSubject().getId(), course.getAcademicClass().getId());

        if (!isMapped && !User.Role.ADMIN.equals(teacher.getRole())) {
            throw new RuntimeException("Staff is not assigned to this subject/class combination");
        }

        course.setTeacher(teacher);
        UUID subjectId = course.getSubject().getId();
        if (subjectId != null) {
            course.setSubject(subjectRepository.findById(subjectId).orElseThrow());
        }
        UUID classId = course.getAcademicClass().getId();
        if (classId != null) {
            course.setAcademicClass(academicClassRepository.findById(classId).orElseThrow());
        }

        return courseRepository.save(course);
    }

    public Course updateCourse(UUID id, Course courseDetails) {
        Course course = getCourseById(id);
        course.setName(courseDetails.getName());
        course.setDescription(courseDetails.getDescription());
        course.setAiMode(courseDetails.getAiMode());
        return courseRepository.save(course);
    }

    public void deleteCourse(UUID id) {
        Course course = getCourseById(id);
        course.setActive(false);
        courseRepository.save(course);
    }
}
