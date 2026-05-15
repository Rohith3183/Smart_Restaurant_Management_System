import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import SupportFooter from "./SupportFooter";
import { useState, useEffect } from "react";

const DashboardLayout = ({ children, title }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [currentDateTime, setCurrentDateTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentDateTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const formatDateTime = (date) => {
    const options = {
      weekday: "short",
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    };
    return date.toLocaleString("en-IN", options);
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: 'var(--gray-50)' }}>
      <header className="dashboard-header">
        <div className="dashboard-header-content">
          <div className="flex items-center gap-3">
            <div className="dashboard-logo">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M6 13.87A4 4 0 0 1 7.41 6a5.11 5.11 0 0 1 1.05-1.54 5 5 0 0 1 7.08 0A5.11 5.11 0 0 1 16.59 6 4 4 0 0 1 18 13.87V21H6Z"/>
                <line x1="6" y1="17" x2="18" y2="17"/>
              </svg>
            </div>
            <div>
              <h1 className="text-xlD font-bold">{title}</h1>
              <p className="text-sm text-primary-500">{user?.restaurantName}</p>
            </div>
          </div>

          {/* RIGHT SIDE - ADD DATE/TIME HERE */}
          <div className="flex items-center gap-4">
            {/* ADD THIS DATE/TIME DISPLAY */}
            <div
              style={{
                padding: "0.5rem 0.75rem",
                backgroundColor: "var(--primary-50)",
                borderRadius: "0.5rem",
                border: "1px solid var(--primary-200)",
              }}
            >
              <p
                style={{
                  fontSize: "0.75rem",
                  fontWeight: 600,
                  color: "var(--primary-700)",
                  margin: 0,
                }}
              >
                {formatDateTime(currentDateTime)}
              </p>
            </div>
            </div>

          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-medium">{user?.username}</p>
              <p style={{ fontSize: '0.75rem', color: 'var(--c-mid)' }}>{user?.role}</p>
            </div>
            <button
              onClick={handleLogout}
              className="btn btn-secondary"
              style={{ padding: '0.5rem' }}
              title="Logout"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
            </button>
          </div>
        </div>
      </header>

      <main className="container py-8">
        {children}
        <SupportFooter />
      </main>
    </div>
  );
};

export default DashboardLayout;
