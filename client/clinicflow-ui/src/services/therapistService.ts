import { apiRequest } from "../api/client";
import type {
  EntityListResult,
  PageResponse,
  Therapist,
  TherapistCreateRequest,
  TherapistUpdateRequest,
} from "../types/admin";

type ListTherapistParams = {
  page?: number;
  size?: number;
  search?: string;
};

export async function listTherapists(params: ListTherapistParams = {}): Promise<EntityListResult<Therapist>> {
  const searchParams = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 1000),
  });
  if (params.search?.trim()) {
    searchParams.set("search", params.search.trim());
  }

  const response = await apiRequest<PageResponse<Therapist>>(`/therapists?${searchParams.toString()}`);
  return { rows: response.content, rowCount: response.totalElements };
}

export function createTherapist(payload: TherapistCreateRequest) {
  return apiRequest<Therapist>("/therapists", { method: "POST", body: payload });
}

export function updateTherapist(id: number, payload: TherapistUpdateRequest) {
  return apiRequest<Therapist>(`/therapists/${id}`, { method: "PATCH", body: payload });
}

export function removeTherapist(id: number) {
  return apiRequest<void>(`/therapists/${id}`, { method: "DELETE" });
}
