package Accessment.LibraryManagment_BE.repositories;

import Accessment.LibraryManagment_BE.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book,Long> {

    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM Book b")
    long getTotalAvailableBooks();



}
