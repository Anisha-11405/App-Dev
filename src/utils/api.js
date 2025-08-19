// import axios from 'axios';

// const api = axios.create({
//   baseURL: 'http://localhost:8080',
//   headers: {
//     'Content-Type': 'application/json'
//   },
//   timeout: 10000
// });

// // Request interceptor - add token to requests
// api.interceptors.request.use(
//   async (config) => {
//     const token = localStorage.getItem('token');
//     if (token) {
//       config.headers.Authorization = `Bearer ${token}`;
//       console.log(`Adding token to ${config.method?.toUpperCase()} ${config.url}`);
//     } else {
//       console.warn(`No token found for ${config.method?.toUpperCase()} ${config.url}`);
//     }
    
//     console.log(`API Request: ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
//     return config;
//   },
//   (error) => {
//     console.error('Request interceptor error:', error);
//     return Promise.reject(error);
//   }
// );

// // Response interceptor - handle errors (NO auto token refresh)
// api.interceptors.response.use(
//   (response) => {
//     console.log(`API Response: ${response.status} ${response.config.method?.toUpperCase()} ${response.config.url}`);
//     return response;
//   },
//   async (error) => {
//     const { config, response, message } = error;
    
//     console.error(`API Error: ${config?.method?.toUpperCase()} ${config?.url}`);
//     console.error('Error details:', {
//       status: response?.status,
//       statusText: response?.statusText,
//       data: response?.data,
//       message: message
//     });
    
//     // Handle different error types
//     if (response?.status === 401) {
//   console.warn('Unauthorized - logging out');
//   localStorage.removeItem('token');
//   window.location.href = '/login'; 
// }

//      else if (response?.status === 403) {
//       console.warn('Forbidden - Insufficient permissions');
//     } else if (response?.status === 404) {
//       console.warn('Not Found - Resource does not exist');
//     } else if (response?.status >= 500) {
//       console.error('Server Error');
//     } else if (message === 'Network Error') {
//       console.error('Network Error - Check if backend is running');
//     } else if (message.includes('timeout')) {
//       console.error('Request Timeout');
//     }
    
//     return Promise.reject(error);
//   }
// );

// export const testConnection = async () => {
//   try {
//     const response = await api.get('/auth/test-connection');
//     return { success: true, message: `Connected: ${response.status}` };
//   } catch (error) {
//     return { 
//       success: false, 
//       message: error.message,
//       status: error.response?.status 
//     };
//   }
// };

// export default api;

import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080", // backend base URL
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

// Attach token before request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`Adding token to ${config.method?.toUpperCase()} ${config.url}`);
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle errors without auto-logout
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.warn("Unauthorized - token invalid or expired");
      // ❌ Don't auto-logout here anymore
      // ✅ Instead just return error so frontend can decide
    } else if (error.response?.status === 403) {
      console.warn("Forbidden - you don't have permission");
    } else if (error.code === "ECONNABORTED") {
      console.error("Request timeout");
    }
    return Promise.reject(error);
  }
);

export default api;
