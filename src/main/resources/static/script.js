// ── CONFIG ────────────────────────────────────────────────
const CAT_COLORS = { FOOD:'#34d399', TRANSPORT:'#f59e0b', UTILITIES:'#60a5fa', ENTERTAINMENT:'#a78bfa', OTHER:'#f87171' };
const fmt = n => '₹' + Number(n||0).toLocaleString('en-IN');
const catColor = c => CAT_COLORS[c] || '#666';
const dot = c => `<span style="width:7px;height:7px;background:${c};border-radius:50%;display:inline-block;margin-right:6px"></span>`;

const PERM_MAP = {
  view_records:        {admin:true, analyst:true,  viewer:true},
  view_counts_recent:  {admin:true, analyst:true,  viewer:true},
  view_period_kpis:    {admin:true, analyst:true,  viewer:true},
  create_record:       {admin:true, analyst:false, viewer:false},
  edit_record:         {admin:true, analyst:false, viewer:false},
  delete_record:       {admin:true, analyst:false, viewer:false},
  view_summary:        {admin:true, analyst:true,  viewer:false},
  view_deleted:        {admin:true, analyst:false, viewer:false},
  restore_record:      {admin:true, analyst:false, viewer:false},
  manage_users:        {admin:true, analyst:false, viewer:false},
};

const PERMS = [
  {action:'View Records — GET /api/transactions and /api/transactions/{id}', admin:true, analyst:true, viewer:true},
  {action:'View Counts/Recent — GET /api/transactions/count, /recent', admin:true, analyst:true, viewer:true},
  {action:'View Period KPIs — GET /api/transactions/recordCount,totalIncome,totalExpense,netspend', admin:true, analyst:true, viewer:true},
  {action:'Create Record — POST /api/transactions', admin:true, analyst:false, viewer:false},
  {action:'Edit Record — PUT /api/transactions/{id}', admin:true, analyst:false, viewer:false},
  {action:'Delete Record — DELETE /api/transactions/{id}', admin:true, analyst:false, viewer:false},
  {action:'View Summary — POST /api/transactions/summary', admin:true, analyst:true, viewer:false},
  {action:'View Deleted — GET /api/transactions/deleted', admin:true, analyst:false, viewer:false},
  {action:'Restore Record — PUT /api/transactions/{id}/restore', admin:true, analyst:false, viewer:false},
  {action:'Manage Users — /users/**', admin:true, analyst:false, viewer:false},
];

let records = [], users = [], deletedRecs = [], summaryData = null;
let recEditId = null, userEditId = null;
let currentRole = null;

// 👉 Pagination State
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let totalElements = 0;

// ── AUTH ──────────────────────────────────────────────────
function getToken() {
  const t = localStorage.getItem('token');
  if (!t) { window.location.href = 'login.html'; return null; }
  return t;
}

function safeJwtPayload(token) {
  try {
    const parts = String(token).split('.');
    if (parts.length < 2) return null;
    const b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(atob(b64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
    return JSON.parse(json);
  } catch (e) {
    return null;
  }
}

function initRoleFromToken() {
  const token = localStorage.getItem('token');
  const payload = token ? safeJwtPayload(token) : null;
  currentRole = (payload?.role || null);
}

function applyRoleVisibility() {
  // Role names are stored in JWT as "ADMIN" | "ANALYST" | "VIEWER"
  const role = (currentRole || '').toUpperCase();
  const allow = {
    overview: true,
    analytics: role === 'ADMIN' || role === 'ANALYST',
    users: role === 'ADMIN',
    access: role === 'ADMIN',
    deleted: role === 'ADMIN',
  };

  document.querySelectorAll('.ni[data-section]').forEach(el => {
    const sec = el.getAttribute('data-section');
    el.style.display = allow[sec] ? '' : 'none';
  });

  // If current visible section is not allowed, force back to overview.
  const active = document.querySelector('.ni.active[data-section]');
  const activeSec = active?.getAttribute('data-section');
  if (activeSec && !allow[activeSec]) {
    const first = document.querySelector('.ni[data-section="overview"]');
    if (first) nav(first, 'overview');
  }
}

async function apiFetch(url, opts = {}) {
  const token = getToken(); if (!token) return null;
  const headers = { 'Authorization': `Bearer ${token}` };
  if (opts.body) headers['Content-Type'] = 'application/json';
  
  const res = await fetch(url, { ...opts, headers: { ...headers, ...(opts.headers||{}) } });
  
  // 👉 Auto-logout only when token is invalid/expired
  // 403 can be a valid "role not allowed" response (e.g., Viewer calling /users).
  if (res.status === 401) {
    localStorage.removeItem('token');
    window.location.href = 'login.html';
    return null;
  }
  
  if (!res.ok) { 
    if (res.status === 403) {
      const txt = await res.text().catch(()=>'');
      const msg = (txt && txt.includes('"message"')) ? (() => { try { return JSON.parse(txt).message; } catch { return null; } })() : null;
      toast(msg || 'Access denied — you don’t have permission for this action.', 'error');
      throw new Error('403');
    }
    const txt = await res.text().catch(()=>''); 
    toast('Error: ' + (txt || res.status), 'error'); 
    throw new Error(txt || res.status); 
  }
  
  return res;
}

// ── NAV ───────────────────────────────────────────────────
function nav(el, section) {
  document.querySelectorAll('.ni').forEach(n => n.classList.remove('active'));
  el.classList.add('active');
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  document.getElementById('sec-' + section).classList.add('active');
  const labels = {overview:'Records', analytics:'Analytics', users:'User Management', access:'Access & System', deleted:'Deleted Records'};
  document.getElementById('pg-title').textContent = labels[section] || section;
  if (section === 'deleted') loadDeleted();
}

// ── LOAD ──────────────────────────────────────────────────
async function loadData() {
  await fetchRecordsRemote();
  await loadRecordKpis();

  const role = (currentRole || '').toUpperCase();
  if (role === 'ADMIN') {
    try {
      const r2 = await apiFetch('/users');
      if (r2) users = await r2.json();
    } catch(e) {}
  } else {
    users = [];
  }
  renderUsers(); renderPermMatrix(); updateSystemStats();

  try {
    const rCount = await apiFetch('/api/transactions/count');
    if (rCount) {
      const counts = await rCount.json();
      document.getElementById('ov-count-total').textContent = counts.total;
      document.getElementById('ov-count-income').textContent = counts.income;
      document.getElementById('ov-count-expense').textContent = counts.expense;
    }
  } catch(e) {}

  try {
    const rRecent = await apiFetch('/api/transactions/recent?limit=5');
    if (rRecent) {
      const recent = await rRecent.json();
      document.getElementById('recent-records-list').innerHTML = recent.length === 0 ? '<div class="empty">No recent</div>' :
        recent.map(r => `<div class="stat-row" style="cursor:pointer" onclick="viewRecord(${r.id})">
          <div style="flex:1"><div style="font-size:12px; font-weight:600">${r.category}</div><div style="font-size:10px; color:var(--t3)">${r.date}</div></div>
          <div style="font-weight:600; color:${r.type==='INCOME'?'var(--green)':'var(--red)'}">${fmt(r.amount)}</div>
        </div>`).join('');
    }
  } catch(e) {}
}

async function loadSummary() {
  try {
    const r = await apiFetch('/api/transactions/summary', { method:'POST', body: JSON.stringify({period: 'ALL_TIME'}) });
    if (r) { summaryData = await r.json(); renderSummary(); }
  } catch(e) {}
}

async function loadSummaryRange() {
  const period = document.getElementById('sum-period')?.value || '';
  try {
    const r = await apiFetch('/api/transactions/summary', { method:'POST', body: JSON.stringify({period}) });
    if (r) { summaryData = await r.json(); renderSummary(); toast('Summary loaded for period', 'success'); }
  } catch(e) {}
}

async function loadDeleted() {
  try {
    const r = await apiFetch('/api/transactions/deleted');
    if (r) { deletedRecs = await r.json(); renderDeleted(); }
  } catch(e) {}
}

// ── RENDER: RECORDS ───────────────────────────────────────
function renderRecords(list) {
  list = list || records;
  document.getElementById('records-table').innerHTML = list.length === 0
    ? `<tr><td colspan="6" class="empty">No records found</td></tr>`
    : list.map(r => `<tr style="cursor:pointer" onclick="viewRecord(${r.id})">
        <td style="color:var(--t2)">${r.date}</td>
        <td>${dot(catColor(r.cat))}${r.cat}</td>
        <td><span class="badge ${r.type==='INCOME'?'bg':'br'}">${r.type}</span></td>
        <td style="font-weight:600;color:${r.type==='INCOME'?'var(--green)':'var(--red)'}">${fmt(r.amount)}</td>
        <td style="color:var(--t2)">${(r.description || '').toString().slice(0, 40)}${(r.description || '').length > 40 ? '…' : ''}</td>
        <td><div class="acts">
          <button class="abt ae" onclick="event.stopPropagation(); openEditRecord(${r.id})" title="Edit"><i class="fa-solid fa-pen"></i></button>
          <button class="abt ad" onclick="event.stopPropagation(); deleteRecord(${r.id})"   title="Delete"><i class="fa-solid fa-trash"></i></button>
        </div></td>
      </tr>`).join('');
}

// 👉 Updated pagination renderer
function renderPagination() {
  const wrap = document.getElementById('pagination-wrap');
  if (!wrap) return;

  if (totalElements === 0) {
    wrap.innerHTML = `<span style="color:var(--t3);font-size:12px">0 records</span>`;
    return;
  }

  wrap.innerHTML = `
    <span style="color:var(--t2); font-weight:500;">Page ${currentPage + 1} of ${totalPages} &nbsp;&middot;&nbsp; ${totalElements} total records</span>
    <div style="display:flex; gap:6px;">
      <button class="btn btn-g btn-sm" onclick="changePage(${currentPage - 1})" ${currentPage <= 0 ? 'disabled' : ''}><i class="fa-solid fa-chevron-left"></i> Prev</button>
      <button class="btn btn-g btn-sm" onclick="changePage(${currentPage + 1})" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>Next <i class="fa-solid fa-chevron-right"></i></button>
    </div>
  `;
}

// 👉 Function to handle clicks on Next/Prev
function changePage(p) {
  if (p < 0 || p >= totalPages) return;
  currentPage = p;
  fetchRecordsRemote();
}

// 👉 Called when a filter dropdown changes to reset to page 1
function applyFilters() {
  currentPage = 0; 
  fetchRecordsRemote();
  loadRecordKpis();
}

async function loadRecordKpis() {
  const periodSel = (document.getElementById('fil-period')?.value || '').trim();
  const period = periodSel || 'MONTHLY';
  const params = new URLSearchParams();
  if (period) params.append('period', period);

  try {
    const [rcRes, incRes, expRes, netRes] = await Promise.all([
      apiFetch('/api/transactions/recordCount?' + params.toString()),
      apiFetch('/api/transactions/totalIncome?' + params.toString()),
      apiFetch('/api/transactions/totalExpense?' + params.toString()),
      apiFetch('/api/transactions/netspend?' + params.toString()),
    ]);

    const rc = rcRes ? await rcRes.json() : null;
    const inc = incRes ? await incRes.json() : null;
    const exp = expRes ? await expRes.json() : null;
    const net = netRes ? await netRes.json() : null;

    const setText = (id, txt) => {
      const el = document.getElementById(id);
      if (el) el.textContent = txt;
    };

    setText('kpi-rec-count', rc?.value ?? '—');
    setText('kpi-total-income', fmt(inc?.value ?? 0));
    setText('kpi-total-expense', fmt(exp?.value ?? 0));
    setText('kpi-netspend', fmt(net?.value ?? 0));
  } catch (e) {
    // Keep UI non-blocking; viewer/admin already see other parts.
  }
}

async function fetchRecordsRemote() {
  const type = document.getElementById('fil-type').value;
  const cat  = document.getElementById('fil-cat').value;
  const period = document.getElementById('fil-period').value;
  const from = document.getElementById('fil-from').value;
  const to   = document.getElementById('fil-to').value;
  const sort = (document.getElementById('fil-sort')?.value || 'date,desc').split(',');
  
  const params = new URLSearchParams();
  if (type)   params.append('type', type);
  if (cat)    params.append('category', cat);
  if (period) params.append('period', period);
  if (from)   params.append('startDate', from);
  if (to)     params.append('endDate', to);
  
  // 👉 Added Sort + Pagination params for Spring Boot
  if (sort.length === 2) { params.append('sort', `${sort[0]},${sort[1]}`); }
  params.append('page', currentPage);
  params.append('size', pageSize);
  
  try {
    const res = await apiFetch('/api/transactions?' + params.toString());
    if (res) {
      const pageData = await res.json();
      
      // 👉 Extract data from Spring Boot's Page object
      records = pageData.content || [];
      records.forEach(r => r.cat = r.category);
      
      totalPages = pageData.totalPages || 0;
      totalElements = pageData.totalElements || 0;
      currentPage = pageData.number || 0;
      
      renderRecords(records);
      renderPagination();
    }
  } catch(e) {}
}

function clearFilters() {
  ['fil-type','fil-cat','fil-period','fil-from','fil-to'].forEach(id => document.getElementById(id).value = '');
  document.getElementById('fil-sort').value = 'date,desc';
  applyFilters(); // Apply filter resets back to page 1 automatically
}

async function viewRecord(id) {
  try {
    const res = await apiFetch('/api/transactions/' + id);
    if (res) {
      const r = await res.json();
      document.getElementById('vm-amount').textContent = fmt(r.amount);
      document.getElementById('vm-date').textContent = r.date;
      document.getElementById('vm-type').innerHTML = `<span class="badge ${r.type==='INCOME'?'bg':'br'}">${r.type}</span>`;
      document.getElementById('vm-cat').innerHTML = `${dot(catColor(r.category))}${r.category}`;
      document.getElementById('vm-desc').textContent = (r.description && String(r.description).trim()) ? r.description : '—';
      openModal('view-modal');
    }
  } catch(e) {}
}

function openRecordModal() {
  recEditId = null;
  document.getElementById('rec-modal-title').textContent = 'Add Financial Record';
  document.getElementById('rm-amount').value = '';
  document.getElementById('rm-date').value = new Date().toISOString().slice(0,10);
  document.getElementById('rm-desc').value = '';
  openModal('rec-modal');
}
function openEditRecord(id) {
  const r = records.find(x => x.id === id); if (!r) return;
  recEditId = id;
  document.getElementById('rec-modal-title').textContent = 'Edit Record';
  document.getElementById('rm-amount').value = r.amount;
  document.getElementById('rm-date').value   = r.date;
  document.getElementById('rm-type').value   = r.type;
  document.getElementById('rm-cat').value    = r.cat;
  document.getElementById('rm-desc').value   = r.description || '';
  openModal('rec-modal');
}
async function saveRecord() {
  const amount = parseFloat(document.getElementById('rm-amount').value);
  const date   = document.getElementById('rm-date').value;
  const type   = document.getElementById('rm-type').value;
  const cat    = document.getElementById('rm-cat').value;
  const description = (document.getElementById('rm-desc').value || '').trim();
  if (!amount || amount <= 0) return toast('Amount must be positive', 'error');
  if (!date) return toast('Date is required', 'error');
  if (!description) return toast('Description is required', 'error');
  const body = JSON.stringify({ amount, date, type, category: cat, description, deleted: false });
  try {
    if (recEditId) { await apiFetch('/api/transactions/' + recEditId, { method:'PUT', body }); toast('Record updated', 'success'); }
    else           { await apiFetch('/api/transactions',              { method:'POST', body }); toast('Record created', 'success'); }
    closeModal('rec-modal'); 
    applyFilters(); // Refresh and jump back to page 1 to see the new record
  } catch(e) {}
}
async function deleteRecord(id) {
  if (!confirm('Soft-delete this record?')) return;
  try { 
    await apiFetch('/api/transactions/' + id, { method:'DELETE' }); 
    toast('Record deleted', 'success'); 
    fetchRecordsRemote(); // Stay on current page when deleting
  } catch(e) {}
}


// ── RENDER: ANALYTICS (summary endpoint) ─────────────────
function renderSummary() {
  if (!summaryData) return;
  const s = summaryData;
  animNum(document.getElementById('kpi-income'),  s.totalIncome,  true);
  animNum(document.getElementById('kpi-expense'), s.totalExpense, true);
  animNum(document.getElementById('kpi-balance'), s.netBalance,   true);
  document.getElementById('kpi-count').textContent   = s.recordCount || 0;
  document.getElementById('kpi-inc-sub').textContent = `${s.incomeCount || 0} records`;
  document.getElementById('kpi-exp-sub').textContent = `${s.expenseCount || 0} records`;
  document.getElementById('kpi-bal-sub').textContent = s.netBalance >= 0 ? 'Surplus' : 'Deficit';
  document.getElementById('kpi-cnt-sub').textContent = 'in selected range';
  document.getElementById('avg-inc').textContent   = fmt(s.averageIncome  || 0);
  document.getElementById('avg-exp').textContent   = fmt(s.averageExpense || 0);
  document.getElementById('inc-count').textContent = s.incomeCount  || 0;
  document.getElementById('exp-count').textContent = s.expenseCount || 0;
  document.getElementById('rec-count2').textContent= s.recordCount  || 0;

  // Category totals from summaryData.categoryTotals map
  const cats = s.categoryTotals || {};
  const max  = Math.max(...Object.values(cats), 1);
  document.getElementById('cat-breakdown').innerHTML = Object.entries(cats)
    .filter(([,v]) => v > 0)
    .sort((a,b) => b[1] - a[1])
    .map(([cat, amt]) => `
      <div class="stat-row"><span style="color:var(--t2)">${dot(catColor(cat))}${cat}</span><span style="font-weight:600">${fmt(amt)}</span></div>
      <div class="prog" style="margin:-4px 0 6px"><div class="prog-f" style="width:${Math.round(amt/max*100)}%;background:${catColor(cat)}"></div></div>`)
    .join('') || '<div class="empty">No data in range</div>';
}

function animNum(el, target, currency) {
  let c = 0; const step = (target||0) / 40;
  const t = setInterval(() => { c = Math.min(c+step, target||0); el.textContent = currency ? fmt(Math.floor(c)) : Math.floor(c); if(c>=(target||0)) clearInterval(t); }, 16);
}

// ── RENDER: USERS ─────────────────────────────────────────
function renderUsers(list) {
  list = list || users;
  document.getElementById('users-table').innerHTML = list.map(u => {
    const role = (u.role?.name || u.role || 'VIEWER').toUpperCase();
    const created = u.createdAt ? new Date(u.createdAt).toLocaleDateString() : 'N/A';
    const rclass  = {ADMIN:'br', ANALYST:'bb', VIEWER:'bx'}[role] || 'bx';
    return `<tr>
      <td><strong>${u.name||'—'}</strong></td>
      <td style="color:var(--t2)">${u.email}</td>
      <td><span class="badge ${rclass}">${role}</span></td>
      <td><span class="badge ${u.isActive ? 'bg' : 'bx'}">${u.isActive ? 'Active' : 'Inactive'}</span></td>
      <td style="color:var(--t3)">${created}</td>
      <td><div class="acts">
        <button class="abt ae" onclick="openEditUser(${u.id})" title="Edit"><i class="fa-solid fa-pen"></i></button>
        <button class="abt at" onclick="toggleUser(${u.id})"  title="Toggle"><i class="fa-solid fa-power-off"></i></button>
        <button class="abt ad" onclick="deleteUser(${u.id})"  title="Delete"><i class="fa-solid fa-trash"></i></button>
      </div></td>
    </tr>`;
  }).join('');
  document.getElementById('user-count').textContent = `${list.length} of ${users.length} users`;
  document.getElementById('user-pill').textContent  = users.length;
}

function filterUsers(q) {
  const role   = document.getElementById('fil-role').value;
  const status = document.getElementById('fil-status').value;
  q = q || document.querySelector('#sec-users .finput[type=text]').value.toLowerCase();
  renderUsers(users.filter(u => {
    const r = (u.role?.name || u.role || '').toUpperCase();
    return (!q      || u.name?.toLowerCase().includes(q) || u.email?.toLowerCase().includes(q))
        && (!role   || r === role)
        && (!status || String(u.isActive) === status);
  }));
}

function openUserModal() {
  userEditId = null;
  document.getElementById('user-modal-title').textContent = 'Add User';
  ['um-name','um-email'].forEach(id => document.getElementById(id).value = '');
  openModal('user-modal');
}
function openEditUser(id) {
  const u = users.find(x => x.id === id); if (!u) return;
  userEditId = id;
  document.getElementById('user-modal-title').textContent = 'Edit User';
  document.getElementById('um-name').value   = u.name;
  document.getElementById('um-email').value  = u.email;
  document.getElementById('um-role').value   = (u.role?.name || u.role || 'VIEWER').toUpperCase();
  document.getElementById('um-status').value = String(u.isActive);
  openModal('user-modal');
}
async function saveUser() {
  const name   = document.getElementById('um-name').value.trim();
  const email  = document.getElementById('um-email').value.trim();
  const role   = document.getElementById('um-role').value;
  const active = document.getElementById('um-status').value === 'true';
  if (!name)  return toast('Name is required', 'error');
  if (!email || !email.includes('@')) return toast('Valid email required', 'error');
  try {
    if (userEditId) {
      // PUT /users/{id} expects a full User-ish payload (role as object, plus isActive)
      const body = JSON.stringify({ name, email, role: { name: role }, password: 'defaultPassword1', isActive: active });
      await apiFetch('/users/' + userEditId, { method: 'PUT', body });
      toast('User updated', 'success');
      closeModal('user-modal');
      await loadData();
      return;
    }

    // POST /users expects CreateUserRequestDTO (role as string)
    const createBody = JSON.stringify({ name, email, role, password: 'defaultPassword1' });
    const res = await apiFetch('/users', { method: 'POST', body: createBody });
    const created = res ? await res.json().catch(() => null) : null;

    // Show temp password if backend returns it
    if (created?.temporaryPassword) {
      toast(`User created. Temp password: ${created.temporaryPassword}`, 'success');
    } else {
      toast('User added', 'success');
    }

    closeModal('user-modal');

    // Backend createUser() always sets active=true; if UI requested inactive, flip it after creation.
    await loadData();
    if (!active) {
      const newUser = users.find(u => (u.email || '').toLowerCase() === email.toLowerCase());
      if (newUser?.id) {
        await apiFetch(`/users/${newUser.id}/status?active=false`, { method: 'PUT' });
        await loadData();
        toast('User set to inactive', 'info');
      }
    }
  } catch(e) {}
}
async function toggleUser(id) {
  const u = users.find(x=>x.id===id); if(!u) return;
  try { await apiFetch(`/users/${id}/status?active=${!u.isActive}`,{method:'PUT'}); await loadData(); toast(`User ${!u.isActive?'activated':'deactivated'}`,'success'); } catch(e) {}
}
async function deleteUser(id) {
  const u = users.find(x=>x.id===id);
  if (!confirm(`Delete "${u?.name||'user'}"?`)) return;
  try { await apiFetch('/users/'+id,{method:'DELETE'}); toast('User deleted','success'); await loadData(); } catch(e) {}
}


// ── RENDER: DELETED ───────────────────────────────────────
function renderDeleted() {
  document.getElementById('deleted-table').innerHTML = deletedRecs.length === 0
    ? `<tr><td colspan="6" class="empty">No soft-deleted records</td></tr>`
    : deletedRecs.map(r => `<tr>
        <td style="color:var(--t2)">${r.date}</td>
        <td>${dot(catColor(r.category))}${r.category}</td>
        <td><span class="badge ${r.type==='INCOME'?'bg':'br'}">${r.type}</span></td>
        <td style="font-weight:600">${fmt(r.amount)}</td>
        <td style="color:var(--t2)">${(r.description || '').toString().slice(0, 40)}${(r.description || '').length > 40 ? '…' : ''}</td>
        <td><button class="abt at" onclick="restoreRecord(${r.id})" title="Restore"><i class="fa-solid fa-rotate-left"></i></button></td>
      </tr>`).join('');
  document.getElementById('del-count').textContent = `${deletedRecs.length} deleted records`;
  document.getElementById('ss-deleted').textContent = deletedRecs.length;
}
async function restoreRecord(id) {
  try { await apiFetch('/api/transactions/'+id+'/restore',{method:'PUT'}); toast('Record restored','success'); await loadDeleted(); await loadData(); } catch(e) {}
}

// ── RENDER: PERMISSIONS ───────────────────────────────────
function renderPermMatrix() {
  document.getElementById('perm-matrix').innerHTML = PERMS.map(p => `<tr>
    <td>${p.action}</td>
    <td>${p.admin   ?'<i class="fa-solid fa-check cy"></i>':'<i class="fa-solid fa-xmark cn"></i>'}</td>
    <td>${p.analyst ?'<i class="fa-solid fa-check cy"></i>':'<i class="fa-solid fa-xmark cn"></i>'}</td>
    <td>${p.viewer  ?'<i class="fa-solid fa-check cy"></i>':'<i class="fa-solid fa-xmark cn"></i>'}</td>
  </tr>`).join('');
}

function testPerm() {
  const role   = document.getElementById('pt-role').value.toLowerCase();
  const action = document.getElementById('pt-action').value;
  const ok     = (PERM_MAP[action]||{})[role] || false;
  const res    = document.getElementById('pt-result');
  res.style.display = 'block';
  res.innerHTML = ok
    ? `<div class="badge bg" style="padding:10px 14px;font-size:13px;width:100%;justify-content:center"><i class="fa-solid fa-circle-check"></i>&nbsp; ALLOWED</div>`
    : `<div class="badge br" style="padding:10px 14px;font-size:13px;width:100%;justify-content:center"><i class="fa-solid fa-ban"></i>&nbsp; DENIED</div>`;
}

// ── SYSTEM STATS ──────────────────────────────────────────
function updateSystemStats() {
  document.getElementById('ss-users').textContent   = users.length;
  document.getElementById('ss-records').textContent = totalElements; // Update to reflect total elements from pagination
}

// ── UTILS ─────────────────────────────────────────────────
function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }
document.querySelectorAll('.overlay').forEach(o => o.addEventListener('click', e => { if(e.target===o) o.classList.remove('open'); }));

const T_ICONS = {success:'fa-circle-check',error:'fa-circle-xmark',info:'fa-circle-info'};
const T_CLR   = {success:'var(--green)',    error:'var(--red)',      info:'var(--blue)'  };
function toast(msg, type='info') {
  const w = document.getElementById('toast-wrap');
  const t = document.createElement('div');
  t.className = `toast t${type[0]}`;
  t.innerHTML = `<i class="fa-solid ${T_ICONS[type]}" style="color:${T_CLR[type]}"></i> ${msg}`;
  w.appendChild(t);
  requestAnimationFrame(() => requestAnimationFrame(() => t.classList.add('show')));
  setTimeout(() => { t.classList.remove('show'); setTimeout(() => t.remove(), 300); }, 3000);
}
function confirmLogout() {
  if (!confirm('Log out?')) return;
  localStorage.clear();
  toast('Logging out…','info');
  setTimeout(() => window.location.href = 'login.html', 1000);
}

// ── INIT ──────────────────────────────────────────────────
initRoleFromToken();
applyRoleVisibility();
loadData();
if (['ADMIN', 'ANALYST'].includes((currentRole || '').toUpperCase())) {
  loadSummary();
}