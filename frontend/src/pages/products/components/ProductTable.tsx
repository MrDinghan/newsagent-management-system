import { DeleteOutlined, EditOutlined, StockOutlined } from "@ant-design/icons";
import { css } from "@emotion/css";
import { Button, Popconfirm, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import type { TablePaginationConfig } from "antd/es/table";
import { type FC } from "react";

import type { ProductVO } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import { productTypeColors, productTypeLabels } from "@/constants/product";
import { formatDateTime } from "@/utils/format";

interface ProductTableProps {
  dataSource?: ProductVO[];
  loading: boolean;
  pagination: TablePaginationConfig;
  onEdit: (product: ProductVO) => void;
  onAdjustStock: (product: ProductVO) => void;
  onDelete: (id: string) => void;
}

const ProductTable: FC<ProductTableProps> = ({
  dataSource,
  loading,
  pagination,
  onEdit,
  onAdjustStock,
  onDelete,
}) => {
  const columns: ColumnsType<ProductVO> = [
    {
      title: "Name",
      dataIndex: "name",
    },
    {
      title: "Type",
      dataIndex: "type",
      render: (type: string) => (
        <Tag color={productTypeColors[type]}>
          {productTypeLabels[type] ?? type}
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
        <span
          className={
            stock === 0
              ? css`
                  color: red;
                `
              : undefined
          }
        >
          {stock}
        </span>
      ),
    },
    {
      title: "Created At",
      dataIndex: "createdAt",
      render: (val: string) => formatDateTime(val),
    },
    {
      title: "Updated At",
      dataIndex: "updatedAt",
      render: (val: string) => formatDateTime(val),
    },
    {
      title: "Action",
      fixed: "end",
      render: (_, record) => (
        <>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => onEdit(record)}
            size="small"
          >
            Edit
          </Button>
          <Button
            type="link"
            icon={<StockOutlined />}
            onClick={() => onAdjustStock(record)}
            size="small"
          >
            Adjust Stock
          </Button>
          <Popconfirm
            title="Delete Product"
            description={`Are you sure you want to delete "${record.name}"?`}
            onConfirm={() => record.id && onDelete(record.id)}
            okText="Delete"
            okButtonProps={{ danger: true }}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              Delete
            </Button>
          </Popconfirm>
        </>
      ),
    },
  ];

  return (
    <Table
      rowKey="id"
      columns={columns}
      dataSource={dataSource}
      loading={loading}
      pagination={pagination}
      className={css`
        .ant-table-body {
          scrollbar-width: thin;
          overflow: auto !important;
        }
      `}
      scroll={{
        x: "max-content",
        y: "calc(100vh - 390px)",
      }}
    />
  );
};

export default ProductTable;
