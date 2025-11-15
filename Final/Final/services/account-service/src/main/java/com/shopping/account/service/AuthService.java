package com.shopping.account.service;

import com.shopping.account.dto.LoginRequest;
import com.shopping.account.dto.LoginResponse;
import com.shopping.account.dto.RegisterRequest;
import com.shopping.account.dto.UserResponse;
import com.shopping.account.entity.User;
import com.shopping.account.repository.UserRepository;
import com.shopping.common.security.JwtTokenUtil;
import com.shopping.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setShippingAddress(request.getShippingAddress());
        user.setBillingAddress(request.getBillingAddress());
        user.setPaymentMethod(request.getPaymentMethod());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(userPrincipal.getEmail(), userPrincipal.getId());

        return new LoginResponse(token, userPrincipal.getId(), userPrincipal.getEmail(), userPrincipal.getUsername());
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setShippingAddress(user.getShippingAddress());
        response.setBillingAddress(user.getBillingAddress());
        response.setPaymentMethod(user.getPaymentMethod());
        response.setEnabled(user.getEnabled());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}