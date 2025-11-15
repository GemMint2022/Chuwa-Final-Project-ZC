package com.shopping.item.service;

import com.shopping.item.model.document.Item;
import java.util.List;
import java.util.Optional;

public interface ItemService {

    Item createItem(Item item);

    Optional<Item> getItemById(String id);

    Optional<Item> getItemByItemId(String itemId);

    List<Item> getAllItems();

    List<Item> getItemsByCategory(String category);

    List<Item> searchItems(String keyword);

    Item updateItem(String itemId, Item item);

    void deleteItem(String itemId);

    boolean existsByItemId(String itemId);

    boolean existsByUpc(String upc);
}