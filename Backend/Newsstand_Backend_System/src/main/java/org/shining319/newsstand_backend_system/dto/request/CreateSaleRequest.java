package org.shining319.newsstand_backend_system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: 创建销售订单请求
 **/
@Data
@Schema(description = "Create sale order request")
public class CreateSaleRequest {

    @NotEmpty(message = "购物车不能为空")
    @Valid
    @Schema(description = "购物车商品列表（至少1个商品）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SaleItemRequest> items;
}
