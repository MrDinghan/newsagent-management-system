package org.shining319.newsstand_backend_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: shining319
 * @Date: 2026/2/25
 * @Description: 库存验证结果视图对象
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stock check result")
public class StockCheckVO {

    @Schema(description = "库存是否充足（true=充足，false=不足）", example = "true")
    private Boolean available;

    @Schema(description = "当前库存数量", example = "50")
    private Integer currentStock;
}