package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("John");
        userDto.setEmail("john@yandex.ru");
    }

    @Test
    void shouldCreateUser() {
        UserDto created = userService.create(userDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("John");
        assertThat(created.getEmail()).isEqualTo("john@yandex.ru");

        User saved = userRepository.findById(created.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("John");
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        userDto.setName("");
        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Имя пользователя не может быть пустым");

        userDto.setName("   ");
        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Имя пользователя не может быть пустым");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsDuplicate() {
        userService.create(userDto);

        UserDto duplicate = new UserDto();
        duplicate.setName("Jane Doe");
        duplicate.setEmail("john@yandex.ru");

        assertThatThrownBy(() -> userService.create(duplicate))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email уже используется");
    }

    @Test
    void shouldAcceptEmailCaseInsensitive() {
        UserDto first = new UserDto();
        first.setName("Alice");
        first.setEmail("ALICE@YANDEX.RU");
        userService.create(first);

        UserDto second = new UserDto();
        second.setName("Bob");
        second.setEmail("alice@yandex.ru");

        assertThatThrownBy(() -> userService.create(second))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email уже используется");
    }

    @Test
    void shouldGetAllUsers() {
        userService.create(userDto);

        UserDto user2 = new UserDto();
        user2.setName("Jane Doe");
        user2.setEmail("jane@yandex.ru");
        userService.create(user2);

        Collection<UserDto> users = userService.getUsers();

        assertThat(users).hasSize(2);
        assertThat(users.stream().map(UserDto::getEmail))
                .containsExactlyInAnyOrder("john@yandex.ru", "jane@yandex.ru");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        Collection<UserDto> users = userService.getUsers();

        assertThat(users).isEmpty();
    }

    @Test
    void shouldGetUserById() {
        UserDto created = userService.create(userDto);

        UserDto found = userService.getUser(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo(created.getName());
        assertThat(found.getEmail()).isEqualTo(created.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    @Test
    void shouldUpdateUserName() {
        UserDto created = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");

        UserDto updated = userService.update(updateDto, created.getId());

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("john@yandex.ru");
    }

    @Test
    void shouldUpdateUserEmail() {
        UserDto created = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setEmail("new-email@yandex.ru");

        UserDto updated = userService.update(updateDto, created.getId());

        assertThat(updated.getEmail()).isEqualTo("new-email@yandex.ru");
        assertThat(updated.getName()).isEqualTo("John");
    }

    @Test
    void shouldUpdateBothNameAndEmail() {
        UserDto created = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");
        updateDto.setEmail("new-email@yandex.ru");

        UserDto updated = userService.update(updateDto, created.getId());

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new-email@yandex.ru");
    }

    @Test
    void shouldNotUpdateWhenFieldsAreNull() {
        UserDto created = userService.create(userDto);

        UserDto updateDto = new UserDto();

        UserDto updated = userService.update(updateDto, created.getId());

        assertThat(updated.getName()).isEqualTo("John");
        assertThat(updated.getEmail()).isEqualTo("john@yandex.ru");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsTakenByOtherUser() {
        UserDto user1 = new UserDto();
        user1.setName("Alice");
        user1.setEmail("alice@yandex.ru");
        userService.create(user1);

        UserDto user2 = new UserDto();
        user2.setName("Bob");
        user2.setEmail("bob@yandex.ru");
        UserDto createdUser2 = userService.create(user2);

        UserDto updateDto = new UserDto();
        updateDto.setEmail("alice@yandex.ru");

        assertThatThrownBy(() -> userService.update(updateDto, createdUser2.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email уже используется другим пользователем");
    }

    @Test
    void shouldAllowUpdatingToOwnEmail() {
        UserDto created = userService.create(userDto);

        UserDto updateDto = new UserDto();
        updateDto.setEmail("john@yandex.ru");
        UserDto updated = userService.update(updateDto, created.getId());

        assertThat(updated.getEmail()).isEqualTo("john@yandex.ru");
    }

    @Test
    void shouldDeleteUser() {
        UserDto created = userService.create(userDto);

        userService.delete(created.getId());

        assertThatThrownBy(() -> userService.getUser(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }
}