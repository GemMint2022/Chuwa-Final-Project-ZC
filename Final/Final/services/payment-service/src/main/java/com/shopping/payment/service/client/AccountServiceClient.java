package com.shopping.payment.service.client;

import com.shopping.api.models.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "account-service", url = "${account.service.url}")
public interface AccountServiceClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("/api/users/me")
    UserResponse getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader
    );

}