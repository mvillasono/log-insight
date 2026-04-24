/* ── Log Insight Dashboard ────────────────────────────────────
   Vanilla JS, sin dependencias externas.
   API base: /log-insight/api
─────────────────────────────────────────────────────────────── */

const API = '/log-insight/api';
const REFRESH_MS = 30_000;

const state = {
  analyses: [],
  filter: 'ALL',
  search: '',
};

// ── API ──────────────────────────────────────────────────────

async function fetchAnalyses() {
  const [analyses, stats] = await Promise.all([
    fetch(`${API}/analyses`).then(r => r.json()),
    fetch(`${API}/stats`).then(r => r.json()),
  ]);
  return { analyses, stats };
}

// ── Render ───────────────────────────────────────────────────

function renderStats(stats) {
  el('cnt-critical').textContent = stats.critical ?? 0;
  el('cnt-high').textContent     = stats.high     ?? 0;
  el('cnt-medium').textContent   = stats.medium   ?? 0;
  el('cnt-low').textContent      = stats.low      ?? 0;
}

function renderList() {
  const list    = el('analyses-list');
  const empty   = el('empty-state');
  const visible = filtered();

  if (visible.length === 0) {
    empty.style.display = 'block';
    // remove previous cards
    [...list.querySelectorAll('.analysis-card')].forEach(c => c.remove());
    return;
  }

  empty.style.display = 'none';
  list.innerHTML = '';
  visible.forEach(a => list.appendChild(makeCard(a)));
}

function filtered() {
  return state.analyses.filter(a => {
    const sevMatch = state.filter === 'ALL' || a.severity === state.filter;
    const q = state.search.toLowerCase();
    const textMatch = !q ||
      (a.rootCause  ?? '').toLowerCase().includes(q) ||
      (a.logger     ?? '').toLowerCase().includes(q) ||
      (a.message    ?? '').toLowerCase().includes(q) ||
      (a.service    ?? '').toLowerCase().includes(q);
    return sevMatch && textMatch;
  });
}

function makeCard(a) {
  const card = document.createElement('div');
  card.className = 'analysis-card';
  card.dataset.sev = a.severity;

  const badgeClass = `badge badge-${a.severity.toLowerCase()}`;

  const httpTag = (a.httpMethod && a.httpPath)
    ? `<span class="card-http-tag"><span class="http-method http-method--${a.httpMethod.toLowerCase()}">${escHtml(a.httpMethod)}</span><span class="http-path">${escHtml(a.httpPath)}</span></span>`
    : '';

  card.innerHTML = `
    <div class="card-sev-bar"></div>
    <div class="card-body">
      <div class="card-top">
        <span class="${badgeClass}">${a.severity}</span>
        ${httpTag}
        <span class="card-logger">${escHtml(a.logger)}</span>
      </div>
      <div class="card-root-cause">${escHtml(a.rootCause)}</div>
      <div class="card-analysis">${escHtml(a.analysis)}</div>
    </div>
    <div class="card-right">
      <span class="card-time">${relativeTime(a.analyzedAt)}</span>
      <span class="card-occurrences">${a.occurrences}×</span>
      <span class="card-arrow">›</span>
    </div>
  `;

  card.addEventListener('click', () => openModal(a));
  return card;
}

// ── Modal ────────────────────────────────────────────────────

function openModal(a) {
  const badgeClass = `badge badge-${a.severity.toLowerCase()}`;
  el('m-severity').className = badgeClass;
  el('m-severity').textContent = a.severity;
  el('m-service').textContent  = a.service;
  el('m-logger').textContent   = a.logger;
  el('m-time').textContent     = formatDate(a.analyzedAt);
  el('m-occurrences').textContent = `${a.occurrences} occurrence${a.occurrences !== 1 ? 's' : ''}`;
  const httpBadge = el('m-http');
  if (a.httpMethod && a.httpPath) {
    httpBadge.innerHTML = `<span class="http-method http-method--${a.httpMethod.toLowerCase()}">${escHtml(a.httpMethod)}</span><span class="http-path">${escHtml(a.httpPath)}</span>`;
    httpBadge.style.display = 'flex';
  } else {
    httpBadge.style.display = 'none';
  }

  el('m-root-cause').textContent  = a.rootCause;
  el('m-analysis').textContent    = a.analysis;
  el('m-log').textContent         = a.message || '—';

  const stackEl = el('m-stack');
  const stackSection = el('stack-section');
  if (a.stackTrace) {
    stackEl.textContent = a.stackTrace;
    stackSection.style.display = 'block';
  } else {
    stackSection.style.display = 'none';
  }

  const suggestEl = el('m-suggestions');
  suggestEl.innerHTML = '';
  (a.suggestions ?? []).forEach(s => {
    const li = document.createElement('li');
    li.textContent = s;
    suggestEl.appendChild(li);
  });

  el('overlay').classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function closeModal() {
  el('overlay').classList.add('hidden');
  document.body.style.overflow = '';
}

// ── Data refresh ─────────────────────────────────────────────

async function refresh() {
  try {
    const { analyses, stats } = await fetchAnalyses();
    state.analyses = analyses;

    // Service name from first analysis
    const svc = analyses[0]?.service ?? '—';
    el('service-name').textContent = svc;

    renderStats(stats);
    renderList();
    el('last-updated').textContent = `Updated ${formatTime(new Date())}`;
  } catch (err) {
    console.warn('[LogInsight] Fetch failed:', err.message);
  }
}

// ── Utilities ────────────────────────────────────────────────

function el(id) { return document.getElementById(id); }

function escHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function relativeTime(isoStr) {
  if (!isoStr) return '—';
  const diff = Date.now() - new Date(isoStr).getTime();
  const s = Math.floor(diff / 1000);
  if (s < 60)   return `${s}s ago`;
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

function formatDate(isoStr) {
  if (!isoStr) return '—';
  return new Date(isoStr).toLocaleString();
}

function formatTime(date) {
  return date.toLocaleTimeString();
}

// ── Event listeners ──────────────────────────────────────────

function setup() {
  // Refresh button
  el('refresh-btn').addEventListener('click', refresh);

  // Close modal
  el('modal-close').addEventListener('click', closeModal);
  el('overlay').addEventListener('click', e => {
    if (e.target === el('overlay')) closeModal();
  });
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeModal();
  });

  // Search
  el('search').addEventListener('input', e => {
    state.search = e.target.value;
    renderList();
  });

  // Severity filter pills
  document.querySelectorAll('.pill').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.pill').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.filter = btn.dataset.filter;
      renderList();
    });
  });

  // Stat cards act as quick filters
  document.querySelectorAll('.stat-card').forEach(card => {
    card.addEventListener('click', () => {
      const sev = card.dataset.sev.toUpperCase();
      const pill = [...document.querySelectorAll('.pill')]
        .find(p => p.dataset.filter === sev);
      if (pill) pill.click();
    });
  });
}

// ── Boot ─────────────────────────────────────────────────────

setup();
refresh();
setInterval(refresh, REFRESH_MS);
