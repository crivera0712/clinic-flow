import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createAppointment,
  listAppointmentsByDate,
  removeAppointment,
  updateAppointment,
} from "../services/appointmentService";
import type {
  AppointmentCreateRequest,
  AppointmentStatus,
  AppointmentUpdateRequest,
  BoardRow,
} from "../types/appointment";
import { queryKeys } from "./keys";

// One day's appointments. While a newly picked date loads, the previous date's rows stay as
// placeholder data (`isPlaceholderData` is true), so the page can dim them instead of blanking.
export function useAppointments(date: string, options: { refetchInterval?: number } = {}) {
  return useQuery({
    queryKey: queryKeys.appointments.byDate(date),
    queryFn: () => listAppointmentsByDate(date),
    placeholderData: keepPreviousData,
    refetchInterval: options.refetchInterval,
  });
}

export function useCreateAppointment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: AppointmentCreateRequest) => createAppointment(payload),
    onSuccess: (_created, payload) => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.appointments.all });
      // Quick add can create the patient inline.
      if (payload.patient) void queryClient.invalidateQueries({ queryKey: queryKeys.patients.all });
    },
  });
}

export function useUpdateAppointment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, patch }: { id: number; patch: AppointmentUpdateRequest }) =>
      updateAppointment(id, patch),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.appointments.all }),
  });
}

// Check-in (Arrived / Start visit). The row changes in the cache immediately; if the server
// rejects the change, the row goes back to its previous status.
export function useUpdateAppointmentStatus() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ row, status }: { row: BoardRow; status: AppointmentStatus }) =>
      updateAppointment(row.id, { status }),
    onMutate: async ({ row, status }) => {
      const key = queryKeys.appointments.byDate(row.scheduledAt.slice(0, 10));
      // Stop an in-flight refetch from overwriting the optimistic row with older data.
      await queryClient.cancelQueries({ queryKey: key });
      const previous = queryClient.getQueryData<BoardRow[]>(key);
      queryClient.setQueryData<BoardRow[]>(key, (rows) =>
        rows?.map((current) => (current.id === row.id ? { ...current, status } : current)),
      );
      return { key, previous };
    },
    onError: (_error, _variables, context) => {
      if (context?.previous) queryClient.setQueryData(context.key, context.previous);
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: queryKeys.appointments.all }),
  });
}

export function useRemoveAppointment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => removeAppointment(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.appointments.all }),
  });
}
