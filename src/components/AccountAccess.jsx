import { useState } from "react";
import { loginUser, registerUser } from "../services/userApi.js";

function AccountAccess({ onSignedIn }) {
  const [mode, setMode] = useState("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function changeMode(nextMode) {
    setMode(nextMode);
    setMessage("");
    setMessageType("");
    setPassword("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage("");
    setMessageType("");
    setIsSubmitting(true);
    try {
      if (mode === "register") {
        const createdUser = await registerUser({ name, email, password });
        setMessage(`Account created for ${createdUser.name}. You can now log in.`);
        setMessageType("success");
        setMode("login");
        setName("");
        setPassword("");
      } else {
        const signedInUser = await loginUser({ email, password });
        onSignedIn(signedInUser);
        setMessage(`Signed in as ${signedInUser.name}.`);
        setMessageType("success");
        setPassword("");
      }
    } catch (requestError) {
      setMessage(requestError.message || "The account request could not be completed.");
      setMessageType("error");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="account-access" aria-labelledby="account-access-title">
      <div className="account-access-heading">
        <span className="account-access-kicker">YOUR RSS WORKSPACE</span>
        <h2 id="account-access-title">{mode === "login" ? "Welcome back" : "Create your account"}</h2>
        <p>{mode === "login" ? "Sign in to manage your account." : "Join to keep your account details in one place."}</p>
      </div>

      <div className="account-tabs" role="tablist" aria-label="Account actions">
        <button type="button" role="tab" aria-selected={mode === "login"} className={mode === "login" ? "primary" : ""} onClick={() => changeMode("login")}>
          Sign in
        </button>
        <button type="button" role="tab" aria-selected={mode === "register"} className={mode === "register" ? "primary" : ""} onClick={() => changeMode("register")}>
          Create account
        </button>
      </div>

      <form className="account-form" onSubmit={handleSubmit}>
        {mode === "register" && (
          <label htmlFor="account-name">
            Name
            <input id="account-name" name="name" type="text" value={name} onChange={(event) => setName(event.target.value)} autoComplete="name" maxLength={120} required />
          </label>
        )}
        <label htmlFor="account-email">
          Email address
          <input id="account-email" name="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" required />
        </label>
        <label htmlFor="account-password">
          Password
          <input id="account-password" name="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete={mode === "login" ? "current-password" : "new-password"} minLength={mode === "register" ? 8 : undefined} maxLength={72} required />
        </label>
        <button className="primary account-submit" type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Please wait…" : mode === "login" ? "Sign in" : "Create account"}
        </button>
      </form>

      {message && <p className={messageType === "success" ? "success" : "error"} role="status" aria-live="polite">{message}</p>}
      <p className="hint account-session-note">Your password is securely hashed before it is saved.</p>
    </section>
  );
}

export default AccountAccess;
