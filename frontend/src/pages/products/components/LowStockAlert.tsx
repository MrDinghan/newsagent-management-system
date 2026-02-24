import { WarningOutlined } from "@ant-design/icons";
import { css } from "@emotion/css";
import { Alert, Badge, Flex, Tag } from "antd";
import { type FC, useState } from "react";

import { useGetLowStockProducts } from "@/api/endpoints/product-management";
import { LOW_STOCK_THRESHOLD } from "@/constants/product";

import ProductDetailModal from "./ProductDetailModal";

const LowStockAlert: FC = () => {
  const [selectedProductId, setSelectedProductId] = useState<string | null>(
    null,
  );

  const { data } = useGetLowStockProducts({
    request: {},
    threshold: LOW_STOCK_THRESHOLD,
    size: 100,
  });

  const products = data?.data ?? [];

  if (products.length === 0) {
    return (
      <Alert
        type="success"
        message="All products are well stocked"
        showIcon
        className={css`
          margin-bottom: 16px;
        `}
      />
    );
  }

  return (
    <>
      <Alert
        type="warning"
        showIcon
        icon={<WarningOutlined />}
        className={css`
          margin-bottom: 16px;
        `}
        title={`${products.length} product${products.length > 1 ? "s" : ""} with low stock (≤ ${LOW_STOCK_THRESHOLD})`}
        description={
          <Flex
            wrap
            gap={8}
            className={css`
              margin-top: 4px;
            `}
          >
            {products.map((p) => (
              <Badge
                key={p.id}
                count={p.stock}
                overflowCount={999}
                color={p.stock === 0 ? "#f5222d" : "#faad14"}
                size="small"
              >
                <Tag
                  onClick={() => setSelectedProductId(p.id ?? null)}
                  className={css`
                    cursor: pointer;
                    border: 1px solid #d9d9d9;
                    background: #fff;
                    transition: all 0.2s;
                    &:hover {
                      border-color: #faad14;
                      background: #fffbe6;
                    }
                  `}
                >
                  {p.name}
                </Tag>
              </Badge>
            ))}
          </Flex>
        }
      />
      <ProductDetailModal
        open={!!selectedProductId}
        productId={selectedProductId}
        onClose={() => setSelectedProductId(null)}
      />
    </>
  );
};

export default LowStockAlert;
