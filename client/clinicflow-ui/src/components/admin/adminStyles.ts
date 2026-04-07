import type { SxProps, Theme } from "@mui/material/styles";
import type { TextFieldProps } from "@mui/material/TextField";

export const adminColors = {
  pageBg: "#020617",
  panelBg: "rgba(15, 23, 42, 0.92)",
  panelElevated: "rgba(30, 41, 59, 0.92)",
  panelSelected: "rgba(30, 41, 59, 0.96)",
  border: "rgba(148, 163, 184, 0.16)",
  borderMuted: "rgba(148, 163, 184, 0.08)",
  accent: "#38bdf8",
  accentSoft: "rgba(56, 189, 248, 0.16)",
  textStrong: "#e2e8f0",
  textSecondary: "#94a3b8",
  textMuted: "#64748b",
};

export const adminTextFieldSx: SxProps<Theme> = {
  "& .MuiInputLabel-root": {
    color: adminColors.textSecondary,
  },
  "& .MuiInputLabel-root.Mui-focused": {
    color: adminColors.textStrong,
  },
  "& .MuiInputLabel-root.MuiInputLabel-shrink": {
    px: 0.75,
    backgroundColor: adminColors.panelBg,
  },
  "& .MuiInputBase-input": {
    color: adminColors.textStrong,
  },
  "& .MuiSelect-select": {
    color: adminColors.textStrong,
  },
  "& .MuiInputBase-input::placeholder": {
    color: adminColors.textMuted,
    opacity: 1,
  },
  "& .MuiFormHelperText-root": {
    color: adminColors.textSecondary,
  },
  "& .MuiFormHelperText-root.Mui-error": {
    color: "#fca5a5",
  },
  "& .MuiSvgIcon-root": {
    color: adminColors.textSecondary,
  },
  "& .MuiOutlinedInput-root": {
    color: adminColors.textStrong,
    backgroundColor: "rgba(15, 23, 42, 0.45)",
    "& fieldset": {
      borderColor: "rgba(148, 163, 184, 0.28)",
    },
    "&:hover fieldset": {
      borderColor: "rgba(56, 189, 248, 0.55)",
    },
    "&.Mui-focused fieldset": {
      borderColor: adminColors.accent,
    },
    "&.Mui-disabled": {
      color: adminColors.textSecondary,
      backgroundColor: "rgba(15, 23, 42, 0.3)",
    },
    "&.Mui-disabled .MuiInputBase-input": {
      WebkitTextFillColor: adminColors.textSecondary,
    },
    "&.Mui-disabled .MuiSelect-select": {
      WebkitTextFillColor: adminColors.textSecondary,
    },
  },
  "& .MuiOutlinedInput-input:-webkit-autofill": {
    WebkitBoxShadow: "0 0 0 100px rgba(15, 23, 42, 0.45) inset",
    WebkitTextFillColor: adminColors.textStrong,
    caretColor: adminColors.textStrong,
    borderRadius: "inherit",
  },
};

export const adminSelectProps: TextFieldProps["SelectProps"] = {
  MenuProps: {
    PaperProps: {
      sx: {
        backgroundColor: adminColors.panelElevated,
        color: adminColors.textStrong,
        border: `1px solid ${adminColors.border}`,
        backgroundImage: "none",
        "& .MuiMenuItem-root": {
          color: adminColors.textStrong,
        },
        "& .MuiMenuItem-root:hover": {
          backgroundColor: "rgba(56, 189, 248, 0.12)",
        },
        "& .MuiMenuItem-root.Mui-selected": {
          backgroundColor: "rgba(56, 189, 248, 0.18)",
        },
        "& .MuiMenuItem-root.Mui-selected:hover": {
          backgroundColor: "rgba(56, 189, 248, 0.24)",
        },
      },
    },
  },
};

export const adminDataGridSx: SxProps<Theme> = {
  border: 0,
  color: adminColors.textStrong,
  backgroundColor: adminColors.panelBg,
  "& .MuiDataGrid-columnHeaders": {
    backgroundColor: "#1e293b",
    borderBottom: `1px solid ${adminColors.border}`,
  },
  "& .MuiDataGrid-columnHeader": {
    backgroundColor: "#1e293b",
  },
  "& .MuiDataGrid-columnHeaderTitle": {
    color: "#f8fafc",
    fontWeight: 700,
    letterSpacing: "0.02em",
  },
  "& .MuiDataGrid-columnSeparator": {
    color: "rgba(148, 163, 184, 0.28)",
  },
  "& .MuiDataGrid-virtualScroller": {
    backgroundColor: adminColors.panelBg,
  },
  "& .MuiDataGrid-row": {
    backgroundColor: adminColors.panelBg,
    "&:hover": {
      backgroundColor: "rgba(30, 41, 59, 0.82)",
    },
  },
  "& .MuiDataGrid-row.Mui-selected": {
    backgroundColor: "rgba(30, 41, 59, 0.9)",
  },
  "& .MuiDataGrid-row.Mui-selected:hover": {
    backgroundColor: "rgba(51, 65, 85, 0.92)",
  },
  "& .MuiDataGrid-cell": {
    borderBottomColor: adminColors.borderMuted,
    color: adminColors.textStrong,
  },
  "& .MuiDataGrid-cell:focus, & .MuiDataGrid-columnHeader:focus, & .MuiDataGrid-cell:focus-within, & .MuiDataGrid-columnHeader:focus-within": {
    outline: "none",
  },
  "& .MuiDataGrid-withBorderColor": {
    borderColor: adminColors.border,
  },
  "& .MuiDataGrid-footerContainer": {
    backgroundColor: adminColors.panelElevated,
    color: adminColors.textSecondary,
    borderTopColor: adminColors.border,
  },
  "& .MuiTablePagination-root, & .MuiTablePagination-selectLabel, & .MuiTablePagination-displayedRows": {
    color: adminColors.textSecondary,
  },
  "& .MuiTablePagination-selectIcon, & .MuiTablePagination-actions button": {
    color: adminColors.textStrong,
  },
  "& .MuiDataGrid-overlay, & .MuiDataGrid-overlayWrapper, & .MuiDataGrid-overlayWrapperInner": {
    backgroundColor: adminColors.panelBg,
    color: adminColors.textSecondary,
  },
};
