package com.shopping.item.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "inventory")
public class Inventory {
    @Id
    private String id;

    private String itemId;
    private Integer availableUnits;
    private Integer reservedUnits;
    private Integer totalUnits;
    private LocalDateTime lastUpdated;

    // 库存状态
    public enum Status {
        IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    }

    public Status getStatus() {
        if (availableUnits <= 0) return Status.OUT_OF_STOCK;
        if (availableUnits <= 10) return Status.LOW_STOCK;
        return Status.IN_STOCK;
    }
}