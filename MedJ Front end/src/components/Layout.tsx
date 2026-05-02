import { NavLink, Outlet } from 'react-router-dom';
// import { useNavigate } from 'react-router-dom';
// import { useAuth } from '../context/AuthContext';

export function Layout() {
  // const { user, logoutUser } = useAuth();
  // const navigate = useNavigate();

  // const handleLogout = () => {
  //   logoutUser();
  //   navigate('/login', { replace: true });
  // };

  return (
    <div className="app-layout">
      <nav className="sidebar">
        <div className="logo">
          <h2>MedJ</h2>
        </div>
        <ul className="nav-links">
          <li>
            <NavLink to="/" end className={({ isActive }) => isActive ? 'active' : ''}>
              Dashboard
            </NavLink>
          </li>
          <li>
            <NavLink to="/documents" className={({ isActive }) => isActive ? 'active' : ''}>
              Documents
            </NavLink>
          </li>
          <li>
            <NavLink to="/appointments" className={({ isActive }) => isActive ? 'active' : ''}>
              Appointments
            </NavLink>
          </li>
          <li>
            <NavLink to="/practitioners" className={({ isActive }) => isActive ? 'active' : ''}>
              Practitioners
            </NavLink>
          </li>
          <li>
            <NavLink to="/summary" className={({ isActive }) => isActive ? 'active' : ''}>
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
