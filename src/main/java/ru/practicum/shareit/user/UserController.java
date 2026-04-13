package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;


@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        if (id == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }
        return userService.findById(id);
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        return userService.create(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id,
                          @RequestBody UserDto userDto) {
        if (id == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }
        return userService.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable @Positive(message = "ID пользователя должен быть положительным") Long id) {
        if (id == null) {
            throw new ValidationException("ID пользователя не может быть null");
        }
        userService.deleteById(id);
    }
}
