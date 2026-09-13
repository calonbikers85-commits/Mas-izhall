module.exports = `
/* =========================================================
   CUSTOMER ORDERING, TRACKING & INTERACTIVE MAP MODULE
   ========================================================= */

let bookingSelectedService = null;
let bookingSelectedDuration = 60;
let bookingSelectedTherapistId = 'auto';
let bookingMarker = null;

function openBookingModal(serviceId) {
  const services = getDB(STORAGE_KEYS.SERVICES);
  const service = services.find(s => s.id === serviceId) || services[0];
  if (!service) return;
  
  bookingSelectedService = service;
  bookingSelectedDuration = service.durations[0]?.minutes || 60;
  bookingSelectedTherapistId = 'auto';
  
  const modal = document.getElementById('booking-modal');
  document.getElementById('booking-service-name').innerText = service.name;
  document.getElementById('booking-service-desc').innerText = service.tagline || service.description;
  
  // Date & Time defaults
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('booking-date').value = today;
  
  const now = new Date();
  now.setMinutes(now.getMinutes() + 30);
  const timeStr = String(now.getHours()).padStart(2, '0') + ':' + String(now.getMinutes()).padStart(2, '0');
  document.getElementById('booking-time').value = timeStr;
  
  // Render Duration Tabs
  const durationContainer = document.getElementById('booking-durations');
  durationContainer.innerHTML = service.durations.map((d, idx) => \`
    <button type="button" onclick="selectBookingDuration(\${d.minutes})" 
      id="duration-btn-\${d.minutes}"
      class="duration-btn flex-1 py-2.5 px-3 rounded-xl border text-sm font-semibold transition-all \${d.minutes === bookingSelectedDuration ? 'border-emerald-600 bg-emerald-50 text-emerald-800 ring-2 ring-emerald-500' : 'border-gray-200 bg-white text-gray-700 hover:border-emerald-200'}">
      \${d.minutes} Menit
      <div class="text-xs font-normal text-gray-500 mt-0.5">\${formatRupiah(d.price)}</div>
    </button>
  \`).join('');
  
  // Render Therapist selection list
  renderBookingTherapistList();
  
  // Customer address
  const customer = AppState.currentUser || {};
  document.getElementById('booking-address').value = customer.address || 'Pekalongan, Jawa Tengah';
  document.getElementById('booking-notes').value = '';
  
  updateBookingPrice();
  
  modal.classList.remove('hidden');
  modal.classList.add('flex');
  
  // Init Leaflet map for location picker
  setTimeout(() => {
    initBookingMap(customer.lat || PEKALONGAN_COORDS.lat, customer.lng || PEKALONGAN_COORDS.lng);
  }, 200);
}

function selectBookingDuration(minutes) {
  bookingSelectedDuration = minutes;
  document.querySelectorAll('.duration-btn').forEach(btn => {
    btn.classList.remove('border-emerald-600', 'bg-emerald-50', 'text-emerald-800', 'ring-2', 'ring-emerald-500');
    btn.classList.add('border-gray-200', 'bg-white', 'text-gray-700');
  });
  const activeBtn = document.getElementById('duration-btn-' + minutes);
  if (activeBtn) {
    activeBtn.classList.remove('border-gray-200', 'bg-white', 'text-gray-700');
    activeBtn.classList.add('border-emerald-600', 'bg-emerald-50', 'text-emerald-800', 'ring-2', 'ring-emerald-500');
  }
  updateBookingPrice();
}

function updateBookingPrice() {
  if (!bookingSelectedService) return;
  const durationObj = bookingSelectedService.durations.find(d => d.minutes === bookingSelectedDuration) || bookingSelectedService.durations[0];
  const price = durationObj ? durationObj.price : bookingSelectedService.basePrice;
  document.getElementById('booking-total-price').innerText = formatRupiah(price);
}

function renderBookingTherapistList() {
  const therapists = getDB(STORAGE_KEYS.THERAPISTS).filter(t => t.status === 'approved' && t.isActive);
  const container = document.getElementById('booking-therapists-list');
  const custLat = AppState.currentUser?.lat || PEKALONGAN_COORDS.lat;
  const custLng = AppState.currentUser?.lng || PEKALONGAN_COORDS.lng;
  
  let html = \`
    <div onclick="selectBookingTherapist('auto')" id="th-opt-auto" class="cursor-pointer p-3 rounded-xl border flex items-center justify-between \${bookingSelectedTherapistId === 'auto' ? 'border-emerald-600 bg-emerald-50 text-emerald-900 ring-2 ring-emerald-500' : 'border-gray-200 bg-white hover:border-emerald-300'}">
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold">
          <i class="fa-solid fa-wand-magic-sparkles"></i>
        </div>
        <div>
          <div class="font-bold text-sm">Pilihkan Terapis Terdekat Otomatis</div>
          <div class="text-xs text-gray-500">Sistem akan memprioritaskan terapis online terdekat dengan Anda</div>
        </div>
      </div>
      <i class="fa-solid fa-circle-check \${bookingSelectedTherapistId === 'auto' ? 'text-emerald-600 text-lg' : 'text-gray-300'}"></i>
    </div>
  \`;
  
  therapists.forEach(t => {
    const dist = calculateDistance(custLat, custLng, t.lat, t.lng);
    const isSelected = bookingSelectedTherapistId === t.id;
    html += \`
      <div onclick="selectBookingTherapist('\${t.id}')" id="th-opt-\${t.id}" class="cursor-pointer p-3 rounded-xl border flex items-center justify-between mt-2 \${isSelected ? 'border-emerald-600 bg-emerald-50 text-emerald-900 ring-2 ring-emerald-500' : 'border-gray-200 bg-white hover:border-emerald-300'}">
        <div class="flex items-center space-x-3">
          <div class="relative">
            <img src="\${escapeHtml(t.photo)}" alt="\${escapeHtml(t.name)}" class="w-10 h-10 rounded-full object-cover border">
            <span class="absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white \${t.isOnline ? 'bg-emerald-500' : 'bg-gray-400'}"></span>
          </div>
          <div>
            <div class="font-bold text-sm flex items-center gap-1.5">
              \${escapeHtml(t.name)}
              <span class="text-xs font-normal text-amber-600 flex items-center"><i class="fa-solid fa-star text-[10px] mr-0.5"></i>\${t.rating || 5.0}</span>
            </div>
            <div class="text-xs text-gray-500 flex items-center gap-2">
              <span><i class="fa-solid fa-location-dot text-emerald-600 mr-1"></i>\${dist} km</span>
              <span>•</span>
              <span class="\${t.isOnline ? 'text-emerald-600 font-medium' : 'text-gray-400'}">\${t.isOnline ? 'Online Siap Melayani' : 'Offline'}</span>
            </div>
          </div>
        </div>
        <i class="fa-solid fa-circle-check \${isSelected ? 'text-emerald-600 text-lg' : 'text-gray-300'}"></i>
      </div>
    \`;
  });
  
  container.innerHTML = html;
}

function selectBookingTherapist(thId) {
  bookingSelectedTherapistId = thId;
  renderBookingTherapistList();
}

function initBookingMap(lat, lng) {
  const mapContainer = document.getElementById('booking-leaflet-map');
  if (!mapContainer) return;
  
  if (AppState.bookingMap) {
    AppState.bookingMap.remove();
    AppState.bookingMap = null;
  }
  
  AppState.bookingMap = L.map('booking-leaflet-map').setView([lat, lng], 15);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap'
  }).addTo(AppState.bookingMap);
  
  bookingMarker = L.marker([lat, lng], { draggable: true }).addTo(AppState.bookingMap);
  bookingMarker.bindPopup('<b>Lokasi Penjemputan / Rumah Anda</b><br>Geser pin untuk ubah titik.').openPopup();
  
  bookingMarker.on('dragend', function(e) {
    const position = bookingMarker.getLatLng();
    AppState.customerLocation.lat = position.lat;
    AppState.customerLocation.lng = position.lng;
    document.getElementById('booking-address').value = \`Titik Pin Peta (Lat: \${position.lat.toFixed(4)}, Lng: \${position.lng.toFixed(4)})\`;
    renderBookingTherapistList();
  });
  
  AppState.bookingMap.on('click', function(e) {
    bookingMarker.setLatLng(e.latlng);
    AppState.customerLocation.lat = e.latlng.lat;
    AppState.customerLocation.lng = e.latlng.lng;
    document.getElementById('booking-address').value = \`Titik Pin Peta (Lat: \${e.latlng.lat.toFixed(4)}, Lng: \${e.latlng.lng.toFixed(4)})\`;
    renderBookingTherapistList();
  });
}

// Confirm and Submit Order
function confirmBookingOrder() {
  if (!bookingSelectedService) return;
  const customer = AppState.currentUser;
  if (!customer) {
    showToast('Silakan login terlebih dahulu untuk memesan terapis.', 'error');
    return;
  }
  
  const date = document.getElementById('booking-date').value;
  const time = document.getElementById('booking-time').value;
  const address = document.getElementById('booking-address').value.trim();
  const notes = document.getElementById('booking-notes').value.trim();
  
  if (!date || !time || !address) {
    showToast('Harap lengkapi tanggal, jam, dan alamat layanan.', 'error');
    return;
  }
  
  const durationObj = bookingSelectedService.durations.find(d => d.minutes === bookingSelectedDuration) || bookingSelectedService.durations[0];
  const price = durationObj ? durationObj.price : bookingSelectedService.basePrice;
  
  // Find appropriate therapist
  const therapists = getDB(STORAGE_KEYS.THERAPISTS).filter(t => t.status === 'approved' && t.isActive);
  let assignedTherapist = null;
  
  if (bookingSelectedTherapistId !== 'auto') {
    assignedTherapist = therapists.find(t => t.id === bookingSelectedTherapistId);
  }
  
  if (!assignedTherapist) {
    // Pick nearest online therapist
    const onlineTherapists = therapists.filter(t => t.isOnline);
    if (onlineTherapists.length > 0) {
      // Sort by distance
      onlineTherapists.sort((a, b) => {
        const dA = calculateDistance(customer.lat, customer.lng, a.lat, a.lng);
        const dB = calculateDistance(customer.lat, customer.lng, b.lat, b.lng);
        return dA - dB;
      });
      assignedTherapist = onlineTherapists[0];
    } else if (therapists.length > 0) {
      assignedTherapist = therapists[0];
    }
  }
  
  const orderId = 'ORD-' + new Date().toISOString().slice(0,10).replace(/-/g,'') + '-' + Math.floor(100 + Math.random() * 900);
  
  const newOrder = {
    id: orderId,
    customerId: customer.id,
    customerName: customer.name,
    customerPhone: customer.phone,
    customerAddress: address,
    customerLat: customer.lat || PEKALONGAN_COORDS.lat,
    customerLng: customer.lng || PEKALONGAN_COORDS.lng,
    therapistId: assignedTherapist ? assignedTherapist.id : null,
    therapistName: assignedTherapist ? assignedTherapist.name : 'Mencari Terapis...',
    therapistPhone: assignedTherapist ? assignedTherapist.phone : '-',
    serviceId: bookingSelectedService.id,
    serviceName: bookingSelectedService.name,
    durationMinutes: bookingSelectedDuration,
    basePrice: price,
    extraFee: null,
    totalPrice: price,
    status: 'menunggu', // menunggu -> diterima -> menuju_lokasi -> tiba -> sesi_dimulai -> selesai
    scheduledDate: date,
    scheduledTime: time,
    notes: notes || '-',
    createdAt: new Date().toISOString(),
    acceptedAt: null,
    startedAt: null,
    completedAt: null,
    hasReviewed: false
  };
  
  const orders = getDB(STORAGE_KEYS.ORDERS);
  orders.unshift(newOrder);
  saveDB(STORAGE_KEYS.ORDERS, orders);
  
  // Create notifications
  if (assignedTherapist) {
    addNotification('therapist', assignedTherapist.id, 'Pesanan Masuk Baru!', \`Customer \${customer.name} memesan \${bookingSelectedService.name} (\${bookingSelectedDuration} mnt).\`);
  }
  addNotification('customer', customer.id, 'Pesanan Terkirim', \`Pesanan Anda (\${orderId}) berhasil dikirim ke terapis. Menunggu konfirmasi.\`);
  
  closeModal('booking-modal');
  showToast('Pesanan berhasil dibuat! Menunggu terapis menerima pesanan.', 'success');
  
  // Open live tracking immediately
  openOrderTracking(orderId);
  renderApp();
}
`;
