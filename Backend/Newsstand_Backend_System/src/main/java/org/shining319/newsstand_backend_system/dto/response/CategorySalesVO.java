package org.shining319.newsstand_backend_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: 各类别销售统计视图对象（用于日报类别占比）
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sales breakdown by product category")
public class CategorySalesVO {

    @Schema(description = "报纸销售额（当日无记录时为0）", example = "500.00")
    private BigDecimal newspaperAmount;

    @Schema(description = "杂志销售额（当日无记录时为0）", example = "734.50")
    private BigDecimal magazineAmount;

    @Schema(description = "报纸销售占比（百分比，0-100，当日无记录时为0）", example = "40.5")
    private Double newspaperPercentage;

    @Schema(description = "杂志销售占比（百分比，0-100，当日无记录时为0）", example = "59.5")
    private Double magazinePercentage;
}
