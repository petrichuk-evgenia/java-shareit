package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindItemsByOwnerId() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@yandex.ru");
        userRepository.save(owner);

        Item item1 = new Item();
        item1.setName("Drill");
        item1.setDescription("Powerful");
        item1.setAvailable(true);
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setName("Screwdriver");
        item2.setDescription("Simple");
        item2.setAvailable(true);
        item2.setOwner(owner);

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> items = itemRepository.findByOwnerId(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getName).contains("Drill", "Screwdriver");
    }

    @Test
    void shouldSearchItemsByNameOrDescription() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@yandex.ru");
        userRepository.save(owner);

        Item item1 = new Item();
        item1.setName("Drill");
        item1.setDescription("Electric drill");
        item1.setAvailable(true);
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setName("Table");
        item2.setDescription("Wooden table");
        item2.setAvailable(true);
        item2.setOwner(owner);

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> result = itemRepository.searchAvailableItems("drill");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Drill");
    }
}