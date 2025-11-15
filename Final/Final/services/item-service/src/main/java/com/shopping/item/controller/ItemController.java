package com.shopping.item.controller;

import com.shopping.common.ApiResponse;
import com.shopping.item.model.document.Item;
import com.shopping.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(@RequestBody Item item) {
        try {
            Item createdItem = itemService.createItem(item);
            return ResponseEntity.ok(ApiResponse.success(createdItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Item>>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Item>> getItem(@PathVariable String itemId) {
        return itemService.getItemByItemId(itemId)
                .map(item -> ResponseEntity.ok(ApiResponse.success(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Item>>> getItemsByCategory(@PathVariable String category) {
        List<Item> items = itemService.getItemsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Item>>> searchItems(@RequestParam String keyword) {
        List<Item> items = itemService.searchItems(keyword);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Item>> updateItem(@PathVariable String itemId, @RequestBody Item item) {
        try {
            Item updatedItem = itemService.updateItem(itemId, item);
            return ResponseEntity.ok(ApiResponse.success(updatedItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable String itemId) {
        try {
            itemService.deleteItem(itemId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}