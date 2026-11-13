import React from "react";
import ReactDOM from "react-dom/client";
import { HashRouter } from "react-router-dom";
import App from "./App.jsx";
import { ThemeProvider } from "./context/ThemeContext.jsx";
import { FeedProvider } from "./context/FeedContext.jsx";
import "./style.css";

// HashRouter keeps routes working on GitHub Pages, which has no server rewrites.
ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <HashRouter>
      <ThemeProvider>
        <FeedProvider>
          <App />
        </FeedProvider>
      </ThemeProvider>
    </HashRouter>
  </React.StrictMode>
);
