package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findById(Long id) {
        if (id == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto create(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateEmailException("Email " + userDto.getEmail() + " уже используется");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        if (id == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (!userDto.getEmail().equals(existingUser.getEmail()) &&
                    userRepository.existsByEmail(userDto.getEmail())) {
                throw new DuplicateEmailException("Email " + userDto.getEmail() + " уже используется");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.update(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }

        if (userRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        userRepository.deleteById(id);
    }
}
