// import axios from 'axios';

// const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// const api = axios.create({
//   baseURL: API_BASE_URL,
//   withCredentials: true,
//   headers: {
//     'Content-Type': 'application/json',
//   },
// });

// // Add token to requests
// api.interceptors.request.use((config) => {
//   const token = localStorage.getItem('token');
//   if (token) {
//     config.headers.Authorization = `Bearer ${token}`;
//   }
//   return config;
// });

// // Auth APIs
// export const authAPI = {
//   login: (data) => api.post('/api/v1/auth/login', data),
//   register: (data) => api.post('/restaurants/register', data),
// };

// // Restaurant APIs
// export const restaurantAPI = {
//   getAll: () => api.get('/restaurants'),
// };

// // Menu APIs
// export const menuAPI = {
//   getCategories: () => api.get('/menu/categories'),
//   createCategory: (name) => api.post('/menu/categories', { name }),
//   deleteCategory: (id) => api.delete(`/menu/categories/${id}`),
//   getItems: () => api.get('/menu/items'),
//   createItem: (data) => api.post('/menu/items', data),
//   updateItem: (id, data) => api.put(`/menu/items/${id}`, data),
//   deleteItem: (id) => api.delete(`/menu/items/${id}`),
// };

// // Table APIs
// export const tableAPI = {
//   getAll: () => api.get('/tables'),
//   create: (data) => api.post('/tables', data),
//   update: (id, data) => api.put(`/tables/${id}`, data),
//   delete: (id) => api.delete(`/tables/${id}`),
// };

// // Ingredient APIs
// export const ingredientAPI = {
//   getAll: () => api.get('/ingredients'),
//   getWithBatches: () => api.get('/ingredients/with-batches'),
//   create: (data) => api.post('/ingredients', data),
//   update: (id, data) => api.put(`/ingredients/${id}`, data),
//   delete: (id) => api.delete(`/ingredients/${id}`),
// };

// // Employee APIs
// export const employeeAPI = {
//   getAll: () => api.get('/employees'),
//   create: (data) => api.post('/employees', data),
//   update: (id, data) => api.put(`/employees/${id}`, data),
//   delete: (id) => api.delete(`/employees/${id}`),
// };

// // Order APIs
// export const orderAPI = {
//   create: (data) => api.post('/orders', data),
//   getAll: (status) => api.get('/orders', { params: { status } }),
//   getKitchen: () => api.get('/orders/kitchen'),
//   getReady: () => api.get('/orders/ready'),
//   startCooking: (id) => api.put(`/orders/${id}/start-cooking`),
//   markReady: (id) => api.put(`/orders/${id}/mark-ready`),
//   markServed: (id) => api.put(`/orders/${id}/mark-served`),
// };

// // Billing APIs
// export const billingAPI = {
//   create: (data) => api.post('/bills', data),
//   getByOrder: (orderId) => api.get(`/bills/order/${orderId}`),
// };

// export const userAPI = {
//   getAll: () => api.get('/users'),
//   create: (data) => api.post('/users', data),
//   update: (id, data) => api.put(`/users/${id}`, data),
//   delete: (id) => api.delete(`/users/${id}`),
// };

// export const alertAPI = {
//   create: (data) => api.post('/alerts', data),
//   getUnread: () => api.get('/alerts'),
//   markDone: (id) => api.delete(`/alerts/${id}`),
// };

// export const analyticsAPI = {
//   get: () => api.get('/analytics'),
// };

// export const investmentAPI = {
//   getAll: () => api.get('/investments'),
//   create: (data) => api.post('/investments', data),
//   delete: (id) => api.delete(`/investments/${id}`),
// };

// export const salaryAPI = {
//   getAll: () => api.get('/salaries'),
//   pay: (employeeId) => api.post(`/salaries/${employeeId}/pay`),
// };

// export const rootAdminAPI = {
//   getAllRestaurants: () => api.get('/root/restaurants'),
//   enableRestaurant: (id) => api.put(`/root/restaurants/${id}/enable`),
//   disableRestaurant: (id) => api.put(`/root/restaurants/${id}/disable`),
//   deleteRestaurant: (id) => api.delete(`/root/restaurants/${id}`),
//   getAllFranchises: () => api.get('/root/franchises'),
//   enableFranchise: (id) => api.put(`/root/franchises/${id}/enable`),
//   disableFranchise: (id) => api.put(`/root/franchises/${id}/disable`),
//   deleteFranchise: (id) => api.delete(`/root/franchises/${id}`),
// };

// export const passwordResetAPI = {
//   sendOTP: (email) => api.post('/password-reset/send-otp', { email }),
//   verifyOTP: (email, otp) => api.post('/password-reset/verify-otp', { email, otp }),
//   resetPassword: (email, otp, newPassword) => api.post('/password-reset/reset', { email, otp, newPassword }),
// };

// export const feedbackAPI = {
//   submitPublic: (restaurantCode, data) =>
//     api.post(`/feedback/public/${restaurantCode}`, data),
//   getAnalytics: () => api.get("/feedback/analytics"),
// };

// export const activityAPI = {
//   getRecent: () => api.get("/activity"),
// };

// export const aiAPI = {
//   getSalesInsights: () => api.get("/ai/sales-insights"),
//   chat: (data) => api.post("/ai/chat", data),
// };

// // Franchise APIs
// export const franchiseAPI = {
//   register: (data) => api.post('/franchises/register', data),
//   getRestaurants: () => api.get('/franchises/restaurants'),
//   getAnalytics: () => api.get('/franchises/analytics'),
//   getSalesInsights: () => api.get('/franchises/ai/sales-insights'),
//   chat: (data) => api.post('/franchises/ai/chat', data),
//   impersonate: (restaurantCode) => api.post(`/franchises/impersonate/${restaurantCode}`)
// };

// // FIXED: Use 'api' instance instead of 'axios' directly
// export const inventoryDashboardAPI = {
//   getMetrics: () => api.get("/inventory-dashboard/metrics"),
// };

// export const prepPlanAPI = {
//   getToday: () => api.get("/prep-plan/today"),
// };

// export const batchAPI = {
//   getExpiring: (days = 7) => api.get(`/batches/expiring?days=${days}`),
// };

// export const varianceAPI = {
//   getHighRisk: () => api.get("/stock-variance/high-risk"),
// };

// export const forecastAPI = {
//   getForDate: (dateIso) => api.get(`/forecast?date=${encodeURIComponent(dateIso)}`),
// };

// export const contactAPI = {
//   getAll: () => api.get('/contacts'),
//   create: (data) => api.post('/contacts', data),
//   update: (id, data) => api.put(`/contacts/${id}`, data),
//   delete: (id) => api.delete(`/contacts/${id}`),
//   sendMessage: (data) => api.post('/contacts/send', data),
// };

// export const stockVarianceAPI = {
//   recordPhysicalCount: (data) => api.post('/stock-variance/physical-count', data),
//   getHighRisk: () => api.get('/stock-variance/high-risk'),
//   getToday: () => api.get('/stock-variance/today'),
//   getHistory: (ingredientId) => api.get(`/stock-variance/ingredient/${ingredientId}/history`),
// };

// export const billAPI = {
//   getAll: () => api.get('/bills'),
//   getByOrderId: (orderId) => api.get(`/bills/order/${orderId}`),
// };

// export default api;

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth APIs
export const authAPI = {
  login: (data) => api.post('/api/v1/auth/login', data),
  register: (data) => api.post('/api/v1/restaurants/register', data),
};

// Restaurant APIs
export const restaurantAPI = {
  getAll: () => api.get('/api/v1/restaurants'),
};

// Menu APIs
export const menuAPI = {
  getCategories: () => api.get('/api/v1/menu/categories'),
  createCategory: (name) => api.post('/api/v1/menu/categories', { name }),
  deleteCategory: (id) => api.delete(`/api/v1/menu/categories/${id}`),
  getItems: () => api.get('/api/v1/menu/items'),
  createItem: (data) => api.post('/api/v1/menu/items', data),
  updateItem: (id, data) => api.put(`/api/v1/menu/items/${id}`, data),
  deleteItem: (id) => api.delete(`/api/v1/menu/items/${id}`),
};

// Table APIs
export const tableAPI = {
  getAll: () => api.get('/api/v1/tables'),
  create: (data) => api.post('/api/v1/tables', data),
  update: (id, data) => api.put(`/api/v1/tables/${id}`, data),
  delete: (id) => api.delete(`/api/v1/tables/${id}`),
};

// Ingredient APIs
export const ingredientAPI = {
  getAll: () => api.get('/api/v1/ingredients'),
  getWithBatches: () => api.get('/api/v1/ingredients/with-batches'),
  create: (data) => api.post('/api/v1/ingredients', data),
  update: (id, data) => api.put(`/api/v1/ingredients/${id}`, data),
  delete: (id) => api.delete(`/api/v1/ingredients/${id}`),
};

// Employee APIs
export const employeeAPI = {
  getAll: () => api.get('/api/v1/employees'),
  create: (data) => api.post('/api/v1/employees', data),
  update: (id, data) => api.put(`/api/v1/employees/${id}`, data),
  delete: (id) => api.delete(`/api/v1/employees/${id}`),
};

// Order APIs
export const orderAPI = {
  create: (data) => api.post('/api/v1/orders', data),
  getAll: (status) => api.get('/api/v1/orders', { params: { status } }),
  getKitchen: () => api.get('/api/v1/orders/kitchen'),
  getReady: () => api.get('/api/v1/orders/ready'),
  startCooking: (id) => api.put(`/api/v1/orders/${id}/start-cooking`),
  markReady: (id) => api.put(`/api/v1/orders/${id}/mark-ready`),
  markServed: (id) => api.put(`/api/v1/orders/${id}/mark-served`),
};

// Billing APIs
export const billingAPI = {
  create: (data) => api.post('/api/v1/bills', data),
  getByOrder: (orderId) => api.get(`/api/v1/bills/order/${orderId}`),
};

export const userAPI = {
  getAll: () => api.get('/api/v1/users'),
  create: (data) => api.post('/api/v1/users', data),
  update: (id, data) => api.put(`/api/v1/users/${id}`, data),
  delete: (id) => api.delete(`/api/v1/users/${id}`),
};

export const alertAPI = {
  create: (data) => api.post('/api/v1/alerts', data),
  getUnread: () => api.get('/api/v1/alerts'),
  markDone: (id) => api.delete(`/api/v1/alerts/${id}`),
};

export const analyticsAPI = {
  get: () => api.get('/api/v1/analytics'),
};

export const investmentAPI = {
  getAll: () => api.get('/api/v1/investments'),
  create: (data) => api.post('/api/v1/investments', data),
  delete: (id) => api.delete(`/api/v1/investments/${id}`),
};

export const salaryAPI = {
  getAll: () => api.get('/api/v1/salaries'),
  pay: (employeeId) => api.post(`/api/v1/salaries/${employeeId}/pay`),
};

export const rootAdminAPI = {
  getAllRestaurants: () => api.get('/api/v1/root/restaurants'),
  enableRestaurant: (id) => api.put(`/api/v1/root/restaurants/${id}/enable`),
  disableRestaurant: (id) => api.put(`/api/v1/root/restaurants/${id}/disable`),
  deleteRestaurant: (id) => api.delete(`/api/v1/root/restaurants/${id}`),
  getAllFranchises: () => api.get('/api/v1/root/franchises'),
  enableFranchise: (id) => api.put(`/api/v1/root/franchises/${id}/enable`),
  disableFranchise: (id) => api.put(`/api/v1/root/franchises/${id}/disable`),
  deleteFranchise: (id) => api.delete(`/api/v1/root/franchises/${id}`),
};

export const passwordResetAPI = {
  sendOTP: (email) => api.post('/api/v1/password-reset/send-otp', { email }),
  verifyOTP: (email, otp) => api.post('/api/v1/password-reset/verify-otp', { email, otp }),
  resetPassword: (email, otp, newPassword) =>
    api.post('/api/v1/password-reset/reset', { email, otp, newPassword }),
};

export const feedbackAPI = {
  submitPublic: (restaurantCode, data) =>
    api.post(`/api/v1/feedback/public/${restaurantCode}`, data),
  getAnalytics: () => api.get('/api/v1/feedback/analytics'),
};

export const activityAPI = {
  getRecent: () => api.get('/api/v1/activity'),
};

export const aiAPI = {
  getSalesInsights: () => api.get('/api/v1/ai/sales-insights'),
  chat: (data) => api.post('/api/v1/ai/chat', data),
};

// Franchise APIs
export const franchiseAPI = {
  register: (data) => api.post('/api/v1/franchises/register', data),
  getRestaurants: () => api.get('/api/v1/franchises/restaurants'),
  getAnalytics: () => api.get('/api/v1/franchises/analytics'),
  getSalesInsights: () => api.get('/api/v1/franchises/ai/sales-insights'),
  chat: (data) => api.post('/api/v1/franchises/ai/chat', data),
  impersonate: (restaurantCode) => api.post(`/api/v1/franchises/impersonate/${restaurantCode}`),
};

export const inventoryDashboardAPI = {
  getMetrics: () => api.get('/api/v1/inventory-dashboard/metrics'),
};

export const prepPlanAPI = {
  getToday: () => api.get('/api/v1/prep-plan/today'),
};

export const batchAPI = {
  getExpiring: (days = 7) => api.get(`/api/v1/batches/expiring?days=${days}`),
};

export const varianceAPI = {
  getHighRisk: () => api.get('/api/v1/stock-variance/high-risk'),
};

export const forecastAPI = {
  getForDate: (dateIso) => api.get(`/api/v1/forecast?date=${encodeURIComponent(dateIso)}`),
};

export const contactAPI = {
  getAll: () => api.get('/api/v1/contacts'),
  create: (data) => api.post('/api/v1/contacts', data),
  update: (id, data) => api.put(`/api/v1/contacts/${id}`, data),
  delete: (id) => api.delete(`/api/v1/contacts/${id}`),
  sendMessage: (data) => api.post('/api/v1/contacts/send', data),
};

export const stockVarianceAPI = {
  recordPhysicalCount: (data) => api.post('/api/v1/stock-variance/physical-count', data),
  getHighRisk: () => api.get('/api/v1/stock-variance/high-risk'),
  getToday: () => api.get('/api/v1/stock-variance/today'),
  getHistory: (ingredientId) => api.get(`/api/v1/stock-variance/ingredient/${ingredientId}/history`),
};

export const billAPI = {
  getAll: () => api.get('/api/v1/bills'),
  getByOrderId: (orderId) => api.get(`/api/v1/bills/order/${orderId}`),
};

export default api;
