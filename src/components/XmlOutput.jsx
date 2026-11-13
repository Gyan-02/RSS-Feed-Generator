import { useRef, useState, useEffect } from "react";

function XmlOutput({ xml }) {
  const preRef = useRef(null);
  const downloadRef = useRef(null);
  const timerRef = useRef(null);
  const [status, setStatus] = useState("");

  useEffect(() => () => clearTimeout(timerRef.current), []);

  function flash(message) {
    setStatus(message);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setStatus(""), 2500);
  }

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(preRef.current.textContent);
      flash("Copied to clipboard.");
    } catch {
      flash("Copy failed. Select the XML and copy it manually.");
    }
  }

  function handleDownload() {
    const blob = new Blob([preRef.current.textContent], {
      type: "application/rss+xml",
    });
    const url = URL.createObjectURL(blob);
    const link = downloadRef.current;

    link.href = url;
    link.download = "feed.xml";
    link.click();

    setTimeout(() => URL.revokeObjectURL(url), 1000);
    flash("Download started.");
  }

  if (!xml) {
    return (
      <p className="hint">
        No XML yet. Press "Generate RSS Feed" on the Create Feed page.
      </p>
    );
  }

  return (
    <>
      <div className="actions">
        <button type="button" onClick={handleCopy}>
          Copy XML
        </button>
        <button type="button" onClick={handleDownload}>
          Download XML
        </button>
        <span className="status" role="status">
          {status}
        </span>
        <a ref={downloadRef} hidden aria-hidden="true" tabIndex={-1}>
          Download
        </a>
      </div>
      <pre id="rss-output" ref={preRef}>
        {xml}
      </pre>
    </>
  );
}

export default XmlOutput;
