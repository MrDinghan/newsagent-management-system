import { css } from "@emotion/css";
import { Button, Input, List, Pagination, Spin, Tag, Tooltip } from "antd";
import { type FC, useMemo, useState } from "react";

import type { ProductVO } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import { useQueryProducts } from "@/api/endpoints/product-management";
import { productTypeColors, productTypeLabels } from "@/constants/product";

import type { CartItem } from "../hooks/useCart";

interface ProductSearchProps {
  onAdd: (product: ProductVO) => void;
  cartItems: CartItem[];
}

const PAGE_SIZE = 10;

const ProductSearch: FC<ProductSearchProps> = ({ onAdd, cartItems }) => {
  const [page, setPage] = useState(1);
  const [searchText, setSearchText] = useState("");

  const { data, isLoading } = useQueryProducts({
    request: {},
    page: 0,
    size: 100,
  });

  const filtered = useMemo(() => {
    const allProducts = data?.data ?? [];
    if (!searchText) return allProducts;
    return allProducts.filter((p) =>
      p.name?.toLowerCase().includes(searchText.toLowerCase()),
    );
  }, [data?.data, searchText]);

  const paged = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return filtered.slice(start, start + PAGE_SIZE);
  }, [filtered, page]);

  const handleSearchChange = (value: string) => {
    setSearchText(value);
    setPage(1);
  };

  return (
    <div>
      <Input.Search
        placeholder="Search products by name..."
        allowClear
        value={searchText}
        onChange={(e) => handleSearchChange(e.target.value)}
        className={css`
          margin-bottom: 16px;
        `}
      />

      <Spin spinning={isLoading}>
        <List
          dataSource={paged}
          locale={{ emptyText: "No products found" }}
          renderItem={(product) => (
            <List.Item
              actions={[
                (() => {
                  const outOfStock =
                    (cartItems.find((i) => i.productId === product.id)
                      ?.quantity ?? 0) >= (product.stock ?? 0);
                  return (
                    <Tooltip
                      key="add"
                      title={outOfStock ? "Insufficient stock" : undefined}
                    >
                      <Button
                        type="primary"
                        size="small"
                        disabled={outOfStock}
                        onClick={() => onAdd(product)}
                      >
                        Add
                      </Button>
                    </Tooltip>
                  );
                })(),
              ]}
            >
              <List.Item.Meta
                title={product.name}
                description={
                  <div
                    className={css`
                      display: flex;
                      gap: 12px;
                      align-items: center;
                    `}
                  >
                    <Tag color={productTypeColors[product.type ?? ""]}>
                      {productTypeLabels[product.type ?? ""] ?? product.type}
                    </Tag>
                    <span>Price: &euro;{product.price?.toFixed(2)}</span>
                    <span>Stock: {product.stock}</span>
                  </div>
                }
              />
            </List.Item>
          )}
        />
      </Spin>

      <Pagination
        current={page}
        pageSize={PAGE_SIZE}
        total={filtered.length}
        onChange={setPage}
        showSizeChanger={false}
        className={css`
          margin-top: 16px;
          text-align: right;
        `}
      />
    </div>
  );
};

export default ProductSearch;
