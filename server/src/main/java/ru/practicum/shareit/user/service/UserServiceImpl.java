package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new NotFoundException("Имя пользователя не может быть пустым");
        }

        if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new ValidationException("Email уже используется");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        log.info("Создан пользователь с id={}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public Collection<UserDto> getUsers() {
        Collection<User> users = userRepository.findAll();
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(UserDto userDto, Long id) {
        UserDto existingUser = getUser(id);
        if (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(userDto.getEmail(), id)) {
                throw new ValidationException("Email уже используется другим пользователем");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }

        User updatedUser = userRepository.save(UserMapper.toUser(existingUser));
        log.info("Обновлён пользователь с id={}", updatedUser.getId());

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(UserMapper.toUser(getUser(id)));
    }
}
