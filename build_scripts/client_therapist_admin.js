module.exports = `
/* =========================================================
   THERAPIST WORKFLOW & ADMIN CONTROL PANEL MODULE
   ========================================================= */

// Therapist Online / Offline Toggle
function toggleTherapistOnlineStatus() {
  if (AppState.currentRole !== 'therapist' || !AppState.currentUser) return;
  const therapists = getDB(STORAGE_KEYS.THERAPISTS);
  const th = therapists.find(t => t.id === AppState.currentUser.id);
  if (!th) return;
  
  th.isOnline = !th.isOnline;
  AppState.currentUser.isOnline = th.isOnline;
  saveDB(STORAGE_KEYS.THERAPISTS, therapists);
  localStorage.setItem(STORAGE_KEYS.ACTIVE_SESSION, JSON.stringify({ role: 'therapist', user: th }));
  
  showToast(th.isOnline ? 'Status Anda sekarang ONLINE. Siap menerima pesanan.' : 'Status Anda sekarang OFFLINE.', th.isOnline ? 'success' : 'info');
  renderApp();
}

// Therapist Job Status Progression
function updateJobStatus(orderId, nextStatus) {
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === orderId);
  if (!order) return;
  
  order.status = nextStatus;
  const now = new Date().toISOString();
  
  if (nextStatus === 'diterima') {
    order.acceptedAt = now;
    addNotification('customer', order.customerId, 'Pesanan Diterima!', \`Terapis \${order.therapistName} telah menerima pesanan Anda.\`);
    showToast('Pesanan berhasil diterima. Silakan segera menuju lokasi pelanggan.', 'success');
  } else if (nextStatus === 'menuju_lokasi') {
    addNotification('customer', order.customerId, 'Terapis Menuju Lokasi', \`Terapis \${order.therapistName} sedang dalam perjalanan menuju rumah Anda.\`);
    showToast('Status diperbarui: Menuju lokasi pelanggan.', 'info');
  } else if (nextStatus === 'tiba') {
    addNotification('customer', order.customerId, 'Terapis Telah Tiba', \`Terapis \${order.therapistName} telah sampai di depan lokasi Anda.\`);
    showToast('Status diperbarui: Tiba di lokasi.', 'success');
  } else if (nextStatus === 'sesi_dimulai') {
    order.startedAt = now;
    addNotification('customer', order.customerId, 'Sesi Pijat Dimulai', \`Sesi pijat (\${order.durationMinutes} menit) telah resmi dimulai. Selamat menikmati relaksasi.\`);
    showToast('Sesi pijat dimulai! Timer telah berjalan.', 'success');
  } else if (nextStatus === 'selesai') {
    order.completedAt = now;
    addNotification('customer', order.customerId, 'Sesi Pijat Selesai', 'Sesi pijat telah selesai. Mohon luangkan waktu untuk memberikan ulasan dan rating.');
    showToast('Sesi pekerjaan selesai! Terima kasih atas dedikasi Anda.', 'success');
  }
  
  saveDB(STORAGE_KEYS.ORDERS, orders);
  renderApp();
  if (currentTrackingOrderId === orderId) {
    renderTrackingDetails(order);
  }
}

// Therapist Incoming Order Modal
let incomingOrderId = null;
function checkTherapistIncomingOrders() {
  if (AppState.currentRole !== 'therapist' || !AppState.currentUser) return;
  if (!AppState.currentUser.isOnline) return;
  
  const orders = getDB(STORAGE_KEYS.ORDERS);
  // Find order assigned to this therapist or unassigned with status 'menunggu'
  const pendingOrder = orders.find(o => o.status === 'menunggu' && (o.therapistId === AppState.currentUser.id || !o.therapistId));
  
  const modal = document.getElementById('incoming-order-modal');
  if (pendingOrder && (!modal || modal.classList.contains('hidden'))) {
    incomingOrderId = pendingOrder.id;
    openIncomingOrderPopup(pendingOrder);
  }
}

function openIncomingOrderPopup(order) {
  incomingOrderId = order.id;
  const modal = document.getElementById('incoming-order-modal');
  document.getElementById('incoming-customer-name').innerText = order.customerName;
  document.getElementById('incoming-customer-address').innerText = order.customerAddress;
  document.getElementById('incoming-service-name').innerText = order.serviceName;
  document.getElementById('incoming-service-duration').innerText = order.durationMinutes + ' Menit';
  document.getElementById('incoming-service-price').innerText = formatRupiah(order.totalPrice);
  document.getElementById('incoming-notes').innerText = order.notes || 'Tidak ada catatan khusus';
  
  const dist = calculateDistance(order.customerLat, order.customerLng, AppState.currentUser.lat, AppState.currentUser.lng);
  document.getElementById('incoming-distance').innerText = dist + ' km';
  
  modal.classList.remove('hidden');
  modal.classList.add('flex');
  playChime('incoming');
}

function acceptIncomingOrder() {
  if (!incomingOrderId) return;
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === incomingOrderId);
  if (!order) return;
  
  order.therapistId = AppState.currentUser.id;
  order.therapistName = AppState.currentUser.name;
  order.therapistPhone = AppState.currentUser.phone;
  order.status = 'diterima';
  order.acceptedAt = new Date().toISOString();
  
  saveDB(STORAGE_KEYS.ORDERS, orders);
  closeModal('incoming-order-modal');
  
  addNotification('customer', order.customerId, 'Pesanan Diterima', \`Terapis \${order.therapistName} telah menerima pesanan Anda!\`);
  showToast('Pesanan berhasil diterima! Hubungi pelanggan atau mulai perjalanan.', 'success');
  
  renderApp();
}

function rejectIncomingOrder() {
  const reason = prompt('Masukkan alasan penolakan pesanan (misal: jarak terlalu jauh, ada kendala mendesak):', 'Jarak terlalu jauh');
  if (reason === null) return; // cancelled prompt
  
  if (!incomingOrderId) return;
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === incomingOrderId);
  if (!order) return;
  
  // Find next active therapist
  const currentThId = AppState.currentUser.id;
  const therapists = getDB(STORAGE_KEYS.THERAPISTS).filter(t => t.id !== currentThId && t.status === 'approved' && t.isActive && t.isOnline);
  
  if (therapists.length > 0) {
    // Reassign to next closest therapist
    therapists.sort((a, b) => {
      const dA = calculateDistance(order.customerLat, order.customerLng, a.lat, a.lng);
      const dB = calculateDistance(order.customerLat, order.customerLng, b.lat, b.lng);
      return dA - dB;
    });
    const nextTh = therapists[0];
    order.therapistId = nextTh.id;
    order.therapistName = nextTh.name;
    order.therapistPhone = nextTh.phone;
    addNotification('therapist', nextTh.id, 'Pengalihan Pesanan Masuk', \`Pesanan \${order.id} dialihkan ke Anda dari terapis sebelumnya.\`);
    showToast(\`Pesanan ditolak (\${reason}). Sistem otomatis mengalihkan pesanan ke terapis aktif berikutnya (\${nextTh.name}).\`, 'info');
  } else {
    // No other online therapist right now
    order.therapistId = null;
    order.therapistName = 'Mencari Terapis...';
    showToast(\`Pesanan ditolak (\${reason}). Sistem menempatkan pesanan dalam antrian pencarian terapis.\`, 'info');
  }
  
  saveDB(STORAGE_KEYS.ORDERS, orders);
  closeModal('incoming-order-modal');
  incomingOrderId = null;
  renderApp();
}

// Propose Extra Fee (Biaya Tambahan)
function openProposeExtraFeeModal(orderId) {
  const modal = document.getElementById('extra-fee-modal');
  document.getElementById('propose-fee-order-id').value = orderId;
  document.getElementById('propose-fee-amount').value = '';
  document.getElementById('propose-fee-reason').value = '';
  modal.classList.remove('hidden');
  modal.classList.add('flex');
}

function submitProposeExtraFee() {
  const orderId = document.getElementById('propose-fee-order-id').value;
  const amount = parseInt(document.getElementById('propose-fee-amount').value, 10);
  const reason = document.getElementById('propose-fee-reason').value.trim();
  
  if (!amount || amount <= 0 || !reason) {
    showToast('Harap masukkan nominal biaya dan alasan yang jelas.', 'error');
    return;
  }
  
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === orderId);
  if (!order) return;
  
  order.extraFee = {
    amount,
    reason,
    status: 'pending' // pending -> approved / rejected
  };
  
  saveDB(STORAGE_KEYS.ORDERS, orders);
  addNotification('customer', order.customerId, 'Pengajuan Biaya Tambahan', \`Terapis mengajukan biaya tambahan sebesar \${formatRupiah(amount)} untuk: \${reason}. Silakan beri persetujuan.\`);
  
  closeModal('extra-fee-modal');
  showToast('Pengajuan biaya tambahan berhasil dikirim ke pelanggan. Menunggu persetujuan.', 'success');
  renderApp();
}

/* =========================================================
   ADMINISTRATOR DASHBOARD & AUDIT LOG
   ========================================================= */

function switchAdminTab(tabName) {
  AppState.adminTab = tabName;
  document.querySelectorAll('.admin-nav-btn').forEach(btn => {
    btn.classList.remove('bg-emerald-600', 'text-white', 'shadow-md');
    btn.classList.add('text-gray-600', 'hover:bg-gray-100');
  });
  const activeBtn = document.getElementById('admin-btn-' + tabName);
  if (activeBtn) {
    activeBtn.classList.remove('text-gray-600', 'hover:bg-gray-100');
    activeBtn.classList.add('bg-emerald-600', 'text-white', 'shadow-md');
  }
  
  document.querySelectorAll('.admin-section-view').forEach(view => view.classList.add('hidden'));
  const activeView = document.getElementById('admin-section-' + tabName);
  if (activeView) activeView.classList.remove('hidden');
  
  renderAdminContent();
}

function renderAdminContent() {
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const therapists = getDB(STORAGE_KEYS.THERAPISTS);
  const customers = getDB(STORAGE_KEYS.CUSTOMERS);
  const services = getDB(STORAGE_KEYS.SERVICES);
  const reviews = getDB(STORAGE_KEYS.REVIEWS);
  const priceHistory = getDB(STORAGE_KEYS.PRICE_HISTORY);
  
  // 1. Stats calculation
  const completedOrders = orders.filter(o => o.status === 'selesai');
  const activeOrders = orders.filter(o => ['diterima', 'menuju_lokasi', 'tiba', 'sesi_dimulai'].includes(o.status));
  const pendingOrders = orders.filter(o => o.status === 'menunggu');
  const totalRevenue = completedOrders.reduce((sum, o) => sum + (o.totalPrice || 0), 0);
  
  const approvedTherapists = therapists.filter(t => t.status === 'approved');
  const pendingTherapists = therapists.filter(t => t.status === 'pending');
  
  document.getElementById('admin-stat-revenue').innerText = formatRupiah(totalRevenue);
  document.getElementById('admin-stat-orders').innerText = orders.length;
  document.getElementById('admin-stat-completed').innerText = completedOrders.length;
  document.getElementById('admin-stat-active').innerText = activeOrders.length;
  document.getElementById('admin-stat-customers').innerText = customers.length;
  document.getElementById('admin-stat-therapists').innerText = \`\${approvedTherapists.length} (Menunggu: \${pendingTherapists.length})\`;
  
  // Render Pending Therapist Verifications
  const pendingThContainer = document.getElementById('admin-pending-therapists-list');
  if (pendingTherapists.length === 0) {
    pendingThContainer.innerHTML = \`<div class="p-6 text-center text-gray-400 text-sm bg-gray-50 rounded-xl">Tidak ada pendaftaran terapis yang menunggu persetujuan.</div>\`;
  } else {
    pendingThContainer.innerHTML = pendingTherapists.map(t => \`
      <div class="p-4 bg-white rounded-xl border border-amber-200 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div class="flex items-center space-x-3">
          <img src="\${escapeHtml(t.photo)}" alt="\${escapeHtml(t.name)}" class="w-12 h-12 rounded-full object-cover border">
          <div>
            <div class="font-bold text-gray-900">\${escapeHtml(t.name)} <span class="text-xs px-2 py-0.5 rounded bg-amber-100 text-amber-800 ml-1">Menunggu Persetujuan</span></div>
            <div class="text-xs text-gray-500">\${escapeHtml(t.email)} • \${escapeHtml(t.phone)} • Pengalaman: \${t.experienceYears} Thn</div>
            <div class="text-xs text-emerald-700 mt-0.5">Keahlian: \${(t.specialties || []).join(', ')}</div>
            <div class="text-xs text-gray-400 mt-0.5">Alamat: \${escapeHtml(t.address)}</div>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button onclick="approveTherapist('\${t.id}', true)" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-lg transition-colors flex items-center gap-1.5">
            <i class="fa-solid fa-check"></i> Setujui
          </button>
          <button onclick="approveTherapist('\${t.id}', false)" class="px-4 py-2 bg-rose-100 hover:bg-rose-200 text-rose-700 text-xs font-bold rounded-lg transition-colors flex items-center gap-1.5">
            <i class="fa-solid fa-xmark"></i> Tolak
          </button>
        </div>
      </div>
    \`).join('');
  }
  
  // Render Approved Therapists List with Toggle Aktif/Nonaktif
  const approvedThContainer = document.getElementById('admin-approved-therapists-list');
  approvedThContainer.innerHTML = approvedTherapists.map(t => \`
    <div class="p-4 bg-white rounded-xl border border-gray-200 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-3">
      <div class="flex items-center space-x-3">
        <div class="relative">
          <img src="\${escapeHtml(t.photo)}" alt="\${escapeHtml(t.name)}" class="w-12 h-12 rounded-full object-cover border">
          <span class="absolute bottom-0 right-0 w-3.5 h-3.5 rounded-full border-2 border-white \${t.isOnline ? 'bg-emerald-500' : 'bg-gray-400'}"></span>
        </div>
        <div>
          <div class="font-bold text-gray-900 flex items-center gap-2">
            \${escapeHtml(t.name)}
            <span class="text-xs px-2 py-0.5 rounded-full \${t.isActive ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'}">
              \${t.isActive ? 'Aktif' : 'Dinonaktifkan Admin'}
            </span>
          </div>
          <div class="text-xs text-gray-500">\${escapeHtml(t.email)} • \${escapeHtml(t.phone)}</div>
          <div class="text-xs text-amber-600 flex items-center gap-1 mt-0.5">
            <i class="fa-solid fa-star text-[10px]"></i> \${t.rating || 5.0} (\${t.reviewCount || 0} Ulasan) • \${t.isOnline ? 'Online' : 'Offline'}
          </div>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button onclick="toggleTherapistActive('\${t.id}')" class="px-3 py-1.5 rounded-lg text-xs font-bold transition-colors \${t.isActive ? 'bg-rose-50 text-rose-700 hover:bg-rose-100' : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'}">
          \${t.isActive ? 'Nonaktifkan Akun' : 'Aktifkan Akun'}
        </button>
      </div>
    </div>
  \`).join('');
  
  // Render Customer List
  const custListContainer = document.getElementById('admin-customers-list');
  custListContainer.innerHTML = customers.map(c => {
    const custOrders = orders.filter(o => o.customerId === c.id);
    return \`
      <div class="p-4 bg-white rounded-xl border border-gray-200 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div>
          <div class="font-bold text-gray-900">\${escapeHtml(c.name)} <span class="text-xs text-emerald-600 font-normal"><i class="fa-solid fa-circle-check"></i> Terverifikasi</span></div>
          <div class="text-xs text-gray-500">\${escapeHtml(c.email)} • \${escapeHtml(c.phone)}</div>
          <div class="text-xs text-gray-400 mt-0.5"><i class="fa-solid fa-location-dot mr-1"></i>\${escapeHtml(c.address)}</div>
        </div>
        <div class="text-right">
          <span class="text-xs font-semibold px-2.5 py-1 bg-gray-100 rounded-full text-gray-700">\${custOrders.length} Pesanan</span>
        </div>
      </div>
    \`;
  }).join('');
  
  // Render All Orders Table
  renderAdminOrdersTable(orders);
  
  // Render Services CRUD & Price History
  renderAdminServices(services);
  renderAdminPriceHistory(priceHistory);
  renderAdminReviews(reviews);
  renderAdminRecap(orders);
}

function approveTherapist(thId, isApproved) {
  const therapists = getDB(STORAGE_KEYS.THERAPISTS);
  const th = therapists.find(t => t.id === thId);
  if (!th) return;
  
  th.status = isApproved ? 'approved' : 'rejected';
  th.isActive = isApproved;
  saveDB(STORAGE_KEYS.THERAPISTS, therapists);
  
  addNotification('therapist', th.id, isApproved ? 'Akun Anda Telah Disetujui' : 'Permohonan Ditolak', isApproved ? 'Selamat! Akun terapis Anda telah diverifikasi oleh Admin. Anda kini dapat login dan menerima pesanan.' : 'Mohon maaf, pendaftaran akun Anda tidak memenuhi kriteria verifikasi.');
  
  showToast(\`Terapis \${th.name} telah \${isApproved ? 'disetujui' : 'ditolak'}.\`, 'success');
  renderAdminContent();
}

function toggleTherapistActive(thId) {
  const therapists = getDB(STORAGE_KEYS.THERAPISTS);
  const th = therapists.find(t => t.id === thId);
  if (!th) return;
  
  th.isActive = !th.isActive;
  saveDB(STORAGE_KEYS.THERAPISTS, therapists);
  showToast(\`Status akun \${th.name} diubah menjadi: \${th.isActive ? 'Aktif' : 'Nonaktif'}.\`, 'info');
  renderAdminContent();
}

function renderAdminOrdersTable(orders) {
  const container = document.getElementById('admin-orders-table-body');
  if (!container) return;
  
  container.innerHTML = orders.map(o => \`
    <tr class="border-b border-gray-100 hover:bg-gray-50 text-xs">
      <td class="p-3 font-semibold text-gray-800">\${o.id}</td>
      <td class="p-3">
        <div class="font-medium text-gray-900">\${escapeHtml(o.customerName)}</div>
        <div class="text-[11px] text-gray-400">\${escapeHtml(o.customerPhone)}</div>
      </td>
      <td class="p-3">
        <div class="font-medium text-emerald-800">\${escapeHtml(o.therapistName)}</div>
      </td>
      <td class="p-3">
        <div>\${escapeHtml(o.serviceName)}</div>
        <div class="text-[11px] text-gray-400">\${o.durationMinutes} Menit</div>
      </td>
      <td class="p-3 font-semibold text-gray-900">\${formatRupiah(o.totalPrice)}</td>
      <td class="p-3">
        <span class="px-2 py-1 rounded-full text-[10px] font-bold \${
          o.status === 'selesai' ? 'bg-emerald-100 text-emerald-800' :
          o.status === 'sesi_dimulai' ? 'bg-blue-100 text-blue-800' :
          o.status === 'menunggu' ? 'bg-amber-100 text-amber-800' : 'bg-gray-100 text-gray-800'
        }">\${o.status}</span>
      </td>
      <td class="p-3 text-right">
        <button onclick="openOrderTracking('\${o.id}')" class="px-2 py-1 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 rounded font-medium">Lihat</button>
      </td>
    </tr>
  \`).join('');
}

function renderAdminServices(services) {
  const container = document.getElementById('admin-services-list');
  if (!container) return;
  
  container.innerHTML = services.map(s => \`
    <div class="p-4 bg-white rounded-xl border border-gray-200 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-3">
      <div class="flex items-start space-x-3">
        <div class="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold text-lg">
          <i class="fa-solid \${s.icon || 'fa-spa'}"></i>
        </div>
        <div>
          <div class="font-bold text-gray-900">\${escapeHtml(s.name)} <span class="text-xs px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 font-normal">\${s.category}</span></div>
          <div class="text-xs text-gray-500 mt-0.5">\${escapeHtml(s.description)}</div>
          <div class="text-xs font-semibold text-gray-800 mt-1 flex flex-wrap gap-2">
            \${(s.durations || []).map(d => \`<span class="bg-gray-100 px-2 py-0.5 rounded">\${d.minutes} mnt: \${formatRupiah(d.price)}</span>\`).join('')}
          </div>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button onclick="openEditServiceModal('\${s.id}')" class="px-3 py-1.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg text-xs font-bold">
          <i class="fa-solid fa-pen-to-square mr-1"></i> Edit Tarif
        </button>
        <button onclick="deleteService('\${s.id}')" class="px-3 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 rounded-lg text-xs font-bold">
          <i class="fa-solid fa-trash mr-1"></i> Hapus
        </button>
      </div>
    </div>
  \`).join('');
}

function renderAdminPriceHistory(history) {
  const container = document.getElementById('admin-price-history-body');
  if (!container) return;
  
  container.innerHTML = history.map(h => \`
    <tr class="border-b border-gray-100 text-xs">
      <td class="p-3 text-gray-500">\${formatDate(h.timestamp)}</td>
      <td class="p-3 font-semibold text-gray-900">\${escapeHtml(h.serviceName)}</td>
      <td class="p-3 text-gray-500 line-through">\${formatRupiah(h.oldPrice)}</td>
      <td class="p-3 font-bold text-emerald-700">\${formatRupiah(h.newPrice)}</td>
      <td class="p-3 text-gray-600">\${escapeHtml(h.reason || 'Penyesuaian tarif')}</td>
      <td class="p-3 text-gray-400">\${escapeHtml(h.changedBy)}</td>
    </tr>
  \`).join('');
}

function renderAdminReviews(reviews) {
  const container = document.getElementById('admin-reviews-list');
  if (!container) return;
  
  container.innerHTML = reviews.map(r => \`
    <div class="p-4 bg-white rounded-xl border border-gray-200 shadow-sm mb-3">
      <div class="flex items-center justify-between mb-1">
        <div class="font-bold text-sm text-gray-900">\${escapeHtml(r.customerName)} <span class="text-xs text-gray-400 font-normal">mengulas</span> \${escapeHtml(r.therapistName)}</div>
        <div class="text-amber-500 text-xs font-bold flex items-center gap-1">
          <i class="fa-solid fa-star"></i> \${r.rating}.0
        </div>
      </div>
      <div class="text-xs text-emerald-700 mb-1.5 font-medium">Layanan: \${escapeHtml(r.serviceName)}</div>
      <p class="text-xs text-gray-600 italic">"\${escapeHtml(r.comment)}"</p>
      <div class="text-[10px] text-gray-400 mt-2 text-right">\${formatDate(r.createdAt)}</div>
    </div>
  \`).join('');
}

function renderAdminRecap(orders) {
  const completed = orders.filter(o => o.status === 'selesai');
  const now = new Date();
  const todayStr = now.toISOString().split('T')[0];
  
  // Daily recap
  const todayOrders = completed.filter(o => (o.completedAt || o.createdAt || '').startsWith(todayStr));
  const todayIncome = todayOrders.reduce((sum, o) => sum + (o.totalPrice || 0), 0);
  
  // Weekly recap (last 7 days)
  const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
  const weekOrders = completed.filter(o => new Date(o.completedAt || o.createdAt) >= oneWeekAgo);
  const weekIncome = weekOrders.reduce((sum, o) => sum + (o.totalPrice || 0), 0);
  
  // Monthly recap (current month)
  const currentMonthStr = todayStr.slice(0, 7);
  const monthOrders = completed.filter(o => (o.completedAt || o.createdAt || '').startsWith(currentMonthStr));
  const monthIncome = monthOrders.reduce((sum, o) => sum + (o.totalPrice || 0), 0);
  
  document.getElementById('recap-daily-orders').innerText = todayOrders.length + ' Selesai';
  document.getElementById('recap-daily-income').innerText = formatRupiah(todayIncome);
  
  document.getElementById('recap-weekly-orders').innerText = weekOrders.length + ' Selesai';
  document.getElementById('recap-weekly-income').innerText = formatRupiah(weekIncome);
  
  document.getElementById('recap-monthly-orders').innerText = monthOrders.length + ' Selesai';
  document.getElementById('recap-monthly-income').innerText = formatRupiah(monthIncome);
}

// Service CRUD Handlers
function openAddServiceModal() {
  document.getElementById('service-modal-title').innerText = 'Tambah Layanan Baru';
  document.getElementById('edit-service-id').value = '';
  document.getElementById('service-form-name').value = '';
  document.getElementById('service-form-tagline').value = '';
  document.getElementById('service-form-desc').value = '';
  document.getElementById('service-form-category').value = 'Relaksasi';
  document.getElementById('service-form-p60').value = 100000;
  document.getElementById('service-form-p90').value = 140000;
  document.getElementById('service-form-p120').value = 180000;
  
  const modal = document.getElementById('service-modal');
  modal.classList.remove('hidden');
  modal.classList.add('flex');
}

function openEditServiceModal(serviceId) {
  const services = getDB(STORAGE_KEYS.SERVICES);
  const service = services.find(s => s.id === serviceId);
  if (!service) return;
  
  document.getElementById('service-modal-title').innerText = 'Edit Layanan & Tarif';
  document.getElementById('edit-service-id').value = service.id;
  document.getElementById('service-form-name').value = service.name;
  document.getElementById('service-form-tagline').value = service.tagline || '';
  document.getElementById('service-form-desc').value = service.description || '';
  document.getElementById('service-form-category').value = service.category || 'Relaksasi';
  
  const d60 = service.durations.find(d => d.minutes === 60);
  const d90 = service.durations.find(d => d.minutes === 90);
  const d120 = service.durations.find(d => d.minutes === 120);
  
  document.getElementById('service-form-p60').value = d60 ? d60.price : service.basePrice;
  document.getElementById('service-form-p90').value = d90 ? d90.price : (service.basePrice * 1.4);
  document.getElementById('service-form-p120').value = d120 ? d120.price : (service.basePrice * 1.8);
  
  const modal = document.getElementById('service-modal');
  modal.classList.remove('hidden');
  modal.classList.add('flex');
}

function saveServiceModal(event) {
  event.preventDefault();
  const id = document.getElementById('edit-service-id').value;
  const name = document.getElementById('service-form-name').value.trim();
  const tagline = document.getElementById('service-form-tagline').value.trim();
  const description = document.getElementById('service-form-desc').value.trim();
  const category = document.getElementById('service-form-category').value;
  const p60 = parseInt(document.getElementById('service-form-p60').value, 10) || 100000;
  const p90 = parseInt(document.getElementById('service-form-p90').value, 10) || 140000;
  const p120 = parseInt(document.getElementById('service-form-p120').value, 10) || 180000;
  
  const services = getDB(STORAGE_KEYS.SERVICES);
  
  if (id) {
    // Edit existing service
    const existing = services.find(s => s.id === id);
    if (existing) {
      const oldPrice = existing.basePrice;
      existing.name = name;
      existing.tagline = tagline;
      existing.description = description;
      existing.category = category;
      existing.basePrice = p60;
      existing.durations = [
        { minutes: 60, price: p60 },
        { minutes: 90, price: p90 },
        { minutes: 120, price: p120 }
      ];
      
      // If price changed, write to priceHistory audit log
      if (oldPrice !== p60) {
        const history = getDB(STORAGE_KEYS.PRICE_HISTORY);
        history.unshift({
          id: 'ph-' + Date.now(),
          serviceId: existing.id,
          serviceName: existing.name,
          oldPrice,
          newPrice: p60,
          changedBy: AppState.currentUser ? AppState.currentUser.email : 'calonbikers85@gmail.com',
          reason: 'Pembaruan tarif oleh Admin',
          timestamp: new Date().toISOString()
        });
        saveDB(STORAGE_KEYS.PRICE_HISTORY, history);
      }
      showToast('Layanan berhasil diperbarui.', 'success');
    }
  } else {
    // Create new service
    const newService = {
      id: 'srv-' + Date.now(),
      name,
      tagline,
      description,
      category,
      basePrice: p60,
      durations: [
        { minutes: 60, price: p60 },
        { minutes: 90, price: p90 },
        { minutes: 120, price: p120 }
      ],
      icon: 'fa-spa',
      badge: 'Baru'
    };
    services.push(newService);
    showToast('Layanan baru berhasil ditambahkan.', 'success');
  }
  
  saveDB(STORAGE_KEYS.SERVICES, services);
  closeModal('service-modal');
  renderAdminContent();
}

function deleteService(serviceId) {
  if (!confirm('Apakah Anda yakin ingin menghapus layanan ini?')) return;
  let services = getDB(STORAGE_KEYS.SERVICES);
  services = services.filter(s => s.id !== serviceId);
  saveDB(STORAGE_KEYS.SERVICES, services);
  showToast('Layanan berhasil dihapus.', 'info');
  renderAdminContent();
}
`;
