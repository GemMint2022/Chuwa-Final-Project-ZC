package com.shopping.item.repository;

import com.shopping.item.model.document.Item;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends MongoRepository<Item, String> {

    Optional<Item> findByItemId(String itemId);

    Optional<Item> findByUpc(String upc);

    List<Item> findByCategory(String category);

    List<Item> findByActiveTrue();

    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Item> findByNameContaining(String name);

    boolean existsByItemId(String itemId);

    boolean existsByUpc(String upc);
}