package Accessment.LibraryManagment_BE.services;

import Accessment.LibraryManagment_BE.dtos.request.StudentRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;

import java.util.List;

public interface StudentService {


    StudentResponseDto registerStudent(StudentRequestDto studentRequestDto);

    List<StudentResponseDto> getAllStudents();

    StudentResponseDto updateStudent(Long id,StudentRequestDto studentRequestDto);

    void deleteStudent(Long id);
    StudentResponseDto getById(Long id);
    List<StudentResponseDto> getByGender(String gender);
}
