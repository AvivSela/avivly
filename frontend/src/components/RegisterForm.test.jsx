import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import RegisterForm from './RegisterForm';
import * as api from '../api';

vi.mock('../api', () => ({
  register: vi.fn(),
}));

beforeEach(() => localStorage.clear());

const renderForm = () =>
  render(
    <MemoryRouter>
      <RegisterForm />
    </MemoryRouter>
  );

test('renders email, password, and confirm-password inputs', () => {
  renderForm();
  expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
});

test('password mismatch shows error without calling API', async () => {
  renderForm();

  fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'x@y.com' } });
  fireEvent.change(screen.getByLabelText(/^password$/i), { target: { value: 'password123' } });
  fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'different' } });
  fireEvent.click(screen.getByRole('button', { name: /register/i }));

  expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
  expect(api.register).not.toHaveBeenCalled();
});

test('successful register stores token', async () => {
  api.register.mockResolvedValue({ data: { token: 'tok456', email: 'x@y.com' } });
  renderForm();

  fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'x@y.com' } });
  fireEvent.change(screen.getByLabelText(/^password$/i), { target: { value: 'password123' } });
  fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'password123' } });
  fireEvent.click(screen.getByRole('button', { name: /register/i }));

  await waitFor(() => {
    expect(localStorage.getItem('token')).toBe('tok456');
  });
});

test('409 response shows email-taken error', async () => {
  api.register.mockRejectedValue({ response: { status: 409 } });
  renderForm();

  fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'dup@y.com' } });
  fireEvent.change(screen.getByLabelText(/^password$/i), { target: { value: 'password123' } });
  fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'password123' } });
  fireEvent.click(screen.getByRole('button', { name: /register/i }));

  await waitFor(() => {
    expect(screen.getByText(/email is already taken/i)).toBeInTheDocument();
  });
});
