package org.shining319.newsstand_backend_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.shining319.newsstand_backend_system.dto.request.CreateSaleRequest;
import org.shining319.newsstand_backend_system.dto.response.Result;
import org.shining319.newsstand_backend_system.dto.response.SaleOrderVO;
import org.shining319.newsstand_backend_system.entity.SaleOrder;
import org.shining319.newsstand_backend_system.exception.GlobalExceptionHandler;
import org.shining319.newsstand_backend_system.service.ISaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: 销售管理控制器
 **/
@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sale Management", description = "Sale order management related interfaces")
public class SaleController {

    private final ISaleService saleService;

    @Autowired
    public SaleController(ISaleService saleService) {
        this.saleService = saleService;
    }

    /**
     * 创建销售订单
     *
     * @param request 创建销售订单请求（含购物车商品列表）
     * @return 创建的完整销售订单（含明细）
     */
    @PostMapping
    @Operation(
            summary = "Create sale order",
            description = "Complete a sale transaction: validates stock, deducts inventory, " +
                    "creates the order and its items. Returns the full order with item details. " +
                    "Uses optimistic locking to prevent concurrent stock issues."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sale order created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SaleOrderVOResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed (empty cart, invalid quantity) or business logic error (insufficient stock, concurrent conflict, duplicate productId)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ValidationExceptionResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - A product in the cart does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.NotFoundExceptionResult.class)
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Create sale order request body",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateSaleRequest.class)
            )
    )
    public Result<SaleOrderVO> createSale(@Valid @RequestBody CreateSaleRequest request) {
        SaleOrder order = saleService.createSale(request);
        SaleOrderVO vo = SaleOrderVO.fromEntity(order);
        return Result.ok(vo);
    }

    /**
     * Swagger文档用的响应包装类
     * 用于在OpenAPI文档中正确显示Result<SaleOrderVO>的结构
     */
    @Schema(description = "Create sale order response")
    private static class SaleOrderVOResult extends Result<SaleOrderVO> {
        @Schema(description = "销售订单数据（含明细列表）")
        @Override
        public SaleOrderVO getData() {
            return super.getData();
        }
    }
}
