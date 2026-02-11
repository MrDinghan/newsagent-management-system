import { ReadOutlined, ShoppingOutlined } from "@ant-design/icons";
import { css } from "@emotion/css";
import { Layout, Menu, theme } from "antd";
import { type FC, type ReactNode } from "react";
import { useLocation, useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;

const menuItems = [
  {
    key: "/products",
    icon: <ShoppingOutlined />,
    label: "Products",
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
      <Header
        className={css`
          display: flex;
          align-items: center;
          padding: 0 ${token.paddingLG}px;
          background: ${token.colorBgContainer};
          border-bottom: 1px solid ${token.colorBorderSecondary};
          position: sticky;
          top: 0;
          z-index: 100;
        `}
      >
        <div
          className={css`
            display: flex;
            align-items: center;
            gap: 8px;
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
      </Header>
      <Layout>
        <Sider
          className={css`
            overflow: auto;
            height: calc(100vh - 64px);
            position: sticky;
            top: 64px;
            left: 0;
            background: ${token.colorBgContainer};
            border-right: 1px solid ${token.colorBorderSecondary};
          `}
          theme="light"
        >
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={({ key }) => navigate(key)}
          />
        </Sider>
        <Content
          className={css`
            padding: ${token.paddingLG}px;
            min-height: calc(100vh - 64px);
          `}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
