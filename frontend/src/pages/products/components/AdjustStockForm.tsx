import { css } from "@emotion/css";
import type { FormInstance } from "antd";
import { Form, InputNumber, Typography } from "antd";
import { type FC } from "react";

interface AdjustStockFormProps {
  form: FormInstance;
  productName: string;
  currentStock: number;
  preview: number | null;
  onQuantityChange: (currentStock: number, value: number | null) => void;
}

const AdjustStockForm: FC<AdjustStockFormProps> = ({
  form,
  productName,
  currentStock,
  preview,
  onQuantityChange,
}) => {
  return (
    <>
      <Typography.Paragraph>
        <strong>{productName}</strong> — Current stock: {currentStock}
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
            className={css`
              width: 100%;
            `}
            precision={0}
            placeholder="e.g. 10 or -5"
            onChange={(value) => onQuantityChange(currentStock, value as number | null)}
          />
        </Form.Item>
      </Form>

      {preview !== null && (
        <Typography.Text type={preview < 0 ? "danger" : undefined} strong>
          Expected stock after adjustment: {preview}
        </Typography.Text>
      )}
    </>
  );
};

export default AdjustStockForm;
