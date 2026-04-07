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

type TherapistApiResponse = {
  therapistId: number;
  therapistName: string;
  type: string;
};

function mapTherapist(response: TherapistApiResponse): Therapist {
  return {
    id: response.therapistId,
    therapistName: response.therapistName,
    type: response.type,
  };
}

export async function listTherapists(params: ListTherapistParams = {}): Promise<EntityListResult<Therapist>> {
  const searchParams = new URLSearchParams({
    page: String(params.page ?? 0),
    size: String(params.size ?? 1000),
  });
  if (params.search?.trim()) {
    searchParams.set("search", params.search.trim());
  }

  const response = await apiRequest<PageResponse<TherapistApiResponse>>(`/therapists?${searchParams.toString()}`);
  return { rows: response.content.map(mapTherapist), rowCount: response.totalElements };
}

export function createTherapist(payload: TherapistCreateRequest) {
  return apiRequest<TherapistApiResponse>("/therapists", { method: "POST", body: payload }).then(mapTherapist);
}

export function updateTherapist(id: number, payload: TherapistUpdateRequest) {
  return apiRequest<TherapistApiResponse>(`/therapists/${id}`, { method: "PATCH", body: payload }).then(mapTherapist);
}

export function removeTherapist(id: number) {
  return apiRequest<void>(`/therapists/${id}`, { method: "DELETE" });
}
