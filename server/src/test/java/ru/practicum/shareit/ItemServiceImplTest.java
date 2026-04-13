package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.comments.model.CommentDto;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User booker;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@yandex.ru");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@yandex.ru");
        booker = userRepository.save(booker);

        request = new ItemRequest();
        request.setDescription("Need a drill");
        request.setRequestor(owner);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);
    }

    @Test
    void shouldCreateItem() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .build();

        ItemDto created = itemService.create(dto, owner.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Drill");
        assertThat(created.getAvailable()).isTrue();
    }

    @Test
    void shouldCreateItemWithRequest() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .requestId(request.getId())
                .build();

        ItemDto created = itemService.create(dto, owner.getId());

        assertThat(created.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInCreate() {
        ItemDto dto = ItemDto.builder().name("Drill").description("Desc").available(true).build();

        assertThatThrownBy(() -> itemService.create(dto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    @Test
    void shouldThrowExceptionWhenRequestNotFound() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Desc")
                .available(true)
                .requestId(999L)
                .build();

        assertThatThrownBy(() -> itemService.create(dto, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос с id=999 не найден");
    }

    @Test
    void shouldGetItemById() {
        Item item = createItem("Drill", "Powerful", true);
        itemRepository.save(item);

        ItemWithBookingsDto found = itemService.get(item.getId());

        assertThat(found.getId()).isEqualTo(item.getId());
        assertThat(found.getName()).isEqualTo("Drill");
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        assertThatThrownBy(() -> itemService.get(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Предмет с id=999 не найден");
    }

    @Test
    void shouldUpdateItem() {
        Item item = createItem("Drill", "Old desc", true);
        itemRepository.save(item);

        ItemDto updateDto = ItemDto.builder()
                .name("New name")
                .description("Updated desc")
                .build();

        ItemDto updated = itemService.update(item.getId(), updateDto, owner.getId());

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getDescription()).isEqualTo("Updated desc");
    }

    @Test
    void shouldUpdateOnlyPartially() {
        Item item = createItem("Drill", "Desc", true);
        itemRepository.save(item);

        ItemDto updateDto = ItemDto.builder()
                .name("New name")
                .build();

        ItemDto updated = itemService.update(item.getId(), updateDto, owner.getId());

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getDescription()).isEqualTo("Desc");
    }

    @Test
    void shouldThrowExceptionWhenNotOwnerUpdatesItem() {
        Item item = createItem("Drill", "Desc", true);
        itemRepository.save(item);

        ItemDto updateDto = ItemDto.builder().name("New").build();

        assertThatThrownBy(() -> itemService.update(item.getId(), updateDto, booker.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Только владелец может редактировать вещь");
    }

    @Test
    void shouldThrowExceptionWhenItemToUpdateNotFound() {
        ItemDto updateDto = ItemDto.builder().name("New").build();

        assertThatThrownBy(() -> itemService.update(999L, updateDto, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Предмет с id=999 не найден");
    }

    @Test
    void shouldSearchItemsByAvailableText() {
        Item item1 = createItem("Drill", "Powerful drill", true);
        Item item2 = createItem("Screwdriver", "Simple screwdriver", true);
        Item item3 = createItem("Laptop", "Gaming laptop", false);
        itemRepository.saveAll(List.of(item1, item2, item3));

        Collection<ItemDto> result = itemService.search("drill");

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getName()).isEqualTo("Drill");
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsEmpty() {
        Collection<ItemDto> result = itemService.search("");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsNull() {
        Collection<ItemDto> result = itemService.search(null);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetItemsWithBookingsForOwner() {
        Item item1 = createItem("Drill", "Desc", true);
        Item item2 = createItem("Screwdriver", "Desc", true);
        itemRepository.saveAll(List.of(item1, item2));

        Booking pastBooking = createBooking(item1, booker, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1));
        Booking futureBooking = createBooking(item1, booker, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
        bookingRepository.saveAll(List.of(pastBooking, futureBooking));

        List<ItemWithBookingsDto> items = itemService.getItemsWithBookings(owner.getId());

        assertThat(items).hasSize(2);
        ItemWithBookingsDto first = items.stream()
                .filter(i -> i.getId().equals(item1.getId()))
                .findFirst().orElse(null);

        assertThat(first).isNotNull();
        assertThat(first.getNextBooking().getId()).isEqualTo(futureBooking.getId());
        assertThat(first.getLastBooking().getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void shouldGetItemsWithBookingsWhenNoBookings() {
        Item item = createItem("Drill", "Desc", true);
        itemRepository.save(item);

        List<ItemWithBookingsDto> items = itemService.getItemsWithBookings(owner.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getNextBooking()).isNull();
        assertThat(items.get(0).getLastBooking()).isNull();
    }

    @Test
    void shouldCreateComment() {
        Item item = createItem("Drill", "Desc", true);
        itemRepository.save(item);

        Booking booking = createBooking(item, booker, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Good tool!");

        CommentDto comment = itemService.createComment(item.getId(), booker.getId(), commentDto);

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getText()).isEqualTo("Good tool!");
        assertThat(comment.getAuthorName()).isEqualTo("Booker");
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoPastBooking() {
        Item item = createItem("Drill", "Desc", true);
        itemRepository.save(item);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Bad timing");

        assertThatThrownBy(() -> itemService.createComment(item.getId(), booker.getId(), commentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Комментировать может только пользователь, который брал вещь в аренду");
    }

    @Test
    void shouldThrowExceptionWhenItemNotFoundInComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test");

        assertThatThrownBy(() -> itemService.createComment(999L, booker.getId(), commentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Вещь с id=999 не найдена");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInComment() {
        Item item = createItem("Drill", "Desc", true);
        itemRepository.save(item);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test");

        assertThatThrownBy(() -> itemService.createComment(item.getId(), 999L, commentDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    private Item createItem(String name, String description, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return item;
    }

    private Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(Status.APPROVED);
        return booking;
    }
}