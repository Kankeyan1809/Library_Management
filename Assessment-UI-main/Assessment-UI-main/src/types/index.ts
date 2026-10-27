export type Gender = "M" | "F";

export interface Student {
  studentId: number;
  studentName: string;
  age: number;
  gender: Gender;
  course: string;
  fineAmount: number;
}

export interface Book {
  bookId: number;
  bookTitle: string;
  author: string;
  category: string;
  availableQuantity: number;
}

export interface BorrowRecord {
  borrowRecordId: number;
  borrowDate: string;
  returnDate: string | null;
  numberOfBooks: number;
  initialQuantity?: number;
  paid?: boolean;
  paidAmount?: number;
  fineAmount?: number;
  balanceAmount?: number;
  outstandingBalance?: number;
  paymentStatus?: string;
  studentId: number;
  bookId: number;
}

export interface StudentInput {
  studentName: string;
  age: number;
  gender: Gender;
  course: string;
}

export interface BookInput {
  bookTitle: string;
  author: string;
  category: string;
  availableQuantity: number;
}

export interface BorrowInput {
  borrowRecordId?: number;
  studentId: number;
  bookId: number;
  numberOfBooks: number;
  borrowDate?: string;
  returnDate?: string;
  paid?: boolean;
  paidAmount?: number;
  paymentStatus?: string;
}

export interface FineInput {
  studentId: number;
  lateDays: number;
}

export interface FineDetail {
  borrowRecordId: number;
  bookId: number | null;
  bookTitle: string | null;
  borrowedQuantity: number;
  borrowDate: string;
  dueDate: string;
  returnDate: string | null;
  daysSinceBorrowing: number;
  outstandingQuantity: number;
  lateDays: number;
  fineForOneBook: number;
  baseFine: number;
  lateFine: number;
  totalFine: number;
  paidAmount: number;
  unpaidAmount: number;
  paymentStatus: string;
  paid: boolean;
}

export interface StudentFineResponse {
  studentId: number;
  studentName: string;
  grandTotalFine: number;
  grandTotalPaid: number;
  grandTotalUnpaid: number;
  records: FineDetail[];
}

export interface LibrarySummary {
  totalStudents: number;
  totalMaleStudents: number;
  totalFemaleStudents: number;
  totalBooksCurrentlyBorrowed: number;
  studentsBorrowing0Books: number;
  studentsBorrowing1Book: number;
  studentsBorrowing2Books: number;
  studentsBorrowing3Books: number;
  studentsBorrowing4Books: number;
  studentsBorrowing5Books: number;
  totalRegisteredBooks: number;
  totalAvailableBooks: number;
  totalBorrowedBooks: number;
}
