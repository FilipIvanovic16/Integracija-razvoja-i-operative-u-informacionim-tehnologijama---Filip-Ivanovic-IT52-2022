package com.chronoshop.service;

import com.chronoshop.domain.User;
import com.chronoshop.domain.enums.Role;
import com.chronoshop.dto.AuthDtos.AuthResponse;
import com.chronoshop.dto.AuthDtos.LoginRequest;
import com.chronoshop.dto.AuthDtos.RegisterRequest;
import com.chronoshop.exception.DuplicateResourceException;
import com.chronoshop.repository.UserRepository;
import com.chronoshop.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new DuplicateResourceException("Korisnik sa email adresom '" + request.email() + "' već postoji.");
        }
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user = userRepository.save(user);
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Pogrešan email ili lozinka.");
        }
        User user = userRepository.findByEmail(request.email().toLowerCase()).orElseThrow();
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().name());
    }
}
