export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface EntityListResult<T> {
  rows: T[];
  rowCount: number;
}
