package org.shining319.newsstand_backend_system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shining319.newsstand_backend_system.dao.ProductMapper;
import org.shining319.newsstand_backend_system.dao.SaleItemMapper;
import org.shining319.newsstand_backend_system.dao.SaleOrderMapper;
import org.shining319.newsstand_backend_system.dto.request.CreateSaleRequest;
import org.shining319.newsstand_backend_system.dto.request.QuerySaleHistoryRequest;
import org.shining319.newsstand_backend_system.dto.request.SaleItemRequest;
import org.shining319.newsstand_backend_system.entity.SaleItem;
import org.shining319.newsstand_backend_system.entity.SaleOrder;
import org.shining319.newsstand_backend_system.exception.BusinessException;
import org.shining319.newsstand_backend_system.exception.NotFoundException;
import org.shining319.newsstand_backend_system.service.ISaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: SaleController单元测试
 * 测试范围：HTTP请求/响应处理、参数验证、异常转HTTP状态码、Service调用验证
 * 不测试：业务逻辑细节（留给SaleServiceImplTest）
 **/
@WebMvcTest(controllers = SaleController.class,
        excludeAutoConfiguration = {
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        })
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISaleService saleService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private SaleOrderMapper saleOrderMapper;

    @MockitoBean
    private SaleItemMapper saleItemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PRODUCT_ID_1 = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e";
    private static final String PRODUCT_ID_2 = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8f";

    /**
     * 构建Mock Service返回的SaleOrder（含明细）
     */
    private SaleOrder buildMockSaleOrder() {
        SaleItem item = new SaleItem();
        item.setId("item-uuid");
        item.setOrderId("order-uuid");
        item.setProductId(PRODUCT_ID_1);
        item.setProductName("人民日报");
        item.setUnitPrice(new BigDecimal("2.50"));
        item.setQuantity(2);
        item.setSubtotal(new BigDecimal("5.00"));

        SaleOrder order = new SaleOrder();
        order.setId("order-uuid");
        order.setOrderNumber("SO20260226143025001");
        order.setTotalAmount(new BigDecimal("5.00"));
        order.setItemCount(1);
        order.setTotalQuantity(2);
        order.setCreatedAt(LocalDateTime.of(2026, 2, 26, 14, 30, 25));
        order.setItems(List.of(item));
        return order;
    }

    /**
     * 构建请求体并序列化为JSON字符串
     */
    private String buildRequestJson(String productId, int quantity) throws Exception {
        SaleItemRequest item = new SaleItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(item));
        return objectMapper.writeValueAsString(request);
    }

    // ==================== B2.6.2: 正常下单 ====================

    @Test
    @DisplayName("创建销售订单 - 成功（200 OK，返回含明细的订单）")
    void testCreateSale_Success() throws Exception {
        // Given
        SaleOrder order = buildMockSaleOrder();
        when(saleService.createSale(any(CreateSaleRequest.class))).thenReturn(order);

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(PRODUCT_ID_1, 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("order-uuid"))
                .andExpect(jsonPath("$.data.orderNumber").value("SO20260226143025001"))
                .andExpect(jsonPath("$.data.totalAmount").value(5.00))
                .andExpect(jsonPath("$.data.itemCount").value(1))
                .andExpect(jsonPath("$.data.totalQuantity").value(2))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].productId").value(PRODUCT_ID_1))
                .andExpect(jsonPath("$.data.items[0].productName").value("人民日报"))
                .andExpect(jsonPath("$.data.items[0].unitPrice").value(2.50))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].subtotal").value(5.00));

        verify(saleService, times(1)).createSale(any(CreateSaleRequest.class));
    }

    @Test
    @DisplayName("创建销售订单 - 多商品成功（200 OK）")
    void testCreateSale_Success_MultipleItems() throws Exception {
        // Given
        SaleItem item1 = new SaleItem();
        item1.setId("item-1");
        item1.setProductId(PRODUCT_ID_1);
        item1.setProductName("人民日报");
        item1.setUnitPrice(new BigDecimal("2.50"));
        item1.setQuantity(2);
        item1.setSubtotal(new BigDecimal("5.00"));

        SaleItem item2 = new SaleItem();
        item2.setId("item-2");
        item2.setProductId(PRODUCT_ID_2);
        item2.setProductName("读者");
        item2.setUnitPrice(new BigDecimal("10.00"));
        item2.setQuantity(1);
        item2.setSubtotal(new BigDecimal("10.00"));

        SaleOrder order = new SaleOrder();
        order.setId("order-uuid");
        order.setOrderNumber("SO20260226143025001");
        order.setTotalAmount(new BigDecimal("15.00"));
        order.setItemCount(2);
        order.setTotalQuantity(3);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(List.of(item1, item2));

        when(saleService.createSale(any(CreateSaleRequest.class))).thenReturn(order);

        SaleItemRequest req1 = new SaleItemRequest();
        req1.setProductId(PRODUCT_ID_1);
        req1.setQuantity(2);
        SaleItemRequest req2 = new SaleItemRequest();
        req2.setProductId(PRODUCT_ID_2);
        req2.setQuantity(1);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(req1, req2));

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAmount").value(15.00))
                .andExpect(jsonPath("$.data.itemCount").value(2))
                .andExpect(jsonPath("$.data.totalQuantity").value(3))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2));

        verify(saleService, times(1)).createSale(any(CreateSaleRequest.class));
    }

    // ==================== B2.6.2: 购物车为空 ====================

    @Test
    @DisplayName("创建销售订单 - 购物车为空（空列表）→ 400 Bad Request")
    void testCreateSale_EmptyItems() throws Exception {
        // Given: items是空列表（@NotEmpty验证失败）
        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of());

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).createSale(any());
    }

    @Test
    @DisplayName("创建销售订单 - 购物车为null → 400 Bad Request")
    void testCreateSale_NullItems() throws Exception {
        // Given: items字段为null（@NotEmpty包含null检查）
        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(null);

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).createSale(any());
    }

    // ==================== B2.6.2: 数量校验 ====================

    @Test
    @DisplayName("创建销售订单 - quantity=0（@Min(1)验证失败）→ 400")
    void testCreateSale_InvalidQuantity_Zero() throws Exception {
        // Given: quantity为0，不符合@Min(1)
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(PRODUCT_ID_1, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).createSale(any());
    }

    @Test
    @DisplayName("创建销售订单 - quantity超过最大值（>9999）→ 400")
    void testCreateSale_InvalidQuantity_TooLarge() throws Exception {
        // Given: quantity为10000，超过@Max(9999)
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(PRODUCT_ID_1, 10000)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).createSale(any());
    }

    @Test
    @DisplayName("创建销售订单 - quantity=null → 400")
    void testCreateSale_NullQuantity() throws Exception {
        // Given: quantity字段缺失（@NotNull验证失败）
        String json = "{\"items\":[{\"productId\":\"" + PRODUCT_ID_1 + "\"}]}";

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).createSale(any());
    }

    // ==================== B2.6.2: productId校验 ====================

    @Test
    @DisplayName("创建销售订单 - productId为空字符串（@NotBlank）→ 400")
    void testCreateSale_BlankProductId() throws Exception {
        // Given: productId为空字符串
        SaleItemRequest item = new SaleItemRequest();
        item.setProductId("");
        item.setQuantity(2);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).createSale(any());
    }

    @Test
    @DisplayName("创建销售订单 - productId为空白字符串（@NotBlank）→ 400")
    void testCreateSale_WhitespaceProductId() throws Exception {
        // Given: productId仅有空格
        SaleItemRequest item = new SaleItemRequest();
        item.setProductId("   ");
        item.setQuantity(2);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).createSale(any());
    }

    // ==================== B2.6.2: 业务异常 → HTTP映射 ====================

    @Test
    @DisplayName("创建销售订单 - 商品不存在 → 404 Not Found")
    void testCreateSale_ProductNotFound() throws Exception {
        // Given
        when(saleService.createSale(any(CreateSaleRequest.class)))
                .thenThrow(new NotFoundException("Product not found: id=" + PRODUCT_ID_1));

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(PRODUCT_ID_1, 2)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Product not found: id=" + PRODUCT_ID_1));

        verify(saleService, times(1)).createSale(any(CreateSaleRequest.class));
    }

    @Test
    @DisplayName("创建销售订单 - 库存不足（BusinessException）→ 400 Bad Request")
    void testCreateSale_InsufficientStock() throws Exception {
        // Given
        when(saleService.createSale(any(CreateSaleRequest.class)))
                .thenThrow(new BusinessException("Insufficient stock for product '人民日报': required=10, available=5"));

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(PRODUCT_ID_1, 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Insufficient stock for product '人民日报': required=10, available=5"));

        verify(saleService, times(1)).createSale(any(CreateSaleRequest.class));
    }

    @Test
    @DisplayName("创建销售订单 - 并发冲突（乐观锁，BusinessException）→ 400 Bad Request")
    void testCreateSale_ConcurrentConflict() throws Exception {
        // Given
        when(saleService.createSale(any(CreateSaleRequest.class)))
                .thenThrow(new BusinessException("Stock update failed due to concurrent conflict for product '人民日报', please retry"));

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(PRODUCT_ID_1, 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Stock update failed due to concurrent conflict for product '人民日报', please retry"));

        verify(saleService, times(1)).createSale(any(CreateSaleRequest.class));
    }

    @Test
    @DisplayName("创建销售订单 - 重复productId（BusinessException）→ 400 Bad Request")
    void testCreateSale_DuplicateProductId() throws Exception {
        // Given
        when(saleService.createSale(any(CreateSaleRequest.class)))
                .thenThrow(new BusinessException("Duplicate productId found in items, please merge quantities for the same product"));

        SaleItemRequest req1 = new SaleItemRequest();
        req1.setProductId(PRODUCT_ID_1);
        req1.setQuantity(2);
        SaleItemRequest req2 = new SaleItemRequest();
        req2.setProductId(PRODUCT_ID_1);  // 重复
        req2.setQuantity(3);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(req1, req2));

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Duplicate productId found in items, please merge quantities for the same product"));

        verify(saleService, times(1)).createSale(any(CreateSaleRequest.class));
    }

    // ==================== B2.7.1: 获取订单详情 ====================

    @Test
    @DisplayName("获取订单详情 - 成功（200 OK，返回含明细的订单）")
    void testGetSaleById_Success() throws Exception {
        // Given
        SaleOrder order = buildMockSaleOrder();
        when(saleService.getSaleById(eq("order-uuid"))).thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/sales/order-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("order-uuid"))
                .andExpect(jsonPath("$.data.orderNumber").value("SO20260226143025001"))
                .andExpect(jsonPath("$.data.totalAmount").value(5.00))
                .andExpect(jsonPath("$.data.itemCount").value(1))
                .andExpect(jsonPath("$.data.totalQuantity").value(2))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].productId").value(PRODUCT_ID_1))
                .andExpect(jsonPath("$.data.items[0].productName").value("人民日报"))
                .andExpect(jsonPath("$.data.items[0].unitPrice").value(2.50))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].subtotal").value(5.00));

        verify(saleService, times(1)).getSaleById("order-uuid");
    }

    @Test
    @DisplayName("获取订单详情 - 订单不存在（404 Not Found）")
    void testGetSaleById_NotFound() throws Exception {
        // Given
        when(saleService.getSaleById(eq("non-existent-id")))
                .thenThrow(new NotFoundException("Sale order not found: id=non-existent-id"));

        // When & Then
        mockMvc.perform(get("/api/sales/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("Sale order not found: id=non-existent-id"));

        verify(saleService, times(1)).getSaleById("non-existent-id");
    }

    // ==================== B2.8.1: 查询销售历史 ====================

    /**
     * 构建分页订单列表（不含明细）
     */
    private IPage<SaleOrder> buildMockOrderPage(List<SaleOrder> orders, long total, long pages) {
        Page<SaleOrder> page = new Page<>();
        page.setRecords(orders);
        page.setTotal(total);
        page.setPages(pages);
        return page;
    }

    private SaleOrder buildMockOrderWithoutItems(String id, String orderNumber,
                                                  BigDecimal totalAmount, int itemCount, int totalQuantity) {
        SaleOrder order = new SaleOrder();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(totalAmount);
        order.setItemCount(itemCount);
        order.setTotalQuantity(totalQuantity);
        order.setCreatedAt(LocalDateTime.of(2026, 2, 26, 14, 30, 25));
        order.setItems(null);  // 列表查询不含明细
        return order;
    }

    @Test
    @DisplayName("查询销售历史 - 成功（默认参数，返回分页列表）")
    void testGetSaleHistory_Success_DefaultParams() throws Exception {
        // Given
        SaleOrder order = buildMockOrderWithoutItems(
                "order-uuid", "SO20260226143025001", new BigDecimal("5.00"), 1, 2);
        IPage<SaleOrder> mockPage = buildMockOrderPage(List.of(order), 1L, 1L);
        when(saleService.getSaleHistory(any(QuerySaleHistoryRequest.class))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("order-uuid"))
                .andExpect(jsonPath("$.data[0].orderNumber").value("SO20260226143025001"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(5.00))
                .andExpect(jsonPath("$.data[0].itemCount").value(1))
                .andExpect(jsonPath("$.data[0].totalQuantity").value(2))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(saleService, times(1)).getSaleHistory(any(QuerySaleHistoryRequest.class));
    }

    @Test
    @DisplayName("查询销售历史 - 带日期范围筛选（200 OK）")
    void testGetSaleHistory_WithDateRange() throws Exception {
        // Given
        IPage<SaleOrder> mockPage = buildMockOrderPage(List.of(), 0L, 0L);
        when(saleService.getSaleHistory(any(QuerySaleHistoryRequest.class))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/sales")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.total").value(0));

        verify(saleService, times(1)).getSaleHistory(any(QuerySaleHistoryRequest.class));
    }

    @Test
    @DisplayName("查询销售历史 - 带分页参数（200 OK）")
    void testGetSaleHistory_WithPagination() throws Exception {
        // Given: size=20, total=60 → pages = ceil(60/20) = 3
        // 使用 Page(1, 20) 确保 getPages() 计算正确
        Page<SaleOrder> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of());
        mockPage.setTotal(60L);
        when(saleService.getSaleHistory(any(QuerySaleHistoryRequest.class))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/sales")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.total").value(60))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(saleService, times(1)).getSaleHistory(any(QuerySaleHistoryRequest.class));
    }

    @Test
    @DisplayName("查询销售历史 - 返回空列表（200 OK）")
    void testGetSaleHistory_EmptyResult() throws Exception {
        // Given
        IPage<SaleOrder> mockPage = buildMockOrderPage(List.of(), 0L, 0L);
        when(saleService.getSaleHistory(any(QuerySaleHistoryRequest.class))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("查询销售历史 - page为负数（@Min(0)验证失败）→ 400")
    void testGetSaleHistory_InvalidPage_Negative() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sales")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).getSaleHistory(any());
    }

    @Test
    @DisplayName("查询销售历史 - size为0（@Min(1)验证失败）→ 400")
    void testGetSaleHistory_InvalidSize_Zero() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sales")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).getSaleHistory(any());
    }

    @Test
    @DisplayName("查询销售历史 - size超过100（@Max(100)验证失败）→ 400")
    void testGetSaleHistory_InvalidSize_TooLarge() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sales")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(saleService, never()).getSaleHistory(any());
    }
}
