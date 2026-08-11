package com.chronoshop.service;

import com.chronoshop.domain.Address;
import com.chronoshop.domain.User;
import com.chronoshop.dto.UserDtos.AddressRequest;
import com.chronoshop.dto.UserDtos.AddressResponse;
import com.chronoshop.exception.ResourceNotFoundException;
import com.chronoshop.mapper.EntityMapper;
import com.chronoshop.repository.AddressRepository;
import com.chronoshop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listForUser(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(EntityMapper::toAddressResponse).toList();
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik", userId));
        Address a = new Address();
        a.setUser(user);
        apply(a, req);
        return EntityMapper.toAddressResponse(addressRepository.save(a));
    }

    @Transactional
    public AddressResponse update(Long userId, Long addressId, AddressRequest req) {
        Address a = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Adresa", addressId));
        apply(a, req);
        return EntityMapper.toAddressResponse(addressRepository.save(a));
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        Address a = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Adresa", addressId));
        addressRepository.delete(a);
    }

    private void apply(Address a, AddressRequest req) {
        a.setLabel(req.label());
        a.setStreet(req.street());
        a.setCity(req.city());
        a.setPostalCode(req.postalCode());
        a.setCountry(req.country());
        a.setPhone(req.phone());
    }
}
