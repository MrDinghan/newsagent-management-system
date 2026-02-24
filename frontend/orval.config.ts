import { defineConfig } from "orval";

export default defineConfig({
  api: {
    input: "http://34.194.175.52:8080/v3/api-docs",
    output: {
      mode: "tags",
      target: "./src/api/endpoints",
      client: "react-query",
      override: {
        mutator: {
          path: "./src/api/request.ts",
          name: "request",
        },
        fetch: {
          includeHttpResponseReturnType: false,
        },
      },
    },
  },
});
