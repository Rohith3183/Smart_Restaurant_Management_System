const SupportFooter = () => {
  return (
    <div
      className="card"
      style={{
        marginTop: "2rem",
        backgroundColor: "var(--primary-50)",
        borderColor: "var(--primary-200)"
      }}
    >
      <div
        style={{
          borderBottom: "1px solid var(--primary-200)",
          marginBottom: "0.75rem",
          paddingBottom: "0.5rem"
        }}
      >
        <h3 className="font-semibold" style={{ fontSize: "1rem" }}>
          Need Help? Contact Support
        </h3>
        <p
          style={{
            fontSize: "0.875rem",
            color: "var(--gray-600)",
            marginTop: "0.25rem"
          }}
        >
          “Great service is built on great support. We are just a call or mail away.”
        </p>
      </div>

      <div
        className="grid grid-cols-2"
        style={{ gap: "1rem", fontSize: "0.875rem" }}
      >
        <div>
          <p className="font-medium" style={{ marginBottom: "0.25rem" }}>
            Phone 🎧
          </p>
          <p>+91 9866548204</p>
          <p>+91 7863020024</p>
          <p>+91 6300586268</p>
        </div>
        <div>
          <p className="font-medium" style={{ marginBottom: "0.25rem" }}>
            E-mail 📧
          </p>
          <p>dhanushtemp9@gmail.com</p>
          <p>thannu3183@gmail.com</p>
          <p>rohithmerugu2423@gmail.com</p>
        </div>
      </div>
    </div>
  );
};

export default SupportFooter;
