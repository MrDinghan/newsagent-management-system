package org.shining319.newsstand_backend_system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shining319.newsstand_backend_system.entity.Product;
import org.shining319.newsstand_backend_system.entity.ProductTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author: shining319
 * @Date: 2026/2/9
 * @Description: 产品视图对象
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product view object")
public class ProductVO {

    @Schema(description = "产品ID", example = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e")
    private String id;

    @Schema(description = "产品名称", example = "人民日报")
    private String name;

    @Schema(description = "产品类型", example = "NEWSPAPER")
    private ProductTypeEnum type;

    @Schema(description = "销售价格", example = "2.50")
    private BigDecimal price;

    @Schema(description = "当前库存数量", example = "100")
    private Integer stock;

    @Schema(description = "创建时间", example = "2026-02-08T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-02-08T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * 从Product实体转换为ProductVO
     *
     * @param product 产品实体
     * @return ProductVO
     */
    public static ProductVO fromEntity(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setType(ProductTypeEnum.valueOf(product.getType()));
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setCreatedAt(product.getCreatedAt());
        vo.setUpdatedAt(product.getUpdatedAt());
        return vo;
    }
}
