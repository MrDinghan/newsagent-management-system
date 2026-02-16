import { css } from "@emotion/css";
import { useQueryClient } from "@tanstack/react-query";
import { App, Form, Input, InputNumber, Modal, Select } from "antd";
import { type FC, useEffect } from "react";

import type { ProductVO } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import {
  getGetLowStockProductsQueryKey,
  getQueryProductsQueryKey,
  useUpdateProduct,
} from "@/api/endpoints/product-management";
import { productTypeLabels } from "@/constants/product";

interface EditProductModalProps {
  open: boolean;
  product: ProductVO | null;
  onClose: () => void;
}

const EditProductModal: FC<EditProductModalProps> = ({
  open,
  product,
  onClose,
}) => {
  const [form] = Form.useForm();
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const updateMutation = useUpdateProduct();

  useEffect(() => {
    if (open && product) {
      form.setFieldsValue({
        name: product.name,
        type: product.type,
        price: product.price,
      });
    }
  }, [open, product, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    if (!product?.id) return;
    updateMutation.mutate(
      { id: product.id, data: values },
      {
        onSuccess: () => {
          message.success("Product updated successfully");
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
      title="Edit Product"
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={updateMutation.isPending}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="name"
          label="Product Name"
          rules={[
            { required: true, message: "Please enter product name" },
            { max: 100, message: "Name cannot exceed 100 characters" },
          ]}
        >
          <Input placeholder="Enter product name" />
        </Form.Item>

        <Form.Item
          name="type"
          label="Type"
          rules={[{ required: true, message: "Please select product type" }]}
        >
          <Select
            placeholder="Select type"
            options={Object.entries(productTypeLabels).map(([value, label]) => ({
              label,
              value,
            }))}
          />
        </Form.Item>

        <Form.Item
          name="price"
          label="Price"
          rules={[
            { required: true, message: "Please enter price" },
            {
              type: "number",
              min: 0.01,
              max: 999999.99,
              message: "Price must be between 0.01 and 999999.99",
            },
          ]}
        >
          <InputNumber
            prefix="€"
            className={css`
              width: 100%;
            `}
            precision={2}
            placeholder="0.00"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default EditProductModal;
