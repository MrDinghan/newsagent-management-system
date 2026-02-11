import { type FC } from "react";
import { Navigate } from "react-router-dom";

const HomePage: FC = () => {
  return <Navigate to="/products" replace />;
};

export default HomePage;
