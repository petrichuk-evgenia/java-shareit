package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;

public class UserMapper {

    public static User toUser(UserDto user) {
        if (user == null) return null;

        User dto = new User();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
