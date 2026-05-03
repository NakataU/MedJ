import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
// import { useNavigate } from 'react-router-dom';
// import { useAuth } from '../context/AuthContext';

export function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  // const { user, logoutUser } = useAuth();
  // const navigate = useNavigate();

  // const handleLogout = () => {
  //   logoutUser();
  //   navigate('/login', { replace: true });
  // };

  const closeSidebar = () => setSidebarOpen(false);

  // Close sidebar on navigation (for mobile)
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
              Dashboard
            </NavLink>
          </li>
          <li>
            <NavLink to="/documents" className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              Documents
            </NavLink>
          </li>
          <li>
            <NavLink to="/appointments" className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              Appointments
            </NavLink>
          </li>
          <li>
            <NavLink to="/practitioners" className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              Practitioners
            </NavLink>
          </li>
          <li>
            <NavLink to="/summary" className={({ isActive }) => isActive ? 'active' : ''} onClick={handleNavClick}>
              Summary
            </NavLink>
          </li>
        </ul>

        {/* User section - temporarily disabled until auth is enabled */}
        {/* <div className="sidebar-user">
          <div className="sidebar-user-info">
            <span className="sidebar-user-avatar">
              {user?.firstName?.[0]?.toUpperCase() ?? '?'}
            </span>
            <div className="sidebar-user-details">
              <span className="sidebar-user-name">{user?.firstName}</span>
              <span className="sidebar-user-email">{user?.email}</span>
            </div>
          </div>
          <div className="sidebar-user-actions">
            <NavLink
              to="/change-password"
              className={({ isActive }) => `sidebar-action-link${isActive ? ' active' : ''}`}
            >
              Change Password
            </NavLink>
            <button className="sidebar-logout-btn" onClick={handleLogout}>
              Sign out
            </button>
          </div>
        </div> */}
      </nav>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
