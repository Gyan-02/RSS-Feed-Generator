import { Link } from "react-router-dom";
import { useFeed } from "../context/FeedContext.jsx";
import Stats from "../components/Stats.jsx";
import SavedFeeds from "../components/SavedFeeds.jsx";

function Home() {
  const { feed, isSample } = useFeed();

  return (
    <section>
      <h2>Welcome</h2>
      <p className="lead">
        Build an RSS 2.0 feed in three steps, then copy or download the XML.
      </p>

      <ol className="steps">
        <li>Fill in your feed details and add articles on Create Feed.</li>
        <li>Press Generate RSS Feed to build the XML.</li>
        <li>Review, copy or download the result on Preview.</li>
      </ol>

      <h3>{feed.title ? `Draft: ${feed.title}` : "Your draft"}</h3>
      <p className="hint">
        {isSample
          ? "No items yet. Your draft is saved in this browser as you type."
          : "Your draft is saved in this browser, so it will still be here after a refresh."}
      </p>
      <Stats />

      <div className="actions">
        <Link className="button primary" to="/create">
          {isSample ? "Start a feed" : "Continue editing"}
        </Link>
        <Link className="button" to="/preview">
          View preview
        </Link>
      </div>

      <hr />
      <SavedFeeds />
    </section>
  );
}

export default Home;
