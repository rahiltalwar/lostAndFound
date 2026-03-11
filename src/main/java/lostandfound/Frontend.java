package lostandfound;

/**
 * The entire frontend UI, returned as an HTML string.
 * Served from memory — no static file server needed.
 */
public class Frontend {

    public static String getHtml() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>Lost & Found — School AI Agent</title>
<link href="https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:ital,wght@0,300;0,400;0,500;1,300&display=swap" rel="stylesheet"/>
<style>
  :root {
    --bg: #0f0e17;
    --surface: #1a1929;
    --surface2: #22213a;
    --accent: #ff6b35;
    --accent2: #ffd166;
    --accent3: #06d6a0;
    --text: #fffffe;
    --text-muted: #a7a9be;
    --border: rgba(255,255,255,0.08);
    --radius: 16px;
    --shadow: 0 8px 32px rgba(0,0,0,0.4);
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: 'DM Sans', sans-serif;
    background: var(--bg);
    color: var(--text);
    min-height: 100vh;
    overflow-x: hidden;
  }

  /* ── Background mesh ── */
  body::before {
    content: '';
    position: fixed; inset: 0;
    background:
      radial-gradient(ellipse 80% 50% at 20% 10%, rgba(255,107,53,0.12) 0%, transparent 60%),
      radial-gradient(ellipse 60% 40% at 80% 80%, rgba(6,214,160,0.08) 0%, transparent 60%);
    pointer-events: none; z-index: 0;
  }

  /* ── Layout ── */
  .app { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; padding: 0 24px 60px; }

  /* ── Header ── */
  header {
    padding: 40px 0 32px;
    display: flex; align-items: center; gap: 20px;
    border-bottom: 1px solid var(--border);
    margin-bottom: 40px;
  }
  .logo {
    width: 56px; height: 56px; border-radius: 14px;
    background: linear-gradient(135deg, var(--accent), var(--accent2));
    display: flex; align-items: center; justify-content: center;
    font-size: 26px; flex-shrink: 0;
    box-shadow: 0 4px 20px rgba(255,107,53,0.4);
  }
  header h1 {
    font-family: 'Syne', sans-serif;
    font-size: clamp(1.5rem, 4vw, 2.2rem);
    font-weight: 800;
    line-height: 1.1;
  }
  header h1 span { color: var(--accent); }
  header p { color: var(--text-muted); font-size: 0.9rem; margin-top: 4px; }
  .header-badge {
    margin-left: auto;
    background: rgba(6,214,160,0.15);
    color: var(--accent3);
    border: 1px solid rgba(6,214,160,0.3);
    border-radius: 20px; padding: 6px 14px;
    font-size: 0.78rem; font-weight: 500;
    white-space: nowrap;
  }

  /* ── Tabs ── */
  .tabs {
    display: flex; gap: 4px;
    background: var(--surface); border-radius: 12px; padding: 4px;
    margin-bottom: 36px; width: fit-content;
  }
  .tab {
    padding: 10px 24px; border-radius: 10px; border: none;
    font-family: 'Syne', sans-serif; font-size: 0.85rem; font-weight: 600;
    cursor: pointer; transition: all 0.2s; color: var(--text-muted);
    background: transparent;
  }
  .tab.active { background: var(--accent); color: white; box-shadow: 0 2px 12px rgba(255,107,53,0.4); }
  .tab:not(.active):hover { color: var(--text); background: var(--surface2); }

  /* ── Panels ── */
  .panel { display: none; }
  .panel.active { display: block; animation: fadeIn 0.3s ease; }
  @keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: none; } }

  /* ── Search Panel ── */
  .search-hero {
    background: var(--surface);
    border-radius: var(--radius); padding: 32px;
    border: 1px solid var(--border);
    margin-bottom: 32px;
  }
  .search-hero h2 {
    font-family: 'Syne', sans-serif; font-size: 1.3rem; font-weight: 700;
    margin-bottom: 6px;
  }
  .search-hero p { color: var(--text-muted); font-size: 0.9rem; margin-bottom: 24px; }
  .search-bar {
    display: flex; gap: 12px; align-items: center;
  }
  .search-bar input {
    flex: 1; padding: 14px 20px;
    background: var(--surface2); border: 1px solid var(--border);
    border-radius: 12px; color: var(--text); font-size: 1rem;
    font-family: 'DM Sans', sans-serif;
    transition: border-color 0.2s;
    outline: none;
  }
  .search-bar input:focus { border-color: var(--accent); }
  .search-bar input::placeholder { color: var(--text-muted); }
  .btn-search {
    padding: 14px 28px;
    background: linear-gradient(135deg, var(--accent), #ff8c5a);
    border: none; border-radius: 12px; color: white;
    font-family: 'Syne', sans-serif; font-size: 0.9rem; font-weight: 700;
    cursor: pointer; transition: all 0.2s; white-space: nowrap;
    box-shadow: 0 4px 16px rgba(255,107,53,0.3);
  }
  .btn-search:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(255,107,53,0.4); }
  .btn-search:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

  .search-hints {
    display: flex; gap: 8px; flex-wrap: wrap; margin-top: 16px;
  }
  .hint-chip {
    padding: 6px 14px; border-radius: 20px;
    background: var(--surface2); border: 1px solid var(--border);
    font-size: 0.78rem; color: var(--text-muted);
    cursor: pointer; transition: all 0.2s;
  }
  .hint-chip:hover { border-color: var(--accent); color: var(--accent); }

  /* ── AI Response box ── */
  .ai-response {
    background: linear-gradient(135deg, rgba(6,214,160,0.08), rgba(6,214,160,0.03));
    border: 1px solid rgba(6,214,160,0.25);
    border-radius: var(--radius); padding: 20px 24px;
    margin-bottom: 28px; display: none;
  }
  .ai-response.visible { display: flex; gap: 14px; }
  .ai-icon { font-size: 1.4rem; flex-shrink: 0; }
  .ai-response p { color: var(--text); font-size: 0.95rem; line-height: 1.6; }

  /* ── Items Grid ── */
  .section-header {
    display: flex; align-items: center; justify-content: space-between;
    margin-bottom: 20px;
  }
  .section-header h3 {
    font-family: 'Syne', sans-serif; font-weight: 700; font-size: 1.1rem;
  }
  .count-badge {
    background: var(--surface2); border-radius: 20px;
    padding: 4px 12px; font-size: 0.78rem; color: var(--text-muted);
  }
  .items-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }
  .item-card {
    background: var(--surface); border-radius: var(--radius);
    border: 1px solid var(--border); overflow: hidden;
    transition: all 0.25s; cursor: pointer;
  }
  .item-card:hover {
    transform: translateY(-3px);
    border-color: rgba(255,107,53,0.4);
    box-shadow: var(--shadow);
  }
  .item-card.claimed { opacity: 0.55; }
  .item-img {
    width: 100%; height: 180px; object-fit: cover;
    background: var(--surface2);
  }
  .item-img-placeholder {
    width: 100%; height: 180px;
    background: var(--surface2);
    display: flex; align-items: center; justify-content: center;
    font-size: 3rem;
  }
  .item-body { padding: 16px; }
  .item-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
  .item-id { font-family: 'Syne', sans-serif; font-weight: 700; font-size: 1rem; }
  .item-status {
    font-size: 0.72rem; font-weight: 600; padding: 3px 10px; border-radius: 10px;
    text-transform: uppercase; letter-spacing: 0.04em;
  }
  .item-status.unclaimed { background: rgba(255,209,102,0.15); color: var(--accent2); }
  .item-status.claimed { background: rgba(6,214,160,0.15); color: var(--accent3); }
  .item-desc { font-size: 0.88rem; color: var(--text-muted); line-height: 1.5; margin-bottom: 10px; }
  .item-tags { display: flex; gap: 6px; flex-wrap: wrap; }
  .tag {
    font-size: 0.72rem; padding: 3px 10px; border-radius: 8px;
    background: var(--surface2); color: var(--text-muted);
    border: 1px solid var(--border);
  }
  .tag.color-tag { border-color: rgba(255,107,53,0.3); color: var(--accent); }

  /* ── Upload Panel ── */
  .upload-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
  @media (max-width: 700px) { .upload-grid { grid-template-columns: 1fr; } }

  .upload-card {
    background: var(--surface); border-radius: var(--radius);
    border: 1px solid var(--border); padding: 28px;
  }
  .upload-card h3 {
    font-family: 'Syne', sans-serif; font-weight: 700; font-size: 1rem;
    margin-bottom: 20px; display: flex; align-items: center; gap: 8px;
  }
  .upload-card h3 span { font-size: 1.2rem; }

  /* Drop zone */
  .dropzone {
    border: 2px dashed var(--border); border-radius: 12px;
    padding: 40px 20px; text-align: center;
    cursor: pointer; transition: all 0.2s;
    position: relative;
    background: var(--surface2);
    margin-bottom: 16px;
  }
  .dropzone:hover, .dropzone.dragover {
    border-color: var(--accent); background: rgba(255,107,53,0.05);
  }
  .dropzone input[type=file] {
    position: absolute; inset: 0; opacity: 0; cursor: pointer; width: 100%; height: 100%;
  }
  .dropzone-icon { font-size: 2.5rem; margin-bottom: 10px; }
  .dropzone p { color: var(--text-muted); font-size: 0.88rem; }
  .dropzone strong { color: var(--accent); }
  .preview-img {
    width: 100%; border-radius: 10px; object-fit: cover;
    max-height: 200px; display: none; margin-bottom: 12px;
  }
  .ai-analyzing {
    background: rgba(6,214,160,0.08); border: 1px solid rgba(6,214,160,0.2);
    border-radius: 10px; padding: 12px 16px;
    font-size: 0.85rem; color: var(--accent3);
    display: none; margin-bottom: 12px;
  }

  /* Form fields */
  .field { margin-bottom: 16px; }
  .field label { display: block; font-size: 0.82rem; color: var(--text-muted); margin-bottom: 6px; font-weight: 500; }
  .field input, .field select, .field textarea {
    width: 100%; padding: 11px 14px;
    background: var(--surface2); border: 1px solid var(--border);
    border-radius: 10px; color: var(--text); font-size: 0.9rem;
    font-family: 'DM Sans', sans-serif;
    transition: border-color 0.2s; outline: none;
    resize: vertical;
  }
  .field input:focus, .field select:focus, .field textarea:focus { border-color: var(--accent); }
  .field select option { background: var(--surface2); }

  .btn-primary {
    width: 100%; padding: 14px;
    background: linear-gradient(135deg, var(--accent), #ff8c5a);
    border: none; border-radius: 12px; color: white;
    font-family: 'Syne', sans-serif; font-size: 0.95rem; font-weight: 700;
    cursor: pointer; transition: all 0.2s;
    box-shadow: 0 4px 16px rgba(255,107,53,0.3);
    margin-top: 8px;
  }
  .btn-primary:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(255,107,53,0.4); }
  .btn-primary:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

  /* ── Modal ── */
  .modal-overlay {
    position: fixed; inset: 0; z-index: 100;
    background: rgba(0,0,0,0.7); backdrop-filter: blur(4px);
    display: flex; align-items: center; justify-content: center;
    padding: 20px; opacity: 0; pointer-events: none;
    transition: opacity 0.2s;
  }
  .modal-overlay.open { opacity: 1; pointer-events: all; }
  .modal {
    background: var(--surface); border-radius: 20px;
    border: 1px solid var(--border); padding: 0;
    max-width: 560px; width: 100%;
    box-shadow: 0 24px 80px rgba(0,0,0,0.6);
    overflow: hidden;
    transform: scale(0.95); transition: transform 0.2s;
  }
  .modal-overlay.open .modal { transform: scale(1); }
  .modal-img { width: 100%; max-height: 280px; object-fit: cover; }
  .modal-body { padding: 24px; }
  .modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
  .modal-title { font-family: 'Syne', sans-serif; font-weight: 800; font-size: 1.3rem; }
  .modal-close { background: var(--surface2); border: none; color: var(--text-muted); width: 32px; height: 32px; border-radius: 8px; cursor: pointer; font-size: 1rem; }
  .modal-close:hover { color: var(--text); }
  .detail-row { display: flex; gap: 12px; margin-bottom: 12px; font-size: 0.9rem; }
  .detail-label { color: var(--text-muted); min-width: 120px; font-size: 0.82rem; }
  .detail-value { color: var(--text); line-height: 1.5; }
  .ai-desc-box {
    background: rgba(6,214,160,0.07); border: 1px solid rgba(6,214,160,0.2);
    border-radius: 10px; padding: 14px; margin: 16px 0;
  }
  .ai-desc-box .ai-label {
    font-size: 0.75rem; font-weight: 600; color: var(--accent3);
    text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 6px;
    display: flex; align-items: center; gap: 6px;
  }
  .ai-desc-box p { font-size: 0.88rem; color: var(--text-muted); line-height: 1.6; }
  .btn-claim {
    width: 100%; padding: 13px;
    background: linear-gradient(135deg, var(--accent3), #05b88a);
    border: none; border-radius: 12px; color: white;
    font-family: 'Syne', sans-serif; font-size: 0.9rem; font-weight: 700;
    cursor: pointer; transition: all 0.2s; margin-top: 8px;
  }
  .btn-claim:hover { transform: translateY(-1px); }
  .btn-unclaim {
    width: 100%; padding: 13px;
    background: transparent;
    border: 1px solid var(--border); border-radius: 12px; color: var(--text-muted);
    font-family: 'Syne', sans-serif; font-size: 0.9rem; font-weight: 600;
    cursor: pointer; transition: all 0.2s; margin-top: 8px;
  }

  /* ── Toast ── */
  .toast {
    position: fixed; bottom: 28px; right: 28px; z-index: 200;
    background: var(--surface); border: 1px solid var(--border);
    border-radius: 12px; padding: 14px 20px;
    font-size: 0.9rem; box-shadow: var(--shadow);
    transform: translateY(80px); opacity: 0;
    transition: all 0.3s; pointer-events: none;
  }
  .toast.show { transform: none; opacity: 1; }
  .toast.success { border-left: 3px solid var(--accent3); }
  .toast.error { border-left: 3px solid #ef4565; }

  /* ── Image mode toggle ── */
  .img-mode-toggle {
    display: flex; gap: 6px; margin-bottom: 14px;
    background: var(--surface2); border-radius: 10px; padding: 4px;
  }
  .img-mode-btn {
    flex: 1; padding: 9px 12px; border-radius: 8px; border: none;
    font-family: 'Syne', sans-serif; font-size: 0.82rem; font-weight: 600;
    cursor: pointer; transition: all 0.2s; color: var(--text-muted);
    background: transparent;
  }
  .img-mode-btn.active { background: var(--surface); color: var(--text); box-shadow: 0 2px 8px rgba(0,0,0,0.3); }

  /* ── Camera ── */
  .camera-container {
    position: relative; border-radius: 12px; overflow: hidden;
    background: #000; margin-bottom: 12px;
    aspect-ratio: 4/3;
  }
  #cameraFeed {
    width: 100%; height: 100%; object-fit: cover; display: block;
  }
  .camera-overlay {
    position: absolute; inset: 0; pointer-events: none;
    display: flex; align-items: center; justify-content: center;
  }
  .camera-frame {
    width: 70%; height: 70%;
    border: 2px solid rgba(255,255,255,0.5);
    border-radius: 12px;
    box-shadow: 0 0 0 9999px rgba(0,0,0,0.25);
  }
  .camera-controls {
    position: absolute; bottom: 16px; left: 0; right: 0;
    display: flex; align-items: center; justify-content: center; gap: 24px;
  }
  .btn-shutter {
    width: 64px; height: 64px; border-radius: 50%;
    background: white; border: 4px solid rgba(255,255,255,0.5);
    cursor: pointer; transition: transform 0.1s;
    box-shadow: 0 4px 16px rgba(0,0,0,0.5);
  }
  .btn-shutter:hover { transform: scale(1.05); }
  .btn-shutter:active { transform: scale(0.94); background: #ddd; }
  .btn-flip {
    width: 44px; height: 44px; border-radius: 50%;
    background: rgba(0,0,0,0.5); border: 1px solid rgba(255,255,255,0.25);
    color: white; font-size: 1.1rem; cursor: pointer;
    display: flex; align-items: center; justify-content: center;
    transition: background 0.2s;
  }
  .btn-flip:hover { background: rgba(0,0,0,0.75); }
  .camera-error {
    padding: 40px 20px; text-align: center; color: var(--text-muted);
    font-size: 0.88rem; background: var(--surface2); border-radius: 12px;
    margin-bottom: 12px; border: 1px dashed var(--border);
  }
  .camera-error .err-icon { font-size: 2rem; margin-bottom: 10px; }
  .btn-retake {
    width: 100%; padding: 10px; border-radius: 10px;
    background: var(--surface2); border: 1px solid var(--border);
    color: var(--text-muted); font-family: 'Syne', sans-serif;
    font-size: 0.85rem; font-weight: 600; cursor: pointer;
    transition: all 0.2s; margin-bottom: 12px;
  }
  .btn-retake:hover { border-color: var(--accent); color: var(--accent); }

  /* ── Loading spinner ── */
  .spinner {
    display: inline-block; width: 16px; height: 16px;
    border: 2px solid rgba(255,255,255,0.3);
    border-top-color: white; border-radius: 50%;
    animation: spin 0.8s linear infinite; margin-right: 8px;
  }
  @keyframes spin { to { transform: rotate(360deg); } }

  /* ── Empty state ── */
  .empty-state {
    text-align: center; padding: 60px 20px;
    color: var(--text-muted); grid-column: 1 / -1;
  }
  .empty-state .empty-icon { font-size: 3rem; margin-bottom: 16px; }
  .empty-state h4 { font-family: 'Syne', sans-serif; margin-bottom: 8px; color: var(--text); }

  /* ── Stats bar ── */
  .stats-bar {
    display: flex; gap: 16px; margin-bottom: 32px;
    flex-wrap: wrap;
  }
  .stat {
    background: var(--surface); border-radius: 12px;
    padding: 16px 20px; border: 1px solid var(--border);
    flex: 1; min-width: 120px;
  }
  .stat-num {
    font-family: 'Syne', sans-serif; font-size: 1.8rem; font-weight: 800;
    line-height: 1;
  }
  .stat-num.orange { color: var(--accent); }
  .stat-num.green { color: var(--accent3); }
  .stat-num.yellow { color: var(--accent2); }
  .stat-label { font-size: 0.78rem; color: var(--text-muted); margin-top: 4px; }
</style>
</head>
<body>
<div class="app">
  <!-- Header -->
  <header>
    <div class="logo">🎒</div>
    <div>
      <h1>Lost & Found <span>AI</span></h1>
      <p>Powered by Claude AI · School Edition</p>
    </div>
    <div class="header-badge">🤖 AI Enabled</div>
  </header>

  <!-- Tabs -->
  <div class="tabs">
    <button class="tab active" onclick="switchTab('search')">🔍 Search Items</button>
    <button class="tab" onclick="switchTab('browse')">📦 Browse All</button>
    <button class="tab" onclick="switchTab('upload')">➕ Add Found Item</button>
  </div>

  <!-- SEARCH PANEL -->
  <div id="panel-search" class="panel active">
    <div class="search-hero">
      <h2>Looking for something?</h2>
      <p>Describe the lost item in plain English — our AI will find it.</p>
      <div class="search-bar">
        <input type="text" id="searchInput" placeholder="e.g. red water bottle with Ankit written on it..."
          onkeydown="if(event.key==='Enter') doSearch()"/>
        <button class="btn-search" id="searchBtn" onclick="doSearch()">Search</button>
      </div>
      <div class="search-hints">
        <span style="font-size:0.78rem;color:var(--text-muted);align-self:center">Try:</span>
        <span class="hint-chip" onclick="fillSearch(this)">blue water bottle</span>
        <span class="hint-chip" onclick="fillSearch(this)">black Nike bag</span>
        <span class="hint-chip" onclick="fillSearch(this)">glasses near canteen</span>
        <span class="hint-chip" onclick="fillSearch(this)">calculator with name sticker</span>
        <span class="hint-chip" onclick="fillSearch(this)">red umbrella</span>
      </div>
    </div>

    <div class="ai-response" id="aiResponse">
      <div class="ai-icon">🤖</div>
      <p id="aiResponseText"></p>
    </div>

    <div class="section-header">
      <h3 id="searchResultsTitle">Results</h3>
      <span class="count-badge" id="searchCount">0 items</span>
    </div>
    <div class="items-grid" id="searchResults">
      <div class="empty-state">
        <div class="empty-icon">🔍</div>
        <h4>Enter a search query above</h4>
        <p>Describe what you lost and our AI will search through all found items.</p>
      </div>
    </div>
  </div>

  <!-- BROWSE PANEL -->
  <div id="panel-browse" class="panel">
    <div class="stats-bar" id="statsBar">
      <div class="stat"><div class="stat-num orange" id="statTotal">0</div><div class="stat-label">Total Items</div></div>
      <div class="stat"><div class="stat-num yellow" id="statUnclaimed">0</div><div class="stat-label">Unclaimed</div></div>
      <div class="stat"><div class="stat-num green" id="statClaimed">0</div><div class="stat-label">Claimed</div></div>
    </div>
    <div class="section-header">
      <h3>All Found Items</h3>
      <span class="count-badge" id="browseCount">Loading...</span>
    </div>
    <div class="items-grid" id="browseGrid">
      <div class="empty-state">
        <div class="empty-icon">📦</div>
        <h4>No items yet</h4>
        <p>Add found items using the "Add Found Item" tab.</p>
      </div>
    </div>
  </div>

  <!-- UPLOAD PANEL -->
  <div id="panel-upload" class="panel">
    <div class="upload-grid">
      <!-- Left: Image -->
      <div class="upload-card">
        <h3><span>📷</span> Photo of Item</h3>

        <!-- Mode toggle -->
        <div class="img-mode-toggle">
          <button class="img-mode-btn active" id="modeUploadBtn" onclick="setImageMode('upload')">&#11014; Upload File</button>
          <button class="img-mode-btn" id="modeCameraBtn" onclick="setImageMode('camera')">&#128247; Take Photo</button>
        </div>

        <!-- Upload mode -->
        <div id="uploadMode">
          <div class="dropzone" id="dropzone"
               ondragover="event.preventDefault();this.classList.add('dragover')"
               ondragleave="this.classList.remove('dragover')"
               ondrop="handleDrop(event)">
            <input type="file" id="imageInput" accept="image/*" onchange="handleImageSelect(event)"/>
            <img class="preview-img" id="previewImg" src="" alt="preview"/>
            <div id="dropzoneContent">
              <div class="dropzone-icon">&#128206;</div>
              <p><strong>Click to upload</strong> or drag &amp; drop</p>
              <p style="margin-top:4px;font-size:0.78rem">JPG, PNG, WebP supported</p>
            </div>
          </div>
        </div>

        <!-- Camera mode -->
        <div id="cameraMode" style="display:none">
          <div class="camera-container" id="cameraContainer">
            <video id="cameraFeed" autoplay playsinline muted></video>
            <canvas id="cameraCanvas" style="display:none"></canvas>
            <div class="camera-overlay">
              <div class="camera-frame"></div>
            </div>
            <div class="camera-controls">
              <button class="btn-flip" id="flipBtn" onclick="flipCamera()" title="Flip camera">&#128260;</button>
              <button class="btn-shutter" id="shutterBtn" onclick="takePhoto()"></button>
              <div style="width:44px"></div>
            </div>
          </div>
          <div id="cameraPreviewWrap" style="display:none; position:relative;">
            <img class="preview-img" id="cameraPreviewImg" src="" alt="captured" style="display:block; margin-bottom:10px;"/>
            <button class="btn-retake" onclick="retakePhoto()">&#8617; Retake</button>
          </div>
        </div>

        <div class="ai-analyzing" id="aiAnalyzing">
          <span class="spinner"></span> Claude AI is analyzing the image...
        </div>
        <div class="field">
          <label>AI Description (auto-filled after upload)</label>
          <textarea id="aiDescField" rows="4" placeholder="Will be filled automatically by AI after you add a photo..." readonly
            style="background:rgba(6,214,160,0.05);border-color:rgba(6,214,160,0.2);color:var(--text-muted);cursor:default"></textarea>
        </div>
      </div>

      <!-- Right: Details -->
      <div class="upload-card">
        <h3><span>📝</span> Item Details</h3>
        <div class="field">
          <label>Category *</label>
          <select id="fCategory">
            <option value="">Select category...</option>
            <option>Water Bottle</option><option>Bag / Backpack</option>
            <option>Clothing</option><option>Shoes</option>
            <option>Electronics</option><option>Stationery</option>
            <option>Spectacles</option><option>Umbrella</option>
            <option>Sports Equipment</option><option>Books / Notebooks</option>
            <option>Lunch Box</option><option>Jewellery</option>
            <option>Keys</option><option>Other</option>
          </select>
        </div>
        <div class="field">
          <label>Color(s)</label>
          <input type="text" id="fColor" placeholder="e.g. Blue and white"/>
        </div>
        <div class="field">
          <label>Where was it found? *</label>
          <input type="text" id="fLocation" placeholder="e.g. Near the canteen, Grade 5 classroom, Library..."/>
        </div>
        <div class="field">
          <label>Description</label>
          <input type="text" id="fDescription" placeholder="e.g. Metal water bottle with dents"/>
        </div>
        <div class="field">
          <label>Identifying Marks (name, grade, stickers…)</label>
          <input type="text" id="fMarks" placeholder="e.g. 'Ankit' written with marker, Grade 6B sticker"/>
        </div>
        <div class="field">
          <label>Date Found</label>
          <input type="date" id="fDate"/>
        </div>
        <button class="btn-primary" id="submitBtn" onclick="submitItem()">
          ➕ Add to Lost & Found
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Modal -->
<div class="modal-overlay" id="modalOverlay" onclick="if(event.target===this)closeModal()">
  <div class="modal" id="modal">
    <img class="modal-img" id="modalImg" src="" style="display:none"/>
    <div class="modal-body">
      <div class="modal-header">
        <div>
          <div class="modal-title" id="modalTitle">Item Details</div>
          <div style="font-size:0.8rem;color:var(--text-muted);margin-top:2px" id="modalDate"></div>
        </div>
        <button class="modal-close" onclick="closeModal()">✕</button>
      </div>
      <div id="modalContent"></div>
    </div>
  </div>
</div>

<!-- Toast -->
<div class="toast" id="toast"></div>

<script>
let allItems = [];
let selectedImage = null;
let selectedMediaType = null;

// ── Tab switching ──────────────────────────────────────────────────
function switchTab(name) {
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
  event.target.classList.add('active');
  document.getElementById('panel-' + name).classList.add('active');
  if (name === 'browse') loadItems();
  if (name === 'upload') { document.getElementById('fDate').valueAsDate = new Date(); }
  if (name !== 'upload') stopCamera();
}

// ── Load / render items ────────────────────────────────────────────
async function loadItems() {
  try {
    const res = await fetch('/api/items');
    allItems = await res.json();
    renderBrowse(allItems);
    updateStats(allItems);
  } catch(e) { console.error(e); }
}

function updateStats(items) {
  document.getElementById('statTotal').textContent = items.length;
  document.getElementById('statUnclaimed').textContent = items.filter(i=>i.status!=='claimed').length;
  document.getElementById('statClaimed').textContent = items.filter(i=>i.status==='claimed').length;
}

function renderBrowse(items) {
  const grid = document.getElementById('browseGrid');
  document.getElementById('browseCount').textContent = items.length + ' item' + (items.length!==1?'s':'');
  if (!items.length) {
    grid.innerHTML = `<div class="empty-state"><div class="empty-icon">📦</div><h4>No items yet</h4><p>Add found items using the "Add Found Item" tab.</p></div>`;
    return;
  }
  grid.innerHTML = items.map(renderCard).join('');
}

function renderCard(item) {
  const emoji = categoryEmoji(item.category);
  const imgHtml = item.imageFilename
    ? `<img class="item-img" src="/images/${item.imageFilename}" alt="item"/>`
    : `<div class="item-img-placeholder">${emoji}</div>`;
  const desc = item.description || item.aiDescription || 'No description';
  const shortDesc = desc.length > 80 ? desc.substring(0,80) + '…' : desc;
  return `
    <div class="item-card ${item.status}" onclick="openModal('${item.id}')">
      ${imgHtml}
      <div class="item-body">
        <div class="item-header">
          <span class="item-id">#${item.id}</span>
          <span class="item-status ${item.status}">${item.status}</span>
        </div>
        <div class="item-desc">${shortDesc}</div>
        <div class="item-tags">
          ${item.category ? `<span class="tag">${item.category}</span>` : ''}
          ${item.color ? `<span class="tag color-tag">🎨 ${item.color}</span>` : ''}
          ${item.locationFound ? `<span class="tag">📍 ${item.locationFound}</span>` : ''}
        </div>
      </div>
    </div>`;
}

function categoryEmoji(cat) {
  const map = {
    'Water Bottle':'🍶','Bag / Backpack':'🎒','Clothing':'👕','Shoes':'👟',
    'Electronics':'📱','Stationery':'✏️','Spectacles':'👓','Umbrella':'☂️',
    'Sports Equipment':'⚽','Books / Notebooks':'📚','Lunch Box':'🍱',
    'Jewellery':'💍','Keys':'🔑'
  };
  return (cat && map[cat]) || '📦';
}

// ── Search ──────────────────────────────────────────────────────────
function fillSearch(el) {
  document.getElementById('searchInput').value = el.textContent;
  doSearch();
}

async function doSearch() {
  const query = document.getElementById('searchInput').value.trim();
  if (!query) return;
  const btn = document.getElementById('searchBtn');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span>Searching…';
  document.getElementById('aiResponse').classList.remove('visible');

  try {
    const res = await fetch('/api/search', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({query})
    });
    const data = await res.json();

    document.getElementById('aiResponseText').textContent = data.message || '';
    document.getElementById('aiResponse').classList.add('visible');

    const items = data.items || [];
    document.getElementById('searchResultsTitle').textContent =
      items.length ? `Found ${items.length} match${items.length!==1?'es':''}` : 'No matches found';
    document.getElementById('searchCount').textContent = items.length + ' item' + (items.length!==1?'s':'');

    const grid = document.getElementById('searchResults');
    if (!items.length) {
      grid.innerHTML = `<div class="empty-state"><div class="empty-icon">😔</div><h4>Nothing found</h4><p>Try different words or check back later.</p></div>`;
    } else {
      // Store these items so modal works
      items.forEach(item => { if (!allItems.find(i=>i.id===item.id)) allItems.push(item); });
      grid.innerHTML = items.map(renderCard).join('');
    }
  } catch(e) {
    showToast('Search failed. Is the server running?', 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = 'Search';
  }
}

// ── Image mode toggle ──────────────────────────────────────────────
let cameraStream = null;
let facingMode = 'environment'; // start with rear camera

function setImageMode(mode) {
  document.getElementById('modeUploadBtn').classList.toggle('active', mode === 'upload');
  document.getElementById('modeCameraBtn').classList.toggle('active', mode === 'camera');
  document.getElementById('uploadMode').style.display = mode === 'upload' ? 'block' : 'none';
  document.getElementById('cameraMode').style.display = mode === 'camera' ? 'block' : 'none';

  if (mode === 'camera') {
    startCamera();
  } else {
    stopCamera();
  }
}

async function startCamera() {
  stopCamera(); // clean up any existing stream
  const container = document.getElementById('cameraContainer');
  const previewWrap = document.getElementById('cameraPreviewWrap');
  container.style.display = 'block';
  previewWrap.style.display = 'none';

  try {
    cameraStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode, width: { ideal: 1280 }, height: { ideal: 960 } },
      audio: false
    });
    const feed = document.getElementById('cameraFeed');
    feed.srcObject = cameraStream;
  } catch (err) {
    const container = document.getElementById('cameraContainer');
    container.innerHTML = `
      <div class="camera-error">
        <div class="err-icon">&#128247;</div>
        <p><strong>Camera not available</strong></p>
        <p style="margin-top:6px">${err.name === 'NotAllowedError'
          ? 'Camera permission was denied. Please allow camera access in your browser and try again.'
          : 'Could not access the camera: ' + err.message}</p>
      </div>`;
  }
}

function stopCamera() {
  if (cameraStream) {
    cameraStream.getTracks().forEach(t => t.stop());
    cameraStream = null;
  }
  const feed = document.getElementById('cameraFeed');
  if (feed) feed.srcObject = null;
}

async function flipCamera() {
  facingMode = facingMode === 'environment' ? 'user' : 'environment';
  await startCamera();
}

function takePhoto() {
  const video = document.getElementById('cameraFeed');
  const canvas = document.getElementById('cameraCanvas');

  // Match canvas to video dimensions
  canvas.width = video.videoWidth || 1280;
  canvas.height = video.videoHeight || 960;

  const ctx = canvas.getContext('2d');
  // Mirror if using front camera
  if (facingMode === 'user') {
    ctx.translate(canvas.width, 0);
    ctx.scale(-1, 1);
  }
  ctx.drawImage(video, 0, 0);

  selectedImage = canvas.toDataURL('image/jpeg', 0.92);
  selectedMediaType = 'image/jpeg';

  // Show preview, hide live feed
  document.getElementById('cameraContainer').style.display = 'none';
  const previewWrap = document.getElementById('cameraPreviewWrap');
  document.getElementById('cameraPreviewImg').src = selectedImage;
  previewWrap.style.display = 'block';

  stopCamera();

  // Animate shutter flash
  const flash = document.createElement('div');
  flash.style.cssText = 'position:fixed;inset:0;background:white;opacity:0.7;z-index:9999;pointer-events:none;transition:opacity 0.3s';
  document.body.appendChild(flash);
  setTimeout(() => { flash.style.opacity = '0'; setTimeout(() => flash.remove(), 300); }, 50);
}

function retakePhoto() {
  selectedImage = null; selectedMediaType = null;
  document.getElementById('cameraPreviewWrap').style.display = 'none';
  document.getElementById('cameraContainer').style.display = 'block';
  startCamera();
}

// ── File upload ────────────────────────────────────────────────────
function handleDrop(e) {
  e.preventDefault();
  document.getElementById('dropzone').classList.remove('dragover');
  const file = e.dataTransfer.files[0];
  if (file) processImageFile(file);
}
function handleImageSelect(e) {
  const file = e.target.files[0];
  if (file) processImageFile(file);
}
function processImageFile(file) {
  selectedMediaType = file.type || 'image/jpeg';
  const reader = new FileReader();
  reader.onload = function(e) {
    selectedImage = e.target.result;
    const preview = document.getElementById('previewImg');
    preview.src = selectedImage;
    preview.style.display = 'block';
    document.getElementById('dropzoneContent').style.display = 'none';
  };
  reader.readAsDataURL(file);
}

// ── Submit item ────────────────────────────────────────────────────
async function submitItem() {
  const location = document.getElementById('fLocation').value.trim();
  const category = document.getElementById('fCategory').value;
  if (!location || !category) {
    showToast('Please fill in Category and Location fields.', 'error'); return;
  }

  const btn = document.getElementById('submitBtn');
  btn.disabled = true;

  if (selectedImage) {
    btn.innerHTML = '<span class="spinner"></span>Uploading & analyzing with AI…';
    document.getElementById('aiAnalyzing').style.display = 'block';
  } else {
    btn.innerHTML = '<span class="spinner"></span>Adding item…';
  }

  const payload = {
    locationFound: location,
    description: document.getElementById('fDescription').value.trim(),
    color: document.getElementById('fColor').value.trim(),
    identifyingMarks: document.getElementById('fMarks').value.trim(),
    category,
    dateFound: document.getElementById('fDate').value,
    imageData: selectedImage || '',
    mediaType: selectedMediaType || ''
  };

  try {
    const res = await fetch('/api/upload', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    const item = await res.json();
    document.getElementById('aiAnalyzing').style.display = 'none';

    if (item.aiDescription) {
      document.getElementById('aiDescField').value = item.aiDescription;
    }

    showToast('✅ Item #' + item.id + ' added successfully!', 'success');
    resetForm();
    allItems.unshift(item);

    // Switch to browse after short delay
    setTimeout(() => {
      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab')[1].classList.add('active');
      document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
      document.getElementById('panel-browse').classList.add('active');
      renderBrowse(allItems); updateStats(allItems);
    }, 1500);

  } catch(e) {
    showToast('Failed to add item. Check the server.', 'error');
    document.getElementById('aiAnalyzing').style.display = 'none';
  } finally {
    btn.disabled = false;
    btn.innerHTML = '➕ Add to Lost & Found';
  }
}

function resetForm() {
  ['fCategory','fColor','fLocation','fDescription','fMarks'].forEach(id => {
    document.getElementById(id).value = '';
  });
  document.getElementById('fDate').valueAsDate = new Date();
  document.getElementById('previewImg').style.display = 'none';
  document.getElementById('dropzoneContent').style.display = 'block';
  document.getElementById('aiDescField').value = '';
  document.getElementById('imageInput').value = '';
  document.getElementById('aiAnalyzing').style.display = 'none';
  // Reset camera mode
  stopCamera();
  document.getElementById('cameraPreviewWrap').style.display = 'none';
  const container = document.getElementById('cameraContainer');
  if (container) container.style.display = 'block';
  setImageMode('upload');
  selectedImage = null; selectedMediaType = null;
}

// ── Modal ──────────────────────────────────────────────────────────
function openModal(id) {
  const item = allItems.find(i => i.id === id);
  if (!item) return;

  const modalImg = document.getElementById('modalImg');
  if (item.imageFilename) {
    modalImg.src = '/images/' + item.imageFilename;
    modalImg.style.display = 'block';
  } else {
    modalImg.style.display = 'none';
  }

  document.getElementById('modalTitle').textContent = (item.category || 'Item') + ' #' + item.id;
  document.getElementById('modalDate').textContent = item.dateFound ? '📅 Found: ' + item.dateFound : '';

  const rows = [
    ['Location Found', item.locationFound],
    ['Description', item.description],
    ['Color', item.color],
    ['Identifying Marks', item.identifyingMarks],
    ['Status', item.status ? item.status.charAt(0).toUpperCase() + item.status.slice(1) : ''],
    ['Claimed By', item.claimedBy],
  ].filter(([,v]) => v && v.trim());

  let html = rows.map(([l,v]) => `
    <div class="detail-row">
      <span class="detail-label">${l}</span>
      <span class="detail-value">${v}</span>
    </div>`).join('');

  if (item.aiDescription) {
    html += `<div class="ai-desc-box">
      <div class="ai-label">🤖 AI Analysis</div>
      <p>${item.aiDescription.replace(/\\n/g,'<br/>')}</p>
    </div>`;
  }

  if (item.status !== 'claimed') {
    html += `<button class="btn-claim" onclick="markClaimed('${item.id}')">✅ Mark as Claimed</button>`;
  } else {
    html += `<button class="btn-unclaim" onclick="markUnclaimed('${item.id}')">↩ Mark as Unclaimed</button>`;
  }

  document.getElementById('modalContent').innerHTML = html;
  document.getElementById('modalOverlay').classList.add('open');
}

function closeModal() {
  document.getElementById('modalOverlay').classList.remove('open');
}

async function markClaimed(id) {
  const name = prompt('Who is claiming this item? (Enter student name or class)');
  if (name === null) return;
  try {
    const res = await fetch('/api/items/' + id + '/status', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({status:'claimed', claimedBy: name || 'Unknown'})
    });
    const updated = await res.json();
    const idx = allItems.findIndex(i => i.id === id);
    if (idx >= 0) allItems[idx] = updated;
    closeModal();
    showToast('✅ Item marked as claimed!', 'success');
    renderBrowse(allItems); updateStats(allItems);
  } catch(e) { showToast('Failed to update.', 'error'); }
}

async function markUnclaimed(id) {
  try {
    const res = await fetch('/api/items/' + id + '/status', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({status:'unclaimed', claimedBy: ''})
    });
    const updated = await res.json();
    const idx = allItems.findIndex(i => i.id === id);
    if (idx >= 0) allItems[idx] = updated;
    closeModal();
    showToast('↩ Item marked as unclaimed', 'success');
    renderBrowse(allItems); updateStats(allItems);
  } catch(e) { showToast('Failed to update.', 'error'); }
}

// ── Toast ──────────────────────────────────────────────────────────
function showToast(msg, type='success') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = 'toast ' + type + ' show';
  setTimeout(() => t.classList.remove('show'), 3500);
}

// Initial load
loadItems();
document.getElementById('fDate').valueAsDate = new Date();
</script>
</body>
</html>
""";
    }
}
