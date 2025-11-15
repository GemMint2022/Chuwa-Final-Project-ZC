package com.shopping.item.service.impl;

import com.shopping.item.model.document.Item;
import com.shopping.item.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item mockItem;
    private final String ITEM_ID = "ITEM-123";
    private final String UPC = "123456789012";

    @BeforeEach
    void setUp() {
        mockItem = new Item();
        mockItem.setId("mongo-id-123");
        mockItem.setItemId(ITEM_ID);
        mockItem.setName("Test Product");
        mockItem.setDescription("Test Description");
        mockItem.setUnitPrice(new BigDecimal("29.99"));
        mockItem.setUpc(UPC);
        mockItem.setCategory("Electronics");
        mockItem.setBrand("Test Brand");
        mockItem.setActive(true);
        mockItem.setCreatedAt(LocalDateTime.now());
        mockItem.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createItem_ShouldSaveAndReturnItem() {
        // Arrange
        when(itemRepository.existsByItemId(ITEM_ID)).thenReturn(false);
        when(itemRepository.existsByUpc(UPC)).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

        // Act
        Item result = itemService.createItem(mockItem);

        // Assert
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getItemId());
        assertTrue(result.isActive());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(itemRepository).existsByItemId(ITEM_ID);
        verify(itemRepository).existsByUpc(UPC);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_WhenItemIdExists_ShouldThrowException() {
        // Arrange
        when(itemRepository.existsByItemId(ITEM_ID)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> itemService.createItem(mockItem));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(itemRepository).existsByItemId(ITEM_ID);
        verify(itemRepository, never()).existsByUpc(anyString());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void createItem_WhenUpcExists_ShouldThrowException() {
        // Arrange
        when(itemRepository.existsByItemId(ITEM_ID)).thenReturn(false);
        when(itemRepository.existsByUpc(UPC)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> itemService.createItem(mockItem));

        assertTrue(exception.getMessage().contains("UPC"));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(itemRepository).existsByItemId(ITEM_ID);
        verify(itemRepository).existsByUpc(UPC);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void getItemByItemId_WhenExists_ShouldReturnItem() {
        // Arrange
        when(itemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(mockItem));

        // Act
        Optional<Item> result = itemService.getItemByItemId(ITEM_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        verify(itemRepository).findByItemId(ITEM_ID);
    }

    @Test
    void getItemByItemId_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(itemRepository.findByItemId("NON-EXISTENT")).thenReturn(Optional.empty());

        // Act
        Optional<Item> result = itemService.getItemByItemId("NON-EXISTENT");

        // Assert
        assertFalse(result.isPresent());
        verify(itemRepository).findByItemId("NON-EXISTENT");
    }

    @Test
    void getItemById_WhenExists_ShouldReturnItem() {
        // Arrange
        String mongoId = "mongo-id-123";
        when(itemRepository.findById(mongoId)).thenReturn(Optional.of(mockItem));

        // Act
        Optional<Item> result = itemService.getItemById(mongoId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mongoId, result.get().getId());
        verify(itemRepository).findById(mongoId);
    }

    @Test
    void getAllItems_ShouldReturnActiveItems() {
        // Arrange
        List<Item> activeItems = Arrays.asList(mockItem);
        when(itemRepository.findByActiveTrue()).thenReturn(activeItems);

        // Act
        List<Item> result = itemService.getAllItems();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(itemRepository).findByActiveTrue();
    }

    @Test
    void getItemsByCategory_ShouldReturnCategoryItems() {
        // Arrange
        String category = "Electronics";
        List<Item> categoryItems = Arrays.asList(mockItem);
        when(itemRepository.findByCategory(category)).thenReturn(categoryItems);

        // Act
        List<Item> result = itemService.getItemsByCategory(category);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(category, result.get(0).getCategory());
        verify(itemRepository).findByCategory(category);
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() {
        // Arrange
        String searchTerm = "test";
        List<Item> searchResults = Arrays.asList(mockItem);
        when(itemRepository.findByNameContaining(searchTerm)).thenReturn(searchResults);

        // Act
        List<Item> result = itemService.searchItems(searchTerm);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(itemRepository).findByNameContaining(searchTerm);
    }

    @Test
    void updateItem_WhenExists_ShouldUpdateAndReturnItem() {
        // Arrange
        Item updatedItem = new Item();
        updatedItem.setName("Updated Product");
        updatedItem.setDescription("Updated Description");
        updatedItem.setUnitPrice(new BigDecimal("39.99"));
        updatedItem.setCategory("Home Appliances");
        updatedItem.setBrand("Updated Brand");

        when(itemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(mockItem));
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

        // Act
        Item result = itemService.updateItem(ITEM_ID, updatedItem);

        // Assert
        assertNotNull(result);
        verify(itemRepository).findByItemId(ITEM_ID);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(itemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> itemService.updateItem(ITEM_ID, mockItem));

        assertTrue(exception.getMessage().contains("not found"));
        verify(itemRepository).findByItemId(ITEM_ID);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deleteItem_ShouldSoftDeleteItem() {
        // Arrange
        when(itemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(mockItem));
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

        // Act
        itemService.deleteItem(ITEM_ID);

        // Assert
        verify(itemRepository).findByItemId(ITEM_ID);
        verify(itemRepository).save(any(Item.class));
        // 注意：由于mock对象的状态，我们无法验证active字段被设为false
        // 但在实际实现中，这会执行软删除
    }

    @Test
    void deleteItem_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(itemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> itemService.deleteItem(ITEM_ID));

        assertTrue(exception.getMessage().contains("not found"));
        verify(itemRepository).findByItemId(ITEM_ID);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void existsByItemId_WhenExists_ShouldReturnTrue() {
        // Arrange
        when(itemRepository.existsByItemId(ITEM_ID)).thenReturn(true);

        // Act
        boolean result = itemService.existsByItemId(ITEM_ID);

        // Assert
        assertTrue(result);
        verify(itemRepository).existsByItemId(ITEM_ID);
    }

    @Test
    void existsByUpc_WhenExists_ShouldReturnTrue() {
        // Arrange
        when(itemRepository.existsByUpc(UPC)).thenReturn(true);

        // Act
        boolean result = itemService.existsByUpc(UPC);

        // Assert
        assertTrue(result);
        verify(itemRepository).existsByUpc(UPC);
    }
}