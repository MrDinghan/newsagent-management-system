import { Descriptions, Modal, Spin, Tag } from "antd";
import { type FC } from "react";

import { useGetProductById } from "@/api/endpoints/product-management";
import { productTypeColors, productTypeLabels } from "@/constants/product";
import { formatDateTime } from "@/utils/format";

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
  const { data, isLoading } = useGetProductById(productId ?? "", {
    query: { enabled: !!productId },
  });

  const product = data?.data;

  return (
    <Modal
      title="Product Details"
      open={open}
      onCancel={onClose}
      footer={null}
    >
      {isLoading ? (
        <Spin style={{ display: "block", textAlign: "center", padding: 24 }} />
      ) : product ? (
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
          <Descriptions.Item label="Stock">{product.stock}</Descriptions.Item>
          <Descriptions.Item label="Created At">
            {formatDateTime(product.createdAt)}
          </Descriptions.Item>
          <Descriptions.Item label="Updated At">
            {formatDateTime(product.updatedAt)}
          </Descriptions.Item>
        </Descriptions>
      ) : null}
    </Modal>
  );
};

export default ProductDetailModal;
