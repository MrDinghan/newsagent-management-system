import { EditOutlined, PlusOutlined, StockOutlined } from "@ant-design/icons";
import { Button, Select, Space, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { type FC, useState } from "react";

import type {
  ProductVO,
  QueryProductsType,
} from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import { useQueryProducts } from "@/api/endpoints/product-management";

import AdjustStockModal from "./components/AdjustStockModal";
import CreateProductModal from "./components/CreateProductModal";
import EditProductModal from "./components/EditProductModal";

const typeOptions = [
  { label: "All", value: "" },
  { label: "Newspaper", value: "NEWSPAPER" },
  { label: "Magazine", value: "MAGAZINE" },
];

const ProductsPage: FC = () => {
  const [createOpen, setCreateOpen] = useState(false);
  const [editProduct, setEditProduct] = useState<ProductVO | null>(null);
  const [stockProduct, setStockProduct] = useState<ProductVO | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [typeFilter, setTypeFilter] = useState<QueryProductsType | "">("");

  const columns: ColumnsType<ProductVO> = [
    {
      title: "Name",
      dataIndex: "name",
    },
    {
      title: "Type",
      dataIndex: "type",
      render: (type: string) => (
        <Tag color={type === "NEWSPAPER" ? "blue" : "green"}>
          {type === "NEWSPAPER" ? "Newspaper" : "Magazine"}
        </Tag>
      ),
    },
    {
      title: "Price",
      dataIndex: "price",
      render: (price: number) => `€${price.toFixed(2)}`,
    },
    {
      title: "Stock",
      dataIndex: "stock",
      render: (stock: number) => (
        <span style={{ color: stock === 0 ? "red" : undefined }}>{stock}</span>
      ),
    },
    {
      title: "Action",
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => setEditProduct(record)}
          >
            Edit
          </Button>
          <Button
            type="link"
            icon={<StockOutlined />}
            onClick={() => setStockProduct(record)}
          >
            Adjust Stock
          </Button>
        </Space>
      ),
    },
  ];

  const { data, isLoading } = useQueryProducts({
    request: {},
    page,
    size,
    ...(typeFilter ? { type: typeFilter as QueryProductsType } : {}),
  });

  return (
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

      <Table
        rowKey="id"
        columns={columns}
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
