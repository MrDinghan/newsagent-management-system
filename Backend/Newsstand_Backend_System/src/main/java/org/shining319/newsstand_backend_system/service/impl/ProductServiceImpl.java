package org.shining319.newsstand_backend_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.shining319.newsstand_backend_system.dao.ProductMapper;
import org.shining319.newsstand_backend_system.dto.request.CreateProductRequest;
import org.shining319.newsstand_backend_system.dto.request.QueryProductRequest;
import org.shining319.newsstand_backend_system.dto.request.UpdateProductRequest;
import org.shining319.newsstand_backend_system.entity.Product;
import org.shining319.newsstand_backend_system.exception.ConflictException;
import org.shining319.newsstand_backend_system.exception.NotFoundException;
import org.shining319.newsstand_backend_system.service.IProductService;
import org.shining319.newsstand_backend_system.util.UuidUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Author: shining319
 * @Date: 2026/2/9
 * @Description: 产品服务实现类
 * 继承MyBatis-Plus的ServiceImpl，提供标准CRUD方法
 **/
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    /**
     * 创建产品
     *
     * @param request 创建产品请求
     * @return 创建的产品实体
     * @throws ConflictException 当产品名称已存在时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product createProduct(CreateProductRequest request) {
        // 预检查：产品名称是否已存在（只检查未删除的产品）
        if (isNameExists(request.getName())) {
            throw new ConflictException("产品名称已存在: " + request.getName());
        }

        // 构建产品实体
        Product product = new Product();
        product.setId(UuidUtil.generateUuidV7());
        product.setName(request.getName());
        product.setType(request.getType().name());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDeleted(false);
        // createdAt和updatedAt由数据库默认值处理

        try {
            // 保存到数据库（使用自定义的 insertProduct 方法，确保 TypeHandler 生效）
            baseMapper.insertProduct(product);
        } catch (DuplicateKeyException e) {
            // 兜底：处理竞态窗口中的数据库唯一约束冲突
            log.warn("产品名称冲突（竞态条件触发）: name={}", request.getName());
            throw new ConflictException("产品名称已存在: " + request.getName());
        }

        log.info("产品创建成功: id={}, name={}", product.getId(), product.getName());
        return product;
    }

    /**
     * 分页查询产品列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Product> queryProducts(QueryProductRequest request) {
        // 1. 构建分页对象（MyBatis-Plus page从1开始，前端从0开始）
        Page<Product> page = new Page<>(request.getPage() + 1, request.getSize());

        // 2. 使用自定义查询方法（确保 TypeHandler 生效）
        String type = request.getType() != null ? request.getType().name() : null;
        baseMapper.selectPageWithTypeHandler(page, false, type);

        // 3. 返回分页结果
        return page;
    }

    /**
     * 更新产品信息（不包含库存）
     * 使用自定义 XML 方法确保 UUID TypeHandler 生效，不触发乐观锁
     *
     * @param id      产品ID
     * @param request 更新请求（name, type, price）
     * @return 更新后的产品实体
     * @throws NotFoundException 当产品不存在时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product updateProduct(String id, UpdateProductRequest request) {
        // 1. 查找产品（自定义 XML，确保 UUID TypeHandler 生效）
        Product product = baseMapper.selectProductById(id);
        if (product == null) {
            throw new NotFoundException("产品不存在: id=" + id);
        }

        // 2. 将要更新的字段写入 product（null 字段保持原值，XML 的 <if> 会跳过）
        LocalDateTime now = LocalDateTime.now().withNano(0); // DATETIME 精度到秒
        if (request.getName() != null) product.setName(request.getName());
        if (request.getType() != null) product.setType(request.getType().name());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        product.setUpdatedAt(now);

        // 3. 执行更新（自定义 XML，WHERE 显式指定 TypeHandler，不含 version 字段不触发乐观锁）
        baseMapper.updateProductById(id, product);

        log.info("产品更新成功: id={}, name={}", product.getId(), product.getName());
        return product;
    }

    /**
     * 检查产品名称是否已存在（只检查未删除的产品）
     *
     * @param name 产品名称
     * @return true-已存在，false-不存在
     */
    private boolean isNameExists(String name) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getName, name)
                .eq(Product::getDeleted, false);
        // 使用MyBatis-Plus的count方法
        return this.count(wrapper) > 0;
    }
}