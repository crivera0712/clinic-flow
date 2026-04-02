import { apiRequest } from "../api/client";
import type { BodyRegion, BodyRegionCreateRequest, BodyRegionUpdateRequest, EntityListResult, PageResponse } from "../types/admin";

type ListBodyRegionParams = {
  page?: number;
  size?: number;
  search?: string;
};

export async function listBodyRegions(params: ListBodyRegionParams = {}): Promise<EntityListResult<BodyRegion>> {
  const searchParams = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 1000),
  });
  if (params.search?.trim()) {
    searchParams.set("search", params.search.trim());
  }

  const response = await apiRequest<PageResponse<BodyRegion>>(`/bodyregion?${searchParams.toString()}`);
  return { rows: response.content, rowCount: response.totalElements };
}

export function createBodyRegion(payload: BodyRegionCreateRequest) {
  return apiRequest<BodyRegion>("/bodyregion", { method: "POST", body: payload });
}

export function updateBodyRegion(id: number, payload: BodyRegionUpdateRequest) {
  return apiRequest<BodyRegion>(`/bodyregion/${id}`, { method: "PATCH", body: payload });
}

export function removeBodyRegion(id: number) {
  return apiRequest<void>(`/bodyregion/${id}`, { method: "DELETE" });
}
