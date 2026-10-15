import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createTherapist, listTherapists, removeTherapist } from "../services/therapistService";
import type { TherapistCreateRequest } from "../types/therapist";
import { queryKeys } from "./keys";

// Shared by the Schedule page, the Therapists page and the display board.
export function useTherapists(options: { refetchInterval?: number } = {}) {
  return useQuery({
    queryKey: queryKeys.therapists.all,
    queryFn: listTherapists,
    refetchInterval: options.refetchInterval,
  });
}

export function useCreateTherapist() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: TherapistCreateRequest) => createTherapist(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.therapists.all }),
  });
}

export function useRemoveTherapist() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => removeTherapist(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.therapists.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.appointments.all });
    },
  });
}
