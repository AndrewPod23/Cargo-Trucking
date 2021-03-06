package com.itechart.cargotrucking.core.invoice.dto;

import com.itechart.cargotrucking.core.product.dto.ProductAddDto;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class InvoiceAddDto {
    @NotBlank(message = "Invoice number cannot be empty")
    @Length(max = 20, message = "Invoice number length cannot be more than 20 characters")
    private String number;

    @NotNull(message = "Invoice storage cannot be empty")
    private Long storageId;

    @NotNull(message = "Invoice product owner cannot be empty")
    private Long productOwnerId;

    private Long creatorId;

    @NotNull(message = "Invoice driver cannot be empty")
    private Long driverId;

    @NotNull(message = "Invoice products cannot be empty")
    private List<ProductAddDto> products;
}
