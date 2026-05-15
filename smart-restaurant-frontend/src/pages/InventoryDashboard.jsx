import React, { useState, useEffect, useRef } from 'react';
import DashboardLayout from '../components/DashboardLayout';
import Tabs from '../components/Tabs';
import Modal from '../components/Modal';
import { menuAPI, ingredientAPI, tableAPI, alertAPI, inventoryDashboardAPI, prepPlanAPI, batchAPI, varianceAPI, forecastAPI, contactAPI, stockVarianceAPI } from '../services/api';
import SupportFooter from "../components/SupportFooter";
import { Client } from "@stomp/stompjs";
import { color } from 'chart.js/helpers';
import { useAuth } from '../context/AuthContext';

const InventoryDashboard = () => {
  const [activeTab, setActiveTab] = useState('menu');
  const [categories, setCategories] = useState([]);
  const [menuItems, setMenuItems] = useState([]);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [showItemModal, setShowItemModal] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [newCategory, setNewCategory] = useState('');
  const [newItem, setNewItem] = useState({ categoryId: '', name: '', price: '', available: true, majorIngredients: [] });
  const [ingredients, setIngredients] = useState([]);
  const [ingredientSearch, setIngredientSearch] = useState("");
  const [showIngredientModal, setShowIngredientModal] = useState(false);
  const [editingIngredient, setEditingIngredient] = useState(null);
  const [newIngredient, setNewIngredient] = useState({ name: '', currentStock: '', threshold: '', unit: 'Kg', expiryDate: '' });
  const [tables, setTables] = useState([]);
  const [showTableModal, setShowTableModal] = useState(false);
  const [editingTable, setEditingTable] = useState(null);
  const [newTable, setNewTable] = useState({ tableNumber: '', capacity: '' });
  const [issueMessage, setIssueMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [metrics, setMetrics] = useState(null);
  const [prepToday, setPrepToday] = useState([]);
  const [stompClient, setStompClient] = useState(null);
  const wsClientRef = useRef(null);
  const [expiringBatches, setExpiringBatches] = useState([]);
  const [highRiskVariances, setHighRiskVariances] = useState([]);
  const [forecastDate, setForecastDate] = useState("");
  const [forecast, setForecast] = useState([]);
  const restaurantCode = localStorage.getItem("restaurantCode");
  const [contacts, setContacts] = useState([]);
  const [showContactModal, setShowContactModal] = useState(false);
  const [showSendContactModal, setShowSendContactModal] = useState(false);
  const [editingContact, setEditingContact] = useState(null);
  const [selectedContact, setSelectedContact] = useState(null);
  const { user, logout } = useAuth();

  const [newContact, setNewContact] = useState({
    name: '',
    email: '',
    countryCode: '+91',
    phoneNumber: '',
  });

const [contactMessageData, setContactMessageData] = useState({
  subject: '',
  message: '',
});
const [showPhysicalCountModal, setShowPhysicalCountModal] = useState(false);
const [selectedIngredientForCount, setSelectedIngredientForCount] = useState(null);
const [physicalCountData, setPhysicalCountData] = useState({
  ingredientId: '',
  physicalStock: '',
  notes: '',
  checkedBy: 'INVENTORY',
});

  const tabs = [
    { id: 'menu', label: '🍽️ Menu' },
    { id: 'ingredients', label: '🥬 Ingredients' },
    { id: 'tables', label: '🪑 Tables' },
  ];

  useEffect(() => {
  if (activeTab === "menu") {
    loadMenuData();
  } else if (activeTab === "ingredients") {
    loadIngredients();
    loadPrepToday();
    loadExpiring();
    loadHighRiskVariances();
    loadContacts();
  } else if (activeTab === "tables") {
    loadTables();
  }
}, [activeTab]);

  useEffect(() => {
  const loadMetrics = async () => {
    try {
      const res = await inventoryDashboardAPI.getMetrics();
      setMetrics(res.data);
    } catch (err) {
      console.error(err);
      // don't override existing error state if already set for other stuff
    }
  };

  loadMetrics();
}, []);

useEffect(() => {
  // Load ingredients once so menu item modal always has data
  const loadForMenuSelector = async () => {
    try {
      const res = await ingredientAPI.getAll();
      setIngredients(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  loadForMenuSelector();
}, []);

useEffect(() => {
  if (!restaurantCode) return;

  const client = new Client({
    // Use native WebSocket
    brokerURL:
      (window.location.protocol === "https:" ? "wss://" : "ws://") +
      window.location.host +
      "/ws",

    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe(
        `/topic/stock-updates/${restaurantCode}`,
        (message) => {
          if (!message.body) return;
          const payload = JSON.parse(message.body);
          const updated = payload.ingredient;
          setIngredients((prev) =>
            prev.map((ing) =>
              ing.id === updated.id
                ? {
                    ...ing,
                    currentStock: updated.currentStock,
                    threshold: updated.threshold,
                    unit: updated.unit,
                    lowStock: updated.lowStock,
                  }
                : ing
            )
          );
        }
      );

      client.subscribe(
        `/topic/expiry/${restaurantCode}`,
        (message) => {
          if (!message.body) return;
          const payload = JSON.parse(message.body);
          console.log("Expiry warning:", payload.message);
        }
      );
    },
    // needed when not using brokerURL but webSocketFactory; here we use brokerURL so ok
  });

  client.activate();
  wsClientRef.current = client;
  setStompClient(client);

  return () => {
    if (wsClientRef.current) {
      wsClientRef.current.deactivate();
    }
  };
}, [restaurantCode]);

  const loadMenuData = async () => {
    try {
      const [catRes, itemRes] = await Promise.all([menuAPI.getCategories(), menuAPI.getItems()]);
      setCategories(catRes.data);
      setMenuItems(itemRes.data);
    } catch (err) {
      setError('Failed to load menu data');
    }
  };

  const loadIngredients = async () => {
    try {
      const res = await ingredientAPI.getAll();
      setIngredients(res.data);
    } catch (err) {
      setError('Failed to load ingredients');
    }
  };

  const loadContacts = async () => {
  try {
    const res = await contactAPI.getAll();
    setContacts(Array.isArray(res.data) ? res.data : []);
  } catch (err) {
    console.error(err);
    setContacts([]);
    setError('Failed to load contacts');
  }
};

const handleCreateOrUpdateContact = async (e) => {
  e.preventDefault();

  if (!newContact.name.trim() || !newContact.email.trim() || !newContact.countryCode.trim() || !newContact.phoneNumber.trim()) {
    setError('Please fill all contact fields');
    return;
  }

  setLoading(true);
  try {
    const payload = {
      name: newContact.name.trim(),
      email: newContact.email.trim(),
      countryCode: newContact.countryCode.trim(),
      phoneNumber: newContact.phoneNumber.trim(),
    };

    if (editingContact) {
      await contactAPI.update(editingContact.id, payload);
      setSuccess('Contact updated successfully');
    } else {
      await contactAPI.create(payload);
      setSuccess('Contact added successfully');
    }

    setShowContactModal(false);
    setEditingContact(null);
    setNewContact({
      name: '',
      email: '',
      countryCode: '+91',
      phoneNumber: '',
    });
    loadContacts();
  } catch (err) {
    console.error(err);
    setError('Failed to save contact');
  }
  setLoading(false);
};

const handleEditContact = (contact) => {
  setEditingContact(contact);
  setNewContact({
    name: contact.name || '',
    email: contact.email || '',
    countryCode: contact.countryCode || '+91',
    phoneNumber: contact.phoneNumber || '',
  });
  setShowContactModal(true);
};

const handleDeleteContact = async (id) => {
  if (!window.confirm('Delete this contact?')) return;

  try {
    await contactAPI.delete(id);
    setSuccess('Contact deleted successfully');
    loadContacts();
  } catch (err) {
    console.error(err);
    setError('Failed to delete contact');
  }
};

const handleOpenSendModal = (contact) => {
  const restaurantName = localStorage.getItem("restaurantName") || "Restaurant";

  setSelectedContact(contact);
  setContactMessageData({
    subject: `Leftover Food Available - ${user?.restaurantName}`,
    message:
`Dear ${contact.name},

Greetings from ${user?.restaurantName}.

We hope you are doing well.

We are reaching out from our inventory team regarding available leftover food that may be useful for your organization.

[Please enter food details here]

If you are available to collect or coordinate regarding this food, please feel free to contact us.

Thank you for your support and service.

Regards,
Inventory Team
${user?.restaurantName}`
  });
  setShowSendContactModal(true);
};

const handleSendContactMessage = async (e) => {
  e.preventDefault();

  if (!selectedContact) {
    setError('No contact selected');
    return;
  }

  if (!contactMessageData.message.trim()) {
    setError('Please enter message');
    return;
  }

  setLoading(true);
  try {
    await contactAPI.sendMessage({
      contactId: selectedContact.id,
      subject: contactMessageData.subject,
      message: contactMessageData.message,
    });

    setShowSendContactModal(false);
    setSelectedContact(null);
    setContactMessageData({
      subject: 'Leftover Food Available - Smart Restaurant',
      message: 'Hi this is from smart restaurant system. ',
    });
    setSuccess('Message sent successfully');
  } catch (err) {
    console.error(err);
    setError('Failed to send message');
  }
  setLoading(false);
};

const handleOpenPhysicalCountModal = (ingredient) => {
  setSelectedIngredientForCount(ingredient);
  setPhysicalCountData({
    ingredientId: ingredient.id,
    physicalStock: ingredient.currentStock ?? '',
    notes: '',
    checkedBy: 'INVENTORY',
  });
  setShowPhysicalCountModal(true);
};

const handleSubmitPhysicalCount = async (e) => {
  e.preventDefault();

  if (!selectedIngredientForCount) {
    setError('No ingredient selected');
    return;
  }

  if (physicalCountData.physicalStock === '' || physicalCountData.physicalStock === null) {
    setError('Please enter physical stock');
    return;
  }

  setLoading(true);
  try {
    await stockVarianceAPI.recordPhysicalCount({
      ingredientId: selectedIngredientForCount.id,
      physicalStock: parseFloat(physicalCountData.physicalStock),
      notes: physicalCountData.notes,
      checkedBy: physicalCountData.checkedBy || 'INVENTORY',
    });

    setShowPhysicalCountModal(false);
    setSelectedIngredientForCount(null);
    setPhysicalCountData({
      ingredientId: '',
      physicalStock: '',
      notes: '',
      checkedBy: 'INVENTORY',
    });

    await loadIngredients();
    await loadHighRiskVariances();

    setSuccess('Physical count recorded successfully');
    setTimeout(() => setSuccess(''), 3000);
  } catch (err) {
    console.error(err);
    setError('Failed to record physical count');
  }
  setLoading(false);
};

  const loadPrepToday = async () => {
  try {
    const res = await prepPlanAPI.getToday();
    setPrepToday(Array.isArray(res.data) ? res.data : []);
  } catch (err) {
    console.error(err);
    setPrepToday([]);
    // optional: setError("Failed to load prep plan");
  }
};

  const loadTables = async () => {
    try {
      const res = await tableAPI.getAll();
      setTables(res.data);
    } catch (err) {
      setError('Failed to load tables');
    }
  };

  const loadHighRiskVariances = async () => {
  try {
    const res = await varianceAPI.getHighRisk();
    setHighRiskVariances(Array.isArray(res.data) ? res.data : []);
  } catch (err) {
    console.error(err);
    setHighRiskVariances([]);
  }
};

const loadForecast = async () => {
  if (!forecastDate) return;
  try {
    console.log("Loading forecast for", forecastDate);
    const res = await forecastAPI.getForDate(forecastDate);
    console.log("Forecast response", res.data);
    setForecast(Array.isArray(res.data) ? res.data : []);
  } catch (err) {
    console.error("Forecast error", err);
    setForecast([]);
  }
};

const safeIngredients = Array.isArray(ingredients) ? ingredients : [];

const filteredIngredients = safeIngredients.filter((ing) =>
  (ing.name || "").toLowerCase().includes((ingredientSearch || "").toLowerCase())
);

  const handleCreateCategory = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await menuAPI.createCategory(newCategory);
      setShowCategoryModal(false);
      setNewCategory('');
      loadMenuData();
      setSuccess('Category created successfully');
    } catch (err) {
      setError('Failed to create category');
    }
    setLoading(false);
  };

  const handleDeleteCategory = async (id) => {
    if (!window.confirm('Delete this category?')) return;
    try {
      await menuAPI.deleteCategory(id);
      loadMenuData();
      setSuccess('Category deleted successfully');
    } catch (err) {
      setError('Failed to delete category');
    }
  };

  const handleCreateOrUpdateItem = async (e) => {
  e.preventDefault();
  setLoading(true);
  try {
    const data = {
      ...newItem,
      price: parseFloat(newItem.price),
      categoryId: parseInt(newItem.categoryId),
      majorIngredients: (newItem.majorIngredients || []).map((mi) => ({
        ingredientId: mi.ingredientId,
        quantityPerItem: parseFloat(mi.quantityPerItem || 0),
      })),
    };

    if (editingItem) {
      await menuAPI.updateItem(editingItem.id, data);
      setSuccess('Menu item updated successfully');
    } else {
      await menuAPI.createItem(data);
      setSuccess('Menu item created successfully');
    }

    setShowItemModal(false);
    setEditingItem(null);
    setNewItem({
      categoryId: '',
      name: '',
      price: '',
      available: true,
      majorIngredients: [],
    });
    setIngredientSearch("");
    loadMenuData();
  } catch (err) {
    console.error(err);
    setError('Failed to save menu item');
  }
  setLoading(false);
};

  const handleEditItem = (item) => {
  setEditingItem(item);
  setNewItem({
    categoryId: item.categoryId,
    name: item.name,
    price: item.price,
    available: item.available,
    majorIngredients: (item.ingredients || []).map((mi) => ({
      ingredientId: mi.ingredientId,
      name: mi.ingredientName,
      quantityPerItem: mi.quantityPerItem,
    })),
  });
  setIngredientSearch("");
  setShowItemModal(true);
};

  const handleDeleteItem = async (id) => {
    if (!window.confirm('Delete this item?')) return;
    try {
      await menuAPI.deleteItem(id);
      loadMenuData();
      setSuccess('Menu item deleted successfully');
    } catch (err) {
      setError('Failed to delete item');
    }
  };

  const loadExpiring = async () => {
  try {
    const res = await batchAPI.getExpiring(7);
    setExpiringBatches(res.data || []);
  } catch (err) {
    console.error(err);
  }
};

  const handleCreateOrUpdateIngredient = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = { ...newIngredient, currentStock: parseFloat(newIngredient.currentStock), threshold: parseFloat(newIngredient.threshold) };
      if (editingIngredient) {
        await ingredientAPI.update(editingIngredient.id, data);
        setSuccess('Ingredient updated successfully');
      } else {
        await ingredientAPI.create(data);
        setSuccess('Ingredient created successfully');
      }
      setShowIngredientModal(false);
      setEditingIngredient(null);
      setNewIngredient({ name: '', currentStock: '', threshold: '', unit: 'Kg', expiryDate: '' });
      loadIngredients();
    } catch (err) {
      setError('Failed to save ingredient');
    }
    setLoading(false);
  };

  const handleEditIngredient = (ing) => {
    setEditingIngredient(ing);
    setNewIngredient({ name: ing.name, currentStock: ing.currentStock, threshold: ing.threshold, unit: ing.unit, expiryDate: ing.expiryDate || '' });
    setShowIngredientModal(true);
  };

  const handleDeleteIngredient = async (id) => {
    if (!window.confirm('Delete this ingredient?')) return;
    try {
      await ingredientAPI.delete(id);
      loadIngredients();
      setSuccess('Ingredient deleted successfully');
    } catch (err) {
      setError('Failed to delete ingredient');
    }
  };

  const handleCreateOrUpdateTable = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = { tableNumber: parseInt(newTable.tableNumber), capacity: parseInt(newTable.capacity) };
      if (editingTable) {
        await tableAPI.update(editingTable.id, data);
        setSuccess('Table updated successfully');
      } else {
        await tableAPI.create(data);
        setSuccess('Table created successfully');
      }
      setShowTableModal(false);
      setEditingTable(null);
      setNewTable({ tableNumber: '', capacity: '' });
      loadTables();
    } catch (err) {
      setError('Failed to save table');
    }
    setLoading(false);
  };

  const handleEditTable = (table) => {
    setEditingTable(table);
    setNewTable({ tableNumber: table.tableNumber, capacity: table.capacity });
    setShowTableModal(true);
  };

  const handleDeleteTable = async (id) => {
    if (!window.confirm('Delete this table?')) return;
    try {
      await tableAPI.delete(id);
      loadTables();
      setSuccess('Table deleted successfully');
    } catch (err) {
      setError('Failed to delete table');
    }
  };

  const handleSendIssue = async () => {
  if (!issueMessage.trim()) {
    setError('Please enter an issue message');
    return;
  }
  setLoading(true);
  try {
    await alertAPI.create({ message: issueMessage, fromRole: 'INVENTORY' });
    setIssueMessage('');
    setSuccess('Issue sent to Owner successfully');
    setTimeout(() => setSuccess(''), 3000);
  } catch (err) {
    setError('Failed to send issue');
  }
  setLoading(false);
};

  return (
    <DashboardLayout title="Inventory Management">

      {metrics && (
  <div className="grid grid-cols-4 gap-4 mb-6">

    <div className="card p-4">
      <h3 className="text-sm font-semibold text-gray-500">Stockouts this week</h3>
      <p className="text-2xl font-bold">
        {metrics.stockoutsThisWeek}
      </p>
    </div>

    <div className="card p-4">
      <h3 className="text-sm font-semibold text-gray-500">Expiry risk</h3>
      <p className="text-xl font-bold">
        {metrics.expiryRiskLevel}
      </p>
    </div>
  </div>
)}

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

      {/* MENU TAB */}
      {activeTab === 'menu' && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold">Menu Management</h2>
            <div className="flex gap-2">
              <button onClick={() => setShowCategoryModal(true)} className="btn btn-secondary">+ Add Category</button>
              <button onClick={() => { setEditingItem(null); setNewItem({ categoryId: '', name: '', price: '', available: true, majorIngredients: [] }); setShowItemModal(true); }} className="btn btn-primary">+ Add Menu Item</button>
            </div>
          </div>

          {categories.length === 0 ? (
            <div className="card text-center"><p className="text-gray-600">No categories yet. Create one to get started!</p></div>
          ) : (
            categories.map((category) => (
              <div key={category.id} className="card mb-4">
                <div className="flex justify-between items-center mb-4">
                  <h3 className="text-lg font-semibold">{category.name}</h3>
                  <button onClick={() => handleDeleteCategory(category.id)} className="btn btn-danger" style={{ padding: '0.5rem 1rem', fontSize: '0.875rem' }}>Delete Category</button>
                </div>
                <div className="grid grid-cols-3">
                  {menuItems.filter((item) => item.categoryId === category.id).map((item) => (
                    <div key={item.id} className="card" style={{ padding: '1rem' }}>
                      <div className="flex justify-between items-start mb-2">
                        <h4 className="font-semibold">{item.name}</h4>
                        <button onClick={() => handleDeleteItem(item.id)} style={{ background: 'none', border: 'none', color: 'var(--error)', cursor: 'pointer', fontSize: '1.5rem' }}>×</button>
                      </div>
                      <p className="text-lg font-bold" style={{ color: 'var(--primary-600)' }}>₹{item.price}</p>
                      <span className="badge" style={{ backgroundColor: item.available ? 'var(--success)' : 'var(--gray-300)', color: 'white', marginTop: '0.5rem' }}>
                        {item.available ? 'Available' : 'Unavailable'}
                      </span>
                      <button onClick={() => handleEditItem(item)} className="btn btn-secondary" style={{ width: '100%', marginTop: '0.5rem', padding: '0.5rem', fontSize: '0.75rem' }}>Edit</button>
                    </div>
                  ))}
                </div>
                {menuItems.filter((item) => item.categoryId === category.id).length === 0 && <p className="text-gray-500 text-sm">No items in this category</p>}
              </div>
            ))
          )}
        </div>
      )}

      {/* INGREDIENTS TAB */}
      {activeTab === 'ingredients' && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold">Ingredients</h2>
            <button onClick={() => { setEditingIngredient(null); setNewIngredient({ name: '', currentStock: '', threshold: '', unit: 'Kg', expiryDate: '' }); setShowIngredientModal(true); }} className="btn btn-primary">+ Add Ingredient</button>
          </div>
              {/* Prep Today section */}
    
      <div className="card mb-4 p-4 bg-blue-50">
        <h3 className="text-lg font-semibold mb-2">Prep today (suggested)</h3>

        {!Array.isArray(prepToday) || prepToday.length === 0 ? (
      <p className="text-sm text-gray-500">No data.</p>
    ) : (
        <ul className="list-disc list-inside text-sm text-gray-700">
          {prepToday.map((item) => (
    <li key={item.ingredientId}>
      <span className="font-semibold">{item.ingredientName}</span>
      {" – "}
      {item.recommendedQuantity != null
        ? item.recommendedQuantity.toFixed(1)
        : "-"}{" "}
      {item.unit}
      {" "}
      <span className="uppercase text-xs text-gray-500">
        ({item.confidenceLevel})
      </span>
    </li>
  ))}
        </ul>
    )}
      </div>

<br></br>
    <div className="card mb-4 p-4">
  <div className="flex items-end gap-2">
    <div>
      <label className="block text-xs font-semibold text-gray-500">
        Forecast date
      </label>
      <input
        type="date"
        className="form-input text-sm"
        value={forecastDate}
        onChange={(e) => setForecastDate(e.target.value)}
      />
    </div>
    <button
      className="btn btn-secondary text-sm mb-0.5"
      onClick={loadForecast}
      disabled={!forecastDate}
    >
      Load forecast
    </button>
  </div>

    <div className="mt-3">
      <h3 className="text-sm font-semibold mb-1">
        Forecast for {forecastDate}
      </h3>
       {!forecastDate ? (
      <p className="text-xs text-gray-500">Select a date and click Load forecast.</p>
    ) : !Array.isArray(forecast) || forecast.length === 0 ? (
      <p className="text-xs text-gray-500">No data.</p>
    ) : (
      <ul className="list-disc list-inside text-xs text-gray-700 max-h-40 overflow-auto">
        {forecast.map((item) => (
          <li key={item.ingredientId}>
            <span className="font-semibold">{item.ingredientName}</span>
            {" – "}
            {item.recommendedQuantity != null
      ? item.recommendedQuantity.toFixed(1)
      : "-"}{" "}
       {item.unit}
            {" "}
            <span className="uppercase text-[10px] text-gray-500">
              ({item.confidenceLevel})
            </span>
          </li>
        ))}
      </ul>
    )}
    </div>
</div>

<br></br>
  <div className="card mb-4 p-4 bg-red-50">
    <h3 className="text-lg font-semibold mb-2">Expiring within 7 days</h3>
    {!Array.isArray(expiringBatches) || expiringBatches.length === 0 ? (
      <p className="text-sm text-gray-500">No data.</p>
    ) : (
    <table className="table text-sm">
      <thead>
        <tr>
          <th>Ingredient</th>
          <th>Qty</th>
          <th>Expiry</th>
          <th>Risk</th>
        </tr>
      </thead>
      <tbody>
        {expiringBatches.map((b) => (
          <tr key={b.id}>
            <td>{b.ingredientName}</td>
            <td>
              {b.quantity} {b.unit}
            </td>
            <td>{b.expiryDate}</td>
            <td>{b.expiryRiskLevel}</td>
          </tr>
        ))}
      </tbody>
    </table>
    )}
  </div>

<br></br>
  <div className="card mb-4 p-4 bg-green-50">
  <div className="flex justify-between items-center mb-3">
    <div>
      <h3 className="text-lg font-semibold">NGO's / Shelter Contacts</h3>
      <p className="text-sm text-gray-600">
        Inform nearby organizations when food is left over.
      </p>
    </div>

    <button
      onClick={() => {
        setEditingContact(null);
        setNewContact({
          name: '',
          email: '',
          countryCode: '+91',
          phoneNumber: '',
        });
        setShowContactModal(true);
      }}
      className="btn btn-primary"
    >
      + Add Contact
    </button>
  </div>

<br></br>
  {contacts.length === 0 ? (
    <p className="text-sm text-gray-500">No contacts added yet.</p>
  ) : (
    <div className="flex flex-col gap-3">
      {contacts.map((contact) => (
        <div
          key={contact.id}
          className="card"
          style={{
            padding: '0.9rem 1rem',
            border: '1px solid var(--gray-200)',
            background: '#a7d8ef'
          }}
        >
          <div className="flex justify-between items-center gap-4">
            <div style={{ flex: 1 }}>
              <div className="font-semibold">{contact.name}</div>
              <div className="text-sm text-gray-600">{contact.email}</div>
              <div className="text-sm text-gray-600">
                {contact.countryCode} {contact.phoneNumber}
              </div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => handleOpenSendModal(contact)}
                className="btn btn-primary"
                style={{ padding: '0.4rem 0.9rem', fontSize: '0.8rem' }}
              >
                Send
              </button>

              <button
                onClick={() => handleEditContact(contact)}
                className="btn btn-secondary"
                style={{ padding: '0.4rem 0.9rem', fontSize: '0.8rem' }}
              >
                Edit
              </button>

              <button
                onClick={() => handleDeleteContact(contact.id)}
                className="btn btn-danger"
                style={{ padding: '0.4rem 0.9rem', fontSize: '0.8rem' }}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  )}
</div>

<br></br>
  <div className="card">
  {ingredients.length === 0 ? (
    <p className="text-center text-gray-600">No ingredients yet</p>
  ) : (
    <table className="table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Stock</th>
          <th>Threshold</th>
          <th>Unit</th>
          <th>Expiry</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {ingredients.map((ing) => (
          <tr key={ing.id}>
            <td>{ing.name}</td>
            <td>{ing.currentStock}</td>
            <td>{ing.threshold}</td>
            <td>{ing.unit}</td>
            <td>{ing.expiryDate || '-'}</td>
            <td>
  {ing.expired ? (
    <span
      className="badge"
      style={{ backgroundColor: 'var(--error)', color: 'white' }}
    >
      Expired
    </span>
  ) : ing.lowStock ? (
    <span
      className="badge"
      style={{ backgroundColor: 'var(--error)', color: 'white' }}
    >
      Low Stock
    </span>
  ) : (
    <span
      className="badge"
      style={{ backgroundColor: 'var(--success)', color: 'white' }}
    >
      OK
    </span>
  )}
</td>
            <td>
              <div className="flex gap-2">
                <button
                  onClick={() => handleEditIngredient(ing)}
                  className="btn btn-secondary"
                  style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem' }}
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDeleteIngredient(ing.id)}
                  className="btn btn-danger"
                  style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem' }}
                >
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

      {/* TABLES TAB */}
      {activeTab === 'tables' && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold">Tables</h2>
            <button onClick={() => { setEditingTable(null); setNewTable({ tableNumber: '', capacity: '' }); setShowTableModal(true); }} className="btn btn-primary">+ Add Table</button>
          </div>
          <div className="grid grid-cols-4">
            {tables.map((table) => (
              <div key={table.id} className="card text-center">
                <h3 className="text-xl font-bold">Table {table.tableNumber}</h3>
                <p className="text-gray-600 mt-2">Capacity: {table.capacity}</p>
                <span className="badge" style={{ backgroundColor: table.status === 'FREE' ? 'var(--success)' : 'var(--warning)', color: 'white', marginTop: '0.5rem' }}>{table.status}</span>
                <div className="flex gap-2" style={{ marginTop: '1rem' }}>
                  <button onClick={() => handleEditTable(table)} className="btn btn-secondary" style={{ flex: 1, padding: '0.5rem', fontSize: '0.75rem' }}>Edit</button>
                  <button onClick={() => handleDeleteTable(table.id)} className="btn btn-danger" style={{ flex: 1, padding: '0.5rem', fontSize: '0.75rem' }}>Delete</button>
                </div>
              </div>
            ))}
            {tables.length === 0 && <div className="card text-center" style={{ gridColumn: '1 / -1' }}><p className="text-gray-600">No tables yet</p></div>}
          </div>
        </div>
      )}

      {/* MODALS */}
      <Modal isOpen={showCategoryModal} onClose={() => setShowCategoryModal(false)} title="Add Category">
        <form onSubmit={handleCreateCategory}>
          <div className="form-group">
            <label className="form-label">Category Name</label>
            <input type="text" className="form-input" placeholder="e.g., Main Course" value={newCategory} onChange={(e) => setNewCategory(e.target.value)} required />
          </div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => setShowCategoryModal(false)} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Creating...' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showItemModal} onClose={() => { setShowItemModal(false); setEditingItem(null); }} title={editingItem ? 'Edit Menu Item' : 'Add Menu Item'}>
        <form onSubmit={handleCreateOrUpdateItem}>
          <div className="form-group">
            <label className="form-label">Category</label>
            <select className="form-select" value={newItem.categoryId} onChange={(e) => setNewItem({ ...newItem, categoryId: e.target.value })} required>
              <option value="">Select Category</option>
              {categories.map((cat) => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Item Name</label>
            <input type="text" className="form-input" placeholder="e.g., Chicken Biryani" value={newItem.name} onChange={(e) => setNewItem({ ...newItem, name: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Price (₹)</label>
            <input type="number" step="0.01" className="form-input" placeholder="e.g., 250" value={newItem.price} onChange={(e) => setNewItem({ ...newItem, price: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input type="checkbox" checked={newItem.available} onChange={(e) => setNewItem({ ...newItem, available: e.target.checked })} />
              Available
            </label>
          </div>
          <div className="form-group">
  <label className="form-label">Major Ingredients Required</label>

  {/* Search bar */}
  <input
    type="text"
    className="form-input mb-2"
    placeholder="Search ingredients..."
    value={ingredientSearch}
    onChange={(e) => setIngredientSearch(e.target.value)}
  />

  {/* Available ingredients list */}
  <div className="border rounded p-2 max-h-40 overflow-auto mb-2">
    {filteredIngredients.length === 0 ? (
      <p className="text-xs text-gray-500">
        No ingredients found. Create them in the Ingredients tab.
      </p>
    ) : (
      filteredIngredients.map((ing) => {
        const alreadySelected = (newItem.majorIngredients || []).some(
          (mi) => mi.ingredientId === ing.id
        );
        return (
          <div
            key={ing.id}
            className="flex justify-between items-center text-sm py-1 border-b last:border-b-0"
          >
            <span>{ing.name}</span>
            <button
              type="button"
              className="btn btn-secondary"
              style={{ padding: '0.15rem 0.5rem', fontSize: '0.7rem' }}
              disabled={alreadySelected}
              onClick={() =>
                setNewItem({
                  ...newItem,
                  majorIngredients: [
                    ...newItem.majorIngredients,
                    {
                      ingredientId: ing.id,
                      name: ing.name,
                      quantityPerItem: 0,
                    },
                  ],
                })
              }
            >
              {alreadySelected ? 'Added' : 'Add'}
            </button>
          </div>
        );
      })
    )}
  </div>

  {/* Selected ingredients with quantity */}
  {(newItem.majorIngredients || []).length > 0 && (
    <div className="border rounded p-2">
      {newItem.majorIngredients.map((mi) => (
        <div
          key={mi.ingredientId}
          className="flex items-center gap-2 text-sm mb-1"
        >
          <span className="flex-1">{mi.name}</span>

          <input
            type="number"
            step="0.01"
            className="form-input"
            style={{ width: '90px' }}
            placeholder="Qty"
            value={mi.quantityPerItem}
            onChange={(e) => {
              const val = parseFloat(e.target.value) || 0;
              setNewItem({
                ...newItem,
                majorIngredients: newItem.majorIngredients.map((x) =>
                  x.ingredientId === mi.ingredientId
                    ? { ...x, quantityPerItem: val }
                    : x
                ),
              });
            }}
          />

          <span className="text-xs text-gray-500">
            {
              (ingredients.find((ing) => ing.id === mi.ingredientId) || {})
                .unit
            }
          </span>

          <button
            type="button"
            className="btn btn-danger"
            style={{ padding: '0.15rem 0.5rem', fontSize: '0.7rem' }}
            onClick={() =>
              setNewItem({
                ...newItem,
                majorIngredients: newItem.majorIngredients.filter(
                  (x) => x.ingredientId !== mi.ingredientId
                ),
              })
            }
          >
            ×
          </button>
        </div>
      ))}
    </div>
  )}
</div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => { setShowItemModal(false); setEditingItem(null); }} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Saving...' : editingItem ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showIngredientModal} onClose={() => { setShowIngredientModal(false); setEditingIngredient(null); }} title={editingIngredient ? 'Edit Ingredient' : 'Add Ingredient'}>
        <form onSubmit={handleCreateOrUpdateIngredient}>
          <div className="form-group">
            <label className="form-label">Name</label>
            <input type="text" className="form-input" placeholder="e.g., Rice" value={newIngredient.name} onChange={(e) => setNewIngredient({ ...newIngredient, name: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Current Stock</label>
            <input type="number" step="0.01" className="form-input" placeholder="e.g., 50" value={newIngredient.currentStock} onChange={(e) => setNewIngredient({ ...newIngredient, currentStock: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Threshold</label>
            <input type="number" step="0.01" className="form-input" placeholder="e.g., 10" value={newIngredient.threshold} onChange={(e) => setNewIngredient({ ...newIngredient, threshold: e.target.value })} required />
          </div>
          <div className="form-group">
  <label className="form-label">Unit</label>
  <select
    className="form-select"
    value={newIngredient.unit}
    onChange={(e) =>
      setNewIngredient({ ...newIngredient, unit: e.target.value })
    }
    required
  >
    <option value="Kg">Kg</option>
    <option value="Litres">Litres</option>
    <option value="Units">Units</option>
  </select>
</div>
<div className="form-group">
  <label className="form-label">Expiry Date</label>
  <input
    type="date"
    className="form-input"
    value={newIngredient.expiryDate}
    onChange={(e) =>
      setNewIngredient({ ...newIngredient, expiryDate: e.target.value })
    }
  />
</div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => { setShowIngredientModal(false); setEditingIngredient(null); }} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Saving...' : editingIngredient ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={showTableModal} onClose={() => { setShowTableModal(false); setEditingTable(null); }} title={editingTable ? 'Edit Table' : 'Add Table'}>
        <form onSubmit={handleCreateOrUpdateTable}>
          <div className="form-group">
            <label className="form-label">Table Number</label>
            <input type="number" className="form-input" placeholder="e.g., 1" value={newTable.tableNumber} onChange={(e) => setNewTable({ ...newTable, tableNumber: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Capacity</label>
            <input type="number" className="form-input" placeholder="e.g., 4" value={newTable.capacity} onChange={(e) => setNewTable({ ...newTable, capacity: e.target.value })} required />
          </div>
          <div className="flex gap-2 justify-end">
            <button type="button" onClick={() => { setShowTableModal(false); setEditingTable(null); }} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Saving...' : editingTable ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </Modal>
      <Modal
  isOpen={showContactModal}
  onClose={() => {
    setShowContactModal(false);
    setEditingContact(null);
  }}
  title={editingContact ? 'Edit Contact' : 'Add Contact'}
>
  <form onSubmit={handleCreateOrUpdateContact}>
    <div className="form-group">
      <label className="form-label">Name</label>
      <input
        type="text"
        className="form-input"
        placeholder="e.g., Helping Hands NGO"
        value={newContact.name}
        onChange={(e) => setNewContact({ ...newContact, name: e.target.value })}
        required
      />
    </div>

    <div className="form-group">
      <label className="form-label">Gmail ID</label>
      <input
        type="email"
        className="form-input"
        placeholder="e.g., ngo@gmail.com"
        value={newContact.email}
        onChange={(e) => setNewContact({ ...newContact, email: e.target.value })}
        required
      />
    </div>

    <div className="form-group">
      <label className="form-label">Country Code</label>
      <input
        type="text"
        className="form-input"
        placeholder="e.g., +91"
        value={newContact.countryCode}
        onChange={(e) => setNewContact({ ...newContact, countryCode: e.target.value })}
        required
      />
    </div>

    <div className="form-group">
      <label className="form-label">Contact Number</label>
      <input
        type="text"
        className="form-input"
        placeholder="e.g., 9876543210"
        value={newContact.phoneNumber}
        onChange={(e) => setNewContact({ ...newContact, phoneNumber: e.target.value })}
        required
      />
    </div>

    <div className="flex gap-2 justify-end">
      <button
        type="button"
        onClick={() => {
          setShowContactModal(false);
          setEditingContact(null);
        }}
        className="btn btn-secondary"
      >
        Cancel
      </button>
      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? 'Saving...' : editingContact ? 'Update' : 'Add'}
      </button>
    </div>
  </form>
</Modal>
<Modal
  isOpen={showSendContactModal}
  onClose={() => {
    setShowSendContactModal(false);
    setSelectedContact(null);
  }}
  title="Send Leftover Food Message"
>
  <form onSubmit={handleSendContactMessage}>
    <div className="form-group">
      <label className="form-label">Name</label>
      <input
        type="text"
        className="form-input"
        value={selectedContact?.name || ''}
        readOnly
      />
    </div>

    <div className="form-group">
      <label className="form-label">Phone Number</label>
      <input
        type="text"
        className="form-input"
        value={
          selectedContact
            ? `${selectedContact.countryCode} ${selectedContact.phoneNumber}`
            : ''
        }
        readOnly
      />
    </div>

    <div className="form-group">
      <label className="form-label">Email</label>
      <input
        type="text"
        className="form-input"
        value={selectedContact?.email || ''}
        readOnly
      />
    </div>

    <div className="form-group">
      <label className="form-label">Subject</label>
      <input
        type="text"
        className="form-input"
        value={contactMessageData.subject}
        onChange={(e) =>
          setContactMessageData({
            ...contactMessageData,
            subject: e.target.value,
          })
        }
        required
      />
    </div>

    <div className="form-group">
      <label className="form-label">Message</label>
      <textarea
        className="form-input"
        rows="6"
        value={contactMessageData.message}
        onChange={(e) =>
          setContactMessageData({
            ...contactMessageData,
            message: e.target.value,
          })
        }
        required
      />
    </div>

    <div className="flex gap-2 justify-end">
      <button
        type="button"
        onClick={() => {
          setShowSendContactModal(false);
          setSelectedContact(null);
        }}
        className="btn btn-secondary"
      >
        Cancel
      </button>
      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? 'Sending...' : 'Send'}
      </button>
    </div>
  </form>
</Modal>

      {/* Issue Reporter */}
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

    </DashboardLayout>
  );
};

export default InventoryDashboard;

