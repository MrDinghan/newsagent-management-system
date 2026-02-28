import { css } from "@emotion/css";
import { useQueryClient } from "@tanstack/react-query";
import { Button, message, Space, Typography } from "antd";
import { type FC } from "react";

import type { CreateSaleRequest } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import { getQueryProductsQueryKey } from "@/api/endpoints/product-management";
import { useCreateSale } from "@/api/endpoints/sale-management";

const { Text } = Typography;

interface SaleSummaryProps {
  itemCount: number;
  totalQuantity: number;
  totalAmount: number;
  toCreateSaleRequest: () => CreateSaleRequest;
  onOrderSuccess: () => void;
}

const SaleSummary: FC<SaleSummaryProps> = ({
  itemCount,
  totalQuantity,
  totalAmount,
  toCreateSaleRequest,
  onOrderSuccess,
}) => {
  const queryClient = useQueryClient();

  const { mutate: createSale, isPending } = useCreateSale({
    mutation: {
      onSuccess: (data) => {
        message.success(`Order ${data.data?.orderNumber} placed successfully!`);
        queryClient.invalidateQueries({
          queryKey: getQueryProductsQueryKey(),
        });
        onOrderSuccess();
      },
    },
  });

  const handlePlaceOrder = () => {
    createSale({ data: toCreateSaleRequest() });
  };

  return (
    <div
      className={css`
        display: flex;
        align-items: center;
        justify-content: space-between;
      `}
    >
      <Space size="large">
        <Text>
          Items: <Text strong>{itemCount}</Text>
        </Text>
        <Text>
          Total Qty: <Text strong>{totalQuantity}</Text>
        </Text>
        <Text>
          Total: <Text strong>&euro;{totalAmount.toFixed(2)}</Text>
        </Text>
      </Space>
      <Button
        type="primary"
        disabled={itemCount === 0}
        loading={isPending}
        onClick={handlePlaceOrder}
      >
        Place Order
      </Button>
    </div>
  );
};

export default SaleSummary;
