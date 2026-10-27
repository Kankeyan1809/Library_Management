package Accessment.LibraryManagment_BE.mapper;


import Accessment.LibraryManagment_BE.dtos.request.StudentRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;
import Accessment.LibraryManagment_BE.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface StudentMapper {

    Student toEntity(StudentRequestDto studentRequestDto);

    @Mapping(source = "studentId",target = "studentId")
    StudentResponseDto toDto(Student student);

    void updateEntityfromDto(StudentRequestDto requestDto ,@MappingTarget Student student);

}
