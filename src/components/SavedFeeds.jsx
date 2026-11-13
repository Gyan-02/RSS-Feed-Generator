import { useEffect, useState } from "react";
import { getFeedRssUrl, getFeeds } from "../services/feedApi.js";

function SavedFeeds() {
  const [feeds, setFeeds] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isCurrent = true;

    getFeeds()
      .then((savedFeeds) => {
        if (isCurrent) setFeeds(savedFeeds);
      })
      .catch((requestError) => {
        if (isCurrent) {
          setError(requestError instanceof TypeError
            ? "Could not reach Spring Boot. Check that the backend is running and CORS allows this frontend."
            : requestError.message || "Could not load saved feeds.");
        }
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false);
      });

    return () => {
      isCurrent = false;
    };
  }, []);

  return (
    <section className="saved-feeds" aria-labelledby="saved-feeds-heading">
      <h3 id="saved-feeds-heading">Saved Feeds</h3>
      <p className="hint">
        Loaded from Spring Boot's H2 database. Records reset when the backend stops.
      </p>

      {isLoading && <p className="status" role="status">Loading saved feeds...</p>}
      {error && <p className="error" role="alert">{error}</p>}
      {!isLoading && !error && feeds.length === 0 && (
        <p className="status">No feeds have been saved to the database yet.</p>
      )}

      {!error && feeds.length > 0 && (
        <ul className="saved-feed-list">
          {feeds.map((savedFeed) => (
            <li className="saved-feed" key={savedFeed.id}>
              <div>
                <h4>{savedFeed.title}</h4>
                <p>{savedFeed.description}</p>
                <span className="hint">
                  {savedFeed.items?.length ?? 0} {(savedFeed.items?.length ?? 0) === 1 ? "article" : "articles"}
                  {savedFeed.category ? ` · ${savedFeed.category}` : ""}
                </span>
              </div>
              <a
                className="button"
                href={getFeedRssUrl(savedFeed.id)}
                target="_blank"
                rel="noreferrer"
              >
                Open RSS XML
              </a>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default SavedFeeds;
