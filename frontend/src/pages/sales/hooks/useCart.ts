import { useCallback, useMemo, useState } from "react";

import type {
  CreateSaleRequest,
  ProductVO,
} from "@/api/endpoints/newsstandManagementSystemAPI.schemas";

export interface CartItem {
  productId: string;
  name: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
  availableStock: number;
}

export function useCart() {
  const [items, setItems] = useState<CartItem[]>([]);

  const addItem = useCallback((product: ProductVO) => {
    const { id, name, price, stock } = product;
    if (!id || !name || price == null || stock == null) return;

    setItems((prev) => {
      const existing = prev.find((item) => item.productId === id);
      if (existing) {
        return prev.map((item) =>
          item.productId === id
            ? {
                ...item,
                quantity: item.quantity + 1,
                subtotal: (item.quantity + 1) * item.unitPrice,
              }
            : item,
        );
      }
      return [
        ...prev,
        {
          productId: id,
          name,
          unitPrice: price,
          quantity: 1,
          subtotal: price,
          availableStock: stock,
        },
      ];
    });
  }, []);

  const updateQuantity = useCallback(
    (productId: string, quantity: number) => {
      setItems((prev) =>
        prev.map((item) =>
          item.productId === productId
            ? { ...item, quantity, subtotal: quantity * item.unitPrice }
            : item,
        ),
      );
    },
    [],
  );

  const removeItem = useCallback((productId: string) => {
    setItems((prev) => prev.filter((item) => item.productId !== productId));
  }, []);

  const clearCart = useCallback(() => {
    setItems([]);
  }, []);

  const totalAmount = useMemo(
    () => items.reduce((sum, item) => sum + item.subtotal, 0),
    [items],
  );

  const totalQuantity = useMemo(
    () => items.reduce((sum, item) => sum + item.quantity, 0),
    [items],
  );

  const itemCount = useMemo(() => items.length, [items]);

  const toCreateSaleRequest = useCallback((): CreateSaleRequest => {
    return {
      items: items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
    };
  }, [items]);

  return {
    items,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    totalAmount,
    totalQuantity,
    itemCount,
    toCreateSaleRequest,
  };
}
