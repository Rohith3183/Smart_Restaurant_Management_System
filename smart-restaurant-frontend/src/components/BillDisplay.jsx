import { useState, useRef } from "react";
import Modal from "./Modal";

const BillDisplay = ({ bill, restaurantName, onClose, onPaid }) => {
  const [splitCount, setSplitCount] = useState(1);
  const [showSplitModal, setShowSplitModal] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const billRef = useRef(null); // USE REF instead of getElementById

  const amountPerPerson =
    splitCount > 1 ? (bill.finalAmount / splitCount).toFixed(2) : null;

  const handlePaid = async () => {
    if (window.confirm("Mark this order as PAID and close it?")) {
      await onPaid();
      onClose();
    }
  };

  const handleDownloadPDF = async () => {
    setDownloading(true);
    try {
      // Dynamically import html2pdf to avoid SSR issues
      const html2pdf = (await import("html2pdf.js")).default;

      const element = billRef.current;

      if (!element) {
        alert("Bill content not found. Please try again.");
        setDownloading(false);
        return;
      }

      const opt = {
        margin: [10, 10, 10, 10],           // top, left, bottom, right in mm
        filename: `Bill_OrderNo-${bill.orderId}.pdf`,
        image: { type: "jpeg", quality: 1 },
        html2canvas: {
          scale: 3,                         // higher scale = sharper PDF
          useCORS: true,
          logging: false,
          backgroundColor: "#ffffff",
        },
        jsPDF: {
          unit: "mm",
          format: "a4",
          orientation: "portrait",
        },
        pagebreak: { mode: "avoid-all" },   // prevent page breaks in content
      };

      await html2pdf().set(opt).from(element).save();
    } catch (err) {
      console.error("PDF generation failed:", err);
      alert("PDF download failed: " + err.message);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <Modal isOpen={true} onClose={onClose} title="Bill Receipt">

      {/* BILL CONTENT - captured by ref */}
      <div
        ref={billRef}
        style={{
          fontFamily: "monospace",
          backgroundColor: "#ffffff",
          padding: "1.5rem",
          borderRadius: "8px",
          maxWidth: "500px",
          margin: "0 auto",
        }}
      >
        {/* Restaurant Header */}
        <div
          style={{
            textAlign: "center",
            borderBottom: "2px solid #111827",
            paddingBottom: "1rem",
            marginBottom: "1rem",
          }}
        >
          <h2
            style={{
              fontSize: "1.5rem",
              fontWeight: "bold",
              margin: 0,
              color: "#111827",
            }}
          >
            {restaurantName}
          </h2>
          <p style={{ margin: "0.25rem 0 0", fontSize: "0.85rem", color: "#6b7280" }}>
            Tax Invoice
          </p>
        </div>

        {/* Bill Info */}
        <div style={{ marginBottom: "1rem", fontSize: "0.875rem", color: "#111827" }}>
          {[
            { label: "Bill No", value: `#${bill.id}` },
            { label: "Order No", value: `#${bill.orderId}` },
            {
              label: "Date",
              value: new Date(bill.createdAt).toLocaleDateString("en-IN"),
            },
            {
              label: "Time",
              value: new Date(bill.createdAt).toLocaleTimeString("en-IN", {
                hour: "2-digit",
                minute: "2-digit",
                second: "2-digit",
                hour12: true,
              }),
            },
          ].map((row) => (
            <div
              key={row.label}
              style={{
                display: "flex",
                justifyContent: "space-between",
                marginBottom: "0.3rem",
              }}
            >
              <span style={{ color: "#6b7280" }}>{row.label}</span>
              <span style={{ fontWeight: 600 }}>{row.value}</span>
            </div>
          ))}
        </div>

        {/* Divider */}
        <div
          style={{
            borderTop: "1px dashed #9ca3af",
            marginBottom: "1rem",
          }}
        />

        {/* Amount Breakdown */}
        <div style={{ marginBottom: "1rem", fontSize: "0.9rem", color: "#111827" }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              marginBottom: "0.5rem",
            }}
          >
            <span>Subtotal</span>
            <span>₹{parseFloat(bill.total).toFixed(2)}</span>
          </div>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              marginBottom: "0.5rem",
            }}
          >
            <span>GST (18%)</span>
            <span>₹{parseFloat(bill.tax).toFixed(2)}</span>
          </div>
          {parseFloat(bill.discount) > 0 && (
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                marginBottom: "0.5rem",
                color: "#16a34a",
              }}
            >
              <span>Discount</span>
              <span>- ₹{parseFloat(bill.discount).toFixed(2)}</span>
            </div>
          )}
        </div>

        {/* Total */}
        <div
          style={{
            borderTop: "2px solid #111827",
            paddingTop: "1rem",
            marginBottom: "1rem",
            display: "flex",
            justifyContent: "space-between",
            fontSize: "1.4rem",
            fontWeight: "bold",
            color: "#111827",
          }}
        >
          <span>TOTAL</span>
          <span style={{ color: "#0e7490" }}>
            ₹{parseFloat(bill.finalAmount).toFixed(2)}
          </span>
        </div>

        {/* Payment Method */}
        <div style={{ textAlign: "center", marginBottom: "1rem" }}>
          <span
            style={{
              display: "inline-block",
              backgroundColor: "#0e7490",
              color: "#ffffff",
              fontSize: "0.9rem",
              fontWeight: 600,
              padding: "0.4rem 1.2rem",
              borderRadius: "999px",
            }}
          >
            Payment: {bill.paymentMethod}
          </span>
        </div>

        {/* Split Bill Info (only if active) */}
        {splitCount > 1 && (
          <div
            style={{
              backgroundColor: "#f0f9ff",
              border: "1px solid #0e7490",
              borderRadius: "8px",
              padding: "1rem",
              marginBottom: "1rem",
              textAlign: "center",
            }}
          >
            <p style={{ margin: "0 0 0.5rem", fontWeight: 600, color: "#111827" }}>
              Split Among {splitCount} People
            </p>
            <p
              style={{
                margin: 0,
                fontSize: "1.5rem",
                fontWeight: "bold",
                color: "#0e7490",
              }}
            >
              ₹{amountPerPerson} per person
            </p>
          </div>
        )}

        {/* Footer */}
        <div
          style={{
            borderTop: "1px dashed #9ca3af",
            paddingTop: "1rem",
            textAlign: "center",
            fontSize: "0.75rem",
            color: "#6b7280",
          }}
        >
          <p style={{ margin: "0 0 0.25rem" }}>Thank you for dining with us!</p>
          <p style={{ margin: 0 }}>Visit again soon</p>
        </div>
      </div>

      {/* ACTION BUTTONS - NOT part of PDF */}
      <div
        style={{
          display: "flex",
          gap: "0.5rem",
          marginTop: "1.5rem",
        }}
      >
        <button
          onClick={() => setShowSplitModal(true)}
          className="btn btn-secondary"
          style={{ flex: 1 }}
        >
          Split Bill
        </button>

        <button
          onClick={handleDownloadPDF}
          className="btn btn-primary"
          style={{ flex: 1 }}
          disabled={downloading}
        >
          {downloading ? "Downloading..." : "Download PDF"}
        </button>

        <button
          onClick={handlePaid}
          className="btn btn-success"
          style={{ flex: 1 }}
        >
          Mark as Paid
        </button>
      </div>

      {/* Split Bill Modal */}
      <Modal
        isOpen={showSplitModal}
        onClose={() => setShowSplitModal(false)}
        title="Split Bill"
      >
        <div className="form-group">
          <label className="form-label">Number of People</label>
          <input
            type="number"
            min="1"
            max="20"
            className="form-input"
            value={splitCount}
            onChange={(e) => setSplitCount(parseInt(e.target.value) || 1)}
            style={{ fontSize: "1.5rem", textAlign: "center" }}
          />
        </div>

        {splitCount > 1 && (
          <div
            className="card text-center"
            style={{
              backgroundColor: "var(--success)",
              color: "white",
              padding: "1.5rem",
              marginTop: "1rem",
            }}
          >
            <p style={{ fontSize: "0.875rem", marginBottom: "0.5rem" }}>
              Each person pays
            </p>
            <p style={{ fontSize: "2.5rem", fontWeight: "bold" }}>
              ₹{amountPerPerson}
            </p>
          </div>
        )}

        <div
          className="flex gap-2 justify-end"
          style={{ marginTop: "1.5rem" }}
        >
          <button
            onClick={() => setShowSplitModal(false)}
            className="btn btn-primary"
          >
            Done
          </button>
        </div>
      </Modal>
    </Modal>
  );
};

export default BillDisplay;
