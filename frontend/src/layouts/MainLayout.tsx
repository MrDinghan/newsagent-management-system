import { ShoppingOutlined } from "@ant-design/icons";
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
          left: 0;
          top: 0;
          background: ${token.colorBgContainer};
        `}
      >
        <div
          className={css`
            height: 64px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            color: ${token.colorTextBase};
            font-size: ${token.fontSizeLG}px;
          `}
        >
          Newsstand
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
          padding: ${token.paddingLG}px;
          min-height: 100vh;
        `}
      >
        {children}
      </Content>
    </Layout>
  );
};

export default MainLayout;
