export type RoleName = "DISPLAY" | "ADMIN";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface JwtResponse {
  token: string;
}

export interface CurrentUser {
  id: number;
  username: string;
  roleName: RoleName;
  isDemo: boolean;
}
