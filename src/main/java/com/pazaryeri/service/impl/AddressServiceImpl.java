package com.pazaryeri.service.impl;

import com.pazaryeri.dto.request.AddressRequest;
import com.pazaryeri.dto.response.AddressResponse;
import com.pazaryeri.entity.Address;
import com.pazaryeri.entity.City;
import com.pazaryeri.entity.District;
import com.pazaryeri.entity.User;
import com.pazaryeri.exception.BusinessException;
import com.pazaryeri.exception.ResourceNotFoundException;
import com.pazaryeri.repository.AddressRepository;
import com.pazaryeri.repository.CityRepository;
import com.pazaryeri.repository.DistrictRepository;
import com.pazaryeri.repository.UserRepository;
import com.pazaryeri.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    @Override
    @Transactional
    public AddressResponse createAddress(String userEmail, AddressRequest request) {
        User user = getUser(userEmail);
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Şehir", request.getCityId()));
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("İlçe", request.getDistrictId()));

        // Eğer varsayılan adres seçilmişse diğerlerini kaldır
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultAddresses(user.getId());
        }

        Address address = Address.builder()
                .user(user)
                .title(request.getTitle())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .city(city)
                .district(district)
                .address(request.getAddress())
                .zipCode(request.getZipCode())
                .isDefault(request.getIsDefault() != null && request.getIsDefault())
                .build();

        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, String userEmail, AddressRequest request) {
        User user = getUser(userEmail);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Adres", addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bu adresi düzenleme yetkiniz yok");
        }

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Şehir", request.getCityId()));
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("İlçe", request.getDistrictId()));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultAddresses(user.getId());
        }

        address.setTitle(request.getTitle());
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setCity(city);
        address.setDistrict(district);
        address.setAddress(request.getAddress());
        address.setZipCode(request.getZipCode());
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault());

        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId, String userEmail) {
        User user = getUser(userEmail);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Adres", addressId));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bu adresi silme yetkiniz yok");
        }
        addressRepository.delete(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String userEmail) {
        User user = getUser(userEmail);
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long addressId, String userEmail) {
        User user = getUser(userEmail);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Adres", addressId));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Bu adres size ait değil");
        }
        addressRepository.clearDefaultAddresses(user.getId());
        address.setIsDefault(true);
        return toResponse(addressRepository.save(address));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
    }

    private AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .fullName(a.getFullName())
                .phone(a.getPhone())
                .cityId(a.getCity().getId())
                .cityName(a.getCity().getName())
                .districtId(a.getDistrict().getId())
                .districtName(a.getDistrict().getName())
                .address(a.getAddress())
                .zipCode(a.getZipCode())
                .isDefault(a.getIsDefault())
                .build();
    }
}
