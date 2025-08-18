import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`Adding token to ${config.method?.toUpperCase()} ${config.url}`);
    } else {
      console.warn(`No token found for ${config.method?.toUpperCase()} ${config.url}`);
    }
    
    console.log(`API Request: ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    console.log(`API Response: ${response.status} ${response.config.method?.toUpperCase()} ${response.config.url}`);
    return response;
  },
  (error) => {
    const { config, response, message } = error;
    
    console.error(`API Error: ${config?.method?.toUpperCase()} ${config?.url}`);
    console.error('Error details:', {
      status: response?.status,
      statusText: response?.statusText,
      data: response?.data,
      message: message
    });
    
    if (response?.status === 401) {
      console.warn('Unauthorized - Token may be expired or invalid');
      localStorage.removeItem('token');
      localStorage.removeItem('isAuthenticated');
    } else if (response?.status === 403) {
      console.warn('Forbidden - Insufficient permissions');
    } else if (response?.status === 404) {
      console.warn('Not Found - Resource does not exist');
    } else if (response?.status >= 500) {
      console.error('Server Error');
    } else if (message === 'Network Error') {
      console.error('Network Error - Check if backend is running');
    } else if (message.includes('timeout')) {
      console.error('Request Timeout');
    }
    
    return Promise.reject(error);
  }
);

export const testConnection = async () => {
  try {
    const response = await api.get('/auth/test-connection');
    return { success: true, message: 'Connected successfully' };
  } catch (error) {
    return { 
      success: false, 
      message: error.message,
      status: error.response?.status 
    };
  }
};

export default api;