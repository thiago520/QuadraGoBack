package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.StudentDTO;
import com.quadrago.backend.dtos.StudentResponseDTO;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/students", produces = "application/json")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<StudentResponseDTO>> list() {
        log.debug("Listing students");
        List<StudentResponseDTO> result = studentService.list();
        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Student> create(@RequestBody @Valid StudentDTO dto) {
        log.info("Creating student: email='{}'", dto.getEmail());
        Student created = studentService.create(dto);
        log.info("Student created: id={}, email='{}'", created.getId(), created.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Student> findById(@PathVariable Long id) {
        log.debug("Fetching student id={}", id);
        return studentService.findById(id)
                .map(student -> {
                    log.debug("Student found id={}", id);
                    return ResponseEntity.ok(student);
                })
                .orElseGet(() -> {
                    log.warn("Student not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Student> update(@PathVariable Long id, @RequestBody @Valid StudentDTO dto) {
        log.info("Updating student id={}", id);
        return studentService.update(id, dto)
                .map(updated -> {
                    log.info("Student updated id={}", id);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> {
                    log.warn("Student not found for update id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("Deleting student id={}", id);
        if (studentService.delete(id)) {
            log.info("Student deleted id={}", id);
            return ResponseEntity.noContent().build();
        }
        log.warn("Student not found for delete id={}", id);
        return ResponseEntity.notFound().build();
    }
}
