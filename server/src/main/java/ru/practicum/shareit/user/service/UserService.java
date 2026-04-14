package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.UserDto;

import java.util.Collection;

public interface UserService {

    UserDto create(UserDto user);

    Collection<UserDto> getUsers();

    UserDto getUser(Long id);

    UserDto update(UserDto user, Long id);

    void delete(Long id);

}
