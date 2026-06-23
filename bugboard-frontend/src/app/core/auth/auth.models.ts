export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  username: string;
  email: string;
  role: 'ADMIN' | 'TECHNICAL' | 'EXTERNAL';
}

export interface UserRegistrationRequest extends AuthRequest {
  token: string;
  username: string;
}
