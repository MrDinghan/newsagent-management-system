package org.shining319.newsstand_backend_system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.shining319.newsstand_backend_system.dto.request.CreateProductRequest;
import org.shining319.newsstand_backend_system.dto.request.QueryProductRequest;
import org.shining319.newsstand_backend_system.dto.response.ProductVO;
import org.shining319.newsstand_backend_system.dto.response.Result;
import org.shining319.newsstand_backend_system.entity.Product;
import org.shining319.newsstand_backend_system.exception.GlobalExceptionHandler;
import org.shining319.newsstand_backend_system.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: shining319
 * @Date: 2026/2/9
 * @Description: 产品管理控制器
 **/
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "Product management related interfaces")
public class ProductController {

    private final IProductService productService;
    @Autowired
    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    /**
     * 创建产品
     *
     * @param request 创建产品请求
     * @return 创建的产品信息
     */
    @PostMapping("/create")
    @Operation(
            summary = "Create product",
            description = "Create a new newspaper or magazine product, the product name cannot be repeated"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductVOResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed (field validation errors)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ValidationExceptionResult.class)
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Create product request body",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateProductRequest.class)
            )
    )
    public Result<ProductVO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productService.createProduct(request);
        ProductVO vo = ProductVO.fromEntity(product);
        return Result.ok(vo);
    }

    /**
     * 分页查询产品列表
     *
     * @param request 查询请求
     * @return 产品列表
     */
    @GetMapping("/query")
    @Operation(
            summary = "Query product list with pagination",
            description = "Query product list with pagination and optional type filter. " +
                    "Returns non-deleted products ordered by creation time descending. " +
                    "**IMPORTANT: page parameter starts from 0 (0 = first page)**"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Query successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductListResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed (field validation errors)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ValidationExceptionResult.class)
                    )
            )
    })
    @Parameters({
            @Parameter(
                    name = "page",
                    description = "**Page number (starts from 0, 0 means first page)**",
                    example = "0",
                    schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            ),
            @Parameter(
                    name = "size",
                    description = "Page size (1-100)",
                    example = "20",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20")
            ),
            @Parameter(
                    name = "type",
                    description = "Product type (optional, query all if not specified)",
                    example = "NEWSPAPER",
                    schema = @Schema(type = "string", allowableValues = {"NEWSPAPER", "MAGAZINE"})
            )
    })
    public Result<List<ProductVO>> queryProducts(@Valid QueryProductRequest request) {
        // 1. 调用Service执行分页查询
        Page<Product> pageResult = productService.queryProducts(request);

        // 2. 转换Entity为VO（隐藏内部字段）
        List<ProductVO> voList = pageResult.getRecords()
                .stream()
                .map(ProductVO::fromEntity)
                .collect(Collectors.toList());

        // 3. 包装为Result，返回data、total和totalPages
        return Result.ok(
                voList,                        // data: 产品列表
                pageResult.getTotal(),         // total: 总记录数
                (int) pageResult.getPages()    // totalPages: 总页数
        );
    }

    /**
     * Swagger文档用的自定义响应包装类
     * 用于在OpenAPI文档中正确显示Result<ProductVO>的结构
     */
    @Schema(description = "创建产品响应")
    private static class ProductVOResult extends Result<ProductVO> {
        @Schema(description = "产品数据")
        @Override
        public ProductVO getData() {
            return super.getData();
        }
    }

    /**
     * Swagger文档用的分页查询响应包装类
     * 用于在OpenAPI文档中正确显示Result<List<ProductVO>>的结构
     */
    @Schema(description = "分页查询产品响应")
    private static class ProductListResult extends Result<List<ProductVO>> {
        @Schema(description = "产品列表")
        @Override
        public List<ProductVO> getData() {
            return super.getData();
        }

        @Schema(description = "总记录数", example = "100")
        @Override
        public Long getTotal() {
            return super.getTotal();
        }

        @Schema(description = "总页数", example = "5")
        @Override
        public Integer getTotalPages() {
            return super.getTotalPages();
        }
    }
}
