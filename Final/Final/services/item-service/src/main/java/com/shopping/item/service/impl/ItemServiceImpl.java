package com.shopping.item.service.impl;

import com.shopping.item.model.document.Item;
import com.shopping.item.repository.ItemRepository;
import com.shopping.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public Item createItem(Item item) {
        if (itemRepository.existsByItemId(item.getItemId())) {
            throw new RuntimeException("Item with ID " + item.getItemId() + " already exists");
        }

        if (itemRepository.existsByUpc(item.getUpc())) {
            throw new RuntimeException("Item with UPC " + item.getUpc() + " already exists");
        }

        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setActive(true);

        Item savedItem = itemRepository.save(item);
        log.info("Created item: {}", savedItem.getItemId());
        return savedItem;
    }

    @Override
    public Optional<Item> getItemById(String id) {
        return itemRepository.findById(id);
    }

    @Override
    public Optional<Item> getItemByItemId(String itemId) {
        return itemRepository.findByItemId(itemId);
    }

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findByActiveTrue();
    }

    @Override
    public List<Item> getItemsByCategory(String category) {
        return itemRepository.findByCategory(category);
    }

    @Override
    public List<Item> searchItems(String keyword) {
        return itemRepository.findByNameContaining(keyword);
    }

    @Override
    public Item updateItem(String itemId, Item item) {
        Item existingItem = itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

        // 更新字段
        existingItem.setName(item.getName());
        existingItem.setDescription(item.getDescription());
        existingItem.setUnitPrice(item.getUnitPrice());
        existingItem.setPictureUrls(item.getPictureUrls());
        existingItem.setCategory(item.getCategory());
        existingItem.setBrand(item.getBrand());
        existingItem.setSpecifications(item.getSpecifications());
        existingItem.setUpdatedAt(LocalDateTime.now());

        return itemRepository.save(existingItem);
    }

    @Override
    public void deleteItem(String itemId) {
        Item item = itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

        item.setActive(false);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);

        log.info("Soft deleted item: {}", itemId);
    }

    @Override
    public boolean existsByItemId(String itemId) {
        return itemRepository.existsByItemId(itemId);
    }

    @Override
    public boolean existsByUpc(String upc) {
        return itemRepository.existsByUpc(upc);
    }
}