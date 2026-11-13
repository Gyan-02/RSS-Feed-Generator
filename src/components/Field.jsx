function Field({ id, label, error, as: Control = "input", inputRef, ...props }) {
  const errorId = `${id}-error`;

  return (
    <>
      <label htmlFor={id}>{label}</label>
      <Control
        id={id}
        ref={inputRef}
        className={error ? "invalid" : undefined}
        aria-invalid={error ? "true" : undefined}
        aria-describedby={error ? errorId : undefined}
        {...props}
      />
      {error && (
        <p className="error" id={errorId} role="alert">
          {error}
        </p>
      )}
    </>
  );
}

export default Field;
