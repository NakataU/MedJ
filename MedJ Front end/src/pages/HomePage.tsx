import { useState } from 'react';

// Placeholder data for diagrams - will be connected to real data later
const placeholderStats = {
  totalDocuments: 24,
  totalAppointments: 8,
  upcomingAppointments: 3,
  recentUploads: 5,
};

// Placeholder chart data
const monthlyData = [
  { month: 'Jan', documents: 4, appointments: 2 },
  { month: 'Feb', documents: 6, appointments: 3 },
  { month: 'Mar', documents: 3, appointments: 1 },
  { month: 'Apr', documents: 8, appointments: 4 },
  { month: 'May', documents: 5, appointments: 2 },
  { month: 'Jun', documents: 7, appointments: 3 },
];

export function HomePage() {
  const [stats] = useState(placeholderStats);
  const [chartData] = useState(monthlyData);

  // Find max value for chart scaling
  const maxValue = Math.max(...chartData.map(d => Math.max(d.documents, d.appointments)));

  return (
    <div className="page home-page">
      <header className="home-header">
        <h1>Welcome to MedJ</h1>
        <p className="subtitle">Your personal medical document management system</p>
      </header>

      {/* Stats Cards */}
      <section className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon documents-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
            </svg>
          </div>
          <div className="stat-content">
            <span className="stat-value">{stats.totalDocuments}</span>
            <span className="stat-label">Total Documents</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon appointments-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
          </div>
          <div className="stat-content">
            <span className="stat-value">{stats.totalAppointments}</span>
            <span className="stat-label">Total Appointments</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon upcoming-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
          </div>
          <div className="stat-content">
            <span className="stat-value">{stats.upcomingAppointments}</span>
            <span className="stat-label">Upcoming</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon uploads-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="15" />
            </svg>
          </div>
          <div className="stat-content">
            <span className="stat-value">{stats.recentUploads}</span>
            <span className="stat-label">Recent Uploads</span>
          </div>
        </div>
      </section>

      {/* Charts Section */}
      <section className="charts-section">
        <div className="chart-container">
          <h2>Monthly Activity</h2>
          <p className="chart-description">Documents and appointments over the past 6 months</p>

          <div className="bar-chart">
            <div className="chart-y-axis">
              <span>{maxValue}</span>
              <span>{Math.round(maxValue / 2)}</span>
              <span>0</span>
            </div>
            <div className="chart-bars">
              {chartData.map((item) => (
                <div key={item.month} className="chart-bar-group">
                  <div className="bars">
                    <div
                      className="bar documents-bar"
                      style={{ height: `${(item.documents / maxValue) * 100}%` }}
                      title={`Documents: ${item.documents}`}
                    />
                    <div
                      className="bar appointments-bar"
                      style={{ height: `${(item.appointments / maxValue) * 100}%` }}
                      title={`Appointments: ${item.appointments}`}
                    />
                  </div>
                  <span className="bar-label">{item.month}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="chart-legend">
            <div className="legend-item">
              <span className="legend-color documents-color"></span>
              <span>Documents</span>
            </div>
            <div className="legend-item">
              <span className="legend-color appointments-color"></span>
              <span>Appointments</span>
            </div>
          </div>
        </div>

        <div className="chart-container">
          <h2>Quick Overview</h2>
          <p className="chart-description">Document distribution by type</p>

          <div className="donut-chart-container">
            <div className="donut-chart">
              <svg viewBox="0 0 36 36" className="donut-svg">
                <circle
                  className="donut-ring"
                  cx="18"
                  cy="18"
                  r="15.915"
                  fill="transparent"
                  stroke="#e2e8f0"
                  strokeWidth="3"
                />
                <circle
                  className="donut-segment donut-segment-1"
                  cx="18"
                  cy="18"
                  r="15.915"
                  fill="transparent"
                  stroke="#2b6cb0"
                  strokeWidth="3"
                  strokeDasharray="40 60"
                  strokeDashoffset="25"
                />
                <circle
                  className="donut-segment donut-segment-2"
                  cx="18"
                  cy="18"
                  r="15.915"
                  fill="transparent"
                  stroke="#48bb78"
                  strokeWidth="3"
                  strokeDasharray="25 75"
                  strokeDashoffset="85"
                />
                <circle
                  className="donut-segment donut-segment-3"
                  cx="18"
                  cy="18"
                  r="15.915"
                  fill="transparent"
                  stroke="#ed8936"
                  strokeWidth="3"
                  strokeDasharray="20 80"
                  strokeDashoffset="60"
                />
                <circle
                  className="donut-segment donut-segment-4"
                  cx="18"
                  cy="18"
                  r="15.915"
                  fill="transparent"
                  stroke="#9f7aea"
                  strokeWidth="3"
                  strokeDasharray="15 85"
                  strokeDashoffset="40"
                />
              </svg>
              <div className="donut-center">
                <span className="donut-total">{stats.totalDocuments}</span>
                <span className="donut-label">Total</span>
              </div>
            </div>

            <div className="donut-legend">
              <div className="legend-item">
                <span className="legend-color" style={{ backgroundColor: '#2b6cb0' }}></span>
                <span>PDF Reports (40%)</span>
              </div>
              <div className="legend-item">
                <span className="legend-color" style={{ backgroundColor: '#48bb78' }}></span>
                <span>Lab Results (25%)</span>
              </div>
              <div className="legend-item">
                <span className="legend-color" style={{ backgroundColor: '#ed8936' }}></span>
                <span>Prescriptions (20%)</span>
              </div>
              <div className="legend-item">
                <span className="legend-color" style={{ backgroundColor: '#9f7aea' }}></span>
                <span>Other (15%)</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
