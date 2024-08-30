package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.entity.Configuration;
import dev.dunglv202.hoaithuong.entity.User;

public interface ConfigService {
    Configuration getConfigsByUser(User user);
    void saveConfigs(Configuration configs);
}
