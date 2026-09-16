/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  darkMode: ["class", '[data-theme="clinic-dark"]'],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "Roboto", "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [
    require("daisyui"),
    require("autoprefixer"),
  ],
  daisyui: {
    styled: true,
    themes: [
      {
        // Thème clair : blanc / gris très clair / bleu médical
        "clinic-light": {
          primary: "#0e7490",
          "primary-content": "#f0fdff",
          secondary: "#1d4ed8",
          "secondary-content": "#eff6ff",
          accent: "#0891b2",
          "accent-content": "#ecfeff",
          neutral: "#1e293b",
          "neutral-content": "#f1f5f9",
          "base-100": "#ffffff",
          "base-200": "#f8fafc",
          "base-300": "#e2e8f0",
          "base-content": "#0f172a",
          info: "#0284c7",
          success: "#15803d",
          warning: "#b45309",
          error: "#dc2626",
        },
      },
      {
        // Thème sombre : gris ardoise profond, mêmes teintes accent
        "clinic-dark": {
          primary: "#22d3ee",
          "primary-content": "#083344",
          secondary: "#60a5fa",
          "secondary-content": "#172554",
          accent: "#2dd4bf",
          "accent-content": "#042f2e",
          neutral: "#334155",
          "neutral-content": "#e2e8f0",
          "base-100": "#0f172a",
          "base-200": "#1e293b",
          "base-300": "#334155",
          "base-content": "#e2e8f0",
          info: "#38bdf8",
          success: "#4ade80",
          warning: "#fbbf24",
          error: "#f87171",
        },
      },
    ],
    darkTheme: "clinic-dark",
    base: true,
    utils: true,
    logs: false,
  },
};
