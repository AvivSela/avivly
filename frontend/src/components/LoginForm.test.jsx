import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import LoginForm from './LoginForm';
import * as api from '../api';

vi.mock('../api', () => ({
  login: vi.fn(),
}));

beforeEach(() => localStorage.clear());

const renderForm = () =>
  render(
    <MemoryRouter>
      <LoginForm />
    </MemoryRouter>
  );

test('renders email and password inputs', () => {
  renderForm();
  expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
});

test('successful login stores token and navigates to /', async () => {
  api.login.mockResolvedValue({ data: { token: 'tok123', email: 'a@b.com' } });
  renderForm();

  fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'a@b.com' } });
  fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } });
  fireEvent.click(screen.getByRole('button', { name: /log in/i }));

  await waitFor(() => {
    expect(localStorage.getItem('token')).toBe('tok123');
  });
});

test('401 response shows error message', async () => {
  api.login.mockRejectedValue({ response: { status: 401 } });
  renderForm();

  fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'a@b.com' } });
  fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrongpass' } });
  fireEvent.click(screen.getByRole('button', { name: /log in/i }));

  await waitFor(() => {
    expect(screen.getByText(/invalid email or password/i)).toBeInTheDocument();
  });
});
