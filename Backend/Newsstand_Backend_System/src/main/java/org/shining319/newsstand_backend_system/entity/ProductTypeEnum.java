package org.shining319.newsstand_backend_system.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Author: shining319
 * @Date: 2026/2/9
 * @Description: 产品类型枚举
 **/
@Schema(description = "产品类型枚举")
public enum ProductTypeEnum {

    @Schema(description = "报纸")
    NEWSPAPER,

    @Schema(description = "杂志")
    MAGAZINE
}
