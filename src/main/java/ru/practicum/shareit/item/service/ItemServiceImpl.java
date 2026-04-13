package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Repository
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemDto> findAllByOwnerId(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(ownerId);
        return items.stream()
                .map(item -> mapToItemDtoWithBookingsAndComments(item, ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto findById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + id + " не найдена"));

        return mapToItemDtoWithoutBookings(item);
    }

    public ItemDto findById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        return mapToItemDtoWithBookingsAndComments(item, userId);
    }

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности должен быть указан");
        }

        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(userRepository.findById(ownerId).get());

        Item savedItem = itemRepository.save(item);
        return mapToItemDtoWithoutBookings(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!ownerId.equals(existingItem.getOwner().getId())) {
            throw new NotFoundException("Редактировать вещь может только её владелец");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return mapToItemDtoWithBookingsAndComments(updatedItem, ownerId);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(this::mapToItemDtoWithoutBookings)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CreateCommentDto createCommentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();

        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, now, BookingStatus.APPROVED);

        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или аренда ещё не завершена");
        }

        Comment comment = new Comment();
        comment.setText(createCommentDto.getText());
        comment.setItemId(itemId);
        comment.setAuthorId(userId);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(savedComment.getId());
        commentDto.setText(savedComment.getText());
        commentDto.setAuthorName(author.getName());
        commentDto.setCreated(savedComment.getCreated());

        return commentDto;
    }

    // Метод для маппинга с датами бронирований
    private ItemDto mapToItemDtoWithBookingsAndComments(Item item, Long userId) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());

        LocalDateTime now = LocalDateTime.now();

        if (userId.equals(item.getOwner().getId())) {
            List<Booking> lastBookings = bookingRepository.findLastBookingByItemId(item.getId(), now);
            if (!lastBookings.isEmpty()) {
                Booking lastBooking = lastBookings.get(0);
                BookingDto lastBookingDto = new BookingDto();
                lastBookingDto.setId(lastBooking.getId());
                lastBookingDto.setStart(lastBooking.getStart());
                lastBookingDto.setEnd(lastBooking.getEnd());
                lastBookingDto.setStatus(lastBooking.getStatus());
                itemDto.setLastBooking(lastBookingDto);
            }

            List<Booking> nextBookings = bookingRepository.findNextBookingByItemId(item.getId(), now);
            if (!nextBookings.isEmpty()) {
                Booking nextBooking = nextBookings.get(0);
                BookingDto nextBookingDto = new BookingDto();
                nextBookingDto.setId(nextBooking.getId());
                nextBookingDto.setStart(nextBooking.getStart());
                nextBookingDto.setEnd(nextBooking.getEnd());
                nextBookingDto.setStatus(nextBooking.getStatus());
                itemDto.setNextBooking(nextBookingDto);
            }
        } else {
            itemDto.setLastBooking(null);
            itemDto.setNextBooking(null);
        }

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> {
                    User author = userRepository.findById(comment.getAuthorId()).orElse(null);
                    CommentDto commentDto = new CommentDto();
                    commentDto.setId(comment.getId());
                    commentDto.setText(comment.getText());
                    commentDto.setCreated(comment.getCreated());
                    if (author != null) {
                        commentDto.setAuthorName(author.getName());
                    }
                    return commentDto;
                })
                .collect(Collectors.toList());

        itemDto.setComments(commentDtos);

        return itemDto;
    }

    // Метод для маппинга без дат бронирований (для поиска и создания)
    private ItemDto mapToItemDtoWithoutBookings(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setLastBooking(null);
        itemDto.setNextBooking(null);

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> {
                    User author = userRepository.findById(comment.getAuthorId()).orElse(null);
                    CommentDto commentDto = new CommentDto();
                    commentDto.setId(comment.getId());
                    commentDto.setText(comment.getText());
                    commentDto.setCreated(comment.getCreated());
                    if (author != null) {
                        commentDto.setAuthorName(author.getName());
                    }
                    return commentDto;
                })
                .collect(Collectors.toList());

        itemDto.setComments(commentDtos);

        return itemDto;
    }
}