import { css } from "@emotion/css";
import { Button, Descriptions, Modal, Space, Spin, Tag } from "antd";
import { type FC, useEffect, useMemo, useState } from "react";

import {
  getGetProductByIdQueryKey,
  useGetProductById,
} from "@/api/endpoints/product-management";
import { productTypeColors, productTypeLabels } from "@/constants/product";
import { formatDateTime } from "@/utils/format";

import { useAdjustStockForm } from "../hooks/useAdjustStockForm";
import AdjustStockForm from "./AdjustStockForm";

interface ProductDetailModalProps {
  open: boolean;
  productId: string | null;
  onClose: () => void;
}

const ProductDetailModal: FC<ProductDetailModalProps> = ({
  open,
  productId,
  onClose,
}) => {
  const [showAdjustStock, setShowAdjustStock] = useState(false);

  const { data, isLoading } = useGetProductById(productId ?? "", {
    query: { enabled: !!productId },
  });

  const product = data?.data;

  const extraInvalidateKeys = useMemo(
    () => (productId ? [getGetProductByIdQueryKey(productId)] : []),
    [productId],
  );

  const {
    form,
    preview,
    isPending,
    handleQuantityChange,
    handleSubmit,
    reset,
  } = useAdjustStockForm({
    onSuccess: () => setShowAdjustStock(false),
    extraInvalidateKeys,
  });

  useEffect(() => {
    if (open) {
      setShowAdjustStock(false);
      reset();
    }
  }, [open, reset]);

  const handleCancel = () => {
    if (showAdjustStock) {
      setShowAdjustStock(false);
      reset();
    } else {
      onClose();
    }
  };

  const getContent = () => {
    if (isLoading) {
      return (
        <Spin
          className={css`
            display: block;
            text-align: center;
            padding: 24px;
          `}
        />
      );
    }

    if (!product) {
      return null;
    }

    if (showAdjustStock) {
      return (
        <AdjustStockForm
          form={form}
          productName={product.name ?? ""}
          currentStock={product.stock ?? 0}
          preview={preview}
          onQuantityChange={handleQuantityChange}
        />
      );
    }

    return (
      <Descriptions column={1} bordered size="small">
        <Descriptions.Item label="Name">{product.name}</Descriptions.Item>
        <Descriptions.Item label="Type">
          <Tag color={productTypeColors[product.type ?? ""]}>
            {productTypeLabels[product.type ?? ""] ?? product.type}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item label="Price">
          €{product.price?.toFixed(2)}
        </Descriptions.Item>
        <Descriptions.Item label="Stock">
          <Space>
            {product.stock}
            <Button
              type="link"
              size="small"
              onClick={() => setShowAdjustStock(true)}
            >
              Adjust Stock
            </Button>
          </Space>
        </Descriptions.Item>
        <Descriptions.Item label="Created At">
          {formatDateTime(product.createdAt)}
        </Descriptions.Item>
        <Descriptions.Item label="Updated At">
          {formatDateTime(product.updatedAt)}
        </Descriptions.Item>
      </Descriptions>
    );
  };

  return (
    <Modal
      title={showAdjustStock ? "Adjust Stock" : "Product Details"}
      open={open}
      onCancel={handleCancel}
      footer={
        showAdjustStock && (
          <Space>
            <Button onClick={handleCancel}>Cancel</Button>
            <Button
              type="primary"
              onClick={() => handleSubmit(product?.id)}
              loading={isPending}
            >
              OK
            </Button>
          </Space>
        )
      }
    >
      {getContent()}
    </Modal>
  );
};

export default ProductDetailModal;
