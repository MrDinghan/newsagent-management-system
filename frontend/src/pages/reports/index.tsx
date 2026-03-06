import { css } from "@emotion/css";
import {
  Card,
  Col,
  DatePicker,
  Empty,
  Row,
  Spin,
  Statistic,
  Typography,
} from "antd";
import dayjs from "dayjs";
import ReactECharts from "echarts-for-react";
import { type FC, useState } from "react";

import { useGetDailyReport } from "@/api/endpoints/report";

const { Title } = Typography;

const ReportsPage: FC = () => {
  const [date, setDate] = useState<dayjs.Dayjs>(dayjs());

  const { data, isLoading } = useGetDailyReport({
    date: date.format("YYYY-MM-DD"),
  });

  const report = data?.data;
  const hasData = (report?.orderCount ?? 0) > 0;
  const categorySales = report?.categorySales;
  const topProducts = report?.topProducts ?? [];

  const barOption = {
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    grid: {
      left: "3%",
      right: "8%",
      top: "4%",
      bottom: "4%",
      containLabel: true,
    },
    xAxis: { type: "value", minInterval: 1 },
    yAxis: {
      type: "category",
      data: [...topProducts].reverse().map((p) => p.productName ?? ""),
    },
    series: [
      {
        type: "bar",
        data: [...topProducts].reverse().map((p) => p.totalQuantity ?? 0),
        itemStyle: { color: "#1677ff" },
        label: { show: true, position: "right" },
      },
    ],
  };

  const pieOption = {
    tooltip: {
      trigger: "item",
      formatter: (params: { name: string; value: number; percent: number }) =>
        `${params.name}: €${params.value.toFixed(2)} (${params.percent}%)`,
    },
    legend: { bottom: 0 },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        center: ["50%", "45%"],
        data: [
          {
            value: categorySales?.newspaperAmount ?? 0,
            name: "Newspaper",
            itemStyle: { color: "#1677ff" },
          },
          {
            value: categorySales?.magazineAmount ?? 0,
            name: "Magazine",
            itemStyle: { color: "#52c41a" },
          },
        ],
        label: { formatter: "{b}\n{d}%" },
      },
    ],
  };

  return (
    <Card
      className={css`
        height: 100%;
        display: flex;
        flex-direction: column;
      `}
      styles={{
        body: {
          flex: 1,
          minHeight: 0,
          display: "flex",
          flexDirection: "column",
          gap: 16,
        },
      }}
    >
      <div
        className={css`
          display: flex;
          align-items: center;
          justify-content: space-between;
          flex-shrink: 0;
        `}
      >
        <Title level={5}>Daily Sales Report</Title>
        <DatePicker
          value={date}
          onChange={(d) => d && setDate(d)}
          disabledDate={(d) => d.isAfter(dayjs(), "day")}
          allowClear={false}
        />
      </div>

      <Spin
        spinning={isLoading}
        className={css`
          flex-shrink: 0;
        `}
      >
        <Row gutter={16}>
          <Col span={12}>
            <Card>
              <Statistic
                title="Total Sales"
                value={report?.totalAmount ?? 0}
                precision={2}
                prefix="€"
              />
            </Card>
          </Col>
          <Col span={12}>
            <Card>
              <Statistic title="Orders" value={report?.orderCount ?? 0} />
            </Card>
          </Col>
        </Row>
      </Spin>

      <Row
        gutter={16}
        className={css`
          flex: 1;
          min-height: 0;
        `}
      >
        <Col
          span={12}
          className={css`
            height: 100%;
          `}
        >
          <Card
            title="Top 5 Products"
            className={css`
              height: 100%;
              display: flex;
              flex-direction: column;
            `}
            styles={{ body: { flex: 1, minHeight: 0 } }}
          >
            {!isLoading && !hasData ? (
              <div
                className={css`
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  height: 100%;
                `}
              >
                <Empty description="No sales data" />
              </div>
            ) : (
              <ReactECharts
                option={barOption}
                style={{ height: "100%" }}
                notMerge
              />
            )}
          </Card>
        </Col>

        <Col
          span={12}
          className={css`
            height: 100%;
          `}
        >
          <Card
            title="Sales by Category"
            className={css`
              height: 100%;
              display: flex;
              flex-direction: column;
            `}
            styles={{ body: { flex: 1, minHeight: 0 } }}
          >
            {!isLoading && !hasData ? (
              <div
                className={css`
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  height: 100%;
                `}
              >
                <Empty description="No sales data" />
              </div>
            ) : (
              <ReactECharts
                option={pieOption}
                style={{ height: "100%" }}
                notMerge
              />
            )}
          </Card>
        </Col>
      </Row>
    </Card>
  );
};

export default ReportsPage;
