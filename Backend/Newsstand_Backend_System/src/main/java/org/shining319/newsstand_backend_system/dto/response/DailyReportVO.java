package org.shining319.newsstand_backend_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: 销售日报视图对象
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Daily sales report data")
public class DailyReportVO {

    @Schema(description = "查询日期", example = "2026-02-27")
    private LocalDate date;

    @Schema(description = "总销售额（当日无记录时为0）", example = "1234.50")
    private BigDecimal totalAmount;

    @Schema(description = "订单总数（当日无记录时为0）", example = "5")
    private Integer orderCount;

    @Schema(description = "TOP5畅销商品列表（当日无记录时为空列表）")
    private List<TopProductVO> topProducts;

    @Schema(description = "各类别销售统计（当日无记录时金额为0、占比为0）")
    private CategorySalesVO categorySales;
}
