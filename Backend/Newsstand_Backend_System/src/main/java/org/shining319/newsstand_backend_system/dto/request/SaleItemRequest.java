package org.shining319.newsstand_backend_system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: 销售订单中单个商品条目请求
 **/
@Data
@Schema(description = "Sale item request (single product in the order)")
public class SaleItemRequest {

    @NotBlank(message = "商品ID不能为空")
    @Schema(description = "产品ID (UUIDv7)", example = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    @Max(value = 9999, message = "单次购买数量不能超过9999")
    @Schema(description = "购买数量", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
