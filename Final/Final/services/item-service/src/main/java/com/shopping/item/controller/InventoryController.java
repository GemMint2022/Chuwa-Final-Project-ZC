package com.shopping.item.controller;

import com.shopping.common.ApiResponse;
import com.shopping.item.model.document.Inventory;
import com.shopping.item.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Inventory>> getInventory(@PathVariable String itemId) {
        Optional<Inventory> inventory = inventoryService.getInventory(itemId);
        return inventory.map(inv -> ResponseEntity.ok(ApiResponse.success(inv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{itemId}/available")
    public ResponseEntity<ApiResponse<Integer>> getAvailableUnits(@PathVariable String itemId) {
        Integer availableUnits = inventoryService.getAvailableUnits(itemId);
        return ResponseEntity.ok(ApiResponse.success(availableUnits));
    }

    @PostMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Inventory>> initializeInventory(
            @PathVariable String itemId,
            @RequestParam Integer quantity) {
        try {
            Inventory inventory = inventoryService.initializeInventory(itemId, quantity);
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Inventory>> updateInventory(
            @PathVariable String itemId,
            @RequestParam Integer quantity) {
        try {
            Inventory inventory = inventoryService.updateInventory(itemId, quantity);
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{itemId}/reserve")
    public ResponseEntity<ApiResponse<Boolean>> reserveInventory(
            @PathVariable String itemId,
            @RequestParam Integer quantity) {
        boolean success = inventoryService.reserveInventory(itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success(success));
    }
}