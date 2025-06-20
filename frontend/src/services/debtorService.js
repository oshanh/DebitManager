import apiClient from './apiClient';

const BASE_URL = '/debtors';

const debtorService = {
  getAllDebtors: async () => {
    try {
      const response = await apiClient.get(BASE_URL);
      return response.data;
    } catch (error) {
      console.error('Error fetching debtors:', error);
      throw error;
    }
  },

  getDebtorById: async (debtorId) => {
    try {
      const response = await apiClient.get(`${BASE_URL}/${debtorId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching debtor:', error);
      throw error;
    }
  },

  createDebtor: (debtorData, file) => {
    const formData = new FormData();
    formData.append('debtor', new Blob([JSON.stringify(debtorData)], {
      type: 'application/json'
    }));
    if (file) {
      formData.append('file', file);
    }
    return apiClient.post(BASE_URL, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  updateDebtor: async (debtorId, debtorData, file) => {
    try {
      const formData = new FormData();
      formData.append('debtor', new Blob([JSON.stringify(debtorData)], {
        type: 'application/json'
      }));
      if (file) {
        formData.append('file', file);
      }
      const response = await apiClient.put(`${BASE_URL}/${debtorId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      return response.data;
    } catch (error) {
      console.error('Error updating debtor:', error);
      throw error;
    }
  },

  deleteDebtor: async (debtorId) => {
    try {
      await apiClient.delete(`${BASE_URL}/${debtorId}`);
    } catch (error) {
      console.error('Error deleting debtor:', error);
      throw error;
    }
  },

  getProfilePhotoUrl(filename) {
    if (!filename) return null;
    const actualFilename = filename.split('/').pop();
    return `${apiClient.defaults.baseURL.replace('/api/v1', '')}/api/v1/files/debtors/${actualFilename}`;
  },

  getSecureFileUrl(filename) {
    if (!filename) return null;
    const actualFilename = filename.split('/').pop();
    const url = `${apiClient.defaults.baseURL.replace('/api/v1', '')}/api/v1/files/debtors/${actualFilename}`;
    
    const secureUrl = new URL(url);
    const token = localStorage.getItem('token');
    if (token) {
      secureUrl.searchParams.append('token', token);
    }
    
    return secureUrl.toString();
  }
};

export default debtorService; 