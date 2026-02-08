package org.shining319.newsstand_backend_system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shining319.newsstand_backend_system.entity.Product;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 产品Mapper接口
 **/
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // 继承 BaseMapper，自动获得 CRUD 方法
    // 特殊查询方法在需要时添加
}
