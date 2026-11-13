function FeedItem({ item, onEdit, onRemove }) {
  return (
    <article>
      <h3>{item.title}</h3>
      <p>{item.description}</p>
      <a href={item.url} target="_blank" rel="noreferrer">
        Read Article
      </a>

      {(onEdit || onRemove) && (
        <div className="item-actions">
          {onEdit && (
            <button type="button" onClick={onEdit}>
              Edit
            </button>
          )}
          {onRemove && (
            <button type="button" className="danger" onClick={onRemove}>
              Remove
            </button>
          )}
        </div>
      )}
    </article>
  );
}

export default FeedItem;
