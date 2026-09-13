// Complete compiler script for PIJATKU
const fs = require('fs');
const path = require('path');

const defaultData = require('./default_data.js');
const cssBundle = require('./css_bundle.js');
const htmlTemplates = require('./templates.js');
const clientJs = require('./client_js.js');
const clientAuth = require('./client_auth_customer.js');
const clientCustomerOrder = require('./client_customer_order.js');
const clientTrackingChat = require('./client_tracking_chat.js');
const clientTherapistAdmin = require('./client_therapist_admin.js');
const clientViewsRender = require('./client_views_render.js');

const defaultDataScript = `
const DEFAULT_DATA = {
  services: ${JSON.stringify(defaultData.defaultServices, null, 2)},
  therapists: ${JSON.stringify(defaultData.defaultTherapists, null, 2)},
  customers: ${JSON.stringify(defaultData.defaultCustomers, null, 2)},
  orders: ${JSON.stringify(defaultData.defaultOrders, null, 2)},
  reviews: ${JSON.stringify(defaultData.defaultReviews, null, 2)},
  priceHistory: ${JSON.stringify(defaultData.defaultPriceHistory, null, 2)},
  messages: ${JSON.stringify(defaultData.defaultMessages, null, 2)},
  notifications: ${JSON.stringify(defaultData.defaultNotifications, null, 2)}
};
`;

const completeHtml = `<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>PIJATKU – Pesan Terapis, Nyaman di Rumah</title>
  <meta name="description" content="Aplikasi layanan pesan terapis pijat panggilan profesional langsung ke rumah Anda di area Pekalongan dan sekitarnya.">

  <!-- Tailwind CSS CDN -->
  <script src="https://cdn.tailwindcss.com"></script>
  
  <!-- Font Awesome 6 Icons -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">

  <!-- Leaflet CSS & JS for Interactive Map -->
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

  <!-- Google Fonts: Plus Jakarta Sans -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

  <style>
${cssBundle}
  </style>
</head>
<body class="bg-gray-100 text-gray-900 antialiased min-h-screen">

${htmlTemplates}

  <script>
${defaultDataScript}

${clientJs}

${clientAuth}

${clientCustomerOrder}

${clientTrackingChat}

${clientTherapistAdmin}

${clientViewsRender}
  </script>
</body>
</html>
`;

// Write to root index.html
fs.writeFileSync(path.join(__dirname, '../index.html'), completeHtml, 'utf8');
console.log('Successfully generated /index.html');

// Write to public/index.html
fs.mkdirSync(path.join(__dirname, '../public'), { recursive: true });
fs.writeFileSync(path.join(__dirname, '../public/index.html'), completeHtml, 'utf8');
console.log('Successfully generated /public/index.html');

// Write to app/src/main/assets/index.html
fs.mkdirSync(path.join(__dirname, '../app/src/main/assets'), { recursive: true });
fs.writeFileSync(path.join(__dirname, '../app/src/main/assets/index.html'), completeHtml, 'utf8');
console.log('Successfully generated /app/src/main/assets/index.html');
