package dev.dunglv202.hoaithuong.mapper;

import dev.dunglv202.hoaithuong.dto.ConfigsDTO;
import dev.dunglv202.hoaithuong.entity.Configuration;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConfigMapper {
    ConfigMapper INSTANCE = Mappers.getMapper(ConfigMapper.class);

    ConfigsDTO toConfigsDTO(Configuration config);
}
