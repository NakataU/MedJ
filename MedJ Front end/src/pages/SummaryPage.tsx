import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getMedicalSummary, generateMedicalSummary } from '../api/summary';

export function SummaryPage() {
  const { t } = useTranslation();
  const [prompt, setPrompt] = useState('');
  const [answer, setAnswer] = useState('');
  const [summaryLoading, setSummaryLoading] = useState(false);

  //const [qrSrc, setQrSrc] = useState('');
  const [generateLoading, setGenerateLoading] = useState(false);

  const [error, setError] = useState('');

  const handleGenerateSummary = async () => {
    if (!prompt.trim()) return;
    setSummaryLoading(true);
    setError('');
    setAnswer('');
    try {
      const result = await getMedicalSummary(prompt);
      setAnswer(result);
    } catch {
      setError(t('summary.errorSummary'));
    } finally {
      setSummaryLoading(false);
    }
  };

  const handleGenerateCard = async () => {
    if (!prompt.trim()) return;
    setGenerateLoading(true);
    setError('');
    //setQrSrc('');
    try {
      const result = await generateMedicalSummary(prompt);

      // Display QR code
      //setQrSrc(`data:image/png;base64,${result.qrBase64}`);

      // Open PDF in new tab
      const pdfBytes = Uint8Array.from(atob(result.pdfBase64), c => c.charCodeAt(0));
      const blob = new Blob([pdfBytes], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank');
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
        <div className="summary-answer">
          {summaryLoading ? t('summary.generatingAnswer') : answer || t('summary.answerPlaceholder')}
        </div>
      </div>

    </div>
  );
}
