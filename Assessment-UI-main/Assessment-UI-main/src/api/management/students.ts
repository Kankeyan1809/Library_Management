import type { FineDetail, FineInput, Gender, Student, StudentFineResponse, StudentInput } from "../../types";
import { apiClient } from "../../lib/api";
import { API_ENDPOINTS } from "../../utils/apiEndpoint";

/** Student management API methods. */
export const studentsApi = {
  getAll(): Promise<Student[]> {
    return apiClient.request<Student[]>(API_ENDPOINTS.students);
  },

  getById(id: number): Promise<Student> {
    return apiClient.request<Student>(API_ENDPOINTS.studentById(id));
  },

  getByGender(gender: Gender): Promise<Student[]> {
    return apiClient.request<Student[]>(API_ENDPOINTS.studentsByGender(gender));
  },

  create(input: StudentInput): Promise<Student> {
    return apiClient.request<Student>(API_ENDPOINTS.students, {
      method: "POST",
      body: JSON.stringify(input),
    });
  },

  update(id: number, input: StudentInput): Promise<Student> {
    return apiClient.request<Student>(API_ENDPOINTS.studentById(id), {
      method: "PUT",
      body: JSON.stringify(input),
    });
  },

  remove(id: number): Promise<void> {
    return apiClient.request<void>(API_ENDPOINTS.studentById(id), { method: "DELETE" });
  },

  calculateFine(input: FineInput): Promise<StudentFineResponse> {
    return apiClient.request<StudentFineResponse>(API_ENDPOINTS.studentFine(input.studentId), {
      method: "POST",
      body: JSON.stringify({ lateDays: input.lateDays }),
    });
  },

  getFine(studentId: number): Promise<StudentFineResponse> {
    return apiClient.request<StudentFineResponse>(API_ENDPOINTS.studentFine(studentId));
  },

  payFine(studentId: number, recordId: number): Promise<StudentFineResponse> {
    return apiClient.request<StudentFineResponse>(API_ENDPOINTS.studentPayFine(studentId, recordId), {
      method: "POST",
    });
  },

  togglePayFine(studentId: number, recordId: number): Promise<StudentFineResponse> {
    return apiClient.request<StudentFineResponse>(API_ENDPOINTS.studentTogglePayFine(studentId, recordId), {
      method: "POST",
    });
  },
};
