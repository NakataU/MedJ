import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { getDocumentsByUserId, uploadDocuments, deleteDocument } from '../api/documents';
import { useAuth } from '../context/AuthContext';
import { getAllCategories } from '../api/categories';
import type { DocumentListOutView, Page, CategoryOutView } from '../types';
import { Pagination } from '../components/Pagination';

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export function DocumentsPage() {
  const { t } = useTranslation();
  const [documents, setDocuments] = useState<DocumentListOutView[]>([]);
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<Page<DocumentListOutView> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Upload modal state
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadStep, setUploadStep] = useState<1 | 2>(1); // 1 = select files, 2 = assign categories
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [documentCategories, setDocumentCategories] = useState<CategoryOutView[]>([]);
  const [categoriesLoading, setCategoriesLoading] = useState(false);
  // Map: fileIndex -> selected categoryId
  const [selectedCategories, setSelectedCategories] = useState<Record<number, number>>({});
  const [uploadLoading, setUploadLoading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const navigate = useNavigate();
  const { user } = useAuth();

  const fetchDocuments = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getDocumentsByUserId(user!.id, page, 10);
      setDocuments(data.content);
      setPageData(data);
    } catch (err) {
      console.error(err);
      setError(t('documents.error'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, [page]);

  const handleDocumentClick = (id: number) => {
    navigate(`/documents/${id}`);
  };

  const handleDelete = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation();
    setDeletingId(id);
    try {
      await deleteDocument(id);
      if (documents.length === 1 && page > 0) {
        setPage((p) => p - 1);
      } else {
        fetchDocuments();
      }
    } catch {
      setError(t('documents.error'));
    } finally {
      setDeletingId(null);
    }
  };

  const openUploadModal = () => {
    setSelectedFiles([]);
    setSelectedCategories({});
    setUploadError(null);
    setUploadStep(1);
    setShowUploadModal(true);
  };

  const closeUploadModal = () => {
    setShowUploadModal(false);
    setSelectedFiles([]);
    setSelectedCategories({});
    setUploadError(null);
    setUploadStep(1);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const newFiles = Array.from(e.target.files);
      setSelectedFiles((prev) => [...prev, ...newFiles]);
    }
  };

  const removeFile = (index: number) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
    // Rebuild selectedCategories with shifted indexes
    setSelectedCategories((prev) => {
      const rebuilt: Record<number, number> = {};
      Object.entries(prev).forEach(([key, val]) => {
        const i = Number(key);
        if (i < index) rebuilt[i] = val;
        else if (i > index) rebuilt[i - 1] = val;
      });
      return rebuilt;
    });
  };

  const goToCategories = async () => {
    setUploadError(null);
    setCategoriesLoading(true);
    try {
      const data = await getAllCategories('DOCUMENT', 0, 100);
      setDocumentCategories(data.content);
      setUploadStep(2);
    } catch {
      setUploadError(t('documents.error'));
    } finally {
      setCategoriesLoading(false);
    }
  };

  const handleUpload = async () => {
    if (selectedFiles.length === 0) return;
    setUploadLoading(true);
    setUploadError(null);
    try {
      const documentCategoryIds: (number | null)[] = selectedFiles.map((_, i) =>
        selectedCategories[i] !== undefined ? selectedCategories[i] : null
      );
      await uploadDocuments(selectedFiles, documentCategoryIds);
      closeUploadModal();
      fetchDocuments();
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.data) {
        const data = err.response.data;
        setUploadError(data.exMessage || data.message || 'Upload failed.');
      } else {
        setUploadError('Upload failed. Please try again.');
      }
    } finally {
      setUploadLoading(false);
    }
  };

  if (loading) return <div className="loading">{t('documents.loading')}</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t('documents.title')}</h1>
        <button className="button" onClick={openUploadModal}>
          {t('documents.upload')}
        </button>
      </div>

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="modal-overlay" onClick={closeUploadModal}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{uploadStep === 1 ? t('documents.upload') : t('documents.assignCategories')}</h2>
              <button className="modal-close" onClick={closeUploadModal}>&times;</button>
            </div>
            <div className="modal-body">
              {uploadError && <div className="form-error" style={{ marginBottom: 12 }}>{uploadError}</div>}

              {/* Step 1: Select files */}
              {uploadStep === 1 && (
                <>
                  <div className="form-group">
                    <label>{t('documents.selectFiles')}</label>
                    <input
                      type="file"
                      multiple
                      className="file-input"
                      onChange={handleFileChange}
                    />
                  </div>

                  {selectedFiles.length > 0 && (
                    <div className="selected-files" style={{ marginTop: 12 }}>
                      {selectedFiles.map((file, index) => (
                        <div key={index} className="file-item">
                          <div className="file-header">
                            <span className="file-name">{file.name}</span>
                            <button
                              type="button"
                              className="remove-file-btn"
                              onClick={() => removeFile(index)}
                            >
                              &times;
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}

                  <div className="form-actions" style={{ marginTop: 20 }}>
                    <button type="button" className="button secondary" onClick={closeUploadModal}>
                      {t('common.cancel')}
                    </button>
                    <button
                      type="button"
                      className="button"
                      onClick={goToCategories}
                      disabled={categoriesLoading || selectedFiles.length === 0}
                    >
                      {categoriesLoading ? t('common.loading') : t('documents.nextStep')}
                    </button>
                  </div>
                </>
              )}

              {/* Step 2: Assign categories */}
              {uploadStep === 2 && (
                <>
                  <p className="doc-category-subtitle" style={{ marginBottom: 16 }}>
                    {t('documents.categoryOptional')}
                  </p>

                  <div className="doc-category-list" style={{ marginBottom: 20 }}>
                    {selectedFiles.map((file, fileIndex) => (
                      <div key={fileIndex} className="doc-category-card">
                        <div className="doc-category-card-header">
                          <span className="doc-category-file-icon">&#128196;</span>
                          <div className="doc-category-file-info">
                            <span className="doc-category-file-name">{file.name}</span>
                            <span className="doc-category-file-size">
                              {(file.size / 1024).toFixed(1)} KB
                            </span>
                          </div>
                          <button
                            type="button"
                            className="doc-remove-btn"
                            onClick={() => removeFile(fileIndex)}
                            disabled={uploadLoading}
                            title="Remove document"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="3 6 5 6 21 6" />
                              <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                              <path d="M10 11v6" />
                              <path d="M14 11v6" />
                              <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                            </svg>
                          </button>
                        </div>

                        <div className="doc-category-body">
                          {documentCategories.length === 0 ? (
                            <p className="empty-hint">{t('documents.noCategoriesAvailable')}</p>
                          ) : (
                            <div className="doc-radio-group">
                              {documentCategories.map((category) => (
                                <label key={category.id} className="doc-radio-label">
                                  <input
                                    type="radio"
                                    name={`upload-doc-cat-${fileIndex}`}
                                    value={category.id}
                                    checked={selectedCategories[fileIndex] === category.id}
                                    onChange={() =>
                                      setSelectedCategories((prev) => ({ ...prev, [fileIndex]: category.id }))
                                    }
                                  />
                                  {category.label}
                                </label>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="form-actions">
                    <button
                      type="button"
                      className="button secondary"
                      onClick={() => { setUploadStep(1); setUploadError(null); }}
                      disabled={uploadLoading}
                    >
                      {t('common.back')}
                    </button>
                    <button
                      type="button"
                      className="button"
                      onClick={handleUpload}
                      disabled={uploadLoading || selectedFiles.length === 0}
                    >
                      {uploadLoading ? t('documents.uploading') : t('documents.uploadBtn')}
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {documents.length === 0 ? (
        <p className="empty-message">{t('documents.noDocuments')}</p>
      ) : (
        <>
          <table className="data-table">
            <thead>
              <tr>
                <th>{t('documents.fileName')}</th>
                <th>{t('documents.size')}</th>
                <th>{t('documents.createdOn')}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {documents.map((doc) => (
                <tr
                  key={doc.id}
                  onClick={() => handleDocumentClick(doc.id)}
                  className="clickable-row"
                >
                  <td>{doc.fileName}</td>
                  <td>{doc.size}</td>
                  <td>{formatDate(doc.createdOn)}</td>
                  <td>
                    <button
                      className="icon-btn"
                      title={t('documents.deleteConfirm')}
                      disabled={deletingId === doc.id}
                      onClick={(e) => handleDelete(doc.id, e)}
                    >
                      {deletingId === doc.id ? '…' : (
                        <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="3 6 5 6 21 6" />
                          <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                          <path d="M10 11v6" /><path d="M14 11v6" />
                          <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                        </svg>
                      )}
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
