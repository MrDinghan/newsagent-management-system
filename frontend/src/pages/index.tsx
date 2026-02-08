import { css } from "@emotion/css";
import { Button } from "antd";
import { type FC } from "react";

const HomePage: FC = () => {
  return (
    <div>
      <div
        className={css`
          color: red;
        `}
      >
        home page
      </div>
      <Button type="primary">click</Button>
    </div>
  );
};

export default HomePage;
