package Accessment.LibraryManagment_BE.services.impl;


import Accessment.LibraryManagment_BE.dtos.request.StudentRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;
import Accessment.LibraryManagment_BE.entity.Student;
import Accessment.LibraryManagment_BE.mapper.StudentMapper;
import Accessment.LibraryManagment_BE.repositories.StudentRepository;
import Accessment.LibraryManagment_BE.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import Accessment.LibraryManagment_BE.dtos.response.StudentFineResponseDto;
import Accessment.LibraryManagment_BE.entity.Book;
import Accessment.LibraryManagment_BE.entity.BorrowRecord;
import Accessment.LibraryManagment_BE.repositories.BookRepository;
import Accessment.LibraryManagment_BE.repositories.BorrowRecordRepository;
import Accessment.LibraryManagment_BE.services.FineService;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImplementation implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final FineService fineService;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;

    @Override
    public StudentResponseDto registerStudent(StudentRequestDto studentRequestDto){
        Student student = studentMapper.toEntity(studentRequestDto);

        Student saved= studentRepository.save(student);

        return studentMapper.toDto(saved);
    }
    @Override
    public List<StudentResponseDto> getAllStudents(){

        List<Student> students=studentRepository.findAll();
        for (Student student : students) {
            if (student.getFineAmount() == null || student.getFineAmount() == 0) {
                try {
                    StudentFineResponseDto fine = fineService.getStudentFine(student.getStudentId());
                    if (fine != null && fine.getGrandTotalPaid() > 0) {
                        student.setFineAmount((int) Math.round(fine.getGrandTotalPaid()));
                    } else if (student.getFineAmount() == null) {
                        student.setFineAmount(0);
                    }
                } catch (Exception ignored) {
                    if (student.getFineAmount() == null) {
                        student.setFineAmount(0);
                    }
                }
            }
        }
        return students.stream().map(studentMapper::toDto).toList();

    }
    @Override
    public StudentResponseDto updateStudent( Long id, StudentRequestDto studentRequestDto){
        Student student=studentRepository.findById(id).orElseThrow(()->new RuntimeException("Student Not Found"));
        studentMapper.updateEntityfromDto(studentRequestDto,student);

        Student updated=studentRepository.save(student);

        return studentMapper.toDto(updated);

    }
    @Override
    @Transactional
    public void deleteStudent(Long id){
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student Not Found"));
        List<BorrowRecord> records = borrowRecordRepository.findByStudentStudentId(id);
        if (records != null && !records.isEmpty()) {
            for (BorrowRecord record : records) {
                if (record.getBook() != null && record.getNumberOfBooks() != null && record.getNumberOfBooks() > 0) {
                    Book book = record.getBook();
                    long currentQty = book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 0L;
                    long booksToRestore = record.getNumberOfBooks() != null ? record.getNumberOfBooks() : 0L;
                    book.setAvailableQuantity(currentQty + booksToRestore);
                    bookRepository.save(book);
                }
            }
            borrowRecordRepository.deleteAll(records);
            borrowRecordRepository.flush();
        }
        studentRepository.delete(student);
        studentRepository.flush();
    }
    @Override
    public StudentResponseDto getById(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student Not Found"));
        if (student.getFineAmount() == null || student.getFineAmount() == 0) {
            try {
                StudentFineResponseDto fine = fineService.getStudentFine(id);
                if (fine != null && fine.getGrandTotalPaid() > 0) {
                    student.setFineAmount((int) Math.round(fine.getGrandTotalPaid()));
                } else if (student.getFineAmount() == null) {
                    student.setFineAmount(0);
                }
            } catch (Exception ignored) {
                if (student.getFineAmount() == null) {
                    student.setFineAmount(0);
                }
            }
        }
        return studentMapper.toDto(student);
    }

    @Override
    public List<StudentResponseDto> getByGender(String gender) {
        return studentRepository.findByGender(gender).stream().map(studentMapper::toDto).toList();
    }
}
