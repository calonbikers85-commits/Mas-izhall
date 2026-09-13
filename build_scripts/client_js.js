// Client-side JavaScript for PIJATKU
module.exports = `
/* =========================================================
   PIJATKU - Application State & LocalStorage Manager
   ========================================================= */

// Security Note:
// AKUN ADMIN BAWAAN (calonbikers85@gmail.com / Pekalongan27)
// Disediakan semata-mata untuk keperluan DEMO & EVALUASI prototipe.
// Pada aplikasi produksi, otentikasi wajib menggunakan backend terenkripsi,
// HTTPS, JWT/Session token aman, dan password hashing standar industri (Argon2/bcrypt).

const STORAGE_KEYS = {
  USERS: 'users',
  CUSTOMERS: 'customers',
  THERAPISTS: 'therapists',
  SERVICES: 'services',
  ORDERS: 'orders',
  MESSAGES: 'messages',
  NOTIFICATIONS: 'notifications',
  REVIEWS: 'reviews',
  PRICE_HISTORY: 'priceHistory',
  ACTIVE_SESSION: 'pijatku_active_session'
};

// Default Pekalongan Coordinate Fallback
const PEKALONGAN_COORDS = {
  lat: -6.8886,
  lng: 109.6753,
  name: 'Alun-Alun Pekalongan'
};

// State Store
const AppState = {
  currentRole: null, // 'customer', 'therapist', 'admin'
  currentUser: null,
  activeOrder: null,
  activeTab: 'home',
  customerLocation: { ...PEKALONGAN_COORDS },
  therapistLocation: { ...PEKALONGAN_COORDS },
  trackingMap: null,
  bookingMap: null,
  adminTab: 'stats',
  activeChatOrderId: null,
  selectedCategory: 'Semua',
  sessionTimerInterval: null,
  pendingOTP: null
};

// Database Initialization
function initDatabase() {
  if (!localStorage.getItem(STORAGE_KEYS.SERVICES)) {
    localStorage.setItem(STORAGE_KEYS.SERVICES, JSON.stringify(DEFAULT_DATA.services));
  }
  if (!localStorage.getItem(STORAGE_KEYS.THERAPISTS)) {
    localStorage.setItem(STORAGE_KEYS.THERAPISTS, JSON.stringify(DEFAULT_DATA.therapists));
  }
  if (!localStorage.getItem(STORAGE_KEYS.CUSTOMERS)) {
    localStorage.setItem(STORAGE_KEYS.CUSTOMERS, JSON.stringify(DEFAULT_DATA.customers));
  }
  if (!localStorage.getItem(STORAGE_KEYS.ORDERS)) {
    localStorage.setItem(STORAGE_KEYS.ORDERS, JSON.stringify(DEFAULT_DATA.orders));
  }
  if (!localStorage.getItem(STORAGE_KEYS.REVIEWS)) {
    localStorage.setItem(STORAGE_KEYS.REVIEWS, JSON.stringify(DEFAULT_DATA.reviews));
  }
  if (!localStorage.getItem(STORAGE_KEYS.PRICE_HISTORY)) {
    localStorage.setItem(STORAGE_KEYS.PRICE_HISTORY, JSON.stringify(DEFAULT_DATA.priceHistory));
  }
  if (!localStorage.getItem(STORAGE_KEYS.MESSAGES)) {
    localStorage.setItem(STORAGE_KEYS.MESSAGES, JSON.stringify(DEFAULT_DATA.messages));
  }
  if (!localStorage.getItem(STORAGE_KEYS.NOTIFICATIONS)) {
    localStorage.setItem(STORAGE_KEYS.NOTIFICATIONS, JSON.stringify(DEFAULT_DATA.notifications));
  }
  
  // Restore session if exists
  const savedSession = localStorage.getItem(STORAGE_KEYS.ACTIVE_SESSION);
  if (savedSession) {
    try {
      const session = JSON.parse(savedSession);
      AppState.currentRole = session.role;
      AppState.currentUser = session.user;
    } catch(e) {
      localStorage.removeItem(STORAGE_KEYS.ACTIVE_SESSION);
    }
  } else {
    // Default to customer Dimas for seamless initial preview
    autoLoginDemo('customer');
  }
}

// Data Accessors
function getDB(key) {
  try {
    return JSON.parse(localStorage.getItem(key)) || [];
  } catch (e) {
    console.error('Error reading localStorage key:', key, e);
    return [];
  }
}

function saveDB(key, data) {
  try {
    localStorage.setItem(key, JSON.stringify(data));
    // Trigger cross-view sync event
    window.dispatchEvent(new Event('pijatku_data_updated'));
  } catch (e) {
    console.error('Error saving to localStorage key:', key, e);
  }
}

// Distance Calculation (Haversine in km)
function calculateDistance(lat1, lon1, lat2, lon2) {
  if (!lat1 || !lon1 || !lat2 || !lon2) return 1.5;
  const R = 6371; // Radius of earth in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  const d = R * c;
  return Math.round(d * 10) / 10; // 1 decimal place
}

// Sound Synthesizer for notifications
function playChime(type = 'normal') {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    
    if (type === 'incoming') {
      // Ringtone chime
      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
      osc.frequency.setValueAtTime(880, ctx.currentTime + 0.15); // A5
      gain.gain.setValueAtTime(0.2, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.4);
      osc.start();
      osc.stop(ctx.currentTime + 0.4);
    } else {
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(523.25, ctx.currentTime); // C5
      osc.frequency.setValueAtTime(659.25, ctx.currentTime + 0.1); // E5
      gain.gain.setValueAtTime(0.15, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3);
      osc.start();
      osc.stop(ctx.currentTime + 0.3);
    }
  } catch(e) {}
}

// Toast Notifications
function showToast(message, type = 'info', title = '') {
  playChime(type === 'alert' ? 'incoming' : 'normal');
  const toastContainer = document.getElementById('toast-container');
  if (!toastContainer) return;
  
  const toast = document.createElement('div');
  const bgClass = type === 'success' ? 'bg-emerald-600 text-white' : 
                  type === 'alert' ? 'bg-amber-600 text-white' :
                  type === 'error' ? 'bg-rose-600 text-white' : 'bg-gray-900 text-white';
  
  toast.className = \`transform transition-all duration-300 ease-out translate-y-2 opacity-0 p-4 rounded-xl shadow-xl flex items-start space-x-3 mb-2 \${bgClass} text-sm max-w-sm w-full\`;
  
  const icon = type === 'success' ? 'fa-circle-check' :
               type === 'alert' ? 'fa-bell' :
               type === 'error' ? 'fa-circle-exclamation' : 'fa-circle-info';
               
  toast.innerHTML = \`
    <i class="fa-solid \${icon} text-lg mt-0.5"></i>
    <div class="flex-1">
      \${title ? \`<div class="font-bold text-xs uppercase tracking-wider mb-0.5">\${escapeHtml(title)}</div>\` : ''}
      <div class="leading-snug">\${escapeHtml(message)}</div>
    </div>
    <button onclick="this.parentElement.remove()" class="text-white/80 hover:text-white p-1">
      <i class="fa-solid fa-xmark"></i>
    </button>
  \`;
  
  toastContainer.appendChild(toast);
  setTimeout(() => {
    toast.classList.remove('translate-y-2', 'opacity-0');
  }, 20);
  
  setTimeout(() => {
    if (toast.parentElement) {
      toast.classList.add('opacity-0', 'translate-y-2');
      setTimeout(() => toast.remove(), 300);
    }
  }, 4500);
}

// Text Sanitization to prevent XSS
function escapeHtml(str) {
  if (typeof str !== 'string') return str || '';
  return str.replace(/[&<>'"]/g, 
    tag => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      "'": '&#39;',
      '"': '&quot;'
    }[tag] || tag)
  );
}

function formatRupiah(num) {
  return 'Rp ' + Number(num || 0).toLocaleString('id-ID');
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' }) + ' ' +
         d.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' });
}

// Save Notification in LocalStorage
function addNotification(targetRole, targetUserId, title, message) {
  const notifs = getDB(STORAGE_KEYS.NOTIFICATIONS);
  const newNotif = {
    id: 'notif-' + Date.now(),
    targetRole,
    targetUserId,
    title,
    message,
    timestamp: new Date().toISOString(),
    read: false
  };
  notifs.unshift(newNotif);
  saveDB(STORAGE_KEYS.NOTIFICATIONS, notifs);
  
  if (AppState.currentRole === targetRole) {
    showToast(message, 'alert', title);
  }
}
`;
