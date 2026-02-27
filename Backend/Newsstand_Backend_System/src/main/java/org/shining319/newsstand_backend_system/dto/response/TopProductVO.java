package org.shining319.newsstand_backend_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: 畅销商品视图对象（用于日报TOP5列表）
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Top selling product info")
public class TopProductVO {

    @Schema(description = "商品名称", example = "人民日报")
    private String productName;

    @Schema(description = "销售数量", example = "15")
    private Integer totalQuantity;
}
