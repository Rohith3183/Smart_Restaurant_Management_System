import { useState, useEffect } from 'react';
import DashboardLayout from '../components/DashboardLayout';
import { rootAdminAPI } from '../services/api';

const RootAdminDashboard = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [franchises, setFranchises] = useState([]);

  useEffect(() => {
    loadRestaurants();
  }, []);

  const loadRestaurants = async () => {
  try {
    setLoading(true);
    const res = await rootAdminAPI.getAllRestaurants();
    setRestaurants(res.data);
    const fRes = await rootAdminAPI.getAllFranchises();
    setFranchises(fRes.data);
  } catch (err) {
    setError('Failed to load restaurants');
  } finally {
    setLoading(false);
  }
};

  const handleEnable = async (id) => {
    if (!window.confirm('Enable this restaurant? Users will be able to login.')) return;
    setLoading(true);
    try {
      await rootAdminAPI.enableRestaurant(id);
      setSuccess('Restaurant enabled successfully');
      loadRestaurants();
    } catch (err) {
      setError('Failed to enable restaurant');
    }
    setLoading(false);
  };

  const handleDisable = async (id) => {
    if (!window.confirm('Disable this restaurant? All users will be blocked from login.')) return;
    setLoading(true);
    try {
      await rootAdminAPI.disableRestaurant(id);
      setSuccess('Restaurant disabled successfully');
      loadRestaurants();
    } catch (err) {
      setError('Failed to disable restaurant');
    }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('⚠️ DELETE this restaurant permanently? This will remove ALL data including users, orders, bills, etc. This action CANNOT be undone!')) return;
    
    const confirmText = window.prompt('Type "DELETE" to confirm permanent deletion:');
    if (confirmText !== 'DELETE') {
      setError('Deletion cancelled');
      return;
    }

    setLoading(true);
    try {
      await rootAdminAPI.deleteRestaurant(id);
      setSuccess('Restaurant deleted permanently');
      loadRestaurants();
    } catch (err) {
      setError('Failed to delete restaurant');
    }
    setLoading(false);
  };

  const handleEnableFranchise = async (id) => {
  if (!window.confirm('Enable this franchise?')) return;
  try {
    await rootAdminAPI.enableFranchise(id);
    setSuccess('Franchise enabled successfully');
    loadRestaurants();
  } catch (err) {
    setError('Failed to enable franchise');
  }
};

const handleDisableFranchise = async (id) => {
  if (!window.confirm('Disable this franchise?')) return;
  try {
    await rootAdminAPI.disableFranchise(id);
    setSuccess('Franchise disabled successfully');
    loadRestaurants();
  } catch (err) {
    setError('Failed to disable franchise');
  }
};

const handleDeleteFranchise = async (id, name) => {
  if (
    !window.confirm(
      `Delete franchise "${name}" and ALL its restaurants? This cannot be undone.`
    )
  ) {
    return;
  }
  try {
    await rootAdminAPI.deleteFranchise(id);
    setSuccess('Franchise and all its restaurants deleted');
    loadRestaurants(); // reload both restaurants + franchises
  } catch (err) {
    setError(err.response?.status === 403
        ? 'Only ROOT admin can delete franchises'
        : 'Failed to delete franchise');
  }
};

  return (
    <DashboardLayout title="🔐 Root Admin - System Dashboard">
      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
          {error}
          <button onClick={() => setError('')} style={{ marginLeft: '1rem', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.5rem' }}>×</button>
        </div>
      )}

      {success && (
        <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
          {success}
          <button onClick={() => setSuccess('')} style={{ marginLeft: '1rem', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.5rem' }}>×</button>
        </div>
      )}

      <div className="card" style={{ marginBottom: '1.5rem', backgroundColor: 'var(--error)', color: 'white', padding: '1rem' }}>
        <p className="font-bold">⚠️ WARNING: You are in ROOT ADMIN mode. Actions here affect ALL restaurants in the system.</p>
      </div>

      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">All Restaurants ({restaurants.length})</h2>
        <button onClick={loadRestaurants} className="btn btn-secondary">🔄 Refresh</button>
      </div>

      {loading && restaurants.length === 0 ? (
        <div className="card text-center" style={{ padding: '3rem' }}>
          <div className="spinner" style={{ margin: '0 auto' }}></div>
          <p className="text-gray-600 mt-4">Loading restaurants...</p>
        </div>
      ) : restaurants.length === 0 ? (
        <div className="card text-center" style={{ padding: '3rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🏪</div>
          <h3 className="text-xl font-bold text-gray-600">No Restaurants Registered Yet</h3>
          <p className="text-gray-500 mt-2">Restaurants will appear here when they register</p>
        </div>
      ) : (
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Restaurant Name</th>
                <th>Code</th>
                <th>Location</th>
                <th>Contact</th>
                <th>Franchise</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {restaurants.map((restaurant) => (
                <tr key={restaurant.id}>
                  <td className="font-bold">#{restaurant.id}</td>
                  <td className="font-semibold">{restaurant.name}</td>
                  <td>
                    <span className="badge" style={{ backgroundColor: 'var(--primary-600)', color: 'white' }}>
                      {restaurant.code}
                    </span>
                  </td>
                  <td>{restaurant.location || '-'}</td>
                  <td>{restaurant.contact || '-'}</td>
                  <td>{restaurant.franchiseId || "Self"}</td>
                  <td>
                    {restaurant.enabled ? (
                      <span className="badge" style={{ backgroundColor: 'var(--success)', color: 'white' }}>
                        ✅ ENABLED
                      </span>
                    ) : (
                      <span className="badge" style={{ backgroundColor: 'var(--error)', color: 'white' }}>
                        🚫 DISABLED
                      </span>
                    )}
                  </td>
                  <td>
                    <div className="flex gap-2">
                      {restaurant.enabled ? (
                        <button
                          onClick={() => handleDisable(restaurant.id)}
                          className="btn btn-danger"
                          style={{ padding: '0.5rem 0.75rem', fontSize: '0.75rem' }}
                          disabled={loading}
                        >
                          🚫 Disable
                        </button>
                      ) : (
                        <button
                          onClick={() => handleEnable(restaurant.id)}
                          className="btn btn-success"
                          style={{ padding: '0.5rem 0.75rem', fontSize: '0.75rem' }}
                          disabled={loading}
                        >
                          ✅ Enable
                        </button>
                      )}
                      <button
                        onClick={() => handleDelete(restaurant.id)}
                        className="btn"
                        style={{ 
                          padding: '0.5rem 0.75rem', 
                          fontSize: '0.75rem',
                          backgroundColor: '#7f1d1d',
                          color: 'white'
                        }}
                        disabled={loading}
                      >
                        🗑️ Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <br></br>
          <br></br>
          <h2 className="text-2xl font-bold mt-10 mb-4">All Franchises ({franchises.length})</h2>
<div className="card">
  {franchises.length === 0 ? (
    <p className="text-center text-gray-600">No franchises registered yet.</p>
  ) : (
    <table className="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Franchise Name</th>
          <th>Code</th>
          <th>Owner Username</th>
          <th>Owner Email</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {franchises.map((f, idx) => (
          <tr key={f.id}>
            <td className='font-bold'>#{f.id}</td>
            <td>{f.name}</td>
            <td>
              <span className="badge" style={{ backgroundColor: 'var(--primary-600)', color: 'white' }}>{f.code}</span>
            </td>
            <td>{f.ownerUsername}</td>
            <td>{f.ownerEmail}</td>
            <td>
              <span
                className="badge"
                style={{
                  backgroundColor: f.enabled ? 'var(--success)' : 'var(--error)',
                  color: 'white',
                }}
              >
                {f.enabled ? '✅ ENABLED' : '🚫 DISABLED'}
              </span>
            </td>
            <td>
              <div className="flex gap-2">
                {f.enabled ? (
                  <button
                    onClick={() => handleDisableFranchise(f.id)}
                    className="btn btn-danger"
                    style={{ padding: '0.5rem 0.75rem', fontSize: '0.75rem' }}
                  >
                    🚫 Disable
                  </button>
                ) : (
                  <button
                    onClick={() => handleEnableFranchise(f.id)}
                    className="btn btn-success"
                    style={{ padding: '0.5rem 0.75rem', fontSize: '0.75rem' }}
                  >
                   ✅ Enable
                  </button>
                )}
                <button
      onClick={() => handleDeleteFranchise(f.id, f.name)}
      className="btn btn-danger"
      style={{ 
                          padding: '0.5rem 0.75rem', 
                          fontSize: '0.75rem',
                          backgroundColor: '#7f1d1d',
                          color: 'white'
                        }}
    >
     🗑️ Delete
    </button>
              </div>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )}
</div>

        </div>
      )}
    </DashboardLayout>
  );
};

export default RootAdminDashboard;
