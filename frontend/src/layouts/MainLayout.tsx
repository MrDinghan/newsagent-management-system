import {
  HistoryOutlined,
  ReadOutlined,
  ShoppingCartOutlined,
  ShoppingOutlined,
} from "@ant-design/icons";
import { css } from "@emotion/css";
import { Layout, Menu, theme } from "antd";
import { type FC, type ReactNode } from "react";
import { useLocation, useNavigate } from "react-router-dom";

const { Sider, Content } = Layout;

const menuItems = [
  {
    key: "/products",
    icon: <ShoppingOutlined />,
    label: "Products",
  },
  {
    key: "/sales",
    icon: <ShoppingCartOutlined />,
    label: "Sales",
  },
  {
    key: "/sales/history",
    icon: <HistoryOutlined />,
    label: "Sales History",
  },
];

const MainLayout: FC<{ children: ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = theme.useToken();

  return (
    <Layout
      className={css`
        min-height: 100vh;
      `}
    >
      <Sider
        className={css`
          overflow: auto;
          height: 100vh;
          position: sticky;
          top: 0;
          left: 0;
          background: ${token.colorBgContainer};
          border-right: 1px solid ${token.colorBorderSecondary};
        `}
        theme="light"
      >
        <div
          className={css`
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 16px 24px;
          `}
        >
          <ReadOutlined
            className={css`
              font-size: 24px;
              color: ${token.colorPrimary};
            `}
          />
          <span
            className={css`
              font-size: ${token.fontSizeHeading5}px;
              font-weight: 600;
              color: ${token.colorTextBase};
            `}
          >
            Newsstand
          </span>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Content
        className={css`
          padding: ${token.padding}px;
          height: 100vh;
          overflow-y: auto;
        `}
      >
        <div
          className={css`
            padding: ${token.padding}px;
            background: ${token.colorBgContainer};
            border-radius: ${token.borderRadius}px;
            height: 100%;
          `}
        >
          {children}
        </div>
      </Content>
    </Layout>
  );
};

export default MainLayout;
