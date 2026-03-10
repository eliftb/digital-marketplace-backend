package com.pazaryeri.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AddressResponse {
    private Long id;
    private String title;
    private String fullName;
    private String phone;
    private Long cityId;
    private String cityName;
    private Long districtId;
    private String districtName;
    private String address;
    private String zipCode;
    private Boolean isDefault;
}
