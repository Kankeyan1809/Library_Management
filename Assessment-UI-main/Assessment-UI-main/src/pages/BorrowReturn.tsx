import { useEffect, useState } from "react";
import { ArrowDownToLine, ArrowUpFromLine } from "lucide-react";
import Modal from "../components/common/Modal";
import PageHeader from "../components/common/PageHeader";
import { borrowReturnApi } from "../api/management/borrowReturn";
import type { Book, BorrowRecord, Student } from "../types";

const getToday = () => {
  const today = new Date();
  const localDate = new Date(today.getTime() - today.getTimezoneOffset() * 60_000);
  return localDate.toISOString().slice(0, 10);
};

const parseDateParts = (d: string) => {
  if (!d) return null;
  const parts = d.split("-").map(Number);
  if (parts.length !== 3 || parts.some(isNaN)) return null;
  return new Date(parts[0], parts[1] - 1, parts[2]);
};

const formatDateIso = (date: Date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

export default function BorrowReturn() {
  const [students, setStudents] = useState<Student[]>([]);
  const [books, setBooks] = useState<Book[]>([]);
  const [records, setRecords] = useState<BorrowRecord[]>([]);
  const [modal, setModal] = useState<"borrow" | "return" | null>(null);

  // Borrow modal states
  const [borrowStudentId, setBorrowStudentId] = useState("");
  const [borrowBookId, setBorrowBookId] = useState("");
  const [borrowQuantity, setBorrowQuantity] = useState(1);
  const [borrowDate, setBorrowDate] = useState(getToday);

  // Return modal states
  const [returnStudentId, setReturnStudentId] = useState("");
  const [returnRecordId, setReturnRecordId] = useState("");
  const [returnQuantity, setReturnQuantity] = useState(1);
  const [returnDate, setReturnDate] = useState(getToday);
  const [paymentStatus, setPaymentStatus] = useState<"paid" | "unpaid">("unpaid");
  const [paidFineAmount, setPaidFineAmount] = useState<string>("0");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const load = async () => {
    try {
      const [s, b, r] = await Promise.all([
        borrowReturnApi.getStudents(),
        borrowReturnApi.getBooks(),
        borrowReturnApi.getRecords(),
      ]);

      setStudents(s);
      setBooks(b);
      setRecords(r);
    } catch (err) {
      console.error("Failed to load Borrow/Return data:", err);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const activeRecords = records.filter((r) => r.numberOfBooks > 0);
  const studentsWithActiveLoans = students.filter((student) =>
    activeRecords.some((record) => record.studentId === student.studentId),
  );

  const openBorrowModal = () => {
    setBorrowStudentId("");
    setBorrowBookId("");
    setBorrowQuantity(1);
    setBorrowDate(getToday());
    setError("");
    setSuccessMessage("");
    setModal("borrow");
  };

  const openReturnModal = () => {
    setError("");
    setSuccessMessage("");
    setReturnDate(getToday());

    if (activeRecords.length > 0) {
      const first = activeRecords[0];
      setReturnStudentId(String(first.studentId));
      setReturnRecordId(String(first.borrowRecordId));
      setReturnQuantity(first.numberOfBooks);
      const isPaid = first.paid === true || first.paymentStatus === "PAID";
      setPaymentStatus(isPaid ? "paid" : "unpaid");
      setPaidFineAmount("0");
    } else {
      setReturnStudentId("");
      setReturnRecordId("");
      setReturnQuantity(1);
      setPaymentStatus("unpaid");
      setPaidFineAmount("0");
    }
    setModal("return");
  };

  const openReturnForRecord = (record: BorrowRecord) => {
    setReturnStudentId(String(record.studentId));
    setReturnRecordId(String(record.borrowRecordId));
    setReturnQuantity(record.numberOfBooks);
    setReturnDate(getToday());
    const isPaid = record.paid === true || record.paymentStatus === "PAID";
    setPaymentStatus(isPaid ? "paid" : "unpaid");
    setPaidFineAmount("0");
    setError("");
    setSuccessMessage("");
    setModal("return");
  };

  const openReturnForRecordWithPayment = (record: BorrowRecord) => {
    setReturnStudentId(String(record.studentId));
    setReturnRecordId(String(record.borrowRecordId));
    setReturnQuantity(record.numberOfBooks);
    setReturnDate(getToday());
    setPaymentStatus("paid");
    setError("");
    setSuccessMessage("");
    setModal("return");
  };

  const handleReturnStudentChange = (id: string) => {
    setReturnStudentId(id);
    const recs = activeRecords.filter((r) => r.studentId === Number(id));
    if (recs.length > 0) {
      const rec = recs[0];
      setReturnRecordId(String(rec.borrowRecordId));
      setReturnQuantity(rec.numberOfBooks);
      const isPaid = rec.paid === true || rec.paymentStatus === "PAID";
      setPaymentStatus(isPaid ? "paid" : "unpaid");
      setPaidFineAmount("0");
    } else {
      setReturnRecordId("");
      setReturnQuantity(1);
      setPaymentStatus("unpaid");
      setPaidFineAmount("0");
    }
  };

  const handleReturnRecordChange = (recId: string) => {
    setReturnRecordId(recId);
    const rec = activeRecords.find((r) => r.borrowRecordId === Number(recId));
    if (rec) {
      setReturnQuantity(rec.numberOfBooks);
      const isPaid = rec.paid === true || rec.paymentStatus === "PAID";
      setPaymentStatus(isPaid ? "paid" : "unpaid");
      setPaidFineAmount("0");
    }
  };

  const submitBorrow = async () => {
    try {
      setError("");
      if (!borrowStudentId || !borrowBookId) throw new Error("Student and book are required");
      if (borrowQuantity <= 0) throw new Error("Number of books must be greater than 0");

      await borrowReturnApi.borrow({
        studentId: Number(borrowStudentId),
        bookId: Number(borrowBookId),
        numberOfBooks: borrowQuantity,
        borrowDate,
      });

      await load();
      setModal(null);
      setBorrowStudentId("");
      setBorrowBookId("");
      setBorrowQuantity(1);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Borrow operation failed");
    }
  };

  const submitReturn = async () => {
    try {
      setError("");
      setSuccessMessage("");
      if (!selectedReturnRecord) throw new Error("Please select a borrow record");
      if (returnQuantity <= 0) throw new Error("Number of books must be greater than 0");
      if (returnQuantity > selectedReturnRecord.numberOfBooks) {
        throw new Error(`Cannot return more than ${selectedReturnRecord.numberOfBooks} books`);
      }

      let parsedPaid = Number(paidFineAmount);
      if (isRecordAlreadyPaid || isZeroFine) {
        parsedPaid = 0;
      } else if (paymentStatus === "paid" && (isNaN(parsedPaid) || parsedPaid <= 0)) {
        parsedPaid = totalReturningCharge;
      }

      if (isNaN(parsedPaid) || parsedPaid < 0) {
        throw new Error("Please enter a valid non-negative paid fine amount");
      }

      setIsSubmitting(true);
      const res = await borrowReturnApi.return({
        borrowRecordId: selectedReturnRecord.borrowRecordId,
        studentId: selectedReturnRecord.studentId,
        bookId: selectedReturnRecord.bookId,
        numberOfBooks: returnQuantity,
        returnDate,
        paid: isRecordAlreadyPaid || isZeroFine || paymentStatus === "paid" || parsedPaid >= totalReturningCharge,
        paidAmount: parsedPaid,
        paymentStatus: isRecordAlreadyPaid || isZeroFine || parsedPaid >= totalReturningCharge ? "PAID" : (parsedPaid > 0 ? "PARTIALLY_PAID" : "UNPAID"),
      });

      await load();
      setModal(null);
      setSuccessMessage(
        `Successfully returned ${returnQuantity} book(s)${
          parsedPaid > 0 ? ` with fine payment of Rs. ${parsedPaid}` : ""
        }. Payment Status: ${res.paymentStatus || (isRecordAlreadyPaid || parsedPaid >= totalReturningCharge ? "PAID" : "UNPAID")}.`
      );
    } catch (e) {
      setError(e instanceof Error ? e.message : "Return operation failed");
    } finally {
      setIsSubmitting(false);
    }
  };

  // Calculations for Return Book modal
  const returnableRecordsForStudent = activeRecords.filter(
    (record) => record.studentId === Number(returnStudentId)
  );
  const selectedReturnRecord = activeRecords.find(
    (r) => r.borrowRecordId === Number(returnRecordId)
  );
  const selectedReturnStudent = students.find(
    (s) => s.studentId === Number(returnStudentId)
  );
  const selectedReturnBook = selectedReturnRecord
    ? books.find((b) => b.bookId === selectedReturnRecord.bookId)
    : undefined;

  const borrowDateObj = selectedReturnRecord ? parseDateParts(selectedReturnRecord.borrowDate) : null;
  const dueDateObj = borrowDateObj
    ? (() => {
        const d = new Date(borrowDateObj);
        d.setDate(d.getDate() + 3);
        return d;
      })()
    : null;
  const dueDateStr = dueDateObj ? formatDateIso(dueDateObj) : "-";

  const returnDateObj = parseDateParts(returnDate);

  const daysSinceBorrowing = borrowDateObj && returnDateObj
    ? Math.max(0, Math.round((returnDateObj.getTime() - borrowDateObj.getTime()) / (1000 * 60 * 60 * 24)))
    : 0;

  const lateDays = dueDateObj && returnDateObj
    ? Math.max(0, Math.round((returnDateObj.getTime() - dueDateObj.getTime()) / (1000 * 60 * 60 * 24)))
    : 0;

  const fineForOneBook = lateDays * 10;
  const fineForReturningQuantity = fineForOneBook * returnQuantity;
  const baseRecordCharge = selectedReturnRecord ? selectedReturnRecord.numberOfBooks * 50 : 0;
  const baseReturningCharge = returnQuantity * 50;
  const totalReturningCharge = baseReturningCharge + fineForReturningQuantity;

  const isRecordAlreadyPaid = Boolean(
    selectedReturnRecord &&
    (selectedReturnRecord.paid === true ||
     selectedReturnRecord.paymentStatus === "PAID" ||
     ((selectedReturnRecord.paidAmount ?? 0) >= totalReturningCharge && totalReturningCharge > 0))
  );
  const isZeroFine = totalReturningCharge === 0;
  const isFullyPaid = isRecordAlreadyPaid || isZeroFine || (paymentStatus === "paid" && Number(paidFineAmount) >= totalReturningCharge && totalReturningCharge > 0);

  useEffect(() => {
    if (modal === "return") {
      if (isRecordAlreadyPaid || isZeroFine) {
        setPaymentStatus("paid");
        setPaidFineAmount("0");
      } else if (paymentStatus === "paid") {
        setPaidFineAmount(String(totalReturningCharge));
      }
    }
  }, [modal, isRecordAlreadyPaid, isZeroFine, paymentStatus, totalReturningCharge]);

  return (
    <>
      <PageHeader
        title="Borrow / Return"
        description="Manage active book borrowing and returning records."
        action={
          <div className="header-actions">
            <button className="primary-button" onClick={openBorrowModal}><ArrowUpFromLine size={18} /> Borrow Book</button>
            <button className="secondary-button" onClick={openReturnModal}><ArrowDownToLine size={18} /> Return Book</button>
          </div>
        }
      />

      {successMessage && <div className="success-message" role="status">{successMessage}</div>}

      <div className="info-strip">
        <span>Maximum books per student: <strong>5</strong></span>
        <span>Active borrow records: <strong>{activeRecords.length}</strong></span>
      </div>

      <div className="table-card">
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Record ID</th>
                <th>Student</th>
                <th>Book</th>
                <th>Borrow Date</th>
                <th>Quantity</th>
                <th>Payment Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {activeRecords.map((record) => {
                const student = students.find((s) => s.studentId === record.studentId);
                const book = books.find((b) => b.bookId === record.bookId);
                const isPaid = record.paid === true || record.paymentStatus === "PAID";
                const isPartial = record.paymentStatus === "PARTIALLY_PAID";
                return (
                  <tr key={record.borrowRecordId}>
                    <td>#{record.borrowRecordId}</td>
                    <td className="strong">{student?.studentName ?? `Student #${record.studentId}`}</td>
                    <td>{book?.bookTitle ?? `Book #${record.bookId}`}</td>
                    <td>{record.borrowDate}</td>
                    <td>{record.numberOfBooks}</td>
                    <td>
                      <span className={`status-badge ${isPaid ? "paid" : (isPartial ? "partial" : "unpaid")}`}>
                        {isPaid ? "PAID" : (isPartial ? "PARTIALLY PAID" : "UNPAID")}
                      </span>
                    </td>
                    <td>
                      <div className="actions" style={{ display: "flex", gap: "8px", alignItems: "center" }}>
                        {isPaid ? (
                          <button className="pay-btn paid" disabled title="Fine has already been fully paid">Paid</button>
                        ) : (
                          <button className="pay-btn" onClick={() => openReturnForRecordWithPayment(record)} title="Pay Fine & Return">Pay Fine</button>
                        )}
                        <button className="secondary-button" onClick={() => openReturnForRecord(record)}>Return</button>
                      </div>
                    </td>
                  </tr>
                );
              })}
              {activeRecords.length === 0 && <tr><td colSpan={7} className="empty">No active borrow records.</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {/* Borrow Book Modal - Unchanged */}
      <Modal open={modal === "borrow"} title="Borrow Book" width="small" onClose={() => setModal(null)}>
        <div className="form-grid">
          <div className="form-field full">
            <label>Student</label>
            <select value={borrowStudentId} onChange={(e) => { setBorrowStudentId(e.target.value); setBorrowBookId(""); setBorrowQuantity(1); }}>
              <option value="">Select student</option>
              {students.map((s) => <option key={s.studentId} value={s.studentId}>#{s.studentId} - {s.studentName}</option>)}
            </select>
          </div>
          <div className="form-field full">
            <label>Book</label>
            <select value={borrowBookId} onChange={(e) => { setBorrowBookId(e.target.value); setBorrowQuantity(1); }}>
              <option value="">Select book</option>
              {books.map((b) => <option key={b.bookId} value={b.bookId}>#{b.bookId} - {b.bookTitle} ({b.availableQuantity} available)</option>)}
            </select>
          </div>
          <div className="form-field">
            <label>Number of Books</label>
            <input type="number" min="1" max="5" value={borrowQuantity} onChange={(e) => setBorrowQuantity(Number(e.target.value))} />
          </div>
          <div className="form-field">
            <label htmlFor="borrow-date">Borrow Date</label>
            <input id="borrow-date" type="date" value={borrowDate} onChange={(e) => setBorrowDate(e.target.value)} />
          </div>
        </div>
        {error && <div className="error-message">{error}</div>}
        <div className="modal-footer">
          <button className="secondary-button" onClick={() => setModal(null)}>Cancel</button>
          <button className="primary-button" onClick={submitBorrow}>Borrow Book</button>
        </div>
      </Modal>

      {/* Return Book Modal - Styled matching requested UI */}
      <Modal
        open={modal === "return"}
        title="Return Book"
        width="large"
        className="return-modal"
        onClose={() => setModal(null)}
      >
        <div className="return-form-stack">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "10px 14px", background: "#f8fafc", borderRadius: "8px", border: "1px solid #e5e9f1", marginBottom: "8px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span style={{ fontSize: "13px", fontWeight: 600, color: "#344054" }}>Payment Status:</span>
              <span className={`status-badge ${isFullyPaid ? "paid" : (paymentStatus === "paid" || Number(paidFineAmount) > 0 ? "partial" : "unpaid")}`}>
                {isFullyPaid ? "PAID" : (Number(paidFineAmount) > 0 ? "PARTIALLY PAID" : "UNPAID")}
              </span>
            </div>
            <div style={{ fontSize: "13px", color: "#475467" }}>
              <span>Total Fine Due: <strong>Rs. {isRecordAlreadyPaid ? 0 : totalReturningCharge}</strong></span>
              {Number(paidFineAmount) > 0 && !isRecordAlreadyPaid && (
                <span style={{ marginLeft: "14px" }}>Amount Paying: <strong>Rs. {paidFineAmount}</strong></span>
              )}
            </div>
          </div>

          <div className="form-field full">
            <label>Student</label>
            <select
              value={returnStudentId}
              onChange={(e) => handleReturnStudentChange(e.target.value)}
            >
              <option value="">Select student</option>
              {studentsWithActiveLoans.map((s) => (
                <option key={s.studentId} value={s.studentId}>
                  #{s.studentId} - {s.studentName}
                </option>
              ))}
            </select>
          </div>

          <div className="form-field full">
            <label>Borrow Record</label>
            <select
              value={returnRecordId}
              onChange={(e) => handleReturnRecordChange(e.target.value)}
            >
              <option value="">Select borrow record</option>
              {returnableRecordsForStudent.map((r) => (
                <option key={r.borrowRecordId} value={r.borrowRecordId}>
                  Book #{r.bookId} — {r.numberOfBooks} outstanding · borrowed {r.borrowDate} · record #{r.borrowRecordId}
                </option>
              ))}
            </select>
          </div>

          <div className="form-grid">
            <div className="form-field">
              <label>Number of Books</label>
              <input
                type="number"
                min="1"
                max={selectedReturnRecord?.numberOfBooks ?? 1}
                value={returnQuantity}
                onChange={(e) => {
                  const val = Number(e.target.value);
                  const maxVal = selectedReturnRecord?.numberOfBooks ?? 1;
                  setReturnQuantity(Math.max(1, Math.min(val, maxVal)));
                }}
              />
            </div>
            <div className="form-field">
              <label htmlFor="modal-return-date">Return Date</label>
              <input
                id="modal-return-date"
                type="date"
                value={returnDate}
                onChange={(e) => setReturnDate(e.target.value)}
              />
            </div>
            <div className="form-field full">
              <label htmlFor="modal-paid-fine">Fine Payment Amount (Rs.)</label>
              <input
                id="modal-paid-fine"
                type="number"
                min="0"
                step="any"
                value={paidFineAmount}
                placeholder="0"
                disabled={isRecordAlreadyPaid || isZeroFine}
                onChange={(e) => {
                  const val = e.target.value;
                  setPaidFineAmount(val);
                  const num = Number(val);
                  if (num >= totalReturningCharge && totalReturningCharge > 0) {
                    setPaymentStatus("paid");
                  } else if (num > 0) {
                    setPaymentStatus("unpaid");
                  } else {
                    setPaymentStatus("unpaid");
                  }
                }}
              />
              <span className="subtext" style={{ marginTop: "4px", display: "block" }}>
                Total fine for returning {returnQuantity} copy/copies: <strong>Rs. {totalReturningCharge}</strong> (Base: Rs. {baseReturningCharge} + Late: Rs. {fineForReturningQuantity})
                {isRecordAlreadyPaid && (
                  <span style={{ color: "#027a48", fontWeight: 600, display: "block", marginTop: "4px" }}>
                    ✓ Fine has already been fully paid (PAID). No additional payment required.
                  </span>
                )}
                {isZeroFine && !isRecordAlreadyPaid && (
                  <span style={{ color: "#027a48", fontWeight: 600, display: "block", marginTop: "4px" }}>
                    ✓ No fine applicable for this return.
                  </span>
                )}
              </span>
            </div>
          </div>

          <div>
            <span className="fine-details-label">Fine Details</span>
            <div className="fine-table-container">
              <table className="fine-table">
                <thead>
                  <tr>
                    <th>STUDENT NAME</th>
                    <th>BOOK TITLE</th>
                    <th>NUMBER RETURNING</th>
                    <th>BORROW DATE</th>
                    <th>DUE DATE</th>
                    <th>RETURN DATE</th>
                    <th>DAYS SINCE BORROWING</th>
                    <th>LATE DAYS</th>
                    <th>FINE FOR ONE BOOK</th>
                    <th>FINE FOR RETURNING QUANTITY</th>
                    <th>BASE BORROWING CHARGES</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedReturnRecord ? (
                    <tr>
                      <td>{selectedReturnStudent?.studentName ?? (returnStudentId ? `Student #${returnStudentId}` : "-")}</td>
                      <td>
                        {selectedReturnBook?.bookTitle && (
                          <div className="strong">{selectedReturnBook.bookTitle}</div>
                        )}
                        <span className="subtext">Record #{selectedReturnRecord.borrowRecordId}</span>
                      </td>
                      <td>{returnQuantity} ({selectedReturnRecord.numberOfBooks} currently outstanding)</td>
                      <td>{selectedReturnRecord.borrowDate}</td>
                      <td>{dueDateStr}</td>
                      <td>{returnDate}</td>
                      <td>{daysSinceBorrowing}</td>
                      <td>{lateDays}</td>
                      <td>Rs. {fineForOneBook}</td>
                      <td>Rs. {fineForReturningQuantity}</td>
                      <td>
                        <div>Rs. {baseRecordCharge}</div>
                        <div className="subtext">Returning copies: Rs. {baseReturningCharge}</div>
                      </td>
                    </tr>
                  ) : (
                    <tr>
                      <td colSpan={11} className="empty">
                        Select a student and borrow record to calculate fine details.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="return-modal-footer">
          {isFullyPaid ? (
            <button
              type="button"
              className="payment-toggle-btn paid-disabled"
              disabled
              title={isRecordAlreadyPaid ? "Fine has already been fully paid" : "Full fine amount is paid"}
            >
              Paid
            </button>
          ) : (
            <>
              <button
                type="button"
                className={`payment-toggle-btn ${paymentStatus === "paid" ? "selected" : ""}`}
                onClick={() => {
                  setPaymentStatus("paid");
                  setPaidFineAmount(String(totalReturningCharge));
                }}
              >
                Pay Fine (Rs. {totalReturningCharge})
              </button>
              <button
                type="button"
                className={`payment-toggle-btn ${paymentStatus === "unpaid" ? "selected" : ""}`}
                onClick={() => {
                  setPaymentStatus("unpaid");
                  setPaidFineAmount("0");
                }}
              >
                Unpaid
              </button>
            </>
          )}
          <button
            type="button"
            className="return-submit-btn"
            onClick={submitReturn}
            disabled={!selectedReturnRecord || isSubmitting}
          >
            {isSubmitting ? "Processing..." : "Return Book"}
          </button>
        </div>
      </Modal>
    </>
  );
}
