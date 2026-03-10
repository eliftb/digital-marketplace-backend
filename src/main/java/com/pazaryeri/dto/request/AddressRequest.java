package com.pazaryeri.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "Adres başlığı zorunludur")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "Ad Soyad zorunludur")
    @Size(max = 200)
    private String fullName;

    @NotBlank(message = "Telefon zorunludur")
    private String phone;

    @NotNull(message = "Şehir zorunludur")
    private Long cityId;

    @NotNull(message = "İlçe zorunludur")
    private Long districtId;

    @NotBlank(message = "Adres zorunludur")
    private String address;

    private String zipCode;
    private Boolean isDefault = false;
}
