package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestWithItem;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemRequestDto createRequest(ItemRequestDto item, Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = new ItemRequest();
        request.setDescription(item.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = itemRequestRepository.save(request);
        log.info("Создан запрос с id={}", saved.getId());
        return ItemRequestMapper.toDto(saved);
    }

    @Override
    public List<ItemRequestWithItem> getRequests(Long requestorId) {

        if (!userRepository.existsById(requestorId)) {
            throw new NotFoundException("Пользователь с id=" + requestorId + " не найден");
        }

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestorId);

        return requests.stream()
                .map(request -> {
                    List<Item> items = itemRepository.findByRequestId(request.getId());
                    return ItemRequestMapper.toWithItemsDto(request, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);

        return requests.stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestWithItem getRequestById(Long requestId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        List<Item> items = itemRepository.findByRequestId(request.getId());

        return ItemRequestMapper.toWithItemsDto(request, items);
    }
}
