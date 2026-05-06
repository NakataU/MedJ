import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { changePassword } from '../api/auth';

export function ChangePasswordPage() {
  const { t } = useTranslation();
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setError('');
    setSuccess(false);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    if (form.newPassword !== form.confirmPassword) {
      setError(t('changePassword.errorMismatch'));
      return;
    }

    if (form.newPassword.length < 6) {
      setError(t('changePassword.errorLength'));
      return;
    }

    setLoading(true);
    try {
      await changePassword({ currentPassword: form.currentPassword, newPassword: form.newPassword });
      setSuccess(true);
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch {
      setError(t('changePassword.errorFailed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>{t('changePassword.title')}</h1>

      <div className="change-password-card">
        {success && <div className="auth-success">{t('changePassword.success')}</div>}
        {error && <div className="auth-error">{error}</div>}

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="currentPassword">{t('changePassword.current')}</label>
            <input id="currentPassword" name="currentPassword" type="password"
              value={form.currentPassword} onChange={handleChange} placeholder="••••••••" required autoFocus />
          </div>

          <div className="form-group">
            <label htmlFor="newPassword">{t('changePassword.new')}</label>
            <input id="newPassword" name="newPassword" type="password"
              value={form.newPassword} onChange={handleChange} placeholder="••••••••" required />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">{t('changePassword.confirm')}</label>
            <input id="confirmPassword" name="confirmPassword" type="password"
              value={form.confirmPassword} onChange={handleChange} placeholder="••••••••" required />
          </div>

          <button type="submit" className="auth-submit-btn" disabled={loading}>
            {loading ? t('changePassword.submitting') : t('changePassword.submit')}
          </button>
        </form>
      </div>
    </div>
  );
}
