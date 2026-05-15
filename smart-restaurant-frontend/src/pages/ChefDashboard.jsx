import { useState, useEffect } from 'react';
import DashboardLayout from '../components/DashboardLayout';
import { orderAPI, alertAPI } from '../services/api';
import SupportFooter from "../components/SupportFooter";

const ChefDashboard = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [issueMessage, setIssueMessage] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadOrders();
    const interval = setInterval(loadOrders, 5000); // Auto-refresh every 5 seconds
    return () => clearInterval(interval);
  }, []);

  const loadOrders = async () => {
    try {
      const res = await orderAPI.getKitchen();
      setOrders(res.data);
    } catch (err) {
      setError('Failed to load orders');
    }
  };

  const handleStartCooking = async (orderId) => {
    setLoading(true);
    try {
      await orderAPI.startCooking(orderId);
      loadOrders();
    } catch (err) {
      setError('Failed to start cooking');
    }
    setLoading(false);
  };

  const handleMarkReady = async (orderId) => {
    setLoading(true);
    try {
      await orderAPI.markReady(orderId);
      loadOrders();
    } catch (err) {
      setError('Failed to mark as ready');
    }
    setLoading(false);
  };

  const getOrderColor = (status, createdAt) => {
    const minutes = Math.floor((new Date() - new Date(createdAt)) / 60000);
    if (minutes > 30) return 'var(--error)';
    if (minutes > 15) return 'var(--warning)';
    return status === 'NEW' ? 'var(--info)' : 'var(--warning)';
  };

  const getWaitTime = (createdAt) => {
    const minutes = Math.floor((new Date() - new Date(createdAt)) / 60000);
    return `${minutes} min${minutes !== 1 ? 's' : ''}`;
  };

  const handleSendIssue = async () => {
  if (!issueMessage.trim()) {
    setError('Please enter an issue message');
    return;
  }
  setLoading(true);
  try {
    await alertAPI.create({ message: issueMessage, fromRole: 'CHEF' });
    setIssueMessage('');
    setSuccess('Issue sent to Owner successfully');
    setTimeout(() => setSuccess(''), 3000);
  } catch (err) {
    setError('Failed to send issue');
  }
  setLoading(false);
};

  return (
    <DashboardLayout title="Chef Dashboard - Kitchen Display">

      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">Kitchen Orders ({orders.length})</h2>
        <button onClick={loadOrders} className="btn btn-secondary">🔄 Refresh</button>
      </div>

      {orders.length === 0 ? (
        <div className="card text-center" style={{ padding: '4rem' }}>
          <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>👨‍🍳</div>
          <h3 className="text-xl font-bold text-gray-600">No pending orders</h3>
          <p className="text-gray-500 mt-2">New orders will appear here automatically</p>
        </div>
      ) : (
        <div className="grid grid-cols-3">
          {orders.map((order) => (
            <div
              key={order.id}
              className="card"
              style={{
                borderLeft: `4px solid ${getOrderColor(order.status, order.createdAt)}`,
                position: 'relative',
              }}
            >
              {/* Order Header */}
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="text-2xl font-bold">Order #{order.id}</h3>
                  <p className="text-sm text-gray-600">
                    {order.type === 'DINE_IN' ? `🪑 Table ${order.tableNumber}` : '📦 Parcel'}
                  </p>
                </div>
                <div className="text-right">
                  <span className={`badge badge-${order.status.toLowerCase().replace('_', '-')}`}>
                    {order.status}
                  </span>
                  <p className="text-sm font-bold mt-2" style={{ color: getOrderColor(order.status, order.createdAt) }}>
                    ⏱️ {getWaitTime(order.createdAt)}
                  </p>
                </div>
              </div>

              {/* Order Items */}
              <div style={{ marginBottom: '1rem' }}>
                <h4 className="font-semibold mb-2">Items:</h4>
                {order.items && order.items.map((item, idx) => (
                  <div key={idx} className="flex justify-between" style={{ padding: '0.5rem', background: 'var(--gray-50)', borderRadius: '0.5rem', marginBottom: '0.5rem' }}>
                    <span className="font-medium">{item.menuItemName}</span>
                    <span className="badge" style={{ backgroundColor: 'var(--primary-600)', color: 'white' }}>
                      x{item.quantity}
                    </span>
                  </div>
                ))}
              </div>

              {/* Action Buttons */}
              <div style={{ marginTop: '1rem' }}>
                {order.status === 'NEW' && (
                  <button
                    onClick={() => handleStartCooking(order.id)}
                    className="btn btn-primary"
                    style={{ width: '100%' }}
                    disabled={loading}
                  >
                    🔥 Start Cooking
                  </button>
                )}
                {order.status === 'IN_KITCHEN' && (
                  <button
                    onClick={() => handleMarkReady(order.id)}
                    className="btn btn-success"
                    style={{ width: '100%' }}
                    disabled={loading}
                  >
                    ✅ Mark as Ready
                  </button>
                )}
              </div>

              {/* Time Created */}
              <p className="text-xs text-gray-500 mt-2">
                Created: {new Date(order.createdAt).toLocaleTimeString()}
              </p>
            </div>
          ))}
        </div>
      )}
      
      <br></br>
      <br></br>
      <div className="card" style={{ marginBottom: '1.5rem', backgroundColor: 'var(--primary-50)' }}>
    <h3 className="font-semibold mb-2">📢 Report Issue to Owner</h3>
    <div className="flex gap-2">
      <input type="text" className="form-input" placeholder="Describe the issue..." value={issueMessage} onChange={(e) => setIssueMessage(e.target.value)} style={{ flex: 1 }} />
      <button onClick={handleSendIssue} className="btn btn-primary" disabled={loading}>Send Issue</button>
    </div>
  </div>

  {success && (
    <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
      {success}
      <button onClick={() => setSuccess('')} style={{ marginLeft: '1rem', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.5rem' }}>×</button>
    </div>
  )}
    </DashboardLayout>
  );
};

export default ChefDashboard;
