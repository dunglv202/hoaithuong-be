package dev.dunglv202.hoaithuong.dto;

import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.mapper.ConfigMapper;
import lombok.Getter;

@Getter
public class DetailProfileDTO extends UserInfoDTO {
    private final ConfigsDTO configs;

    public DetailProfileDTO(User user, Configuration configs) {
        super(user);
        this.configs = ConfigMapper.INSTANCE.toConfigsDTO(configs);
    }
}
