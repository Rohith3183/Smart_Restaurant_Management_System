import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import PublicMenuPage from "./pages/PublicMenuPage";
import PublicFeedbackPage from "./pages/PublicFeedbackPage";

// We'll create these in next steps
import OwnerDashboard from './pages/OwnerDashboard';
import AccountantDashboard from './pages/AccountantDashboard';
import ChefDashboard from './pages/ChefDashboard';
import WaiterDashboard from './pages/WaiterDashboard';
import InventoryDashboard from './pages/InventoryDashboard';
import RootAdminDashboard from './pages/RootAdminDashboard';
import FranchiseDashboard from './pages/FranchiseDashboard';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          <Route
            path="/owner/*"
            element={
              <ProtectedRoute allowedRoles={['OWNER']}>
                <OwnerDashboard />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/accountant"
            element={
              <ProtectedRoute allowedRoles={['ACCOUNTANT']}>
                <AccountantDashboard />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/chef"
            element={
              <ProtectedRoute allowedRoles={['CHEF']}>
                <ChefDashboard />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/waiter"
            element={
              <ProtectedRoute allowedRoles={['WAITER']}>
                <WaiterDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/inventory"
            element={
              <ProtectedRoute allowedRoles={['INVENTORY']}>
                <InventoryDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/root-admin"
            element={
              <ProtectedRoute allowedRoles={['OWNER']}>
                <RootAdminDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="franchise"
            element={
              <ProtectedRoute allowedRoles={['OWNER']}>
                <FranchiseDashboard />
              </ProtectedRoute>
            }
          />

          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          
          <Route path="/" element={<Navigate to="/login" replace />} />

          <Route path="/menu/:code" element={<PublicMenuPage />} />

          <Route path="/feedback/:code" element={<PublicFeedbackPage />} />

        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
