import { useCallback, useEffect, useState } from "react";

type EntityListResult<T> = {
  rows: T[];
  rowCount: number;
};

type EntityCrudService<T, TListParams, TCreate, TUpdate> = {
  list: (params: TListParams) => Promise<EntityListResult<T>>;
  create: (payload: TCreate) => Promise<unknown>;
  update: (id: number, payload: TUpdate) => Promise<unknown>;
  remove: (id: number) => Promise<void>;
};

type MutationKind = "create" | "update" | "delete" | null;

export function useEntityCrud<T, TListParams, TCreate, TUpdate>(
  service: EntityCrudService<T, TListParams, TCreate, TUpdate>,
  listParams: TListParams,
  queryKey: string,
) {
  const [rows, setRows] = useState<T[]>([]);
  const [rowCount, setRowCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [mutationKind, setMutationKind] = useState<MutationKind>(null);

  const refresh = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await service.list(listParams);
      setRows(result.rows);
      setRowCount(result.rowCount);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Failed to load records.");
    } finally {
      setLoading(false);
    }
  }, [listParams, service]);

  useEffect(() => {
    void refresh();
  }, [queryKey, refresh]);

  async function createEntity(payload: TCreate) {
    setMutationKind("create");
    try {
      await service.create(payload);
      await refresh();
    } finally {
      setMutationKind(null);
    }
  }

  async function updateEntity(id: number, payload: TUpdate) {
    setMutationKind("update");
    try {
      await service.update(id, payload);
      await refresh();
    } finally {
      setMutationKind(null);
    }
  }

  async function deleteEntity(id: number) {
    setMutationKind("delete");
    try {
      await service.remove(id);
      await refresh();
    } finally {
      setMutationKind(null);
    }
  }

  return {
    rows,
    rowCount,
    loading,
    error,
    refresh,
    createEntity,
    updateEntity,
    deleteEntity,
    mutationKind,
  };
}
