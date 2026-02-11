package org.shining319.newsstand_backend_system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shining319.newsstand_backend_system.dao.ProductMapper;
import org.shining319.newsstand_backend_system.dto.request.AdjustStockRequest;
import org.shining319.newsstand_backend_system.dto.request.CreateProductRequest;
import org.shining319.newsstand_backend_system.dto.request.QueryProductRequest;
import org.shining319.newsstand_backend_system.dto.request.UpdateProductRequest;
import org.shining319.newsstand_backend_system.entity.Product;
import org.shining319.newsstand_backend_system.entity.ProductTypeEnum;
import org.shining319.newsstand_backend_system.exception.BusinessException;
import org.shining319.newsstand_backend_system.exception.ConflictException;
import org.shining319.newsstand_backend_system.exception.NotFoundException;
import org.shining319.newsstand_backend_system.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Author: shining319
 * @Date: 2026/2/11
 * @Description: ProductController单元测试
 * 测试范围：HTTP请求/响应处理、参数验证、异常转HTTP状态码、Service调用验证
 * 不测试：业务逻辑细节（留给Service单测）
 **/
@WebMvcTest(controllers = ProductController.class,
        excludeAutoConfiguration = {
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        })
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建测试用Product实体
     */
    private Product createTestProduct() {
        Product product = new Product();
        product.setId("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e");
        product.setName("人民日报");
        product.setType("NEWSPAPER");
        product.setPrice(new BigDecimal("2.50"));
        product.setStock(100);
        product.setVersion(0);
        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    // ==================== B1.1.3: 创建产品API单元测试 ====================

    @Test
    @DisplayName("创建产品 - 成功")
    void testCreateProduct_Success() throws Exception {
        // Given
        CreateProductRequest request = new CreateProductRequest();
        request.setName("人民日报");
        request.setType(ProductTypeEnum.NEWSPAPER);
        request.setPrice(new BigDecimal("2.50"));
        request.setStock(100);

        Product product = createTestProduct();
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(product);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("人民日报"))
                .andExpect(jsonPath("$.data.type").value("NEWSPAPER"))
                .andExpect(jsonPath("$.data.price").value(2.50))
                .andExpect(jsonPath("$.data.stock").value(100));

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("创建产品 - name为空（Bean Validation）")
    void testCreateProduct_NameBlank() throws Exception {
        // Given
        CreateProductRequest request = new CreateProductRequest();
        request.setName("");  // 空字符串
        request.setType(ProductTypeEnum.NEWSPAPER);
        request.setPrice(new BigDecimal("2.50"));
        request.setStock(100);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("创建产品 - price<=0（Bean Validation）")
    void testCreateProduct_PriceInvalid() throws Exception {
        // Given
        CreateProductRequest request = new CreateProductRequest();
        request.setName("人民日报");
        request.setType(ProductTypeEnum.NEWSPAPER);
        request.setPrice(new BigDecimal("0.00"));  // 价格为0
        request.setStock(100);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("创建产品 - name重复（409 Conflict）")
    void testCreateProduct_NameDuplicate() throws Exception {
        // Given
        CreateProductRequest request = new CreateProductRequest();
        request.setName("人民日报");
        request.setType(ProductTypeEnum.NEWSPAPER);
        request.setPrice(new BigDecimal("2.50"));
        request.setStock(100);

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new ConflictException("Product name already exists: 人民日报"));

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Product name already exists: 人民日报"));

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }

    // ==================== B1.2.2: 查询产品列表API单元测试 ====================

    @Test
    @DisplayName("查询产品列表 - 成功（分页）")
    void testQueryProducts_Success() throws Exception {
        // Given
        List<Product> products = new ArrayList<>();
        products.add(createTestProduct());

        Page<Product> page = new Page<>(1, 20);
        page.setRecords(products);
        page.setTotal(1);
        page.setPages(1);

        when(productService.queryProducts(any(QueryProductRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("人民日报"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(productService, times(1)).queryProducts(any(QueryProductRequest.class));
    }

    @Test
    @DisplayName("查询产品列表 - 类型筛选")
    void testQueryProducts_WithTypeFilter() throws Exception {
        // Given
        Page<Product> page = new Page<>(1, 20);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);
        page.setPages(0);

        when(productService.queryProducts(any(QueryProductRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "20")
                        .param("type", "NEWSPAPER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(productService, times(1)).queryProducts(any(QueryProductRequest.class));
    }

    @Test
    @DisplayName("查询产品列表 - 空结果")
    void testQueryProducts_EmptyResult() throws Exception {
        // Given
        Page<Product> page = new Page<>(1, 20);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);
        page.setPages(0);

        when(productService.queryProducts(any(QueryProductRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.total").value(0));

        verify(productService, times(1)).queryProducts(any(QueryProductRequest.class));
    }

    @Test
    @DisplayName("查询产品列表 - page<0（Bean Validation）")
    void testQueryProducts_InvalidPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(productService, never()).queryProducts(any());
    }

    @Test
    @DisplayName("查询产品列表 - size>100（Bean Validation）")
    void testQueryProducts_InvalidSize() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(productService, never()).queryProducts(any());
    }

    // ==================== B1.3.2: 更新产品API单元测试 ====================

    @Test
    @DisplayName("更新产品 - 成功")
    void testUpdateProduct_Success() throws Exception {
        // Given
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("人民日报（新版）");
        request.setPrice(new BigDecimal("3.00"));

        Product product = createTestProduct();
        product.setName("人民日报（新版）");
        product.setPrice(new BigDecimal("3.00"));

        when(productService.updateProduct(eq("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"), any(UpdateProductRequest.class)))
                .thenReturn(product);

        // When & Then
        mockMvc.perform(put("/api/products/018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("人民日报（新版）"))
                .andExpect(jsonPath("$.data.price").value(3.00));

        verify(productService, times(1)).updateProduct(eq("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"), any(UpdateProductRequest.class));
    }

    @Test
    @DisplayName("更新产品 - 产品不存在（404 Not Found）")
    void testUpdateProduct_NotFound() throws Exception {
        // Given
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("人民日报（新版）");

        when(productService.updateProduct(eq("non-existent-id"), any(UpdateProductRequest.class)))
                .thenThrow(new NotFoundException("Product not found: id=non-existent-id"));

        // When & Then
        mockMvc.perform(put("/api/products/non-existent-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Product not found: id=non-existent-id"));

        verify(productService, times(1)).updateProduct(eq("non-existent-id"), any(UpdateProductRequest.class));
    }

    @Test
    @DisplayName("更新产品 - price无效（Bean Validation）")
    void testUpdateProduct_InvalidPrice() throws Exception {
        // Given
        UpdateProductRequest request = new UpdateProductRequest();
        request.setPrice(new BigDecimal("0.00"));  // 价格为0

        // When & Then
        mockMvc.perform(put("/api/products/018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(productService, never()).updateProduct(any(), any());
    }

    // ==================== B1.4.2: 调整库存API单元测试 ====================

    @Test
    @DisplayName("调整库存 - 增加库存（成功）")
    void testAdjustStock_IncreaseStock() throws Exception {
        // Given
        AdjustStockRequest request = new AdjustStockRequest();
        request.setQuantity(50);  // 增加50

        Product product = createTestProduct();
        product.setStock(150);  // 原100 + 50 = 150

        when(productService.adjustStock(eq("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"), any(AdjustStockRequest.class)))
                .thenReturn(product);

        // When & Then
        mockMvc.perform(post("/api/products/018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e/adjust-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stock").value(150));

        verify(productService, times(1)).adjustStock(eq("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"), any(AdjustStockRequest.class));
    }

    @Test
    @DisplayName("调整库存 - 减少库存（成功）")
    void testAdjustStock_DecreaseStock() throws Exception {
        // Given
        AdjustStockRequest request = new AdjustStockRequest();
        request.setQuantity(-30);  // 减少30

        Product product = createTestProduct();
        product.setStock(70);  // 原100 - 30 = 70

        when(productService.adjustStock(eq("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"), any(AdjustStockRequest.class)))
                .thenReturn(product);

        // When & Then
        mockMvc.perform(post("/api/products/018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e/adjust-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stock").value(70));

        verify(productService, times(1)).adjustStock(eq("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"), any(AdjustStockRequest.class));
    }

    @Test
    @DisplayName("调整库存 - 库存不足（400 Business Exception）")
    void testAdjustStock_InsufficientStock() throws Exception {
        // Given
        AdjustStockRequest request = new AdjustStockRequest();
        request.setQuantity(-150);  // 减少150，但当前只有100

        when(productService.adjustStock(eq("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"), any(AdjustStockRequest.class)))
                .thenThrow(new BusinessException("Insufficient stock, current stock: 100, adjustment: -150"));

        // When & Then
        mockMvc.perform(post("/api/products/018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e/adjust-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Insufficient stock, current stock: 100, adjustment: -150"));

        verify(productService, times(1)).adjustStock(eq("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e"), any(AdjustStockRequest.class));
    }

    @Test
    @DisplayName("调整库存 - 产品不存在（404 Not Found）")
    void testAdjustStock_NotFound() throws Exception {
        // Given
        AdjustStockRequest request = new AdjustStockRequest();
        request.setQuantity(50);

        when(productService.adjustStock(eq("non-existent-id"), any(AdjustStockRequest.class)))
                .thenThrow(new NotFoundException("Product not found: id=non-existent-id"));

        // When & Then
        mockMvc.perform(post("/api/products/non-existent-id/adjust-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Product not found: id=non-existent-id"));

        verify(productService, times(1)).adjustStock(eq("non-existent-id"), any(AdjustStockRequest.class));
    }

    @Test
    @DisplayName("调整库存 - quantity为null（Bean Validation）")
    void testAdjustStock_QuantityNull() throws Exception {
        // Given
        AdjustStockRequest request = new AdjustStockRequest();
        // quantity未设置，为null

        // When & Then
        mockMvc.perform(post("/api/products/018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e/adjust-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(productService, never()).adjustStock(any(), any());
    }

    @Test
    @DisplayName("调整库存 - quantity超出范围（Bean Validation）")
    void testAdjustStock_QuantityOutOfRange() throws Exception {
        // Given
        AdjustStockRequest request = new AdjustStockRequest();
        request.setQuantity(10000);  // 超过最大值9999

        // When & Then
        mockMvc.perform(post("/api/products/018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e/adjust-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(productService, never()).adjustStock(any(), any());
    }
}
