package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto user) {
        return userService.create(user);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@RequestBody UserDto user, @PathVariable Long id) {
        return userService.update(user, id);
    }

    @GetMapping
    public Collection<UserDto> getUsers() {
        return userService.getUsers();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
