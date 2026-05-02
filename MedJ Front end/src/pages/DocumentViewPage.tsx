import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDocumentById, getPreviewUrl, getDownloadUrl } from '../api/documents';
import type { DocumentOutView } from '../types';


// Helper to format file size — handles both raw bytes (number) and pre-formatted string
const formatSize = (size: string | number): string => {
  const bytes = Number(size);
  if (!isNaN(bytes) && bytes > 0) {
    const k = 1024;
    const units = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + units[i];
  }
  if (bytes === 0) return '0 Bytes';
  return String(size);
};

// Helper to check if file type is previewable
const isPreviewable = (contentType: string): boolean => {
  const previewableTypes = [
    'application/pdf',
    'image/png',
    'image/jpeg',
    'image/jpg',
    'image/gif',
    'image/webp',
    'image/svg+xml',
    'text/plain',
    'text/html',
  ];
  return previewableTypes.some(type => contentType?.toLowerCase().includes(type));
};

// Helper to check if file is an image
const isImage = (contentType: string): boolean => {
  return contentType?.toLowerCase().startsWith('image/');
};

export function DocumentViewPage() {
  const { id } = useParams<{ id: string }>();
  const [document, setDocument] = useState<DocumentOutView | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [previewError, setPreviewError] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!id) {
      setError('Invalid document ID');
      setLoading(false);
      return;
    }

    const documentId = Number(id);

    if (isNaN(documentId)) {
      setError('Invalid document ID');
      setLoading(false);
      return;
    }

    const fetchDocument = async () => {
      setLoading(true);
      setError(null);
      setPreviewError(false);
      try {
        const data = await getDocumentById(documentId);
        setDocument(data);
      } catch (err) {
        console.error(err);
        setError('Failed to load document');
      } finally {
        setLoading(false);
      }
    };

    fetchDocument();
  }, [id]);

  const handlePreviewError = () => {
    setPreviewError(true);
  };

  const renderPreview = () => {
    if (!document) return null;

    const previewUrl = getPreviewUrl(document.id);
    const canPreview = isPreviewable(document.contentType);

    if (previewError || !canPreview) {
      return (
        <div className="preview-unavailable">
          <p>Preview not available for this file type.</p>
          <p>Content type: {document.contentType || 'Unknown'}</p>
          <p>Please download the file to view it.</p>
        </div>
      );
    }

    if (isImage(document.contentType)) {
      return (
        <img
          src={previewUrl}
          alt={document.fileName}
          className="preview-image"
          onError={handlePreviewError}
        />
      );
    }

    // PDF and other documents use iframe
    return (
      <iframe
        src={previewUrl}
        title={document.fileName}
        className="preview-pdf"
        onError={handlePreviewError}
      />
    );
  };

  if (loading) return <div className="loading">Loading document...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!document) return <div className="error">Document not found</div>;

  return (
    <div className="page">
      <button className="back-button" onClick={() => navigate('/documents')}>
        ← Back to Documents
      </button>

      <div className="document-details">
        <h1>{document.fileName}</h1>

        <div className="document-info">
          <p><strong>Size:</strong> {formatSize(document.size)}</p>
          <p><strong>Type:</strong> {document.contentType || 'Unknown'}</p>
        </div>

        <div className="document-actions">
          <a
            href={getDownloadUrl(document.id)}
            className="button"
            download={document.fileName}
          >
            Download
          </a>
          <a
            href={getPreviewUrl(document.id)}
            target="_blank"
            rel="noopener noreferrer"
            className="button secondary"
          >
            Open in New Tab
          </a>
        </div>

        <div className="document-preview">
          {renderPreview()}
        </div>
      </div>
    </div>
  );
}
