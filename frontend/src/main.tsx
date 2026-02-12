import "./index.css";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp, ConfigProvider, theme } from "antd";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";

import App from "./App.tsx";

const queryClient = new QueryClient();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ConfigProvider
      theme={{
        algorithm: theme.defaultAlgorithm,
      }}
    >
      <AntdApp>
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </QueryClientProvider>
      </AntdApp>
    </ConfigProvider>
  </StrictMode>,
);
