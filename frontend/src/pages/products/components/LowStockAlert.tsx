import { WarningOutlined } from "@ant-design/icons";
import { Alert, Badge, Flex } from "antd";
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
        style={{ marginBottom: 16 }}
      />
    );
  }

  return (
    <>
      <Alert
        type="warning"
        showIcon
        icon={<WarningOutlined />}
        style={{ marginBottom: 16 }}
        message={`${products.length} product${products.length > 1 ? "s" : ""} with low stock (≤ ${LOW_STOCK_THRESHOLD})`}
        description={
          <Flex wrap gap={8} style={{ marginTop: 4 }}>
            {products.map((p) => (
              <Badge
                key={p.id}
                count={p.stock}
                overflowCount={999}
                color={p.stock === 0 ? "#f5222d" : "#faad14"}
                size="small"
              >
                <div
                  onClick={() => setSelectedProductId(p.id ?? null)}
                  style={{
                    padding: "4px 12px",
                    background: "#fff",
                    border: "1px solid #d9d9d9",
                    borderRadius: 6,
                    cursor: "pointer",
                    fontSize: 13,
                    transition: "all 0.2s",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = "#faad14";
                    e.currentTarget.style.background = "#fffbe6";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = "#d9d9d9";
                    e.currentTarget.style.background = "#fff";
                  }}
                >
                  {p.name}
                </div>
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
