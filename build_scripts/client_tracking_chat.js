module.exports = `
/* =========================================================
   LIVE TRACKING, LEAFLET MAP, CHAT & CALL SIMULATION
   ========================================================= */

let currentTrackingOrderId = null;
let trackingInterval = null;

function openOrderTracking(orderId) {
  currentTrackingOrderId = orderId;
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === orderId);
  if (!order) {
    showToast('Pesanan tidak ditemukan.', 'error');
    return;
  }
  
  const modal = document.getElementById('tracking-modal');
  modal.classList.remove('hidden');
  modal.classList.add('flex');
  
  renderTrackingDetails(order);
  
  setTimeout(() => {
    initTrackingMap(order);
  }, 250);
  
  // Polling for live status updates
  if (trackingInterval) clearInterval(trackingInterval);
  trackingInterval = setInterval(() => {
    const updatedOrders = getDB(STORAGE_KEYS.ORDERS);
    const curr = updatedOrders.find(o => o.id === currentTrackingOrderId);
    if (curr) {
      renderTrackingDetails(curr);
    }
  }, 2000);
}

function closeTrackingModal() {
  closeModal('tracking-modal');
  if (trackingInterval) clearInterval(trackingInterval);
  if (AppState.trackingMap) {
    AppState.trackingMap.remove();
    AppState.trackingMap = null;
  }
}

function renderTrackingDetails(order) {
  document.getElementById('track-order-id').innerText = order.id;
  document.getElementById('track-service-name').innerText = order.serviceName;
  document.getElementById('track-service-duration').innerText = order.durationMinutes + ' Menit';
  document.getElementById('track-service-price').innerText = formatRupiah(order.totalPrice);
  document.getElementById('track-customer-address').innerText = order.customerAddress;
  
  // Therapist info
  const therapists = getDB(STORAGE_KEYS.THERAPISTS);
  const th = therapists.find(t => t.id === order.therapistId);
  
  const thPhoto = th ? th.photo : 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80';
  const thName = th ? th.name : (order.therapistName || 'Mencari terapis...');
  const thRating = th ? th.rating : 4.9;
  
  document.getElementById('track-therapist-photo').src = thPhoto;
  document.getElementById('track-therapist-name').innerText = thName;
  document.getElementById('track-therapist-rating').innerText = thRating;
  
  // Calculate distance & ETA
  const cLat = order.customerLat || PEKALONGAN_COORDS.lat;
  const cLng = order.customerLng || PEKALONGAN_COORDS.lng;
  const tLat = th ? th.lat : (cLat + 0.01);
  const tLng = th ? th.lng : (cLng + 0.01);
  
  const dist = calculateDistance(cLat, cLng, tLat, tLng);
  const etaMinutes = Math.max(5, Math.round(dist * 3)); // approx 3 mins per km
  
  document.getElementById('track-distance-text').innerText = \`\${dist} km (\${etaMinutes} mnt)\`;
  
  // Stepper Visuals: menunggu -> diterima -> menuju_lokasi -> tiba -> sesi_dimulai -> selesai
  const steps = ['menunggu', 'diterima', 'menuju_lokasi', 'tiba', 'sesi_dimulai', 'selesai'];
  const stepIdx = steps.indexOf(order.status);
  
  // Update status badge
  const statusBadge = document.getElementById('track-status-badge');
  const statusTexts = {
    menunggu: { text: 'Menunggu Terapis', color: 'bg-amber-100 text-amber-800' },
    diterima: { text: 'Pesanan Diterima', color: 'bg-blue-100 text-blue-800' },
    menuju_lokasi: { text: 'Terapis Menuju Lokasi', color: 'bg-indigo-100 text-indigo-800' },
    tiba: { text: 'Terapis Tiba di Lokasi', color: 'bg-teal-100 text-teal-800' },
    sesi_dimulai: { text: 'Sesi Pijat Berjalan', color: 'bg-emerald-100 text-emerald-800 animate-pulse' },
    selesai: { text: 'Selesai', color: 'bg-gray-100 text-gray-800' },
    dibatalkan: { text: 'Dibatalkan', color: 'bg-rose-100 text-rose-800' }
  };
  const currStatus = statusTexts[order.status] || { text: order.status, color: 'bg-gray-100 text-gray-800' };
  statusBadge.className = \`px-3 py-1 rounded-full text-xs font-bold \${currStatus.color}\`;
  statusBadge.innerText = currStatus.text;
  
  // Stepper elements
  steps.forEach((st, idx) => {
    const el = document.getElementById('step-dot-' + st);
    if (!el) return;
    if (idx <= stepIdx && order.status !== 'dibatalkan') {
      el.classList.remove('bg-gray-200', 'text-gray-400');
      el.classList.add('bg-emerald-600', 'text-white');
    } else {
      el.classList.remove('bg-emerald-600', 'text-white');
      el.classList.add('bg-gray-200', 'text-gray-400');
    }
  });
  
  // Session Timer Display
  const timerContainer = document.getElementById('track-timer-container');
  if (order.status === 'sesi_dimulai') {
    timerContainer.classList.remove('hidden');
    updateSessionTimerUI(order);
  } else {
    timerContainer.classList.add('hidden');
  }
  
  // Extra Fee Approval Card (Customer Action)
  const extraFeeContainer = document.getElementById('track-extra-fee-card');
  if (order.extraFee && order.extraFee.amount > 0) {
    extraFeeContainer.classList.remove('hidden');
    document.getElementById('extra-fee-amount').innerText = formatRupiah(order.extraFee.amount);
    document.getElementById('extra-fee-reason').innerText = order.extraFee.reason;
    
    const actionBtns = document.getElementById('extra-fee-actions');
    const statusText = document.getElementById('extra-fee-status');
    
    if (order.extraFee.status === 'pending') {
      if (AppState.currentRole === 'customer') {
        actionBtns.classList.remove('hidden');
        statusText.innerText = 'Terapis mengajukan biaya tambahan. Menunggu persetujuan Anda:';
      } else {
        actionBtns.classList.add('hidden');
        statusText.innerText = 'Menunggu persetujuan pelanggan.';
      }
    } else if (order.extraFee.status === 'approved') {
      actionBtns.classList.add('hidden');
      statusText.innerHTML = '<span class="text-emerald-700 font-semibold"><i class="fa-solid fa-circle-check mr-1"></i>Biaya tambahan disetujui pelanggan & ditambahkan ke tagihan.</span>';
    } else if (order.extraFee.status === 'rejected') {
      actionBtns.classList.add('hidden');
      statusText.innerHTML = '<span class="text-rose-700 font-semibold"><i class="fa-solid fa-circle-xmark mr-1"></i>Biaya tambahan ditolak oleh pelanggan.</span>';
    }
  } else {
    extraFeeContainer.classList.add('hidden');
  }
  
  // Review prompt if completed and not yet reviewed
  const reviewPrompt = document.getElementById('track-review-prompt');
  if (order.status === 'selesai' && !order.hasReviewed && AppState.currentRole === 'customer') {
    reviewPrompt.classList.remove('hidden');
  } else {
    reviewPrompt.classList.add('hidden');
  }
  
  // Setup Chat and Call Buttons
  const chatBtn = document.getElementById('track-chat-btn');
  const callBtn = document.getElementById('track-call-btn');
  const mapsBtn = document.getElementById('track-maps-btn');
  
  chatBtn.onclick = () => openChatModal(order.id);
  callBtn.onclick = () => openCallModal(th ? th.name : 'Terapis', th ? th.phone : '081234567891');
  mapsBtn.onclick = () => {
    const url = \`https://www.google.com/maps/dir/?api=1&destination=\${cLat},\${cLng}\`;
    window.open(url, '_blank');
  };
}

// Session Timer UI
function updateSessionTimerUI(order) {
  if (!order.startedAt) return;
  const started = new Date(order.startedAt).getTime();
  const totalMs = (order.durationMinutes || 60) * 60 * 1000;
  const elapsed = Date.now() - started;
  const remaining = Math.max(0, totalMs - elapsed);
  
  const mins = Math.floor(remaining / 60000);
  const secs = Math.floor((remaining % 60000) / 1000);
  
  const timerEl = document.getElementById('session-timer-text');
  if (timerEl) {
    timerEl.innerText = \`\${String(mins).padStart(2, '0')}:\${String(secs).padStart(2, '0')}\`;
  }
}

// Leaflet Map Init for Order Tracking
function initTrackingMap(order) {
  const mapContainer = document.getElementById('tracking-leaflet-map');
  if (!mapContainer) return;
  
  if (AppState.trackingMap) {
    try {
      AppState.trackingMap.remove();
    } catch (e) {
      console.warn('Map cleanup error:', e);
    }
    AppState.trackingMap = null;
  }

  if (typeof L === 'undefined') {
    mapContainer.innerHTML = '<div class="h-full flex items-center justify-center text-xs text-gray-400 bg-gray-100"><i class="fa-solid fa-map mr-2"></i> Peta pelacakan koordinat Pekalongan</div>';
    return;
  }
  
  try {
    const therapists = getDB(STORAGE_KEYS.THERAPISTS);
    const th = therapists.find(t => t.id === order.therapistId);
    
    const cLat = order.customerLat || PEKALONGAN_COORDS.lat;
    const cLng = order.customerLng || PEKALONGAN_COORDS.lng;
    const tLat = th ? th.lat : (cLat + 0.008);
    const tLng = th ? th.lng : (cLng + 0.008);
    
    AppState.trackingMap = L.map('tracking-leaflet-map').setView([cLat, cLng], 14);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(AppState.trackingMap);
    
    // Custom Icons
    const customerIcon = L.divIcon({
      className: 'custom-map-icon',
      html: \`<div class="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center shadow-lg border-2 border-white"><i class="fa-solid fa-house text-xs"></i></div>\`,
      iconSize: [32, 32],
      iconAnchor: [16, 16]
    });
    
    const therapistIcon = L.divIcon({
      className: 'custom-map-icon',
      html: \`<div class="w-9 h-9 rounded-full bg-emerald-600 text-white flex items-center justify-center shadow-lg border-2 border-white pulse-emerald"><i class="fa-solid fa-spa text-sm"></i></div>\`,
      iconSize: [36, 36],
      iconAnchor: [18, 18]
    });
    
    const custMarker = L.marker([cLat, cLng], { icon: customerIcon }).addTo(AppState.trackingMap);
    custMarker.bindPopup(\`<b>Rumah Customer</b><br>\${order.customerName}\`).openPopup();
    
    const thMarker = L.marker([tLat, tLng], { icon: therapistIcon }).addTo(AppState.trackingMap);
    thMarker.bindPopup(\`<b>Posisi Terapis</b><br>\${th ? th.name : 'Terapis'}\`);
    
    // Draw Route Polyline
    const routeLine = L.polyline([[tLat, tLng], [cLat, cLng]], {
      color: '#059669',
      weight: 4,
      dashArray: '8, 8',
      opacity: 0.85
    }).addTo(AppState.trackingMap);
    
    // Fit bounds to show both points nicely
    AppState.trackingMap.fitBounds([[tLat, tLng], [cLat, cLng]], { padding: [40, 40] });
  } catch (err) {
    console.warn('Leaflet map error caught:', err);
    mapContainer.innerHTML = '<div class="h-full flex items-center justify-center text-xs text-gray-500 bg-gray-50 p-4 text-center">Titik penjemputan terapis & customer siap dituju di Google Maps</div>';
  }
}

// Extra Fee Customer Decision
function respondExtraFee(decision) {
  if (!currentTrackingOrderId) return;
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === currentTrackingOrderId);
  if (!order || !order.extraFee) return;
  
  if (decision === 'approve') {
    order.extraFee.status = 'approved';
    order.totalPrice = (order.basePrice || 0) + (order.extraFee.amount || 0);
    showToast('Biaya tambahan berhasil disetujui.', 'success');
    addNotification('therapist', order.therapistId, 'Biaya Tambahan Disetujui', \`Customer telah menyetujui biaya tambahan Rp \${order.extraFee.amount.toLocaleString('id-ID')}.\`);
  } else {
    order.extraFee.status = 'rejected';
    showToast('Biaya tambahan ditolak.', 'info');
    addNotification('therapist', order.therapistId, 'Biaya Tambahan Ditolak', 'Customer menolak pengajuan biaya tambahan.');
  }
  
  saveDB(STORAGE_KEYS.ORDERS, orders);
  renderTrackingDetails(order);
  renderApp();
}

// Rating & Review Modal
function openReviewModal(orderId) {
  const modal = document.getElementById('review-modal');
  document.getElementById('review-order-id').value = orderId;
  document.getElementById('review-comment').value = '';
  setStarRating(5);
  modal.classList.remove('hidden');
  modal.classList.add('flex');
}

let currentSelectedRating = 5;
function setStarRating(rating) {
  currentSelectedRating = rating;
  for (let i = 1; i <= 5; i++) {
    const star = document.getElementById('star-' + i);
    if (!star) continue;
    if (i <= rating) {
      star.className = 'fa-solid fa-star text-2xl text-amber-400 cursor-pointer transition-transform hover:scale-110';
    } else {
      star.className = 'fa-regular fa-star text-2xl text-gray-300 cursor-pointer transition-transform hover:scale-110';
    }
  }
}

function submitReview() {
  const orderId = document.getElementById('review-order-id').value || currentTrackingOrderId;
  const comment = document.getElementById('review-comment').value.trim();
  
  if (!comment) {
    showToast('Silakan tulis ulasan/kesan Anda mengenai layanan terapis.', 'error');
    return;
  }
  
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === orderId);
  if (!order) return;
  
  const reviews = getDB(STORAGE_KEYS.REVIEWS);
  const newReview = {
    id: 'rev-' + Date.now(),
    orderId: order.id,
    customerId: order.customerId,
    customerName: order.customerName,
    therapistId: order.therapistId,
    therapistName: order.therapistName,
    serviceName: order.serviceName,
    rating: currentSelectedRating,
    comment,
    createdAt: new Date().toISOString()
  };
  
  reviews.unshift(newReview);
  saveDB(STORAGE_KEYS.REVIEWS, reviews);
  
  // Update Therapist Rating
  const therapists = getDB(STORAGE_KEYS.THERAPISTS);
  const th = therapists.find(t => t.id === order.therapistId);
  if (th) {
    const thReviews = reviews.filter(r => r.therapistId === th.id);
    const avg = thReviews.reduce((sum, r) => sum + r.rating, 0) / thReviews.length;
    th.rating = Math.round(avg * 10) / 10;
    th.reviewCount = thReviews.length;
    saveDB(STORAGE_KEYS.THERAPISTS, therapists);
  }
  
  order.hasReviewed = true;
  saveDB(STORAGE_KEYS.ORDERS, orders);
  
  addNotification('therapist', order.therapistId, 'Review Baru Diterima', \`Customer \${order.customerName} memberikan rating \${currentSelectedRating} bintang.\`);
  
  closeModal('review-modal');
  showToast('Terima kasih atas ulasan dan rating Anda!', 'success');
  renderApp();
  if (currentTrackingOrderId === orderId) {
    renderTrackingDetails(order);
  }
}

// Chat Modal & Realtime Simulation
function openChatModal(orderId) {
  AppState.activeChatOrderId = orderId;
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === orderId);
  if (!order) return;
  
  const modal = document.getElementById('chat-modal');
  const isCustomer = AppState.currentRole === 'customer';
  const partnerName = isCustomer ? order.therapistName : order.customerName;
  document.getElementById('chat-partner-name').innerText = partnerName || 'Kontak Pesanan';
  document.getElementById('chat-order-tag').innerText = order.id;
  
  renderChatMessages();
  modal.classList.remove('hidden');
  modal.classList.add('flex');
}

function renderChatMessages() {
  if (!AppState.activeChatOrderId) return;
  const messages = getDB(STORAGE_KEYS.MESSAGES).filter(m => m.orderId === AppState.activeChatOrderId);
  const container = document.getElementById('chat-messages-container');
  if (!container) return;
  
  if (messages.length === 0) {
    container.innerHTML = \`
      <div class="text-center text-gray-400 text-xs py-8">
        <i class="fa-regular fa-comments text-2xl mb-2 block"></i>
        Belum ada obrolan. Mulai percakapan dengan terapis/pelanggan Anda.
      </div>
    \`;
    return;
  }
  
  const currentRole = AppState.currentRole;
  container.innerHTML = messages.map(m => {
    const isMe = m.senderRole === currentRole;
    return \`
      <div class="flex flex-col \${isMe ? 'items-end' : 'items-start'} mb-3">
        <div class="text-[10px] text-gray-400 mb-1 px-1">\${escapeHtml(m.senderName)} • \${formatDate(m.timestamp).split(' ')[3] || ''}</div>
        <div class="p-3 rounded-2xl max-w-[80%] text-sm shadow-sm \${isMe ? 'chat-bubble-user' : 'chat-bubble-peer'}">
          \${escapeHtml(m.text)}
        </div>
      </div>
    \`;
  }).join('');
  
  container.scrollTop = container.scrollHeight;
}

function sendChatMessage(textOverride) {
  const input = document.getElementById('chat-input');
  const text = (textOverride || input.value).trim();
  if (!text || !AppState.activeChatOrderId) return;
  
  const orders = getDB(STORAGE_KEYS.ORDERS);
  const order = orders.find(o => o.id === AppState.activeChatOrderId);
  if (!order) return;
  
  const senderRole = AppState.currentRole;
  const senderName = AppState.currentUser ? AppState.currentUser.name : (senderRole === 'customer' ? 'Customer' : 'Terapis');
  
  const messages = getDB(STORAGE_KEYS.MESSAGES);
  const newMsg = {
    id: 'msg-' + Date.now(),
    orderId: order.id,
    senderRole,
    senderName,
    text,
    timestamp: new Date().toISOString()
  };
  
  messages.push(newMsg);
  saveDB(STORAGE_KEYS.MESSAGES, messages);
  if (!textOverride) input.value = '';
  
  renderChatMessages();
  
  // Notification to partner
  const targetRole = senderRole === 'customer' ? 'therapist' : 'customer';
  const targetUserId = senderRole === 'customer' ? order.therapistId : order.customerId;
  addNotification(targetRole, targetUserId, 'Pesan Baru', \`\${senderName}: \${text.slice(0, 35)}...\`);
}

// Call Simulation Modal
let callTimerInterval = null;
let callDurationSec = 0;

function openCallModal(name, phone) {
  const modal = document.getElementById('call-modal');
  document.getElementById('call-contact-name').innerText = name;
  document.getElementById('call-contact-phone').innerText = phone;
  document.getElementById('call-status-text').innerText = 'Memanggil...';
  document.getElementById('call-timer').innerText = '00:00';
  document.getElementById('call-timer').classList.add('hidden');
  
  modal.classList.remove('hidden');
  modal.classList.add('flex');
  
  playChime('incoming');
  
  // Connect after 2.5 seconds
  setTimeout(() => {
    document.getElementById('call-status-text').innerText = 'Terhubung';
    document.getElementById('call-timer').classList.remove('hidden');
    callDurationSec = 0;
    if (callTimerInterval) clearInterval(callTimerInterval);
    callTimerInterval = setInterval(() => {
      callDurationSec++;
      const m = Math.floor(callDurationSec / 60);
      const s = callDurationSec % 60;
      document.getElementById('call-timer').innerText = \`\${String(m).padStart(2,'0')}:\${String(s).padStart(2,'0')}\`;
    }, 1000);
  }, 2500);
}

function endCall() {
  if (callTimerInterval) clearInterval(callTimerInterval);
  closeModal('call-modal');
  showToast('Panggilan berakhir.', 'info');
}

function launchWhatsApp(phone) {
  const cleanPhone = (phone || '').replace(/[^0-9]/g, '');
  const waNumber = cleanPhone.startsWith('0') ? '62' + cleanPhone.slice(1) : cleanPhone;
  window.open(\`https://wa.me/\${waNumber}\`, '_blank');
}
`;
