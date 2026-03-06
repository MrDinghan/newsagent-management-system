import { css } from "@emotion/css";
import { Button, Empty, InputNumber, Table } from "antd";
import type { ColumnsType } from "antd/es/table";
import { type FC, useMemo } from "react";

import type { CartItem } from "../hooks/useCart";

interface CartTableProps {
  items: CartItem[];
  onUpdateQuantity: (productId: string, quantity: number) => void;
  onRemove: (productId: string) => void;
}

const CartTable: FC<CartTableProps> = ({
  items,
  onUpdateQuantity,
  onRemove,
}) => {
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
        render: (_: number, record: CartItem) => (
          <InputNumber
            min={1}
            max={record.availableStock}
            value={record.quantity}
            size="small"
            onChange={(val) => {
              if (val != null) onUpdateQuantity(record.productId, val);
            }}
          />
        ),
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
        render: (_: unknown, record: CartItem) => (
          <Button
            type="link"
            danger
            size="small"
            onClick={() => onRemove(record.productId)}
          >
            Remove
          </Button>
        ),
      },
    ],
    [onUpdateQuantity, onRemove],
  );

  if (items.length === 0) {
    return (
      <div
        className={css`
          display: flex;
          align-items: center;
          justify-content: center;
          flex: 1;
          min-height: 0;
        `}
      >
        <Empty description="Cart is empty" />
      </div>
    );
  }

  return (
    <div
      className={css`
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        /* scrollbar-width: thin; */
      `}
    >
      <Table
        columns={columns}
        dataSource={items}
        rowKey="productId"
        pagination={false}
        size="small"
      />
    </div>
  );
};

export default CartTable;
