package org.shining319.newsstand_backend_system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shining319.newsstand_backend_system.entity.ProductTypeEnum;

/**
 * @Author: shining319
 * @Date: 2026/2/9
 * @Description: 查询产品列表请求
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Query product list request")
public class QueryProductRequest {

    @Schema(
            description = "页码（从0开始，0表示第一页）",
            example = "0",
            defaultValue = "0"
    )
    @Min(value = 0, message = "页码必须大于等于0")
    private Integer page = 0;

    @Schema(
            description = "每页数量（1-100之间）",
            example = "20",
            defaultValue = "20"
    )
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 20;

    @Schema(
            description = "产品类型（可选，不传则查询全部）",
            example = "NEWSPAPER",
            allowableValues = {"NEWSPAPER", "MAGAZINE"}
    )
    private ProductTypeEnum type;
}
