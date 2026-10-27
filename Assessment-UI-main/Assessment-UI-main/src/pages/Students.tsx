import { useEffect, useState } from "react";
import { Eye, Pencil, Plus, Search, Trash2, DollarSign } from "lucide-react";
import Modal from "../components/common/Modal";
import PageHeader from "../components/common/PageHeader";
import { studentsApi } from "../api/management/students";
import { borrowReturnApi } from "../api/management/borrowReturn";
import type { Book, BorrowRecord, Gender, Student, StudentFineResponse, StudentInput } from "../types";
import { formatCurrency } from "../utils/constants";

const emptyForm: StudentInput = { studentName: "", age: 0, gender: "" as Gender, course: "" };

const formatLKR = (amount: number) => `LKR ${amount.toFixed(2)}`;

export default function Students() {
  const [students, setStudents] = useState<Student[]>([]);
  const [records, setRecords] = useState<BorrowRecord[]>([]);
  const [books, setBooks] = useState<Book[]>([]);
  const [fineData, setFineData] = useState<StudentFineResponse | null>(null);
  const [isLoadingFine, setIsLoadingFine] = useState(false);

  const [search, setSearch] = useState("");
  const [gender, setGender] = useState<"ALL" | Gender>("ALL");
  const [modal, setModal] = useState<"add" | "edit" | "fine" | "view" | "delete" | null>(null);
  const [selected, setSelected] = useState<Student | null>(null);
  const [viewingStudent, setViewingStudent] = useState<Student | null>(null);
  const [isLoadingStudent, setIsLoadingStudent] = useState(false);
  const [form, setForm] = useState<StudentInput>(emptyForm);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const load = async () => {
    try {
      const [s, r, b] = await Promise.all([
        studentsApi.getAll(),
        borrowReturnApi.getRecords().catch(() => []),
        borrowReturnApi.getBooks().catch(() => []),
      ]);
      setStudents(s);
      setRecords(r);
      setBooks(b);
    } catch (err) {
      console.error("Failed to load students data:", err);
    }
  };

  useEffect(() => { void load(); }, []);

  const filtered = students.filter((s) =>
    (gender === "ALL" || s.gender === gender) &&
    `${s.studentName} ${s.course} ${s.studentId}`.toLowerCase().includes(search.toLowerCase())
  );

  const openAdd = () => {
    setError("");
    setSuccess("");
    setForm(emptyForm);
    setModal("add");
  };

  const openEdit = (student: Student) => {
    setError("");
    setSuccess("");
    setSelected(student);
    setForm({
      studentName: student.studentName,
      age: student.age,
      gender: student.gender,
      course: student.course,
    });
    setModal("edit");
  };

  const openView = async (studentId: number) => {
    setError("");
    setViewingStudent(null);
    setIsLoadingStudent(true);
    setModal("view");

    try {
      setViewingStudent(await studentsApi.getById(studentId));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not load student");
    } finally {
      setIsLoadingStudent(false);
    }
  };

  const openFineModal = async (student: Student) => {
    setSelected(student);
    setError("");
    setFineData(null);
    setIsLoadingFine(true);
    setModal("fine");
    try {
      const data = await studentsApi.getFine(student.studentId);
      setFineData(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load fine details");
    } finally {
      setIsLoadingFine(false);
    }
  };

  const togglePayRecord = async (recordId: number) => {
    if (!selected) return;
    try {
      setError("");
      const updated = await studentsApi.togglePayFine(selected.studentId, recordId);
      setFineData(updated);
      const updatedStudents = await studentsApi.getAll();
      setStudents(updatedStudents);
      setSuccess("Fine payment status updated successfully.");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to update payment");
    }
  };

  const saveStudent = async () => {
    try {
      setError("");
      if (!form.studentName.trim()) throw new Error("Student name is required");
      if (form.age <= 0) throw new Error("Age must be positive");
      if (!form.gender) throw new Error("Gender is required");
      if (!form.course.trim()) throw new Error("Course is required");

      const action = modal === "add" ? "Added" : "Updated";
      if (modal === "add") await studentsApi.create(form);
      if (modal === "edit" && selected) await studentsApi.update(selected.studentId, form);

      await load();
      setModal(null);
      setSuccess(`Successfully ${action} student record.`);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Operation failed");
    }
  };

  const removeStudent = async () => {
    if (!selected) return;
    try {
      setError("");
      const idToDelete = selected.studentId;
      await studentsApi.remove(idToDelete);
      setStudents((prev) => prev.filter((s) => s.studentId !== idToDelete));
      await load();
      setModal(null);
      setSuccess("Successfully deleted student record.");
      setSelected(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Delete failed");
    }
  };

  const filterGender = async (value: "ALL" | Gender) => {
    setGender(value);
    if (value === "ALL") {
      await load();
    } else {
      setStudents(await studentsApi.getByGender(value));
    }
  };

  return (
    <>
      <PageHeader
        title="Student Management"
        description="Register, search, update, delete and manage student fines."
        action={<button className="primary-button" onClick={openAdd}><Plus size={18} /> Register Student</button>}
      />

      <div className="toolbar student-toolbar">
        <div className="search-box student-search">
          <Search size={18} />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search by name, ID or course..." />
        </div>
        <select className="student-gender-filter" value={gender} onChange={(e) => filterGender(e.target.value as "ALL" | Gender)}>
          <option value="ALL">All Genders</option>
          <option value="M">Male</option>
          <option value="F">Female</option>
        </select>
      </div>

      {success && <div className="success-message" role="status">{success}</div>}

      <div className="table-card">
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>ID</th><th>Student Name</th><th>Age</th><th>Gender</th><th>Course</th><th>Fine Amount</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((student) => (
                <tr key={student.studentId}>
                  <td>#{student.studentId}</td>
                  <td className="strong">{student.studentName}</td>
                  <td>{student.age}</td>
                  <td><span className={`gender-badge ${student.gender === "M" ? "male" : "female"}`}>{student.gender}</span></td>
                  <td>{student.course}</td>
                  <td>{formatCurrency(student.fineAmount)}</td>
                  <td>
                    <div className="actions">
                      <button className="action-button" title="View Student" aria-label={`View ${student.studentName}`} onClick={() => openView(student.studentId)}><Eye size={16} /></button>
                      <button className="action-button" title="Edit" onClick={() => openEdit(student)}><Pencil size={16} /></button>
                      <button className="action-button fine" title="Calculate Fine" onClick={() => openFineModal(student)}><DollarSign size={16} /></button>
                      <button className="action-button danger" title="Delete" onClick={() => { setSelected(student); setError(""); setModal("delete"); }}><Trash2 size={16} /></button>
                    </div>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && <tr><td colSpan={7} className="empty">No students found.</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      <Modal open={modal === "add" || modal === "edit"} title={modal === "add" ? "Register Student" : "Update Student"} onClose={() => setModal(null)}>
        <StudentForm form={form} setForm={setForm} error={error} onSave={saveStudent} onCancel={() => setModal(null)} />
      </Modal>

      <Modal open={modal === "view"} title="Student Details" onClose={() => setModal(null)}>
        {isLoadingStudent && <div className="loading">Loading student...</div>}
        {error && <div className="error-message">{error}</div>}
        {viewingStudent && !isLoadingStudent && (
          <div className="form-grid">
            <div className="form-field"><label>Student ID</label><input value={viewingStudent.studentId} readOnly /></div>
            <div className="form-field"><label>Student Name</label><input value={viewingStudent.studentName} readOnly /></div>
            <div className="form-field"><label>Age</label><input value={viewingStudent.age} readOnly /></div>
            <div className="form-field"><label>Gender</label><input value={viewingStudent.gender} readOnly /></div>
            <div className="form-field full"><label>Course</label><input value={viewingStudent.course} readOnly /></div>
            <div className="form-field"><label>Fine Amount</label><input value={formatCurrency(viewingStudent.fineAmount)} readOnly /></div>
          </div>
        )}
        <div className="modal-footer">
          <button className="secondary-button" onClick={() => setModal(null)}>Close</button>
        </div>
      </Modal>

      <Modal open={modal === "delete"} title="Confirm Delete" width="small" onClose={() => setModal(null)}>
        <p>Delete {selected?.studentName}?</p>
        {error && <div className="error-message">{error}</div>}
        <div className="modal-footer">
          <button className="secondary-button" onClick={() => setModal(null)}>Cancel</button>
          <button className="primary-button" onClick={removeStudent}>Delete Student</button>
        </div>
      </Modal>

      {/* Calculate Fine Modal - Updated to match requested UI */}
      <Modal
        open={modal === "fine"}
        title="Calculate Fine"
        width="large"
        className="calculate-fine-modal"
        onClose={() => setModal(null)}
      >
        <h3 className="fine-student-name">{selected?.studentName}</h3>

        <div className="calculate-fine-container">
          <table className="calculate-fine-table">
            <thead>
              <tr>
                <th>BOOK TITLE</th>
                <th>BORROWED QUANTITY</th>
                <th>BORROW DATE</th>
                <th>DUE DATE</th>
                <th>RETURN DATE</th>
                <th>DAYS SINCE BORROWING</th>
                <th>OUTSTANDING QUANTITY</th>
                <th>LATE DAYS</th>
                <th>TOTAL FINE</th>
                <th>PAID AMOUNT</th>
                <th>UNPAID AMOUNT</th>
                <th>PAYMENT STATUS</th>
                <th>PAYMENT</th>
              </tr>
            </thead>
            <tbody>
              {isLoadingFine && (
                <tr>
                  <td colSpan={13} className="empty">
                    Loading fine records...
                  </td>
                </tr>
              )}
              {!isLoadingFine && fineData?.records.map((r) => (
                <tr key={r.borrowRecordId} className={r.paid ? "" : "unpaid-row"}>
                  <td>
                    {r.bookTitle ? (
                      <div>
                        <div className="strong">{r.bookTitle}</div>
                        <span className="subtext">Record #{r.borrowRecordId}</span>
                      </div>
                    ) : (
                      <span className="subtext">Record #{r.borrowRecordId}</span>
                    )}
                  </td>
                  <td>{r.borrowedQuantity}</td>
                  <td>{r.borrowDate}</td>
                  <td>{r.dueDate}</td>
                  <td>{r.returnDate ? r.returnDate : "—"}</td>
                  <td>{r.daysSinceBorrowing}</td>
                  <td>{r.outstandingQuantity}</td>
                  <td>{r.lateDays}</td>
                  <td>{formatLKR(r.totalFine)}</td>
                  <td>{formatLKR(r.paidAmount)}</td>
                  <td>{formatLKR(r.unpaidAmount)}</td>
                  <td className="strong">
                    <span className={`status-badge ${r.paid || r.paymentStatus === "PAID" || r.paymentStatus === "Paid" ? "paid" : (r.paidAmount > 0 ? "partial" : "unpaid")}`}>
                      {r.paymentStatus}
                    </span>
                  </td>
                  <td>
                    {r.paid || r.paymentStatus === "PAID" || r.paymentStatus === "Paid" ? (
                      <button type="button" className="pay-btn paid" disabled title="Fine has already been paid">
                        Paid
                      </button>
                    ) : (
                      <button
                        type="button"
                        className="pay-btn"
                        onClick={() => togglePayRecord(r.borrowRecordId)}
                      >
                        Pay
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {!isLoadingFine && (!fineData || fineData.records.length === 0) && (
                <tr>
                  <td colSpan={13} className="empty">
                    No borrow records found for this student.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="fine-summary-strip">
          <span>Total Fine: <strong>{formatLKR(fineData?.grandTotalFine ?? 0)}</strong></span>
          <span>Total Paid: <strong>{formatLKR(fineData?.grandTotalPaid ?? 0)}</strong></span>
          <span>Total Unpaid: <strong>{formatLKR(fineData?.grandTotalUnpaid ?? 0)}</strong></span>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="modal-footer" style={{ marginTop: "24px" }}>
          <button className="secondary-button" onClick={() => setModal(null)}>Close</button>
        </div>
      </Modal>
    </>
  );
}

function StudentForm({ form, setForm, error, onSave, onCancel }: {
  form: StudentInput;
  setForm: React.Dispatch<React.SetStateAction<StudentInput>>;
  error: string;
  onSave: () => void;
  onCancel: () => void;
}) {
  return (
    <>
      <div className="form-grid">
        <div className="form-field full">
          <label>Student Name</label>
          <input value={form.studentName} onChange={(e) => setForm({ ...form, studentName: e.target.value })} />
        </div>
        <div className="form-field">
          <label>Age</label>
          <input type="number" min="1" value={form.age} onChange={(e) => setForm({ ...form, age: Number(e.target.value) })} />
        </div>
        <div className="form-field">
          <label>Gender</label>
          <select value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value as Gender })}>
            <option value="" disabled>Select gender</option>
            <option value="M">Male (M)</option>
            <option value="F">Female (F)</option>
          </select>
        </div>
        <div className="form-field full">
          <label>Course</label>
          <input value={form.course} onChange={(e) => setForm({ ...form, course: e.target.value })} />
        </div>
      </div>
      {error && <div className="error-message">{error}</div>}
      <div className="modal-footer">
        <button className="secondary-button" onClick={onCancel}>Cancel</button>
        <button className="primary-button" onClick={onSave}>Save Student</button>
      </div>
    </>
  );
}
