import { useState, useEffect } from 'react';
import DashboardLayout from '../components/DashboardLayout';
import Modal from '../components/Modal';
import { tableAPI, menuAPI, orderAPI, billingAPI, alertAPI, investmentAPI } from '../services/api';
import BillDisplay from '../components/BillDisplay';
import { useAuth } from '../context/AuthContext';
import SupportFooter from "../components/SupportFooter";

const AccountantDashboard = () => {
  const [tables, setTables] = useState([]);
  const [menuItems, setMenuItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [showBillModal, setShowBillModal] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [newOrder, setNewOrder] = useState({ type: 'DINE_IN', tableId: '', items: [] });
  const [newBill, setNewBill] = useState({ orderId: '', discount: 0, paymentMethod: 'CASH', customerPhone: ""});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showBillDisplay, setShowBillDisplay] = useState(false);
  const [currentBill, setCurrentBill] = useState(null);
  const [issueMessage, setIssueMessage] = useState('');
  const [success, setSuccess] = useState('');
  const { user } = useAuth();
  const [investments, setInvestments] = useState([]);
  const [showInvestmentModal, setShowInvestmentModal] = useState(false);
  const [newInvestment, setNewInvestment] = useState({
    amount: "",
    description: "",
    type: "PURCHASE"
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [tablesRes, menuRes, ordersRes, invRes] = await Promise.all([
        tableAPI.getAll(),
        menuAPI.getItems(),
        orderAPI.getAll(),
        investmentAPI.getAll()
      ]);
      setTables(tablesRes.data);
      setMenuItems(menuRes.data);
      setOrders(ordersRes.data);
      setInvestments(invRes.data);
    } catch (err) {
      console.error("Accountant loadData error", err.response?.data || err.message);
      setError('Failed to load data');
    }
  };

  const handleAddItem = (menuItemId) => {
    const existingIndex = newOrder.items.findIndex(item => item.menuItemId === menuItemId);
    if (existingIndex >= 0) {
      const updated = [...newOrder.items];
      updated[existingIndex].quantity += 1;
      setNewOrder({ ...newOrder, items: updated });
    } else {
      setNewOrder({ ...newOrder, items: [...newOrder.items, { menuItemId, quantity: 1 }] });
    }
  };

  const handleRemoveItem = (menuItemId) => {
    setNewOrder({ ...newOrder, items: newOrder.items.filter(item => item.menuItemId !== menuItemId) });
  };

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    if (newOrder.items.length === 0) {
      setError('Please add at least one item');
      return;
    }
    setLoading(true);
    try {
      await orderAPI.create({
        type: newOrder.type,
        tableId: newOrder.type === 'DINE_IN' ? parseInt(newOrder.tableId) : null,
        items: newOrder.items,
      });
      setShowOrderModal(false);
      setNewOrder({ type: 'DINE_IN', tableId: '', items: [] });
      loadData();
    } catch (err) {
      setError('Failed to create order');
    }
    setLoading(false);
  };

  const handleCreateBill = async (e) => {
  e.preventDefault();
  setLoading(true);
  try {
    const response = await billingAPI.create({
      orderId: parseInt(newBill.orderId),
      discount: parseFloat(newBill.discount),
      paymentMethod: newBill.paymentMethod,
      customerPhone: newBill.customerPhone,
    });
    setShowBillModal(false);
    setCurrentBill(response.data);
    setShowBillDisplay(true);
    setNewBill({ orderId: '', discount: 0, paymentMethod: 'CASH', customerPhone: ""});
  } catch (err) {
    setError('Failed to create bill');
  }
  setLoading(false);
};

const handleMarkAsPaid = async () => {
  // Bill is already created and order is already closed by backend
  // Just reload data to refresh the UI
  await loadData();
  setShowBillDisplay(false);
  setCurrentBill(null);
};


  const getMenuItemById = (id) => menuItems.find(item => item.id === id);

  const calculateTotal = () => {
    return newOrder.items.reduce((sum, item) => {
      const menuItem = getMenuItemById(item.menuItemId);
      return sum + (menuItem ? menuItem.price * item.quantity : 0);
    }, 0);
  };

  const handleSendIssue = async () => {
  if (!issueMessage.trim()) {
    setError('Please enter an issue message');
    return;
  }
  setLoading(true);
  try {
    await alertAPI.create({ message: issueMessage, fromRole: 'ACCOUNTANT' });
    setIssueMessage('');
    setSuccess('Issue sent to Owner successfully');
    setTimeout(() => setSuccess(''), 3000);
  } catch (err) {
    setError('Failed to send issue');
  }
  setLoading(false);
};

const handleCreateInvestment = async (e) => {
  e.preventDefault();
  setLoading(true);
  setError("");
  try {
    await investmentAPI.create({
      ...newInvestment,
      amount: parseFloat(newInvestment.amount)
    });
    setShowInvestmentModal(false);
    setNewInvestment({ amount: "", description: "", type: "PURCHASE" });
    // refresh investments
    const res = await investmentAPI.getAll();
    setInvestments(res.data);
    setSuccess("Investment added successfully");
  } catch (err) {
    setError("Failed to add investment");
  } finally {
    setLoading(false);
  }
};

const handleDeleteInvestment = async (id) => {
  if (!window.confirm("Delete this investment?")) return;
  try {
    await investmentAPI.delete(id);
    const res = await investmentAPI.getAll();
    setInvestments(res.data);
    setSuccess("Investment deleted");
  } catch (err) {
    setError("Failed to delete investment");
  }
};

  return (
    <DashboardLayout title="Accountant Dashboard">

      <div className="flex gap-4 mb-6">
        <button onClick={() => setShowOrderModal(true)} className="btn btn-primary">+ Create Order</button>
        <button onClick={() => setShowBillModal(true)} className="btn btn-success">💳 Generate Bill</button>
        <button onClick={loadData} className="btn btn-secondary">🔄 Refresh</button>
      </div>

      {/* Tables Grid */}
      <h2 className="text-xl font-bold mb-4">Tables</h2>
      <div className="grid grid-cols-4 mb-8">
        {tables.map((table) => (
          <div key={table.id} className="card text-center" style={{ padding: '1rem' }}>
            <h3 className="text-lg font-bold">Table {table.tableNumber}</h3>
            <p className="text-sm text-gray-600">Capacity: {table.capacity}</p>
            <span className="badge" style={{ backgroundColor: table.status === 'FREE' ? 'var(--success)' : 'var(--error)', color: 'white', marginTop: '0.5rem' }}>
              {table.status}
            </span>
          </div>
        ))}
      </div>

      {/* Orders List */}
      <h2 className="text-xl font-bold mb-4">Orders</h2>
      <div className="card">
        {orders.length === 0 ? (
          <p className="text-center text-gray-600">No orders yet</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Type</th>
                <th>Table</th>
                <th>Status</th>
                <th>Amount</th>
                <th>Date</th>
                <th>Time</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>#{order.id}</td>
                  <td>{order.type}</td>
                  <td>{order.tableNumber ? `Table ${order.tableNumber}` : '-'}</td>
                  <td><span className={`badge badge-${order.status.toLowerCase().replace('_', '-')}`}>{order.status}</span></td>
                  <td>₹{order.totalAmount}</td>
                  <td>{new Date(order.createdAt).toLocaleDateString("en-IN")}</td>
                  <td>{new Date(order.createdAt).toLocaleTimeString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <br></br>
      <br></br>
      <div className="mt-8">
  <div className="flex justify-between items-center mb-6">
    <h2 className="text-2xl font-bold">Investment Tracking</h2>
    <button
      onClick={() => setShowInvestmentModal(true)}
      className="btn btn-primary"
    >
      + Add Investment
    </button>
  </div>

  <div className="card">
    {investments.length === 0 ? (
      <p className="text-center text-gray-600">
        No investments recorded yet
      </p>
    ) : (
      <table className="table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Amount</th>
            <th>Description</th>
            <th>Type</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {investments.map((inv) => (
            <tr key={inv.id}>
              <td>{new Date(inv.createdAt).toLocaleDateString()}</td>
              <td
                className="font-bold"
                style={{ color: "var(--error)" }}
              >
                {parseFloat(inv.amount).toFixed(2)}
              </td>
              <td>{inv.description}</td>
              <td>
                <span
                  className="badge"
                  style={{
                    backgroundColor:
                      inv.type === "SALARY"
                        ? "var(--warning)"
                        : inv.type === "PURCHASE"
                        ? "var(--info)"
                        : "var(--gray-600)",
                    color: "white"
                  }}
                >
                  {inv.type}
                </span>
              </td>
              <td>
                <button
                  onClick={() => handleDeleteInvestment(inv.id)}
                  className="btn btn-danger"
                  style={{
                    padding: "0.25rem 0.75rem",
                    fontSize: "0.75rem"
                  }}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    )}
  </div>
</div>

      {/* Create Order Modal */}
      <Modal isOpen={showOrderModal} onClose={() => setShowOrderModal(false)} title="Create Order">
        <form onSubmit={handleCreateOrder}>
          <div className="form-group">
            <label className="form-label">Order Type</label>
            <select className="form-select" value={newOrder.type} onChange={(e) => setNewOrder({ ...newOrder, type: e.target.value, tableId: '' })}>
              <option value="DINE_IN">Dine In</option>
              <option value="PARCEL">Parcel</option>
            </select>
          </div>

          {newOrder.type === 'DINE_IN' && (
            <div className="form-group">
              <label className="form-label">Select Table</label>
              <select className="form-select" value={newOrder.tableId} onChange={(e) => setNewOrder({ ...newOrder, tableId: e.target.value })} required>
                <option value="">Choose a table</option>
                {tables.filter(t => t.status === 'FREE').map((table) => (
                  <option key={table.id} value={table.id}>Table {table.tableNumber} (Capacity: {table.capacity})</option>
                ))}
              </select>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Menu Items</label>
            <div style={{ maxHeight: '200px', overflowY: 'auto', border: '1px solid var(--gray-200)', borderRadius: '0.5rem', padding: '0.5rem' }}>
              {menuItems.filter(item => item.available).map((item) => (
                <div key={item.id} className="flex justify-between items-center" style={{ padding: '0.5rem', borderBottom: '1px solid var(--gray-100)' }}>
                  <div>
                    <p className="font-medium">{item.name}</p>
                    <p className="text-sm text-gray-600">₹{item.price}</p>
                  </div>
                  <button type="button" onClick={() => handleAddItem(item.id)} className="btn btn-primary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem' }}>+ Add</button>
                </div>
              ))}
            </div>
          </div>

          {newOrder.items.length > 0 && (
            <div className="form-group">
              <label className="form-label">Selected Items</label>
              {newOrder.items.map((item) => {
                const menuItem = getMenuItemById(item.menuItemId);
                return menuItem ? (
                  <div key={item.menuItemId} className="flex justify-between items-center" style={{ padding: '0.5rem', background: 'var(--gray-50)', borderRadius: '0.5rem', marginBottom: '0.5rem' }}>
                    <span>{menuItem.name} x {item.quantity}</span>
                    <span>₹{(menuItem.price * item.quantity).toFixed(2)}</span>
                    <button type="button" onClick={() => handleRemoveItem(item.menuItemId)} style={{ background: 'none', border: 'none', color: 'var(--error)', cursor: 'pointer', fontSize: '1.5rem' }}>×</button>
                  </div>
                ) : null;
              })}
              <div className="flex justify-between font-bold" style={{ marginTop: '1rem', padding: '0.5rem', borderTop: '2px solid var(--gray-300)' }}>
                <span>Total:</span>
                <span>₹{calculateTotal().toFixed(2)}</span>
              </div>
            </div>
          )}

          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => setShowOrderModal(false)} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Creating...' : 'Create Order'}</button>
          </div>
        </form>
      </Modal>

      <Modal
  isOpen={showInvestmentModal}
  onClose={() => setShowInvestmentModal(false)}
  title="Add Investment"
>
  <form onSubmit={handleCreateInvestment}>
    <div className="form-group">
      <label className="form-label">Amount</label>
      <input
        type="number"
        step="0.01"
        className="form-input"
        placeholder="e.g., 5000"
        value={newInvestment.amount}
        onChange={(e) =>
          setNewInvestment({ ...newInvestment, amount: e.target.value })
        }
        required
      />
    </div>

    <div className="form-group">
      <label className="form-label">Description</label>
      <input
        type="text"
        className="form-input"
        placeholder="e.g., Purchased 10kg rice"
        value={newInvestment.description}
        onChange={(e) =>
          setNewInvestment({ ...newInvestment, description: e.target.value })
        }
        required
      />
    </div>

    <div className="form-group">
      <label className="form-label">Type</label>
      <select
        className="form-select"
        value={newInvestment.type}
        onChange={(e) =>
          setNewInvestment({ ...newInvestment, type: e.target.value })
        }
      >
        <option value="PURCHASE">Purchase</option>
        <option value="OTHER">Other</option>
      </select>
    </div>

    <div className="flex gap-2 justify-end">
      <button
        type="button"
        onClick={() => setShowInvestmentModal(false)}
        className="btn btn-secondary"
      >
        Cancel
      </button>
      <button
        type="submit"
        className="btn btn-primary"
        disabled={loading}
      >
        {loading ? "Adding..." : "Add Investment"}
      </button>
    </div>
  </form>
</Modal>

      {/* Generate Bill Modal */}
      <Modal isOpen={showBillModal} onClose={() => setShowBillModal(false)} title="Generate Bill">
        <form onSubmit={handleCreateBill}>
          <div className="form-group">
            <label className="form-label">Select Order (SERVED status only)</label>
            <select className="form-select" value={newBill.orderId} onChange={(e) => setNewBill({ ...newBill, orderId: e.target.value })} required>
              <option value="">Choose an order</option>
              {orders.filter(o => o.status === 'SERVED').map((order) => (
                <option key={order.id} value={order.id}>Order #{order.id} - ₹{order.totalAmount}</option>
              ))}
            </select>
          </div>

          {/* ADD THIS CUSTOMER PHONE FIELD */}
    <div className="form-group">
      <label className="form-label">Customer Phone (Optional)</label>
      <input
        type="tel"
        className="form-input"
        placeholder="10-digit mobile number"
        value={newBill.customerPhone || ""}
        onChange={(e) => setNewBill({ ...newBill, customerPhone: e.target.value })}
        maxLength="10"
        pattern="[0-9]{10}"
      />
      <p style={{ fontSize: "0.75rem", color: "var(--gray-500)", marginTop: "0.25rem" }}>
        Leave blank if customer doesn't wish to provide
      </p>
    </div>

          <div className="form-group">
            <label className="form-label">Discount (₹)</label>
            <input type="number" step="0.01" className="form-input" value={newBill.discount} onChange={(e) => setNewBill({ ...newBill, discount: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Payment Method</label>
            <select className="form-select" value={newBill.paymentMethod} onChange={(e) => setNewBill({ ...newBill, paymentMethod: e.target.value })}>
              <option value="CASH">Cash</option>
              <option value="CARD">Card</option>
              <option value="UPI">UPI</option>
            </select>
          </div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => setShowBillModal(false)} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-success" disabled={loading}>{loading ? 'Generating...' : 'Generate Bill'}</button>
          </div>
        </form>
      </Modal>
      {showBillDisplay && currentBill && (
        <BillDisplay
          bill={currentBill}
          restaurantName={user?.restaurantName || 'Restaurant'}
          onClose={() => setShowBillDisplay(false)}
          onPaid={handleMarkAsPaid}
        />
      )}

      <br></br>
      <br></br>
      <div className="card" style={{ marginBottom: '1.5rem', backgroundColor: 'var(--primary-50)' }}>
    <h3 className="font-semibold mb-2">📢 Report Issue to Owner</h3>
    <div className="flex gap-2">
      <input
        type="text"
        className="form-input"
        placeholder="Describe the issue..."
        value={issueMessage}
        onChange={(e) => setIssueMessage(e.target.value)}
        style={{ flex: 1 }}
      />
      <button onClick={handleSendIssue} className="btn btn-primary" disabled={loading}>
        Send Issue
      </button>
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

export default AccountantDashboard;
