import { useMemo, useState } from "react";
import {
  AlertTriangle, ArrowUpRight, Check, CircleHelp, FileCode2, Fingerprint,
  Github, Linkedin, LoaderCircle, LockKeyhole, Menu, Plus, Search, Shield,
  ShieldAlert, ShieldCheck, X
} from "lucide-react";

const DEFAULT_API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function App() {
  const [page, setPage] = useState("Scanner");
  const [findings, setFindings] = useState([]);
  const [repoUrl, setRepoUrl] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [query, setQuery] = useState("");
  const [riskFilter, setRiskFilter] = useState("ALL");
  const [selectedFinding, setSelectedFinding] = useState(null);
  const [mobileNav, setMobileNav] = useState(false);
  const [lastRepo, setLastRepo] = useState("");

  const api = DEFAULT_API.replace(/\/+$/, "");
  const visibleFindings = useMemo(() => findings.filter(f => {
    const text = `${f.secretType} ${f.filePath} ${f.explanation}`.toLowerCase();
    return text.includes(query.toLowerCase()) &&
      (riskFilter === "ALL" || (f.risk || "").toUpperCase() === riskFilter);
  }), [findings, query, riskFilter]);

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
    } catch (e) {
      setError(e.message || "Unable to complete scan.");
    } finally {
      setLoading(false);
    }
  }

  const navItems = [
    { name: "Scanner", icon: Plus },
    { name: "Findings", icon: ShieldAlert },
  ];

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileNav ? "sidebar-open" : ""}`}>
        <div className="brand">
          <div className="brand-mark"><Shield size={21} strokeWidth={2.4} /></div>
          <div><div className="brand-name">secret<span>guard</span></div><div className="brand-caption">REPOSITORY SECURITY</div></div>
          <button className="mobile-close icon-btn" onClick={() => setMobileNav(false)}><X size={18}/></button>
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
          <div className="privacy-card contact-card">
            <div className="privacy-icon"><Linkedin size={16}/></div>
            <strong>Let’s connect</strong>
            <p>Have feedback or questions? Reach out to me on LinkedIn.</p>
            <a className="text-action" href="https://www.linkedin.com/in/rohini-sankari-byra-32252b274/" target="_blank" rel="noopener noreferrer">
              Contact me <ArrowUpRight size={14}/>
            </a>
          </div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <button className="icon-btn menu-btn" onClick={() => setMobileNav(true)}><Menu size={19}/></button>
          <div className="breadcrumb">SecretGuard <span>/</span> <strong>{page}</strong></div>
          <div className="top-actions">
            <div className="connection"><span className="connection-dot"/> API connection</div>
          </div>
        </header>

        <div className="content">
          {error && <div className="alert alert-error"><AlertTriangle size={17}/><span>{error}</span><button onClick={() => setError("")}><X size={16}/></button></div>}
          {notice && <div className="alert alert-success"><Check size={17}/><span>{notice}</span><button onClick={() => setNotice("")}><X size={16}/></button></div>}

          {page === "Scanner" && <NewScan repoUrl={repoUrl} setRepoUrl={setRepoUrl}
            runScan={runScan} loading={loading} api={api} />}
          {page === "Findings" && <Findings findings={visibleFindings} allCount={findings.length}
            query={query} setQuery={setQuery} riskFilter={riskFilter} setRiskFilter={setRiskFilter}
            selected={selectedFinding} setSelected={setSelectedFinding} lastRepo={lastRepo}
            onNew={() => setPage("Scanner")} />}
        </div>
        <footer className="footer"><span>© 2026 SecretGuard</span><span><span className="footer-dot"/> Built for safer repositories</span><span>v1.0.0</span></footer>
      </main>

    </div>
  );
}

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

function EmptyState({icon: Icon,title,text,action,onClick}) {
  return <div className="empty-state"><div className="empty-icon"><Icon size={22}/></div><strong>{title}</strong><p>{text}</p>{action && <button className="btn btn-secondary" onClick={onClick}>{action} <ArrowUpRight size={14}/></button>}</div>;
}
export default App;
