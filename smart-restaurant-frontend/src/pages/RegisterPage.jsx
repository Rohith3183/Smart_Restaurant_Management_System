import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { franchiseAPI } from '../services/api';
import '../PublicMenuPage.css';

const RegisterPage = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [formData, setFormData] = useState({
    name: '',
    code: '',
    location: '',
    contact: '',
    tableCount: '',
    capacity: '',
    ownerUsername: '',
    ownerPassword: '',
    ownerEmail: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [activeTab, setActiveTab] = useState('restaurant');

  const [isFranchise, setIsFranchise] = useState(false);
  const [franchiseCode, setFranchiseCode] = useState('');
  const [franchiseForm, setFranchiseForm] = useState({
    code: '',
    name: '',
    ownerEmail: '',
    ownerUsername: '',
    ownerPassword: '',
  });

  // Restaurant registration submit
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const payload = {
      ...formData,
      tableCount: parseInt(formData.tableCount) || 0,
      capacity: parseInt(formData.capacity) || 0,
      franchise: isFranchise,
      franchiseCode: isFranchise ? franchiseCode : null,
      // name must be non-blank to satisfy @NotBlank, but backend ignores it when franchise=true
      name: isFranchise ? 'FRANCHISE_OUTLET' : formData.name,
    };

    const result = await register(payload);

    if (result.success) {
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } else {
      setError(result.error);
    }

    setLoading(false);
  };

  // Franchise registration submit
  const handleFranchiseSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await franchiseAPI.register({
        ...franchiseForm,
        code: franchiseForm.code.toUpperCase(),
      });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(
        err.response?.data?.message || 'Franchise registration failed'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div style={{ width: '100%', maxWidth: '48rem' }}>
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
            Register
          </h1>
          <p className="text-gray-600 mt-2">
            Create your restaurant or franchise account
          </p>
        </div>

        <div className="card">
          <button
            onClick={() => navigate('/login')}
            className="btn btn-secondary mb-6"
            style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
          >
            <span>←</span> Back to Login
          </button>

          {error && <div className="alert alert-error">{error}</div>}
          {success && (
            <div className="alert alert-success">
              Registration successful! Redirecting to login...
            </div>
          )}

          {/* Tabs */}
          <br></br>
          <div className="register-tabs">
            <button
              type="button"
              className={
                'register-tab' + (activeTab === 'restaurant' ? ' active' : '')
              }
              onClick={() => setActiveTab('restaurant')}
            >
              Restaurant
            </button>
            <button
              type="button"
              className={
                'register-tab' + (activeTab === 'franchise' ? ' active' : '')
              }
              onClick={() => setActiveTab('franchise')}
            >
              Franchise
            </button>
          </div>

          {/* RESTAURANT TAB */}
          {activeTab === 'restaurant' && (
            <form onSubmit={handleSubmit}>
              {/* Franchise YES/NO slider */}
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label
                  className="form-label"
                  style={{ display: 'block', marginBottom: '0.5rem' }}
                >
                  Under Franchise ?
                </label>
                <div
                  className={
                    'yesno-toggle ' + (isFranchise ? 'yes' : 'no')
                  }
                  onClick={() => setIsFranchise((prev) => !prev)}
                >
                  <span className="yesno-toggle-label">
                    {isFranchise ? 'YES-.' : '.-NO'}
                  </span>
                  <div className="yesno-toggle-knob" />
                </div>
              </div>

              <h3 className="text-lg font-semibold mb-4">Restaurant Details</h3>
              <div className="grid grid-cols-2 gap-4 mb-6">
                {/* Restaurant Name – only when NOT franchise */}
                {!isFranchise && (
                  <div className="form-group">
                    <label className="form-label">Restaurant Name *</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g., Dhanu Restaurant"
                      value={formData.name}
                      onChange={(e) =>
                        setFormData({ ...formData, name: e.target.value })
                      }
                      required={!isFranchise}
                    />
                  </div>
                )}

                {/* Franchise Code – only when franchise */}
                {isFranchise && (
                  <div className="form-group">
                    <label className="form-label">Franchise Code *</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g., F1234"
                      value={franchiseCode}
                      onChange={(e) =>
                        setFranchiseCode(e.target.value.toUpperCase())
                      }
                      required={isFranchise}
                    />
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label">Unique Code *</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="e.g., DHANU001"
                    value={formData.code}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        code: e.target.value.toUpperCase(),
                      })
                    }
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Location</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="e.g., Hyderabad"
                    value={formData.location}
                    onChange={(e) =>
                      setFormData({ ...formData, location: e.target.value })
                    }
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Contact</label>
                  <input
                    type="tel"
                    className="form-input"
                    placeholder="e.g., 9876543210"
                    value={formData.contact}
                    onChange={(e) =>
                      setFormData({ ...formData, contact: e.target.value })
                    }
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Number of Tables</label>
                  <input
                    type="number"
                    className="form-input"
                    placeholder="e.g., 10"
                    value={formData.tableCount}
                    onChange={(e) =>
                      setFormData({ ...formData, tableCount: e.target.value })
                    }
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Total Capacity</label>
                  <input
                    type="number"
                    className="form-input"
                    placeholder="e.g., 40"
                    value={formData.capacity}
                    onChange={(e) =>
                      setFormData({ ...formData, capacity: e.target.value })
                    }
                  />
                </div>
              </div>

              <h3 className="text-lg font-semibold mb-4">Owner Account</h3>
              <div className="grid grid-cols-2 gap-4 mb-6">
                <div className="form-group">
                  <label className="form-label">Email *</label>
                  <input
                    type="email"
                    className="form-input"
                    placeholder="owner@example.com"
                    value={formData.ownerEmail}
                    onChange={(e) =>
                      setFormData({ ...formData, ownerEmail: e.target.value })
                    }
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Username *</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="Owner username"
                    value={formData.ownerUsername}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        ownerUsername: e.target.value,
                      })
                    }
                    required
                  />
                </div>

                <div className="form-group" style={{ gridColumn: 'span 2' }}>
                  <label className="form-label">Password *</label>
                  <input
                    type="password"
                    className="form-input"
                    placeholder="Strong password"
                    value={formData.ownerPassword}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        ownerPassword: e.target.value,
                      })
                    }
                    required
                  />
                </div>
              </div>

              <h3 className="text-lg font-semibold mb-4">Google Map Location</h3>
              <div className="grid grid-cols-2 gap-4 mb-6">
                <div className="form-group">
                  <label className="form-label">Link *</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="https://googlemaps.com"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ width: '100%' }}
                disabled={loading}
              >
                {loading ? 'Registering...' : 'Register Restaurant'}
              </button>
            </form>
          )}

          {/* FRANCHISE TAB */}
          {activeTab === 'franchise' && (
            <form onSubmit={handleFranchiseSubmit}>
              <h3 className="text-lg font-semibold mb-4">Franchise Details</h3>
              <div className="grid grid-cols-2 gap-4 mb-6">
                <div className="form-group">
                  <label className="form-label">Franchise Code *</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="e.g., F1234"
                    value={franchiseForm.code}
                    onChange={(e) =>
                      setFranchiseForm({
                        ...franchiseForm,
                        code: e.target.value.toUpperCase(),
                      })
                    }
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Franchise Name *</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="e.g., Dhanu Group"
                    value={franchiseForm.name}
                    onChange={(e) =>
                      setFranchiseForm({
                        ...franchiseForm,
                        name: e.target.value,
                      })
                    }
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Email *</label>
                  <input
                    type="email"
                    className="form-input"
                    placeholder="owner@example.com"
                    value={franchiseForm.ownerEmail}
                    onChange={(e) =>
                      setFranchiseForm({
                        ...franchiseForm,
                        ownerEmail: e.target.value,
                      })
                    }
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Username *</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="Franchise username"
                    value={franchiseForm.ownerUsername}
                    onChange={(e) =>
                      setFranchiseForm({
                        ...franchiseForm,
                        ownerUsername: e.target.value,
                      })
                    }
                    required
                  />
                </div>

                <div className="form-group" style={{ gridColumn: 'span 2' }}>
                  <label className="form-label">Password *</label>
                  <input
                    type="password"
                    className="form-input"
                    placeholder="Strong password"
                    value={franchiseForm.ownerPassword}
                    onChange={(e) =>
                      setFranchiseForm({
                        ...franchiseForm,
                        ownerPassword: e.target.value,
                      })
                    }
                    required
                  />
                </div>
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ width: '100%' }}
                disabled={loading}
              >
                {loading ? 'Registering...' : 'Register Franchise'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
