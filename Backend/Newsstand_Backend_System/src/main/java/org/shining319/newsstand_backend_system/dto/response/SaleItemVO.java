package org.shining319.newsstand_backend_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shining319.newsstand_backend_system.entity.SaleItem;

import java.math.BigDecimal;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: 销售明细视图对象
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sale item view object")
public class SaleItemVO {

    @Schema(description = "明细ID", example = "019512f3-b2c3-7000-9d4e-5f6a7b8c9d0e")
    private String id;

    @Schema(description = "关联产品ID", example = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e")
    private String productId;

    @Schema(description = "产品名称（销售时快照）", example = "人民日报")
    private String productName;

    @Schema(description = "单价（销售时快照）", example = "2.50")
    private BigDecimal unitPrice;

    @Schema(description = "购买数量", example = "2")
    private Integer quantity;

    @Schema(description = "小计金额（单价 × 数量）", example = "5.00")
    private BigDecimal subtotal;

    public static SaleItemVO fromEntity(SaleItem item) {
        SaleItemVO vo = new SaleItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        return vo;
    }
}
