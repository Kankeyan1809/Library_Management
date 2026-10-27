package Accessment.LibraryManagment_BE.dtos.request;


import lombok.Data;

@Data
public class StudentRequestDto {

    private String studentName;

    private Integer age;

    private String gender;

    private String course;


}
