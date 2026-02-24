import { useRoutes } from "react-router-dom";

import MainLayout from "@/layouts/MainLayout";
import routes from "~react-pages";

function App() {
  const pages = useRoutes(routes);

  return <MainLayout>{pages}</MainLayout>;
}

export default App;
