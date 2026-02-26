package org.shining319.newsstand_backend_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shining319.newsstand_backend_system.entity.SaleOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: 销售订单视图对象（含明细列表）
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sale order view object (includes item details)")
public class SaleOrderVO {

    @Schema(description = "订单ID", example = "019512f3-a1b2-7000-8c3d-4e5f6a7b8c9d")
    private String id;

    @Schema(description = "订单号", example = "SO20260226143025001")
    private String orderNumber;

    @Schema(description = "订单总金额", example = "25.50")
    private BigDecimal totalAmount;

    @Schema(description = "商品种类数量", example = "3")
    private Integer itemCount;

    @Schema(description = "商品总数量", example = "5")
    private Integer totalQuantity;

    @Schema(description = "订单创建时间", example = "2026-02-26T14:30:25")
    private LocalDateTime createdAt;

    @Schema(description = "订单明细列表")
    private List<SaleItemVO> items;

    public static SaleOrderVO fromEntity(SaleOrder order) {
        SaleOrderVO vo = new SaleOrderVO();
        vo.setId(order.getId());
        vo.setOrderNumber(order.getOrderNumber());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setItemCount(order.getItemCount());
        vo.setTotalQuantity(order.getTotalQuantity());
        vo.setCreatedAt(order.getCreatedAt());
        if (order.getItems() != null) {
            vo.setItems(order.getItems().stream()
                    .map(SaleItemVO::fromEntity)
                    .collect(Collectors.toList()));
        }
        return vo;
    }
}
