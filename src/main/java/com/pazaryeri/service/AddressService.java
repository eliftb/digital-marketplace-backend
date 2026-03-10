package com.pazaryeri.service;

import com.pazaryeri.dto.request.AddressRequest;
import com.pazaryeri.dto.response.AddressResponse;
import java.util.List;

public interface AddressService {
    AddressResponse createAddress(String userEmail, AddressRequest request);
    AddressResponse updateAddress(Long addressId, String userEmail, AddressRequest request);
    void deleteAddress(Long addressId, String userEmail);
    List<AddressResponse> getMyAddresses(String userEmail);
    AddressResponse setDefaultAddress(Long addressId, String userEmail);
}
