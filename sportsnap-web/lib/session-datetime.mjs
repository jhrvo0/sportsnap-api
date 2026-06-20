const DATE_TIME_LOCAL_LENGTH = 16;
const API_LOCAL_DATE_TIME_LENGTH = 19;
const TIMEZONE_SUFFIX = /(?:Z|[+-]\d{2}:?\d{2})$/;

function pad(value) {
  return String(value).padStart(2, "0");
}

function formatLocalDateTime(date) {
  return [
    date.getFullYear(),
    "-",
    pad(date.getMonth() + 1),
    "-",
    pad(date.getDate()),
    "T",
    pad(date.getHours()),
    ":",
    pad(date.getMinutes()),
  ].join("");
}

export function toDateTimeLocalValue(value) {
  if (!value) return "";

  if (TIMEZONE_SUFFIX.test(value)) {
    return formatLocalDateTime(new Date(value));
  }

  return value.slice(0, DATE_TIME_LOCAL_LENGTH);
}

export function fromDateTimeLocalValue(value) {
  if (!value) return "";

  if (value.length === DATE_TIME_LOCAL_LENGTH) {
    return `${value}:00`;
  }

  return value.slice(0, API_LOCAL_DATE_TIME_LENGTH);
}
