package Accessment.LibraryManagment_BE.repositories;


import Accessment.LibraryManagment_BE.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository
        extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByStudentStudentIdAndReturnDateIsNull(Long studentId);

    Optional<BorrowRecord> findFirstByStudentStudentIdAndBookBookIdAndReturnDateIsNull(
            Long studentId, Long bookId);

    List<BorrowRecord> findByReturnDateIsNull();

    @Query("SELECT COALESCE(SUM(b.numberOfBooks), 0) " +
            "FROM BorrowRecord b " +
            "WHERE b.returnDate IS NULL")
    long getTotalBorrowedBooks();


    List<BorrowRecord> findByStudentStudentId(Long studentId);
}
