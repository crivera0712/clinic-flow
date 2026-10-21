import { useEffect, useState } from "react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createPatient,
  listPatients,
  searchPatients,
  updatePatient,
} from "../services/patientService";
import type { PatientCreateRequest, PatientUpdateRequest } from "../types/patient";
import { queryKeys } from "./keys";

// The directory loads the first 200 patients in one request and pages through them client-side.
export function usePatients() {
  return useQuery({
    queryKey: queryKeys.patients.list,
    queryFn: () => listPatients({ page: 0, size: 200 }),
  });
}

function useDebouncedValue<T>(value: T, delayMs: number): T {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = window.setTimeout(() => setDebounced(value), delayMs);
    return () => window.clearTimeout(id);
  }, [value, delayMs]);
  return debounced;
}

// Typeahead search, debounced 200ms. Keeps the last matches on screen while the next search runs.
export function usePatientSearch(query: string) {
  const debounced = useDebouncedValue(query.trim(), 200);
  return useQuery({
    queryKey: queryKeys.patients.search(debounced),
    queryFn: () => searchPatients(debounced),
    enabled: debounced.length > 0,
    placeholderData: keepPreviousData,
  });
}

export function useCreatePatient() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: PatientCreateRequest) => createPatient(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.patients.all }),
  });
}

export function useUpdatePatient() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, patch }: { id: number; patch: PatientUpdateRequest }) =>
      updatePatient(id, patch),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.patients.all });
      // Schedule rows and the board show patient names.
      void queryClient.invalidateQueries({ queryKey: queryKeys.appointments.all });
    },
  });
}
