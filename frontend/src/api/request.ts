import { message } from "antd";
import type { AxiosRequestConfig } from "axios";
import Axios from "axios";

export const AXIOS_INSTANCE = Axios.create({
  withCredentials: true,
});

AXIOS_INSTANCE.interceptors.request.use((config) => {
  return config;
});

AXIOS_INSTANCE.interceptors.response.use(
  (response) => {
    const success = response.data?.success;
    const errorMsg = response.data?.errorMsg;
    if (success) {
      return response;
    }
    return Promise.reject(new Error(errorMsg || "Unknown error"));
  },
  (error) => {
    if (Axios.isCancel(error)) {
      return Promise.reject(error);
    }
    let errorMsg = error?.message || "An error occurred";
    const resErrorMsg = error.response?.data?.errorMsg;
    if (resErrorMsg) {
      errorMsg = resErrorMsg;
    }
    message.error(errorMsg);
    return Promise.reject(new Error(errorMsg));
  },
);

export const request = <T>(url: string, options?: RequestInit): Promise<T> => {
  const source = Axios.CancelToken.source();
  const promise = AXIOS_INSTANCE({
    url,
    method: options?.method as AxiosRequestConfig["method"],
    data: options?.body,
    headers: options?.headers as AxiosRequestConfig["headers"],
    cancelToken: source.token,
    signal: options?.signal as AbortSignal,
  }).then((res) => res.data as T);

  // @ts-expect-error attach cancel method for react-query
  promise.cancel = () => source.cancel("Query was cancelled");

  return promise;
};

export default request;
