// import { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
// import { useAuth } from '../context/AuthContext';

// const LoginPage = () => {
//   const navigate = useNavigate();
//   const { login } = useAuth();
  
//   const [formData, setFormData] = useState({
//     restaurantCode: '',
//     username: '',
//     password: '',
//     role: 'OWNER',
//   });
  
//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState('');

//   const handleSubmit = async (e) => {
//   e.preventDefault();
//   setError('');
//   setLoading(true);

//   console.log('Login attempt:', formData);  // Debug log

//   const result = await login(formData);
  
//   console.log('Login result:', result);  // Debug log
  
//   if (result.success) {
//     // Get user data from localStorage to check restaurant code
//     const userData = JSON.parse(localStorage.getItem('user'));
//     console.log('User data:', userData);  // Debug log
    
//     // Check if ROOT admin
//     if (userData && userData.restaurantCode === 'ROOT3183') {
//       console.log('Navigating to root-admin');  // Debug log
//       navigate('/root-admin');
//     } else {
//       const roleRoutes = {
//         OWNER: '/owner',
//         ACCOUNTANT: '/accountant',
//         CHEF: '/chef',
//         WAITER: '/waiter',
//         INVENTORY: '/inventory',
//       };
//       console.log('Navigating to:', roleRoutes[formData.role]);  // Debug log
//       navigate(roleRoutes[formData.role]);
//     }
//   } else {
//     setError(result.error);
//   }
  
//   setLoading(false);
// };


//   return (
//     <div className="auth-container">
//       <div style={{ width: '100%', maxWidth: '28rem' }}>
//         <div className="text-center mb-8">
//           <div className="auth-logo" style={{ margin: '0 auto' }}>
//             <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
//               <path d="M6 13.87A4 4 0 0 1 7.41 6a5.11 5.11 0 0 1 1.05-1.54 5 5 0 0 1 7.08 0A5.11 5.11 0 0 1 16.59 6 4 4 0 0 1 18 13.87V21H6Z"/>
//               <line x1="6" y1="17" x2="18" y2="17"/>
//             </svg>
//           </div>
//           <h1 className="text-3xl font-bold" style={{ marginTop: '1rem' }}>Smart Restaurant</h1>
//           <p className="text-gray-600 mt-2">Management System</p>
//         </div>

//         <div className="auth-card">
//           <h2 className="text-2xl font-bold mb-6">Sign In</h2>
          
//           {error && (
//             <div className="alert alert-error">
//               {error}
//             </div>
//           )}

//           <form onSubmit={handleSubmit}>
//             <div className="form-group">
//               <label className="form-label">Restaurant Code</label>
//               <input
//                 type="text"
//                 className="form-input"
//                 placeholder="Enter restaurant code"
//                 value={formData.restaurantCode}
//                 onChange={(e) => setFormData({ ...formData, restaurantCode: e.target.value })}
//                 required
//               />
//             </div>

//             <div className="form-group">
//               <label className="form-label">Role</label>
//               <select
//                 className="form-select"
//                 value={formData.role}
//                 onChange={(e) => setFormData({ ...formData, role: e.target.value })}
//               >
//                 <option value="OWNER">Owner</option>
//                 <option value="ACCOUNTANT">Accountant</option>
//                 <option value="CHEF">Chef</option>
//                 <option value="WAITER">Waiter</option>
//                 <option value="INVENTORY">Inventory</option>
//               </select>
//             </div>

//             <div className="form-group">
//               <label className="form-label">Username</label>
//               <input
//                 type="text"
//                 className="form-input"
//                 placeholder="Enter username"
//                 value={formData.username}
//                 onChange={(e) => setFormData({ ...formData, username: e.target.value })}
//                 required
//               />
//             </div>

//             <div className="form-group">
//               <label className="form-label">Password</label>
//               <input
//                 type="password"
//                 className="form-input"
//                 placeholder="Enter password"
//                 value={formData.password}
//                 onChange={(e) => setFormData({ ...formData, password: e.target.value })}
//                 required
//               />
//             </div>

//             <div className="form-group">
//   <div className="flex justify-between items-center mb-2">
//     {formData.role === 'OWNER' && (
//       <button
//         type="button"
//         onClick={() => navigate('/forgot-password')}
//         style={{ 
//           background: 'none', 
//           border: 'none', 
//           color: 'var(--primary-600)', 
//           cursor: 'pointer', 
//           fontSize: '0.75rem',
//           fontWeight: 500
//         }}
//       >
//         Forgot Password?
//       </button>
//     )}
//   </div>
// </div>


//             <button
//               type="submit"
//               className="btn btn-primary"
//               style={{ width: '100%', marginTop: '0.5rem' }}
//               disabled={loading}
//             >
//               {loading ? 'Signing in...' : 'Sign In'}
//             </button>
//           </form>

//           <div className="text-center mt-6">
//             <button
//               onClick={() => navigate('/register')}
//               style={{ background: 'none', border: 'none', color: 'var(--primary-600)', cursor: 'pointer', fontSize: '0.875rem', fontWeight: 500 }}
//             >
//               New restaurant? Register here
//             </button>
//           </div>
//         </div>
//       </div>
//     </div>
//   );
// };

// export default LoginPage;

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    restaurantCode: '',
    username: '',
    password: '',
    role: 'OWNER',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
  e.preventDefault();
  setError('');
  setLoading(true);

  console.log('Login attempt:', formData);

  const result = await login(formData);

  console.log('Login result:', result);

  if (result.success) {
    const userData = JSON.parse(localStorage.getItem('user'));
    console.log('User data:', userData);

    if (userData) {
      if (userData.restaurantCode === 'ROOT3183') {
        navigate('/root-admin');
      } else if (userData.restaurantCode.startsWith('B-')) {
        // Franchise owner
        navigate('/franchise');
      } else {
        const roleRoutes = {
          OWNER: '/owner',
          ACCOUNTANT: '/accountant',
          CHEF: '/chef',
          WAITER: '/waiter',
          INVENTORY: '/inventory',
        };
        navigate(roleRoutes[formData.role]);
      }
    }
  } else {
    setError(result.error);
  }

  setLoading(false);
};

  return (
    <div className="auth-container">
      <div style={{ width: '100%', maxWidth: '28rem' }}>
        <div className="text-center mb-8">
          <div className="auth-logo" style={{ margin: '0 auto' }}>
            <svg
              width="32"
              height="32"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M6 13.87A4 4 0 0 1 7.41 6a5.11 5.11 0 0 1 1.05-1.54 5 5 0 0 1 7.08 0A5.11 5.11 0 0 1 16.59 6 4 4 0 0 1 18 13.87V21H6Z" />
              <line x1="6" y1="17" x2="18" y2="17" />
            </svg>
          </div>
          <h1 className="text-3xl font-bold" style={{ marginTop: '1rem' }}>
            Smart Restaurant
          </h1>
          <p className="text-gray-600 mt-2">Management System</p>
        </div>

        <div className="auth-card">
          <h2 className="text-2xl font-bold mb-6">Sign In</h2>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Restaurant Code</label>
              <input
                type="text"
                className="form-input"
                placeholder="Enter restaurant code"
                value={formData.restaurantCode}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    restaurantCode: e.target.value,
                  })
                }
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Role</label>
              <select
                className="form-select"
                value={formData.role}
                onChange={(e) =>
                  setFormData({ ...formData, role: e.target.value })
                }
              >
                <option value="OWNER">Owner</option>
                <option value="ACCOUNTANT">Accountant</option>
                <option value="CHEF">Chef</option>
                <option value="WAITER">Waiter</option>
                <option value="INVENTORY">Inventory</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Username</label>
              <input
                type="text"
                className="form-input"
                placeholder="Enter username"
                value={formData.username}
                onChange={(e) =>
                  setFormData({ ...formData, username: e.target.value })
                }
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-input"
                placeholder="Enter password"
                value={formData.password}
                onChange={(e) =>
                  setFormData({ ...formData, password: e.target.value })
                }
                required
              />
            </div>

            <div className="form-group">
              <div className="flex justify-between items-center mb-2">
                {formData.role === 'OWNER' && (
                  <button
                    type="button"
                    onClick={() => navigate('/forgot-password')}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--primary-600)',
                      cursor: 'pointer',
                      fontSize: '0.75rem',
                      fontWeight: 500,
                    }}
                  >
                    Forgot Password?
                  </button>
                )}
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', marginTop: '0.5rem' }}
              disabled={loading}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <div className="text-center mt-6">
            <button
              onClick={() => navigate('/register')}
              style={{
                background: 'none',
                border: 'none',
                color: 'var(--primary-600)',
                cursor: 'pointer',
                fontSize: '0.875rem',
                fontWeight: 500,
              }}
            >
              New restaurant? Register here
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
