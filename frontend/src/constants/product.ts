import { ProductVOType } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";

export const LOW_STOCK_THRESHOLD = 5;

export const productTypeLabels: Record<string, string> = {
  [ProductVOType.NEWSPAPER]: "Newspaper",
  [ProductVOType.MAGAZINE]: "Magazine",
};

export const productTypeColors: Record<string, string> = {
  [ProductVOType.NEWSPAPER]: "blue",
  [ProductVOType.MAGAZINE]: "green",
};
