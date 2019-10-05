package com.itechart.cargotrucking.core.product.dto;

import com.itechart.cargotrucking.core.product.ProductWriteoffStatus;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ProductWriteoffUpdateDto {
    @NotNull(message = "Product amount cannot be null")
    private Short amount;

    @NotNull(message = "Product status cannot be null")
    private ProductWriteoffStatus status;
}
