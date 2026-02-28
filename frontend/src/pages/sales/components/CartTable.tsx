import { Empty, Table } from "antd";
import type { ColumnsType } from "antd/es/table";
import { type FC, useMemo } from "react";

import type { CartItem } from "../hooks/useCart";

interface CartTableProps {
  items: CartItem[];
}

const CartTable: FC<CartTableProps> = ({ items }) => {
  const columns: ColumnsType<CartItem> = useMemo(
    () => [
      {
        title: "Name",
        dataIndex: "name",
        key: "name",
      },
      {
        title: "Unit Price",
        dataIndex: "unitPrice",
        key: "unitPrice",
        render: (price: number) => `€${price.toFixed(2)}`,
      },
      {
        title: "Quantity",
        dataIndex: "quantity",
        key: "quantity",
      },
      {
        title: "Subtotal",
        dataIndex: "subtotal",
        key: "subtotal",
        render: (subtotal: number) => `€${subtotal.toFixed(2)}`,
      },
      {
        title: "Action",
        key: "action",
        render: () => <span>-</span>,
      },
    ],
    [],
  );

  if (items.length === 0) {
    return <Empty description="Cart is empty" />;
  }

  return (
    <Table
      columns={columns}
      dataSource={items}
      rowKey="productId"
      pagination={false}
      size="small"
    />
  );
};

export default CartTable;
