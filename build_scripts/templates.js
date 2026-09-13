module.exports = `
  <!-- DEMO TOOLBAR AT TOP -->
  <div class="bg-gray-900 text-white text-xs px-4 py-2 flex flex-wrap items-center justify-between gap-2 sticky top-0 z-50 shadow-md">
    <div class="flex items-center space-x-2">
      <span class="font-black text-emerald-400 tracking-wider">PIJATKU DEMO:</span>
      <span id="demo-current-role-badge" class="px-2 py-0.5 rounded-full text-[11px] font-bold bg-gray-700 text-gray-200">Memuat...</span>
    </div>
    <div class="flex items-center space-x-1.5 overflow-x-auto">
      <button onclick="autoLoginDemo('customer')" class="px-2.5 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded text-[11px] font-bold transition-all">
        <i class="fa-solid fa-user mr-1"></i> Pelanggan
      </button>
      <button onclick="autoLoginDemo('therapist')" class="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-[11px] font-bold transition-all">
        <i class="fa-solid fa-spa mr-1"></i> Terapis
      </button>
      <button onclick="autoLoginDemo('admin')" class="px-2.5 py-1 bg-amber-600 hover:bg-amber-700 text-white rounded text-[11px] font-bold transition-all">
        <i class="fa-solid fa-shield-halved mr-1"></i> Admin
      </button>
      <button onclick="resetAllData()" class="px-2.5 py-1 bg-gray-700 hover:bg-gray-600 text-gray-300 hover:text-white rounded text-[11px] transition-all ml-1">
        <i class="fa-solid fa-rotate mr-1"></i> Reset Data
      </button>
      <button onclick="logout()" class="px-2 py-1 bg-rose-900 hover:bg-rose-800 text-rose-200 rounded text-[11px] transition-all">
        <i class="fa-solid fa-arrow-right-from-bracket"></i>
      </button>
    </div>
  </div>

  <!-- TOAST NOTIFICATION CONTAINER -->
  <div id="toast-container" class="fixed top-12 right-4 z-50 flex flex-col items-end pointer-events-none"></div>

  <!-- MAIN APP CONTAINER -->
  <main id="app-root" class="min-h-screen">

    <!-- ========================================================
         VIEW 1: AUTHENTICATION (LOGIN & REGISTRATION)
         ======================================================== -->
    <section id="view-auth" class="app-container hidden py-6 px-4">
      <div class="text-center mb-6">
        <div class="w-16 h-16 bg-gradient-to-br from-emerald-500 to-teal-700 rounded-3xl mx-auto flex items-center justify-center text-white text-3xl shadow-lg mb-3">
          <i class="fa-solid fa-spa"></i>
        </div>
        <h1 class="text-2xl font-black text-gray-900 tracking-tight">PIJATKU</h1>
        <p class="text-xs text-emerald-800 font-semibold tracking-wide uppercase mt-0.5">Pesan Terapis, Nyaman di Rumah</p>
        <p class="text-xs text-gray-500 mt-1">Layanan pijat panggilan profesional, higienis & terpercaya</p>
      </div>

      <!-- Auth Tabs -->
      <div class="flex border-b border-gray-200 mb-6 text-xs font-bold overflow-x-auto no-scrollbar">
        <button onclick="switchAuthTab('login-customer')" id="auth-tab-login-customer" class="auth-tab-btn flex-1 py-2.5 px-3 border-b-2 whitespace-nowrap text-center">Masuk Pelanggan</button>
        <button onclick="switchAuthTab('login-therapist')" id="auth-tab-login-therapist" class="auth-tab-btn flex-1 py-2.5 px-3 border-b-2 whitespace-nowrap text-center">Masuk Terapis</button>
        <button onclick="switchAuthTab('login-admin')" id="auth-tab-login-admin" class="auth-tab-btn flex-1 py-2.5 px-3 border-b-2 whitespace-nowrap text-center">Admin</button>
        <button onclick="switchAuthTab('reg-customer')" id="auth-tab-reg-customer" class="auth-tab-btn flex-1 py-2.5 px-3 border-b-2 whitespace-nowrap text-center">Daftar Pelanggan</button>
        <button onclick="switchAuthTab('reg-therapist')" id="auth-tab-reg-therapist" class="auth-tab-btn flex-1 py-2.5 px-3 border-b-2 whitespace-nowrap text-center">Daftar Mitra Terapis</button>
      </div>

      <!-- Tab: Login Customer -->
      <div id="auth-panel-login-customer" class="auth-panel">
        <div class="bg-blue-50/70 border border-blue-100 rounded-2xl p-3.5 mb-4 text-xs flex items-center justify-between">
          <div>
            <span class="font-bold text-blue-900">Demo Cepat:</span> Dimas Arya (081987654321)
          </div>
          <button onclick="autoLoginDemo('customer')" class="px-2.5 py-1 bg-blue-600 text-white rounded-lg font-bold text-[11px]">Auto Fill</button>
        </div>
        <form onsubmit="handleLogin(event, 'customer')" class="space-y-3.5">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Email atau Nomor HP</label>
            <input type="text" name="identifier" value="dimas.arya@gmail.com" required class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none" placeholder="0812... / email@anda.com">
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Password</label>
            <input type="password" name="password" value="customerdimas123" required class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none" placeholder="••••••••">
          </div>
          <button type="submit" class="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-sm shadow-md transition-all">
            Masuk sebagai Pelanggan
          </button>
        </form>
      </div>

      <!-- Tab: Login Therapist -->
      <div id="auth-panel-login-therapist" class="auth-panel hidden">
        <div class="bg-emerald-50/70 border border-emerald-100 rounded-2xl p-3.5 mb-4 text-xs flex items-center justify-between">
          <div>
            <span class="font-bold text-emerald-900">Demo Cepat:</span> Budi Santoso (081234567891)
          </div>
          <button onclick="autoLoginDemo('therapist')" class="px-2.5 py-1 bg-emerald-600 text-white rounded-lg font-bold text-[11px]">Auto Fill</button>
        </div>
        <form onsubmit="handleLogin(event, 'therapist')" class="space-y-3.5">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Email atau Nomor HP Terapis</label>
            <input type="text" name="identifier" value="budi.terapis@gmail.com" required class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none">
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Password</label>
            <input type="password" name="password" value="terapisbudi123" required class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none">
          </div>
          <button type="submit" class="w-full py-3 bg-emerald-700 hover:bg-emerald-800 text-white font-bold rounded-xl text-sm shadow-md transition-all">
            Masuk Portal Terapis
          </button>
        </form>
      </div>

      <!-- Tab: Login Admin -->
      <div id="auth-panel-login-admin" class="auth-panel hidden">
        <div class="bg-amber-50 border border-amber-200 rounded-2xl p-3 mb-4 text-xs text-amber-900">
          <div class="font-bold flex items-center gap-1.5 mb-1">
            <i class="fa-solid fa-lock"></i> Kredensial Khusus Administrator
          </div>
          <div>Email: <b>calonbikers85@gmail.com</b></div>
          <div>Password: <b>Pekalongan27</b></div>
          <div class="text-[10px] text-amber-700 mt-1 italic">*Hanya untuk demo prototipe. Sistem produksi wajib menggunakan backend auth.</div>
        </div>
        <form onsubmit="handleLogin(event, 'admin')" class="space-y-3.5">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Email Admin</label>
            <input type="email" name="identifier" value="calonbikers85@gmail.com" required class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-amber-500 focus:outline-none">
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Password Admin</label>
            <input type="password" name="password" value="Pekalongan27" required class="w-full px-3.5 py-2.5 rounded-xl border border-gray-300 text-sm focus:ring-2 focus:ring-amber-500 focus:outline-none">
          </div>
          <button type="submit" class="w-full py-3 bg-amber-600 hover:bg-amber-700 text-white font-bold rounded-xl text-sm shadow-md transition-all">
            Masuk Panel Admin
          </button>
        </form>
      </div>

      <!-- Tab: Register Customer -->
      <div id="auth-panel-reg-customer" class="auth-panel hidden">
        <form onsubmit="handleCustomerRegister(event)" class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Nama Lengkap</label>
            <input type="text" name="name" required class="w-full px-3.5 py-2 rounded-xl border text-sm" placeholder="Contoh: Dimas Arya">
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Email</label>
              <input type="email" name="email" required class="w-full px-3 py-2 rounded-xl border text-sm" placeholder="nama@email.com">
            </div>
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Nomor HP</label>
              <input type="tel" name="phone" required class="w-full px-3 py-2 rounded-xl border text-sm" placeholder="081234567890">
            </div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Password</label>
            <input type="password" name="password" required class="w-full px-3.5 py-2 rounded-xl border text-sm" placeholder="Minimal 6 karakter">
          </div>
          <div>
            <div class="flex items-center justify-between mb-1">
              <label class="block text-xs font-semibold text-gray-700">Alamat Lengkap</label>
              <button type="button" onclick="requestGPS((c, e) => { AppState.customerLocation = c; document.getElementById('reg-cust-addr').value = 'Titik GPS: Pekalongan (' + c.lat.toFixed(4) + ', ' + c.lng.toFixed(4) + ')'; showToast(e || 'GPS terdeteksi!', e ? 'alert' : 'success'); })" class="text-[11px] text-emerald-700 font-bold flex items-center gap-1">
                <i class="fa-solid fa-location-crosshairs"></i> Deteksi GPS
              </button>
            </div>
            <textarea name="address" id="reg-cust-addr" rows="2" required class="w-full px-3 py-2 rounded-xl border text-sm" placeholder="Nama jalan, nomor rumah, kelurahan, Pekalongan"></textarea>
          </div>
          <button type="submit" class="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-sm shadow-md transition-all">
            Daftar & Kirim OTP SMS
          </button>
        </form>
      </div>

      <!-- Tab: Register Therapist -->
      <div id="auth-panel-reg-therapist" class="auth-panel hidden">
        <div class="bg-emerald-50 text-emerald-800 text-xs p-3 rounded-xl mb-3 flex items-start space-x-2">
          <i class="fa-solid fa-circle-info text-sm mt-0.5"></i>
          <span>Pendaftaran mitra terapis memerlukan persetujuan dari Admin sebelum akun aktif dan dapat menerima pesanan.</span>
        </div>
        <form onsubmit="handleTherapistRegister(event)" class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Nama Lengkap & Gelar (jika ada)</label>
            <input type="text" name="name" required class="w-full px-3.5 py-2 rounded-xl border text-sm" placeholder="Contoh: Rahmat Hidayat, A.Md.Kes">
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Email</label>
              <input type="email" name="email" required class="w-full px-3 py-2 rounded-xl border text-sm" placeholder="terapis@email.com">
            </div>
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Nomor WhatsApp/HP</label>
              <input type="tel" name="phone" required class="w-full px-3 py-2 rounded-xl border text-sm" placeholder="0812...">
            </div>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Jenis Kelamin</label>
              <select name="gender" class="w-full px-3 py-2 rounded-xl border text-sm bg-white">
                <option value="Laki-laki">Laki-laki</option>
                <option value="Perempuan">Perempuan</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Pengalaman (Tahun)</label>
              <input type="number" name="experience" min="1" max="40" value="3" required class="w-full px-3 py-2 rounded-xl border text-sm">
            </div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Keahlian / Spesialisasi</label>
            <div class="grid grid-cols-2 gap-2 text-xs bg-gray-50 p-2.5 rounded-xl border">
              <label class="flex items-center space-x-1.5"><input type="checkbox" name="specialties" value="Pijat Tradisional Jawa" checked> <span>Tradisional Jawa</span></label>
              <label class="flex items-center space-x-1.5"><input type="checkbox" name="specialties" value="Refleksi Kaki & Tangan" checked> <span>Refleksi Kaki</span></label>
              <label class="flex items-center space-x-1.5"><input type="checkbox" name="specialties" value="Deep Tissue & Sport Massage"> <span>Deep Tissue</span></label>
              <label class="flex items-center space-x-1.5"><input type="checkbox" name="specialties" value="Aromaterapi & Lulur Keraton"> <span>Aromaterapi</span></label>
              <label class="flex items-center space-x-1.5"><input type="checkbox" name="specialties" value="Pijat Ibu Hamil & Pasca Salin"> <span>Ibu Hamil</span></label>
              <label class="flex items-center space-x-1.5"><input type="checkbox" name="specialties" value="Totok Wajah & Bekam Kering"> <span>Totok Wajah</span></label>
            </div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">URL Foto Profil / Terapis</label>
            <input type="url" name="photo" class="w-full px-3.5 py-2 rounded-xl border text-sm" placeholder="https://..." value="https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop&q=80">
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Alamat Domisili</label>
            <textarea name="address" rows="2" required class="w-full px-3 py-2 rounded-xl border text-sm" placeholder="Kecamatan & Kota di Pekalongan"></textarea>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Bio Singkat & Sertifikasi</label>
            <input type="text" name="bio" class="w-full px-3.5 py-2 rounded-xl border text-sm" placeholder="Contoh: Sertifikat BNSP Pijat Tradisional 2023">
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Password Akun</label>
            <input type="password" name="password" required class="w-full px-3.5 py-2 rounded-xl border text-sm" placeholder="••••••••">
          </div>
          <button type="submit" class="w-full py-3 bg-teal-700 hover:bg-teal-800 text-white font-bold rounded-xl text-sm shadow-md transition-all">
            Kirim Permohonan Mitra Terapis
          </button>
        </form>
      </div>
    </section>

    <!-- ========================================================
         VIEW 2: CUSTOMER DASHBOARD & SCREENS
         ======================================================== -->
    <section id="view-customer" class="app-container hidden pb-24">
      <!-- Tab 1: Customer Home -->
      <div id="cust-view-home" class="cust-view-tab">
        <!-- Header -->
        <div class="bg-gradient-to-r from-emerald-800 via-emerald-700 to-teal-800 text-white p-5 rounded-b-3xl shadow-lg">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-full bg-emerald-600/60 border border-emerald-400/40 flex items-center justify-center font-bold text-lg">
                <i class="fa-solid fa-user"></i>
              </div>
              <div>
                <div class="text-[11px] text-emerald-200">Selamat Datang,</div>
                <div id="cust-header-name" class="font-extrabold text-base leading-tight">Dimas Arya</div>
              </div>
            </div>
            <button onclick="setCustomerTab('history')" class="w-9 h-9 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center text-white relative">
              <i class="fa-regular fa-bell text-sm"></i>
            </button>
          </div>
          
          <!-- Location Pill -->
          <div class="bg-white/10 backdrop-blur-md px-3 py-2 rounded-2xl flex items-center justify-between text-xs">
            <div class="flex items-center space-x-2 truncate">
              <i class="fa-solid fa-location-dot text-emerald-300"></i>
              <span id="cust-header-address" class="truncate">Pekalongan, Jawa Tengah</span>
            </div>
            <button onclick="requestGPS((c, e) => { AppState.customerLocation = c; showToast(e || 'GPS aktif: ' + c.lat.toFixed(4) + ', ' + c.lng.toFixed(4), e ? 'alert' : 'success'); renderCustomerNearbyTherapists(); })" class="text-[10px] font-bold bg-emerald-500/80 hover:bg-emerald-500 px-2 py-1 rounded-lg shrink-0">
              Ubah GPS
            </button>
          </div>
        </div>

        <!-- Active Order Floating Banner (Pulsing live pill) -->
        <div id="cust-active-order-banner" class="mx-4 -mt-3 bg-gradient-to-r from-emerald-600 to-teal-600 text-white p-3.5 rounded-2xl shadow-xl flex items-center justify-between cursor-pointer hover:scale-[1.01] transition-transform hidden">
          <div class="flex items-center space-x-3">
            <div class="w-3 h-3 rounded-full bg-amber-300 pulse-emerald"></div>
            <div>
              <div class="text-[10px] text-emerald-100 font-bold uppercase tracking-wider">Pesanan Sedang Aktif</div>
              <div id="active-order-service-name" class="font-bold text-xs">Pijat Tradisional Jawa</div>
            </div>
          </div>
          <div class="flex items-center space-x-1.5">
            <span id="active-order-status-tag" class="px-2 py-0.5 rounded-full text-[10px] font-black bg-white text-emerald-800">MEMUAT</span>
            <i class="fa-solid fa-chevron-right text-xs"></i>
          </div>
        </div>

        <!-- Promo Banner -->
        <div class="p-4">
          <div class="bg-gradient-to-br from-amber-500 via-amber-600 to-orange-600 rounded-2xl p-4 text-white shadow-md relative overflow-hidden">
            <div class="relative z-10 max-w-[70%]">
              <span class="px-2 py-0.5 bg-white/20 text-[10px] font-bold rounded-full uppercase">Pijat Nyaman Di Rumah</span>
              <h2 class="text-base font-extrabold mt-1 leading-snug">Relaksasi Tanpa Perlu Keluar Macet</h2>
              <p class="text-xs text-amber-100 mt-1">Terapis profesional bersertifikat datang ke alamat Anda di area Pekalongan.</p>
            </div>
            <i class="fa-solid fa-spa absolute right-2 -bottom-2 text-7xl text-white/15"></i>
          </div>
        </div>

        <!-- Category Filters -->
        <div class="px-4 mb-2">
          <h3 class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Kategori Layanan</h3>
          <div class="flex space-x-2 overflow-x-auto no-scrollbar pb-1">
            <button onclick="filterServiceCategory('Semua')" id="cat-pill-Semua" class="cat-pill px-3.5 py-1.5 rounded-full text-xs font-bold bg-emerald-600 text-white shadow-md whitespace-nowrap">Semua</button>
            <button onclick="filterServiceCategory('Relaksasi')" id="cat-pill-Relaksasi" class="cat-pill px-3.5 py-1.5 rounded-full text-xs font-bold bg-white text-gray-700 border whitespace-nowrap">Relaksasi</button>
            <button onclick="filterServiceCategory('Refleksi')" id="cat-pill-Refleksi" class="cat-pill px-3.5 py-1.5 rounded-full text-xs font-bold bg-white text-gray-700 border whitespace-nowrap">Refleksi</button>
            <button onclick="filterServiceCategory('Terapi')" id="cat-pill-Terapi" class="cat-pill px-3.5 py-1.5 rounded-full text-xs font-bold bg-white text-gray-700 border whitespace-nowrap">Terapi Khusus</button>
            <button onclick="filterServiceCategory('Ibu & Anak')" id="cat-pill-Ibu-&-Anak" class="cat-pill px-3.5 py-1.5 rounded-full text-xs font-bold bg-white text-gray-700 border whitespace-nowrap">Ibu & Anak</button>
            <button onclick="filterServiceCategory('Kecantikan')" id="cat-pill-Kecantikan" class="cat-pill px-3.5 py-1.5 rounded-full text-xs font-bold bg-white text-gray-700 border whitespace-nowrap">Kecantikan</button>
          </div>
        </div>

        <!-- Services Grid -->
        <div class="p-4">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-extrabold text-gray-900 text-base">Pilihan Layanan Pijat</h3>
          </div>
          <div id="cust-services-grid" class="grid grid-cols-1 gap-3.5">
            <!-- Rendered by JS -->
          </div>
        </div>

        <!-- Nearby Therapists Section -->
        <div class="p-4 bg-emerald-50/50 mt-2">
          <div class="flex items-center justify-between mb-3">
            <div>
              <h3 class="font-extrabold text-gray-900 text-base">Terapis Sekitar Anda</h3>
              <p class="text-xs text-gray-500">Terapis terdekat di sekitar Pekalongan</p>
            </div>
          </div>
          <div id="cust-nearby-therapists" class="flex space-x-3 overflow-x-auto no-scrollbar snap-x pb-2">
            <!-- Rendered by JS -->
          </div>
        </div>
      </div>

      <!-- Tab 2: Customer Orders History -->
      <div id="cust-view-history" class="cust-view-tab hidden p-4">
        <h2 class="text-lg font-black text-gray-900 mb-1">Riwayat Pesanan</h2>
        <p class="text-xs text-gray-500 mb-4">Daftar pesanan aktif dan selesai</p>
        <div id="cust-history-list">
          <!-- Rendered by JS -->
        </div>
      </div>

      <!-- Tab 3: Customer Profile -->
      <div id="cust-view-profile" class="cust-view-tab hidden p-4">
        <div class="bg-white rounded-2xl p-5 border shadow-sm text-center mb-4">
          <div class="w-16 h-16 rounded-full bg-emerald-100 text-emerald-800 text-2xl flex items-center justify-center mx-auto mb-2 font-bold">
            <i class="fa-solid fa-user"></i>
          </div>
          <h3 class="font-extrabold text-gray-900 text-base">Dimas Arya Nugraha</h3>
          <p class="text-xs text-gray-500">dimas.arya@gmail.com • 081987654321</p>
          <span class="inline-block px-3 py-1 bg-emerald-100 text-emerald-800 rounded-full text-[11px] font-bold mt-2">Pelanggan Terverifikasi</span>
        </div>
        <div class="space-y-2 text-sm">
          <button onclick="setCustomerTab('history')" class="w-full p-3.5 bg-white border rounded-xl flex items-center justify-between text-gray-700 font-semibold hover:bg-gray-50">
            <span><i class="fa-solid fa-clock-rotate-left mr-2 text-emerald-600"></i> Riwayat Pesanan Saya</span>
            <i class="fa-solid fa-chevron-right text-gray-400 text-xs"></i>
          </button>
          <button onclick="requestGPS((c, e) => showToast('Lokasi: ' + c.lat + ', ' + c.lng, 'info'))" class="w-full p-3.5 bg-white border rounded-xl flex items-center justify-between text-gray-700 font-semibold hover:bg-gray-50">
            <span><i class="fa-solid fa-location-crosshairs mr-2 text-emerald-600"></i> Kalibrasi Ulang GPS</span>
            <i class="fa-solid fa-chevron-right text-gray-400 text-xs"></i>
          </button>
          <button onclick="logout()" class="w-full p-3.5 bg-rose-50 border border-rose-100 rounded-xl text-rose-700 font-bold flex items-center justify-center gap-2 hover:bg-rose-100 mt-4">
            <i class="fa-solid fa-arrow-right-from-bracket"></i> Keluar dari Akun
          </button>
        </div>
      </div>

      <!-- Customer Bottom Navigation -->
      <nav class="fixed bottom-0 left-0 right-0 max-w-[480px] mx-auto glass-nav z-40 px-6 py-2 flex items-center justify-around">
        <button onclick="setCustomerTab('home')" id="cust-nav-home" class="cust-nav-btn text-emerald-700 font-bold flex flex-col items-center text-[11px] transition-colors">
          <i class="fa-solid fa-house text-lg mb-0.5"></i>
          <span>Beranda</span>
        </button>
        <button onclick="setCustomerTab('history')" id="cust-nav-history" class="cust-nav-btn text-gray-400 flex flex-col items-center text-[11px] transition-colors">
          <i class="fa-solid fa-receipt text-lg mb-0.5"></i>
          <span>Pesanan</span>
        </button>
        <button onclick="setCustomerTab('profile')" id="cust-nav-profile" class="cust-nav-btn text-gray-400 flex flex-col items-center text-[11px] transition-colors">
          <i class="fa-solid fa-circle-user text-lg mb-0.5"></i>
          <span>Profil</span>
        </button>
      </nav>
    </section>

    <!-- ========================================================
         VIEW 3: THERAPIST DASHBOARD & SCREENS
         ======================================================== -->
    <section id="view-therapist" class="app-container hidden pb-24">
      <!-- Top Status Card -->
      <div class="bg-gray-900 text-white p-5 rounded-b-3xl shadow-lg">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center space-x-3">
            <div class="w-11 h-11 rounded-full bg-emerald-500/30 border border-emerald-400 flex items-center justify-center font-bold text-xl text-emerald-400">
              <i class="fa-solid fa-spa"></i>
            </div>
            <div>
              <div class="text-[11px] text-gray-400">Portal Mitra Terapis</div>
              <div id="th-header-name" class="font-extrabold text-base">Budi Santoso, S.Tr.Kes</div>
              <div class="text-xs text-amber-400 flex items-center gap-1 font-semibold">
                <i class="fa-solid fa-star text-[10px]"></i> <span id="th-header-rating">4.9</span>
              </div>
            </div>
          </div>
          <button onclick="logout()" class="text-gray-400 hover:text-white p-2">
            <i class="fa-solid fa-arrow-right-from-bracket"></i>
          </button>
        </div>

        <!-- Online/Offline Switcher -->
        <div class="bg-gray-800/90 rounded-2xl p-3 flex items-center justify-between border border-gray-700">
          <div class="flex items-center space-x-2">
            <i class="fa-solid fa-power-off text-emerald-400"></i>
            <span id="th-status-label" class="text-xs font-bold text-emerald-400">ONLINE (Siap Terima Pesanan)</span>
          </div>
          <label class="relative inline-flex items-center cursor-pointer">
            <input type="checkbox" id="th-status-toggle" onchange="toggleTherapistOnlineStatus()" class="sr-only peer">
            <div class="w-11 h-6 bg-gray-600 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600"></div>
          </label>
        </div>
      </div>

      <!-- Pending Approval Banner -->
      <div id="th-pending-approval-banner" class="m-4 bg-amber-50 border-2 border-amber-300 text-amber-900 p-4 rounded-2xl text-xs hidden">
        <div class="font-bold flex items-center gap-2 text-sm mb-1 text-amber-800">
          <i class="fa-solid fa-triangle-exclamation"></i> Menunggu Persetujuan Admin
        </div>
        <p>Akun terapis Anda sedang dalam proses verifikasi dokumen dan keahlian oleh tim Administrator. Anda belum dapat menerima pesanan pelanggan hingga akun disetujui.</p>
      </div>

      <!-- Tab 1: Therapist Active Job -->
      <div id="th-view-jobs" class="th-view-tab p-4">
        <!-- Active Job Card -->
        <div id="th-active-job-card" class="bg-white rounded-3xl p-5 border-2 border-emerald-500 shadow-xl hidden">
          <div class="flex items-center justify-between pb-3 border-b border-gray-100 mb-3">
            <span class="text-xs font-black text-emerald-700 uppercase tracking-wider">Tugas Pijat Aktif</span>
            <span id="job-order-id" class="text-xs font-bold text-gray-500">ORD-...</span>
          </div>

          <!-- Customer Info -->
          <div class="flex items-start justify-between mb-3">
            <div>
              <h3 id="job-customer-name" class="font-extrabold text-base text-gray-900">Nama Customer</h3>
              <div id="job-customer-phone" class="text-xs text-gray-500">0812...</div>
              <div class="text-xs text-gray-600 mt-1 flex items-start gap-1">
                <i class="fa-solid fa-location-dot text-emerald-600 mt-0.5"></i>
                <span id="job-customer-address">Alamat</span>
              </div>
            </div>
            <div class="text-right">
              <span id="job-distance" class="inline-block px-2.5 py-1 bg-emerald-100 text-emerald-800 rounded-lg text-xs font-bold">1.2 km</span>
            </div>
          </div>

          <!-- Service summary -->
          <div class="bg-gray-50 p-3 rounded-2xl text-xs space-y-1 mb-3">
            <div class="flex justify-between">
              <span class="text-gray-500">Layanan:</span>
              <span id="job-service-name" class="font-bold text-gray-900">Pijat Tradisional</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">Durasi:</span>
              <span id="job-duration" class="font-semibold text-gray-900">90 Menit</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">Catatan Khusus:</span>
              <span id="job-notes" class="text-gray-700 italic">-</span>
            </div>
            <div class="flex justify-between pt-1 border-t border-gray-200">
              <span class="font-bold text-gray-700">Total Tarif:</span>
              <span id="job-total-price" class="font-bold text-emerald-700 text-sm">Rp 140.000</span>
            </div>
          </div>

          <!-- Extra Fee Badge if proposed -->
          <div id="job-extra-fee-status-badge" class="mb-3 p-2.5 bg-amber-50 border border-amber-200 text-amber-800 rounded-xl text-xs font-semibold hidden">
            <!-- Rendered by JS -->
          </div>

          <!-- Quick Action Buttons -->
          <div class="grid grid-cols-3 gap-2 mb-4">
            <button id="job-maps-btn" class="py-2.5 bg-blue-50 hover:bg-blue-100 text-blue-700 text-xs font-bold rounded-xl flex items-center justify-center gap-1.5 transition-colors">
              <i class="fa-solid fa-diamond-turn-right"></i> Navigasi
            </button>
            <button id="job-chat-btn" class="py-2.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 text-xs font-bold rounded-xl flex items-center justify-center gap-1.5 transition-colors">
              <i class="fa-solid fa-comment"></i> Chat
            </button>
            <button id="job-call-btn" class="py-2.5 bg-amber-50 hover:bg-amber-100 text-amber-800 text-xs font-bold rounded-xl flex items-center justify-center gap-1.5 transition-colors">
              <i class="fa-solid fa-phone"></i> Telepon
            </button>
          </div>

          <!-- Propose extra fee button -->
          <button id="job-extra-fee-btn" class="w-full py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-semibold rounded-xl mb-3 flex items-center justify-center gap-1.5 transition-colors">
            <i class="fa-solid fa-plus-circle"></i> Ajukan Biaya Tambahan (Transport/Aroma)
          </button>

          <!-- Step Progression Button -->
          <div id="job-action-buttons">
            <!-- Rendered by JS based on status -->
          </div>
        </div>

        <!-- No Job Idle Card -->
        <div id="th-no-job-card" class="bg-white rounded-3xl p-8 border border-gray-100 shadow-sm text-center">
          <div class="w-16 h-16 rounded-full bg-emerald-50 text-emerald-600 text-3xl flex items-center justify-center mx-auto mb-3">
            <i class="fa-solid fa-bell"></i>
          </div>
          <h3 class="font-extrabold text-gray-900 text-base">Menunggu Pesanan Baru</h3>
          <p class="text-xs text-gray-500 mt-1 max-w-xs mx-auto">Pastikan status Anda ONLINE dan perangkat aktif agar pesanan dari pelanggan sekitar Pekalongan dapat masuk.</p>
        </div>
      </div>

      <!-- Tab 2: Therapist History & Earnings -->
      <div id="th-view-history" class="th-view-tab p-4 hidden">
        <h2 class="text-lg font-black text-gray-900 mb-1">Riwayat & Pendapatan</h2>
        <p class="text-xs text-gray-500 mb-4">Ringkasan hasil kerja Anda</p>

        <div class="grid grid-cols-2 gap-3 mb-4">
          <div class="bg-emerald-600 text-white p-4 rounded-2xl shadow-md">
            <div class="text-[11px] text-emerald-100 font-semibold">Total Pendapatan</div>
            <div id="th-total-earnings" class="text-base font-extrabold mt-1">Rp 0</div>
          </div>
          <div class="bg-white border p-4 rounded-2xl shadow-sm">
            <div class="text-[11px] text-gray-500 font-semibold">Pekerjaan Selesai</div>
            <div id="th-completed-jobs-count" class="text-base font-extrabold text-gray-900 mt-1">0 Sesi</div>
          </div>
        </div>

        <div id="th-history-list">
          <!-- Rendered by JS -->
        </div>
      </div>

      <!-- Therapist Bottom Navigation -->
      <nav class="fixed bottom-0 left-0 right-0 max-w-[480px] mx-auto glass-nav z-40 px-8 py-2 flex items-center justify-around">
        <button onclick="setTherapistTab('jobs')" id="th-nav-jobs" class="th-nav-btn text-emerald-700 font-bold flex flex-col items-center text-[11px] transition-colors">
          <i class="fa-solid fa-briefcase text-lg mb-0.5"></i>
          <span>Pekerjaan</span>
        </button>
        <button onclick="setTherapistTab('history')" id="th-nav-history" class="th-nav-btn text-gray-400 flex flex-col items-center text-[11px] transition-colors">
          <i class="fa-solid fa-chart-line text-lg mb-0.5"></i>
          <span>Pendapatan</span>
        </button>
      </nav>
    </section>

    <!-- ========================================================
         VIEW 4: SUPER ADMINISTRATOR DASHBOARD
         ======================================================== -->
    <section id="view-admin" class="admin-container hidden p-4 md:p-8 pb-20">
      <!-- Admin Security Banner -->
      <div class="bg-amber-50 border-2 border-amber-300 rounded-2xl p-4 mb-6 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-3 text-xs">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-xl bg-amber-500 text-white flex items-center justify-center font-bold text-lg shrink-0">
            <i class="fa-solid fa-shield-halved"></i>
          </div>
          <div>
            <div class="font-extrabold text-amber-900 text-sm">Mode Evaluasi Administrator (calonbikers85@gmail.com)</div>
            <div class="text-amber-800">Akun Admin ini ditanam khusus untuk keperluan demo. Pada aplikasi production, wajib gunakan backend server authentication dan password hashing (bcrypt/argon2).</div>
          </div>
        </div>
        <button onclick="logout()" class="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl shrink-0 transition-colors">
          <i class="fa-solid fa-arrow-right-from-bracket mr-1.5"></i> Logout Admin
        </button>
      </div>

      <!-- Admin Header & Nav -->
      <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4 mb-6">
        <div>
          <h1 class="text-2xl font-black text-gray-900">Panel Kontrol Super Admin</h1>
          <p class="text-xs text-gray-500">Manajemen pesanan, mitra terapis, pelanggan, tarif layanan, dan riwayat audit.</p>
        </div>
        <!-- Admin Tabs -->
        <div class="flex items-center space-x-2 overflow-x-auto no-scrollbar bg-white p-1.5 rounded-2xl border shadow-sm text-xs font-bold">
          <button onclick="switchAdminTab('stats')" id="admin-btn-stats" class="admin-nav-btn px-3 py-2 rounded-xl bg-emerald-600 text-white shadow-md">Statistik</button>
          <button onclick="switchAdminTab('therapists')" id="admin-btn-therapists" class="admin-nav-btn px-3 py-2 rounded-xl text-gray-600 hover:bg-gray-100">Terapis</button>
          <button onclick="switchAdminTab('customers')" id="admin-btn-customers" class="admin-nav-btn px-3 py-2 rounded-xl text-gray-600 hover:bg-gray-100">Pelanggan</button>
          <button onclick="switchAdminTab('orders')" id="admin-btn-orders" class="admin-nav-btn px-3 py-2 rounded-xl text-gray-600 hover:bg-gray-100">Pesanan</button>
          <button onclick="switchAdminTab('services')" id="admin-btn-services" class="admin-nav-btn px-3 py-2 rounded-xl text-gray-600 hover:bg-gray-100">Layanan & Tarif</button>
          <button onclick="switchAdminTab('price-history')" id="admin-btn-price-history" class="admin-nav-btn px-3 py-2 rounded-xl text-gray-600 hover:bg-gray-100">Audit Tarif</button>
          <button onclick="switchAdminTab('reviews')" id="admin-btn-reviews" class="admin-nav-btn px-3 py-2 rounded-xl text-gray-600 hover:bg-gray-100">Ulasan</button>
        </div>
      </div>

      <!-- Admin Section 1: Dashboard Stats -->
      <div id="admin-section-stats" class="admin-section-view">
        <!-- Stat Cards -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <div class="bg-white p-5 rounded-2xl border shadow-sm">
            <div class="text-xs text-gray-500 font-semibold mb-1">Total Pendapatan Selesai</div>
            <div id="admin-stat-revenue" class="text-xl font-black text-emerald-700">Rp 0</div>
            <div class="text-[11px] text-emerald-600 mt-1"><i class="fa-solid fa-arrow-trend-up mr-1"></i>Akumulasi transaksi</div>
          </div>
          <div class="bg-white p-5 rounded-2xl border shadow-sm">
            <div class="text-xs text-gray-500 font-semibold mb-1">Total Pesanan</div>
            <div id="admin-stat-orders" class="text-xl font-black text-gray-900">0</div>
            <div class="text-[11px] text-gray-500 mt-1">Selesai: <span id="admin-stat-completed">0</span> • Aktif: <span id="admin-stat-active">0</span></div>
          </div>
          <div class="bg-white p-5 rounded-2xl border shadow-sm">
            <div class="text-xs text-gray-500 font-semibold mb-1">Total Pelanggan</div>
            <div id="admin-stat-customers" class="text-xl font-black text-gray-900">0</div>
            <div class="text-[11px] text-gray-500 mt-1">Pengguna terdaftar</div>
          </div>
          <div class="bg-white p-5 rounded-2xl border shadow-sm">
            <div class="text-xs text-gray-500 font-semibold mb-1">Mitra Terapis</div>
            <div id="admin-stat-therapists" class="text-xl font-black text-gray-900">0</div>
            <div class="text-[11px] text-gray-500 mt-1">Status persetujuan & aktif</div>
          </div>
        </div>

        <!-- Recaps: Harian, Mingguan, Bulanan -->
        <div class="bg-white p-6 rounded-2xl border shadow-sm mb-6">
          <h3 class="text-base font-extrabold text-gray-900 mb-4 flex items-center gap-2">
            <i class="fa-solid fa-calendar-check text-emerald-600"></i> Rekapan Pekerjaan & Omzet
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="p-4 bg-gray-50 rounded-xl border">
              <div class="text-xs font-bold text-gray-500 uppercase tracking-wider">Rekapan Hari Ini</div>
              <div id="recap-daily-income" class="text-lg font-black text-gray-900 mt-1">Rp 0</div>
              <div id="recap-daily-orders" class="text-xs text-gray-500 mt-0.5">0 Selesai</div>
            </div>
            <div class="p-4 bg-gray-50 rounded-xl border">
              <div class="text-xs font-bold text-gray-500 uppercase tracking-wider">Rekapan 7 Hari Terakhir</div>
              <div id="recap-weekly-income" class="text-lg font-black text-emerald-700 mt-1">Rp 0</div>
              <div id="recap-weekly-orders" class="text-xs text-gray-500 mt-0.5">0 Selesai</div>
            </div>
            <div class="p-4 bg-gray-50 rounded-xl border">
              <div class="text-xs font-bold text-gray-500 uppercase tracking-wider">Rekapan Bulan Ini</div>
              <div id="recap-monthly-income" class="text-lg font-black text-blue-700 mt-1">Rp 0</div>
              <div id="recap-monthly-orders" class="text-xs text-gray-500 mt-0.5">0 Selesai</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Admin Section 2: Therapists Management -->
      <div id="admin-section-therapists" class="admin-section-view hidden">
        <div class="mb-6">
          <h3 class="text-base font-extrabold text-gray-900 mb-3 flex items-center gap-2">
            <i class="fa-solid fa-user-clock text-amber-500"></i> Menunggu Persetujuan Akun
          </h3>
          <div id="admin-pending-therapists-list" class="space-y-3">
            <!-- Rendered by JS -->
          </div>
        </div>
        <div>
          <h3 class="text-base font-extrabold text-gray-900 mb-3 flex items-center gap-2">
            <i class="fa-solid fa-user-check text-emerald-600"></i> Daftar Mitra Terapis Terdaftar
          </h3>
          <div id="admin-approved-therapists-list" class="space-y-3">
            <!-- Rendered by JS -->
          </div>
        </div>
      </div>

      <!-- Admin Section 3: Customers Management -->
      <div id="admin-section-customers" class="admin-section-view hidden">
        <h3 class="text-base font-extrabold text-gray-900 mb-4">Daftar Pelanggan Terdaftar</h3>
        <div id="admin-customers-list" class="space-y-3">
          <!-- Rendered by JS -->
        </div>
      </div>

      <!-- Admin Section 4: All Orders Management -->
      <div id="admin-section-orders" class="admin-section-view hidden">
        <h3 class="text-base font-extrabold text-gray-900 mb-4">Seluruh Pesanan PIJATKU</h3>
        <div class="bg-white rounded-2xl border shadow-sm overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-gray-50 border-b text-[11px] font-bold text-gray-500 uppercase">
                <th class="p-3">ID Pesanan</th>
                <th class="p-3">Pelanggan</th>
                <th class="p-3">Terapis</th>
                <th class="p-3">Layanan</th>
                <th class="p-3">Total</th>
                <th class="p-3">Status</th>
                <th class="p-3 text-right">Aksi</th>
              </tr>
            </thead>
            <tbody id="admin-orders-table-body">
              <!-- Rendered by JS -->
            </tbody>
          </table>
        </div>
      </div>

      <!-- Admin Section 5: Services & Pricing CRUD -->
      <div id="admin-section-services" class="admin-section-view hidden">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-base font-extrabold text-gray-900">Katalog Layanan & Tarif Pijat</h3>
          <button onclick="openAddServiceModal()" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-sm transition-all flex items-center gap-1.5">
            <i class="fa-solid fa-plus"></i> Tambah Layanan Baru
          </button>
        </div>
        <div id="admin-services-list" class="space-y-3">
          <!-- Rendered by JS -->
        </div>
      </div>

      <!-- Admin Section 6: Price Audit Log -->
      <div id="admin-section-price-history" class="admin-section-view hidden">
        <h3 class="text-base font-extrabold text-gray-900 mb-1">Riwayat Audit Perubahan Harga</h3>
        <p class="text-xs text-gray-500 mb-4">Catatan historis setiap perubahan tarif layanan oleh administrator</p>
        <div class="bg-white rounded-2xl border shadow-sm overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-gray-50 border-b text-[11px] font-bold text-gray-500 uppercase">
                <th class="p-3">Tanggal</th>
                <th class="p-3">Layanan</th>
                <th class="p-3">Tarif Lama</th>
                <th class="p-3">Tarif Baru</th>
                <th class="p-3">Alasan Penyesuaian</th>
                <th class="p-3">Diubah Oleh</th>
              </tr>
            </thead>
            <tbody id="admin-price-history-body">
              <!-- Rendered by JS -->
            </tbody>
          </table>
        </div>
      </div>

      <!-- Admin Section 7: Reviews -->
      <div id="admin-section-reviews" class="admin-section-view hidden">
        <h3 class="text-base font-extrabold text-gray-900 mb-4">Ulasan & Rating Pelanggan</h3>
        <div id="admin-reviews-list">
          <!-- Rendered by JS -->
        </div>
      </div>
    </section>
  </main>

  <!-- ========================================================
       MODALS SYSTEM
       ======================================================== -->

  <!-- MODAL: OTP SIMULATION -->
  <div id="otp-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 hidden items-center justify-center p-4">
    <div class="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl text-center">
      <div class="w-14 h-14 bg-emerald-100 text-emerald-700 text-2xl rounded-full flex items-center justify-center mx-auto mb-3">
        <i class="fa-solid fa-message"></i>
      </div>
      <h3 class="text-lg font-black text-gray-900">Verifikasi Nomor HP</h3>
      <p class="text-xs text-gray-500 mt-1">Kode OTP telah dikirimkan ke <span id="otp-phone-display" class="font-bold text-gray-800">0812...</span></p>
      
      <!-- Simulated SMS Display -->
      <div class="my-4 p-3 bg-amber-50 border border-amber-200 rounded-2xl text-xs text-amber-900">
        <div class="font-bold text-[10px] text-amber-700 uppercase tracking-wider mb-1">Simulasi Pesan SMS Masuk:</div>
        <div>"Gunakan kode OTP <b id="otp-code-hint" class="text-base text-emerald-700">123456</b> untuk verifikasi akun PIJATKU Anda. Jangan bagikan kepada siapapun."</div>
      </div>

      <input type="text" id="otp-input" maxlength="6" class="w-full text-center tracking-[0.4em] font-mono text-2xl font-bold py-3 rounded-xl border border-gray-300 focus:ring-2 focus:ring-emerald-500 focus:outline-none mb-4" placeholder="••••••">
      
      <div class="space-y-2">
        <button onclick="verifyOTP()" class="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-sm shadow-md transition-all">
          Verifikasi & Masuk
        </button>
        <button onclick="closeModal('otp-modal')" class="w-full py-2.5 text-gray-500 hover:text-gray-700 text-xs font-semibold">
          Batal
        </button>
      </div>
    </div>
  </div>

  <!-- MODAL: CUSTOMER BOOKING FLOW -->
  <div id="booking-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 hidden items-center justify-center p-3">
    <div class="bg-white rounded-3xl max-w-md w-full max-h-[92vh] flex flex-col shadow-2xl overflow-hidden">
      <!-- Header -->
      <div class="p-4 border-b border-gray-100 flex items-center justify-between bg-emerald-800 text-white">
        <div>
          <div class="text-[10px] text-emerald-200 font-bold uppercase">Konfirmasi Pemesanan</div>
          <h3 id="booking-service-name" class="font-extrabold text-base">Pijat Tradisional Jawa</h3>
        </div>
        <button onclick="closeModal('booking-modal')" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center text-white">
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>

      <!-- Scrollable Body -->
      <div class="p-4 overflow-y-auto space-y-4 text-xs">
        <p id="booking-service-desc" class="text-gray-500 leading-relaxed"></p>

        <!-- Duration Selector -->
        <div>
          <label class="block font-bold text-gray-800 mb-1.5">Pilih Durasi Layanan</label>
          <div id="booking-durations" class="flex space-x-2">
            <!-- Rendered by JS -->
          </div>
        </div>

        <!-- Date & Time -->
        <div class="grid grid-cols-2 gap-2">
          <div>
            <label class="block font-bold text-gray-800 mb-1">Tanggal</label>
            <input type="date" id="booking-date" class="w-full p-2.5 rounded-xl border bg-white font-medium">
          </div>
          <div>
            <label class="block font-bold text-gray-800 mb-1">Jam Kedatangan</label>
            <input type="time" id="booking-time" class="w-full p-2.5 rounded-xl border bg-white font-medium">
          </div>
        </div>

        <!-- Location Pin & Leaflet Map -->
        <div>
          <div class="flex items-center justify-between mb-1.5">
            <label class="font-bold text-gray-800">Alamat & Titik Lokasi Rumah</label>
            <button type="button" onclick="requestGPS((c, e) => { AppState.customerLocation = c; initBookingMap(c.lat, c.lng); showToast(e || 'GPS berhasil dikalibrasi', e ? 'alert' : 'success'); })" class="text-[11px] text-emerald-700 font-bold flex items-center gap-1">
              <i class="fa-solid fa-location-crosshairs"></i> Deteksi GPS
            </button>
          </div>
          <div id="booking-leaflet-map" class="w-full h-36 rounded-xl border mb-2"></div>
          <input type="text" id="booking-address" class="w-full p-2.5 rounded-xl border bg-gray-50" placeholder="Alamat lengkap...">
        </div>

        <!-- Preferred Therapist -->
        <div>
          <label class="block font-bold text-gray-800 mb-1.5">Pilih Mitra Terapis</label>
          <div id="booking-therapists-list" class="space-y-1.5 max-h-40 overflow-y-auto">
            <!-- Rendered by JS -->
          </div>
        </div>

        <!-- Notes -->
        <div>
          <label class="block font-bold text-gray-800 mb-1">Catatan untuk Terapis (Opsional)</label>
          <input type="text" id="booking-notes" class="w-full p-2.5 rounded-xl border" placeholder="Contoh: Fokus punggung bawah, masuk angin, dsb.">
        </div>
      </div>

      <!-- Footer -->
      <div class="p-4 border-t border-gray-100 bg-gray-50 flex items-center justify-between">
        <div>
          <div class="text-[10px] text-gray-400 font-semibold">Total Biaya</div>
          <div id="booking-total-price" class="text-lg font-black text-emerald-700">Rp 100.000</div>
        </div>
        <button onclick="confirmBookingOrder()" class="py-3 px-6 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-xs shadow-md transition-all">
          Konfirmasi & Pesan
        </button>
      </div>
    </div>
  </div>

  <!-- MODAL: REALTIME ORDER TRACKING & LEAFLET MAP -->
  <div id="tracking-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 hidden items-center justify-center p-3">
    <div class="bg-white rounded-3xl max-w-md w-full max-h-[94vh] flex flex-col shadow-2xl overflow-hidden">
      <!-- Header -->
      <div class="p-4 border-b border-gray-100 flex items-center justify-between bg-gray-900 text-white">
        <div>
          <div class="text-[10px] text-emerald-400 font-bold uppercase tracking-wider">Pelacakan Langsung</div>
          <h3 id="track-order-id" class="font-extrabold text-sm">ORD-...</h3>
        </div>
        <button onclick="closeTrackingModal()" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center text-white">
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>

      <!-- Body -->
      <div class="p-4 overflow-y-auto space-y-3.5 text-xs">
        <!-- Status Stepper Header -->
        <div class="flex items-center justify-between">
          <div>
            <div id="track-service-name" class="font-extrabold text-sm text-gray-900">Layanan</div>
            <div class="text-gray-500"><span id="track-service-duration">60 Menit</span> • <span id="track-service-price" class="font-bold text-emerald-700">Rp ...</span></div>
          </div>
          <div id="track-status-badge">Menunggu</div>
        </div>

        <!-- Visual Stepper Progress -->
        <div class="bg-gray-50 p-3 rounded-2xl border">
          <div class="flex justify-between items-center text-[10px] font-bold text-gray-500">
            <div class="flex flex-col items-center">
              <div id="step-dot-menunggu" class="w-6 h-6 rounded-full bg-emerald-600 text-white flex items-center justify-center text-[10px] mb-1">1</div>
              <span>Menunggu</span>
            </div>
            <div class="flex flex-col items-center">
              <div id="step-dot-diterima" class="w-6 h-6 rounded-full bg-gray-200 text-gray-400 flex items-center justify-center text-[10px] mb-1">2</div>
              <span>Diterima</span>
            </div>
            <div class="flex flex-col items-center">
              <div id="step-dot-menuju_lokasi" class="w-6 h-6 rounded-full bg-gray-200 text-gray-400 flex items-center justify-center text-[10px] mb-1">3</div>
              <span>Menuju</span>
            </div>
            <div class="flex flex-col items-center">
              <div id="step-dot-tiba" class="w-6 h-6 rounded-full bg-gray-200 text-gray-400 flex items-center justify-center text-[10px] mb-1">4</div>
              <span>Tiba</span>
            </div>
            <div class="flex flex-col items-center">
              <div id="step-dot-sesi_dimulai" class="w-6 h-6 rounded-full bg-gray-200 text-gray-400 flex items-center justify-center text-[10px] mb-1">5</div>
              <span>Sesi</span>
            </div>
            <div class="flex flex-col items-center">
              <div id="step-dot-selesai" class="w-6 h-6 rounded-full bg-gray-200 text-gray-400 flex items-center justify-center text-[10px] mb-1">6</div>
              <span>Selesai</span>
            </div>
          </div>
        </div>

        <!-- Leaflet Tracking Map -->
        <div class="relative">
          <div id="tracking-leaflet-map" class="w-full h-44 rounded-2xl border shadow-inner"></div>
          <div class="absolute bottom-2 left-2 bg-white/95 backdrop-blur-md px-2.5 py-1 rounded-lg text-[10px] font-bold shadow-md z-[1000] flex items-center gap-1">
            <i class="fa-solid fa-motorcycle text-emerald-600"></i>
            <span id="track-distance-text">Menghitung jarak...</span>
          </div>
        </div>

        <!-- Therapist Info Card -->
        <div class="bg-white rounded-2xl p-3 border shadow-sm flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <img id="track-therapist-photo" src="" class="w-12 h-12 rounded-full object-cover border">
            <div>
              <div id="track-therapist-name" class="font-extrabold text-sm text-gray-900">Nama Terapis</div>
              <div class="text-[11px] text-amber-500 font-semibold flex items-center gap-1">
                <i class="fa-solid fa-star"></i> <span id="track-therapist-rating">4.9</span> • Mitra Terverifikasi
              </div>
            </div>
          </div>
          <div class="flex items-center space-x-1.5">
            <button id="track-chat-btn" class="w-9 h-9 rounded-full bg-emerald-50 text-emerald-700 hover:bg-emerald-100 flex items-center justify-center font-bold">
              <i class="fa-solid fa-comment"></i>
            </button>
            <button id="track-call-btn" class="w-9 h-9 rounded-full bg-blue-50 text-blue-700 hover:bg-blue-100 flex items-center justify-center font-bold">
              <i class="fa-solid fa-phone"></i>
            </button>
            <button id="track-maps-btn" class="w-9 h-9 rounded-full bg-gray-100 text-gray-700 hover:bg-gray-200 flex items-center justify-center font-bold">
              <i class="fa-solid fa-diamond-turn-right"></i>
            </button>
          </div>
        </div>

        <!-- Live Session Countdown Timer -->
        <div id="track-timer-container" class="bg-gradient-to-r from-emerald-600 to-teal-700 text-white p-4 rounded-2xl text-center shadow-md hidden">
          <div class="text-[11px] text-emerald-200 font-bold uppercase tracking-wider">Waktu Sesi Pijat Berjalan</div>
          <div id="session-timer-text" class="text-3xl font-black font-mono my-1 timer-display">60:00</div>
          <div class="text-[10px] text-emerald-100">Selamat menikmati relaksasi pijat di rumah Anda</div>
        </div>

        <!-- Extra Fee Approval Card (Customer Action) -->
        <div id="track-extra-fee-card" class="bg-amber-50 border-2 border-amber-300 rounded-2xl p-3.5 text-amber-900 hidden">
          <div class="font-bold text-xs flex items-center gap-1.5 mb-1">
            <i class="fa-solid fa-file-invoice-dollar text-amber-600"></i> Pengajuan Biaya Tambahan
          </div>
          <div id="extra-fee-status" class="text-xs mb-2">Terapis mengajukan biaya tambahan:</div>
          <div class="bg-white p-2.5 rounded-xl border border-amber-200 mb-3 flex items-center justify-between">
            <span id="extra-fee-reason" class="text-xs font-medium text-gray-700">Alasan</span>
            <span id="extra-fee-amount" class="text-xs font-extrabold text-amber-700">Rp 15.000</span>
          </div>
          <div id="extra-fee-actions" class="flex gap-2">
            <button onclick="respondExtraFee('approve')" class="flex-1 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-xs shadow-sm">
              <i class="fa-solid fa-check mr-1"></i> Setujui Biaya
            </button>
            <button onclick="respondExtraFee('reject')" class="flex-1 py-2 bg-rose-100 hover:bg-rose-200 text-rose-800 font-bold rounded-xl text-xs">
              <i class="fa-solid fa-xmark mr-1"></i> Tolak Biaya
            </button>
          </div>
        </div>

        <!-- Review Prompt -->
        <div id="track-review-prompt" class="bg-blue-50 border border-blue-200 rounded-2xl p-3.5 text-center hidden">
          <div class="font-bold text-sm text-blue-900 mb-1">Sesi Pijat Telah Selesai!</div>
          <p class="text-xs text-blue-700 mb-3">Bagikan pengalaman relaksasi Anda untuk membantu terapis kami.</p>
          <button onclick="openReviewModal(currentTrackingOrderId)" class="py-2.5 px-5 bg-amber-500 hover:bg-amber-600 text-white font-bold rounded-xl text-xs shadow-sm">
            <i class="fa-solid fa-star mr-1"></i> Beri Rating & Review
          </button>
        </div>

        <!-- Destination Address -->
        <div class="text-[11px] text-gray-400 flex items-start gap-1.5">
          <i class="fa-solid fa-house-chimney text-emerald-600 mt-0.5"></i>
          <span id="track-customer-address">Alamat</span>
        </div>
      </div>
    </div>
  </div>

  <!-- MODAL: REALTIME CHAT -->
  <div id="chat-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 hidden items-center justify-center p-3">
    <div class="bg-white rounded-3xl max-w-md w-full h-[85vh] flex flex-col shadow-2xl overflow-hidden">
      <!-- Chat Header -->
      <div class="p-3.5 border-b border-gray-100 flex items-center justify-between bg-emerald-800 text-white">
        <div class="flex items-center space-x-2.5">
          <div class="w-8 h-8 rounded-full bg-emerald-600 flex items-center justify-center text-white font-bold text-xs">
            <i class="fa-solid fa-comments"></i>
          </div>
          <div>
            <h3 id="chat-partner-name" class="font-bold text-sm">Obrolan</h3>
            <span id="chat-order-tag" class="text-[10px] text-emerald-200">ORD-...</span>
          </div>
        </div>
        <button onclick="closeModal('chat-modal')" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center text-white">
          <i class="fa-solid fa-xmark"></i>
        </button>
      </div>

      <!-- Quick Chips -->
      <div class="px-3 py-2 bg-gray-50 border-b flex space-x-1.5 overflow-x-auto no-scrollbar text-[11px]">
        <button onclick="sendChatMessage('Halo, apakah posisi sudah dekat?')" class="px-2.5 py-1 bg-white border rounded-full text-gray-700 whitespace-nowrap hover:bg-emerald-50">Posisi sudah dekat?</button>
        <button onclick="sendChatMessage('Saya sudah siap di rumah.')" class="px-2.5 py-1 bg-white border rounded-full text-gray-700 whitespace-nowrap hover:bg-emerald-50">Siap di rumah</button>
        <button onclick="sendChatMessage('Mohon bawa minyak tanpa aroma.')" class="px-2.5 py-1 bg-white border rounded-full text-gray-700 whitespace-nowrap hover:bg-emerald-50">Minyak tanpa aroma</button>
      </div>

      <!-- Messages Area -->
      <div id="chat-messages-container" class="flex-1 p-4 overflow-y-auto bg-gray-50/50">
        <!-- Rendered by JS -->
      </div>

      <!-- Input Area -->
      <div class="p-3 bg-white border-t flex items-center space-x-2">
        <input type="text" id="chat-input" onkeydown="if(event.key === 'Enter') sendChatMessage()" placeholder="Ketik pesan..." class="flex-1 px-4 py-2.5 rounded-full border border-gray-300 text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-none">
        <button onclick="sendChatMessage()" class="w-10 h-10 rounded-full bg-emerald-600 hover:bg-emerald-700 text-white flex items-center justify-center font-bold shadow-md">
          <i class="fa-solid fa-paper-plane text-xs"></i>
        </button>
      </div>
    </div>
  </div>

  <!-- MODAL: DIRECT CALL SIMULATION -->
  <div id="call-modal" class="fixed inset-0 bg-gray-950/95 backdrop-blur-md z-50 hidden items-center justify-center p-4">
    <div class="max-w-sm w-full text-center text-white">
      <div class="w-24 h-24 rounded-full bg-gradient-to-br from-emerald-500 to-teal-700 mx-auto flex items-center justify-center text-4xl shadow-2xl mb-4 border-4 border-white/20">
        <i class="fa-solid fa-user"></i>
      </div>
      <h3 id="call-contact-name" class="font-extrabold text-xl">Kontak</h3>
      <div id="call-contact-phone" class="text-xs text-gray-400 mt-0.5">0812...</div>
      <div id="call-status-text" class="text-sm font-semibold text-emerald-400 mt-3 animate-pulse">Memanggil...</div>
      <div id="call-timer" class="text-2xl font-mono font-bold text-white mt-2 hidden">00:00</div>

      <div class="mt-12 flex items-center justify-center space-x-6">
        <button onclick="endCall()" class="w-16 h-16 rounded-full bg-rose-600 hover:bg-rose-700 flex items-center justify-center text-2xl text-white shadow-xl transition-transform active:scale-95">
          <i class="fa-solid fa-phone-slash"></i>
        </button>
      </div>
      
      <div class="mt-8 text-[11px] text-gray-500">
        Simulasi panggilan suara in-app PIJATKU.
      </div>
    </div>
  </div>

  <!-- MODAL: RATING & REVIEW -->
  <div id="review-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 hidden items-center justify-center p-4">
    <div class="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl text-center">
      <input type="hidden" id="review-order-id">
      <div class="w-14 h-14 bg-amber-100 text-amber-600 text-2xl rounded-full flex items-center justify-center mx-auto mb-3">
        <i class="fa-solid fa-star"></i>
      </div>
      <h3 class="text-lg font-black text-gray-900">Beri Ulasan Terapis</h3>
      <p class="text-xs text-gray-500 mt-1">Bagaimana pelayanan dan pijatan terapis Anda?</p>

      <!-- 5 Interactive Stars -->
      <div class="flex items-center justify-center space-x-2 my-4">
        <i id="star-1" onclick="setStarRating(1)" class="fa-solid fa-star text-2xl text-amber-400 cursor-pointer"></i>
        <i id="star-2" onclick="setStarRating(2)" class="fa-solid fa-star text-2xl text-amber-400 cursor-pointer"></i>
        <i id="star-3" onclick="setStarRating(3)" class="fa-solid fa-star text-2xl text-amber-400 cursor-pointer"></i>
        <i id="star-4" onclick="setStarRating(4)" class="fa-solid fa-star text-2xl text-amber-400 cursor-pointer"></i>
        <i id="star-5" onclick="setStarRating(5)" class="fa-solid fa-star text-2xl text-amber-400 cursor-pointer"></i>
      </div>

      <textarea id="review-comment" rows="3" class="w-full p-3 rounded-xl border border-gray-300 text-xs focus:ring-2 focus:ring-amber-500 focus:outline-none mb-4" placeholder="Tulis ulasan Anda (contoh: Pijatan sangat pas, tepat waktu, ramah...)"></textarea>

      <div class="space-y-2">
        <button onclick="submitReview()" class="w-full py-3 bg-amber-500 hover:bg-amber-600 text-white font-bold rounded-xl text-sm shadow-md transition-all">
          Kirim Ulasan & Rating
        </button>
        <button onclick="closeModal('review-modal')" class="w-full py-2 text-gray-500 text-xs">
          Lewati
        </button>
      </div>
    </div>
  </div>

  <!-- MODAL: THERAPIST INCOMING ORDER POPUP -->
  <div id="incoming-order-modal" class="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 hidden items-center justify-center p-4">
    <div class="bg-white rounded-3xl p-5 max-w-sm w-full shadow-2xl text-center border-4 border-emerald-500">
      <div class="w-14 h-14 bg-emerald-100 text-emerald-700 text-2xl rounded-full flex items-center justify-center mx-auto mb-2 animate-bounce">
        <i class="fa-solid fa-bell"></i>
      </div>
      <h3 class="text-lg font-black text-gray-900">Pesanan Pelanggan Masuk!</h3>
      <p class="text-xs text-gray-500">Pelanggan baru membutuhkan layanan pijat Anda</p>

      <div class="bg-gray-50 rounded-2xl p-3.5 my-3 text-left text-xs space-y-1.5 border">
        <div class="flex justify-between">
          <span class="text-gray-500">Nama Customer:</span>
          <span id="incoming-customer-name" class="font-bold text-gray-900">Dimas Arya</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500">Layanan:</span>
          <span id="incoming-service-name" class="font-bold text-emerald-800">Pijat Tradisional</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500">Durasi:</span>
          <span id="incoming-service-duration" class="font-bold text-gray-900">90 Menit</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500">Jarak ke Customer:</span>
          <span id="incoming-distance" class="font-bold text-blue-700">1.2 km</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500">Alamat:</span>
          <span id="incoming-customer-address" class="font-medium text-gray-700 truncate max-w-[180px]">Pekalongan</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500">Catatan:</span>
          <span id="incoming-notes" class="text-gray-700 italic">-</span>
        </div>
        <div class="flex justify-between pt-1 border-t">
          <span class="font-bold text-gray-700">Total Tarif:</span>
          <span id="incoming-service-price" class="font-black text-emerald-700 text-sm">Rp 140.000</span>
        </div>
      </div>

      <div class="flex gap-2">
        <button onclick="acceptIncomingOrder()" class="flex-1 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-xs shadow-md">
          <i class="fa-solid fa-check mr-1"></i> Terima Pesanan
        </button>
        <button onclick="rejectIncomingOrder()" class="flex-1 py-3 bg-rose-100 hover:bg-rose-200 text-rose-800 font-bold rounded-xl text-xs">
          <i class="fa-solid fa-xmark mr-1"></i> Tolak Pesanan
        </button>
      </div>
    </div>
  </div>

  <!-- MODAL: PROPOSE EXTRA FEE (THERAPIST) -->
  <div id="extra-fee-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 hidden items-center justify-center p-4">
    <div class="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl">
      <input type="hidden" id="propose-fee-order-id">
      <h3 class="text-base font-black text-gray-900 mb-1">Ajukan Biaya Tambahan</h3>
      <p class="text-xs text-gray-500 mb-4">Biaya tambahan hanya akan ditagihkan jika disetujui pelanggan.</p>

      <div class="space-y-3 text-xs mb-4">
        <div>
          <label class="block font-semibold text-gray-700 mb-1">Nominal Biaya Tambahan (Rp)</label>
          <input type="number" id="propose-fee-amount" step="5000" min="5000" class="w-full p-2.5 rounded-xl border" placeholder="Contoh: 15000">
        </div>
        <div>
          <label class="block font-semibold text-gray-700 mb-1">Alasan Pengajuan</label>
          <input type="text" id="propose-fee-reason" class="w-full p-2.5 rounded-xl border" placeholder="Contoh: Ongkos jarak jauh malam hari / Minyak cendana spesial">
        </div>
      </div>

      <div class="flex gap-2">
        <button onclick="submitProposeExtraFee()" class="flex-1 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-xs">
          Kirim Pengajuan
        </button>
        <button onclick="closeModal('extra-fee-modal')" class="py-2.5 px-4 bg-gray-100 text-gray-600 font-bold rounded-xl text-xs">
          Batal
        </button>
      </div>
    </div>
  </div>

  <!-- MODAL: ADMIN SERVICE ADD/EDIT -->
  <div id="service-modal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 hidden items-center justify-center p-4">
    <div class="bg-white rounded-3xl p-6 max-w-md w-full shadow-2xl">
      <h3 id="service-modal-title" class="text-base font-black text-gray-900 mb-3">Kelola Layanan</h3>
      <form onsubmit="saveServiceModal(event)" class="space-y-3 text-xs">
        <input type="hidden" id="edit-service-id">
        <div>
          <label class="block font-semibold text-gray-700 mb-1">Nama Layanan</label>
          <input type="text" id="service-form-name" required class="w-full p-2.5 rounded-xl border">
        </div>
        <div class="grid grid-cols-2 gap-2">
          <div>
            <label class="block font-semibold text-gray-700 mb-1">Kategori</label>
            <select id="service-form-category" class="w-full p-2.5 rounded-xl border bg-white">
              <option value="Relaksasi">Relaksasi</option>
              <option value="Refleksi">Refleksi</option>
              <option value="Terapi">Terapi</option>
              <option value="Ibu & Anak">Ibu & Anak</option>
              <option value="Kecantikan">Kecantikan</option>
            </select>
          </div>
          <div>
            <label class="block font-semibold text-gray-700 mb-1">Tagline</label>
            <input type="text" id="service-form-tagline" class="w-full p-2.5 rounded-xl border" placeholder="Contoh: Terapi otot kaku">
          </div>
        </div>
        <div>
          <label class="block font-semibold text-gray-700 mb-1">Deskripsi Lengkap</label>
          <textarea id="service-form-desc" rows="2" class="w-full p-2.5 rounded-xl border"></textarea>
        </div>
        <div class="p-3 bg-gray-50 rounded-xl border space-y-2">
          <div class="font-bold text-gray-800">Tarif Berdasarkan Durasi</div>
          <div class="grid grid-cols-3 gap-2">
            <div>
              <label class="text-[10px] text-gray-500">60 Menit (Rp)</label>
              <input type="number" id="service-form-p60" step="5000" class="w-full p-1.5 rounded border">
            </div>
            <div>
              <label class="text-[10px] text-gray-500">90 Menit (Rp)</label>
              <input type="number" id="service-form-p90" step="5000" class="w-full p-1.5 rounded border">
            </div>
            <div>
              <label class="text-[10px] text-gray-500">120 Menit (Rp)</label>
              <input type="number" id="service-form-p120" step="5000" class="w-full p-1.5 rounded border">
            </div>
          </div>
        </div>
        <div class="flex gap-2 pt-2">
          <button type="submit" class="flex-1 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-xs shadow-md">
            Simpan Perubahan Tarif
          </button>
          <button type="button" onclick="closeModal('service-modal')" class="py-3 px-4 bg-gray-100 text-gray-600 font-bold rounded-xl text-xs">
            Batal
          </button>
        </div>
      </form>
    </div>
  </div>
`;
