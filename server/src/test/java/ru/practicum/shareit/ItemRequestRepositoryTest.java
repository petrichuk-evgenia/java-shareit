package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldFindAllByRequestorIdOrderByCreatedDesc() {
        User requestor = new User();
        requestor.setName("Requestor");
        requestor.setEmail("requestor@yandex.ru");
        userRepository.save(requestor);

        ItemRequest req1 = new ItemRequest();
        req1.setDescription("Need drill");
        req1.setRequestor(requestor);
        req1.setCreated(LocalDateTime.now().minusHours(2));

        ItemRequest req2 = new ItemRequest();
        req2.setDescription("Need screwdriver");
        req2.setRequestor(requestor);
        req2.setCreated(LocalDateTime.now().minusHours(1));

        itemRequestRepository.save(req1);
        itemRequestRepository.save(req2);

        List<ItemRequest> result = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestor.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(req2.getId());
        assertThat(result.get(1).getId()).isEqualTo(req1.getId());
    }

    @Test
    public void shouldFindByRequestorIdNotOrderByCreatedDesc() {
        User requestor = new User();
        requestor.setName("Requestor");
        requestor.setEmail("requestor@yandex.ru");
        userRepository.save(requestor);

        User otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail("other@yandex.ru");
        userRepository.save(otherUser);

        ItemRequest myReq = new ItemRequest();
        myReq.setDescription("My request");
        myReq.setRequestor(requestor);
        myReq.setCreated(LocalDateTime.now().minusHours(1));

        ItemRequest otherReq = new ItemRequest();
        otherReq.setDescription("Other's request");
        otherReq.setRequestor(otherUser);
        otherReq.setCreated(LocalDateTime.now());

        itemRequestRepository.save(myReq);
        itemRequestRepository.save(otherReq);

        List<ItemRequest> result = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(
                requestor.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(otherReq.getId());
    }
}