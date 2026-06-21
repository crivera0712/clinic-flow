import type { SxProps, Theme } from "@mui/material/styles";
import type { TextFieldProps } from "@mui/material/TextField";
import { colors } from "../../theme";

export const adminColors = {
  pageBg: colors.canvas,
  panelBg: colors.surface,
  panelElevated: colors.surfaceRaised,
  panelSelected: colors.surfaceHover,
  border: colors.borderSoft,
  borderMuted: "rgba(148, 163, 184, 0.08)",
  accent: colors.primary,
  accentSoft: "rgba(56, 189, 248, 0.16)",
  textStrong: colors.text,
  textSecondary: colors.textSecondary,
  textMuted: colors.textMuted,
};

export const adminTextFieldSx: SxProps<Theme> = {
  "& .MuiOutlinedInput-input:-webkit-autofill": {
    WebkitBoxShadow: `0 0 0 100px ${colors.canvas} inset`,
    WebkitTextFillColor: adminColors.textStrong,
    caretColor: adminColors.textStrong,
    borderRadius: "inherit",
  },
};

export const adminSelectProps: TextFieldProps["SelectProps"] = {
  MenuProps: {
    PaperProps: {
      sx: {
        backgroundColor: colors.surfaceRaised,
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
  "& .MuiDataGrid-cell:focus-visible, & .MuiDataGrid-columnHeader:focus-visible": {
    outline: `2px solid ${colors.primary}`,
    outlineOffset: -2,
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
