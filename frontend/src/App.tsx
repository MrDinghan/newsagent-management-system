import { useRoutes } from "react-router-dom";

import routes from "~react-pages";

function App() {
  const pages = useRoutes(routes);

  return pages;
}

export default App;
