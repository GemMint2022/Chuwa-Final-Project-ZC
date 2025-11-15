package com.shopping.item.config;

import com.shopping.item.model.document.Item;
import com.shopping.item.service.ItemService;
import com.shopping.item.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev") // 只在开发环境运行
public class DataInitializer implements CommandLineRunner {

    private final ItemService itemService;
    private final InventoryService inventoryService;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");

        // 检查是否已经初始化过
        if (itemService.existsByItemId("LAPTOP001")) {
            log.info("Data already initialized, skipping...");
            return;
        }

        // 商品1: 笔记本电脑
        Item laptop = createLaptop();
        itemService.createItem(laptop);
        inventoryService.initializeInventory("LAPTOP001", 25);

        // 商品2: 智能手机
        Item phone = createSmartphone();
        itemService.createItem(phone);
        inventoryService.initializeInventory("PHONE001", 50);

        // 商品3: 耳机
        Item headphones = createHeadphones();
        itemService.createItem(headphones);
        inventoryService.initializeInventory("HEADPHONE001", 100);

        // 商品4: 书籍
        Item book = createBook();
        itemService.createItem(book);
        inventoryService.initializeInventory("BOOK001", 200);

        // 商品5: 智能手表
        Item watch = createSmartWatch();
        itemService.createItem(watch);
        inventoryService.initializeInventory("WATCH001", 30);

        log.info("Data initialization completed successfully!");
    }

    private Item createLaptop() {
        Map<String, Object> specs = new HashMap<>();
        specs.put("processor", "Intel Core i7-11800H");
        specs.put("ram", "16GB DDR4");
        specs.put("storage", "1TB NVMe SSD");
        specs.put("display", "15.6\" FHD IPS");
        specs.put("graphics", "NVIDIA RTX 3060");
        specs.put("weight", "2.3kg");

        Item item = new Item();
        item.setItemId("LAPTOP001");
        item.setName("Gaming Laptop Pro");
        item.setDescription("High-performance gaming laptop with RTX 3060");
        item.setUnitPrice(new BigDecimal("1299.99"));
        item.setPictureUrls(Arrays.asList(
                "https://example.com/images/laptop1.jpg",
                "https://example.com/images/laptop2.jpg",
                "https://example.com/images/laptop3.jpg"
        ));
        item.setUpc("123456789012");
        item.setCategory("Electronics");
        item.setBrand("GamingBrand");
        item.setSpecifications(specs);
        return item;
    }

    private Item createSmartphone() {
        Map<String, Object> specs = new HashMap<>();
        specs.put("screen", "6.7\" Super Retina XDR");
        specs.put("storage", "128GB");
        specs.put("camera", "Triple 12MP");
        specs.put("battery", "4352mAh");
        specs.put("os", "iOS 15");
        specs.put("colors", Arrays.asList("Black", "Silver", "Gold"));

        Item item = new Item();
        item.setItemId("PHONE001");
        item.setName("Smartphone X Pro");
        item.setDescription("Flagship smartphone with advanced camera system");
        item.setUnitPrice(new BigDecimal("999.99"));
        item.setPictureUrls(Arrays.asList(
                "https://example.com/images/phone1.jpg",
                "https://example.com/images/phone2.jpg"
        ));
        item.setUpc("234567890123");
        item.setCategory("Electronics");
        item.setBrand("PhoneBrand");
        item.setSpecifications(specs);
        return item;
    }

    private Item createHeadphones() {
        Map<String, Object> specs = new HashMap<>();
        specs.put("type", "Over-ear");
        specs.put("connectivity", "Bluetooth 5.0");
        specs.put("batteryLife", "30 hours");
        specs.put("noiseCancelling", true);
        specs.put("color", "Black");

        Item item = new Item();
        item.setItemId("HEADPHONE001");
        item.setName("Wireless Noise Cancelling Headphones");
        item.setDescription("Premium headphones with active noise cancellation");
        item.setUnitPrice(new BigDecimal("299.99"));
        item.setPictureUrls(Arrays.asList(
                "https://example.com/images/headphones1.jpg",
                "https://example.com/images/headphones2.jpg"
        ));
        item.setUpc("345678901234");
        item.setCategory("Electronics");
        item.setBrand("AudioTech");
        item.setSpecifications(specs);
        return item;
    }

    private Item createBook() {
        Map<String, Object> specs = new HashMap<>();
        specs.put("author", "John Doe");
        specs.put("pages", 320);
        specs.put("publisher", "Tech Publishing");
        specs.put("language", "English");
        specs.put("isbn", "978-3-16-148410-0");

        Item item = new Item();
        item.setItemId("BOOK001");
        item.setName("Spring Boot in Action");
        item.setDescription("Comprehensive guide to Spring Boot development");
        item.setUnitPrice(new BigDecimal("39.99"));
        item.setPictureUrls(Arrays.asList(
                "https://example.com/images/book1.jpg"
        ));
        item.setUpc("456789012345");
        item.setCategory("Books");
        item.setBrand("Tech Publishing");
        item.setSpecifications(specs);
        return item;
    }

    private Item createSmartWatch() {
        Map<String, Object> specs = new HashMap<>();
        specs.put("display", "1.78\" AMOLED");
        specs.put("battery", "7 days");
        specs.put("waterResistant", "5ATM");
        specs.put("features", Arrays.asList("Heart Rate", "GPS", "Sleep Tracking"));
        specs.put("compatibility", Arrays.asList("iOS", "Android"));

        Item item = new Item();
        item.setItemId("WATCH001");
        item.setName("Smart Fitness Watch");
        item.setDescription("Advanced fitness tracking smartwatch");
        item.setUnitPrice(new BigDecimal("199.99"));
        item.setPictureUrls(Arrays.asList(
                "https://example.com/images/watch1.jpg",
                "https://example.com/images/watch2.jpg"
        ));
        item.setUpc("567890123456");
        item.setCategory("Electronics");
        item.setBrand("FitTech");
        item.setSpecifications(specs);
        return item;
    }
}