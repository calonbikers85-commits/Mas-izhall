// Data Models and Default Initial LocalStorage Data for PIJATKU

const defaultServices = [
  {
    id: "srv-1",
    name: "Pijat Tradisional Jawa",
    tagline: "Relaksasi otot kaku dan melancarkan peredaran darah",
    description: "Kombinasi pijat urut tradisional khas Jawa dengan minyak aromaterapi alami. Efektif meredakan pegal linu, kelelahan fisik, dan masuk angin.",
    basePrice: 100000,
    durations: [
      { minutes: 60, price: 100000 },
      { minutes: 90, price: 140000 },
      { minutes: 120, price: 180000 }
    ],
    icon: "fa-spa",
    badge: "Terfavorit",
    category: "Relaksasi"
  },
  {
    id: "srv-2",
    name: "Refleksi Kaki & Tangan",
    tagline: "Titik akupresur untuk vitalitas dan stimulasi organ",
    description: "Terapi pijat titik refleksi telapak kaki dan tangan untuk menstimulasi fungsi organ tubuh, mengurangi stres, dan meningkatkan imunitas.",
    basePrice: 85000,
    durations: [
      { minutes: 60, price: 85000 },
      { minutes: 90, price: 120000 }
    ],
    icon: "fa-shoe-prints",
    badge: "Populer",
    category: "Refleksi"
  },
  {
    id: "srv-3",
    name: "Deep Tissue & Sport Massage",
    tagline: "Tekanan terfokus untuk pemulihan cedera dan otot tegang",
    description: "Terapi dengan tekanan mendalam untuk melepaskan simpul otot (muscle knot), asam laktat, dan memulihkan kondisi pasca berolahraga berat.",
    basePrice: 130000,
    durations: [
      { minutes: 60, price: 130000 },
      { minutes: 90, price: 180000 },
      { minutes: 120, price: 230000 }
    ],
    icon: "fa-dumbbell",
    badge: "Intensif",
    category: "Terapi"
  },
  {
    id: "srv-4",
    name: "Aromaterapi & Lulur Keraton",
    tagline: "Pijat lembut dengan minyak esensial dan eksfoliasi kulit",
    description: "Perawatan memanjakan tubuh dengan scrub rempah alami untuk mengangkat sel kulit mati, dilanjutkan pijat aromaterapi bunga melati dan lavender.",
    basePrice: 140000,
    durations: [
      { minutes: 90, price: 140000 },
      { minutes: 120, price: 195000 }
    ],
    icon: "fa-feather-pointed",
    badge: "Relaksasi Mewah",
    category: "Kecantikan"
  },
  {
    id: "srv-5",
    name: "Pijat Ibu Hamil & Pasca Salin",
    tagline: "Teknik khusus bersertifikat aman untuk calon ibu",
    description: "Pijat lembut posisi menyamping untuk meredakan nyeri pinggang, bengkak kaki, dan ketegangan pada ibu hamil dan pemulihan setelah melahirkan.",
    basePrice: 120000,
    durations: [
      { minutes: 60, price: 120000 },
      { minutes: 90, price: 165000 }
    ],
    icon: "fa-person-pregnant",
    badge: "Khusus Ibu",
    category: "Ibu & Anak"
  },
  {
    id: "srv-6",
    name: "Totok Wajah & Bekam Kering",
    tagline: "Segarkan aura wajah dan lancarkan sirkulasi kepala",
    description: "Akupresur wajah untuk peremajaan kulit dan meredakan migrain/sinus, dikombinasikan bekam angin kering untuk membuang toksin tubuh.",
    basePrice: 95000,
    durations: [
      { minutes: 60, price: 95000 },
      { minutes: 90, price: 135000 }
    ],
    icon: "fa-face-smile",
    badge: "Segar",
    category: "Terapi"
  }
];

const defaultTherapists = [
  {
    id: "th-1",
    name: "Budi Santoso, S.Tr.Kes",
    email: "budi.terapis@gmail.com",
    phone: "081234567891",
    password: "terapisbudi123",
    role: "therapist",
    gender: "Laki-laki",
    specialties: ["Pijat Tradisional Jawa", "Deep Tissue & Sport Massage", "Refleksi Kaki"],
    experienceYears: 7,
    rating: 4.9,
    reviewCount: 48,
    status: "approved", // approved | pending | rejected
    isActive: true, // admin enable/disable
    isOnline: true, // therapist toggle
    photo: "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=400&auto=format&fit=crop&q=80",
    address: "Jl. Hayam Wuruk No. 14, Pekalongan Barat",
    lat: -6.8860,
    lng: 109.6710,
    bio: "Spesialis pemulihan otot kaku dan saraf terjepit dengan pengalaman klinik dan panggilan terpercaya."
  },
  {
    id: "th-2",
    name: "Siti Rahmawati",
    email: "siti.rahma@gmail.com",
    phone: "081234567892",
    password: "terapissiti123",
    role: "therapist",
    gender: "Perempuan",
    specialties: ["Pijat Ibu Hamil & Pasca Salin", "Aromaterapi & Lulur Keraton", "Pijat Tradisional Jawa"],
    experienceYears: 5,
    rating: 4.8,
    reviewCount: 36,
    status: "approved",
    isActive: true,
    isOnline: true,
    photo: "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80",
    address: "Jl. Sultan Agung No. 28, Pekalongan Timur",
    lat: -6.8920,
    lng: 109.6800,
    bio: "Sertifikasi kebidanan dan perawatan relaksasi ibu hamil. Lembut, higienis, dan ramah."
  },
  {
    id: "th-3",
    name: "Ahmad Fauzi",
    email: "ahmad.fauzi@gmail.com",
    phone: "081234567893",
    password: "terapisahmad123",
    role: "therapist",
    gender: "Laki-laki",
    specialties: ["Totok Wajah & Bekam Kering", "Refleksi Kaki & Tangan", "Pijat Tradisional Jawa"],
    experienceYears: 4,
    rating: 4.7,
    reviewCount: 22,
    status: "approved",
    isActive: true,
    isOnline: false,
    photo: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
    address: "Jl. Progo No. 5, Pekalongan Utara",
    lat: -6.8790,
    lng: 109.6730,
    bio: "Ahli totok aura wajah dan bekam sunnah tanpa rasa sakit. Bekerja profesional dan tepat waktu."
  },
  {
    id: "th-4",
    name: "Dewi Lestari",
    email: "dewi.lestari@gmail.com",
    phone: "081234567894",
    password: "terapisdewi123",
    role: "therapist",
    gender: "Perempuan",
    specialties: ["Aromaterapi & Lulur Keraton", "Pijat Tradisional Jawa"],
    experienceYears: 3,
    rating: 5.0,
    reviewCount: 1,
    status: "pending", // Waiting admin approval
    isActive: true,
    isOnline: false,
    photo: "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=400&auto=format&fit=crop&q=80",
    address: "Jl. Kurinci No. 12, Pekalongan Selatan",
    lat: -6.9010,
    lng: 109.6680,
    bio: "Terapis baru dengan sertifikasi SPA Nasional 2024. Menunggu verifikasi admin."
  }
];

const defaultCustomers = [
  {
    id: "cust-1",
    name: "Dimas Arya Nugraha",
    email: "dimas.arya@gmail.com",
    phone: "081987654321",
    password: "customerdimas123",
    role: "customer",
    isVerified: true,
    address: "Perumahan Griya Tirto Indah Blok C No. 4, Pekalongan",
    lat: -6.8895,
    lng: 109.6745,
    createdAt: "2026-08-10T10:00:00Z"
  },
  {
    id: "cust-2",
    name: "Anisa Putri Wijaya",
    email: "anisa.putri@gmail.com",
    phone: "081987654322",
    password: "customeranisa123",
    role: "customer",
    isVerified: true,
    address: "Jl. Veteran No. 15, Pekalongan",
    lat: -6.8870,
    lng: 109.6780,
    createdAt: "2026-08-20T14:30:00Z"
  }
];

const defaultOrders = [
  {
    id: "ORD-20260910-001",
    customerId: "cust-1",
    customerName: "Dimas Arya Nugraha",
    customerPhone: "081987654321",
    customerAddress: "Perumahan Griya Tirto Indah Blok C No. 4, Pekalongan",
    customerLat: -6.8895,
    customerLng: 109.6745,
    therapistId: "th-1",
    therapistName: "Budi Santoso, S.Tr.Kes",
    therapistPhone: "081234567891",
    serviceId: "srv-1",
    serviceName: "Pijat Tradisional Jawa",
    durationMinutes: 90,
    basePrice: 140000,
    extraFee: {
      amount: 15000,
      reason: "Minyak Aromaterapi Esensial Cendana Premium",
      status: "approved" // pending | approved | rejected | null
    },
    totalPrice: 155000,
    status: "selesai", // menunggu, diterima, menuju_lokasi, tiba, sesi_dimulai, selesai, dibatalkan
    scheduledDate: "2026-09-10",
    scheduledTime: "19:00",
    notes: "Tolong fokus di pundak dan punggung bawah",
    createdAt: "2026-09-10T18:45:00Z",
    acceptedAt: "2026-09-10T18:47:00Z",
    startedAt: "2026-09-10T19:05:00Z",
    completedAt: "2026-09-10T20:35:00Z",
    hasReviewed: true
  },
  {
    id: "ORD-20260912-002",
    customerId: "cust-2",
    customerName: "Anisa Putri Wijaya",
    customerPhone: "081987654322",
    customerAddress: "Jl. Veteran No. 15, Pekalongan",
    customerLat: -6.8870,
    customerLng: 109.6780,
    therapistId: "th-2",
    therapistName: "Siti Rahmawati",
    therapistPhone: "081234567892",
    serviceId: "srv-5",
    serviceName: "Pijat Ibu Hamil & Pasca Salin",
    durationMinutes: 60,
    basePrice: 120000,
    extraFee: null,
    totalPrice: 120000,
    status: "sesi_dimulai",
    scheduledDate: "2026-09-12",
    scheduledTime: "16:30",
    notes: "Ibu hamil 6 bulan, pegal di pinggul",
    createdAt: "2026-09-12T16:00:00Z",
    acceptedAt: "2026-09-12T16:05:00Z",
    startedAt: "2026-09-12T16:35:00Z",
    completedAt: null,
    hasReviewed: false
  }
];

const defaultReviews = [
  {
    id: "rev-1",
    orderId: "ORD-20260910-001",
    customerId: "cust-1",
    customerName: "Dimas Arya Nugraha",
    therapistId: "th-1",
    therapistName: "Budi Santoso, S.Tr.Kes",
    serviceName: "Pijat Tradisional Jawa",
    rating: 5,
    comment: "Pijatan Pak Budi sangat mantap dan teliti! Rasa pegal di leher dan punggung langsung hilang. Sangat sopan dan membawa alas higienis sendiri. Pasti langganan lagi.",
    createdAt: "2026-09-10T20:40:00Z"
  }
];

const defaultPriceHistory = [
  {
    id: "ph-1",
    serviceId: "srv-1",
    serviceName: "Pijat Tradisional Jawa",
    oldPrice: 90000,
    newPrice: 100000,
    changedBy: "calonbikers85@gmail.com",
    reason: "Penyesuaian biaya bahan aromaterapi & insentif terapis",
    timestamp: "2026-08-01T09:00:00Z"
  },
  {
    id: "ph-2",
    serviceId: "srv-3",
    serviceName: "Deep Tissue & Sport Massage",
    oldPrice: 120000,
    newPrice: 130000,
    changedBy: "calonbikers85@gmail.com",
    reason: "Penyesuaian standar durasi dan teknik khusus",
    timestamp: "2026-08-15T11:20:00Z"
  }
];

const defaultMessages = [
  {
    id: "msg-1",
    orderId: "ORD-20260910-001",
    senderRole: "customer",
    senderName: "Dimas Arya",
    text: "Halo Pak Budi, posisi sudah sampai mana ya?",
    timestamp: "2026-09-10T18:50:00Z"
  },
  {
    id: "msg-2",
    orderId: "ORD-20260910-001",
    senderRole: "therapist",
    senderName: "Budi Santoso",
    text: "Halo Mas Dimas, saya sudah di lampu merah Jl. Hayam Wuruk, sekitar 5 menit lagi tiba ya.",
    timestamp: "2026-09-10T18:52:00Z"
  }
];

const defaultNotifications = [
  {
    id: "notif-1",
    targetRole: "customer",
    targetUserId: "cust-1",
    title: "Pesanan Selesai",
    message: "Sesi pijat ORD-20260910-001 telah selesai. Silakan berikan review untuk terapis Anda.",
    timestamp: "2026-09-10T20:35:00Z",
    read: false
  },
  {
    id: "notif-2",
    targetRole: "therapist",
    targetUserId: "th-2",
    title: "Sesi Pijat Berlangsung",
    message: "Pesanan Anisa Putri Wijaya sedang berjalan.",
    timestamp: "2026-09-12T16:35:00Z",
    read: true
  }
];

module.exports = {
  defaultServices,
  defaultTherapists,
  defaultCustomers,
  defaultOrders,
  defaultReviews,
  defaultPriceHistory,
  defaultMessages,
  defaultNotifications
};
