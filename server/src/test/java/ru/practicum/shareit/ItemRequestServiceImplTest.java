package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestWithItem;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User requestor;
    private User otherUser;
    private User ownerOfItem;

    @BeforeEach
    public void setUp() {
        requestor = new User();
        requestor.setName("Requestor");
        requestor.setEmail("requestor@yandex.ru");
        requestor = userRepository.save(requestor);

        otherUser = new User();
        otherUser.setName("OtherUser");
        otherUser.setEmail("other@yandex.ru");
        otherUser = userRepository.save(otherUser);

        ownerOfItem = new User();
        ownerOfItem.setName("Owner");
        ownerOfItem.setEmail("owner@yandex.ru");
        ownerOfItem = userRepository.save(ownerOfItem);
    }

    @Test
    public void shouldCreateRequest() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");

        ItemRequestDto created = itemRequestService.createRequest(dto, requestor.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        assertThat(created.getCreated()).isNotNull();
    }

    @Test
    public void shouldThrowExceptionWhenUserNotFoundInCreate() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");

        assertThatThrownBy(() -> itemRequestService.createRequest(dto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    public void shouldGetAllRequestsByRequestor() {
        ItemRequest request1 = createRequest("Need a drill", requestor);
        ItemRequest request2 = createRequest("Need a screwdriver", requestor);
        itemRequestRepository.saveAll(List.of(request1, request2));

        List<ItemRequestWithItem> requests = itemRequestService.getRequests(requestor.getId());

        assertThat(requests).hasSize(2);
        assertThat(requests.stream().map(ItemRequestWithItem::getDescription))
                .containsExactlyInAnyOrder("Need a drill", "Need a screwdriver");
    }

    @Test
    public void shouldReturnEmptyListWhenRequestorHasNoRequests() {
        List<ItemRequestWithItem> requests = itemRequestService.getRequests(otherUser.getId());

        assertThat(requests).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenRequestorNotFound() {
        assertThatThrownBy(() -> itemRequestService.getRequests(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    @Test
    public void shouldGetAllOtherRequests() {
        ItemRequest request1 = createRequest("Need a drill", requestor);
        itemRequestRepository.save(request1);

        ItemRequest request2 = createRequest("Need a laptop", otherUser);
        itemRequestRepository.save(request2);

        List<ItemRequestDto> requests = itemRequestService.getAllRequests(ownerOfItem.getId());

        assertThat(requests).hasSize(2);
        assertThat(requests.stream().map(ItemRequestDto::getDescription))
                .contains("Need a drill", "Need a laptop");
    }

    @Test
    public void shouldReturnEmptyListWhenNoOtherRequests() {
        List<ItemRequestDto> requests = itemRequestService.getAllRequests(otherUser.getId());

        assertThat(requests).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenUserNotFoundInGetAllRequests() {
        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    @Test
    public void shouldGetRequestById() {
        ItemRequest request = createRequest("Need a drill", requestor);
        itemRequestRepository.save(request);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Powerful");
        item.setAvailable(true);
        item.setOwner(ownerOfItem);
        item.setRequest(request);
        itemRepository.save(item);

        ItemRequestWithItem found = itemRequestService.getRequestById(request.getId(), otherUser.getId());

        assertThat(found.getId()).isEqualTo(request.getId());
        assertThat(found.getDescription()).isEqualTo("Need a drill");
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    public void shouldGetRequestWithoutItems() {
        ItemRequest request = createRequest("Need a drill", requestor);
        itemRequestRepository.save(request);

        ItemRequestWithItem found = itemRequestService.getRequestById(request.getId(), otherUser.getId());

        assertThat(found.getId()).isEqualTo(request.getId());
        assertThat(found.getItems()).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenRequestNotFound() {
        assertThatThrownBy(() -> itemRequestService.getRequestById(999L, requestor.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос с id=999 не найден");
    }

    @Test
    public void shouldThrowExceptionWhenUserNotFoundInGetRequestById() {
        ItemRequest request = createRequest("Need a drill", requestor);
        itemRequestRepository.save(request);

        assertThatThrownBy(() -> itemRequestService.getRequestById(request.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    private ItemRequest createRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }
}