package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.comments.CommentRepository;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.model.CommentDto;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithBookingsDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = ItemMapper.toItem(itemDto, owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id=" + itemDto.getRequestId() + " не найден"));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemWithBookingsDto get(Long itemId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id=" + itemId + " не найден"));

        List<Comment> comments = commentRepository.findByItemId(itemId);

        LocalDateTime now = LocalDateTime.now();

        List<Booking> lastBookings = bookingRepository.findByItemAndStatusAndStartBeforeOrderByEndDesc(
                item, Status.APPROVED, now);
        Booking lastBooking = lastBookings.isEmpty() ? null : lastBookings.get(0);

        List<Booking> nextBookings = bookingRepository.findByItemAndStatusAndStartAfterOrderByStartAsc(
                item, Status.APPROVED, now);
        Booking nextBooking = nextBookings.isEmpty() ? null : nextBookings.get(0);

        return ItemMapper.toItemWithBookingsDto(item, null, nextBooking, comments);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto item, Long userId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id=" + itemId + " не найден"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может редактировать вещь");
        }

        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String searchText = text.toLowerCase().trim();

        List<Item> items = itemRepository.searchAvailableItems(searchText);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemWithBookingsDto> getItemsWithBookings(Long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream().map(item -> {
            List<Booking> lastBookings = bookingRepository.findByItemAndStatusAndStartBeforeOrderByEndDesc(
                    item, Status.APPROVED, now);
            Booking lastBooking = lastBookings.isEmpty() ? null : lastBookings.get(0);

            List<Booking> nextBookings = bookingRepository.findByItemAndStatusAndStartAfterOrderByStartAsc(
                    item, Status.APPROVED, now);
            Booking nextBooking = nextBookings.isEmpty() ? null : nextBookings.get(0);

            List<Comment> comments = commentRepository.findByItemId(item.getId());

            return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
        }).collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Вещь с id=" + itemId + " не найдена"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        boolean hasPastBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now());

        if (!hasPastBooking) {
            throw new ValidationException("Комментировать может только пользователь, который брал вещь в аренду и уже вернул её");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }
}
