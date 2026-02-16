import { useQueryClient } from "@tanstack/react-query";
import { App, Form, Input, InputNumber, Modal, Select } from "antd";
import { type FC } from "react";

import {
  getGetLowStockProductsQueryKey,
  getQueryProductsQueryKey,
  useCreateProduct,
} from "@/api/endpoints/product-management";
import { productTypeLabels } from "@/constants/product";

interface CreateProductModalProps {
  open: boolean;
  onClose: () => void;
}

const CreateProductModal: FC<CreateProductModalProps> = ({ open, onClose }) => {
  const [form] = Form.useForm();
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const createMutation = useCreateProduct();

  const handleOk = async () => {
    const values = await form.validateFields();
    createMutation.mutate(
      { data: values },
      {
        onSuccess: () => {
          message.success("Product created successfully");
          queryClient.invalidateQueries({
            queryKey: getQueryProductsQueryKey(),
          });
          queryClient.invalidateQueries({
            queryKey: getGetLowStockProductsQueryKey(),
          });
          form.resetFields();
          onClose();
        },
      },
    );
  };

  const handleCancel = () => {
    form.resetFields();
    onClose();
  };

  return (
    <Modal
      title="Add Product"
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      confirmLoading={createMutation.isPending}
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
            style={{ width: "100%" }}
            precision={2}
            placeholder="0.00"
          />
        </Form.Item>

        <Form.Item
          name="stock"
          label="Initial Stock"
          rules={[
            { required: true, message: "Please enter initial stock" },
            {
              type: "number",
              min: 0,
              message: "Stock cannot be negative",
            },
          ]}
        >
          <InputNumber
            style={{ width: "100%" }}
            precision={0}
            placeholder="0"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default CreateProductModal;
