package org.shining319.newsstand_backend_system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: shining319
 * @Date: 2026/2/11
 * @Description: 调整库存请求DTO
 **/
@Data
@Schema(description = "Adjust product stock request")
public class AdjustStockRequest {

    @NotNull(message = "调整数量不能为空")
    @Min(value = -9999, message = "库存调整量不能小于-9999")
    @Max(value = 9999, message = "库存调整量不能大于9999")
    @Schema(description = "库存调整量（正数为增加，负数为减少）", example = "-5")
    private Integer quantity;
}
