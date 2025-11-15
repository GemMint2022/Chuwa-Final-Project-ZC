package com.shopping.item.service;

import com.shopping.item.model.document.Inventory;

import java.util.Optional;

public interface InventoryService {

    Inventory initializeInventory(String itemId, Integer initialQuantity);

    Optional<Inventory> getInventory(String itemId);

    Inventory updateInventory(String itemId, Integer quantity);

    boolean reserveInventory(String itemId, Integer quantity);

    void releaseReservedInventory(String itemId, Integer quantity);

    void consumeReservedInventory(String itemId, Integer quantity);

    Integer getAvailableUnits(String itemId);
}