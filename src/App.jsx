import { Routes, Route, Navigate } from "react-router-dom";
import Header from "./components/Header.jsx";
import Nav from "./components/Nav.jsx";
import Footer from "./components/Footer.jsx";
import Home from "./pages/Home.jsx";
import CreateFeed from "./pages/CreateFeed.jsx";
import Preview from "./pages/Preview.jsx";
import Account from "./pages/Account.jsx";

function App() {
  return (
    <>
      <Header />
      <Nav />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/create" element={<CreateFeed />} />
          <Route path="/preview" element={<Preview />} />
          <Route path="/account" element={<Account />} />
          <Route path="/login" element={<Account />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </>
  );
}

export default App;
