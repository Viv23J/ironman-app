package com.ironman.service;

import com.ironman.dto.request.AddressRequest;
import com.ironman.dto.response.AddressResponse;
import com.ironman.exception.BadRequestException;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.Address;
import com.ironman.model.User;
import com.ironman.repository.AddressRepository;
import com.ironman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        log.info("Creating address for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If this is set as default, unset other default addresses
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultAddresses(userId);
        }

        Address address = new Address();
        address.setUser(user);
        address.setLabel(request.getLabel());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setIsDefault(request.getIsDefault());

        Address savedAddress = addressRepository.save(address);
        log.info("Address created with ID: {}", savedAddress.getId());

        return mapToResponse(savedAddress);
    }

    public List<AddressResponse> getAllAddresses(Long userId) {
        log.info("Fetching all addresses for user: {}", userId);

        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse getAddressById(Long userId, Long addressId) {
        log.info("Fetching address {} for user: {}", addressId, userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        return mapToResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        log.info("Updating address {} for user: {}", addressId, userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // If this is set as default, unset other default addresses
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            unsetDefaultAddresses(userId);
        }

        address.setLabel(request.getLabel());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setIsDefault(request.getIsDefault());

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully");

        return mapToResponse(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
        log.info("Address deleted successfully");
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        log.info("Setting address {} as default for user: {}", addressId, userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Unset all other default addresses
        unsetDefaultAddresses(userId);

        // Set this as default
        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);

        log.info("Address set as default successfully");
        return mapToResponse(updatedAddress);
    }

    private void unsetDefaultAddresses(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(address -> {
                    address.setIsDefault(false);
                    addressRepository.save(address);
                });
    }

    private AddressResponse mapToResponse(Address address) {
        String fullAddress = String.format("%s, %s, %s, %s, %s - %s",
                address.getAddressLine1(),
                address.getAddressLine2() != null ? address.getAddressLine2() + "," : "",
                address.getLandmark() != null ? address.getLandmark() + "," : "",
                address.getCity(),
                address.getState(),
                address.getPincode()).replace(", ,", ",");

        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.getIsDefault())
                .fullAddress(fullAddress)
                .createdAt(address.getCreatedAt())
                .build();
    }
}