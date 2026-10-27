package Accessment.LibraryManagment_BE.repositories;

import Accessment.LibraryManagment_BE.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface StudentRepository extends JpaRepository<Student,Long> {

    long countByGender(String gender);

    java.util.List<Student> findByGender(String gender);

}
