import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getMedicalSummary, generateMedicalSummary } from '../api/summary';
import type { DocumentListOutView } from '../types';

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

export function SummaryPage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [prompt, setPrompt] = useState('');
  const [answer, setAnswer] = useState('');
  const [usedDocuments, setUsedDocuments] = useState<DocumentListOutView[]>([]);
  const [summaryLoading, setSummaryLoading] = useState(false);

  const [generateLoading, setGenerateLoading] = useState(false);

  const [error, setError] = useState('');

  const handleGenerateSummary = async () => {
    if (!prompt.trim()) return;
    setSummaryLoading(true);
    setError('');
    setAnswer('');
    setUsedDocuments([]);
    try {
      const result = await getMedicalSummary(prompt, i18n.language);
      setAnswer(result.summary);
      setUsedDocuments(result.usedDocuments);
    } catch {
      setError(t('summary.errorSummary'));
    } finally {
      setSummaryLoading(false);
    }
  };

  const handleGenerateCard = async () => {
    if (!answer.trim()) return;
    setGenerateLoading(true);
    setError('');
    try {
      const result = await generateMedicalSummary(answer, i18n.language);

      // Display QR code
      //setQrSrc(`data:image/png;base64,${result.qrBase64}`);

      // Download PDF (works in Safari + all browsers)
      const pdfBytes = Uint8Array.from(atob(result.pdfBase64), c => c.charCodeAt(0));
      const blob = new Blob([pdfBytes], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'MedJ-Summary.pdf';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      setTimeout(() => URL.revokeObjectURL(url), 10000);
    } catch {
      setError(t('summary.errorCard'));
    } finally {
      setGenerateLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>{t('summary.title')}</h1>

      <div className="summary-form">
        <label htmlFor="prompt-input" className="summary-label">{t('summary.prompt')}</label>
        <textarea
          id="prompt-input"
          className="summary-textarea"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder={t('summary.promptPlaceholder')}
          rows={5}
        />
        <div className="summary-actions">
          <button
            className="summary-btn"
            onClick={handleGenerateSummary}
            disabled={summaryLoading || !prompt.trim()}
          >
            {summaryLoading ? t('summary.generating') : t('summary.generateSummary')}
          </button>

          <button
            className={`summary-btn ${answer.trim() ? 'summary-btn--active' : 'summary-btn--disabled'}`}
            onClick={handleGenerateCard}
            disabled={generateLoading || !answer.trim()}
          >
            {generateLoading ? t('summary.generating') : t('summary.generateCard')}
          </button>
        </div>
      </div>

      {error && <div className="summary-error">{error}</div>}

      <div className="summary-answer-section">
        <label className="summary-label">{t('summary.answer')}</label>
        <div className="summary-answer" style={{ whiteSpace: 'pre-wrap' }}>
          {summaryLoading ? t('summary.generatingAnswer') : answer || t('summary.answerPlaceholder')}
        </div>
      </div>

      {usedDocuments.length > 0 && (
        <div className="summary-docs-section">
          <label className="summary-label">{t('summary.usedDocuments')}</label>
          <div className="summary-docs-list">
            {usedDocuments.map((doc) => (
              <div
                key={doc.id}
                className="summary-doc-item"
                onClick={() => navigate(`/documents/${doc.id}`)}
              >
                <span className="summary-doc-icon">&#128196;</span>
                <div className="summary-doc-info">
                  <span className="summary-doc-name">{doc.fileName}</span>
                  <span className="summary-doc-date">{formatDate(doc.createdOn)}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

    </div>
  );
}
