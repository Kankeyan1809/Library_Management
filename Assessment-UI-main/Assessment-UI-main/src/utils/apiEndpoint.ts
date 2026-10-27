/** Base URL and API endpoint paths used by the application. */
export const API_BASE_URL = "http://52.231.71.47:8080";

export const API_ENDPOINTS = {
  students: "/api/v1/student",
  studentById: (id: number) => `/api/v1/student/${id}`,
  studentsByGender: (gender: string) => `/api/v1/student/gender/${gender}`,
  studentFine: (id: number) => `/api/v1/student/${id}/fine`,
  studentPayFine: (studentId: number, recordId: number) => `/api/v1/student/${studentId}/pay/${recordId}`,
  studentTogglePayFine: (studentId: number, recordId: number) => `/api/v1/student/${studentId}/pay-toggle/${recordId}`,
  books: "/api/v1/books",
  borrow: "/api/v1/borrow",
  return: "/api/v1/return",
  librarySummary: "/api/v1/library/summary",
} as const;
