import { Modal } from "antd";
import { type FC, useEffect } from "react";

import type { ProductVO } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";

import { useAdjustStockForm } from "../hooks/useAdjustStockForm";
import AdjustStockForm from "./AdjustStockForm";

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
  const { form, preview, isPending, handleQuantityChange, handleSubmit, reset } =
    useAdjustStockForm({ onSuccess: onClose });

  useEffect(() => {
    if (open) reset();
  }, [open, reset]);

  return (
    <Modal
      title="Adjust Stock"
      open={open}
      onOk={() => handleSubmit(product?.id)}
      onCancel={onClose}
      confirmLoading={isPending}
      destroyOnHidden
    >
      <AdjustStockForm
        form={form}
        productName={product?.name ?? ""}
        currentStock={product?.stock ?? 0}
        preview={preview}
        onQuantityChange={handleQuantityChange}
      />
    </Modal>
  );
};

export default AdjustStockModal;
