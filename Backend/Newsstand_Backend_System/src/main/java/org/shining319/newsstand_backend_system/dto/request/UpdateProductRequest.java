package org.shining319.newsstand_backend_system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.shining319.newsstand_backend_system.entity.ProductTypeEnum;

import java.math.BigDecimal;

/**
 * @Author: shining319
 * @Date: 2026/2/10
 * @Description: 更新产品请求DTO（不包含库存，库存通过单独接口调整）
 **/
@Data
@Schema(description = "Update product request")
public class UpdateProductRequest {

    @Size(min = 1, max = 100, message = "产品名称长度必须在1-100之间")
    @Schema(description = "产品名称", example = "人民日报")
    private String name;

    @Schema(description = "产品类型", example = "NEWSPAPER")
    private ProductTypeEnum type;

    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "999999.99", message = "价格不能超过999999.99")
    @Schema(description = "销售价格", example = "2.50")
    private BigDecimal price;
}
