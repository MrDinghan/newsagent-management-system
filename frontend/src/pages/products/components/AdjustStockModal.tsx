import { useQueryClient } from "@tanstack/react-query";
import { App, Form, InputNumber, Modal, Typography } from "antd";
import { type FC, useEffect, useState } from "react";

import type { ProductVO } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import {
  getGetLowStockProductsQueryKey,
  getQueryProductsQueryKey,
  useAdjustStock,
} from "@/api/endpoints/product-management";

interface AdjustStockModalProps {
  open: boolean;
  product: ProductVO | null;
  onClose: () => void;
}

const AdjustStockModal: FC<AdjustStockModalProps> = ({
  open,
  product,
  onClose,
}) => {
  const [form] = Form.useForm();
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const adjustMutation = useAdjustStock();
  const [preview, setPreview] = useState<number | null>(null);

  const currentStock = product?.stock ?? 0;

  useEffect(() => {
    if (open) {
      form.resetFields();
      setPreview(null);
    }
  }, [open, form]);

  const handleQuantityChange = (value: number | null) => {
    if (value !== null) {
      setPreview(currentStock + value);
    } else {
      setPreview(null);
    }
  };

  const handleOk = async () => {
    const values = await form.validateFields();
    if (!product?.id) return;
    adjustMutation.mutate(
      { id: product.id, data: { quantity: values.quantity } },
      {
        onSuccess: () => {
          message.success("Stock adjusted successfully");
          queryClient.invalidateQueries({
            queryKey: getQueryProductsQueryKey(),
          });
          queryClient.invalidateQueries({
            queryKey: getGetLowStockProductsQueryKey(),
          });
          onClose();
        },
      },
    );
  };

  return (
    <Modal
      title="Adjust Stock"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={adjustMutation.isPending}
      destroyOnHidden
    >
      <Typography.Paragraph>
        <strong>{product?.name}</strong> — Current stock: {currentStock}
      </Typography.Paragraph>

      <Form form={form} layout="vertical">
        <Form.Item
          name="quantity"
          label="Adjustment Quantity"
          extra="Positive to increase, negative to decrease"
          rules={[
            { required: true, message: "Please enter quantity" },
            {
              type: "number",
              min: -currentStock,
              message: `Stock cannot go below 0 (min: ${-currentStock})`,
            },
          ]}
        >
          <InputNumber
            style={{ width: "100%" }}
            precision={0}
            placeholder="e.g. 10 or -5"
            onChange={handleQuantityChange}
          />
        </Form.Item>
      </Form>

      {preview !== null && (
        <Typography.Text
          type={preview < 0 ? "danger" : undefined}
          strong
        >
          Expected stock after adjustment: {preview}
        </Typography.Text>
      )}
    </Modal>
  );
};

export default AdjustStockModal;
