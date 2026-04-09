package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> findAllByOwnerId(Long ownerId);

    Optional<Item> findById(Long id);

    Item save(Item item);

    Item update(Item item);

    void deleteById(Long id);

    List<Item> search(String text);
}
