package dev.dunglv202.hoaithuong.mapper;

import dev.dunglv202.hoaithuong.dto.MinimalStudentDTO;
import dev.dunglv202.hoaithuong.dto.NewStudentDTO;
import dev.dunglv202.hoaithuong.dto.StudentDTO;
import dev.dunglv202.hoaithuong.dto.UpdatedStudentDTO;
import dev.dunglv202.hoaithuong.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StudentMapper {
    StudentMapper INSTANCE = Mappers.getMapper(StudentMapper.class);

    Student toStudent(NewStudentDTO newStudent);

    StudentDTO toStudentDTO(Student student);

    MinimalStudentDTO toMinimalStudentDTO(Student student);

    @Mapping(target = "id", ignore = true)
    void mergeStudent(@MappingTarget Student old, UpdatedStudentDTO updated);
}
