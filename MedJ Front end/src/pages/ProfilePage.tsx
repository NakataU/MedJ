import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { getProfile, updateProfile } from '../api/auth';

export function ProfilePage() {
  const { t } = useTranslation();
  const { user } = useAuth();

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) return;
    setLoading(true);
    getProfile(user.id)
      .then((p) => {
        setFirstName(p.firstName ?? '');
        setLastName(p.lastName ?? '');
        setPhone(p.phone ?? '');
        setAddress(p.address ?? '');
      })
      .catch(() => setError(t('profile.error')))
      .finally(() => setLoading(false));
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      await updateProfile(user.id, { firstName, lastName, phone, address });
      setSuccess(t('profile.success'));
    } catch {
      setError(t('profile.errorSave'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading">{t('common.loading')}</div>;

  return (
    <div className="page">
      <h1>{t('profile.title')}</h1>

      <div className="change-password-card">
        {success && <div className="auth-success">{success}</div>}
        {error && <div className="auth-error">{error}</div>}

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>{t('profile.firstName')}</label>
            <input
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              placeholder={t('profile.firstName')}
            />
          </div>

          <div className="form-group">
            <label>{t('profile.lastName')}</label>
            <input
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              placeholder={t('profile.lastName')}
            />
          </div>

          <div className="form-group">
            <label>{t('profile.phone')}</label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder={t('profile.phone')}
            />
          </div>

          <div className="form-group">
            <label>{t('profile.address')}</label>
            <input
              type="text"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              placeholder={t('profile.address')}
            />
          </div>

          <button type="submit" className="auth-submit-btn" disabled={saving}>
            {saving ? t('profile.saving') : t('common.save')}
          </button>
        </form>
      </div>
    </div>
  );
}
