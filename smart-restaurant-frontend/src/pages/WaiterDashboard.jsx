import { useState, useEffect } from "react";
import DashboardLayout from "../components/DashboardLayout";
import Modal from "../components/Modal";               // ✅ ADD THIS
import { orderAPI, tableAPI, alertAPI, menuAPI } from "../services/api";
import SupportFooter from "../components/SupportFooter";
import { useAuth } from "../context/AuthContext";

const WaiterDashboard = () => {
  const [orders, setOrders] = useState([]);
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [issueMessage, setIssueMessage] = useState("");
  const [success, setSuccess] = useState("");
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [menuItems, setMenuItems] = useState([]);
  const [newOrder, setNewOrder] = useState({
    type: "DINE_IN",
    tableId: "",
    items: []                        // ✅ proper field
  });

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 5000); // Auto-refresh every 5 seconds
    return () => clearInterval(interval);
  }, []);

  const loadData = async () => {
    try {
      const [ordersRes, tablesRes, menuRes] = await Promise.all([
        orderAPI.getReady(),
        tableAPI.getAll(),
        menuAPI.getItems()
      ]);
      setOrders(ordersRes.data);
      setTables(tablesRes.data);
      setMenuItems(menuRes.data);
    } catch (err) {
      setError("Failed to load data");
    }
  };

  const handleMarkServed = async (orderId) => {
    setLoading(true);
    try {
      await orderAPI.markServed(orderId);
      loadData();
    } catch (err) {
      setError("Failed to mark as served");
    }
    setLoading(false);
  };

  const handleSendIssue = async () => {
    if (!issueMessage.trim()) {
      setError("Please enter an issue message");
      return;
    }
    setLoading(true);
    try {
      await alertAPI.create({ message: issueMessage, fromRole: "WAITER" });
      setIssueMessage("");
      setSuccess("Issue sent to Owner successfully");
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      setError("Failed to send issue");
    }
    setLoading(false);
  };

  const handleAddItem = (menuItemId) => {
    const existingIndex = newOrder.items.findIndex(
      (item) => item.menuItemId === menuItemId
    );
    if (existingIndex >= 0) {
      const updated = [...newOrder.items];
      updated[existingIndex] = {
        ...updated[existingIndex],
        quantity: updated[existingIndex].quantity + 1
      };
      setNewOrder({ ...newOrder, items: updated });
    } else {
      setNewOrder({
        ...newOrder,
        items: [...newOrder.items, { menuItemId, quantity: 1 }]
      });
    }
  };

  const handleRemoveItem = (menuItemId) => {
    setNewOrder({
      ...newOrder,
      items: newOrder.items.filter((item) => item.menuItemId !== menuItemId)
    });
  };

  const getMenuItemById = (id) =>
    menuItems.find((item) => item.id === id);

  const calculateTotal = () =>
    newOrder.items.reduce((sum, item) => {
      const menuItem = getMenuItemById(item.menuItemId);
      return sum + (menuItem ? menuItem.price * item.quantity : 0);
    }, 0);

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    if (newOrder.items.length === 0) {
      setError("Please add at least one item");
      return;
    }
    setLoading(true);
    setError("");
    try {
      await orderAPI.create({
        type: newOrder.type,
        tableId:
          newOrder.type === "DINE_IN" ? parseInt(newOrder.tableId) : null,
        items: newOrder.items
      });
      setShowOrderModal(false);
      setNewOrder({ type: "DINE_IN", tableId: "", items: [] });
      await loadData();
      setSuccess("Order created successfully");
    } catch (err) {
      console.error("Create order error", err.response?.data || err.message);
      setError("Failed to create order");
    } finally {
      setLoading(false);
    }
  };

  const { user } = useAuth();

const handleOpenPublicMenu = () => {
  if (!user?.restaurantCode) return;
  const url = `${window.location.origin}/menu/${user.restaurantCode}`;
  window.open(url, "_blank", "noopener,noreferrer");
};

const handleOpenFeedback = () => {
  if (!user?.restaurantCode) return;
  const url = `${window.location.origin}/feedback/${user.restaurantCode}`;
  window.open(url, "_blank", "noopener,noreferrer");
};

  return (
    <DashboardLayout title="Waiter Dashboard">
      {error && (
        <div className="alert alert-error" style={{ marginBottom: "1rem" }}>
          {error}
          <button
            onClick={() => setError("")}
            style={{
              marginLeft: "1rem",
              background: "none",
              border: "none",
              cursor: "pointer",
              fontSize: "1.5rem"
            }}
          >
            ×
          </button>
        </div>
      )}

      {success && (
        <div className="alert alert-success" style={{ marginBottom: "1rem" }}>
          {success}
          <button
            onClick={() => setSuccess("")}
            style={{
              marginLeft: "1rem",
              background: "none",
              border: "none",
              cursor: "pointer",
              fontSize: "1.5rem"
            }}
          >
            ×
          </button>
        </div>
      )}

      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold">Ready Orders {orders.length}</h2>
        <div className="flex gap-2">
          <button
            onClick={() => setShowOrderModal(true)}
            className="btn btn-primary"
          >
            + Create Order
          </button>
          <button
      onClick={handleOpenPublicMenu}
      className="btn btn-secondary"
      style={{
        backgroundColor: "var(--primary-600)",
        color: "white",
        padding: "0.4rem 0.9rem",
        borderRadius: "9999px",
        fontSize: "0.8rem"
      }}
    >
      View Menu
    </button>
    <button
      onClick={handleOpenFeedback}
      className="btn btn-primary"
      style={{ fontSize: "0.8rem" }}
    >
      Get Feedback
    </button>
          <button onClick={loadData} className="btn btn-secondary">
            Refresh
          </button>
        </div>
      </div>

      {/* Ready Orders */}
      <div className="mb-8">
        {orders.length === 0 ? (
          <div className="card text-center" style={{ padding: "3rem" }}>
            <div style={{ fontSize: "3rem", marginBottom: "1rem" }}>🍽️</div>
            <h3 className="text-xl font-bold text-gray-600">
              No orders ready to serve
            </h3>
            <p className="text-gray-500 mt-2">
              Ready orders will appear here
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-2">
            {orders.map((order) => (
              <div
                key={order.id}
                className="card"
                style={{ borderLeft: "4px solid var(--success)" }}
              >
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="text-xl font-bold">
                      Order #{order.id}
                    </h3>
                    <p
                      className="text-lg font-medium"
                      style={{ color: "var(--primary-600)" }}
                    >
                      {order.type === "DINE_IN"
                        ? `🪑 Table ${order.tableNumber}`
                        : "📦 Parcel"}
                    </p>
                  </div>
                  <span className="badge badge-ready">READY</span>
                </div>

                <div style={{ marginBottom: "1rem" }}>
                  <h4 className="font-semibold mb-2">Items:</h4>
                  {order.items &&
                    order.items.map((item, idx) => (
                      <div
                        key={idx}
                        className="flex justify-between"
                        style={{
                          padding: "0.5rem",
                          background: "var(--gray-50)",
                          borderRadius: "0.5rem",
                          marginBottom: "0.5rem"
                        }}
                      >
                        <span>{item.menuItemName}</span>
                        <span
                          className="badge"
                          style={{
                            backgroundColor: "var(--gray-600)",
                            color: "white"
                          }}
                        >
                          x{item.quantity}
                        </span>
                      </div>
                    ))}
                </div>

                <button
                  onClick={() => handleMarkServed(order.id)}
                  className="btn btn-success"
                  style={{
                    width: "100%",
                    fontSize: "1rem",
                    padding: "0.75rem"
                  }}
                  disabled={loading}
                >
                  ✅ Mark as Served
                </button>

                <p className="text-xs text-gray-500 mt-2">
                  Ready since:{" "}
                  {new Date(order.createdAt).toLocaleTimeString()}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Tables Status */}
      <h2 className="text-xl font-bold mb-4">Table Status</h2>
      <div className="grid grid-cols-4">
        {tables.map((table) => (
          <div
            key={table.id}
            className="card text-center"
            style={{ padding: "1rem" }}
          >
            <h3 className="text-lg font-bold">
              Table {table.tableNumber}
            </h3>
            <p className="text-sm text-gray-600">
              Capacity: {table.capacity}
            </p>
            <span
              className="badge"
              style={{
                backgroundColor:
                  table.status === "FREE"
                    ? "var(--success)"
                    : "var(--error)",
                color: "white",
                marginTop: "0.5rem"
              }}
            >
              {table.status}
            </span>
          </div>
        ))}
      </div>

      {/* Create Order Modal */}
      <Modal
        isOpen={showOrderModal}
        onClose={() => setShowOrderModal(false)}
        title="Create Order"
      >
        <form onSubmit={handleCreateOrder}>
          <div className="form-group">
            <label className="form-label">Order Type</label>
            <select
              className="form-select"
              value={newOrder.type}
              onChange={(e) =>
                setNewOrder({
                  ...newOrder,
                  type: e.target.value,
                  tableId: ""
                })
              }
            >
              <option value="DINE_IN">Dine In</option>
              <option value="PARCEL">Parcel</option>
            </select>
          </div>

          {newOrder.type === "DINE_IN" && (
            <div className="form-group">
              <label className="form-label">Select Table</label>
              <select
                className="form-select"
                value={newOrder.tableId}
                onChange={(e) =>
                  setNewOrder({
                    ...newOrder,
                    tableId: e.target.value
                  })
                }
                required
              >
                <option value="">Choose a table</option>
                {tables
                  .filter((t) => t.status === "FREE")
                  .map((table) => (
                    <option key={table.id} value={table.id}>
                      {`Table ${table.tableNumber} - Capacity ${table.capacity}`}
                    </option>
                  ))}
              </select>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Menu Items</label>
            <div
              style={{
                maxHeight: "200px",
                overflowY: "auto",
                border: "1px solid var(--gray-200)",
                borderRadius: "0.5rem",
                padding: "0.5rem"
              }}
            >
              {menuItems
                .filter((item) => item.available)
                .map((item) => (
                  <div
                    key={item.id}
                    className="flex justify-between items-center"
                    style={{
                      padding: "0.5rem",
                      borderBottom: "1px solid var(--gray-100)"
                    }}
                  >
                    <div>
                      <p className="font-medium">{item.name}</p>
                      <p className="text-sm text-gray-600">
                        {item.price}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleAddItem(item.id)}
                      className="btn btn-primary"
                      style={{
                        padding: "0.25rem 0.75rem",
                        fontSize: "0.75rem"
                      }}
                    >
                      Add
                    </button>
                  </div>
                ))}
            </div>
          </div>

          {newOrder.items.length > 0 && (
            <div className="form-group">
              <label className="form-label">Selected Items</label>
              {newOrder.items.map((item) => {
                const menuItem = getMenuItemById(item.menuItemId);
                if (!menuItem) return null;
                return (
                  <div
                    key={item.menuItemId}
                    className="flex justify-between items-center"
                    style={{
                      padding: "0.5rem",
                      background: "var(--gray-50)",
                      borderRadius: "0.5rem",
                      marginBottom: "0.5rem"
                    }}
                  >
                    <span>
                      {menuItem.name} x {item.quantity}
                    </span>
                    <span>
                      {(menuItem.price * item.quantity).toFixed(2)}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleRemoveItem(item.menuItemId)}
                      style={{
                        background: "none",
                        border: "none",
                        color: "var(--error)",
                        cursor: "pointer",
                        fontSize: "1.5rem"
                      }}
                    >
                      &times;
                    </button>
                  </div>
                );
              })}
              <div
                className="flex justify-between font-bold"
                style={{
                  marginTop: "1rem",
                  padding: "0.5rem",
                  borderTop: "2px solid var(--gray-300)"
                }}
              >
                <span>Total</span>
                <span>{calculateTotal().toFixed(2)}</span>
              </div>
            </div>
          )}

          <div className="flex gap-2 justify-end">
            <button
              type="button"
              onClick={() => setShowOrderModal(false)}
              className="btn btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? "Creating..." : "Create Order"}
            </button>
          </div>
        </form>
      </Modal>

      <br />

      <div
        className="card"
        style={{
          marginBottom: "1.5rem",
          backgroundColor: "var(--primary-50)"
        }}
      >
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
          <button
            onClick={handleSendIssue}
            className="btn btn-primary"
            disabled={loading}
          >
            Send Issue
          </button>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default WaiterDashboard;
