import { NavLink } from "react-router-dom";
import { useTheme } from "../context/ThemeContext.jsx";

const links = [
  { to: "/", label: "Home" },
  { to: "/create", label: "Create Feed" },
  { to: "/preview", label: "Preview" },
  { to: "/account", label: "Account" },
];

function Nav() {
  const { theme, toggleTheme } = useTheme();
  const nextTheme = theme === "dark" ? "light" : "dark";

  return (
    <nav>
      <ul>
        {links.map((link) => (
          <li key={link.to}>
            <NavLink to={link.to} end={link.to === "/"}>
              {link.label}
            </NavLink>
          </li>
        ))}
        <li>
          <button
            type="button"
            className="theme-toggle"
            onClick={toggleTheme}
            aria-label={`Switch to ${nextTheme} mode`}
          >
            {nextTheme === "dark" ? "Dark mode" : "Light mode"}
          </button>
        </li>
      </ul>
    </nav>
  );
}

export default Nav;
