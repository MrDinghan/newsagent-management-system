import { type FC, useState } from "react";

import type { ProductVO } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import { useGetLowStockProducts } from "@/api/endpoints/product-management";
import { LOW_STOCK_THRESHOLD } from "@/constants/product";

import ProductTable from "./ProductTable";

interface LowStockTabProps {
  onEdit: (product: ProductVO) => void;
  onAdjustStock: (product: ProductVO) => void;
  onDelete: (id: string) => void;
}

const LowStockTab: FC<LowStockTabProps> = ({
  onEdit,
  onAdjustStock,
  onDelete,
}) => {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const { data, isLoading } = useGetLowStockProducts({
    request: {},
    threshold: LOW_STOCK_THRESHOLD,
    page,
    size,
  });

  return (
    <ProductTable
      dataSource={data?.data}
      loading={isLoading}
      pagination={{
        current: page + 1,
        pageSize: size,
        total: data?.total,
        showSizeChanger: true,
        onChange: (p, s) => {
          setPage(p - 1);
          setSize(s);
        },
        showTotal: (total) => `Total ${total} products`,
      }}
      onEdit={onEdit}
      onAdjustStock={onAdjustStock}
      onDelete={onDelete}
    />
  );
};

export default LowStockTab;
