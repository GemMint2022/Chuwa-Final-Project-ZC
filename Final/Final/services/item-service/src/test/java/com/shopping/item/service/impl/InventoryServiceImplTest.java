package com.shopping.item.service.impl;

import com.shopping.item.model.document.Inventory;
import com.shopping.item.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.client.result.UpdateResult; // 正确的导入
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private UpdateResult updateResult; // 添加Mock

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory mockInventory;
    private final String ITEM_ID = "item-123";
    private final Integer QUANTITY = 10;

    @BeforeEach
    void setUp() {
        mockInventory = new Inventory();
        mockInventory.setId("inv-123");
        mockInventory.setItemId(ITEM_ID);
        mockInventory.setAvailableUnits(20);
        mockInventory.setReservedUnits(5);
        mockInventory.setTotalUnits(25);
        mockInventory.setLastUpdated(LocalDateTime.now());
    }

    @Test
    void initializeInventory_ShouldCreateNewInventory() {
        // Arrange
        Integer initialQuantity = 10;
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory savedInventory = invocation.getArgument(0);
            // 确保保存的库存数量是正确的
            savedInventory.setAvailableUnits(initialQuantity);
            savedInventory.setTotalUnits(initialQuantity);
            return savedInventory;
        });

        // Act
        Inventory result = inventoryService.initializeInventory(ITEM_ID, initialQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getItemId());
        assertEquals(initialQuantity, result.getAvailableUnits()); // 这里应该期望10
        assertEquals(initialQuantity, result.getTotalUnits());
        assertEquals(0, result.getReservedUnits()); // 预留库存应该为0
        assertNotNull(result.getLastUpdated());
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void getInventory_WhenExists_ShouldReturnInventory() {
        // Arrange
        when(inventoryRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(mockInventory));

        // Act
        Optional<Inventory> result = inventoryService.getInventory(ITEM_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ITEM_ID, result.get().getItemId());
        verify(inventoryRepository).findByItemId(ITEM_ID);
    }

    @Test
    void getInventory_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(inventoryRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        // Act
        Optional<Inventory> result = inventoryService.getInventory(ITEM_ID);

        // Assert
        assertFalse(result.isPresent());
        verify(inventoryRepository).findByItemId(ITEM_ID);
    }

    @Test
    void reserveInventory_WhenSufficientStock_ShouldReturnTrue() {
        // Arrange
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Inventory.class)))
                .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
        // 或者使用mock方式
        // when(updateResult.getModifiedCount()).thenReturn(1L);
        // when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Inventory.class)))
        //         .thenReturn(updateResult);

        // Act
        boolean result = inventoryService.reserveInventory(ITEM_ID, QUANTITY);

        // Assert
        assertTrue(result);
        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(Inventory.class));
    }

    @Test
    void reserveInventory_WhenInsufficientStock_ShouldReturnFalse() {
        // Arrange
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Inventory.class)))
                .thenReturn(UpdateResult.acknowledged(0L, 0L, null));

        // Act
        boolean result = inventoryService.reserveInventory(ITEM_ID, 100); // 请求数量超过库存

        // Assert
        assertFalse(result);
        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(Inventory.class));
    }

    @Test
    void getAvailableUnits_WhenInventoryExists_ShouldReturnUnits() {
        // Arrange
        when(inventoryRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(mockInventory));

        // Act
        Integer result = inventoryService.getAvailableUnits(ITEM_ID);

        // Assert
        assertEquals(20, result);
        verify(inventoryRepository).findByItemId(ITEM_ID);
    }

    @Test
    void getAvailableUnits_WhenInventoryNotExists_ShouldReturnZero() {
        // Arrange
        when(inventoryRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        // Act
        Integer result = inventoryService.getAvailableUnits(ITEM_ID);

        // Assert
        assertEquals(0, result);
        verify(inventoryRepository).findByItemId(ITEM_ID);
    }

    @Test
    void updateInventory_WhenExists_ShouldUpdateInventory() {
        // Arrange
        when(inventoryRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(mockInventory);

        // Act
        Inventory result = inventoryService.updateInventory(ITEM_ID, 30);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository).findByItemId(ITEM_ID);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WhenNotExists_ShouldInitializeInventory() {
        // Arrange
        when(inventoryRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(mockInventory);

        // Act
        Inventory result = inventoryService.updateInventory(ITEM_ID, 30);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository).findByItemId(ITEM_ID);
        verify(inventoryRepository).save(any(Inventory.class));
    }
}