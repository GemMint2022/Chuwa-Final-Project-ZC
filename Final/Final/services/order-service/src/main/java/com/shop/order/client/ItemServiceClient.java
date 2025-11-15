package com.shop.order.client;

import com.shop.order.exception.ItemServiceException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemServiceClient {

    private final RestTemplate restTemplate;

    public List<ItemInfo> getItemsInfo(List<String> itemIds) {
        String url = "http://item-service:8082/api/items/batch?ids=" +
                String.join(",", itemIds);

        try {
            ResponseEntity<com.shopping.common.ApiResponse<List<ItemInfo>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<com.shopping.common.ApiResponse<List<ItemInfo>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getData();
            }
            throw new ItemServiceException("Failed to fetch item info from item-service");
        } catch (Exception e) {
            log.error("Error calling item-service: {}", e.getMessage());
            throw new ItemServiceException("Item service unavailable: " + e.getMessage(), e);
        }
    }

    @Data
    public static class ItemInfo {
        private String itemId;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private String imageUrl;
    }
}