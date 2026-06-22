import { apiRequest } from "../api/client";
import type { Therapist, TherapistCreateRequest } from "../types/therapist";

export function listTherapists(): Promise<Therapist[]> {
  return apiRequest<Therapist[]>("/therapists");
}

export function createTherapist(payload: TherapistCreateRequest): Promise<Therapist> {
  return apiRequest<Therapist>("/therapists", { method: "POST", body: payload });
}

export function removeTherapist(id: number): Promise<void> {
  return apiRequest<void>(`/therapists/${id}`, { method: "DELETE" });
}
