module.exports = `
/* =========================================================
   AUTH & CUSTOMER MODULE
   ========================================================= */

// Quick Demo Login Switcher
function autoLoginDemo(role) {
  if (role === 'customer') {
    const customers = getDB(STORAGE_KEYS.CUSTOMERS);
    const user = customers[0] || {
      id: 'cust-1',
      name: 'Dimas Arya Nugraha',
      phone: '081987654321',
      email: 'dimas.arya@gmail.com',
      address: 'Perumahan Griya Tirto Indah Blok C No. 4, Pekalongan',
      lat: -6.8895,
      lng: 109.6745
    };
    AppState.currentRole = 'customer';
    AppState.currentUser = user;
    localStorage.setItem(STORAGE_KEYS.ACTIVE_SESSION, JSON.stringify({ role: 'customer', user }));
    showToast('Masuk sebagai Pelanggan: ' + user.name, 'success');
  } else if (role === 'therapist') {
    const therapists = getDB(STORAGE_KEYS.THERAPISTS);
    const user = therapists.find(t => t.id === 'th-1') || therapists[0];
    AppState.currentRole = 'therapist';
    AppState.currentUser = user;
    localStorage.setItem(STORAGE_KEYS.ACTIVE_SESSION, JSON.stringify({ role: 'therapist', user }));
    showToast('Masuk sebagai Terapis: ' + user.name, 'success');
  } else if (role === 'admin') {
    const adminUser = {
      email: 'calonbikers85@gmail.com',
      role: 'admin',
      name: 'Super Admin Pijatku'
    };
    AppState.currentRole = 'admin';
    AppState.currentUser = adminUser;
    localStorage.setItem(STORAGE_KEYS.ACTIVE_SESSION, JSON.stringify({ role: 'admin', user: adminUser }));
    showToast('Masuk sebagai Administrator Pijatku', 'success');
  }
  renderApp();
}

function logout() {
  AppState.currentRole = null;
  AppState.currentUser = null;
  localStorage.removeItem(STORAGE_KEYS.ACTIVE_SESSION);
  showToast('Anda telah keluar.', 'info');
  renderApp();
}

function resetAllData() {
  if (confirm('Reset seluruh database LocalStorage ke data awal demo?')) {
    localStorage.clear();
    initDatabase();
    autoLoginDemo('customer');
    showToast('Seluruh data berhasil di-reset ke data bawaan.', 'success');
  }
}

// GPS Detection with graceful fallback
function requestGPS(callback) {
  if ('geolocation' in navigator) {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const coords = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude
        };
        callback(coords, null);
      },
      (err) => {
        console.warn('Geolocation failed or denied, using Pekalongan fallback:', err.message);
        callback({ ...PEKALONGAN_COORDS }, 'GPS tidak aktif atau izin ditolak. Menggunakan titik demo Pekalongan.');
      },
      { timeout: 8000, enableHighAccuracy: true }
    );
  } else {
    callback({ ...PEKALONGAN_COORDS }, 'Perangkat tidak mendukung Geolocation API. Menggunakan titik Pekalongan.');
  }
}

// Customer Registration & OTP Simulation
function handleCustomerRegister(event) {
  event.preventDefault();
  const form = event.target;
  const name = form.name.value.trim();
  const email = form.email.value.trim();
  const phone = form.phone.value.trim();
  const password = form.password.value;
  const address = form.address.value.trim();
  
  if (!name || !email || !phone || !password || !address) {
    showToast('Harap lengkapi semua kolom pendaftaran.', 'error');
    return;
  }
  
  // Validate existing customer
  const customers = getDB(STORAGE_KEYS.CUSTOMERS);
  if (customers.some(c => c.email.toLowerCase() === email.toLowerCase() || c.phone === phone)) {
    showToast('Email atau nomor HP sudah terdaftar. Silakan login.', 'error');
    return;
  }
  
  // Generate 6 digit OTP
  const simulatedOTP = Math.floor(100000 + Math.random() * 900000).toString();
  
  AppState.pendingRegistration = {
    role: 'customer',
    data: {
      id: 'cust-' + Date.now(),
      name,
      email,
      phone,
      password,
      address,
      lat: AppState.customerLocation.lat || PEKALONGAN_COORDS.lat,
      lng: AppState.customerLocation.lng || PEKALONGAN_COORDS.lng,
      isVerified: false,
      createdAt: new Date().toISOString()
    },
    otp: simulatedOTP
  };
  
  openOTPModal(phone, simulatedOTP);
}

function openOTPModal(phone, otp) {
  const modal = document.getElementById('otp-modal');
  document.getElementById('otp-phone-display').innerText = phone;
  document.getElementById('otp-code-hint').innerText = otp;
  document.getElementById('otp-input').value = '';
  modal.classList.remove('hidden');
  modal.classList.add('flex');
  showToast(\`Kode OTP simulasi: \${otp}\`, 'alert', 'SIMULASI SMS');
}

function verifyOTP() {
  const enteredOTP = document.getElementById('otp-input').value.trim();
  if (!AppState.pendingRegistration) {
    showToast('Tidak ada pendaftaran tertunda.', 'error');
    return;
  }
  
  if (enteredOTP === AppState.pendingRegistration.otp) {
    const customers = getDB(STORAGE_KEYS.CUSTOMERS);
    const newCust = AppState.pendingRegistration.data;
    newCust.isVerified = true;
    customers.push(newCust);
    saveDB(STORAGE_KEYS.CUSTOMERS, customers);
    
    // Auto login
    AppState.currentRole = 'customer';
    AppState.currentUser = newCust;
    localStorage.setItem(STORAGE_KEYS.ACTIVE_SESSION, JSON.stringify({ role: 'customer', user: newCust }));
    
    closeModal('otp-modal');
    AppState.pendingRegistration = null;
    showToast('Pendaftaran & verifikasi berhasil! Selamat datang di PIJATKU.', 'success');
    renderApp();
  } else {
    showToast('Kode OTP salah. Periksa kembali kode di kotak simulasi.', 'error');
  }
}

// Therapist Registration
function handleTherapistRegister(event) {
  event.preventDefault();
  const form = event.target;
  const name = form.name.value.trim();
  const email = form.email.value.trim();
  const phone = form.phone.value.trim();
  const password = form.password.value;
  const gender = form.gender.value;
  const experienceYears = parseInt(form.experience.value, 10) || 1;
  const address = form.address.value.trim();
  const photo = form.photo.value.trim() || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop&q=80';
  const bio = form.bio.value.trim();
  
  // Specialties checkboxes
  const checkboxes = form.querySelectorAll('input[name="specialties"]:checked');
  const specialties = Array.from(checkboxes).map(cb => cb.value);
  
  if (!name || !email || !phone || !password || !address || specialties.length === 0) {
    showToast('Lengkapi nama, email, HP, password, keahlian, dan alamat.', 'error');
    return;
  }
  
  const therapists = getDB(STORAGE_KEYS.THERAPISTS);
  if (therapists.some(t => t.email.toLowerCase() === email.toLowerCase() || t.phone === phone)) {
    showToast('Email atau nomor HP terapis sudah terdaftar.', 'error');
    return;
  }
  
  const newTherapist = {
    id: 'th-' + Date.now(),
    name,
    email,
    phone,
    password,
    role: 'therapist',
    gender,
    specialties,
    experienceYears,
    rating: 5.0,
    reviewCount: 0,
    status: 'pending', // Menunggu persetujuan Admin
    isActive: true,
    isOnline: false,
    photo,
    address,
    bio: bio || 'Terapis profesional bersertifikat.',
    lat: AppState.therapistLocation.lat || PEKALONGAN_COORDS.lat,
    lng: AppState.therapistLocation.lng || PEKALONGAN_COORDS.lng
  };
  
  therapists.push(newTherapist);
  saveDB(STORAGE_KEYS.THERAPISTS, therapists);
  
  addNotification('admin', 'admin', 'Terapis Baru Mendaftar', \`\${name} mendaftar sebagai terapis dan membutuhkan verifikasi persetujuan.\`);
  
  showToast('Pendaftaran terapis berhasil! Akun Anda sedang menunggu verifikasi Admin.', 'success');
  
  // Switch to therapist login
  switchAuthTab('therapist');
}

// Login Handler
function handleLogin(event, role) {
  event.preventDefault();
  const form = event.target;
  const identifier = form.identifier.value.trim();
  const password = form.password.value;
  
  if (role === 'admin') {
    // Exact Admin credentials required
    if (identifier === 'calonbikers85@gmail.com' && password === 'Pekalongan27') {
      const adminUser = { email: identifier, role: 'admin', name: 'Super Admin Pijatku' };
      AppState.currentRole = 'admin';
      AppState.currentUser = adminUser;
      localStorage.setItem(STORAGE_KEYS.ACTIVE_SESSION, JSON.stringify({ role: 'admin', user: adminUser }));
      showToast('Login Admin berhasil. Selamat datang!', 'success');
      renderApp();
    } else {
      showToast('Email atau Password Admin salah! Periksa data akun khusus.', 'error');
    }
    return;
  }
  
  if (role === 'therapist') {
    const therapists = getDB(STORAGE_KEYS.THERAPISTS);
    const th = therapists.find(t => (t.email.toLowerCase() === identifier.toLowerCase() || t.phone === identifier) && t.password === password);
    if (th) {
      if (th.status === 'pending') {
        showToast('Akun Anda masih dalam status MENUNGGU PERSETUJUAN Admin. Hubungi admin atau tunggu verifikasi.', 'alert', 'MENUNGGU VERIFIKASI');
        return;
      }
      if (th.status === 'rejected') {
        showToast('Maaf, permohonan akun terapis Anda ditolak oleh Admin.', 'error', 'AKUN DITOLAK');
        return;
      }
      if (!th.isActive) {
        showToast('Akun terapis Anda dinonaktifkan sementara oleh Admin.', 'error', 'AKUN DINONAKTIFKAN');
        return;
      }
      
      AppState.currentRole = 'therapist';
      AppState.currentUser = th;
      localStorage.setItem(STORAGE_KEYS.ACTIVE_SESSION, JSON.stringify({ role: 'therapist', user: th }));
      showToast('Selamat datang kembali, Terapis ' + th.name, 'success');
      renderApp();
    } else {
      showToast('Kombinasi email/HP dan password terapis tidak ditemukan.', 'error');
    }
    return;
  }
  
  if (role === 'customer') {
    const customers = getDB(STORAGE_KEYS.CUSTOMERS);
    const cust = customers.find(c => (c.email.toLowerCase() === identifier.toLowerCase() || c.phone === identifier) && c.password === password);
    if (cust) {
      AppState.currentRole = 'customer';
      AppState.currentUser = cust;
      localStorage.setItem(STORAGE_KEYS.ACTIVE_SESSION, JSON.stringify({ role: 'customer', user: cust }));
      showToast('Login berhasil. Selamat datang, ' + cust.name, 'success');
      renderApp();
    } else {
      showToast('Email/HP atau password pelanggan tidak cocok.', 'error');
    }
  }
}
`;
