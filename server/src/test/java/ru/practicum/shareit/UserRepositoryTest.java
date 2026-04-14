package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldFindUserByEmail() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@yandex.ru");
        userRepository.save(user);

        Optional<User> found = Optional.ofNullable(userRepository.findByEmail("john@yandex.ru"));

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John");
    }
}