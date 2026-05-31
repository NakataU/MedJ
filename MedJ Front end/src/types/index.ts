export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface DocumentListOutView {
  id: number;
  fileName: string;
  size: string;
  createdOn: string;
}

export interface DocumentOutView {
  id: number;
  fileName: string;
  contentType: string;
  path: string;
  checksum: string;
  size: string | number;
  uploadedByUserId: number;
  content: string | null;
}

export interface SpecialtyOutView {
  id: number;
  specialty: string;
}

export interface PractitionerOutView {
  id: number;
  firstName: string;
  lastName: string;
  specialty: SpecialtyOutView;
  specialization: string;
}

export interface AppointmentOutView {
  id: number;
  name: string;
  place: string;
  date: string | number[];
  practitioner: PractitionerOutView | null;
  appointmentDocuments: DocumentListOutView[];
}

// Category types
export type CategoryTarget = 'APPOINTMENT' | 'DOCUMENT';
export type CategoryType = 'DOCUMENT_TYPE' | 'MEDICAL_SPECIALTY' | 'MEDICAL_CATEGORY';

export interface CategoryOutView {
  id: number;
  label: string;
  target: CategoryTarget;
  categoryType: CategoryType;
}

// Input types for creating/updating
export interface PractitionerCreateInput {
  firstName: string;
  lastName: string;
  specialtyId: number;
  specialization: string;
}

// Matches backend AppointmentInView exactly
export interface AppointmentCreateInput {
  name: string;
  place: string;
  date: string;
  practitionerId?: number | null;
}

// Per-document category selection (one categoryId per file)
export interface DocumentCategoryInput {
  categoryId: number | null;
}

// ── Auth ─────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  role: 'ADMIN' | 'REGULAR';
  username: string;
  id: number;
}
