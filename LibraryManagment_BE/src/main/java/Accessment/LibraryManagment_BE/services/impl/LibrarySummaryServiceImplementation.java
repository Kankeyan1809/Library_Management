package Accessment.LibraryManagment_BE.services.impl;

import Accessment.LibraryManagment_BE.dtos.response.LibrarySummaryResponseDto;
import Accessment.LibraryManagment_BE.entity.BorrowRecord;
import Accessment.LibraryManagment_BE.entity.Student;
import Accessment.LibraryManagment_BE.repositories.BookRepository;
import Accessment.LibraryManagment_BE.repositories.BorrowRecordRepository;
import Accessment.LibraryManagment_BE.repositories.StudentRepository;
import Accessment.LibraryManagment_BE.services.LibrarySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LibrarySummaryServiceImplementation implements LibrarySummaryService {

    private final StudentRepository studentRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    public LibrarySummaryResponseDto getSummary() {

        LibrarySummaryResponseDto response =
                new LibrarySummaryResponseDto();

        // Total students count
        response.setTotalStudents(studentRepository.count());

        // Total male students count
        response.setTotalMaleStudents(
                studentRepository.countByGender("M")
        );

        // Total female students count
        response.setTotalFemaleStudents(
                studentRepository.countByGender("F")
        );

        // Total registered books count
        response.setTotalRegisteredBooks(
                bookRepository.count()
        );

        // Total available books
        response.setTotalAvailableBooks(
                bookRepository.getTotalAvailableBooks()
        );

        // Total borrowed books
        long totalBorrowed =
                borrowRecordRepository.getTotalBorrowedBooks();

        response.setTotalBorrowedBooks(totalBorrowed);
        response.setTotalBooksCurrentlyBorrowed(totalBorrowed);

        // Calculate distribution of students by current borrowed books
        List<Student> allStudents = studentRepository.findAll();
        List<BorrowRecord> activeRecords = borrowRecordRepository.findByReturnDateIsNull();

        Map<Long, Long> studentBorrowedMap = new HashMap<>();
        if (activeRecords != null) {
            for (BorrowRecord record : activeRecords) {
                if (record.getStudent() != null && record.getStudent().getStudentId() != null) {
                    long qty = (record.getNumberOfBooks() != null && record.getNumberOfBooks() > 0)
                            ? record.getNumberOfBooks()
                            : 0L;
                    studentBorrowedMap.merge(record.getStudent().getStudentId(), qty, Long::sum);
                }
            }
        }

        long count0 = 0;
        long count1 = 0;
        long count2 = 0;
        long count3 = 0;
        long count4 = 0;
        long count5 = 0;

        for (Student student : allStudents) {
            long borrowed = studentBorrowedMap.getOrDefault(student.getStudentId(), 0L);
            if (borrowed <= 0) {
                count0++;
            } else if (borrowed == 1) {
                count1++;
            } else if (borrowed == 2) {
                count2++;
            } else if (borrowed == 3) {
                count3++;
            } else if (borrowed == 4) {
                count4++;
            } else {
                count5++;
            }
        }

        response.setStudentsBorrowing0Books(count0);
        response.setStudentsBorrowing1Book(count1);
        response.setStudentsBorrowing2Books(count2);
        response.setStudentsBorrowing3Books(count3);
        response.setStudentsBorrowing4Books(count4);
        response.setStudentsBorrowing5Books(count5);

        return response;
    }
}
