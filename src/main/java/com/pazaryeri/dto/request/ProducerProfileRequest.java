package com.pazaryeri.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProducerProfileRequest {

    @NotBlank(message = "Mağaza adı zorunludur")
    @Size(max = 200)
    private String storeName;

    private String storeDescription;

    private Long cityId;
    private Long districtId;
    private String address;
    private String taxNumber;
    private String iban;
    private String logoUrl;
    private String coverUrl;
}
