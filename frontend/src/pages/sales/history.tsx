import { css } from "@emotion/css";
import { Button, Card, DatePicker, Space, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import { type FC, useState } from "react";

import type { SaleOrderVO } from "@/api/endpoints/newsstandManagementSystemAPI.schemas";
import { useGetSaleHistory } from "@/api/endpoints/sale-management";
import { formatDateTime } from "@/utils/format";

import SaleDetailModal from "./components/SaleDetailModal";

const { Title } = Typography;
const { RangePicker } = DatePicker;

const SaleHistoryPage: FC = () => {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [dateRange, setDateRange] = useState<
    [dayjs.Dayjs | null, dayjs.Dayjs | null] | null
  >(null);
  const [detailId, setDetailId] = useState<string | null>(null);

  const { data, isLoading } = useGetSaleHistory({
    request: {},
    page,
    size: pageSize,
    startDate: dateRange?.[0]?.format("YYYY-MM-DD"),
    endDate: dateRange?.[1]?.format("YYYY-MM-DD"),
  });

  const columns: ColumnsType<SaleOrderVO> = [
    {
      title: "Order No.",
      dataIndex: "orderNumber",
      key: "orderNumber",
    },
    {
      title: "Date",
      dataIndex: "createdAt",
      key: "createdAt",
      render: formatDateTime,
    },
    {
      title: "Items",
      dataIndex: "itemCount",
      key: "itemCount",
    },
    {
      title: "Total Qty",
      dataIndex: "totalQuantity",
      key: "totalQuantity",
    },
    {
      title: "Total Amount",
      dataIndex: "totalAmount",
      key: "totalAmount",
      render: (amount: number) => `€${amount.toFixed(2)}`,
    },
    {
      title: "Action",
      key: "action",
      render: (_: unknown, record: SaleOrderVO) => (
        <Button
          type="link"
          size="small"
          onClick={() => setDetailId(record.id ?? null)}
        >
          View
        </Button>
      ),
    },
  ];

  return (
    <Card
      className={css`
        height: 100%;
      `}
    >
      <div
        className={css`
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 16px;
        `}
      >
        <Title level={5}>Sales History</Title>
        <Space>
          <RangePicker
            value={dateRange}
            onChange={(dates) => {
              setDateRange(dates);
              setPage(0);
            }}
          />
          {dateRange && (
            <Button
              onClick={() => {
                setDateRange(null);
                setPage(0);
              }}
            >
              Reset
            </Button>
          )}
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={data?.data}
        rowKey="id"
        loading={isLoading}
        className={css`
          .ant-table-body {
            scrollbar-width: thin;
            overflow: auto !important;
          }
        `}
        scroll={{
          x: "max-content",
          y: "calc(100vh - 250px)",
        }}
        pagination={{
          current: page + 1,
          pageSize,
          total: data?.total ?? 0,
          showSizeChanger: true,
          onChange: (p, size) => {
            setPage(p - 1);
            setPageSize(size);
          },
        }}
      />

      <SaleDetailModal
        open={!!detailId}
        saleId={detailId}
        onClose={() => setDetailId(null)}
      />
    </Card>
  );
};

export default SaleHistoryPage;
