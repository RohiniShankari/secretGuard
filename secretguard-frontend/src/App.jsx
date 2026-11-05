import { useEffect, useMemo, useState } from "react";
import {
  Activity, AlertTriangle, ArrowDownRight, ArrowUpRight, Bell, Check,
  ChevronDown, CircleHelp, Clock3, Code2, FileCode2, Fingerprint,
  Github, History, LayoutDashboard, LoaderCircle, LockKeyhole, Menu,
  Plus, RefreshCw, Search, Settings2, Shield, ShieldAlert, ShieldCheck,
  SlidersHorizontal, Sparkles, Terminal, X
} from "lucide-react";

const DEFAULT_API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function App() {
  const [page, setPage] = useState("Overview");
  const [apiBase, setApiBase] = useState(() => localStorage.getItem("sg-api") || DEFAULT_API);
  const [scans, setScans] = useState([]);
  const [findings, setFindings] = useState([]);
  const [repoUrl, setRepoUrl] = useState("");
  const [loading, setLoading] = useState(false);
  const [loadingScans, setLoadingScans] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [query, setQuery] = useState("");
  const [riskFilter, setRiskFilter] = useState("ALL");
  const [selectedFinding, setSelectedFinding] = useState(null);
  const [showSettings, setShowSettings] = useState(false);
  const [mobileNav, setMobileNav] = useState(false);
  const [lastRepo, setLastRepo] = useState("");

  const api = apiBase.replace(/\/+$/, "");
  const visibleFindings = useMemo(() => findings.filter(f => {
    const text = `${f.secretType} ${f.filePath} ${f.explanation}`.toLowerCase();
    return text.includes(query.toLowerCase()) &&
      (riskFilter === "ALL" || (f.risk || "").toUpperCase() === riskFilter);
  }), [findings, query, riskFilter]);

  const counts = useMemo(() => ({
    total: findings.length,
    critical: findings.filter(f => (f.severity || "").toUpperCase() === "CRITICAL").length,
    high: findings.filter(f => ["HIGH", "CRITICAL"].includes((f.risk || "").toUpperCase())).length,
    medium: findings.filter(f => (f.risk || "").toUpperCase() === "MEDIUM").length
  }), [findings]);

  async function loadScans() {
    setLoadingScans(true);
    try {
      const response = await fetch(`${api}/api/scans`);
      if (!response.ok) throw new Error(`API returned ${response.status}`);
      const data = await response.json();
      setScans(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(`Could not load scan history: ${e.message}. Check that Spring Boot is running and the API URL is correct.`);
    } finally {
      setLoadingScans(false);
    }
  }

  useEffect(() => { loadScans(); }, []);

  async function runScan(event) {
    event.preventDefault();
    setError("");
    setNotice("");
    setFindings([]);
    if (!/^https:\/\/github\.com\/[^/]+\/[^/]+\/?$/.test(repoUrl.trim())) {
      setError("Enter a valid public GitHub repository URL, for example https://github.com/owner/repository");
      return;
    }
    setLoading(true);
    try {
      const response = await fetch(`${api}/api/github-scan`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ repositoryUrl: repoUrl.trim() })
      });
      const body = await response.json().catch(() => null);
      if (!response.ok) {
        throw new Error(body?.errorMessage || body?.message || body?.error || `Scan failed (${response.status})`);
      }
      const result = Array.isArray(body) ? body : (body?.findings || body?.results || []);
      if (!Array.isArray(result)) throw new Error("The API response format was not recognized.");
      setFindings(result);
      setLastRepo(repoUrl.trim());
      setPage("Findings");
      setNotice(`Scan completed. ${result.length} finding${result.length === 1 ? "" : "s"} returned.`);
      loadScans();
    } catch (e) {
      setError(e.message || "Unable to complete scan.");
    } finally {
      setLoading(false);
    }
  }

  function saveApi() {
    localStorage.setItem("sg-api", apiBase.trim());
    setShowSettings(false);
    setNotice("API address saved.");
    loadScans();
  }

  const navItems = [
    { name: "Overview", icon: LayoutDashboard },
    { name: "New scan", icon: Plus },
    { name: "Findings", icon: ShieldAlert },
    { name: "Exposure timeline", icon: History }
  ];

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileNav ? "sidebar-open" : ""}`}>
        <div className="brand">
          <div className="brand-mark"><Shield size={21} strokeWidth={2.4} /></div>
          <div><div className="brand-name">secret<span>guard</span></div><div className="brand-caption">REPOSITORY SECURITY</div></div>
          <button className="mobile-close icon-btn" onClick={() => setMobileNav(false)}><X size={18}/></button>
        </div>
        <div className="workspace-label">WORKSPACE</div>
        <div className="workspace">
          <div className="workspace-avatar">SG</div>
          <div className="workspace-copy"><strong>Personal workspace</strong><small>Local environment</small></div>
          <ChevronDown size={15} className="muted"/>
        </div>
        <div className="nav-label">SECURITY CENTER</div>
        <nav>
          {navItems.map(({name, icon: Icon}) => (
            <button key={name} className={`nav-item ${page === name ? "active" : ""}`}
              onClick={() => { setPage(name); setMobileNav(false); setError(""); }}>
              <Icon size={17}/><span>{name}</span>
              {name === "Findings" && findings.length > 0 && <b className="nav-count">{findings.length}</b>}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="privacy-card">
            <div className="privacy-icon"><LockKeyhole size={16}/></div>
            <strong>Your scans, your control.</strong>
            <p>Secrets are masked in findings. Never share raw credentials.</p>
          </div>
          <button className="nav-item settings-link" onClick={() => setShowSettings(true)}><Settings2 size={17}/>Settings</button>
          <div className="profile">
            <div className="profile-avatar">R</div>
            <div><strong>Developer</strong><small>SecretGuard workspace</small></div>
            <MoreDots/>
          </div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <button className="icon-btn menu-btn" onClick={() => setMobileNav(true)}><Menu size={19}/></button>
          <div className="breadcrumb">Workspace <span>/</span> <strong>{page}</strong></div>
          <div className="top-actions">
            <div className="connection"><span className="connection-dot"/> API connection</div>
            <button className="icon-btn" title="Refresh scan history" onClick={loadScans}><RefreshCw size={17}/></button>
            <button className="icon-btn" title="Settings" onClick={() => setShowSettings(true)}><Settings2 size={17}/></button>
            <div className="top-avatar">R</div>
          </div>
        </header>

        <div className="content">
          {error && <div className="alert alert-error"><AlertTriangle size={17}/><span>{error}</span><button onClick={() => setError("")}><X size={16}/></button></div>}
          {notice && <div className="alert alert-success"><Check size={17}/><span>{notice}</span><button onClick={() => setNotice("")}><X size={16}/></button></div>}

          {page === "Overview" && <Overview scans={scans} findings={findings} counts={counts}
            onNew={() => setPage("New scan")} onRefresh={loadScans} loading={loadingScans}
            onFindings={() => setPage("Findings")} />}
          {page === "New scan" && <NewScan repoUrl={repoUrl} setRepoUrl={setRepoUrl}
            runScan={runScan} loading={loading} api={api} />}
          {page === "Findings" && <Findings findings={visibleFindings} allCount={findings.length}
            query={query} setQuery={setQuery} riskFilter={riskFilter} setRiskFilter={setRiskFilter}
            selected={selectedFinding} setSelected={setSelectedFinding} lastRepo={lastRepo}
            onNew={() => setPage("New scan")} />}
          {page === "Exposure timeline" && <Timeline />}
        </div>
        <footer className="footer"><span>© 2026 SecretGuard</span><span><span className="footer-dot"/> Built for safer repositories</span><span>v1.0.0</span></footer>
      </main>

      {showSettings && <div className="modal-backdrop" onClick={() => setShowSettings(false)}>
        <div className="modal" onClick={e => e.stopPropagation()}>
          <div className="modal-head"><div><span className="eyebrow">CONFIGURATION</span><h2>Connection settings</h2></div><button className="icon-btn" onClick={() => setShowSettings(false)}><X size={18}/></button></div>
          <label className="field-label">Spring Boot API base URL</label>
          <input className="text-input" value={apiBase} onChange={e => setApiBase(e.target.value)} placeholder="http://localhost:8080"/>
          <p className="field-help">The frontend calls <code>/api/github-scan</code> and <code>/api/scans</code> on this server.</p>
          <div className="modal-actions"><button className="btn btn-quiet" onClick={() => setShowSettings(false)}>Cancel</button><button className="btn btn-primary" onClick={saveApi}>Save settings</button></div>
        </div>
      </div>}
    </div>
  );
}

function MoreDots() { return <span className="more-dots">•••</span>; }

function Overview({ scans, findings, counts, onNew, onRefresh, loading, onFindings }) {
  return <>
    <div className="page-heading">
      <div><div className="eyebrow">OVERVIEW</div><h1>Security at a glance<span className="heading-period">.</span></h1>
        <p>Monitor exposed secrets and keep your repositories safer.</p></div>
      <button className="btn btn-primary" onClick={onNew}><Plus size={17}/> New scan</button>
    </div>
    <div className="welcome-strip">
      <div className="welcome-symbol"><ShieldCheck size={23}/></div>
      <div><strong>Stay ahead of exposed credentials</strong><p>Scan a GitHub repository to identify potential secrets, understand the risk, and take action.</p></div>
      <button className="welcome-action" onClick={onNew}>Start a scan <ArrowUpRight size={15}/></button>
      <div className="welcome-orb orb-one"/><div className="welcome-orb orb-two"/>
    </div>
    <div className="section-title-row"><div><h2>Security summary</h2><p>Findings from your latest scan session</p></div>
      <span className="updated-label"><span className="connection-dot"/> Live API data</span></div>
    <div className="stats-grid">
      <StatCard icon={Fingerprint} label="Total findings" value={counts.total} note="Current results" tone="purple"/>
      <StatCard icon={ShieldAlert} label="High risk" value={counts.high} note="Requires attention" tone="red"/>
      <StatCard icon={AlertTriangle} label="Critical severity" value={counts.critical} note="Highest severity" tone="orange"/>
      <StatCard icon={Activity} label="Medium risk" value={counts.medium} note="Review recommended" tone="blue"/>
    </div>
    <div className="dashboard-grid">
      <section className="panel recent-panel">
        <div className="panel-heading"><div><h2>Recent scans</h2><p>Repository scan activity</p></div>
          <button className="text-action" onClick={onRefresh} disabled={loading}>{loading ? <LoaderCircle className="spin" size={15}/> : <RefreshCw size={14}/>} Refresh</button></div>
        {scans.length ? <div className="scan-list">{scans.slice(0,5).map((s,i) => <div className="scan-row" key={s.id || i}>
          <div className="repo-icon"><Github size={17}/></div><div className="scan-info"><strong>{s.repositoryName || s.repositoryUrl || "Repository scan"}</strong><small>{s.sourceType || "GitHub"} · {formatDate(s.startedAt)}</small></div>
          <span className={`status-pill ${(s.status || "").toLowerCase()}`}>{s.status || "Recorded"}</span><ChevronDown size={15} className="muted"/>
        </div>)}</div> : <EmptyState icon={Github} title="No scans yet" text="Your repository scans will appear here once you run your first scan." action="Scan a repository" onClick={onNew}/>}
      </section>
      <section className="panel posture-panel">
        <div className="panel-heading"><div><h2>Risk distribution</h2><p>Based on current findings</p></div><SlidersHorizontal size={16} className="muted"/></div>
        {findings.length ? <div className="risk-content">
          <div className="risk-donut" style={{"--high": counts.high, "--medium": counts.medium, "--low": Math.max(0, findings.length-counts.high-counts.medium), "--total": findings.length}}>
            <div><strong>{findings.length}</strong><small>Findings</small></div>
          </div>
          <div className="risk-legend">
            <RiskLegend color="red" label="High / Critical" value={counts.high}/>
            <RiskLegend color="orange" label="Medium" value={counts.medium}/>
            <RiskLegend color="green" label="Low / Other" value={Math.max(0, findings.length-counts.high-counts.medium)}/>
          </div>
        </div> : <EmptyState icon={Activity} title="No risk data yet" text="Run a scan to see your repository risk distribution." action="Start scanning" onClick={onNew}/>}
      </section>
    </div>
    <section className="bottom-callout"><div className="callout-icon"><Sparkles size={18}/></div><div><strong>Make every finding actionable</strong><p>SecretGuard explains detection signals so developers can investigate with confidence.</p></div><button className="text-action" onClick={onFindings}>View findings <ArrowUpRight size={14}/></button></section>
  </>;
}

function StatCard({icon: Icon, label, value, note, tone}) {
  return <div className="stat-card"><div className={`stat-icon ${tone}`}><Icon size={18}/></div><div className="stat-label">{label}</div><div className="stat-value">{value}</div><div className="stat-note">{note}</div></div>;
}
function RiskLegend({color,label,value}) { return <div className="legend-row"><span className={`legend-dot ${color}`}/><span>{label}</span><strong>{value}</strong></div>; }

function NewScan({repoUrl,setRepoUrl,runScan,loading,api}) {
  return <>
    <div className="page-heading"><div><div className="eyebrow">REPOSITORY SCANNER</div><h1>Start a new scan<span className="heading-period">.</span></h1><p>Find exposed credentials before they become a security incident.</p></div></div>
    <div className="scan-layout">
      <section className="panel scan-form-panel">
        <div className="form-title"><div className="form-title-icon"><Github size={21}/></div><div><h2>Scan a GitHub repository</h2><p>Connect a public repository by URL.</p></div></div>
        <form onSubmit={runScan}>
          <label className="field-label" htmlFor="repo">Repository URL</label>
          <div className="repo-input-wrap"><Github size={18}/><input id="repo" className="repo-input" value={repoUrl} onChange={e => setRepoUrl(e.target.value)} placeholder="https://github.com/owner/repository" /></div>
          <p className="field-help">Example: https://github.com/octocat/Hello-World</p>
          <div className="scan-options"><div className="option-icon"><ShieldCheck size={17}/></div><div><strong>Explainable detection enabled</strong><p>Pattern matching, context analysis, entropy and confidence scoring.</p></div><span className="enabled-tag"><Check size={12}/> ON</span></div>
          <button className="btn btn-primary scan-submit" type="submit" disabled={loading}>
            {loading ? <><LoaderCircle size={17} className="spin"/> Scanning repository…</> : <><Shield size={17}/> Start security scan</>}
          </button>
        </form>
        <div className="scan-footnote"><LockKeyhole size={14}/> Only masked findings are shown in this interface. Do not enter credentials.</div>
      </section>
      <aside className="scan-side">
        <div className="panel what-panel"><div className="eyebrow">WHAT WE CHECK</div><h3>Multiple signals.<br/>Clear explanations.</h3>
          <div className="check-item"><span><Check size={14}/></span><div><strong>Known secret patterns</strong><small>AWS keys, GitHub tokens, private-key headers</small></div></div>
          <div className="check-item"><span><Check size={14}/></span><div><strong>Sensitive context</strong><small>Looks for password, token and key-related context</small></div></div>
          <div className="check-item"><span><Check size={14}/></span><div><strong>Entropy analysis</strong><small>Helps identify high-randomness values</small></div></div>
          <div className="check-item"><span><Check size={14}/></span><div><strong>Risk prioritization</strong><small>Combines severity and confidence heuristics</small></div></div>
        </div>
        <div className="side-note"><CircleHelp size={16}/><p><strong>Before you scan</strong><br/>The backend must be running and reachable at <code>{api}</code>. The repository must be public.</p></div>
      </aside>
    </div>
  </>;
}

function Findings({findings,allCount,query,setQuery,riskFilter,setRiskFilter,selected,setSelected,lastRepo,onNew}) {
  return <>
    <div className="page-heading"><div><div className="eyebrow">DETECTION RESULTS</div><h1>Security findings<span className="heading-period">.</span></h1><p>{lastRepo || "Review detected secrets and prioritize remediation."}</p></div>
      <button className="btn btn-primary" onClick={onNew}><Plus size={16}/> New scan</button></div>
    <div className="findings-toolbar"><div className="search-box"><Search size={16}/><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search findings…"/></div>
      <select className="filter-select" value={riskFilter} onChange={e => setRiskFilter(e.target.value)}><option value="ALL">All risk levels</option><option value="CRITICAL">Critical</option><option value="HIGH">High</option><option value="MEDIUM">Medium</option><option value="LOW">Low</option></select>
      <span className="result-count">{findings.length} of {allCount} findings</span></div>
    <section className="panel findings-panel">
      {findings.length ? <div className="table-wrap"><table><thead><tr><th>SECRET TYPE</th><th>LOCATION</th><th>CONFIDENCE</th><th>SEVERITY</th><th>RISK</th><th></th></tr></thead>
        <tbody>{findings.map((f,i) => <tr key={`${f.filePath}-${f.lineNumber}-${i}`} onClick={() => setSelected(f)}>
          <td><div className="secret-type"><span className="secret-type-icon"><Fingerprint size={15}/></span><strong>{f.secretType || "Unknown secret"}</strong><small>{f.maskedValue || "Masked value unavailable"}</small></div></td>
          <td><div className="file-location"><FileCode2 size={14}/><span>{f.filePath || "Unknown file"}<small>Line {f.lineNumber ?? "—"}</small></span></div></td>
          <td><span className={`confidence ${(f.confidence || "").toLowerCase()}`}>{f.confidence || "—"} {f.confidenceScore != null && <small>{f.confidenceScore}/100</small>}</span></td>
          <td><span className={`severity ${(f.severity || "").toLowerCase()}`}>{f.severity || "—"}</span></td>
          <td><span className={`risk-pill ${(f.risk || "").toLowerCase()}`}>{f.risk || "—"}</span></td>
          <td><button className="row-arrow" onClick={e => {e.stopPropagation();setSelected(f);}}>↗</button></td>
        </tr>)}</tbody></table></div> : <EmptyState icon={ShieldCheck} title={allCount ? "No matching findings" : "No findings to display"} text={allCount ? "Try changing your search or risk filter." : "Run a repository scan and the detected secrets will appear here."} action={allCount ? null : "Start a scan"} onClick={onNew}/>}
    </section>
    {selected && <div className="modal-backdrop" onClick={() => setSelected(null)}><div className="modal finding-modal" onClick={e => e.stopPropagation()}>
      <div className="modal-head"><div><span className="eyebrow">FINDING DETAILS</span><h2>{selected.secretType || "Detected secret"}</h2></div><button className="icon-btn" onClick={() => setSelected(null)}><X size={18}/></button></div>
      <div className="masked-value"><LockKeyhole size={15}/><code>{selected.maskedValue || "Value masked"}</code></div>
      <div className="detail-grid"><Detail label="File" value={selected.filePath}/><Detail label="Line" value={selected.lineNumber}/><Detail label="Confidence" value={`${selected.confidence || "—"}${selected.confidenceScore != null ? ` (${selected.confidenceScore}/100)` : ""}`}/><Detail label="Severity" value={selected.severity}/><Detail label="Risk" value={selected.risk}/></div>
      <div className="explanation-box"><strong>Why was this flagged?</strong><p>{selected.explanation || "No explanation was returned by the API."}</p></div>
      <div className="remediation-box"><strong>Recommended next steps</strong><ol><li>Verify whether the detected value is a real credential.</li><li>If real, revoke or rotate it with the provider.</li><li>Move the replacement into environment variables or a secret manager.</li><li>Review Git history; removing it from the latest file does not erase old commits.</li></ol></div>
      <div className="modal-actions"><button className="btn btn-primary" onClick={() => setSelected(null)}>Close details</button></div>
    </div></div>}
  </>;
}
function Detail({label,value}) { return <div className="detail-cell"><small>{label}</small><strong>{value ?? "—"}</strong></div>; }

function Timeline() {
  return <><div className="page-heading"><div><div className="eyebrow">EXPOSURE HISTORY</div><h1>Exposure timeline<span className="heading-period">.</span></h1><p>Understand when a secret entered a repository and whether it remains exposed.</p></div></div>
    <div className="timeline-placeholder panel"><div className="timeline-art"><History size={27}/></div><span className="eyebrow">BACKEND CONNECTION PENDING</span><h2>Your timeline view is ready for integration.</h2>
      <p>The backend currently identifies <strong>INTRODUCED</strong>, <strong>STILL_PRESENT</strong> and <strong>REMOVED_FROM_CURRENT</strong> events in its service layer. A REST endpoint to return those events is not connected yet, so this screen does not invent timeline data.</p>
      <div className="timeline-steps"><div><span className="step-dot purple-dot"/>INTRODUCED<small>Secret first appears in a commit</small></div><div><span className="step-dot orange-dot"/>STILL PRESENT<small>Secret exists in current snapshot</small></div><div><span className="step-dot green-dot"/>REMOVED FROM CURRENT<small>Secret is absent from current snapshot</small></div></div>
    </div>
  </>;
}

function EmptyState({icon: Icon,title,text,action,onClick}) {
  return <div className="empty-state"><div className="empty-icon"><Icon size={22}/></div><strong>{title}</strong><p>{text}</p>{action && <button className="btn btn-secondary" onClick={onClick}>{action} <ArrowUpRight size={14}/></button>}</div>;
}
function formatDate(value) {
  if (!value) return "Date unavailable";
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? value : d.toLocaleString();
}

export default App;
