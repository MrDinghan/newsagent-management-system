import { css } from "@emotion/css";
import { Descriptions, Modal, Spin, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { type FC } from "react";

import type { SaleItemVO } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import { useGetSaleById } from "@/api/endpoints/sale-management";
import { formatDateTime } from "@/utils/format";

const { Text } = Typography;

interface SaleDetailModalProps {
  open: boolean;
  saleId: string | null;
  onClose: () => void;
}

const columns: ColumnsType<SaleItemVO> = [
  {
    title: "Product",
    dataIndex: "productName",
    key: "productName",
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
];

const SaleDetailModal: FC<SaleDetailModalProps> = ({
  open,
  saleId,
  onClose,
}) => {
  const { data, isLoading } = useGetSaleById(saleId ?? "", {
    query: { enabled: !!saleId },
  });

  const order = data?.data;

  return (
    <Modal
      title={`Order ${order?.orderNumber ?? ""}`}
      open={open}
      onCancel={onClose}
      footer={null}
      width={640}
    >
      <Spin
        spinning={isLoading}
        className={css`
          width: 100%;
        `}
      >
        {order && (
          <>
            <Descriptions size="small" column={2}>
              <Descriptions.Item label="Date">
                {formatDateTime(order.createdAt)}
              </Descriptions.Item>
              <Descriptions.Item label="Items">
                {order.itemCount}
              </Descriptions.Item>
              <Descriptions.Item label="Total Qty">
                {order.totalQuantity}
              </Descriptions.Item>
              <Descriptions.Item label="Total Amount">
                <Text strong>&euro;{order.totalAmount?.toFixed(2)}</Text>
              </Descriptions.Item>
            </Descriptions>
            <Table
              columns={columns}
              dataSource={order.items}
              rowKey="id"
              pagination={false}
              size="small"
              style={{ marginTop: 16 }}
            />
          </>
        )}
      </Spin>
    </Modal>
  );
};

export default SaleDetailModal;
