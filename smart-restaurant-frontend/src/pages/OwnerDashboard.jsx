import { useState, useEffect, useRef } from 'react';
import DashboardLayout from '../components/DashboardLayout';
import Tabs from '../components/Tabs';
import Modal from '../components/Modal';
import { userAPI, alertAPI, analyticsAPI, investmentAPI, salaryAPI, employeeAPI, activityAPI, aiAPI, billAPI  } from '../services/api';
import SupportFooter from "../components/SupportFooter";
import { Line, Bar, Doughnut } from "react-chartjs-2";
import { useAuth } from "../context/AuthContext";
import "../index.css";
import { Mic, Instagram, Facebook, Youtube } from "lucide-react";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js';
// import { Line, Bar, Doughnut } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend);

const OwnerDashboard = () => {
  const [activeTab, setActiveTab] = useState('analytics');
  const [alerts, setAlerts] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [investments, setInvestments] = useState([]);
  const [salaries, setSalaries] = useState([]);
  const [users, setUsers] = useState([]);
  const [showInvestmentModal, setShowInvestmentModal] = useState(false);
  const [showUserModal, setShowUserModal] = useState(false);
  const [newInvestment, setNewInvestment] = useState({ amount: '', description: '', type: 'PURCHASE' });
  const [newUser, setNewUser] = useState({ username: '', password: '', role: 'ACCOUNTANT' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [editingUser, setEditingUser] = useState(null);
  const [showSalaryModal, setShowSalaryModal] = useState(false);
  const [editingSalary, setEditingSalary] = useState(null); // current row
  const [salaryForm, setSalaryForm] = useState({ salary: "", active: true });
  const [activities, setActivities] = useState([]);
  const [aiInsights, setAiInsights] = useState(null);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState("");
  const [chatMessages, setChatMessages] = useState([
  { role: "assistant", content: "Hi! I'm your AI assistant. Ask me about sales, staffing, menu, or costs." },]);
  const [chatInput, setChatInput] = useState("");
  const [chatLoading, setChatLoading] = useState(false);
  const [isAiChatOpen, setIsAiChatOpen] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const recognitionRef = useRef(null);
  const [bills, setBills] = useState([]);
  const [billLoading, setBillLoading] = useState(false);

  const tabs = [
    { id: 'analytics', label: '📊 Analytics' },
    { id: 'alerts', label: '🔔 Alerts' },
    { id: 'investments', label: '💰 Investments' },
    { id: 'salaries', label: '💳 Salaries' },
    { id: 'users', label: '👤 Users' },
    { id: 'bills', label: '💵 Bills' },
  ];

  useEffect(() => {
  if (activeTab === "analytics") {
    loadAnalytics();
    loadActivities();
    loadAiInsights();
  } else if (activeTab === "alerts") {
    loadAlerts();
  } else if (activeTab === "investments") {
    loadInvestments();
  } else if (activeTab === "salaries") {
    loadSalaries();
  } else if (activeTab === "users") {
    loadUsers();
  } else if (activeTab === 'bills') {
    loadBills();
  }
}, [activeTab]);

useEffect(() => {
  if (activeTab !== "analytics") return;

  loadActivities();
  const interval = setInterval(loadActivities, 5000); // every 5 s
  return () => clearInterval(interval);
}, [activeTab]);

  const loadAnalytics = async () => {
    try {
      const res = await analyticsAPI.get();
      console.log("Analytics from API:", res.data);
      setAnalytics(res.data);
    } catch (err) {
      setError('Failed to load analytics');
    }
  };

  const loadActivities = async () => {
  try {
    const res = await activityAPI.getRecent();
    setActivities(res.data);
  } catch (err) {
    console.error("Activity loading error", err);
  }
};

const loadAiInsights = async () => {
    try {
      setAiLoading(true);
      setAiError("");
      const res = await aiAPI.getSalesInsights();
      setAiInsights(res.data.summary);
    } catch (err) {
      setAiError("Failed to load AI insights");
    } finally {
      setAiLoading(false);
    }
  };

  const loadAlerts = async () => {
  try {
    const res = await alertAPI.getUnread();
    setAlerts(res.data || []);  // Ensure it's always an array
  } catch (err) {
    console.error('Alert loading error:', err);  // Log the actual error
    setError(err.response?.data?.message || 'Failed to load alerts');
    setAlerts([]);  // Set empty array on error
  }
};

  const loadInvestments = async () => {
    try {
      const res = await investmentAPI.getAll();
      setInvestments(res.data);
    } catch (err) {
      setError('Failed to load investments');
    }
  };

  const loadBills = async () => {
  try {
    setBillLoading(true);
    const res = await billAPI.getAll();
    setBills(Array.isArray(res.data) ? res.data : []);
  } catch (err) {
    console.error('Bill loading error:', err);
    setError('Failed to load bills');
    setBills([]);
  } finally {
    setBillLoading(false);
  }
};

  const loadSalaries = async () => {
    try {
      const res = await salaryAPI.getAll();
      setSalaries(res.data);
    } catch (err) {
      setError('Failed to load salaries');
    }
  };

  const loadUsers = async () => {
    try {
      const res = await userAPI.getAll();
      setUsers(res.data);
    } catch (err) {
      setError('Failed to load users');
    }
  };

  const openSocialPage = (url) => {
  window.open(url, '_blank', 'noopener,noreferrer');
};

const socialMediaLinks = [
  {
    name: 'Instagram',
    url: 'https://www.instagram.com/',
    color: 'linear-gradient(135deg, #f58529, #dd2a7b, #8134af, #515bd4)',
    icon: <Instagram size={26} color="white" />
  },
  {
    name: 'Facebook',
    url: 'https://www.facebook.com/',
    color: 'linear-gradient(135deg, #1877f2, #0d5fd3)',
    icon: <Facebook size={26} color="white" />
  },
  {
    name: 'X',
    url: 'https://x.com/',
    color: 'linear-gradient(135deg, #111827, #000000)',
    icon: (
      <span style={{ color: 'white', fontWeight: 800, fontSize: '1.2rem' }}>
        X
      </span>
    )
  },
  {
    name: 'YouTube',
    url: 'https://www.youtube.com/',
    color: 'linear-gradient(135deg, #ff0000, #cc0000)',
    icon: <Youtube size={26} color="white" />
  }
];

  const handleMarkAlertDone = async (id) => {
    try {
      await alertAPI.markDone(id);
      loadAlerts();
      setSuccess('Alert marked as done');
    } catch (err) {
      setError('Failed to mark alert as done');
    }
  };

  const handleCreateInvestment = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await investmentAPI.create({ ...newInvestment, amount: parseFloat(newInvestment.amount) });
      setShowInvestmentModal(false);
      setNewInvestment({ amount: '', description: '', type: 'PURCHASE' });
      loadInvestments();
      loadAnalytics();
      setSuccess('Investment added successfully');
    } catch (err) {
      setError('Failed to add investment');
    }
    setLoading(false);
  };

  const handleDeleteInvestment = async (id) => {
    if (!window.confirm('Delete this investment?')) return;
    try {
      await investmentAPI.delete(id);
      loadInvestments();
      loadAnalytics();
      setSuccess('Investment deleted');
    } catch (err) {
      setError('Failed to delete investment');
    }
  };

  const handlePaySalary = async (employeeId) => {
    if (!window.confirm('Pay salary for this employee? This will be auto-added to investments.')) return;
    setLoading(true);
    try {
      await salaryAPI.pay(employeeId);
      loadSalaries();
      loadInvestments();
      loadAnalytics();
      setSuccess('Salary paid and added to investments');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to pay salary');
    }
    setLoading(false);
  };

  const handleEditSalary = (sal) => {
    setEditingSalary(sal);
    setSalaryForm({
      salary: sal.salary,          // BigDecimal -> number
      active: true                 // or derive from an employee list if you show it
    });
    setShowSalaryModal(true);
  };

  const handleSaveSalary = async (e) => {
  e.preventDefault();
  if (!editingSalary) return;

  setLoading(true);
  setError("");
  try {
    await employeeAPI.update(editingSalary.employeeId, {
      name: editingSalary.employeeName,
      role: editingSalary.role,                 // already in SalaryDto
      salary: parseFloat(salaryForm.salary),
      active: salaryForm.active
    });
    setShowSalaryModal(false);
    setEditingSalary(null);
    // reload salaries and analytics (because salary affects salary/investment)
    await loadSalaries();
    await loadAnalytics();
    setSuccess("Salary updated successfully");
  } catch (err) {
    console.error("Update salary error", err.response?.data || err.message);
    setError("Failed to update salary");
  } finally {
    setLoading(false);
  }
};

const handleMicClick = () => {
    const SpeechRecognition =
      window.SpeechRecognition || window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
      alert("Sorry, your browser does not support Speech Recognition.");
      return;
    }

    // If already listening → stop
    if (isListening && recognitionRef.current) {
      recognitionRef.current.stop();
      setIsListening(false);
      return;
    }

    // Create recognition only once
    const recognition = new SpeechRecognition();
    recognition.lang = "en-US";
    recognition.continuous = false; // auto stop after speech
    recognition.interimResults = false;

    recognitionRef.current = recognition;

    recognition.onstart = () => {
      setIsListening(true);
    };

    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript;
      setChatInput(transcript);
    };

    recognition.onerror = () => {
      setIsListening(false);
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    recognition.start();
  };

const { user } = useAuth();

const handleOpenPublicMenu = () => {
  if (!user?.restaurantCode) return;
  const url = `${window.location.origin}/menu/${user.restaurantCode}`;
  window.open(url, "_blank", "noopener,noreferrer");
};

  const handleCreateUser = async (e) => {
  e.preventDefault();
  setLoading(true);
  try {
    if (editingUser) {
      // Update existing user
      await userAPI.update(editingUser.id, { 
        username: newUser.username, 
        password: newUser.password || undefined  // Only send if changed
      });
      setSuccess('User updated successfully');
    } else {
      // Create new user
      await userAPI.create(newUser);
      setSuccess('User created successfully');
    }
    setShowUserModal(false);
    setEditingUser(null);
    setNewUser({ username: '', password: '', role: 'ACCOUNTANT' });
    loadUsers();
  } catch (err) {
    setError(err.response?.data?.message || 'Failed to save user');
  }
  setLoading(false);
};

  const handleDeleteUser = async (id) => {
    if (!window.confirm('Delete this user? They will not be able to login.')) return;
    try {
      await userAPI.delete(id);
      loadUsers();
      setSuccess('User deleted');
    } catch (err) {
      setError('Failed to delete user');
    }
  };

  const handleEditUser = (user) => {
  setEditingUser(user);
  setNewUser({ username: user.username, password: '', role: user.role });
  setShowUserModal(true);
};

const sendChat = async () => {
  if (!chatInput.trim()) return;

  const newMessages = [
    ...chatMessages,
    { role: "user", content: chatInput.trim() },
  ];
  setChatMessages(newMessages);
  setChatInput("");
  setChatLoading(true);

  try {
    const res = await aiAPI.chat({ messages: newMessages });
    setChatMessages([
      ...newMessages,
      { role: "assistant", content: res.data.reply },
    ]);
  } catch (err) {
    setChatMessages([
      ...newMessages,
      {
        role: "assistant",
        content: "Sorry, I couldn't answer right now. Please try again.",
      },
    ]);
  } finally {
    setChatLoading(false);
  }
};

const totalBills = bills.length;

const totalBillRevenue = bills.reduce(
  (sum, bill) => sum + parseFloat(bill.finalAmount || 0),
  0
);

const averageBillValue = totalBills > 0 ? totalBillRevenue / totalBills : 0;

const highestBillAmount = bills.reduce(
  (max, bill) => Math.max(max, parseFloat(bill.finalAmount || 0)),
  0
);

const cashBills = bills.filter((bill) => bill.paymentMethod === 'CASH').length;
const cardBills = bills.filter((bill) => bill.paymentMethod === 'CARD').length;
const upiBills = bills.filter((bill) => bill.paymentMethod === 'UPI').length;

const totalDiscountGiven = bills.reduce(
  (sum, bill) => sum + parseFloat(bill.discount || 0),
  0
);

const totalTaxCollected = bills.reduce(
  (sum, bill) => sum + parseFloat(bill.tax || 0),
  0
);

  const getProfitColor = (profit) => {
    const val = parseFloat(profit);
    return val >= 0 ? 'var(--success)' : 'var(--error)';
  };

  const chartData = analytics?.profitLossGraph ? {
    labels: analytics.profitLossGraph.map(d => d.month),
    datasets: [
      {
        label: 'Revenue',
        data: analytics.profitLossGraph.map(d => d.revenue),
        borderColor: 'rgb(34, 197, 94)',
        backgroundColor: 'rgba(34, 197, 94, 0.1)',
        tension: 0.3,
      },
      {
        label: 'Investment',
        data: analytics.profitLossGraph.map(d => d.investment),
        borderColor: 'rgb(239, 68, 68)',
        backgroundColor: 'rgba(239, 68, 68, 0.1)',
        tension: 0.3,
      },
      {
        label: 'Profit',
        data: analytics.profitLossGraph.map(d => d.profit),
        borderColor: 'rgb(59, 130, 246)',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        tension: 0.3,
      },
    ],
  } : null;

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { position: 'top' },
      title: { display: true, text: 'Profit & Loss Trend (Last 6 Months)' },
    },
    scales: {
      y: { beginAtZero: true },
    },
  };

const peakHours = analytics?.peakHours || [];
const hourly = analytics?.hourlyOrdersRevenue || [];
const weekly = analytics?.weeklyRevenue || [];
const byCategory = analytics?.ordersByCategory || [];
const topItems = analytics?.topSellingItems || [];
const recentOrders = analytics?.recentOrders || [];

const hourlyChartData =
  hourly.length > 0
    ? {
        labels: hourly.map((d) => d.hourLabel || d.hour),
        datasets: [
          {
            label: "Orders",
            data: hourly.map((d) => d.orders),
            borderColor: "rgb(255, 132, 0)",
            backgroundColor: "rgba(255, 132, 0, 0.1)",
            yAxisID: "y"
          },
          {
            label: "Revenue (₹)",
            data: hourly.map((d) => d.revenue),
            borderColor: "rgb(25, 231, 224)",
            backgroundColor: "rgba(25, 231, 224, 0.1)",
            yAxisID: "y1"
          }
        ]
      }
    : null;

const hourlyChartOptions = {
  responsive: true,
  interaction: { mode: "index", intersect: false },
  scales: {
    y: { type: "linear", position: "left", beginAtZero: true },
    y1: {
      type: "linear",
      position: "right",
      beginAtZero: true,
      grid: { drawOnChartArea: false }
    }
  }
};

const weeklyRevenueData =
  weekly.length > 0
    ? {
        labels: weekly.map((d) => d.day),
        datasets: [
          {
            label: "Revenue",
            data: weekly.map((d) => d.revenue),
            backgroundColor: "rgba(41, 77, 135, 0.92)"
          }
        ]
      }
    : null;

const categoryData =
  byCategory.length > 0
    ? {
        labels: byCategory.map((c) => c.category),
        datasets: [
          {
            data: byCategory.map((c) => c.count),
            backgroundColor: [
              "#24384B",
              "#36526B",
              "#4A6E8C",
              "#5D8AA8",
              "#85B5D9"
            ]
          }
        ]
      }
    : null;

const topItemsChartData =
  topItems.length > 0
    ? {
        labels: topItems.map((i) => i.name),
        datasets: [
          {
            label: "Orders",
            data: topItems.map((i) => i.orders),
            backgroundColor: "rgba(41, 77, 135, 0.8)"
          }
        ]
      }
    : null;

  return (
    <DashboardLayout title="Owner Dashboard - Restaurant Monitoring">
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

      <Tabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} />

      {/* ANALYTICS TAB */}
      {activeTab === 'analytics' && analytics && (
        <div>

          <div className="flex justify-between items-center mb-4">
  <h2 className="text-2xl font-bold">MENU</h2>
  <button
    onClick={handleOpenPublicMenu}
    className="btn btn-secondary"
    style={{
      backgroundColor: "var(--primary-600)",
      color: "white",
      padding: "0.5rem 1rem",
      borderRadius: "9999px",
      fontSize: "0.875rem"
    }}
  >
    View Menu
  </button>
</div>
          
          <br></br>
          {/* Key Stats */}
          <h2 className="text-2xl font-bold mb-4">Key Metrics</h2>
          <div className="grid grid-cols-4" style={{ marginBottom: '2rem' }}>
            <div className="stat-card">
              <div className="stat-value">{analytics.totalOrders}</div>
              <div className="stat-label">Total Orders</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{analytics.totalMenuItems}</div>
              <div className="stat-label">Menu Items</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{analytics.totalEmployees}</div>
              <div className="stat-label">Employees</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{analytics.totalTables}</div>
              <div className="stat-label">Tables</div>
            </div>
          </div>

          <br></br>
          {/* Advanced Analytics cards */}
<h2 className="text-2xl font-bold mb-2">Advanced Analytics</h2>
<p className="text-sm text-gray-500 mb-4">
  Deep insights into your restaurant performance
</p>

<div className="grid grid-cols-4 gap-4 mb-8">
  <div className="stat-card">
    <p className="stat-label">Avg Order Value</p>
    <p className="stat-value">
      ₹{parseFloat(analytics?.avgOrderValue || 0).toFixed(1)}
    </p>
  </div>

  <div className="stat-card">
    <p className="stat-label">Table Turnover</p>
    <p className="stat-value">
      {parseFloat(analytics?.tableTurnover || 0).toFixed(1)}x
    </p>
  </div>

  <div className="stat-card">
  <div className="stat-label">Customer Satisfaction</div>
  <div className="stat-value">
    {analytics?.feedback ? (
      <>
        {analytics.feedback.restaurantRating.toFixed(1)}
        <span style={{ fontSize: "1.4rem" }}>⭐</span>
      </>
    ) : (
      <>0.0<span style={{ fontSize: "1.4rem" }}>⭐</span></>
    )}
  </div>
</div>


  <div className="stat-card">
    <p className="stat-label">Avg Wait Time</p>
    <p className="stat-value">
      {analytics?.avgWaitTimeMinutes || 0} min
    </p>
  </div>
</div>

{/* Live Updates */}
<br></br>
<h2 className="text-2xl font-bold mb-4">Live Updates</h2>
<div
  className="card"
  style={{
    maxHeight: "480px",
    overflowY: "auto",
    padding: "1rem 1.25rem",
    background: "linear-gradient(135deg, #0f172a, #020617)",
    color: "white",
    boxShadow: "0 18px 45px rgba(15, 23, 42, 0.7)",
    borderRadius: "1rem",
  }}
>
  {activities.length === 0 ? (
    <p style={{ color: "#9ca3af", fontSize: "0.85rem" }}>
      No recent activity in the last 2 days.
    </p>
  ) : (
    activities.map((a) => {
      const createdAt = new Date(a.createdAt);
      const now = new Date();
      const diffMs = now - createdAt;
      const isNew = diffMs < 5 * 60 * 1000; // 5 minutes

      const timeLabel = createdAt.toLocaleTimeString("en-IN", {
  timeZone: "Asia/Kolkata",
  hour: "2-digit",
  minute: "2-digit",
});

const dateLabel = createdAt.toLocaleDateString("en-IN", {
  timeZone: "Asia/Kolkata",
});

      const cleanMessage = a.message.replace(
    /([0-9]{2}:[0-9]{2}:[0-9]{2})\.[0-9]+/,
    "$1"
  )

      return (
        <div
          key={a.id}
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "flex-start",
            padding: "0.6rem 0",
            borderBottom: "1px solid rgba(148, 163, 184, 0.25)",
          }}
        >
          <div style={{ flex: 1, paddingRight: "0.75rem" }}>
            <div
              style={{
                fontSize: "0.85rem",
                fontWeight: 500,
                marginBottom: "0.15rem",
              }}
            >
              {a.message}
            </div>
            <div
              style={{
                fontSize: "0.75rem",
                color: "#9ca3af",
              }}
            >
              {a.actorName ? `By ${a.actorName} • ` : ""}
              {dateLabel} {timeLabel}
            </div>
          </div>
          {isNew && (
            <span
              style={{
                fontSize: "0.7rem",
                fontWeight: 700,
                padding: "0.2rem 0.5rem",
                borderRadius: "999px",
                background:
                  "linear-gradient(135deg, #22c55e, #16a34a, #3b82f6)",
                color: "#0f172a",
                whiteSpace: "nowrap",
              }}
            >
              NEW
            </span>
          )}
        </div>
      );
    })
  )}
</div>

{/* Peak hours + hourly orders/revenue */}
<br></br>
<h3 className="text-xl font-bold mb-2">Peak Hours Analysis</h3>
<p className="text-sm text-gray-500 mb-4">
  Busiest times of the day
</p>

<div className="grid grid-cols-3 gap-4 mb-6">
  {peakHours.slice(0, 3).map((p, idx) => (
    <div
      key={idx}
      className="card"
      style={{ backgroundColor: "var(--primary-50)" }}
    >
      <p className="text-xs text-gray-500 mb-1">
        #{idx + 1} Peak Hour
      </p>
      <p className="text-2xl font-bold text-orange-600">
        {p.hourLabel || `₹{p.hour}:00`}
      </p>
      <p className="text-sm text-gray-600">
        {p.orders} orders • ₹
        {parseFloat(p.revenue || 0).toFixed(0)}
      </p>
    </div>
  ))}
</div>

{hourlyChartData && (
  <div className="card mb-8">
    <Line data={hourlyChartData} options={hourlyChartOptions} />
  </div>
)}

{/* AI Sales Insight Box */}
<br></br>
<div className="card" style={{ marginTop: "1.5rem" }}>
  <div className="flex justify-between items-center mb-2">
    <h3 className="text-lg font-semibold">AI Sales Insights</h3>
    <button
      className="btn btn-secondary"
      onClick={() => loadAiInsights()}
      disabled={aiLoading}
    >
      {aiLoading ? "Refreshing..." : "Refresh"}
    </button>
  </div>

  {aiError && (
    <p className="text-sm text-red-600" style={{ marginBottom: "0.5rem" }}>
      {aiError}
    </p>
  )}

  {aiLoading && !aiInsights && <p>Generating insights...</p>}

  {aiInsights && (
    <div
      style={{
        whiteSpace: "pre-wrap",
        fontSize: "0.875rem",
        color: "var(--gray-700)",
      }}
    >
      {aiInsights}
    </div>
  )}
</div>

{/* Weekly revenue + orders by category */}
<br></br>
<div className="grid grid-cols-2 gap-4 mb-8">
  <div className="card">
    <h4 className="font-semibold mb-2">Weekly Revenue</h4>
    <p className="text-sm text-gray-500 mb-4">
      Revenue overview for the past week
    </p>
    {weeklyRevenueData ? (
      <Bar
        data={weeklyRevenueData}
        options={{
          responsive: true,
          plugins: { legend: { display: false } }
        }}
      />
    ) : (
      <p className="text-center text-gray-600">No weekly data yet</p>
    )}
  </div>

  <div className="card">
    <h4 className="font-semibold mb-2">Orders by Category</h4>
    <p className="text-sm text-gray-500 mb-4">
      Distribution of orders by menu category
    </p>
    {categoryData ? (
      <Doughnut data={categoryData}/>
    ) : (
      <p className="text-center text-gray-600">No category data yet</p>
    )}
  </div>
</div>

{/* Top selling items */}
<h3 className="text-xl font-bold mb-4">Top Selling Items</h3>

<div className="card mb-6">
  {topItemsChartData ? (
    <div style={{ height: "260px" }}>
      <Bar
        data={topItemsChartData}
        options={{
          indexAxis: "y",
          responsive: true,
          plugins: { legend: { display: false } }
        }}
      />
    </div>
  ) : (
    <p className="text-center text-gray-600">No item data yet</p>
  )}
</div>

<div className="card mb-8">
  {topItems.map((item, idx) => (
    <div
      key={item.name}
      className="flex justify-between items-center py-3 border-b border-gray-100 last:border-b-0"
    >
      <div>
        <p className="font-semibold">
          #{idx + 1} {item.name}
        </p>
        <p className="text-sm text-gray-500">
          {item.orders} orders
        </p>
      </div>
      <p className="font-bold">
        ₹{parseFloat(item.revenue || 0).toFixed(2)}
        <span className="text-xs text-gray-500" style={{ marginLeft: "0.4cm" }}>revenue</span>
      </p>
    </div>
  ))}
</div>

{/* Recent orders */}
<h3 className="text-xl font-bold mb-4">Recent Orders</h3>
<div className="card mb-8">
  {recentOrders.length === 0 ? (
    <p className="text-center text-gray-600">No recent orders</p>
  ) : (
    recentOrders.map((o) => (
      <div
        key={o.id}
        className="flex justify-between items-center py-3 border-b border-gray-100 last:border-b-0"
      >
        <div>
          <p className="font-semibold">
            Order #{o.id}
            {o.tableNumber && ` - Table`}
          </p>
          <p className="text-sm text-gray-500">
            {o.itemsCount} items • {o.customerName || "Walk-in"}
          </p>
        </div>
        <div className="text-right">
          <p className="font-bold">
            ₹{parseFloat(o.total || 0).toFixed(2)}
          </p>
          <p
            className="text-sm"
            style={{
              color:
                o.status === "PREPARING"
                  ? "var(--warning)"
                  : o.status === "READY"
                  ? "var(--primary-600)"
                  : o.status === "SERVE" || o.status === "SERVED"
                  ? "var(--success)"
                  : "var(--gray-600)"
            }}
          >
            {o.status}
          </p>
        </div>
      </div>
    ))
  )}
</div>

          <br></br>
          {/* Profit/Loss Cards */}
<h2 className="text-2xl font-bold mb-4">Profit & Loss</h2>
<div className="grid grid-cols-3" style={{ marginBottom: '2rem' }}>
  <div className="card">
    <h3 className="font-semibold mb-2">This Week</h3>
    <p className="text-sm text-gray-600">Revenue: ₹{parseFloat(analytics.weekRevenue).toFixed(2)}</p>
    <p className="text-sm text-gray-600">Investment: ₹{parseFloat(analytics.weekInvestment).toFixed(2)}</p>
    {parseFloat(analytics.weekProfit) >= 0 ? (
      <p className="text-xl font-bold mt-2" style={{ color: 'var(--success)' }}>
        Profit: ₹{parseFloat(analytics.weekProfit).toFixed(2)}
      </p>
    ) : (
      <p className="text-xl font-bold mt-2" style={{ color: 'var(--error)' }}>
        Loss: ₹{Math.abs(parseFloat(analytics.weekProfit)).toFixed(2)}
      </p>
    )}
  </div>
  <div className="card">
    <h3 className="font-semibold mb-2">This Month</h3>
    <p className="text-sm text-gray-600">Revenue: ₹{parseFloat(analytics.monthRevenue).toFixed(2)}</p>
    <p className="text-sm text-gray-600">Investment: ₹{parseFloat(analytics.monthInvestment).toFixed(2)}</p>
    {parseFloat(analytics.monthProfit) >= 0 ? (
      <p className="text-xl font-bold mt-2" style={{ color: 'var(--success)' }}>
        Profit: ₹{parseFloat(analytics.monthProfit).toFixed(2)}
      </p>
    ) : (
      <p className="text-xl font-bold mt-2" style={{ color: 'var(--error)' }}>
        Loss: ₹{Math.abs(parseFloat(analytics.monthProfit)).toFixed(2)}
      </p>
    )}
  </div>
  <div className="card">
    <h3 className="font-semibold mb-2">This Year</h3>
    <p className="text-sm text-gray-600">Revenue: ₹{parseFloat(analytics.yearRevenue).toFixed(2)}</p>
    <p className="text-sm text-gray-600">Investment: ₹{parseFloat(analytics.yearInvestment).toFixed(2)}</p>
    {parseFloat(analytics.yearProfit) >= 0 ? (
      <p className="text-xl font-bold mt-2" style={{ color: 'var(--success)' }}>
        Profit: ₹{parseFloat(analytics.yearProfit).toFixed(2)}
      </p>
    ) : (
      <p className="text-xl font-bold mt-2" style={{ color: 'var(--error)' }}>
        Loss: ₹{Math.abs(parseFloat(analytics.yearProfit)).toFixed(2)}
      </p>
    )}
  </div>
</div>

          {/* Graph */}
          {chartData && (
            <div className="card" style={{ marginBottom: '2rem' }}>
              <Line data={chartData} options={chartOptions} />
            </div>
          )}
          
          <br></br>
          {/* Customer Feedback */}
<h2 className="text-2xl font-bold mb-4">Customer Feedback</h2>
<div className="card" style={{ marginBottom: "2rem" }}>
  {!analytics?.feedback ? (
    <p className="text-gray-600 text-sm">
      No feedback received yet.
    </p>
  ) : (
    <>
      <div className="flex justify-between items-center mb-4">
        <div>
          <p className="text-sm text-gray-600">Overall Customer Satisfaction</p>
          <div className="flex items-center gap-2">
            <span
              style={{
                fontSize: "1.5rem",
                color: "#facc15",
              }}
            >
              {"★".repeat(
                Math.max(
                  0,
                  Math.round(analytics.feedback.restaurantRating || 0)
                )
              )}
            </span>
            <span className="text-lg font-bold">
              {(analytics.feedback.restaurantRating || 0).toFixed(1)} / 5
            </span>
          </div>
        </div>
      </div>

      <div
        className="grid"
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
          gap: "1rem",
          marginBottom: "1.5rem",
        }}
      >
        {[
          ["Food", analytics.feedback.avgFood],
          ["Ambiance", analytics.feedback.avgAmbiance],
          ["Ingredients", analytics.feedback.avgIngredients],
          ["Service", analytics.feedback.avgService],
          ["Cleanliness", analytics.feedback.avgCleanliness],
          ["Value for Money", analytics.feedback.avgValueForMoney],
          ["Overall", analytics.feedback.avgOverall],
        ].map(([label, value]) => (
          <div key={label} className="flex items-center justify-between">
            <span className="text-sm text-gray-700">{label}</span>
            <div className="flex items-center gap-1">
              <span style={{ color: "#facc15" }}>
                {"★".repeat(Math.round(value || 0))}
              </span>
              <span className="text-xs text-gray-500">
                {(value || 0).toFixed(1)}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Feedback graphs: 3 cards per row */}
<div
  className="grid"
  style={{
    display: "grid",
    gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
    gap: "1.5rem",
  }}
>
  {[
    ["Food", analytics.feedback.foodHistogram],
    ["Ambiance", analytics.feedback.ambianceHistogram],
    ["Ingredients", analytics.feedback.ingredientsHistogram],
    ["Service", analytics.feedback.serviceHistogram],
    ["Cleanliness", analytics.feedback.cleanlinessHistogram],
    ["Value for Money", analytics.feedback.valueHistogram],
    ["Overall", analytics.feedback.overallHistogram],
  ].map(([label, hist]) => {
    const data = hist || {};
    const values = Object.values(data);
    const max = values.length ? Math.max(...values) : 1;

    return (
      <div
        key={label}
        className="card"
        style={{
          padding: "1rem",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
        }}
      >
        <div className="flex justify-between items-center mb-2">
          <span className="text-sm font-semibold text-gray-700">
            {label}
          </span>
          <span className="text-xs text-gray-500">
            1–5 star distribution
          </span>
        </div>
        <br></br>
        <div
          style={{
            height: "140px",
            display: "flex",
            alignItems: "flex-end",
            gap: "0.5rem",
          }}
        >
          {[1, 2, 3, 4, 5].map((starVal) => {
            const count = data[starVal] || 0;
            const height =
              max > 0 ? 20 + (100 * count) / max : 20; // 20–120px

            const color =
              starVal >= 4
                ? "#22c55e"
                : starVal === 3
                ? "#eab308"
                : "#ef4444";

            return (
              <div
                key={starVal}
                style={{
                  flex: 1,
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  fontSize: "11px",
                  color: "#6b7280",
                }}
              >
                <div
                  style={{
                    width: "100%",
                    background: color,
                    borderRadius: "6px 6px 0 0",
                    height: `${height}px`,
                    transition: "height 0.2s ease",
                  }}
                />
                <span style={{ marginTop: "4px" }}>{starVal}★</span>
                <span style={{ fontSize: "10px" }}>{count}</span>
              </div>
            );
          })}
        </div>
      </div>
    );
  })}
</div>
    </>
  )}
</div>
          
          <br></br>
          {/* Top Dishes */}
          <h2 className="text-2xl font-bold mb-4">Top 5 Most Ordered Dishes</h2>
          <div className="card">
            {analytics.topDishes && analytics.topDishes.length > 0 ? (
              <table className="table">
                <thead><tr><th>Rank</th><th>Dish Name</th><th>Category</th><th>Orders</th></tr></thead>
                <tbody>
                  {analytics.topDishes.map((dish, idx) => (
                    <tr key={idx}>
                      <td className="font-bold">#{idx + 1}</td>
                      <td>{dish.name}</td>
                      <td><span className="badge" style={{ backgroundColor: 'var(--primary-600)', color: 'white' }}>{dish.category}</span></td>
                      <td className="font-bold">{dish.count} orders</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p className="text-center text-gray-600">No order data yet</p>
            )}
          </div>

          <br></br>
      <br></br>
<h2 className="text-2xl font-bold mb-4">Social Media</h2>
<div className="card" style={{ marginBottom: '2rem' }}>
  <div className="flex justify-between items-center mb-2">
    <div>
      <h3 className="text-lg font-semibold">Official Platforms</h3>
      <br></br>
    </div>
  </div>

  <div
    style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
      gap: '1rem',
      marginTop: '1rem'
    }}
  >
    {socialMediaLinks.map((social) => (
      <button
        key={social.name}
        type="button"
        onClick={() => openSocialPage(social.url)}
        style={{
          border: 'none',
          borderRadius: '1rem',
          padding: '1.2rem 1rem',
          cursor: 'pointer',
          background: social.color,
          color: 'white',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '0.75rem',
          boxShadow: '0 10px 24px rgba(0,0,0,0.15)',
          transition: 'transform 0.2s ease, box-shadow 0.2s ease',
          minHeight: '120px'
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.transform = 'translateY(-4px)';
          e.currentTarget.style.boxShadow = '0 14px 28px rgba(0,0,0,0.2)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.transform = 'translateY(0)';
          e.currentTarget.style.boxShadow = '0 10px 24px rgba(0,0,0,0.15)';
        }}
      >
        <div
          style={{
            width: '52px',
            height: '52px',
            borderRadius: '999px',
            backgroundColor: 'rgba(255,255,255,0.15)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
        >
          {social.icon}
        </div>

        <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>
          {social.name}
        </span>
      </button>
    ))}
  </div>
</div>
        </div>
      )}

      {/* ALERTS TAB */}
      {activeTab === 'alerts' && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold">Alerts & Issues ({alerts.length})</h2>
            <button onClick={loadAlerts} className="btn btn-secondary">🔄 Refresh</button>
          </div>

          {alerts.length === 0 ? (
            <div className="card text-center" style={{ padding: '3rem' }}>
              <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>✅</div>
              <h3 className="text-xl font-bold text-gray-600">No Pending Alerts</h3>
              <p className="text-gray-500 mt-2">All issues are resolved!</p>
            </div>
          ) : (
            <div className="grid grid-cols-2">
              {alerts.map((alert) => (
                <div key={alert.id} className="card" style={{ borderLeft: `4px solid ₹{alert.type === 'STOCK' ? 'var(--error)' : 'var(--warning)'}` }}>
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <span className="badge" style={{ backgroundColor: alert.type === 'STOCK' ? 'var(--error)' : 'var(--info)', color: 'white' }}>
                        {alert.type}
                      </span>
                      <span className="badge" style={{ backgroundColor: 'var(--gray-600)', color: 'white', marginLeft: '0.5rem' }}>
                        FROM: {alert.fromRole}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500">{new Date(alert.createdAt).toLocaleString()}</p>
                  </div>
                  <p className="font-medium mb-3">{alert.message}</p>
                  <button onClick={() => handleMarkAlertDone(alert.id)} className="btn btn-success" style={{ width: '100%' }}>
                    ✅ Mark as Done
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* INVESTMENTS TAB */}
      {activeTab === 'investments' && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold">Investment Tracking</h2>
            <button onClick={() => setShowInvestmentModal(true)} className="btn btn-primary">+ Add Investment</button>
          </div>

          <div className="card">
            {investments.length === 0 ? (
              <p className="text-center text-gray-600">No investments recorded yet</p>
            ) : (
              <table className="table">
                <thead><tr><th>Date</th><th>Amount</th><th>Description</th><th>Type</th><th>Actions</th></tr></thead>
                <tbody>
                  {investments.map((inv) => (
                    <tr key={inv.id}>
                      <td>{new Date(inv.createdAt).toLocaleDateString()}</td>
                      <td className="font-bold" style={{ color: 'var(--error)' }}>₹{parseFloat(inv.amount).toFixed(2)}</td>
                      <td>{inv.description}</td>
                      <td>
                        <span className="badge" style={{ 
                          backgroundColor: inv.type === 'SALARY' ? 'var(--warning)' : inv.type === 'PURCHASE' ? 'var(--info)' : 'var(--gray-600)', 
                          color: 'white' 
                        }}>
                          {inv.type}
                        </span>
                      </td>
                      <td>
                        <button onClick={() => handleDeleteInvestment(inv.id)} className="btn btn-danger" style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem' }}>
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
      )}

      {/* SALARIES TAB */}
      {activeTab === 'salaries' && (
        <div>
          <h2 className="text-2xl font-bold mb-6">Salary Management</h2>

          <div className="card">
            {salaries.length === 0 ? (
              <p className="text-center text-gray-600">No active employees</p>
            ) : (
              <table className="table">
                <thead><tr><th>Employee Name</th><th>Role</th><th>Salary</th><th>Status</th><th>Last Payment</th><th>Action</th></tr></thead>
                <tbody>
                  {salaries.map((sal) => (
                    <tr key={sal.employeeId}>
                      <td className="font-medium">{sal.employeeName}</td>
                      <td><span className="badge" style={{ backgroundColor: 'var(--primary-600)', color: 'white' }}>{sal.role}</span></td>
                      <td className="font-bold">₹{parseFloat(sal.salary).toFixed(2)}</td>
                      <td>
                        {sal.paidThisMonth ? (
                          <span className="badge" style={{ backgroundColor: 'var(--success)', color: 'white' }}>✅ Paid</span>
                        ) : (
                          <span className="badge" style={{ backgroundColor: 'var(--error)', color: 'white' }}>❌ Unpaid</span>
                        )}
                      </td>
                      <td className="text-sm text-gray-600">{sal.lastPaymentDate || 'Never'}</td>
                      <td>
                        <div className="flex gap-2">
                          <button
                            onClick={() => handlePaySalary(sal.employeeId)}
                            className="btn btn-success"
                            style={{ padding: "0.5rem 1rem", fontSize: "0.875rem" }}
                            disabled={sal.paidThisMonth || loading}
                          >
                            {sal.paidThisMonth ? "Already Paid" : "Pay Now"}
                          </button>
                          <button
                            onClick={() => handleEditSalary(sal)}
                            className="btn btn-secondary"
                            style={{ padding: "0.5rem 1rem", fontSize: "0.875rem" }}
                          >
                            Edit Salary
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

      <Modal
  isOpen={showSalaryModal}
  onClose={() => {
    setShowSalaryModal(false);
    setEditingSalary(null);
  }}
  title="Edit Employee Salary"
>
  {editingSalary && (
    <form onSubmit={handleSaveSalary}>
      <div className="form-group">
        <label className="form-label">Employee</label>
        <input
          type="text"
          className="form-input"
          value={`₹{editingSalary.employeeName} (₹{editingSalary.role})`}
          disabled
        />
      </div>

      <div className="form-group">
        <label className="form-label">Salary</label>
        <input
          type="number"
          step="0.01"
          className="form-input"
          value={salaryForm.salary}
          onChange={(e) =>
            setSalaryForm({ ...salaryForm, salary: e.target.value })
          }
          required
        />
      </div>

      <div className="form-group" style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
        <input
          type="checkbox"
          checked={salaryForm.active}
          onChange={(e) =>
            setSalaryForm({ ...salaryForm, active: e.target.checked })
          }
        />
        <label className="form-label">Active employee</label>
      </div>

      <div className="flex gap-2 justify-end" style={{ marginTop: "1rem" }}>
        <button
          type="button"
          onClick={() => {
            setShowSalaryModal(false);
            setEditingSalary(null);
          }}
          className="btn btn-secondary"
        >
          Cancel
        </button>
        <button
          type="submit"
          className="btn btn-primary"
          disabled={loading}
        >
          {loading ? "Saving..." : "Save Changes"}
        </button>
      </div>
    </form>
  )}
</Modal>


      {/* USERS TAB */}
      {activeTab === 'users' && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold">User Accounts</h2>
            <button onClick={() => setShowUserModal(true)} className="btn btn-primary">+ Create User</button>
          </div>

          <div className="card">
            {users.length === 0 ? (
              <p className="text-center text-gray-600">No users yet</p>
            ) : (
              <table className="table">
                <thead><tr><th>Username</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td className="font-medium">{user.username}</td>
                      <td><span className="badge" style={{ backgroundColor: 'var(--primary-600)', color: 'white' }}>{user.role}</span></td>
                      <td>
                        <span className="badge" style={{ backgroundColor: user.active ? 'var(--success)' : 'var(--gray-300)', color: 'white' }}>
                          {user.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td>
  <div className="flex gap-2">
    <button onClick={() => handleEditUser(user)} className="btn btn-secondary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem' }}>
      Edit
    </button>
    <button onClick={() => handleDeleteUser(user.id)} className="btn btn-danger" style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem' }}>
      Delete
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

      {activeTab === 'bills' && (
  <div>
    <div className="flex justify-between items-center mb-6">
      <h2 className="text-2xl font-bold">Bills Overview</h2>
      <button onClick={loadBills} className="btn btn-secondary">
        Refresh
      </button>
    </div>

    <div className="card" style={{ marginBottom: '2rem' }}>
      <div className="flex justify-between items-center mb-3">
        <div>
          <h3 className="text-lg font-semibold">All Bills</h3>
          <p className="text-sm text-gray-500">
            Scroll to view all generated bills.
          </p>
        </div>
      </div>

      <div style={{ maxHeight: '420px', overflowY: 'auto' }}>
        {billLoading ? (
          <p className="text-gray-600">Loading bills...</p>
        ) : bills.length === 0 ? (
          <p className="text-gray-600">No bills found.</p>
        ) : (
          <table className="table">
            <thead style={{ position: 'sticky', top: 0, background: '#fff', zIndex: 1 }}>
              <tr>
                <th>S.No</th>
                <th>Date</th>
                <th>Order ID</th>
                <th>Amount</th>
                <th>Payment Method</th>
              </tr>
            </thead>
            <tbody>
              {bills.map((bill, index) => (
                <tr key={bill.id}>
                  <td>{index + 1}</td>
                  <td>{bill.createdAt ? new Date(bill.createdAt).toLocaleString() : '-'}</td>
                  <td>#{bill.orderId}</td>
                  <td className="font-bold">₹{parseFloat(bill.finalAmount || 0).toFixed(2)}</td>
                  <td>
                    <span
                      className="badge"
                      style={{
                        backgroundColor:
                          bill.paymentMethod === 'CASH'
                            ? 'var(--success)'
                            : bill.paymentMethod === 'CARD'
                            ? 'var(--primary-600)'
                            : 'var(--warning)',
                        color: 'white'
                      }}
                    >
                      {bill.paymentMethod}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>

    <h2 className="text-2xl font-bold mb-4">Bill Insights</h2>
    <div className="grid grid-cols-4 gap-4 mb-8">
      <div className="stat-card">
        <div className="stat-value">{totalBills}</div>
        <div className="stat-label">Total Bills</div>
      </div>

      <div className="stat-card">
        <div className="stat-value">₹{totalBillRevenue.toFixed(2)}</div>
        <div className="stat-label">Total Revenue</div>
      </div>

      <div className="stat-card">
        <div className="stat-value">₹{averageBillValue.toFixed(2)}</div>
        <div className="stat-label">Average Bill</div>
      </div>

      <div className="stat-card">
        <div className="stat-value">₹{highestBillAmount.toFixed(2)}</div>
        <div className="stat-label">Highest Bill</div>
      </div>
    </div>

    <div className="grid grid-cols-4 gap-4 mb-8">
      <div className="card" style={{ borderLeft: '5px solid #22c55e' }}>
        <h4 className="font-semibold mb-2">Cash Bills</h4>
        <p className="text-2xl font-bold">{cashBills}</p>
      </div>

      <div className="card" style={{ borderLeft: '5px solid #3b82f6' }}>
        <h4 className="font-semibold mb-2">Card Bills</h4>
        <p className="text-2xl font-bold">{cardBills}</p>
      </div>

      <div className="card" style={{ borderLeft: '5px solid #f59e0b' }}>
        <h4 className="font-semibold mb-2">UPI Bills</h4>
        <p className="text-2xl font-bold">{upiBills}</p>
      </div>

      <div className="card" style={{ borderLeft: '5px solid #ef4444' }}>
        <h4 className="font-semibold mb-2">Total Discount</h4>
        <p className="text-2xl font-bold">₹{totalDiscountGiven.toFixed(2)}</p>
      </div>
    </div>

    <div className="grid grid-cols-2 gap-4 mb-8">
      <div className="card">
        <h4 className="font-semibold mb-2">Tax Collected</h4>
        <p className="text-sm text-gray-500 mb-2">
          Total tax collected from all generated bills
        </p>
        <p className="text-2xl font-bold text-blue-600">₹{totalTaxCollected.toFixed(2)}</p>
      </div>

      <div className="card">
        <h4 className="font-semibold mb-2">Payment Mix</h4>
        <p className="text-sm text-gray-500 mb-3">
          Quick breakdown of billing methods
        </p>
        <div className="flex gap-3 flex-wrap">
          <span className="badge" style={{ backgroundColor: '#22c55e', color: 'white' }}>
            CASH: {cashBills}
          </span>
          <span className="badge" style={{ backgroundColor: '#3b82f6', color: 'white' }}>
            CARD: {cardBills}
          </span>
          <span className="badge" style={{ backgroundColor: '#f59e0b', color: 'white' }}>
            UPI: {upiBills}
          </span>
        </div>
      </div>
    </div>
  </div>
)}

      {/* MODALS */}
      <Modal isOpen={showInvestmentModal} onClose={() => setShowInvestmentModal(false)} title="Add Investment">
        <form onSubmit={handleCreateInvestment}>
          <div className="form-group">
            <label className="form-label">Amount (₹)</label>
            <input type="number" step="0.01" className="form-input" placeholder="e.g., 5000" value={newInvestment.amount} onChange={(e) => setNewInvestment({ ...newInvestment, amount: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Description</label>
            <input type="text" className="form-input" placeholder="e.g., Purchased 10kg rice" value={newInvestment.description} onChange={(e) => setNewInvestment({ ...newInvestment, description: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Type</label>
            <select className="form-select" value={newInvestment.type} onChange={(e) => setNewInvestment({ ...newInvestment, type: e.target.value })}>
              <option value="PURCHASE">Purchase</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => setShowInvestmentModal(false)} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Adding...' : 'Add Investment'}</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showUserModal} onClose={() => { setShowUserModal(false); setEditingUser(null); }} title={editingUser ? 'Edit User' : 'Create User Account'}>
        <form onSubmit={handleCreateUser}>
          <div className="form-group">
            <label className="form-label">Username</label>
            <input type="text" className="form-input" placeholder="e.g., john_chef" value={newUser.username} onChange={(e) => setNewUser({ ...newUser, username: e.target.value })} required />
          </div>
          <div className="form-group">
  <label className="form-label">Password {editingUser && '(leave blank to keep current)'}</label>
  <input 
    type="password" 
    className="form-input" 
    placeholder={editingUser ? "Leave blank to keep current" : "Strong password"} 
    value={newUser.password} 
    onChange={(e) => setNewUser({ ...newUser, password: e.target.value })} 
    required={!editingUser}  // Only required for new users
  />
</div>

          <div className="form-group">
            <label className="form-label">Role</label>
            <select className="form-select" value={newUser.role} onChange={(e) => setNewUser({ ...newUser, role: e.target.value })} required>
              <option value="ACCOUNTANT">Accountant</option>
              <option value="CHEF">Chef</option>
              <option value="WAITER">Waiter</option>
              <option value="INVENTORY">Inventory</option>
            </select>
          </div>
          <div className="alert" style={{ backgroundColor: 'var(--primary-50)', borderColor: 'var(--primary-200)', color: 'var(--primary-700)', marginBottom: '1rem' }}>
            💡 This user will be able to login with these credentials
          </div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => setShowUserModal(false)} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Creating...' : 'Create User'}</button>
          </div>
        </form>
      </Modal>

      {/* Floating AI Assistant button */}
{!isAiChatOpen && (
  <button
    onClick={() => setIsAiChatOpen(true)}
    style={{
      position: "fixed",
      right: "1.5rem",
      bottom: "1.5rem",
      zIndex: 90,
      display: "flex",
      alignItems: "center",
      gap: "0.5rem",
      padding: "0.75rem 1rem",
      borderRadius: "999px",
      border: "none",
      background:
        "linear-gradient(135deg, var(--primary-600), var(--primary-500))",
      color: "white",
      boxShadow: "0 10px 20px rgba(0,0,0,0.25)",
      cursor: "pointer",
      fontSize: "0.85rem",
      fontWeight: 600,
    }}
  >
    <span
      style={{
        width: "1.8rem",
        height: "1.8rem",
        borderRadius: "999px",
        backgroundColor: "rgba(255,255,255,0.15)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: "1.1rem",
      }}
    >
      🤖
    </span>
    <span>AI Assistant</span>
  </button>
)}

      {/* AI Chat Assistant (Owner only) */}
{/* AI Chat Box */}
{isAiChatOpen && (
  <div
    style={{
      position: "fixed",
      right: "1.5rem",
      bottom: "1.5rem",
      width: "370px",
      height: "70vh",
      zIndex: 90,
    }}
  >
    <div
      className="card"
      style={{
        display: "flex",
        flexDirection: "column",
        height: "100%",
        overflow: "hidden",
      }}
    >
      {/* Header with minimize button */}
      <div
        className="flex justify-between items-center"
        style={{ marginBottom: "0.5rem" }}
      >
        <h3 className="text-sm font-semibold">AI Owner Assistant</h3>
        <button
          type="button"
          onClick={() => setIsAiChatOpen(false)}
          style={{
            border: "none",
            background: "transparent",
            cursor: "pointer",
            fontSize: "1.1rem",
            lineHeight: 1,
            color: "var(--gray-500)",
          }}
          title="Minimize"
        >
          ▾
        </button>
      </div>

      {/* Scrollable messages */}
      <div
        style={{
          flex: 1,
          minHeight: 0,
          overflowY: "auto",
          border: "1px solid var(--gray-200)",
          borderRadius: "0.5rem",
          padding: "0.5rem",
          marginBottom: "0.5rem",
          backgroundColor: "var(--gray-50)",
        }}
      >
        {chatMessages.map((m, idx) => (
          <div
            key={idx}
            style={{
              marginBottom: "0.5rem",
              textAlign: m.role === "user" ? "right" : "left",
            }}
          >
            <div
              style={{
                display: "inline-block",
                padding: "0.4rem 0.6rem",
                borderRadius: "0.5rem",
                fontSize: "0.8rem",
                backgroundColor:
                  m.role === "user" ? "var(--primary-600)" : "white",
                color: m.role === "user" ? "white" : "var(--gray-800)",
              }}
            >
              {m.content}
            </div>
          </div>
        ))}
        {chatLoading && (
          <p style={{ fontSize: "0.8rem", color: "var(--gray-500)" }}>
            Thinking...
          </p>
        )}
      </div>

            {/* Input row */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (!chatLoading) sendChat();
        }}
        style={{ display: "flex", gap: "0.25rem", alignItems: "center" }}
      >
        <input
          id="chatInput"
          type="text"
          className="form-input"
          placeholder="Ask about your restaurant..."
          value={chatInput}
          onChange={(e) => setChatInput(e.target.value)}
          style={{ fontSize: "0.8rem" }}
        />

        {/* Mic button */}
        <button
        type="button"
        id="micBtn"
        className={isListening ? "mic-active" : ""}
        onClick={handleMicClick}
      >
        <Mic size={20} color="white" />
      </button>

        <button
          type="submit"
          className="btn btn-primary"
          disabled={chatLoading}
          style={{ paddingInline: "0.75rem" }}
        >
          Go
        </button>
      </form>
    </div>
  </div>
)}

    </DashboardLayout>
  );
};

export default OwnerDashboard;
