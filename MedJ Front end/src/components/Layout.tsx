import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';

export function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logoutUser } = useAuth();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();

  const toggleLanguage = () => {
    i18n.changeLanguage(i18n.language === 'en' ? 'bg' : 'en');
  };

  const handleLogout = () => {
    logoutUser();
    navigate('/login', { replace: true });
  };

  const closeSidebar = () => setSidebarOpen(false);

  const handleNavClick = () => {
    if (window.innerWidth <= 768) {
      setSidebarOpen(false);
    }
  };

  return (
    <div className="app-layout">
      <header className="mobile-header">
        <button
          className="hamburger-btn"
          onClick={() => setSidebarOpen(!sidebarOpen)}
          aria-label="Toggle menu"
        >
          <span className={`hamburger-icon ${sidebarOpen ? 'open' : ''}`}>
            <span></span>
            <span></span>
            <span></span>
          </span>
        </button>
        <h2 className="mobile-logo">MedJ</h2>
      </header>

      {sidebarOpen && <div className="sidebar-overlay" onClick={closeSidebar} />}

      <nav className={`sidebar ${sidebarOpen ? 'sidebar--open' : ''}`}>
        <div className="logo">
          <h2>MedJ</h2>
        </div>
        <ul className="nav-links">
          <li>
            <NavLink to="/" end className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              {t('nav.dashboard')}
            </NavLink>
          </li>
          <li>
            <NavLink to="/documents" className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              {t('nav.documents')}
            </NavLink>
          </li>
          <li>
            <NavLink to="/appointments" className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              {t('nav.appointments')}
            </NavLink>
          </li>
          <li>
            <NavLink to="/practitioners" className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              {t('nav.practitioners')}
            </NavLink>
          </li>
          <li>
            <NavLink to="/summary" className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              {t('nav.summary')}
            </NavLink>
          </li>
        </ul>

        <button className="lang-toggle" onClick={toggleLanguage}>
  <img
    src={i18n.language === 'en' 
      ? '/flags/bg.svg' 
      : '/flags/en.svg'}
    alt="language"
    style={{ width: '24px', height: '24px' }}
  />
</button>

        <div className="sidebar-user">
          <div className="sidebar-user-info">
            <span className="sidebar-user-avatar">
              {user?.username?.[0]?.toUpperCase() ?? '?'}
            </span>
            <div className="sidebar-user-details">
              <span className="sidebar-user-name">{user?.username}</span>
              <span className="sidebar-user-email">{user?.role}</span>
            </div>
          </div>
          <div className="sidebar-user-actions">
            <NavLink
              to="/profile"
              className={({ isActive }) => `sidebar-action-link${isActive ? ' active' : ''}`}
            >
              {t('nav.profile')}
            </NavLink>
            <NavLink
              to="/change-password"
              className={({ isActive }) => `sidebar-action-link${isActive ? ' active' : ''}`}
            >
              {t('nav.changePassword')}
            </NavLink>
            <button className="sidebar-logout-btn" onClick={handleLogout}>
              {t('nav.signOut')}
            </button>
          </div>
        </div>
      </nav>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
