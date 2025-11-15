package com.shopping.item.service.impl;

import com.shopping.item.model.document.Inventory;
import com.shopping.item.repository.InventoryRepository;
import com.shopping.item.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MongoTemplate mongoTemplate; // 注入MongoTemplate

    @Override
    public boolean reserveInventory(String itemId, Integer quantity) {
        try {
            Query query = new Query(Criteria.where("itemId").is(itemId)
                    .and("availableUnits").gte(quantity));

            Update update = new Update()
                    .inc("availableUnits", -quantity)
                    .inc("reservedUnits", quantity)
                    .set("lastUpdated", LocalDateTime.now());

            // 使用MongoTemplate执行原子更新
            long updatedCount = mongoTemplate.updateFirst(query, update, Inventory.class).getModifiedCount();
            boolean success = updatedCount > 0;

            if (success) {
                log.info("Reserved {} units for item: {}", quantity, itemId);
            } else {
                log.warn("Failed to reserve {} units for item: {} - insufficient inventory", quantity, itemId);
            }
            return success;
        } catch (Exception e) {
            log.error("Failed to reserve inventory for item: {}", itemId, e);
            return false;
        }
    }

    // 其他方法保持不变...
    @Override
    public Inventory initializeInventory(String itemId, Integer initialQuantity) {
        Inventory inventory = new Inventory();
        inventory.setItemId(itemId);
        inventory.setAvailableUnits(initialQuantity);
        inventory.setReservedUnits(0);
        inventory.setTotalUnits(initialQuantity);
        inventory.setLastUpdated(LocalDateTime.now());

        return inventoryRepository.save(inventory);
    }

    @Override
    public Optional<Inventory> getInventory(String itemId) {
        return inventoryRepository.findByItemId(itemId);
    }

    @Override
    public Inventory updateInventory(String itemId, Integer quantity) {
        Optional<Inventory> existingInventory = inventoryRepository.findByItemId(itemId);

        if (existingInventory.isPresent()) {
            Inventory inventory = existingInventory.get();
            inventory.setAvailableUnits(quantity);
            inventory.setTotalUnits(quantity + inventory.getReservedUnits());
            inventory.setLastUpdated(LocalDateTime.now());
            return inventoryRepository.save(inventory);
        } else {
            return initializeInventory(itemId, quantity);
        }
    }

    @Override
    public void releaseReservedInventory(String itemId, Integer quantity) {
        try {
            Query query = new Query(Criteria.where("itemId").is(itemId));
            Update update = new Update()
                    .inc("reservedUnits", -quantity)
                    .set("lastUpdated", LocalDateTime.now());

            mongoTemplate.updateFirst(query, update, Inventory.class);
            log.info("Released {} reserved units for item: {}", quantity, itemId);
        } catch (Exception e) {
            log.error("Failed to release reserved inventory for item: {}", itemId, e);
        }
    }

    @Override
    public void consumeReservedInventory(String itemId, Integer quantity) {
        try {
            Query query = new Query(Criteria.where("itemId").is(itemId));
            Update update = new Update()
                    .inc("reservedUnits", -quantity)
                    .inc("totalUnits", -quantity)
                    .set("lastUpdated", LocalDateTime.now());

            mongoTemplate.updateFirst(query, update, Inventory.class);
            log.info("Consumed {} reserved units for item: {}", quantity, itemId);
        } catch (Exception e) {
            log.error("Failed to consume reserved inventory for item: {}", itemId, e);
        }
    }

    @Override
    public Integer getAvailableUnits(String itemId) {
        return inventoryRepository.findByItemId(itemId)
                .map(Inventory::getAvailableUnits)
                .orElse(0);
    }
}