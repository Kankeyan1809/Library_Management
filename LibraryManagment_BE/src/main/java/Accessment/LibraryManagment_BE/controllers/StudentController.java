package Accessment.LibraryManagment_BE.controllers;


import Accessment.LibraryManagment_BE.dtos.request.FineCalculationRequestDto;
import Accessment.LibraryManagment_BE.dtos.request.StudentRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentFineResponseDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;
import Accessment.LibraryManagment_BE.services.FineService;
import Accessment.LibraryManagment_BE.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final FineService fineService;

    @PostMapping()
    public StudentResponseDto registerStudent(@RequestBody StudentRequestDto requestDto){
        return studentService.registerStudent(requestDto);
    }

    @GetMapping()
    public List<StudentResponseDto> getAllStudents(){
        return studentService.getAllStudents();
    }

    @GetMapping("/gender/{gender}")
    public List<StudentResponseDto> getByGender(@PathVariable String gender){
        return studentService.getByGender(gender);
    }

    @PutMapping("/{id}")
    public StudentResponseDto updateStudents(
            @PathVariable Long id,
            @RequestBody StudentRequestDto studentRequestDto){
        return studentService.updateStudent(id, studentRequestDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id
    ){
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public StudentResponseDto getById(@PathVariable Long id){
        return studentService.getById(id);
    }

    @GetMapping("/{id}/fine")
    public StudentFineResponseDto getStudentFine(@PathVariable Long id){
        return fineService.getStudentFine(id);
    }

    @PostMapping("/{id}/fine")
    public StudentFineResponseDto calculateFine(
            @PathVariable Long id,
            @RequestBody(required = false) FineCalculationRequestDto requestDto){
        return fineService.calculateFine(id, requestDto);
    }

    @PostMapping("/{id}/pay/{borrowRecordId}")
    public StudentFineResponseDto payRecordFine(
            @PathVariable Long id,
            @PathVariable Long borrowRecordId){
        return fineService.payRecordFine(id, borrowRecordId);
    }

    @PostMapping("/{id}/pay-toggle/{borrowRecordId}")
    public StudentFineResponseDto toggleRecordPayment(
            @PathVariable Long id,
            @PathVariable Long borrowRecordId){
        return fineService.toggleRecordPayment(id, borrowRecordId);
    }
}
