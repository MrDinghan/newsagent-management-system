import { PlusOutlined } from "@ant-design/icons";
import { useQueryClient } from "@tanstack/react-query";
import { App, Button, Select, Space, Tabs } from "antd";
import { type FC, useState } from "react";

import type {
  ProductVO,
  QueryProductsType,
} from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import {
  getGetLowStockProductsQueryKey,
  getQueryProductsQueryKey,
  useDeleteProduct,
  useQueryProducts,
} from "@/api/endpoints/product-management";
import { productTypeLabels } from "@/constants/product";

import AdjustStockModal from "./components/AdjustStockModal";
import CreateProductModal from "./components/CreateProductModal";
import EditProductModal from "./components/EditProductModal";
import LowStockAlert from "./components/LowStockAlert";
import LowStockTab from "./components/LowStockTab";
import ProductTable from "./components/ProductTable";

const typeOptions = [
  { label: "All", value: "" },
  ...Object.entries(productTypeLabels).map(([value, label]) => ({
    label,
    value,
  })),
];

const ProductsPage: FC = () => {
  const [createOpen, setCreateOpen] = useState(false);
  const [editProduct, setEditProduct] = useState<ProductVO | null>(null);
  const [stockProduct, setStockProduct] = useState<ProductVO | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [typeFilter, setTypeFilter] = useState<QueryProductsType | "">("");
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const deleteMutation = useDeleteProduct();

  const handleDelete = (id: string) => {
    deleteMutation.mutate(
      { id },
      {
        onSuccess: () => {
          message.success("Product deleted successfully");
          queryClient.invalidateQueries({
            queryKey: getQueryProductsQueryKey(),
          });
          queryClient.invalidateQueries({
            queryKey: getGetLowStockProductsQueryKey(),
          });
        },
      },
    );
  };

  const { data, isLoading } = useQueryProducts({
    request: {},
    page,
    size,
    ...(typeFilter ? { type: typeFilter as QueryProductsType } : {}),
  });

  return (
    <>
      <LowStockAlert />

      <Tabs
        defaultActiveKey="all"
        items={[
          {
            key: "all",
            label: "All Products",
            children: (
              <>
                <Space
                  style={{
                    marginBottom: 16,
                    display: "flex",
                    justifyContent: "space-between",
                  }}
                >
                  <Select
                    value={typeFilter}
                    onChange={(v) => {
                      setTypeFilter(v);
                      setPage(0);
                    }}
                    options={typeOptions}
                    style={{ width: 150 }}
                  />
                  <Button
                    type="primary"
                    icon={<PlusOutlined />}
                    onClick={() => setCreateOpen(true)}
                  >
                    Add Product
                  </Button>
                </Space>
                <ProductTable
                  dataSource={data?.data}
                  loading={isLoading}
                  pagination={{
                    current: page + 1,
                    pageSize: size,
                    total: data?.total,
                    showSizeChanger: true,
                    pageSizeOptions: ["10", "20", "30", "50"],
                    onChange: (p, s) => {
                      setPage(p - 1);
                      setSize(s);
                    },
                    showTotal: (total) => `Total ${total} products`,
                  }}
                  onEdit={setEditProduct}
                  onAdjustStock={setStockProduct}
                  onDelete={handleDelete}
                />
              </>
            ),
          },
          {
            key: "lowStock",
            label: "Low Stock",
            children: (
              <LowStockTab
                onEdit={setEditProduct}
                onAdjustStock={setStockProduct}
                onDelete={handleDelete}
              />
            ),
          },
        ]}
      />

      <CreateProductModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
      />

      <EditProductModal
        open={!!editProduct}
        product={editProduct}
        onClose={() => setEditProduct(null)}
      />

      <AdjustStockModal
        open={!!stockProduct}
        product={stockProduct}
        onClose={() => setStockProduct(null)}
      />
    </>
  );
};

export default ProductsPage;
