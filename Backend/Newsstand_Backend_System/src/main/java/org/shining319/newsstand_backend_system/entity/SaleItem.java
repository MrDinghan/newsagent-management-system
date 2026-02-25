package org.shining319.newsstand_backend_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shining319.newsstand_backend_system.config.UuidBinaryTypeHandler;

import java.math.BigDecimal;

/**
 * @Author: shining319
 * @Date: 2026/2/25
 * @Description: 销售明细实体类
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sale_items", autoResultMap = true)
@Schema(description = "销售明细实体")
public class SaleItem {

    @TableId(type = IdType.INPUT)
    @TableField(value = "id", typeHandler = UuidBinaryTypeHandler.class)
    @Schema(description = "明细ID (UUIDv7)", example = "019512f3-b2c3-7000-9d4e-5f6a7b8c9d0e")
    private String id;

    @TableField(value = "order_id", typeHandler = UuidBinaryTypeHandler.class)
    @Schema(description = "关联的销售订单ID", example = "019512f3-a1b2-7000-8c3d-4e5f6a7b8c9d")
    private String orderId;

    @TableField(value = "product_id", typeHandler = UuidBinaryTypeHandler.class)
    @Schema(description = "关联的产品ID", example = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e")
    private String productId;

    @TableField("product_name")
    @Schema(description = "产品名称（快照，记录销售时的名称）", example = "人民日报")
    private String productName;

    @TableField("unit_price")
    @Schema(description = "单价（快照，记录销售时的价格）", example = "2.50")
    private BigDecimal unitPrice;

    @TableField("quantity")
    @Schema(description = "购买数量", example = "2")
    private Integer quantity;

    @TableField("subtotal")
    @Schema(description = "小计金额（单价 × 数量）", example = "5.00")
    private BigDecimal subtotal;
}
