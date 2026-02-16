import dayjs from "dayjs";

const DATE_TIME_FORMAT = "YYYY-MM-DD HH:mm:ss";

export const formatDateTime = (val?: string | null): string =>
  val ? dayjs(val).format(DATE_TIME_FORMAT) : "-";
