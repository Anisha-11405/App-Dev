import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import AppointmentForm from '../components/AppointmentForm';
import AppointmentList from '../components/AppointmentList';
import * as api from '../utils/api';

describe('Appointment System Integration', () => {
  beforeEach(() => {
    jest.spyOn(api, 'fetchPatients').mockResolvedValue([
      { id: 1, name: 'Alice' },
      { id: 2, name: 'Bob' }
    ]);
    jest.spyOn(api, 'fetchDoctors').mockResolvedValue([
      { id: 1, name: 'Dr. Smith', specialization: 'Cardiology' },
      { id: 2, name: 'Dr. Jane', specialization: 'Neurology' }
    ]);
    jest.spyOn(api, 'createAppointment').mockResolvedValue({});
    jest.spyOn(api, 'updateAppointmentStatus').mockResolvedValue({});
    jest.spyOn(api, 'fetchAppointmentsByPatient').mockImplementation((pid) => {
      if (pid === '1') return Promise.resolve([
        {
          id: 101,
          patient: 1,
          doctor: 1,
          appointmentDate: '2099-12-25',
          appointmentTime: '09:00',
          reason: 'General Checkup',
          status: 'REQUESTED'
        },
        {
          id: 102,
          patient: 1,
          doctor: 2,
          appointmentDate: '2099-12-26',
          appointmentTime: '10:30',
          reason: 'Follow-up',
          status: 'APPROVED'
        }
      ]);
      return Promise.resolve([]);
    });
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  const fillForm = async (overrides = {}) => {
    await screen.findByTestId('patient-select');
    fireEvent.change(screen.getByTestId('patient-select'), { target: { value: overrides.patient || '1' } });
    fireEvent.change(screen.getByTestId('doctor-select'), { target: { value: overrides.doctor || '1' } });
    fireEvent.change(screen.getByTestId('date-input'), { target: { value: overrides.date || '2099-12-30' } });
    fireEvent.change(screen.getByTestId('time-input'), { target: { value: overrides.time || '11:00' } });
    fireEvent.change(screen.getByTestId('reason-input'), { target: { value: overrides.reason || 'Valid Reason' } });
  };

  describe('AppointmentForm Component', () => {
    test('renders all fields correctly', async () => {
      render(<AppointmentForm />);
      await screen.findByTestId('patient-select');
      expect(screen.getByTestId('doctor-select')).toBeInTheDocument();
      expect(screen.getByTestId('date-input')).toBeInTheDocument();
      expect(screen.getByTestId('time-input')).toBeInTheDocument();
      expect(screen.getByTestId('reason-input')).toBeInTheDocument();
    });

    test('disables submit button when form is incomplete', async () => {
      render(<AppointmentForm />);
      await screen.findByTestId('patient-select');
      expect(screen.getByRole('button', { name: /book appointment/i })).not.toBeDisabled();
    });

    test('validates required fields', async () => {
      render(<AppointmentForm />);
      await screen.findByTestId('patient-select');
      fireEvent.click(screen.getByRole('button', { name: /book appointment/i }));
      expect(await screen.findByText(/patient is required/i)).toBeInTheDocument();
    });

    test('handles submission with all valid fields', async () => {
      render(<AppointmentForm />);
      await fillForm();
      fireEvent.click(screen.getByRole('button', { name: /book appointment/i }));
      expect(await screen.findByText(/appointment successfully booked/i)).toBeInTheDocument();
    });

    test('displays server error on API failure', async () => {
      jest.spyOn(api, 'createAppointment').mockRejectedValue(new Error('Server error'));
      render(<AppointmentForm />);
      await fillForm();
      fireEvent.click(screen.getByRole('button', { name: /book appointment/i }));
      expect(await screen.findByText(/server error/i)).toBeInTheDocument();
    });

    test('form clears after successful submit', async () => {
      render(<AppointmentForm />);
      await fillForm();
      fireEvent.click(screen.getByRole('button', { name: /book appointment/i }));
      await screen.findByText(/appointment successfully booked/i);
      expect(screen.getByTestId('reason-input')).toHaveValue('');
    });

    test('submits with different doctor', async () => {
      render(<AppointmentForm />);
      await fillForm({ doctor: '2' });
      fireEvent.click(screen.getByRole('button', { name: /book appointment/i }));
      expect(await screen.findByText(/appointment successfully booked/i)).toBeInTheDocument();
    });
  });

  describe('AppointmentList Component', () => {
    test('shows empty state for no appointments', async () => {
      render(<AppointmentList />);
      fireEvent.change(await screen.findByTestId('patient-filter'), { target: { value: '2' } });
      expect(await screen.findByTestId('no-appointments')).toBeInTheDocument();
    });
  });
});