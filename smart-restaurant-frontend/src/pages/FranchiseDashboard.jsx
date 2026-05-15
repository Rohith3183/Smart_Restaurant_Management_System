import { useEffect, useState, useRef } from 'react';
import DashboardLayout from '../components/DashboardLayout';
import Tabs from '../components/Tabs';
import { franchiseAPI } from '../services/api';
import { Line, Bar, Doughnut } from 'react-chartjs-2';
import { useAuth } from '../context/AuthContext';
import "../index.css";
import { Mic } from "lucide-react";

const FranchiseDashboard = () => {
  const [activeTab, setActiveTab] = useState('analytics');
  const [analytics, setAnalytics] = useState(null);
  const [restaurants, setRestaurants] = useState([]);
  const [aiInsights, setAiInsights] = useState('');
  const [aiLoading, setAiLoading] = useState(false);
  const [chatMessages, setChatMessages] = useState([
    { role: 'assistant', content: "Hi! I'm your Franchise AI assistant. Ask me about combined sales, menu, or performance across all your restaurants." },
  ]);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);
  const [isAiChatOpen, setIsAiChatOpen] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isListening, setIsListening] = useState(false);
  const recognitionRef = useRef(null);

  const tabs = [
    { id: 'analytics', label: 'Analytics' },
    { id: 'restaurants', label: 'My Restaurants' },
  ];

  useEffect(() => {
    if (activeTab === 'analytics') {
      loadAnalytics();
      loadAiInsights();
    } else if (activeTab === 'restaurants') {
      loadRestaurants();
    }
  }, [activeTab]);

  const loadAnalytics = async () => {
    try {
      const res = await franchiseAPI.getAnalytics();
      setAnalytics(res.data);
    } catch (err) {
      setError('Failed to load analytics');
    }
  };

  const loadRestaurants = async () => {
    try {
      const res = await franchiseAPI.getRestaurants();
      setRestaurants(res.data);
    } catch (err) {
      setError('Failed to load restaurants');
    }
  };

  const loadAiInsights = async () => {
    try {
      setAiLoading(true);
      const res = await franchiseAPI.getSalesInsights();
      setAiInsights(res.data.summary);
    } catch {
      // ignore
    } finally {
      setAiLoading(false);
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

  const sendChat = async () => {
    if (!chatInput.trim()) return;
    const userMsg = { role: 'user', content: chatInput.trim() };
    setChatMessages((prev) => [...prev, userMsg]);
    setChatInput('');
    setChatLoading(true);
    try {
      const res = await franchiseAPI.chat({
        messages: [...chatMessages, userMsg],
      });
      const reply = { role: 'assistant', content: res.data.reply };
      setChatMessages((prev) => [...prev, reply]);
    } catch {
      setChatMessages((prev) => [
        ...prev,
        { role: 'assistant', content: 'Failed to get response, please try again.' },
      ]);
    } finally {
      setChatLoading(false);
    }
  };

  const handleImpersonate = async (code) => {
    if (!window.confirm(`Open Owner Dashboard for ${code}?`)) return;
    try {
      const res = await franchiseAPI.impersonate(code);
      const { token, ...userData } = res.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));
      window.location.href = '/owner';
    } catch (err) {
      setError('Failed to open restaurant dashboard');
    }
  };

  return (
    <DashboardLayout title="Franchise Owner Dashboard">
      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
          {error}
          <button
            onClick={() => setError('')}
            style={{ marginLeft: '1rem', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.5rem' }}
          >
            ×
          </button>
        </div>
      )}
      {success && (
        <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
          {success}
          <button
            onClick={() => setSuccess('')}
            style={{ marginLeft: '1rem', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.5rem' }}
          >
            ×
          </button>
        </div>
      )}

      <Tabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} />

      {activeTab === 'analytics' && (
        <div>
          <h2 className="text-2xl font-bold mb-4">Combined Analytics</h2>
          {!analytics ? (
            <p className="text-gray-600">Loading analytics...</p>
          ) : (
            <div className="grid grid-cols-3 gap-4">
              {/* reuse cards similar to OwnerDashboard using analytics fields */}
            </div>
          )}

          <div className="card mt-6">
            <h3 className="text-lg font-semibold mb-2">AI Insights</h3>
            {aiLoading ? (
              <p className="text-gray-500 text-sm">Loading AI insights...</p>
            ) : (
              <p className="text-sm text-gray-800 whitespace-pre-line">
                {aiInsights || 'Ask the AI assistant for more details.'}
              </p>
            )}
          </div>
        </div>
      )}

      {activeTab === 'restaurants' && (
        <div>
          <h2 className="text-2xl font-bold mb-4">My Restaurants</h2>
          {restaurants.length === 0 ? (
            <p className="text-gray-600">No restaurants registered under this franchise yet.</p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3">
  {restaurants.map((r) => (
    <button
      key={r.code}
      onClick={() => handleImpersonate(r.code)}
      className="transition transform hover:-translate-y-1 hover:shadow-lg rounded-xl text-left"
      style={{
        padding: '0.9rem 1rem',
        background:
          'linear-gradient(135deg, rgba(37,99,235,0.07), rgba(16,185,129,0.08))',
        border: '1px solid rgba(148,163,184,0.4)',
      }}
    >
      <div className="flex items-center justify-between mb-1">
        <span
          className="text-xs font-semibold px-2 py-0.5 rounded-full"
          style={{ backgroundColor: 'white', color: '#2563eb' }}
        >
          {r.code}
        </span>
        <span className="text-[10px] uppercase tracking-wide text-gray-500">
          Branch
        </span>
      </div>
      <div className="font-semibold text-sm text-gray-900 truncate">
        {r.name || 'Restaurant'}
      </div>
      <div className="text-xs text-gray-600 mt-1 flex justify-between">
        <span>{r.location || 'Location N/A'}</span>
        <span className="text-blue-600 font-medium">Open&nbsp;Dashboard →</span>
      </div>
    </button>
  ))}
</div>
          )}
        </div>
      )}

      {/* Floating AI Assistant similar to OwnerDashboard but using franchiseAPI */}
{!isAiChatOpen && (
  <button
    onClick={() => setIsAiChatOpen(true)}
    style={{
      position: 'fixed',
      right: '1.5rem',
      bottom: '1.5rem',
      zIndex: 90,
      display: 'flex',
      alignItems: 'center',
      gap: '0.5rem',
      padding: '0.75rem 1rem',
      borderRadius: '999px',
      border: 'none',
      background: 'linear-gradient(135deg, var(--primary-600), var(--primary-500))',
      color: 'white',
      boxShadow: '0 10px 20px rgba(0,0,0,0.25)',
      cursor: 'pointer',
      fontSize: '0.85rem',
      fontWeight: 600,
    }}
  >
    <span
      style={{
        width: '1.8rem',
        height: '1.8rem',
        borderRadius: '999px',
        backgroundColor: 'rgba(255,255,255,0.15)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '1.1rem',
      }}
    >
      🤖
    </span>
    <span>AI Franchise Assistant</span>
  </button>
)}

{isAiChatOpen && (
  <div
    style={{
      position: 'fixed',
      right: '1.5rem',
      bottom: '1.5rem',
      width: '320px',
      height: '70vh',
      zIndex: 90,
    }}
  >
    <div
      className="card"
      style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}
    >
      <div className="flex justify-between items-center" style={{ marginBottom: '0.5rem' }}>
        <h3 className="text-sm font-semibold">AI Franchise Assistant</h3>
        <button
          type="button"
          onClick={() => setIsAiChatOpen(false)}
          style={{
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            fontSize: '1.1rem',
            lineHeight: 1,
            color: 'var(--gray-500)',
          }}
          title="Minimize"
        >
          ×
        </button>
      </div>

      <div
        style={{
          flex: 1,
          minHeight: 0,
          overflowY: 'auto',
          border: '1px solid var(--gray-200)',
          borderRadius: '0.5rem',
          padding: '0.5rem',
          marginBottom: '0.5rem',
          backgroundColor: 'var(--gray-50)',
        }}
      >
        {chatMessages.map((m, idx) => (
          <div
            key={idx}
            style={{
              marginBottom: '0.5rem',
              textAlign: m.role === 'user' ? 'right' : 'left',
            }}
          >
            <div
              style={{
                display: 'inline-block',
                padding: '0.4rem 0.6rem',
                borderRadius: '0.5rem',
                fontSize: '0.8rem',
                backgroundColor:
                  m.role === 'user' ? 'var(--primary-600)' : 'white',
                color: m.role === 'user' ? 'white' : 'var(--gray-800)',
              }}
            >
              {m.content}
            </div>
          </div>
        ))}
        {chatLoading && (
          <p style={{ fontSize: '0.8rem', color: 'var(--gray-500)' }}>
            Thinking...
          </p>
        )}
      </div>

      {/* Input row with mic */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (!chatLoading) sendChat();
        }}
        style={{ display: 'flex', gap: '0.25rem', alignItems: 'center' }}
      >
        <input
          id="franchiseChatInput"
          type="text"
          className="form-input"
          placeholder="Ask about your franchise..."
          value={chatInput}
          onChange={(e) => setChatInput(e.target.value)}
          style={{ fontSize: '0.8rem' }}
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
          style={{ paddingInline: '0.75rem' }}
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

export default FranchiseDashboard;
