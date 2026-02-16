import { useQueryClient } from "@tanstack/react-query";
import { App, Form } from "antd";
import { useCallback, useState } from "react";

import {
  getGetLowStockProductsQueryKey,
  getQueryProductsQueryKey,
  useAdjustStock,
} from "@/api/endpoints/product-management";

interface UseAdjustStockFormOptions {
  onSuccess?: () => void;
  extraInvalidateKeys?: ReadonlyArray<readonly unknown[]>;
}

export function useAdjustStockForm({
  onSuccess,
  extraInvalidateKeys,
}: UseAdjustStockFormOptions = {}) {
  const [form] = Form.useForm();
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const adjustMutation = useAdjustStock();
  const [preview, setPreview] = useState<number | null>(null);

  const handleQuantityChange = useCallback(
    (currentStock: number, value: number | null) => {
      setPreview(value !== null ? currentStock + value : null);
    },
    [],
  );

  const reset = useCallback(() => {
    form.resetFields();
    setPreview(null);
  }, [form]);

  const submit = useCallback(
    async (productId: string, quantity: number) => {
      adjustMutation.mutate(
        { id: productId, data: { quantity } },
        {
          onSuccess: () => {
            message.success("Stock adjusted successfully");
            queryClient.invalidateQueries({
              queryKey: getQueryProductsQueryKey(),
            });
            queryClient.invalidateQueries({
              queryKey: getGetLowStockProductsQueryKey(),
            });
            extraInvalidateKeys?.forEach((queryKey) => {
              queryClient.invalidateQueries({ queryKey });
            });
            reset();
            onSuccess?.();
          },
        },
      );
    },
    [adjustMutation, message, queryClient, extraInvalidateKeys, reset, onSuccess],
  );

  const handleSubmit = useCallback(
    async (productId: string | undefined) => {
      const values = await form.validateFields();
      if (!productId) return;
      await submit(productId, values.quantity);
    },
    [form, submit],
  );

  return {
    form,
    preview,
    isPending: adjustMutation.isPending,
    handleQuantityChange,
    handleSubmit,
    reset,
  };
}
