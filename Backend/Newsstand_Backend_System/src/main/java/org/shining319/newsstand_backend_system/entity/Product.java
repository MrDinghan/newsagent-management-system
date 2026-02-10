package org.shining319.newsstand_backend_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shining319.newsstand_backend_system.config.UuidBinaryTypeHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 产品实体类
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "products",autoResultMap = true)
@Schema(description = "产品实体")
public class Product {

    @TableId(type = IdType.INPUT)
    @TableField(value = "id", typeHandler = UuidBinaryTypeHandler.class)
    @Schema(description = "产品ID (UUIDv7)", example = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e")
    private String id;

    @TableField("name")
    @Schema(description = "产品名称", example = "人民日报")
    private String name;

    @TableField("type")
    @Schema(description = "产品类型 (NEWSPAPER=报纸, MAGAZINE=杂志)", example = "NEWSPAPER")
    private String type;

    @TableField("price")
    @Schema(description = "销售价格", example = "2.50")
    private BigDecimal price;

    @TableField("stock")
    @Schema(description = "当前库存数量", example = "100")
    private Integer stock;

    @TableField("version")
    @Version
    @Schema(description = "乐观锁版本号", example = "0")
    private Integer version;

    @TableField("deleted")
    @TableLogic(value = "false", delval = "true")
    @Schema(description = "软删除标记 (false=正常, true=已删除)", example = "false")
    private Boolean deleted;

    @TableField("created_at")
    @Schema(description = "创建时间", example = "2026-02-08T10:30:00")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @Schema(description = "更新时间", example = "2026-02-08T10:30:00")
    private LocalDateTime updatedAt;
}
