import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { getDocumentsByUserId, uploadDocuments, deleteDocument, updateDocumentContent, updateDocumentCategories } from '../api/documents';
import { useAuth } from '../context/AuthContext';
import { getCategoriesByType } from '../api/categories';
import type { DocumentListOutView, DocumentOutView, Page, CategoryOutView } from '../types';
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

  const ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/png'];
  const ACCEPT_STRING = '.pdf,.jpg,.jpeg,.png';

  // Upload modal state
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadStep, setUploadStep] = useState<1 | 2 | 3>(1);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [docTypeCategories, setDocTypeCategories] = useState<CategoryOutView[]>([]);
  const [medSpecCategories, setMedSpecCategories] = useState<CategoryOutView[]>([]);
  const [medCatCategories, setMedCatCategories] = useState<CategoryOutView[]>([]);
  const [categoriesLoading, setCategoriesLoading] = useState(false);
  // Per-document selections: key = `${docIndex}-${categoryType}`
  const [selectedDocTypes, setSelectedDocTypes] = useState<Record<number, number>>({});
  const [selectedMedSpecs, setSelectedMedSpecs] = useState<Record<number, number>>({});
  const [selectedMedCats, setSelectedMedCats] = useState<Record<number, number>>({});
  const [uploadLoading, setUploadLoading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  // OCR review state (step 2)
  const [uploadedDocs, setUploadedDocs] = useState<DocumentOutView[]>([]);
  const [ocrTexts, setOcrTexts] = useState<Record<number, string>>({});
  const [savingOcr, setSavingOcr] = useState(false);
  const [showValidation, setShowValidation] = useState(false);

  // Filter state
  const [filterDocType, setFilterDocType] = useState<number | undefined>();
  const [filterMedSpec, setFilterMedSpec] = useState<number | undefined>();
  const [filterMedCat, setFilterMedCat] = useState<number | undefined>();
  const [filterDocTypeOptions, setFilterDocTypeOptions] = useState<CategoryOutView[]>([]);
  const [filterMedSpecOptions, setFilterMedSpecOptions] = useState<CategoryOutView[]>([]);
  const [filterMedCatOptions, setFilterMedCatOptions] = useState<CategoryOutView[]>([]);

  const navigate = useNavigate();
  const { user } = useAuth();

  const fetchDocuments = async () => {
    setLoading(true);
    setError(null);
    try {
      const filters = {
        documentTypeId: filterDocType,
        medicalSpecialtyId: filterMedSpec,
        medicalCategoryId: filterMedCat,
      };
      const data = await getDocumentsByUserId(user!.id, page, 10, filters);
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
  }, [page, filterDocType, filterMedSpec, filterMedCat]);

  useEffect(() => {
    Promise.all([
      getCategoriesByType('DOCUMENT', 'DOCUMENT_TYPE'),
      getCategoriesByType('DOCUMENT', 'MEDICAL_SPECIALTY'),
      getCategoriesByType('DOCUMENT', 'MEDICAL_CATEGORY'),
    ]).then(([dt, ms, mc]) => {
      setFilterDocTypeOptions(dt);
      setFilterMedSpecOptions(ms);
      setFilterMedCatOptions(mc);
    }).catch(() => {});
  }, []);

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
    setSelectedDocTypes({});
    setSelectedMedSpecs({});
    setSelectedMedCats({});
    setUploadError(null);
    setUploadStep(1);
    setUploadedDocs([]);
    setOcrTexts({});
    setShowValidation(false);
    setShowUploadModal(true);
  };

  const closeUploadModal = () => {
    setShowUploadModal(false);
    setSelectedFiles([]);
    setSelectedDocTypes({});
    setSelectedMedSpecs({});
    setSelectedMedCats({});
    setUploadError(null);
    setUploadStep(1);
    setUploadedDocs([]);
    setOcrTexts({});
    setShowValidation(false);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const newFiles = Array.from(e.target.files);
      const invalid = newFiles.filter((f) => !ALLOWED_TYPES.includes(f.type));
      if (invalid.length > 0) {
        setUploadError(t('documents.invalidFileType'));
        return;
      }
      setUploadError(null);
      setSelectedFiles((prev) => [...prev, ...newFiles]);
    }
  };

  const shiftIndexMap = (prev: Record<number, number>, index: number) => {
    const rebuilt: Record<number, number> = {};
    Object.entries(prev).forEach(([key, val]) => {
      const i = Number(key);
      if (i < index) rebuilt[i] = val;
      else if (i > index) rebuilt[i - 1] = val;
    });
    return rebuilt;
  };

  const removeFile = (index: number) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
    setSelectedDocTypes((prev) => shiftIndexMap(prev, index));
    setSelectedMedSpecs((prev) => shiftIndexMap(prev, index));
    setSelectedMedCats((prev) => shiftIndexMap(prev, index));
  };

  const handleUploadAndOcr = async () => {
    if (selectedFiles.length === 0) return;
    setUploadLoading(true);
    setUploadError(null);
    try {
      const docs = await uploadDocuments(selectedFiles, []);
      setUploadedDocs(docs);
      const texts: Record<number, string> = {};
      docs.forEach((doc) => {
        texts[doc.id] = doc.content ?? '';
      });
      setOcrTexts(texts);
      setUploadStep(2);
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

  const goToCategories = async () => {
    setUploadError(null);
    setCategoriesLoading(true);
    try {
      const [docTypes, medSpecs, medCats] = await Promise.all([
        getCategoriesByType('DOCUMENT', 'DOCUMENT_TYPE'),
        getCategoriesByType('DOCUMENT', 'MEDICAL_SPECIALTY'),
        getCategoriesByType('DOCUMENT', 'MEDICAL_CATEGORY'),
      ]);
      setDocTypeCategories(docTypes);
      setMedSpecCategories(medSpecs);
      setMedCatCategories(medCats);
      setUploadStep(3);
    } catch {
      setUploadError(t('documents.error'));
    } finally {
      setCategoriesLoading(false);
    }
  };

  const allCategoriesSelected = uploadedDocs.every((_, i) =>
    selectedDocTypes[i] && selectedMedSpecs[i] && selectedMedCats[i]
  );

  const handleSaveAll = async () => {
    if (!allCategoriesSelected) {
      setShowValidation(true);
      return;
    }
    setSavingOcr(true);
    setUploadError(null);
    try {
      for (let i = 0; i < uploadedDocs.length; i++) {
        const doc = uploadedDocs[i];
        const editedText = ocrTexts[doc.id] ?? '';
        if (editedText !== (doc.content ?? '')) {
          await updateDocumentContent(doc.id, editedText);
        }
        await updateDocumentCategories(doc.id, {
          documentTypeId: selectedDocTypes[i],
          medicalSpecialtyId: selectedMedSpecs[i],
          medicalCategoryId: selectedMedCats[i],
        });
      }
      closeUploadModal();
      fetchDocuments();
    } catch {
      setUploadError(t('documents.ocrSaveError'));
    } finally {
      setSavingOcr(false);
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

      <div className="document-filters">
        <select
          className="form-select"
          value={filterDocType ?? ''}
          onChange={(e) => { setFilterDocType(e.target.value ? Number(e.target.value) : undefined); setPage(0); }}
        >
          <option value="">{t('documents.catDocumentType')}</option>
          {filterDocTypeOptions.map((cat) => (
            <option key={cat.id} value={cat.id}>{cat.label}</option>
          ))}
        </select>
        <select
          className="form-select"
          value={filterMedSpec ?? ''}
          onChange={(e) => { setFilterMedSpec(e.target.value ? Number(e.target.value) : undefined); setPage(0); }}
        >
          <option value="">{t('documents.catMedicalSpecialty')}</option>
          {filterMedSpecOptions.map((cat) => (
            <option key={cat.id} value={cat.id}>{cat.label}</option>
          ))}
        </select>
        <select
          className="form-select"
          value={filterMedCat ?? ''}
          onChange={(e) => { setFilterMedCat(e.target.value ? Number(e.target.value) : undefined); setPage(0); }}
        >
          <option value="">{t('documents.catMedicalCategory')}</option>
          {filterMedCatOptions.map((cat) => (
            <option key={cat.id} value={cat.id}>{cat.label}</option>
          ))}
        </select>
      </div>

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="modal-overlay" onClick={closeUploadModal}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{uploadStep === 1 ? t('documents.upload') : uploadStep === 2 ? t('documents.reviewOcr') : t('documents.assignCategories')}</h2>
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
                      accept={ACCEPT_STRING}
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
                      onClick={handleUploadAndOcr}
                      disabled={uploadLoading || selectedFiles.length === 0}
                    >
                      {uploadLoading ? t('documents.uploading') : t('documents.uploadAndScan')}
                    </button>
                  </div>
                </>
              )}

              {/* Step 2: Review OCR results */}
              {uploadStep === 2 && (
                <>
                  <p className="doc-category-subtitle" style={{ marginBottom: 16 }}>
                    {t('documents.ocrDescription')}
                  </p>

                  <div className="ocr-review-list">
                    {uploadedDocs.map((doc, index) => {
                      const file = selectedFiles[index];
                      const previewUrl = file ? URL.createObjectURL(file) : null;
                      const isImage = file?.type.startsWith('image/');
                      const isPdf = file?.type === 'application/pdf';

                      return (
                        <div key={doc.id} className="ocr-review-card">
                          <div className="ocr-review-header">
                            <span className="doc-category-file-icon">&#128196;</span>
                            <span className="doc-category-file-name">{doc.fileName}</span>
                          </div>
                          <div className="ocr-review-body">
                            {previewUrl && (
                              <div className="ocr-preview-pane">
                                {isImage && (
                                  <img
                                    src={previewUrl}
                                    alt={doc.fileName}
                                    className="ocr-preview-image"
                                  />
                                )}
                                {isPdf && (
                                  <iframe
                                    src={previewUrl}
                                    title={doc.fileName}
                                    className="ocr-preview-pdf"
                                  />
                                )}
                              </div>
                            )}
                            <textarea
                              className="ocr-review-textarea"
                              value={ocrTexts[doc.id] ?? ''}
                              onChange={(e) =>
                                setOcrTexts((prev) => ({ ...prev, [doc.id]: e.target.value }))
                              }
                              placeholder={t('documents.ocrNoText')}
                            />
                          </div>
                        </div>
                      );
                    })}
                  </div>

                  <div className="form-actions">
                    <button
                      type="button"
                      className="button secondary"
                      onClick={() => { setUploadStep(1); setUploadError(null); }}
                    >
                      {t('common.back')}
                    </button>
                    <button
                      type="button"
                      className="button"
                      onClick={goToCategories}
                      disabled={categoriesLoading}
                    >
                      {categoriesLoading ? t('common.loading') : t('documents.nextStep')}
                    </button>
                  </div>
                </>
              )}

              {/* Step 3: Assign categories & save */}
              {uploadStep === 3 && (
                <>
                  <p className="doc-category-subtitle" style={{ marginBottom: 16 }}>
                    {t('documents.categoriesRequired')}
                  </p>

                  <div className="doc-category-list" style={{ marginBottom: 20 }}>
                    {uploadedDocs.map((doc, fileIndex) => (
                      <div key={doc.id} className="doc-category-card">
                        <div className="doc-category-card-header">
                          <span className="doc-category-file-icon">&#128196;</span>
                          <div className="doc-category-file-info">
                            <span className="doc-category-file-name">{doc.fileName}</span>
                          </div>
                        </div>

                        <div className="doc-category-body">
                          <div className="doc-category-group">
                            <label className="doc-category-group-label">{t('documents.catDocumentType')}</label>
                            <select
                              className={`form-select${showValidation && !selectedDocTypes[fileIndex] ? ' form-select-error' : ''}`}
                              value={selectedDocTypes[fileIndex] ?? ''}
                              onChange={(e) => setSelectedDocTypes((prev) => ({ ...prev, [fileIndex]: Number(e.target.value) }))}
                            >
                              <option value="">{t('documents.catDocumentType')}...</option>
                              {docTypeCategories.map((cat) => (
                                <option key={cat.id} value={cat.id}>{cat.label}</option>
                              ))}
                            </select>
                          </div>

                          <div className="doc-category-group">
                            <label className="doc-category-group-label">{t('documents.catMedicalSpecialty')}</label>
                            <select
                              className={`form-select${showValidation && !selectedMedSpecs[fileIndex] ? ' form-select-error' : ''}`}
                              value={selectedMedSpecs[fileIndex] ?? ''}
                              onChange={(e) => setSelectedMedSpecs((prev) => ({ ...prev, [fileIndex]: Number(e.target.value) }))}
                            >
                              <option value="">{t('documents.catMedicalSpecialty')}...</option>
                              {medSpecCategories.map((cat) => (
                                <option key={cat.id} value={cat.id}>{cat.label}</option>
                              ))}
                            </select>
                          </div>

                          <div className="doc-category-group">
                            <label className="doc-category-group-label">{t('documents.catMedicalCategory')}</label>
                            <select
                              className={`form-select${showValidation && !selectedMedCats[fileIndex] ? ' form-select-error' : ''}`}
                              value={selectedMedCats[fileIndex] ?? ''}
                              onChange={(e) => setSelectedMedCats((prev) => ({ ...prev, [fileIndex]: Number(e.target.value) }))}
                            >
                              <option value="">{t('documents.catMedicalCategory')}...</option>
                              {medCatCategories.map((cat) => (
                                <option key={cat.id} value={cat.id}>{cat.label}</option>
                              ))}
                            </select>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="form-actions">
                    <button
                      type="button"
                      className="button secondary"
                      onClick={() => { setUploadStep(2); setUploadError(null); }}
                      disabled={savingOcr}
                    >
                      {t('common.back')}
                    </button>
                    <button
                      type="button"
                      className="button"
                      onClick={handleSaveAll}
                      disabled={savingOcr || !allCategoriesSelected}
                    >
                      {savingOcr ? t('common.loading') : t('common.save')}
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
