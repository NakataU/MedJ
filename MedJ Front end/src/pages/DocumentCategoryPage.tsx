import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import type { AppointmentCreateInput } from '../types';

interface LocationState {
  formData: AppointmentCreateInput;
}

export function DocumentCategoryPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState | null;

  useEffect(() => {
    if (!state) {
      navigate('/appointments', { replace: true });
    }
  }, []);

  return null;
}
