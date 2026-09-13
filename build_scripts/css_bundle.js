module.exports = `
:root {
  --primary: #059669;
  --primary-dark: #047857;
  --primary-light: #d1fae5;
  --accent: #d97706;
  --accent-light: #fef3c7;
  --bg-sand: #fbfbf9;
  --surface: #ffffff;
  --text-main: #1f2937;
  --text-muted: #6b7280;
}

body {
  font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
  background-color: #f3f4f6;
  color: var(--text-main);
  -webkit-tap-highlight-color: transparent;
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 9999px;
}
::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* App container */
.app-container {
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  background: #ffffff;
  position: relative;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
}

.admin-container {
  max-width: 1280px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f8fafc;
}

/* Leaflet map styles */
.leaflet-container {
  font-family: inherit;
  border-radius: 1rem;
  z-index: 10 !important;
}

/* Pulse badge for live status */
.pulse-emerald {
  animation: pulse-green 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
@keyframes pulse-green {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.08); }
}

/* Glass effect */
.glass-nav {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid rgba(229, 231, 235, 0.8);
}

/* Card hover animation */
.hover-lift {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px -8px rgba(0,0,0,0.1);
}

/* Step tracker indicator */
.stepper-line {
  position: absolute;
  top: 18px;
  left: 30px;
  right: 30px;
  height: 3px;
  background: #e5e7eb;
  z-index: 1;
}
.stepper-line-active {
  position: absolute;
  top: 18px;
  left: 30px;
  height: 3px;
  background: #059669;
  z-index: 2;
  transition: width 0.4s ease;
}

/* Chat bubble styling */
.chat-bubble-user {
  background: #059669;
  color: #ffffff;
  border-top-right-radius: 4px;
}
.chat-bubble-peer {
  background: #f3f4f6;
  color: #1f2937;
  border-top-left-radius: 4px;
}

/* Hide scrollbar for category tabs */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* Timer ring */
.timer-display {
  font-variant-numeric: tabular-nums;
}
`;
