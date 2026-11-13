import { useNavigate } from "react-router-dom";
import { useFeed } from "../context/FeedContext.jsx";
import FeedItem from "../components/FeedItem.jsx";
import Stats from "../components/Stats.jsx";
import XmlOutput from "../components/XmlOutput.jsx";

function Preview() {
  const { feed, sampleFeed, visibleItems, isSample, rssOutput, backendFeedId, startEdit, removeItem } =
    useFeed();
  const navigate = useNavigate();

  function handleEdit(id) {
    startEdit(id);
    navigate("/create");
  }

  return (
    <section>
      <h2>RSS Feed Preview</h2>

      <Stats />

      <div className="feed-box">
        <h3>{feed.title || sampleFeed.title}</h3>

        <p className="feed-description">
          {feed.description || sampleFeed.description}
        </p>

        <p>
          <strong>Author:</strong> {feed.author || sampleFeed.author}
        </p>

        <p>
          <strong>Category:</strong> {feed.category || sampleFeed.category}
        </p>

        <hr />

        {isSample && (
          <p className="hint">
            These are sample items. Add your own on the Create Feed page.
          </p>
        )}

        {visibleItems.map((item) => (
          <FeedItem
            key={item.id}
            item={item}
            onEdit={isSample ? undefined : () => handleEdit(item.id)}
            onRemove={isSample ? undefined : () => removeItem(item.id)}
          />
        ))}
      </div>

      <h3>Generated RSS XML</h3>
      {backendFeedId && rssOutput && (
        <p className="hint">
          Loaded from Spring Boot endpoint /api/feeds/{backendFeedId}/rss.
        </p>
      )}
      <XmlOutput xml={rssOutput} />
    </section>
  );
}

export default Preview;
