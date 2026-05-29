package com.aiclassroom.controller;

import com.aiclassroom.entity.*;
import com.aiclassroom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public class AcademicController {

    private final DepartmentRepository departmentRepository;
    private final AcademicClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;
    private final UserRepository userRepository;

    @GetMapping("/my-assignments")
    public List<StaffAssignment> getMyAssignments() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        return staffAssignmentRepository.findByStaff_Id(user.getId());
    }

    // --- Departments ---
    @GetMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        if (department == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(departmentRepository.save(department));
    }

    // --- Classes ---
    @GetMapping("/classes")
    public List<AcademicClass> getAllClasses() {
        return classRepository.findAll();
    }

    @GetMapping("/departments/{deptId}/classes")
    public List<AcademicClass> getClassesByDept(@PathVariable UUID deptId) {
        return classRepository.findByDepartment_Id(deptId);
    }

    @PostMapping("/classes")
    public ResponseEntity<AcademicClass> createClass(@RequestBody AcademicClass academicClass) {
        if (academicClass == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(classRepository.save(academicClass));
    }

    // --- Subjects ---
    @GetMapping("/subjects")
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @GetMapping("/departments/{deptId}/subjects")
    public List<Subject> getSubjectsByDept(@PathVariable UUID deptId) {
        return subjectRepository.findByDepartment_Id(deptId);
    }

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) {
        if (subject == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(subjectRepository.save(subject));
    }

    // --- Staff Assignments ---
    @GetMapping("/assignments")
    public List<StaffAssignment> getAllAssignments() {
        return staffAssignmentRepository.findAll();
    }

    @PostMapping("/assignments")
    public ResponseEntity<StaffAssignment> createAssignment(@RequestBody StaffAssignment assignment) {
        if (assignment == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(staffAssignmentRepository.save(assignment));
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID id) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        staffAssignmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
