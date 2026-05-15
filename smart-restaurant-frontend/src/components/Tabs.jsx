const Tabs = ({ tabs, activeTab, onChange }) => {
  return (
    <div style={{ borderBottom: '2px solid var(--gray-200)', marginBottom: '2rem' }}>
      <div style={{ display: 'flex', gap: '1rem', overflowX: 'auto' }}>
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => onChange(tab.id)}
            style={{
              padding: '0.75rem 1.5rem',
              background: 'none',
              border: 'none',
              borderBottom: activeTab === tab.id ? '2px solid var(--primary-600)' : '2px solid transparent',
              color: activeTab === tab.id ? 'var(--primary-600)' : 'var(--gray-600)',
              fontWeight: activeTab === tab.id ? '600' : '500',
              cursor: 'pointer',
              transition: 'all 0.2s',
              whiteSpace: 'nowrap',
              fontSize: '0.875rem',
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>
    </div>
  );
};

export default Tabs;
