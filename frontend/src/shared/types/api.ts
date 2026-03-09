export type ApiResponse<T> = {
  data: T;
  message?: string;
};

export type ApiError = {
  status: number;
  message: string;
  errors?: Record<string, string[]>;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};
