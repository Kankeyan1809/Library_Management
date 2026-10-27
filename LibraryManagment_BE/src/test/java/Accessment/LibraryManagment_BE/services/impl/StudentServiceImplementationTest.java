package Accessment.LibraryManagment_BE.services.impl;

import Accessment.LibraryManagment_BE.dtos.request.StudentRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentFineResponseDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;
import Accessment.LibraryManagment_BE.entity.Book;
import Accessment.LibraryManagment_BE.entity.BorrowRecord;
import Accessment.LibraryManagment_BE.entity.Student;
import Accessment.LibraryManagment_BE.mapper.StudentMapper;
import Accessment.LibraryManagment_BE.repositories.BookRepository;
import Accessment.LibraryManagment_BE.repositories.BorrowRecordRepository;
import Accessment.LibraryManagment_BE.repositories.StudentRepository;
import Accessment.LibraryManagment_BE.services.FineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentServiceImplementation Unit Tests")
class StudentServiceImplementationTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private FineService fineService;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private StudentServiceImplementation studentService;

    private Student student;
    private StudentRequestDto requestDto;
    private StudentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setStudentId(1L);
        student.setStudentName("John Doe");
        student.setAge(22);
        student.setGender("Male");
        student.setCourse("Computer Science");
        student.setFineAmount(0);

        requestDto = new StudentRequestDto();
        requestDto.setStudentName("John Doe");
        requestDto.setAge(22);
        requestDto.setGender("Male");
        requestDto.setCourse("Computer Science");

        responseDto = new StudentResponseDto();
        responseDto.setStudentId(1L);
        responseDto.setStudentName("John Doe");
        responseDto.setAge(22);
        responseDto.setGender("Male");
        responseDto.setCourse("Computer Science");
        responseDto.setFineAmount(0);
    }

    @Nested
    @DisplayName("registerStudent Tests")
    class RegisterStudentTests {

        @Test
        @DisplayName("Should successfully register a student and return response DTO")
        void registerStudent_Success() {
            // Arrange
            when(studentMapper.toEntity(requestDto)).thenReturn(student);
            when(studentRepository.save(student)).thenReturn(student);
            when(studentMapper.toDto(student)).thenReturn(responseDto);

            // Act
            StudentResponseDto result = studentService.registerStudent(requestDto);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getStudentId());
            assertEquals("John Doe", result.getStudentName());
            assertEquals(22, result.getAge());
            assertEquals("Male", result.getGender());
            assertEquals("Computer Science", result.getCourse());
            assertEquals(0, result.getFineAmount());

            verify(studentMapper, times(1)).toEntity(requestDto);
            verify(studentRepository, times(1)).save(student);
            verify(studentMapper, times(1)).toDto(student);
        }

        @Test
        @DisplayName("Should throw exception when repository save fails")
        void registerStudent_WhenRepositoryThrowsException_ShouldPropagateException() {
            // Arrange
            when(studentMapper.toEntity(requestDto)).thenReturn(student);
            when(studentRepository.save(student)).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    studentService.registerStudent(requestDto));

            assertEquals("Database error", exception.getMessage());
            verify(studentMapper, times(1)).toEntity(requestDto);
            verify(studentRepository, times(1)).save(student);
            verify(studentMapper, never()).toDto(any(Student.class));
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return student by ID when fineAmount is already set and greater than 0")
        void getById_Success_WhenFineAmountAlreadySet() {
            // Arrange
            student.setFineAmount(50);
            responseDto.setFineAmount(50);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(studentMapper.toDto(student)).thenReturn(responseDto);

            // Act
            StudentResponseDto result = studentService.getById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getStudentId());
            assertEquals(50, result.getFineAmount());

            verify(studentRepository, times(1)).findById(1L);
            verifyNoInteractions(fineService);
            verify(studentMapper, times(1)).toDto(student);
        }

        @Test
        @DisplayName("Should fetch fine and update student fineAmount when fineAmount is 0")
        void getById_Success_WhenFineAmountIsZero_UpdatesFineFromFineService() {
            // Arrange
            student.setFineAmount(0);
            StudentFineResponseDto fineResponse = StudentFineResponseDto.builder()
                    .studentId(1L)
                    .grandTotalPaid(35.6)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(fineService.getStudentFine(1L)).thenReturn(fineResponse);
            when(studentMapper.toDto(student)).thenReturn(responseDto);

            // Act
            StudentResponseDto result = studentService.getById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(36, student.getFineAmount()); // Math.round(35.6) = 36
            verify(studentRepository, times(1)).findById(1L);
            verify(fineService, times(1)).getStudentFine(1L);
            verify(studentMapper, times(1)).toDto(student);
        }

        @Test
        @DisplayName("Should set fineAmount to 0 when student fineAmount is null and fine response grandTotalPaid is zero")
        void getById_Success_WhenFineAmountIsNull_SetsFineToZero() {
            // Arrange
            student.setFineAmount(null);
            StudentFineResponseDto fineResponse = StudentFineResponseDto.builder()
                    .studentId(1L)
                    .grandTotalPaid(0.0)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(fineService.getStudentFine(1L)).thenReturn(fineResponse);
            when(studentMapper.toDto(student)).thenReturn(responseDto);

            // Act
            StudentResponseDto result = studentService.getById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(0, student.getFineAmount());
            verify(fineService, times(1)).getStudentFine(1L);
            verify(studentMapper, times(1)).toDto(student);
        }

        @Test
        @DisplayName("Should handle exception from FineService and default fineAmount to 0 when null")
        void getById_Success_WhenFineServiceThrowsException_HandledGracefully() {
            // Arrange
            student.setFineAmount(null);
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(fineService.getStudentFine(1L)).thenThrow(new RuntimeException("Fine service unavailable"));
            when(studentMapper.toDto(student)).thenReturn(responseDto);

            // Act
            StudentResponseDto result = studentService.getById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(0, student.getFineAmount());
            verify(fineService, times(1)).getStudentFine(1L);
            verify(studentMapper, times(1)).toDto(student);
        }

        @Test
        @DisplayName("Should throw RuntimeException when student ID is not found")
        void getById_ThrowsException_WhenStudentNotFound() {
            // Arrange
            when(studentRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    studentService.getById(999L));

            assertEquals("Student Not Found", exception.getMessage());
            verify(studentRepository, times(1)).findById(999L);
            verifyNoInteractions(fineService);
            verifyNoInteractions(studentMapper);
        }
    }

    @Nested
    @DisplayName("getAllStudents Tests")
    class GetAllStudentsTests {

        @Test
        @DisplayName("Should return all students and update fineAmount for students with zero fineAmount")
        void getAllStudents_Success_UpdatesFineForStudentsWithZeroFine() {
            // Arrange
            Student student1 = new Student(1L, "Student One", 20, "Female", "Physics", 0);
            Student student2 = new Student(2L, "Student Two", 21, "Male", "Math", 80);

            StudentResponseDto dto1 = new StudentResponseDto();
            dto1.setStudentId(1L);
            dto1.setFineAmount(50);

            StudentResponseDto dto2 = new StudentResponseDto();
            dto2.setStudentId(2L);
            dto2.setFineAmount(80);

            StudentFineResponseDto fineResponse = StudentFineResponseDto.builder()
                    .studentId(1L)
                    .grandTotalPaid(50.0)
                    .build();

            when(studentRepository.findAll()).thenReturn(List.of(student1, student2));
            when(fineService.getStudentFine(1L)).thenReturn(fineResponse);
            when(studentMapper.toDto(student1)).thenReturn(dto1);
            when(studentMapper.toDto(student2)).thenReturn(dto2);

            // Act
            List<StudentResponseDto> results = studentService.getAllStudents();

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals(50, student1.getFineAmount());
            assertEquals(80, student2.getFineAmount());

            verify(studentRepository, times(1)).findAll();
            verify(fineService, times(1)).getStudentFine(1L);
            verify(fineService, never()).getStudentFine(2L);
            verify(studentMapper, times(1)).toDto(student1);
            verify(studentMapper, times(1)).toDto(student2);
        }

        @Test
        @DisplayName("Should handle fineService exception gracefully when fetching all students")
        void getAllStudents_Success_FineServiceThrowsException_HandledGracefully() {
            // Arrange
            Student studentWithNullFine = new Student(1L, "Student One", 20, "Female", "Physics", null);
            StudentResponseDto dto = new StudentResponseDto();
            dto.setStudentId(1L);

            when(studentRepository.findAll()).thenReturn(List.of(studentWithNullFine));
            when(fineService.getStudentFine(1L)).thenThrow(new RuntimeException("Fine service failure"));
            when(studentMapper.toDto(studentWithNullFine)).thenReturn(dto);

            // Act
            List<StudentResponseDto> results = studentService.getAllStudents();

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(0, studentWithNullFine.getFineAmount());

            verify(studentRepository, times(1)).findAll();
            verify(fineService, times(1)).getStudentFine(1L);
            verify(studentMapper, times(1)).toDto(studentWithNullFine);
        }

        @Test
        @DisplayName("Should return empty list when no students exist")
        void getAllStudents_WhenEmpty_ReturnsEmptyList() {
            // Arrange
            when(studentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<StudentResponseDto> results = studentService.getAllStudents();

            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());
            verify(studentRepository, times(1)).findAll();
            verifyNoInteractions(fineService);
            verifyNoInteractions(studentMapper);
        }
    }

    @Nested
    @DisplayName("updateStudent Tests")
    class UpdateStudentTests {

        @Test
        @DisplayName("Should update existing student successfully")
        void updateStudent_Success() {
            // Arrange
            Student existingStudent = new Student(1L, "Old Name", 20, "Male", "Old Course", 0);
            Student updatedStudent = new Student(1L, "New Name", 21, "Male", "New Course", 0);
            StudentResponseDto updatedDto = new StudentResponseDto();
            updatedDto.setStudentId(1L);
            updatedDto.setStudentName("New Name");
            updatedDto.setAge(21);
            updatedDto.setCourse("New Course");

            StudentRequestDto updateRequest = new StudentRequestDto();
            updateRequest.setStudentName("New Name");
            updateRequest.setAge(21);
            updateRequest.setCourse("New Course");

            when(studentRepository.findById(1L)).thenReturn(Optional.of(existingStudent));
            doAnswer(invocation -> {
                existingStudent.setStudentName("New Name");
                existingStudent.setAge(21);
                existingStudent.setCourse("New Course");
                return null;
            }).when(studentMapper).updateEntityfromDto(updateRequest, existingStudent);

            when(studentRepository.save(existingStudent)).thenReturn(updatedStudent);
            when(studentMapper.toDto(updatedStudent)).thenReturn(updatedDto);

            // Act
            StudentResponseDto result = studentService.updateStudent(1L, updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getStudentId());
            assertEquals("New Name", result.getStudentName());
            assertEquals(21, result.getAge());
            assertEquals("New Course", result.getCourse());

            verify(studentRepository, times(1)).findById(1L);
            verify(studentMapper, times(1)).updateEntityfromDto(updateRequest, existingStudent);
            verify(studentRepository, times(1)).save(existingStudent);
            verify(studentMapper, times(1)).toDto(updatedStudent);
        }

        @Test
        @DisplayName("Should throw RuntimeException when updating non-existent student")
        void updateStudent_ThrowsException_WhenStudentNotFound() {
            // Arrange
            when(studentRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    studentService.updateStudent(999L, requestDto));

            assertEquals("Student Not Found", exception.getMessage());
            verify(studentRepository, times(1)).findById(999L);
            verify(studentMapper, never()).updateEntityfromDto(any(), any());
            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteStudent Tests")
    class DeleteStudentTests {

        @Test
        @DisplayName("Should delete student when student has no borrow records")
        void deleteStudent_Success_WithoutBorrowRecords() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(borrowRecordRepository.findByStudentStudentId(1L)).thenReturn(Collections.emptyList());

            // Act
            studentService.deleteStudent(1L);

            // Assert
            verify(studentRepository, times(1)).findById(1L);
            verify(borrowRecordRepository, times(1)).findByStudentStudentId(1L);
            verify(borrowRecordRepository, never()).deleteAll(any());
            verify(borrowRecordRepository, never()).flush();
            verifyNoInteractions(bookRepository);
            verify(studentRepository, times(1)).delete(student);
            verify(studentRepository, times(1)).flush();
        }

        @Test
        @DisplayName("Should restore book quantities, delete borrow records, and delete student")
        void deleteStudent_Success_WithBorrowRecordsRestoresBookQuantities() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            Book book1 = new Book(101L, "Clean Code", "Robert Martin", "Programming", 4L);
            BorrowRecord record1 = new BorrowRecord();
            record1.setBorrowRecordId(1001L);
            record1.setBook(book1);
            record1.setNumberOfBooks(2L);

            Book book2 = new Book(102L, "Refactoring", "Martin Fowler", "Programming", null);
            BorrowRecord record2 = new BorrowRecord();
            record2.setBorrowRecordId(1002L);
            record2.setBook(book2);
            record2.setNumberOfBooks(1L);

            List<BorrowRecord> records = new ArrayList<>(List.of(record1, record2));
            when(borrowRecordRepository.findByStudentStudentId(1L)).thenReturn(records);

            // Act
            studentService.deleteStudent(1L);

            // Assert
            // Book 1: initial quantity 4 + restored 2 = 6
            assertEquals(6L, book1.getAvailableQuantity());
            verify(bookRepository, times(1)).save(book1);

            // Book 2: initial quantity null (defaulted to 0) + restored 1 = 1
            assertEquals(1L, book2.getAvailableQuantity());
            verify(bookRepository, times(1)).save(book2);

            verify(borrowRecordRepository, times(1)).deleteAll(records);
            verify(borrowRecordRepository, times(1)).flush();
            verify(studentRepository, times(1)).delete(student);
            verify(studentRepository, times(1)).flush();
        }

        @Test
        @DisplayName("Should handle borrow records with null book or zero books without calling bookRepository save")
        void deleteStudent_Success_WithBorrowRecordsHavingNullBookOrZeroBooks() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            BorrowRecord recordWithNullBook = new BorrowRecord();
            recordWithNullBook.setBorrowRecordId(1001L);
            recordWithNullBook.setBook(null);
            recordWithNullBook.setNumberOfBooks(2L);

            Book book2 = new Book(102L, "Book with zero borrowed", "Author", "Tech", 5L);
            BorrowRecord recordWithZeroBooks = new BorrowRecord();
            recordWithZeroBooks.setBorrowRecordId(1002L);
            recordWithZeroBooks.setBook(book2);
            recordWithZeroBooks.setNumberOfBooks(0L);

            List<BorrowRecord> records = List.of(recordWithNullBook, recordWithZeroBooks);
            when(borrowRecordRepository.findByStudentStudentId(1L)).thenReturn(records);

            // Act
            studentService.deleteStudent(1L);

            // Assert
            verifyNoInteractions(bookRepository);
            verify(borrowRecordRepository, times(1)).deleteAll(records);
            verify(borrowRecordRepository, times(1)).flush();
            verify(studentRepository, times(1)).delete(student);
            verify(studentRepository, times(1)).flush();
        }

        @Test
        @DisplayName("Should throw RuntimeException when deleting non-existent student")
        void deleteStudent_ThrowsException_WhenStudentNotFound() {
            // Arrange
            when(studentRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    studentService.deleteStudent(999L));

            assertEquals("Student Not Found", exception.getMessage());
            verify(studentRepository, times(1)).findById(999L);
            verifyNoInteractions(borrowRecordRepository);
            verifyNoInteractions(bookRepository);
            verify(studentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getByGender Tests")
    class GetByGenderTests {

        @Test
        @DisplayName("Should return matching students by gender")
        void getByGender_Success_ReturnsMatchingStudents() {
            // Arrange
            Student student1 = new Student(1L, "John Doe", 22, "Male", "CS", 0);
            Student student2 = new Student(2L, "Bob Smith", 24, "Male", "EE", 0);

            StudentResponseDto dto1 = new StudentResponseDto();
            dto1.setStudentId(1L);
            dto1.setGender("Male");

            StudentResponseDto dto2 = new StudentResponseDto();
            dto2.setStudentId(2L);
            dto2.setGender("Male");

            when(studentRepository.findByGender("Male")).thenReturn(List.of(student1, student2));
            when(studentMapper.toDto(student1)).thenReturn(dto1);
            when(studentMapper.toDto(student2)).thenReturn(dto2);

            // Act
            List<StudentResponseDto> results = studentService.getByGender("Male");

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals("Male", results.get(0).getGender());
            assertEquals("Male", results.get(1).getGender());

            verify(studentRepository, times(1)).findByGender("Male");
            verify(studentMapper, times(1)).toDto(student1);
            verify(studentMapper, times(1)).toDto(student2);
        }

        @Test
        @DisplayName("Should return empty list when no students match the gender")
        void getByGender_WhenNoneFound_ReturnsEmptyList() {
            // Arrange
            when(studentRepository.findByGender("Non-Binary")).thenReturn(Collections.emptyList());

            // Act
            List<StudentResponseDto> results = studentService.getByGender("Non-Binary");

            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());
            verify(studentRepository, times(1)).findByGender("Non-Binary");
            verifyNoInteractions(studentMapper);
        }
    }
}
