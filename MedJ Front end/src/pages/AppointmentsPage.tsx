import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import axios from 'axios';
import { getAllAppointments, updateAppointment, linkDocumentsToAppointment } from '../api/appointments';
import { getAllPractitioners } from '../api/practitioners';
import { getDocumentsByAppointmentId, deleteDocument, getPreviewUrl, getDownloadUrl, getAllUserDocuments } from '../api/documents';
import type { AppointmentOutView, PractitionerOutView, Page, AppointmentCreateInput, DocumentListOutView } from '../types';
import { Pagination } from '../components/Pagination';

// ── Inline sub-component: document list per appointment ──────────────────────
function AppointmentDocuments({ appointmentId }: { appointmentId: number }) {
  const [docs, setDocs] = useState<DocumentListOutView[]>([]);
  const [docPage, setDocPage] = useState(0);
  const [docPageData, setDocPageData] = useState<Page<DocumentListOutView> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [previewDoc, setPreviewDoc] = useState<DocumentListOutView | null>(null);

  const fetchDocs = () => {
    setLoading(true);
    setError(null);
    getDocumentsByAppointmentId(appointmentId, docPage, 5)
      .then((data) => {
        setDocs(data.content);
        setDocPageData(data);
      })
      .catch(() => setError('Failed to load documents'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchDocs();
  }, [appointmentId, docPage]);

  const handleDelete = async (docId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    setDeletingId(docId);
    try {
      await deleteDocument(docId);
      if (docs.length === 1 && docPage > 0) {
        setDocPage((p) => p - 1);
      } else {
        fetchDocs();
      }
    } catch {
      setError('Failed to delete document');
    } finally {
      setDeletingId(null);
    }
  };

  const isImage = (fileName: string) =>
    /\.(png|jpe?g|gif|webp|svg)$/i.test(fileName);

  if (loading) return <p className="apt-docs-loading">Loading documents...</p>;
  if (error) return <p className="apt-docs-error">{error}</p>;
  if (docs.length === 0) return <p className="apt-docs-empty">No documents attached.</p>;

  return (
    <>
      {/* Preview Modal */}
      {previewDoc && (
        <div className="modal-overlay apt-preview-overlay" onClick={() => setPreviewDoc(null)}>
          <div className="modal apt-preview-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header apt-preview-header">
              <div className="apt-preview-title">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                </svg>
                <span>{previewDoc.fileName}</span>
              </div>
              <div className="apt-preview-actions">
                <a
                  href={getDownloadUrl(previewDoc.id)}
                  className="button secondary apt-preview-download"
                  download={previewDoc.fileName}
                  onClick={(e) => e.stopPropagation()}
                >
                  ⬇ Download
                </a>
                <button className="modal-close" onClick={() => setPreviewDoc(null)}>&times;</button>
              </div>
            </div>
            <div className="apt-preview-body">
              {isImage(previewDoc.fileName) ? (
                <img
                  src={getPreviewUrl(previewDoc.id)}
                  alt={previewDoc.fileName}
                  className="apt-preview-image"
                />
              ) : (
                <iframe
                  src={getPreviewUrl(previewDoc.id)}
                  title={previewDoc.fileName}
                  className="apt-preview-iframe"
                />
              )}
            </div>
          </div>
        </div>
      )}

      <div className="apt-docs-wrapper">
        <ul className="apt-docs-list">
          {docs.map((doc) => (
            <li
              key={doc.id}
              className="apt-docs-item apt-docs-item-clickable"
              onClick={(e) => { e.stopPropagation(); setPreviewDoc(doc); }}
              title="Click to preview"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              <span className="apt-docs-name">{doc.fileName}</span>
              <span className="apt-docs-size">{doc.size ?? ''}</span>
              <button
                className="apt-docs-delete-btn"
                title="Delete document"
                disabled={deletingId === doc.id}
                onClick={(e) => handleDelete(doc.id, e)}
              >
                {deletingId === doc.id ? '…' : (
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                    <path d="M10 11v6" /><path d="M14 11v6" />
                    <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                  </svg>
                )}
              </button>
            </li>
          ))}
        </ul>
        {docPageData && docPageData.totalPages > 1 && (
          <Pagination
            currentPage={docPage}
            totalPages={docPageData.totalPages}
            onPageChange={setDocPage}
          />
        )}
      </div>
    </>
  );
}

const formatDate = (date: string | number[]): string => {
  let d: Date;
  if (Array.isArray(date)) {
    // Spring LocalDate serialized as [year, month, day]
    d = new Date(date[0], date[1] - 1, date[2]);
  } else {
    d = new Date(date);
  }
  return d.toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

export function AppointmentsPage() {
  const location = useLocation();

  const [appointments, setAppointments] = useState<AppointmentOutView[]>([]);
  const [practitioners, setPractitioners] = useState<PractitionerOutView[]>([]);
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<Page<AppointmentOutView> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  // Modal state for creating appointment
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [practitionersLoading2, setPractitionersLoading2] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  // Document picker state
  const [availableDocs, setAvailableDocs] = useState<DocumentListOutView[]>([]);
  const [selectedDocIds, setSelectedDocIds] = useState<number[]>([]);
  const [docsLoading, setDocsLoading] = useState(false);

  // Modal state for updating appointment
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [updatingAppointment, setUpdatingAppointment] = useState<AppointmentOutView | null>(null);
  const [updateFormData, setUpdateFormData] = useState<AppointmentCreateInput>({ name: '', place: '', date: '', practitionerId: null });
  const [updateLoading, setUpdateLoading] = useState(false);
  const [updateError, setUpdateError] = useState<string | null>(null);

  // Form state
  const [formData, setFormData] = useState<AppointmentCreateInput>({
    name: '',
    place: '',
    date: '',
    practitionerId: null,
  });

  useEffect(() => {
    const fetchAppointments = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await getAllAppointments(page, 10);
        setAppointments(data.content);
        setPageData(data);
      } catch (err) {
        setError('Failed to load appointments');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchAppointments();
  }, [page, location.state]);

  const toggleExpand = (id: number) => {
    setExpandedId(expandedId === id ? null : id);
  };

  // Create Modal Functions
  const openCreateModal = async () => {
    setShowCreateModal(true);
    setCreateError(null);
    resetForm();

    const promises: Promise<void>[] = [];

    if (practitioners.length === 0) {
      setPractitionersLoading2(true);
      promises.push(
        getAllPractitioners(0, 100)
          .then((data) => setPractitioners(data.content))
          .catch(() => setCreateError('Failed to load practitioners'))
          .finally(() => setPractitionersLoading2(false))
      );
    }

    setDocsLoading(true);
    promises.push(
      getAllUserDocuments()
        .then((docs) => setAvailableDocs(docs))
        .catch(() => setCreateError('Failed to load documents'))
        .finally(() => setDocsLoading(false))
    );

    await Promise.all(promises);
  };

  const closeCreateModal = () => {
    setShowCreateModal(false);
    setCreateError(null);
    resetForm();
  };

  const openUpdateModal = async (appointment: AppointmentOutView, e: React.MouseEvent) => {
    e.stopPropagation();
    setUpdatingAppointment(appointment);
    // Format date: if array [y,m,d] convert to yyyy-MM-dd string for the date input
    let dateStr = '';
    if (Array.isArray(appointment.date)) {
      const [y, m, d] = appointment.date;
      dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    } else {
      dateStr = appointment.date.substring(0, 10);
    }
    setUpdateFormData({
      name: appointment.name,
      place: appointment.place,
      date: dateStr,
      practitionerId: appointment.practitioner?.id ?? null,
    });
    setUpdateError(null);
    setShowUpdateModal(true);

    if (practitioners.length === 0) {
      setPractitionersLoading2(true);
      try {
        const data = await getAllPractitioners(0, 100);
        setPractitioners(data.content);
      } catch {
        setUpdateError('Failed to load practitioners');
      } finally {
        setPractitionersLoading2(false);
      }
    }
  };

  const closeUpdateModal = () => {
    setShowUpdateModal(false);
    setUpdatingAppointment(null);
    setUpdateError(null);
  };

  const handleUpdateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!updatingAppointment) return;
    setUpdateLoading(true);
    setUpdateError(null);
    try {
      const updated = await updateAppointment(updatingAppointment.id, updateFormData);
      setAppointments((prev) => prev.map((a) => a.id === updated.id ? updated : a));
      closeUpdateModal();
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.data) {
        const data = err.response.data;
        setUpdateError(data.exMessage || data.message || 'Failed to update appointment.');
      } else {
        setUpdateError('Failed to update appointment.');
      }
    } finally {
      setUpdateLoading(false);
    }
  };

  const handleUpdateInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setUpdateFormData((prev) => ({
      ...prev,
      [name]: name === 'practitionerId' ? (value ? Number(value) : null) : value,
    }));
  };

  const resetForm = () => {
    setFormData({
      name: '',
      place: '',
      date: '',
      practitionerId: null,
    });
    setSelectedDocIds([]);
  };

  const toggleDocSelection = (id: number) => {
    setSelectedDocIds((prev) =>
      prev.includes(id) ? prev.filter((d) => d !== id) : [...prev, id]
    );
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'practitionerId' ? (value ? Number(value) : null) : value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError(null);
    try {
      const { createAppointment } = await import('../api/appointments');
      const newAppointment = await createAppointment(
        formData,
        selectedDocIds.length > 0 ? selectedDocIds : undefined
      );

      setAppointments((prev) => [newAppointment, ...prev]);
      closeCreateModal();
    } catch (err) {
      console.error('Failed to create appointment:', err);
      if (axios.isAxiosError(err) && err.response?.data) {
        const data = err.response.data;
        setCreateError(data.exMessage || data.message || 'Failed to create appointment.');
      } else {
        setCreateError('Failed to create appointment');
      }
    }
  };

  if (loading) {
    return <div className="loading">Loading appointments...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Appointments</h1>
        <button className="button" onClick={openCreateModal}>
          Add Appointment
        </button>
      </div>

      {/* Create Appointment Modal */}
      {showCreateModal && (
        <div className="modal-overlay" onClick={closeCreateModal}>
          <div className="modal create-appointment-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>New Appointment</h2>
              <button className="modal-close" onClick={closeCreateModal}>
                &times;
              </button>
            </div>

            <div className="modal-body">
              {createError && <div className="form-error">{createError}</div>}

              {practitionersLoading2 ? (
                <div className="loading">Loading...</div>
              ) : (
                <form className="appointment-form" onSubmit={handleSubmit}>
                  <div className="form-group">
                    <label htmlFor="name">Appointment Name *</label>
                    <input
                      type="text"
                      id="name"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      placeholder="e.g., Annual Checkup"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="place">Location *</label>
                    <input
                      type="text"
                      id="place"
                      name="place"
                      value={formData.place}
                      onChange={handleInputChange}
                      placeholder="e.g., City Hospital"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="date">Date *</label>
                    <input
                      type="date"
                      id="date"
                      name="date"
                      value={formData.date}
                      onChange={handleInputChange}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="practitionerId">Practitioner</label>
                    <select
                      id="practitionerId"
                      name="practitionerId"
                      value={formData.practitionerId || ''}
                      onChange={handleInputChange}
                    >
                      <option value="">None</option>
                      {practitioners.map((practitioner) => (
                        <option key={practitioner.id} value={practitioner.id}>
                          {practitioner.firstName} {practitioner.lastName}
                          {practitioner.specialty && ` - ${practitioner.specialty.specialty}`}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label>Documents</label>
                    {docsLoading ? (
                      <p className="apt-docs-loading">Loading documents...</p>
                    ) : availableDocs.length === 0 ? (
                      <p className="empty-hint">No documents uploaded yet.</p>
                    ) : (
                      <div className="doc-picker-list">
                        {availableDocs.map((doc) => (
                          <label key={doc.id} className="doc-picker-item">
                            <input
                              type="checkbox"
                              checked={selectedDocIds.includes(doc.id)}
                              onChange={() => toggleDocSelection(doc.id)}
                            />
                            <span className="doc-picker-name">{doc.fileName}</span>
                            {doc.size && <span className="doc-picker-size">{doc.size}</span>}
                          </label>
                        ))}
                      </div>
                    )}
                    {selectedDocIds.length > 0 && (
                      <p className="doc-next-hint">{selectedDocIds.length} document{selectedDocIds.length > 1 ? 's' : ''} selected</p>
                    )}
                  </div>

                  <div className="form-actions">
                    <button
                      type="button"
                      className="button secondary"
                      onClick={closeCreateModal}
                    >
                      Cancel
                    </button>
                    <button type="submit" className="button">
                      Create Appointment
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Update Appointment Modal */}
      {showUpdateModal && (
        <div className="modal-overlay" onClick={closeUpdateModal}>
          <div className="modal practitioner-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Edit Appointment</h2>
              <button className="modal-close" onClick={closeUpdateModal}>&times;</button>
            </div>
            {practitionersLoading2 ? (
              <div className="modal-body"><div className="loading">Loading...</div></div>
            ) : (
              <form className="practitioner-form modal-body" onSubmit={handleUpdateSubmit}>
                {updateError && <div className="form-error">{updateError}</div>}
                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="update-name">Appointment Name</label>
                    <input
                      type="text"
                      id="update-name"
                      name="name"
                      value={updateFormData.name}
                      onChange={handleUpdateInputChange}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="update-date">Date</label>
                    <input
                      type="date"
                      id="update-date"
                      name="date"
                      value={updateFormData.date}
                      onChange={handleUpdateInputChange}
                      required
                    />
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="update-place">Location</label>
                  <input
                    type="text"
                    id="update-place"
                    name="place"
                    value={updateFormData.place}
                    onChange={handleUpdateInputChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="update-practitionerId">Practitioner</label>
                  <select
                    id="update-practitionerId"
                    name="practitionerId"
                    value={updateFormData.practitionerId || ''}
                    onChange={handleUpdateInputChange}
                  >
                    <option value="">None</option>
                    {practitioners.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.firstName} {p.lastName}
                        {p.specialty && ` - ${p.specialty.specialty}`}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-actions">
                  <button type="button" className="button secondary" onClick={closeUpdateModal} disabled={updateLoading}>
                    Cancel
                  </button>
                  <button type="submit" className="button" disabled={updateLoading}>
                    {updateLoading ? 'Saving...' : 'Save Changes'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {appointments.length === 0 ? (
        <p className="empty-message">No appointments found.</p>
      ) : (
        <>
          <div className="appointments-list">
            {appointments.map((appointment) => (
              <div
                key={appointment.id}
                className={`appointment-card ${expandedId === appointment.id ? 'expanded' : ''}`}
                onClick={() => toggleExpand(appointment.id)}
              >
                <div className="appointment-header">
                  <h3>{appointment.name}</h3>
                  <div className="appointment-header-actions">
                    <span className="appointment-date">{formatDate(appointment.date)}</span>
                    <button
                      className="icon-btn"
                      onClick={(e) => openUpdateModal(appointment, e)}
                      title="Edit appointment"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                    </button>
                  </div>
                </div>

                <div className="appointment-summary">
                  <p><strong>Location:</strong> {appointment.place}</p>
                  {appointment.practitioner ? (
                    <p>
                      <strong>Practitioner:</strong>{' '}
                      {appointment.practitioner.firstName} {appointment.practitioner.lastName}
                      {appointment.practitioner.specialty && (
                        <> - {appointment.practitioner.specialty.specialty}</>
                      )}
                    </p>
                  ) : (
                    <p><strong>Practitioner:</strong> Not assigned</p>
                  )}
                </div>

                {expandedId === appointment.id && (
                  <div className="appointment-details">
                    {appointment.practitioner ? (
                      <div className="practitioner-info">
                        <h4>Practitioner Details</h4>
                        <p><strong>Name:</strong> {appointment.practitioner.firstName} {appointment.practitioner.lastName}</p>
                        {appointment.practitioner.specialty && (
                          <p><strong>Specialty:</strong> {appointment.practitioner.specialty.specialty}</p>
                        )}
                        {appointment.practitioner.specialization && (
                          <p><strong>Specialization:</strong> {appointment.practitioner.specialization}</p>
                        )}
                      </div>
                    ) : (
                      <div className="practitioner-info">
                        <h4>Practitioner</h4>
                        <p>No practitioner assigned to this appointment.</p>
                      </div>
                    )}

                    <div className="appointment-documents">
                      <h4>Documents</h4>
                      <AppointmentDocuments appointmentId={appointment.id} />
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>

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
