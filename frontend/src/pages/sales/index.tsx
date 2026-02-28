import { css } from "@emotion/css";
import { Card, Col, Row, Typography } from "antd";
import { type FC } from "react";

import CartTable from "./components/CartTable";
import ProductSearch from "./components/ProductSearch";
import { useCart } from "./hooks/useCart";

const { Title, Text } = Typography;

const SalesPage: FC = () => {
  const cart = useCart();

  return (
    <Row
      gutter={24}
      className={css`
        height: calc(100vh - 64px - 48px);
      `}
    >
      <Col
        span={14}
        className={css`
          height: 100%;
          overflow-y: auto;
        `}
      >
        <Card>
          <Title level={5}>Products</Title>
          <ProductSearch onAdd={cart.addItem} cartItems={cart.items} />
        </Card>
      </Col>

      <Col
        span={10}
        className={css`
          height: 100%;
          display: flex;
          flex-direction: column;
          gap: 16px;
        `}
      >
        <Card
          className={css`
            flex: 1;
            overflow-y: auto;
          `}
        >
          <Title level={5}>Shopping Cart</Title>
          <CartTable items={cart.items} />
        </Card>

        <Card>
          <Title level={5}>Order Summary</Title>
          <div
            className={css`
              display: flex;
              justify-content: space-between;
            `}
          >
            <Text>Items: {cart.itemCount}</Text>
            <Text>Total Qty: {cart.totalQuantity}</Text>
            <Text strong>Total: &euro;{cart.totalAmount.toFixed(2)}</Text>
          </div>
        </Card>
      </Col>
    </Row>
  );
};

export default SalesPage;
