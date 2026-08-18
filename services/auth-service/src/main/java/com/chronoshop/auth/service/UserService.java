package com.chronoshop.auth.service;

import com.chronoshop.auth.domain.User;
import com.chronoshop.auth.mapper.EntityMapper;
import com.chronoshop.auth.repository.UserRepository;
import com.chronoshop.domain.enums.Role;
import com.chronoshop.dto.PageResponse;
import com.chronoshop.dto.UserDtos.UserResponse;
import com.chronoshop.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return EntityMapper.toUserResponse(findEntity(id));
    }

    public User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik", id));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String q, Pageable pageable) {
        Page<User> page = (q == null || q.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCaseOrLastNameContainingIgnoreCase(q.trim(), q.trim(), pageable);
        return PageResponse.from(page, EntityMapper::toUserResponse);
    }

    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        User user = findEntity(id);
        user.setRole(role);
        return EntityMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = findEntity(id);
        user.setEnabled(enabled);
        return EntityMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        // Napomena: monolit je ovde proveravao user.getOrders().isEmpty() pre brisanja.
        // Porudzbine sada zive u order-service/orderdb, van dosega ovog servisa,
        // pa ta provera prelazi u order-service (poziva auth-service pre brisanja naloga).
        User user = findEntity(id);
        userRepository.delete(user);
    }
}
