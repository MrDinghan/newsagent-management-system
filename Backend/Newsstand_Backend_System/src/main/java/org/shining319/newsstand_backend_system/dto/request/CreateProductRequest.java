package org.shining319.newsstand_backend_system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.shining319.newsstand_backend_system.entity.ProductTypeEnum;

import java.math.BigDecimal;

/**
 * @Author: shining319
 * @Date: 2026/2/9
 * @Description: 创建产品请求DTO
 **/
@Data
@Schema(description = "Create product request")
public class CreateProductRequest {

    @NotBlank(message = "产品名称不能为空")
    @Size(min = 1, max = 100, message = "产品名称长度必须在1-100之间")
    @Schema(description = "产品名称", example = "人民日报", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "产品类型不能为空")
    @Schema(description = "产品类型", example = "NEWSPAPER", requiredMode = Schema.RequiredMode.REQUIRED)
    private ProductTypeEnum type;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "999999.99", message = "价格不能超过999999.99")
    @Schema(description = "销售价格", example = "2.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量必须大于等于0")
    @Schema(description = "初始库存数量", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stock;
}
