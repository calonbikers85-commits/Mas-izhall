module.exports = `
/* =========================================================
   VIEWS RENDERING & APP LIFECYCLE
   ========================================================= */

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('hidden');
    modal.classList.remove('flex');
  }
}

function setCustomerTab(tab) {
  AppState.activeTab = tab;
  document.querySelectorAll('.cust-nav-btn').forEach(b => {
    b.classList.remove('text-emerald-700', 'font-bold');
    b.classList.add('text-gray-400');
  });
  const activeBtn = document.getElementById('cust-nav-' + tab);
  if (activeBtn) {
    activeBtn.classList.remove('text-gray-400');
    activeBtn.classList.add('text-emerald-700', 'font-bold');
  }
  
  document.querySelectorAll('.cust-view-tab').forEach(v => v.classList.add('hidden'));
  const activeView = document.getElementById('cust-view-' + tab);
  if (activeView) activeView.classList.remove('hidden');
  
  if (tab === 'history') {
    renderCustomerOrdersHistory();
  }
}

function setTherapistTab(tab) {
  AppState.activeTab = tab;
  document.querySelectorAll('.th-nav-btn').forEach(b => {
    b.classList.remove('text-emerald-700', 'font-bold');
    b.classList.add('text-gray-400');
  });
  const activeBtn = document.getElementById('th-nav-' + tab);
  if (activeBtn) {
    activeBtn.classList.remove('text-gray-400');
    activeBtn.classList.add('text-emerald-700', 'font-bold');
  }
  
  document.querySelectorAll('.th-view-tab').forEach(v => v.classList.add('hidden'));
  const activeView = document.getElementById('th-view-' + tab);
  if (activeView) activeView.classList.remove('hidden');
  
  if (tab === 'history') {
    renderTherapistHistory();
  }
}

function switchAuthTab(type) {
  document.querySelectorAll('.auth-panel').forEach(p => p.classList.add('hidden'));
  document.querySelectorAll('.auth-tab-btn').forEach(b => {
    b.classList.remove('border-emerald-600', 'text-emerald-700', 'bg-emerald-50');
    b.classList.add('border-transparent', 'text-gray-500');
  });
  
  const activePanel = document.getElementById('auth-panel-' + type);
  const activeBtn = document.getElementById('auth-tab-' + type);
  if (activePanel) activePanel.classList.remove('hidden');
  if (activeBtn) {
    activeBtn.classList.remove('border-transparent', 'text-gray-500');
    activeBtn.classList.add('border-emerald-600', 'text-emerald-700', 'bg-emerald-50');
  }
}

function renderApp() {
  const root = document.getElementById('app-root');
  if (!root) return;
  
  // Update Demo Toolbar badges
  const roleBadge = document.getElementById('demo-current-role-badge');
  if (roleBadge) {
    if (AppState.currentRole === 'customer') {
      roleBadge.innerText = 'Pelanggan: ' + (AppState.currentUser ? AppState.currentUser.name.split(' ')[0] : '');
      roleBadge.className = 'px-2 py-0.5 rounded-full text-[11px] font-bold bg-blue-100 text-blue-800';
    } else if (AppState.currentRole === 'therapist') {
      roleBadge.innerText = 'Terapis: ' + (AppState.currentUser ? AppState.currentUser.name.split(' ')[0] : '');
      roleBadge.className = 'px-2 py-0.5 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-800';
    } else if (AppState.currentRole === 'admin') {
      roleBadge.innerText = 'Administrator';
      roleBadge.className = 'px-2 py-0.5 rounded-full text-[11px] font-bold bg-amber-100 text-amber-800';
    } else {
      roleBadge.innerText = 'Belum Login';
      roleBadge.className = 'px-2 py-0.5 rounded-full text-[11px] font-bold bg-gray-100 text-gray-700';
    }
  }
  
  // Show/Hide Role Views
  const authSection = document.getElementById('view-auth');
  const customerSection = document.getElementById('view-customer');
  const therapistSection = document.getElementById('view-therapist');
  const adminSection = document.getElementById('view-admin');
  
  authSection.classList.add('hidden');
  customerSection.classList.add('hidden');
  therapistSection.classList.add('hidden');
  adminSection.classList.add('hidden');
  
  if (!AppState.currentRole) {
    authSection.classList.remove('hidden');
    switchAuthTab('login-customer');
  } else if (AppState.currentRole === 'customer') {
    customerSection.classList.remove('hidden');
    renderCustomerDashboard();
  } else if (AppState.currentRole === 'therapist') {
    therapistSection.classList.remove('hidden');
    renderTherapistDashboard();
  } else if (AppState.currentRole === 'admin') {
    adminSection.classList.remove('hidden');
    switchAdminTab(AppState.adminTab || 'stats');
  }
}

// Render Customer Dashboard
function renderCustomerDashboard() {
  const cust = AppState.currentUser;
  if (!cust) return;
  
  document.getElementById('cust-header-name').innerText = cust.name;
  document.getElementById('cust-header-address').innerText = cust.address || 'Pekalongan, Jawa Tengah';
  
  // Active Order Banner check
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const activeOrder = orders.find(o => o.customerId === cust.id && ['menunggu', 'diterima', 'menuju_lokasi', 'tiba', 'sesi_dimulai'].includes(o.status));
  
  const banner = document.getElementById('cust-active-order-banner');
  if (activeOrder) {
    banner.classList.remove('hidden');
    document.getElementById('active-order-service-name').innerText = activeOrder.serviceName;
    document.getElementById('active-order-status-tag').innerText = activeOrder.status.replace('_', ' ').toUpperCase();
    banner.onclick = () => openOrderTracking(activeOrder.id);
  } else {
    banner.classList.add('hidden');
  }
  
  // Render Categories & Services
  renderCustomerServices();
  renderCustomerNearbyTherapists();
}

function filterServiceCategory(cat) {
  AppState.selectedCategory = cat;
  document.querySelectorAll('.cat-pill').forEach(p => {
    p.classList.remove('bg-emerald-600', 'text-white', 'shadow-md');
    p.classList.add('bg-white', 'text-gray-700', 'border');
  });
  const activePill = document.getElementById('cat-pill-' + cat.replace(/\\s+/g, '-'));
  if (activePill) {
    activePill.classList.remove('bg-white', 'text-gray-700', 'border');
    activePill.classList.add('bg-emerald-600', 'text-white', 'shadow-md');
  }
  renderCustomerServices();
}

function renderCustomerServices() {
  const services = getDB(STORAGE_KEYS.SERVICES);
  const container = document.getElementById('cust-services-grid');
  if (!container) return;
  
  let filtered = services;
  if (AppState.selectedCategory && AppState.selectedCategory !== 'Semua') {
    filtered = services.filter(s => s.category === AppState.selectedCategory);
  }
  
  container.innerHTML = filtered.map(s => \`
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm hover-lift flex flex-col justify-between">
      <div>
        <div class="flex items-start justify-between">
          <div class="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-700 flex items-center justify-center text-xl shadow-inner mb-3">
            <i class="fa-solid \${s.icon || 'fa-spa'}"></i>
          </div>
          \${s.badge ? \`<span class="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-100 text-amber-800">\${s.badge}</span>\` : ''}
        </div>
        <h3 class="font-bold text-gray-900 text-base leading-tight mb-1">\${escapeHtml(s.name)}</h3>
        <p class="text-xs text-emerald-800 font-medium mb-1.5">\${escapeHtml(s.tagline || '')}</p>
        <p class="text-xs text-gray-500 line-clamp-2 leading-relaxed mb-3">\${escapeHtml(s.description)}</p>
      </div>
      <div>
        <div class="flex items-baseline justify-between pt-2 border-t border-gray-100 mb-3">
          <span class="text-[11px] text-gray-400">Mulai dari</span>
          <span class="font-bold text-base text-emerald-700">\${formatRupiah(s.basePrice)}</span>
        </div>
        <button onclick="openBookingModal('\${s.id}')" class="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 active:scale-[0.98] text-white text-xs font-bold rounded-xl shadow-sm transition-all flex items-center justify-center gap-2">
          <span>Pesan Sekarang</span>
          <i class="fa-solid fa-arrow-right text-[10px]"></i>
        </button>
      </div>
    </div>
  \`).join('');
}

function renderCustomerNearbyTherapists() {
  const container = document.getElementById('cust-nearby-therapists');
  if (!container) return;
  const therapists = getDB(STORAGE_KEYS.THERAPISTS).filter(t => t.status === 'approved' && t.isActive);
  const custLat = AppState.currentUser?.lat || PEKALONGAN_COORDS.lat;
  const custLng = AppState.currentUser?.lng || PEKALONGAN_COORDS.lng;
  
  therapists.sort((a, b) => {
    return calculateDistance(custLat, custLng, a.lat, a.lng) - calculateDistance(custLat, custLng, b.lat, b.lng);
  });
  
  container.innerHTML = therapists.slice(0, 4).map(t => {
    const dist = calculateDistance(custLat, custLng, t.lat, t.lng);
    return \`
      <div class="min-w-[220px] bg-white rounded-2xl p-3 border border-gray-100 shadow-sm flex flex-col justify-between snap-start">
        <div class="flex items-center space-x-3 mb-2">
          <div class="relative">
            <img src="\${escapeHtml(t.photo)}" alt="\${escapeHtml(t.name)}" class="w-12 h-12 rounded-xl object-cover border">
            <span class="absolute -bottom-1 -right-1 w-3.5 h-3.5 rounded-full border-2 border-white \${t.isOnline ? 'bg-emerald-500' : 'bg-gray-400'}"></span>
          </div>
          <div>
            <div class="font-bold text-xs text-gray-900 leading-tight">\${escapeHtml(t.name)}</div>
            <div class="text-[10px] text-amber-600 font-semibold flex items-center gap-1 mt-0.5">
              <i class="fa-solid fa-star text-[9px]"></i> \${t.rating || 5.0} (\${t.reviewCount || 0})
            </div>
            <div class="text-[10px] text-gray-400 mt-0.5"><i class="fa-solid fa-location-dot text-emerald-600 mr-1"></i>\${dist} km</div>
          </div>
        </div>
        <div class="text-[10px] text-gray-500 line-clamp-1 mb-2 bg-gray-50 p-1.5 rounded-lg">
          \${(t.specialties || []).slice(0, 2).join(', ')}
        </div>
        <button onclick="openBookingModal('srv-1')" class="w-full py-1.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 text-[11px] font-bold rounded-lg transition-colors">
          Pilih Terapis
        </button>
      </div>
    \`;
  }).join('');
}

function renderCustomerOrdersHistory() {
  const container = document.getElementById('cust-history-list');
  if (!container) return;
  const cust = AppState.currentUser;
  if (!cust) return;
  
  const orders = getDB(STORAGE_KEYS.ORDERS).filter(o => o.customerId === cust.id);
  
  if (orders.length === 0) {
    container.innerHTML = \`
      <div class="p-8 text-center text-gray-400 text-xs">
        <i class="fa-regular fa-clipboard text-3xl mb-2 block"></i>
        Belum ada riwayat pesanan. Nikmati kenyamanan pijat di rumah dengan memesan terapis sekarang.
      </div>
    \`;
    return;
  }
  
  container.innerHTML = orders.map(o => \`
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm mb-3">
      <div class="flex items-center justify-between pb-2 border-b border-gray-100 mb-2">
        <span class="text-xs font-bold text-gray-500">\${o.id}</span>
        <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold \${
          o.status === 'selesai' ? 'bg-emerald-100 text-emerald-800' :
          o.status === 'menunggu' ? 'bg-amber-100 text-amber-800' : 'bg-blue-100 text-blue-800'
        }">\${o.status.toUpperCase()}</span>
      </div>
      <div class="flex items-center justify-between mb-2">
        <div>
          <h4 class="font-bold text-sm text-gray-900">\${escapeHtml(o.serviceName)}</h4>
          <div class="text-xs text-gray-500">\${o.durationMinutes} Menit • Terapis: \${escapeHtml(o.therapistName)}</div>
        </div>
        <div class="text-right font-bold text-sm text-emerald-700">\${formatRupiah(o.totalPrice)}</div>
      </div>
      <div class="flex items-center justify-between text-xs text-gray-400 pt-2 border-t border-gray-50">
        <span>\${formatDate(o.createdAt)}</span>
        <div class="flex items-center gap-2">
          \${o.status === 'selesai' && !o.hasReviewed ? \`
            <button onclick="openReviewModal('\${o.id}')" class="px-3 py-1 bg-amber-500 hover:bg-amber-600 text-white rounded-lg font-bold text-xs">
              <i class="fa-solid fa-star mr-1 text-[10px]"></i> Beri Ulasan
            </button>
          \` : ''}
          <button onclick="openOrderTracking('\${o.id}')" class="px-3 py-1 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 rounded-lg font-bold text-xs">
            Pelacakan & Detail
          </button>
        </div>
      </div>
    </div>
  \`).join('');
}

// Render Therapist Dashboard
function renderTherapistDashboard() {
  const th = AppState.currentUser;
  if (!th) return;
  
  document.getElementById('th-header-name').innerText = th.name;
  document.getElementById('th-header-rating').innerText = th.rating || 5.0;
  
  // Status switch UI
  const statusToggle = document.getElementById('th-status-toggle');
  const statusLabel = document.getElementById('th-status-label');
  if (th.isOnline) {
    statusToggle.checked = true;
    statusLabel.innerText = 'ONLINE (Siap Menerima Pesanan)';
    statusLabel.className = 'text-xs font-bold text-emerald-700';
  } else {
    statusToggle.checked = false;
    statusLabel.innerText = 'OFFLINE (Istirahat)';
    statusLabel.className = 'text-xs font-bold text-gray-400';
  }
  
  // Pending Approval warning
  const pendingBanner = document.getElementById('th-pending-approval-banner');
  if (th.status === 'pending') {
    pendingBanner.classList.remove('hidden');
  } else {
    pendingBanner.classList.add('hidden');
  }
  
  // Active Job Card
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const activeJob = orders.find(o => o.therapistId === th.id && ['diterima', 'menuju_lokasi', 'tiba', 'sesi_dimulai'].includes(o.status));
  const activeJobCard = document.getElementById('th-active-job-card');
  const noJobCard = document.getElementById('th-no-job-card');
  
  if (activeJob) {
    activeJobCard.classList.remove('hidden');
    noJobCard.classList.add('hidden');
    renderTherapistActiveJob(activeJob);
  } else {
    activeJobCard.classList.add('hidden');
    noJobCard.classList.remove('hidden');
  }
  
  // Check incoming popup
  checkTherapistIncomingOrders();
}

function renderTherapistActiveJob(job) {
  document.getElementById('job-order-id').innerText = job.id;
  document.getElementById('job-customer-name').innerText = job.customerName;
  document.getElementById('job-customer-address').innerText = job.customerAddress;
  document.getElementById('job-customer-phone').innerText = job.customerPhone;
  document.getElementById('job-service-name').innerText = job.serviceName;
  document.getElementById('job-duration').innerText = job.durationMinutes + ' Menit';
  document.getElementById('job-notes').innerText = job.notes || '-';
  document.getElementById('job-total-price').innerText = formatRupiah(job.totalPrice);
  
  // Distance to customer
  const dist = calculateDistance(AppState.currentUser.lat, AppState.currentUser.lng, job.customerLat, job.customerLng);
  document.getElementById('job-distance').innerText = dist + ' km';
  
  // Navigation button
  document.getElementById('job-maps-btn').onclick = () => {
    const url = \`https://www.google.com/maps/dir/?api=1&destination=\${job.customerLat || PEKALONGAN_COORDS.lat},\${job.customerLng || PEKALONGAN_COORDS.lng}\`;
    window.open(url, '_blank');
  };
  
  // Chat and Call
  document.getElementById('job-chat-btn').onclick = () => openChatModal(job.id);
  document.getElementById('job-call-btn').onclick = () => openCallModal(job.customerName, job.customerPhone);
  
  // Extra Fee button
  document.getElementById('job-extra-fee-btn').onclick = () => openProposeExtraFeeModal(job.id);
  
  // Extra Fee status badge
  const extraFeeBadge = document.getElementById('job-extra-fee-status-badge');
  if (job.extraFee) {
    extraFeeBadge.classList.remove('hidden');
    extraFeeBadge.innerText = \`Biaya Tambahan: \${formatRupiah(job.extraFee.amount)} (\${job.extraFee.status.toUpperCase()})\`;
  } else {
    extraFeeBadge.classList.add('hidden');
  }
  
  // Step Progression Button based on current status
  const actionBtnContainer = document.getElementById('job-action-buttons');
  if (job.status === 'diterima') {
    actionBtnContainer.innerHTML = \`
      <button onclick="updateJobStatus('\${job.id}', 'menuju_lokasi')" class="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-sm shadow-md flex items-center justify-center gap-2">
        <i class="fa-solid fa-motorcycle"></i> Berangkat Menuju Lokasi Customer
      </button>
    \`;
  } else if (job.status === 'menuju_lokasi') {
    actionBtnContainer.innerHTML = \`
      <button onclick="updateJobStatus('\${job.id}', 'tiba')" class="w-full py-3 bg-teal-600 hover:bg-teal-700 text-white font-bold rounded-xl text-sm shadow-md flex items-center justify-center gap-2">
        <i class="fa-solid fa-location-dot"></i> Saya Telah Tiba di Lokasi Customer
      </button>
    \`;
  } else if (job.status === 'tiba') {
    actionBtnContainer.innerHTML = \`
      <button onclick="updateJobStatus('\${job.id}', 'sesi_dimulai')" class="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-sm shadow-md flex items-center justify-center gap-2">
        <i class="fa-solid fa-play"></i> Mulai Sesi Pijat Sekarang
      </button>
    \`;
  } else if (job.status === 'sesi_dimulai') {
    actionBtnContainer.innerHTML = \`
      <button onclick="updateJobStatus('\${job.id}', 'selesai')" class="w-full py-3 bg-gray-900 hover:bg-black text-white font-bold rounded-xl text-sm shadow-md flex items-center justify-center gap-2">
        <i class="fa-solid fa-flag-checkered"></i> Selesaikan Sesi Pekerjaan
      </button>
    \`;
  }
}

function renderTherapistHistory() {
  const container = document.getElementById('th-history-list');
  if (!container) return;
  const th = AppState.currentUser;
  if (!th) return;
  
  const orders = getDB(STORAGE_KEYS.ORDERS).filter(o => o.therapistId === th.id && o.status === 'selesai');
  const totalEarned = orders.reduce((sum, o) => sum + (o.totalPrice || 0), 0);
  
  document.getElementById('th-total-earnings').innerText = formatRupiah(totalEarned);
  document.getElementById('th-completed-jobs-count').innerText = orders.length;
  
  if (orders.length === 0) {
    container.innerHTML = \`<div class="p-8 text-center text-gray-400 text-xs">Belum ada riwayat pekerjaan yang selesai.</div>\`;
    return;
  }
  
  container.innerHTML = orders.map(o => \`
    <div class="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm mb-3">
      <div class="flex items-center justify-between pb-2 border-b border-gray-100 mb-2">
        <span class="text-xs font-bold text-gray-500">\${o.id}</span>
        <span class="text-xs font-bold text-emerald-700">\${formatRupiah(o.totalPrice)}</span>
      </div>
      <div class="font-bold text-sm text-gray-900">\${escapeHtml(o.serviceName)}</div>
      <div class="text-xs text-gray-500">Customer: \${escapeHtml(o.customerName)} • \${o.durationMinutes} Menit</div>
      <div class="text-[11px] text-gray-400 mt-2 flex items-center justify-between">
        <span>\${formatDate(o.completedAt || o.createdAt)}</span>
        <span class="text-emerald-600 font-semibold"><i class="fa-solid fa-circle-check mr-1"></i>Selesai</span>
      </div>
    </div>
  \`).join('');
}

// Global Event Listeners & Periodic Sync
window.addEventListener('DOMContentLoaded', () => {
  initDatabase();
  renderApp();
  
  // Storage event listener for multi-tab sync
  window.addEventListener('storage', () => {
    renderApp();
    if (currentTrackingOrderId) {
      const orders = getDB(STORAGE_KEYS.ORDERS);
      const curr = orders.find(o => o.id === currentTrackingOrderId);
      if (curr) renderTrackingDetails(curr);
    }
  });
  
  window.addEventListener('pijatku_data_updated', () => {
    renderApp();
  });
  
  // Periodic background check for therapist incoming orders & countdown timer
  setInterval(() => {
    if (AppState.currentRole === 'therapist') {
      checkTherapistIncomingOrders();
    }
    if (currentTrackingOrderId) {
      const orders = getDB(STORAGE_KEYS.ORDERS);
      const curr = orders.find(o => o.id === currentTrackingOrderId);
      if (curr && curr.status === 'sesi_dimulai') {
        updateSessionTimerUI(curr);
      }
    }
  }, 1500);
});
`;
