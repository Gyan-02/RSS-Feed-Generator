import { useState } from "react";
import AccountAccess from "../components/AccountAccess.jsx";
import { updateUserProfile } from "../services/userApi.js";

function Account() {
  const [user, setUser] = useState(null);
  const [currentPassword, setCurrentPassword] = useState("");
  const [profileMessage, setProfileMessage] = useState("");

  async function handleProfileUpdate(event) {
    event.preventDefault();
    setProfileMessage("");
    try {
      const updatedUser = await updateUserProfile(user.id, {
        name: user.name,
        email: user.email,
        currentPassword,
      });
      setUser(updatedUser);
      setCurrentPassword("");
      setProfileMessage("Profile updated.");
    } catch (requestError) {
      setProfileMessage(requestError.message || "The profile could not be updated.");
    }
  }

  return (
    <section className="account-page">
      <AccountAccess onSignedIn={setUser} />
      {user && (
        <div className="account-profile">
          <h3>Account profile</h3>
          <form onSubmit={handleProfileUpdate}>
            <label htmlFor="profile-name">
              Name
              <input id="profile-name" value={user.name} onChange={(event) => setUser({ ...user, name: event.target.value })} maxLength={120} required />
            </label>
            <label htmlFor="profile-email">
              Email
              <input id="profile-email" type="email" value={user.email} onChange={(event) => setUser({ ...user, email: event.target.value })} required />
            </label>
            <label htmlFor="profile-current-password">
              Current password to confirm the change
              <input id="profile-current-password" type="password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} autoComplete="current-password" required />
            </label>
            <button type="submit">Save profile</button>
          </form>
          {profileMessage && <p className={profileMessage === "Profile updated." ? "success" : "error"} role="status">{profileMessage}</p>}
        </div>
      )}
    </section>
  );
}

export default Account;
