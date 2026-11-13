import { useRef, useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useFeed } from "../context/FeedContext.jsx";
import useFormFields from "../hooks/useFormFields.js";
import { validateItem } from "../utils/validate.js";
import Field from "../components/Field.jsx";

const categories = ["Technology", "News", "Education", "Sports", "Entertainment"];

const initialItem = { title: "", url: "", description: "" };

function CreateFeed() {
  const {
    feed,
    handleFeedChange,
    feedErrors,
    items,
    editingId,
    isSaving,
    apiError,
    apiSuccess,
    addItem,
    updateItem,
    cancelEdit,
    generateRss,
  } = useFeed();
  const navigate = useNavigate();
  const item = useFormFields(initialItem);
  const [itemSubmitted, setItemSubmitted] = useState(false);
  const itemTitleRef = useRef(null);

  const isEditing = editingId !== null;

  const itemErrors = useMemo(
    () => (itemSubmitted ? validateItem(item.values) : {}),
    [itemSubmitted, item.values]
  );

  // Load the chosen article into the item form when Edit is pressed.
  useEffect(() => {
    if (editingId === null) return;
    const editing = items.find((existing) => existing.id === editingId);
    if (editing) {
      item.setValues({
        title: editing.title,
        url: editing.url,
        description: editing.description,
      });
      itemTitleRef.current.focus();
    }
  }, [editingId]);

  function handleSaveItem() {
    setItemSubmitted(true);

    if (Object.keys(validateItem(item.values)).length > 0) return;

    if (isEditing) {
      updateItem(editingId, item.values);
    } else {
      addItem(item.values);
    }

    item.reset();
    setItemSubmitted(false);
    itemTitleRef.current.focus();
  }

  function handleCancelEdit() {
    cancelEdit();
    item.reset();
    setItemSubmitted(false);
  }

  async function handleGenerate() {
    if (await generateRss()) {
      navigate("/preview");
    }
  }

  return (
    <section>
      <h2>Create Your Feed</h2>

      <form noValidate onSubmit={(event) => event.preventDefault()}>
        <Field
          id="feed-title"
          label="Feed Title"
          name="title"
          type="text"
          placeholder="My News Feed"
          value={feed.title}
          onChange={handleFeedChange}
          error={feedErrors.title}
        />

        <Field
          id="website"
          label="Website URL"
          name="website"
          type="url"
          placeholder="https://example.com"
          value={feed.website}
          onChange={handleFeedChange}
          error={feedErrors.website}
        />

        <Field
          as="textarea"
          id="description"
          label="Feed Description"
          name="description"
          rows="4"
          placeholder="Enter a short description"
          value={feed.description}
          onChange={handleFeedChange}
          error={feedErrors.description}
        />

        <Field
          id="author"
          label="Author Name"
          name="author"
          type="text"
          placeholder="Your Name"
          value={feed.author}
          onChange={handleFeedChange}
          error={feedErrors.author}
        />

        <label htmlFor="category">Category</label>
        <select
          id="category"
          name="category"
          value={feed.category}
          onChange={handleFeedChange}
        >
          {categories.map((name) => (
            <option key={name}>{name}</option>
          ))}
        </select>

        <hr />

        <h3>{isEditing ? "Edit Feed Item" : "Add Feed Item"}</h3>
        {isEditing && (
          <p className="hint">
            Editing an existing article. Save your changes or cancel.
          </p>
        )}

        <Field
          id="item-title"
          label="Article Title"
          name="title"
          type="text"
          placeholder="Enter article title"
          inputRef={itemTitleRef}
          value={item.values.title}
          onChange={item.handleChange}
          error={itemErrors.title}
        />

        <Field
          id="article-url"
          label="Article URL"
          name="url"
          type="url"
          placeholder="https://example.com/article"
          value={item.values.url}
          onChange={item.handleChange}
          error={itemErrors.url}
        />

        <Field
          as="textarea"
          id="item-description"
          label="Article Description"
          name="description"
          rows="4"
          placeholder="Enter article description"
          value={item.values.description}
          onChange={item.handleChange}
          error={itemErrors.description}
        />

        <div className="actions">
          <button type="button" onClick={handleSaveItem}>
            {isEditing ? "Save Changes" : "Add Feed Item"}
          </button>
          {isEditing && (
            <button type="button" onClick={handleCancelEdit}>
              Cancel
            </button>
          )}
          <button
            type="button"
            className="primary"
            onClick={handleGenerate}
            disabled={isSaving}
          >
            {isSaving ? "Generating..." : "Generate RSS Feed"}
          </button>
        </div>

        {apiSuccess && (
          <p className="success" role="status">
            {apiSuccess}
          </p>
        )}

        {apiError && (
          <p className="error" role="alert">
            {apiError}
          </p>
        )}

        {feedErrors.items && (
          <p className="error" role="alert">
            {feedErrors.items}
          </p>
        )}
      </form>
    </section>
  );
}

export default CreateFeed;
