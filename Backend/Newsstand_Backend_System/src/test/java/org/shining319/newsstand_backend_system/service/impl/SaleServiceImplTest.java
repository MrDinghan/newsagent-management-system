package org.shining319.newsstand_backend_system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shining319.newsstand_backend_system.dao.ProductMapper;
import org.shining319.newsstand_backend_system.dao.SaleItemMapper;
import org.shining319.newsstand_backend_system.dao.SaleOrderMapper;
import org.shining319.newsstand_backend_system.dto.request.CreateSaleRequest;
import org.shining319.newsstand_backend_system.dto.request.QuerySaleHistoryRequest;
import org.shining319.newsstand_backend_system.dto.request.SaleItemRequest;
import org.shining319.newsstand_backend_system.entity.Product;
import org.shining319.newsstand_backend_system.entity.SaleItem;
import org.shining319.newsstand_backend_system.entity.SaleOrder;
import org.shining319.newsstand_backend_system.exception.BusinessException;
import org.shining319.newsstand_backend_system.exception.NotFoundException;
import org.shining319.newsstand_backend_system.util.OrderNumberGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: SaleServiceImpl单元测试
 * 测试范围：业务逻辑（重复检查、商品校验、库存扣减、订单构建）
 * 不测试：HTTP层、数据库实际操作
 **/
@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private SaleOrderMapper saleOrderMapper;

    @Mock
    private SaleItemMapper saleItemMapper;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @InjectMocks
    private SaleServiceImpl saleService;

    private static final String PRODUCT_ID_1 = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e";
    private static final String PRODUCT_ID_2 = "018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8f";
    private static final String ORDER_NUMBER = "SO20260226143025001";

    /**
     * 构建测试用Product实体
     */
    private Product buildProduct(String id, String name, BigDecimal price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setType("NEWSPAPER");
        product.setPrice(price);
        product.setStock(stock);
        product.setVersion(0);
        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    /**
     * 构建测试用SaleItemRequest
     */
    private SaleItemRequest buildItemRequest(String productId, int quantity) {
        SaleItemRequest req = new SaleItemRequest();
        req.setProductId(productId);
        req.setQuantity(quantity);
        return req;
    }

    /**
     * 构建测试用CreateSaleRequest
     */
    private CreateSaleRequest buildSaleRequest(SaleItemRequest... items) {
        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(items));
        return req;
    }

    /**
     * 构建Mock返回用的SaleOrder（含明细）
     */
    private SaleOrder buildMockReturnOrder(String orderId, String orderNumber,
                                            BigDecimal totalAmount, int itemCount, int totalQuantity) {
        SaleOrder order = new SaleOrder();
        order.setId(orderId);
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(totalAmount);
        order.setItemCount(itemCount);
        order.setTotalQuantity(totalQuantity);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(List.of());
        return order;
    }

    /**
     * 为成功创建订单的测试设置通用 stub
     */
    private void stubForSuccessfulOrder() {
        when(orderNumberGenerator.generate()).thenReturn(ORDER_NUMBER);
        when(saleOrderMapper.insertOrder(any(SaleOrder.class))).thenReturn(1);
        when(saleItemMapper.insertItems(anyList())).thenReturn(1);
    }

    // ==================== B2.6.2: 正常下单 ====================

    @Test
    @DisplayName("创建销售订单 - 单商品正常下单")
    void testCreateSale_Success_SingleItem() {
        // Given
        stubForSuccessfulOrder();
        Product product = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 100);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product);
        when(productMapper.adjustStockById(eq(PRODUCT_ID_1), eq(97), eq(0), any(LocalDateTime.class))).thenReturn(1);

        SaleOrder returnOrder = buildMockReturnOrder("order-id", ORDER_NUMBER,
                new BigDecimal("7.50"), 1, 3);
        when(saleOrderMapper.selectOrderWithItems(anyString())).thenReturn(returnOrder);

        CreateSaleRequest request = buildSaleRequest(buildItemRequest(PRODUCT_ID_1, 3));

        // When
        SaleOrder result = saleService.createSale(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(ORDER_NUMBER);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("7.50"));
        assertThat(result.getItemCount()).isEqualTo(1);
        assertThat(result.getTotalQuantity()).isEqualTo(3);

        // 验证调用次数
        verify(productMapper, times(1)).selectProductById(PRODUCT_ID_1);
        verify(productMapper, times(1)).adjustStockById(eq(PRODUCT_ID_1), eq(97), eq(0), any());
        verify(saleOrderMapper, times(1)).insertOrder(any(SaleOrder.class));
        verify(saleItemMapper, times(1)).insertItems(anyList());
        verify(saleOrderMapper, times(1)).selectOrderWithItems(anyString());
    }

    @Test
    @DisplayName("创建销售订单 - 多商品下单，验证金额/种类/数量计算")
    void testCreateSale_Success_MultipleItems() {
        // Given: 2个商品，价格分别为2.50和10.00，数量分别为2和3
        stubForSuccessfulOrder();
        Product product1 = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 100);
        Product product2 = buildProduct(PRODUCT_ID_2, "读者", new BigDecimal("10.00"), 50);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product1);
        when(productMapper.selectProductById(PRODUCT_ID_2)).thenReturn(product2);
        when(productMapper.adjustStockById(eq(PRODUCT_ID_1), eq(98), eq(0), any(LocalDateTime.class))).thenReturn(1);
        when(productMapper.adjustStockById(eq(PRODUCT_ID_2), eq(47), eq(0), any(LocalDateTime.class))).thenReturn(1);

        // totalAmount = 2.50*2 + 10.00*3 = 5.00 + 30.00 = 35.00
        // itemCount = 2（种类）
        // totalQuantity = 2+3 = 5
        SaleOrder returnOrder = buildMockReturnOrder("order-id", ORDER_NUMBER,
                new BigDecimal("35.00"), 2, 5);
        when(saleOrderMapper.selectOrderWithItems(anyString())).thenReturn(returnOrder);

        CreateSaleRequest request = buildSaleRequest(
                buildItemRequest(PRODUCT_ID_1, 2),
                buildItemRequest(PRODUCT_ID_2, 3)
        );

        // When
        SaleOrder result = saleService.createSale(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(result.getItemCount()).isEqualTo(2);
        assertThat(result.getTotalQuantity()).isEqualTo(5);

        // 验证insertOrder传入的订单对象（使用ArgumentCaptor）
        ArgumentCaptor<SaleOrder> orderCaptor = ArgumentCaptor.forClass(SaleOrder.class);
        verify(saleOrderMapper).insertOrder(orderCaptor.capture());
        SaleOrder capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(capturedOrder.getItemCount()).isEqualTo(2);
        assertThat(capturedOrder.getTotalQuantity()).isEqualTo(5);
        assertThat(capturedOrder.getOrderNumber()).isEqualTo(ORDER_NUMBER);
    }

    @Test
    @DisplayName("创建销售订单 - 验证SaleItem明细构建正确（快照字段、小计）")
    void testCreateSale_VerifySaleItemsBuilt() {
        // Given
        stubForSuccessfulOrder();
        Product product = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 100);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product);
        when(productMapper.adjustStockById(eq(PRODUCT_ID_1), eq(98), eq(0), any(LocalDateTime.class))).thenReturn(1);

        SaleOrder returnOrder = buildMockReturnOrder("order-id", ORDER_NUMBER,
                new BigDecimal("5.00"), 1, 2);
        when(saleOrderMapper.selectOrderWithItems(anyString())).thenReturn(returnOrder);

        CreateSaleRequest request = buildSaleRequest(buildItemRequest(PRODUCT_ID_1, 2));

        // When
        saleService.createSale(request);

        // Then: 验证insertItems传入的明细列表
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SaleItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(saleItemMapper).insertItems(itemsCaptor.capture());
        List<SaleItem> capturedItems = itemsCaptor.getValue();

        assertThat(capturedItems).hasSize(1);
        SaleItem item = capturedItems.get(0);
        assertThat(item.getProductId()).isEqualTo(PRODUCT_ID_1);
        assertThat(item.getProductName()).isEqualTo("人民日报");  // 快照
        assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal("2.50"));  // 快照
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(item.getId()).isNotBlank();
        assertThat(item.getOrderId()).isNotBlank();
    }

    // ==================== B2.6.2: 重复productId ====================

    @Test
    @DisplayName("创建销售订单 - 重复productId → BusinessException")
    void testCreateSale_DuplicateProductId() {
        // Given: 同一个productId出现两次
        CreateSaleRequest request = buildSaleRequest(
                buildItemRequest(PRODUCT_ID_1, 2),
                buildItemRequest(PRODUCT_ID_1, 3)  // 重复
        );

        // When & Then
        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Duplicate productId");

        // 确保没有任何mapper调用
        verifyNoInteractions(productMapper, saleOrderMapper, saleItemMapper);
    }

    // ==================== B2.6.2: 商品不存在 ====================

    @Test
    @DisplayName("创建销售订单 - 商品不存在 → NotFoundException")
    void testCreateSale_ProductNotFound() {
        // Given: 第一个商品存在，第二个不存在
        Product product1 = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 100);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product1);
        when(productMapper.selectProductById(PRODUCT_ID_2)).thenReturn(null);

        CreateSaleRequest request = buildSaleRequest(
                buildItemRequest(PRODUCT_ID_1, 1),
                buildItemRequest(PRODUCT_ID_2, 1)
        );

        // When & Then
        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found")
                .hasMessageContaining(PRODUCT_ID_2);

        // 验证: insertOrder和insertItems不会被调用
        verify(saleOrderMapper, never()).insertOrder(any());
        verify(saleItemMapper, never()).insertItems(any());
    }

    @Test
    @DisplayName("创建销售订单 - 第一个商品不存在 → NotFoundException")
    void testCreateSale_FirstProductNotFound() {
        // Given: 第一个商品就不存在
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(null);

        CreateSaleRequest request = buildSaleRequest(buildItemRequest(PRODUCT_ID_1, 1));

        // When & Then
        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found")
                .hasMessageContaining(PRODUCT_ID_1);
    }

    // ==================== B2.6.2: 库存不足 ====================

    @Test
    @DisplayName("创建销售订单 - 库存不足 → BusinessException")
    void testCreateSale_InsufficientStock() {
        // Given: 商品库存只有5，但要购买10
        Product product = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 5);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product);

        CreateSaleRequest request = buildSaleRequest(buildItemRequest(PRODUCT_ID_1, 10));

        // When & Then
        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("人民日报");

        // 库存不足时不执行扣减
        verify(productMapper, never()).adjustStockById(any(), any(), any(), any());
        verify(saleOrderMapper, never()).insertOrder(any());
    }

    @Test
    @DisplayName("创建销售订单 - 多商品中第二个库存不足 → BusinessException")
    void testCreateSale_SecondItemInsufficientStock() {
        // Given: 第一个商品库存充足，第二个不足
        Product product1 = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 100);
        Product product2 = buildProduct(PRODUCT_ID_2, "读者", new BigDecimal("10.00"), 2);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product1);
        when(productMapper.selectProductById(PRODUCT_ID_2)).thenReturn(product2);

        CreateSaleRequest request = buildSaleRequest(
                buildItemRequest(PRODUCT_ID_1, 1),
                buildItemRequest(PRODUCT_ID_2, 5)  // 库存只有2
        );

        // When & Then
        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("读者");
    }

    @Test
    @DisplayName("创建销售订单 - 库存等于购买数量（边界值，应成功）")
    void testCreateSale_StockExactlyEnough() {
        // Given: 库存刚好等于购买数量
        stubForSuccessfulOrder();
        Product product = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 3);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product);
        when(productMapper.adjustStockById(eq(PRODUCT_ID_1), eq(0), eq(0), any(LocalDateTime.class))).thenReturn(1);

        SaleOrder returnOrder = buildMockReturnOrder("order-id", ORDER_NUMBER,
                new BigDecimal("7.50"), 1, 3);
        when(saleOrderMapper.selectOrderWithItems(anyString())).thenReturn(returnOrder);

        CreateSaleRequest request = buildSaleRequest(buildItemRequest(PRODUCT_ID_1, 3));

        // When
        SaleOrder result = saleService.createSale(request);

        // Then: 库存刚好够，应该成功
        assertThat(result).isNotNull();
        verify(productMapper, times(1)).adjustStockById(eq(PRODUCT_ID_1), eq(0), eq(0), any());
    }

    // ==================== B2.6.2: 并发冲突（乐观锁）====================

    @Test
    @DisplayName("创建销售订单 - 乐观锁冲突（adjustStockById返回0）→ BusinessException")
    void testCreateSale_OptimisticLockConflict() {
        // Given: 商品查询成功，库存充足，但在扣减时发生并发冲突
        Product product = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 100);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product);
        // 模拟乐观锁冲突: adjustStockById返回0
        when(productMapper.adjustStockById(eq(PRODUCT_ID_1), eq(97), eq(0), any(LocalDateTime.class))).thenReturn(0);

        CreateSaleRequest request = buildSaleRequest(buildItemRequest(PRODUCT_ID_1, 3));

        // When & Then
        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("concurrent conflict")
                .hasMessageContaining("人民日报");

        // 验证: 发生冲突后不继续创建订单
        verify(saleOrderMapper, never()).insertOrder(any());
        verify(saleItemMapper, never()).insertItems(any());
    }

    @Test
    @DisplayName("创建销售订单 - 多商品中第二个发生乐观锁冲突 → BusinessException")
    void testCreateSale_OptimisticLockConflict_SecondItem() {
        // Given: 第一个商品扣减成功，第二个发生冲突
        Product product1 = buildProduct(PRODUCT_ID_1, "人民日报", new BigDecimal("2.50"), 100);
        Product product2 = buildProduct(PRODUCT_ID_2, "读者", new BigDecimal("10.00"), 50);
        when(productMapper.selectProductById(PRODUCT_ID_1)).thenReturn(product1);
        when(productMapper.selectProductById(PRODUCT_ID_2)).thenReturn(product2);
        // 第一个成功，第二个冲突
        when(productMapper.adjustStockById(eq(PRODUCT_ID_1), anyInt(), anyInt(), any())).thenReturn(1);
        when(productMapper.adjustStockById(eq(PRODUCT_ID_2), anyInt(), anyInt(), any())).thenReturn(0);

        CreateSaleRequest request = buildSaleRequest(
                buildItemRequest(PRODUCT_ID_1, 1),
                buildItemRequest(PRODUCT_ID_2, 1)
        );

        // When & Then
        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("concurrent conflict")
                .hasMessageContaining("读者");

        verify(saleOrderMapper, never()).insertOrder(any());
    }

    // ==================== B2.7.1: 获取订单详情 ====================

    @Test
    @DisplayName("获取订单详情 - 订单存在（含明细列表）")
    void testGetSaleById_Success() {
        // Given
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
        order.setOrderNumber(ORDER_NUMBER);
        order.setTotalAmount(new BigDecimal("5.00"));
        order.setItemCount(1);
        order.setTotalQuantity(2);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(List.of(item));

        when(saleOrderMapper.selectOrderWithItems("order-uuid")).thenReturn(order);

        // When
        SaleOrder result = saleService.getSaleById("order-uuid");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("order-uuid");
        assertThat(result.getOrderNumber()).isEqualTo(ORDER_NUMBER);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductName()).isEqualTo("人民日报");

        verify(saleOrderMapper, times(1)).selectOrderWithItems("order-uuid");
    }

    @Test
    @DisplayName("获取订单详情 - 订单不存在 → NotFoundException")
    void testGetSaleById_NotFound() {
        // Given
        when(saleOrderMapper.selectOrderWithItems("non-existent-id")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> saleService.getSaleById("non-existent-id"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Sale order not found")
                .hasMessageContaining("non-existent-id");

        verify(saleOrderMapper, times(1)).selectOrderWithItems("non-existent-id");
    }

    @Test
    @DisplayName("获取订单详情 - 验证ID正确传递给Mapper")
    void testGetSaleById_VerifyIdPassedToMapper() {
        // Given
        String orderId = "019512f3-a1b2-7000-8c3d-4e5f6a7b8c9d";
        SaleOrder order = new SaleOrder();
        order.setId(orderId);
        order.setOrderNumber(ORDER_NUMBER);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setItemCount(0);
        order.setTotalQuantity(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(List.of());

        when(saleOrderMapper.selectOrderWithItems(orderId)).thenReturn(order);

        // When
        saleService.getSaleById(orderId);

        // Then: 验证Mapper收到正确的ID
        verify(saleOrderMapper, times(1)).selectOrderWithItems(orderId);
        // 不应该调用其他Mapper方法
        verifyNoInteractions(productMapper, saleItemMapper);
    }

    // ==================== B2.8.1: 销售历史查询 ====================

    @Test
    @DisplayName("查询销售历史 - 无日期筛选，验证分页参数正确传递给Mapper")
    void testGetSaleHistory_NoDateFilter() {
        // Given: 默认参数（page=0, size=20），Service 内部转换为 MyBatis-Plus 的 page=1
        QuerySaleHistoryRequest request = new QuerySaleHistoryRequest();
        request.setPage(0);
        request.setSize(20);

        Page<SaleOrder> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0L);

        when(saleOrderMapper.selectOrderPage(any(Page.class), isNull(), isNull()))
                .thenReturn(mockPage);

        // When
        IPage<SaleOrder> result = saleService.getSaleHistory(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0L);

        // 验证分页参数：前端 page=0 → MyBatis-Plus page=1
        ArgumentCaptor<Page<SaleOrder>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(saleOrderMapper).selectOrderPage(pageCaptor.capture(), isNull(), isNull());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1L);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20L);
    }

    @Test
    @DisplayName("查询销售历史 - 带日期范围，验证 LocalDate 正确转换为 LocalDateTime")
    void testGetSaleHistory_WithDateRange() {
        // Given
        QuerySaleHistoryRequest request = new QuerySaleHistoryRequest();
        request.setPage(0);
        request.setSize(20);
        request.setStartDate(LocalDate.of(2026, 2, 1));
        request.setEndDate(LocalDate.of(2026, 2, 28));

        Page<SaleOrder> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0L);

        LocalDateTime expectedStart = LocalDate.of(2026, 2, 1).atStartOfDay();
        LocalDateTime expectedEnd = LocalDate.of(2026, 2, 28).atTime(LocalTime.MAX);

        when(saleOrderMapper.selectOrderPage(any(Page.class), eq(expectedStart), eq(expectedEnd)))
                .thenReturn(mockPage);

        // When
        IPage<SaleOrder> result = saleService.getSaleHistory(request);

        // Then
        assertThat(result).isNotNull();

        // 验证日期转换：startDate → 00:00:00，endDate → 23:59:59.999...
        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(saleOrderMapper).selectOrderPage(any(Page.class), startCaptor.capture(), endCaptor.capture());

        assertThat(startCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0, 0));
        assertThat(endCaptor.getValue().toLocalDate()).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(endCaptor.getValue().toLocalTime()).isEqualTo(LocalTime.MAX);
    }

    @Test
    @DisplayName("查询销售历史 - 只有 startDate，endDate 为 null")
    void testGetSaleHistory_OnlyStartDate() {
        // Given
        QuerySaleHistoryRequest request = new QuerySaleHistoryRequest();
        request.setPage(0);
        request.setSize(20);
        request.setStartDate(LocalDate.of(2026, 2, 1));
        request.setEndDate(null);

        Page<SaleOrder> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0L);

        when(saleOrderMapper.selectOrderPage(any(Page.class), any(LocalDateTime.class), isNull()))
                .thenReturn(mockPage);

        // When
        saleService.getSaleHistory(request);

        // Then: 验证 endDate 传 null
        verify(saleOrderMapper).selectOrderPage(any(Page.class), any(LocalDateTime.class), isNull());
    }

    @Test
    @DisplayName("查询销售历史 - 第2页（page=1）→ Mapper 收到 current=2")
    void testGetSaleHistory_SecondPage() {
        // Given
        QuerySaleHistoryRequest request = new QuerySaleHistoryRequest();
        request.setPage(1);
        request.setSize(10);

        Page<SaleOrder> mockPage = new Page<>(2, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(25L);
        mockPage.setPages(3L);

        when(saleOrderMapper.selectOrderPage(any(Page.class), isNull(), isNull()))
                .thenReturn(mockPage);

        // When
        IPage<SaleOrder> result = saleService.getSaleHistory(request);

        // Then: 前端 page=1 → MyBatis-Plus page=2
        ArgumentCaptor<Page<SaleOrder>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(saleOrderMapper).selectOrderPage(pageCaptor.capture(), isNull(), isNull());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2L);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(10L);
        assertThat(result.getTotal()).isEqualTo(25L);
        assertThat(result.getPages()).isEqualTo(3L);
    }

    @Test
    @DisplayName("查询销售历史 - 返回订单列表（验证数据正确映射）")
    void testGetSaleHistory_ReturnsOrders() {
        // Given
        SaleOrder order1 = new SaleOrder();
        order1.setId("order-1");
        order1.setOrderNumber("SO20260226143025001");
        order1.setTotalAmount(new BigDecimal("15.00"));
        order1.setItemCount(2);
        order1.setTotalQuantity(3);
        order1.setCreatedAt(LocalDateTime.of(2026, 2, 26, 14, 30, 25));

        SaleOrder order2 = new SaleOrder();
        order2.setId("order-2");
        order2.setOrderNumber("SO20260226120000001");
        order2.setTotalAmount(new BigDecimal("5.00"));
        order2.setItemCount(1);
        order2.setTotalQuantity(2);
        order2.setCreatedAt(LocalDateTime.of(2026, 2, 26, 12, 0, 0));

        QuerySaleHistoryRequest request = new QuerySaleHistoryRequest();
        request.setPage(0);
        request.setSize(20);

        Page<SaleOrder> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of(order1, order2));
        mockPage.setTotal(2L);
        mockPage.setPages(1L);

        when(saleOrderMapper.selectOrderPage(any(Page.class), isNull(), isNull()))
                .thenReturn(mockPage);

        // When
        IPage<SaleOrder> result = saleService.getSaleHistory(request);

        // Then
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getId()).isEqualTo("order-1");
        assertThat(result.getRecords().get(1).getId()).isEqualTo("order-2");
        assertThat(result.getTotal()).isEqualTo(2L);
    }
}
