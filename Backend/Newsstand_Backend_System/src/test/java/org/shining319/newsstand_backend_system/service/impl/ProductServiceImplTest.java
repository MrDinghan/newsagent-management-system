package org.shining319.newsstand_backend_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @Author: shining319
 * @Date: 2026/2/11
 * @Description: ProductServiceImpl单元测试（Mock方式）
 * 测试范围：业务逻辑、数据库操作、事务管理、异常处理
 * Mock策略：Mock ProductMapper，不依赖真实数据库
 **/
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Spy
    private ProductServiceImpl productService;

    private Product testProduct;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;
    private QueryProductRequest queryRequest;
    private AdjustStockRequest adjustStockRequest;

    @BeforeEach
    void setUp() {
        // 关键：将Mock的ProductMapper注入到父类ServiceImpl的baseMapper字段
        ReflectionTestUtils.setField(productService, "baseMapper", productMapper);

        // 准备测试数据
        testProduct = new Product();
        testProduct.setId("018d5e8a-3d8c-7000-8b2f-3e4a5b6c7d8e");
        testProduct.setName("人民日报");
        testProduct.setType("NEWSPAPER");
        testProduct.setPrice(new BigDecimal("2.50"));
        testProduct.setStock(100);
        testProduct.setVersion(0);
        testProduct.setDeleted(false);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateProductRequest();
        createRequest.setName("人民日报");
        createRequest.setType(ProductTypeEnum.NEWSPAPER);
        createRequest.setPrice(new BigDecimal("2.50"));
        createRequest.setStock(100);

        updateRequest = new UpdateProductRequest();
        updateRequest.setName("新华日报");
        updateRequest.setPrice(new BigDecimal("3.00"));

        queryRequest = new QueryProductRequest();
        queryRequest.setPage(0);
        queryRequest.setSize(20);

        adjustStockRequest = new AdjustStockRequest();
        adjustStockRequest.setQuantity(10);
    }

    // ==================== B1.1.4: 创建产品Service测试 ====================

    @Test
    @DisplayName("创建产品 - 成功")
    void testCreateProduct_Success() {
        // Given: Mock名称不存在
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(productMapper.insertProduct(any(Product.class))).thenReturn(1);

        // When
        Product result = productService.createProduct(createRequest);

        // Then
        assertNotNull(result);
        assertEquals("人民日报", result.getName());
        assertEquals("NEWSPAPER", result.getType());
        assertEquals(new BigDecimal("2.50"), result.getPrice());
        assertEquals(100, result.getStock());
        assertFalse(result.getDeleted());
        assertNotNull(result.getId()); // UUID已生成

        // Verify: 验证count被调用（检查名称重复）
        verify(productMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        // Verify: 验证insertProduct被调用
        verify(productMapper, times(1)).insertProduct(any(Product.class));
    }

    @Test
    @DisplayName("创建产品 - 名称重复（预检查）")
    void testCreateProduct_NameDuplicate_PreCheck() {
        // Given: Mock名称已存在
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            productService.createProduct(createRequest);
        });

        assertTrue(exception.getMessage().contains("Product name already exists"));
        assertTrue(exception.getMessage().contains("人民日报"));

        // Verify: 验证count被调用，但insertProduct未被调用
        verify(productMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(productMapper, never()).insertProduct(any(Product.class));
    }

    @Test
    @DisplayName("创建产品 - 竞态条件（DuplicateKeyException兜底）")
    void testCreateProduct_RaceCondition_DuplicateKeyException() {
        // Given: 预检查通过，但插入时抛DuplicateKeyException
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(productMapper.insertProduct(any(Product.class))).thenThrow(new DuplicateKeyException("Duplicate entry"));

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            productService.createProduct(createRequest);
        });

        assertTrue(exception.getMessage().contains("Product name already exists"));

        // Verify
        verify(productMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(productMapper, times(1)).insertProduct(any(Product.class));
    }

    @Test
    @DisplayName("创建产品 - 已删除产品名称可重用")
    void testCreateProduct_DeletedProductNameCanBeReused() {
        // Given: Mock count只检查deleted=false的产品（返回0表示未删除产品中无重名）
        when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(productMapper.insertProduct(any(Product.class))).thenReturn(1);

        // When
        Product result = productService.createProduct(createRequest);

        // Then
        assertNotNull(result);
        assertEquals("人民日报", result.getName());

        // Verify: 验证LambdaQueryWrapper包含deleted=false条件
        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(productMapper).selectCount(captor.capture());
        // 注意：由于LambdaQueryWrapper的条件无法直接验证，我们通过行为验证
        // 实际代码中wrapper.eq(Product::getDeleted, false)确保只检查未删除产品
    }

    // ==================== B1.2.3: 查询产品列表Service测试 ====================

    @Test
    @DisplayName("查询产品列表 - 分页查询成功")
    void testQueryProducts_Pagination() {
        // Given: Mock分页查询返回结果
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Product p = new Product();
            p.setId("test-id-" + i);
            p.setName("测试产品" + i);
            p.setType("MAGAZINE");
            p.setPrice(new BigDecimal("10.00"));
            p.setStock(10);
            p.setDeleted(false);
            products.add(p);
        }

        // 使用doAnswer来模拟selectPageWithTypeHandler的行为（修改传入的page对象）
        doAnswer(invocation -> {
            Page<Product> page = invocation.getArgument(0);
            page.setRecords(products);
            page.setTotal(26);
            page.setPages(3);
            return null;
        }).when(productMapper).selectPageWithTypeHandler(any(Page.class), eq(false), isNull());

        // When: 查询第一页（page=0, size=10）
        queryRequest.setPage(0);
        queryRequest.setSize(10);
        Page<Product> result = productService.queryProducts(queryRequest);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getRecords().size());
        assertEquals(26, result.getTotal());
        assertEquals(3, result.getPages());

        // Verify: 验证page参数从0转换为1
        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(productMapper).selectPageWithTypeHandler(pageCaptor.capture(), eq(false), isNull());
        assertEquals(1, pageCaptor.getValue().getCurrent()); // 0 + 1 = 1
        assertEquals(10, pageCaptor.getValue().getSize());
    }

    @Test
    @DisplayName("查询产品列表 - 类型筛选")
    void testQueryProducts_TypeFilter() {
        // Given: Mock返回MAGAZINE类型的产品
        Product magazine = new Product();
        magazine.setId("magazine-id");
        magazine.setName("时尚杂志");
        magazine.setType("MAGAZINE");
        magazine.setPrice(new BigDecimal("15.00"));
        magazine.setStock(20);

        // 使用doAnswer来模拟selectPageWithTypeHandler的行为
        doAnswer(invocation -> {
            Page<Product> page = invocation.getArgument(0);
            page.setRecords(List.of(magazine));
            page.setTotal(1);
            return null;
        }).when(productMapper).selectPageWithTypeHandler(any(Page.class), eq(false), eq("MAGAZINE"));

        // When: 查询MAGAZINE类型
        queryRequest.setType(ProductTypeEnum.MAGAZINE);
        Page<Product> result = productService.queryProducts(queryRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("MAGAZINE", result.getRecords().get(0).getType());

        // Verify: 验证type参数传递
        verify(productMapper).selectPageWithTypeHandler(any(Page.class), eq(false), eq("MAGAZINE"));
    }

    @Test
    @DisplayName("查询产品列表 - 排除已删除产品")
    void testQueryProducts_ExcludeDeleted() {
        // Given: 使用doAnswer模拟返回空结果（所有产品都已删除）
        doAnswer(invocation -> {
            Page<Product> page = invocation.getArgument(0);
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            return null;
        }).when(productMapper).selectPageWithTypeHandler(any(Page.class), eq(false), isNull());

        // When
        Page<Product> result = productService.queryProducts(queryRequest);

        // Then
        assertEquals(0, result.getTotal());

        // Verify: 验证deleted=false参数
        verify(productMapper).selectPageWithTypeHandler(any(Page.class), eq(false), isNull());
    }

    @Test
    @DisplayName("查询产品列表 - 空结果")
    void testQueryProducts_EmptyResult() {
        // Given: 使用doAnswer模拟返回空Page
        doAnswer(invocation -> {
            Page<Product> page = invocation.getArgument(0);
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            return null;
        }).when(productMapper).selectPageWithTypeHandler(any(Page.class), eq(false), isNull());

        // When
        Page<Product> result = productService.queryProducts(queryRequest);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    // ==================== B1.3.3: 更新产品Service测试 ====================

    @Test
    @DisplayName("更新产品 - 成功（全字段）")
    void testUpdateProduct_Success_AllFields() {
        // Given: Mock查询返回产品
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.updateProductById(eq(testProduct.getId()), any(Product.class))).thenReturn(1);

        // When: 更新全字段
        updateRequest.setName("人民日报（新版）");
        updateRequest.setType(ProductTypeEnum.MAGAZINE);
        updateRequest.setPrice(new BigDecimal("3.50"));
        Product result = productService.updateProduct(testProduct.getId(), updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("人民日报（新版）", result.getName());
        assertEquals("MAGAZINE", result.getType());
        assertEquals(new BigDecimal("3.50"), result.getPrice());
        assertEquals(100, result.getStock()); // stock不变

        // Verify
        verify(productMapper).selectProductById(testProduct.getId());
        verify(productMapper).updateProductById(eq(testProduct.getId()), any(Product.class));
    }

    @Test
    @DisplayName("更新产品 - 部分字段更新")
    void testUpdateProduct_PartialFields() {
        // Given: Mock查询返回产品
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.updateProductById(eq(testProduct.getId()), any(Product.class))).thenReturn(1);

        // When: 只更新name字段（type和price为null）
        UpdateProductRequest partialRequest = new UpdateProductRequest();
        partialRequest.setName("人民日报（晚报）");
        Product result = productService.updateProduct(testProduct.getId(), partialRequest);

        // Then: 验证只有name被更新，其他字段保持原值
        assertEquals("人民日报（晚报）", result.getName());
        assertEquals("NEWSPAPER", result.getType()); // 保持原值
        assertEquals(new BigDecimal("2.50"), result.getPrice()); // 保持原值

        // Verify: 验证updateProductById被调用，XML的<if>会跳过null字段
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateProductById(eq(testProduct.getId()), productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        assertEquals("人民日报（晚报）", capturedProduct.getName());
        // type和price在Product对象中保持原值（XML的<if>会检查null）
    }

    @Test
    @DisplayName("更新产品 - 产品不存在")
    void testUpdateProduct_NotFound() {
        // Given: Mock查询返回null
        String nonExistentId = "non-existent-id";
        when(productMapper.selectProductById(nonExistentId)).thenReturn(null);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productService.updateProduct(nonExistentId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("Product not found"));
        assertTrue(exception.getMessage().contains(nonExistentId));

        // Verify: 验证selectProductById被调用，但updateProductById未被调用
        verify(productMapper).selectProductById(nonExistentId);
        verify(productMapper, never()).updateProductById(anyString(), any(Product.class));
    }

    @Test
    @DisplayName("更新产品 - updatedAt自动更新")
    void testUpdateProduct_UpdatedAtChanged() {
        // Given: Mock查询返回产品
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.updateProductById(eq(testProduct.getId()), any(Product.class))).thenReturn(1);

        // When
        Product result = productService.updateProduct(testProduct.getId(), updateRequest);

        // Then: 验证updatedAt已设置
        assertNotNull(result.getUpdatedAt());

        // Verify: 验证updateProductById被调用时，Product的updatedAt已设置
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateProductById(eq(testProduct.getId()), productCaptor.capture());
        assertNotNull(productCaptor.getValue().getUpdatedAt());
    }

    // ==================== B1.4.3: 调整库存Service测试 ====================

    @Test
    @DisplayName("调整库存 - 增加库存")
    void testAdjustStock_IncreaseStock() {
        // Given: Mock查询返回产品，Mock更新成功
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.adjustStockById(eq(testProduct.getId()), eq(150), eq(0), any(LocalDateTime.class))).thenReturn(1);

        // Mock第二次查询返回更新后的产品
        Product updatedProduct = new Product();
        updatedProduct.setId(testProduct.getId());
        updatedProduct.setName(testProduct.getName());
        updatedProduct.setType(testProduct.getType());
        updatedProduct.setPrice(testProduct.getPrice());
        updatedProduct.setStock(150);
        updatedProduct.setVersion(1);
        updatedProduct.setDeleted(false);
        updatedProduct.setCreatedAt(testProduct.getCreatedAt());
        updatedProduct.setUpdatedAt(LocalDateTime.now());
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct, updatedProduct);

        // When: 增加库存50
        adjustStockRequest.setQuantity(50);
        Product result = productService.adjustStock(testProduct.getId(), adjustStockRequest);

        // Then
        assertEquals(150, result.getStock()); // 100 + 50
        assertEquals(1, result.getVersion());

        // Verify: 验证selectProductById被调用2次（查询+重新查询）
        verify(productMapper, times(2)).selectProductById(testProduct.getId());
        verify(productMapper).adjustStockById(eq(testProduct.getId()), eq(150), eq(0), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("调整库存 - 减少库存")
    void testAdjustStock_DecreaseStock() {
        // Given
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.adjustStockById(eq(testProduct.getId()), eq(70), eq(0), any(LocalDateTime.class))).thenReturn(1);

        Product updatedProduct = new Product();
        updatedProduct.setId(testProduct.getId());
        updatedProduct.setStock(70);
        updatedProduct.setVersion(1);
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct, updatedProduct);

        // When: 减少库存30
        adjustStockRequest.setQuantity(-30);
        Product result = productService.adjustStock(testProduct.getId(), adjustStockRequest);

        // Then
        assertEquals(70, result.getStock()); // 100 - 30
    }

    @Test
    @DisplayName("调整库存 - 库存不足")
    void testAdjustStock_InsufficientStock() {
        // Given: Mock查询返回产品
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);

        // When & Then: 尝试减少超过当前库存的数量
        adjustStockRequest.setQuantity(-150); // 当前库存100，减少150会导致负数
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.adjustStock(testProduct.getId(), adjustStockRequest);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        assertTrue(exception.getMessage().contains("100")); // 当前库存
        assertTrue(exception.getMessage().contains("-150")); // 调整量

        // Verify: 验证selectProductById被调用，但adjustStockById未被调用
        verify(productMapper).selectProductById(testProduct.getId());
        verify(productMapper, never()).adjustStockById(anyString(), anyInt(), anyInt(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("调整库存 - 产品不存在")
    void testAdjustStock_NotFound() {
        // Given: Mock查询返回null
        String nonExistentId = "non-existent-id";
        when(productMapper.selectProductById(nonExistentId)).thenReturn(null);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productService.adjustStock(nonExistentId, adjustStockRequest);
        });

        assertTrue(exception.getMessage().contains("Product not found"));

        // Verify
        verify(productMapper).selectProductById(nonExistentId);
        verify(productMapper, never()).adjustStockById(anyString(), anyInt(), anyInt(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("调整库存 - 乐观锁并发冲突")
    void testAdjustStock_OptimisticLockConflict() {
        // Given: Mock查询返回产品，但更新时返回0（乐观锁冲突）
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.adjustStockById(eq(testProduct.getId()), eq(110), eq(0), any(LocalDateTime.class))).thenReturn(0);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.adjustStock(testProduct.getId(), adjustStockRequest);
        });

        assertTrue(exception.getMessage().contains("concurrent conflict"));

        // Verify: 验证adjustStockById被调用但返回0
        verify(productMapper).adjustStockById(eq(testProduct.getId()), eq(110), eq(0), any(LocalDateTime.class));
        // 由于更新失败，不会执行第二次selectProductById
        verify(productMapper, times(1)).selectProductById(testProduct.getId());
    }

    @Test
    @DisplayName("调整库存 - version递增")
    void testAdjustStock_VersionIncremented() {
        // Given
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.adjustStockById(eq(testProduct.getId()), eq(110), eq(0), any(LocalDateTime.class))).thenReturn(1);

        Product updatedProduct = new Product();
        updatedProduct.setId(testProduct.getId());
        updatedProduct.setStock(110);
        updatedProduct.setVersion(1); // version递增
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct, updatedProduct);

        // When
        Product result = productService.adjustStock(testProduct.getId(), adjustStockRequest);

        // Then: 验证version已递增
        assertEquals(1, result.getVersion());
    }

    @Test
    @DisplayName("调整库存 - 返回最新数据")
    void testAdjustStock_ReturnsLatestData() {
        // Given
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.adjustStockById(eq(testProduct.getId()), eq(120), eq(0), any(LocalDateTime.class))).thenReturn(1);

        Product updatedProduct = new Product();
        updatedProduct.setId(testProduct.getId());
        updatedProduct.setName("人民日报");
        updatedProduct.setStock(120);
        updatedProduct.setVersion(1);
        updatedProduct.setUpdatedAt(LocalDateTime.now());
        when(productMapper.selectProductById(testProduct.getId())).thenReturn(testProduct, updatedProduct);

        // When
        adjustStockRequest.setQuantity(20);
        Product result = productService.adjustStock(testProduct.getId(), adjustStockRequest);

        // Then: 验证返回的是重新查询的数据
        assertEquals(120, result.getStock());
        assertEquals(1, result.getVersion());
        assertNotNull(result.getUpdatedAt());

        // Verify: 验证selectProductById被调用2次
        verify(productMapper, times(2)).selectProductById(testProduct.getId());
    }

    // ==================== B1.5.2: 删除产品Service测试 ====================

    @Test
    @DisplayName("删除产品 - 成功")
    void testDeleteProduct_Success() {
        // Given: 产品存在且未删除
        when(productMapper.selectProductByIdIncludeDeleted(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.deleteProductById(testProduct.getId())).thenReturn(1);

        // When
        productService.deleteProduct(testProduct.getId());

        // Then: 验证deleteProductById被调用
        verify(productMapper, times(1)).deleteProductById(testProduct.getId());
        verify(productMapper, times(1)).selectProductByIdIncludeDeleted(testProduct.getId());
    }

    @Test
    @DisplayName("删除产品 - 产品不存在")
    void testDeleteProduct_NotFound() {
        // Given: 产品不存在
        when(productMapper.selectProductByIdIncludeDeleted(testProduct.getId())).thenReturn(null);

        // When & Then: 抛出NotFoundException
        assertThrows(NotFoundException.class, () -> productService.deleteProduct(testProduct.getId()));

        // Verify: deleteProductById不会被调用
        verify(productMapper, never()).deleteProductById(any());
    }

    @Test
    @DisplayName("删除产品 - 幂等性（重复删除已删除产品）")
    void testDeleteProduct_Idempotent() {
        // Given: 产品已删除
        Product deletedProduct = new Product();
        deletedProduct.setId(testProduct.getId());
        deletedProduct.setName("人民日报");
        deletedProduct.setDeleted(true);
        when(productMapper.selectProductByIdIncludeDeleted(testProduct.getId())).thenReturn(deletedProduct);

        // When: 重复删除
        productService.deleteProduct(testProduct.getId());

        // Then: deleteProductById不会被调用（提前返回）
        verify(productMapper, never()).deleteProductById(any());
    }

    @Test
    @DisplayName("删除产品 - 验证日志记录")
    void testDeleteProduct_Logging() {
        // Given: 产品存在且未删除
        when(productMapper.selectProductByIdIncludeDeleted(testProduct.getId())).thenReturn(testProduct);
        when(productMapper.deleteProductById(testProduct.getId())).thenReturn(1);

        // When
        productService.deleteProduct(testProduct.getId());

        // Then: 验证方法调用（日志记录无法直接测试，但可以验证行为）
        verify(productMapper, times(1)).deleteProductById(testProduct.getId());
    }
}


