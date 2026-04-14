package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestWithItem;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto createRequest(ItemRequestDto item, Long userId);

    Collection<ItemRequestWithItem> getRequests(Long requestorId);

    Collection<ItemRequestDto> getAllRequests(Long userId);

    ItemRequestWithItem getRequestById(Long id, Long userId);

}
