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
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: shining319
 * @Date: 2026/2/25
 * @Description: 销售订单实体类
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sale_orders", autoResultMap = true)
@Schema(description = "销售订单实体")
public class SaleOrder {

    @TableId(type = IdType.INPUT)
    @TableField(value = "id", typeHandler = UuidBinaryTypeHandler.class)
    @Schema(description = "订单ID (UUIDv7)", example = "019512f3-a1b2-7000-8c3d-4e5f6a7b8c9d")
    private String id;

    @TableField("order_number")
    @Schema(description = "订单号（格式：SOyyyyMMddHHmmss+序号）", example = "SO20260225143025001")
    private String orderNumber;

    @TableField("total_amount")
    @Schema(description = "订单总金额", example = "25.50")
    private BigDecimal totalAmount;

    @TableField("item_count")
    @Schema(description = "商品种类数量", example = "3")
    private Integer itemCount;

    @TableField("total_quantity")
    @Schema(description = "商品总数量", example = "5")
    private Integer totalQuantity;

    @TableField("created_at")
    @Schema(description = "订单创建时间", example = "2026-02-25T14:30:25")
    private LocalDateTime createdAt;

    /**
     * 订单明细列表（非数据库字段，Mybatis-Plus关联字段）
     * 通过 SaleOrderMapper.xml 中的 collection ResultMap 加载
     */
    @TableField(exist = false)
    @Schema(description = "订单明细列表")
    private List<SaleItem> items;
}
