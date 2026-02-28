import { css } from "@emotion/css";
import { Card, Col, Row, Typography } from "antd";
import { type FC } from "react";

import CartTable from "./components/CartTable";
import ProductSearch from "./components/ProductSearch";
import SaleSummary from "./components/SaleSummary";
import { useCart } from "./hooks/useCart";

const { Title } = Typography;

const SalesPage: FC = () => {
  const cart = useCart();

  return (
    <div
      className={css`
        display: flex;
        flex-direction: column;
        height: 100%;
      `}
    >
      <Row
        gutter={24}
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
              },
            }}
          >
            <Title
              level={5}
              className={css`
                flex-shrink: 0;
              `}
            >
              Products
            </Title>
            <ProductSearch onAdd={cart.addItem} cartItems={cart.items} />
          </Card>
        </Col>

        <Col
          span={12}
          className={css`
            height: 100%;
          `}
        >
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
              },
            }}
          >
            <Title
              level={5}
              className={css`
                flex-shrink: 0;
              `}
            >
              Shopping Cart
            </Title>
            <CartTable
              items={cart.items}
              onUpdateQuantity={cart.updateQuantity}
              onRemove={cart.removeItem}
            />
          </Card>
        </Col>
      </Row>

      <Card
        className={css`
          margin-top: 16px;
          flex-shrink: 0;
        `}
      >
        <SaleSummary
          itemCount={cart.itemCount}
          totalQuantity={cart.totalQuantity}
          totalAmount={cart.totalAmount}
          toCreateSaleRequest={cart.toCreateSaleRequest}
          onOrderSuccess={cart.clearCart}
        />
      </Card>
    </div>
  );
};

export default SalesPage;
