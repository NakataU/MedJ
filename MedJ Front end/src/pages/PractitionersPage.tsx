import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { getAllPractitioners, createPractitioner, updatePractitioner } from '../api/practitioners';
import { getAllSpecialties } from '../api/specialties';
import type { PractitionerOutView, SpecialtyOutView, Page, PractitionerCreateInput } from '../types';
import { Pagination } from '../components/Pagination';

export function PractitionersPage() {
  const { t } = useTranslation();
  const [practitioners, setPractitioners] = useState<PractitionerOutView[]>([]);
  const [specialties, setSpecialties] = useState<SpecialtyOutView[]>([]);
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<Page<PractitionerOutView> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Add modal state
  const [showAddForm, setShowAddForm] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [formData, setFormData] = useState<PractitionerCreateInput>({
    firstName: '',
    lastName: '',
    specialtyId: 0,
    specialization: '',
  });

  // Update modal state
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [updatingPractitioner, setUpdatingPractitioner] = useState<PractitionerOutView | null>(null);
  const [updateFormData, setUpdateFormData] = useState<PractitionerCreateInput>({
    firstName: '',
    lastName: '',
    specialtyId: 0,
    specialization: '',
  });
  const [updateLoading, setUpdateLoading] = useState(false);
  const [updateError, setUpdateError] = useState<string | null>(null);

  useEffect(() => {
    fetchPractitioners();
  }, [page]);

  useEffect(() => {
    getAllSpecialties(0, 100)
      .then((data) => setSpecialties(data.content))
      .catch((err) => console.error('Failed to load specialties:', err));
  }, []);

  const fetchPractitioners = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAllPractitioners(page, 10);
      setPractitioners(data.content);
      setPageData(data);
    } catch (err) {
      console.error(err);
      setError(t('practitioners.error'));
    } finally {
      setLoading(false);
    }
  };

  // ── Add handlers ────────────────────────────────────────────────────────────
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'specialtyId' ? Number(value) : value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError(null);
    try {
      await createPractitioner(formData);
      setFormData({ firstName: '', lastName: '', specialtyId: 0, specialization: '' });
      setShowAddForm(false);
      fetchPractitioners();
    } catch (err) {
      console.error(err);
      if (axios.isAxiosError(err) && err.response?.data) {
        const data = err.response.data;
        setFormError(data.exMessage || data.message || 'Failed to create practitioner.');
      } else {
        setFormError('Failed to create practitioner.');
      }
    } finally {
      setFormLoading(false);
    }
  };

  const handleCancel = () => {
    setShowAddForm(false);
    setFormData({ firstName: '', lastName: '', specialtyId: 0, specialization: '' });
    setFormError(null);
  };

  // ── Update handlers ──────────────────────────────────────────────────────────
  const openUpdateModal = (practitioner: PractitionerOutView) => {
    setUpdatingPractitioner(practitioner);
    setUpdateFormData({
      firstName: practitioner.firstName,
      lastName: practitioner.lastName,
      specialtyId: practitioner.specialty?.id ?? 0,
      specialization: practitioner.specialization || '',
    });
    setUpdateError(null);
    setShowUpdateModal(true);
  };

  const closeUpdateModal = () => {
    setShowUpdateModal(false);
    setUpdatingPractitioner(null);
    setUpdateError(null);
  };

  const handleUpdateInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setUpdateFormData((prev) => ({
      ...prev,
      [name]: name === 'specialtyId' ? Number(value) : value,
    }));
  };

  const handleUpdateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!updatingPractitioner) return;
    setUpdateLoading(true);
    setUpdateError(null);
    try {
      const updated = await updatePractitioner(updatingPractitioner.id, updateFormData);
      setPractitioners((prev) => prev.map((p) => p.id === updated.id ? updated : p));
      closeUpdateModal();
    } catch (err) {
      console.error(err);
      if (axios.isAxiosError(err) && err.response?.data) {
        const data = err.response.data;
        setUpdateError(data.exMessage || data.message || 'Failed to update practitioner.');
      } else {
        setUpdateError('Failed to update practitioner.');
      }
    } finally {
      setUpdateLoading(false);
    }
  };

  if (loading) return <div className="loading">{t('practitioners.loading')}</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t('practitioners.title')}</h1>
        <button className="button" onClick={() => setShowAddForm(true)}>
          {t('practitioners.add')}
        </button>
      </div>

      {/* Add Practitioner Modal */}
      {showAddForm && (
        <div className="modal-overlay" onClick={handleCancel}>
          <div className="modal practitioner-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{t('practitioners.addNew')}</h2>
              <button className="modal-close" onClick={handleCancel}>&times;</button>
            </div>
            <form onSubmit={handleSubmit} className="practitioner-form modal-body">
              {formError && <div className="form-error">{formError}</div>}
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="firstName">{t('practitioners.firstName')}</label>
                  <input type="text" id="firstName" name="firstName" value={formData.firstName}
                    onChange={handleInputChange} required placeholder="e.g. John" />
                </div>
                <div className="form-group">
                  <label htmlFor="lastName">{t('practitioners.lastName')}</label>
                  <input type="text" id="lastName" name="lastName" value={formData.lastName}
                    onChange={handleInputChange} required placeholder="e.g. Smith" />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="specialtyId">{t('practitioners.specialty')}</label>
                <select id="specialtyId" name="specialtyId" value={formData.specialtyId}
                  onChange={handleInputChange} required>
                  <option value={0} disabled>{t('practitioners.selectSpecialty')}</option>
                  {specialties.map((s) => (
                    <option key={s.id} value={s.id}>{s.specialty}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="specialization">{t('practitioners.specialization')}</label>
                <input type="text" id="specialization" name="specialization" value={formData.specialization}
                  onChange={handleInputChange} required placeholder="e.g. Pediatric Cardiology" />
              </div>
              <div className="form-actions">
                <button type="button" className="button secondary" onClick={handleCancel}>{t('common.cancel')}</button>
                <button type="submit" className="button" disabled={formLoading}>
                  {formLoading ? t('practitioners.creating') : t('practitioners.addNew')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Update Practitioner Modal */}
      {showUpdateModal && (
        <div className="modal-overlay" onClick={closeUpdateModal}>
          <div className="modal practitioner-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{t('practitioners.edit')}</h2>
              <button className="modal-close" onClick={closeUpdateModal}>&times;</button>
            </div>
            <form onSubmit={handleUpdateSubmit} className="practitioner-form modal-body">
              {updateError && <div className="form-error">{updateError}</div>}
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="update-firstName">{t('practitioners.firstName')}</label>
                  <input type="text" id="update-firstName" name="firstName" value={updateFormData.firstName}
                    onChange={handleUpdateInputChange} required />
                </div>
                <div className="form-group">
                  <label htmlFor="update-lastName">{t('practitioners.lastName')}</label>
                  <input type="text" id="update-lastName" name="lastName" value={updateFormData.lastName}
                    onChange={handleUpdateInputChange} required />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="update-specialtyId">{t('practitioners.specialty')}</label>
                <select id="update-specialtyId" name="specialtyId" value={updateFormData.specialtyId}
                  onChange={handleUpdateInputChange} required>
                  <option value={0} disabled>{t('practitioners.selectSpecialty')}</option>
                  {specialties.map((s) => (
                    <option key={s.id} value={s.id}>{s.specialty}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="update-specialization">{t('practitioners.specialization')}</label>
                <input type="text" id="update-specialization" name="specialization" value={updateFormData.specialization}
                  onChange={handleUpdateInputChange} required />
              </div>
              <div className="form-actions">
                <button type="button" className="button secondary" onClick={closeUpdateModal} disabled={updateLoading}>
                  {t('common.cancel')}
                </button>
                <button type="submit" className="button" disabled={updateLoading}>
                  {updateLoading ? t('practitioners.saving') : t('practitioners.saveChanges')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Practitioners List */}
      {practitioners.length === 0 ? (
        <p className="empty-message">{t('practitioners.noPractitioners')}</p>
      ) : (
        <>
          <table className="data-table">
            <thead>
              <tr>
                <th>{t('practitioners.name')}</th>
                <th>{t('practitioners.specialty')}</th>
                <th>{t('practitioners.specialization')}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {practitioners.map((practitioner) => (
                <tr key={practitioner.id}>
                  <td>{practitioner.firstName} {practitioner.lastName}</td>
                  <td>{practitioner.specialty?.specialty || '-'}</td>
                  <td>{practitioner.specialization || '-'}</td>
                  <td>
                    <button
                      className="icon-btn"
                      title="Edit practitioner"
                      onClick={() => openUpdateModal(practitioner)}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {pageData && (
            <Pagination
              currentPage={page}
              totalPages={pageData.totalPages}
              onPageChange={setPage}
            />
          )}
        </>
      )}
    </div>
  );
}
