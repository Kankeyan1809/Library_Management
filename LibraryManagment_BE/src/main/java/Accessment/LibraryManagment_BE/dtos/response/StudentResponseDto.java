package Accessment.LibraryManagment_BE.dtos.response;


import lombok.Data;

import java.math.BigDecimal;

@Data

public class StudentResponseDto {

    private Long studentId;

    private String studentName;

    private Integer age;

    private String gender;

    private String course;

    private Integer fineAmount;
}
