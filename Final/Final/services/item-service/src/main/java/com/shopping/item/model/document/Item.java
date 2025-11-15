package com.shopping.item.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "items")
public class Item {
    @Id
    private String id;

    private String itemId;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private List<String> pictureUrls;
    private String upc;
    private String category;
    private String brand;
    private Map<String, Object> specifications;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}