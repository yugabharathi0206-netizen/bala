# Dr. Balaji Homeo Care — Web & Android Integration Guide

This repository contains both:
1. **Dr. Balaji Homeo Care Android App** (for Doctor & Patient management on Android).
2. **Patient Web Booking Portal** (for online patients to book and check consultation status).

---

## ⚡ How Real-Time Appointment Confirmation Works

1. **Client Books on Website**:
   - The patient selects a consultation mode (In-Clinic or Video), chooses an available morning or evening time slot, describes their symptoms, and clicks **Confirm Appointment**.
   - An appointment token (e.g., `HM-2026-XXXX`) is issued.
   - The appointment is automatically written to the shared Cloud Sync database (`ff808181a09d98f701a156f8c9b52440`).

2. **Doctor Receives in Android App**:
   - The Doctor opens **Dr. Balaji Homeo** on Android.
   - The appointment instantly appears in the **Doctor Console** under **Pending Reviews**.
   - The Doctor taps **Confirm Slot** (or adds a clinical prescription / advice).

3. **Client Web Shows Confirmed**:
   - The web portal's real-time poller receives the update within **2–3 seconds**.
   - The status updates from `⏳ Pending Doctor Confirmation` to:
     ```
     ✅ Appointment Confirmed
     🩺 Confirmed by Dr. Balaji: Your appointment has been approved. Please arrive 10 minutes prior to your slot time.
     ```
   - No stuck "pending", no 403 errors, and no uncaught exceptions!

---

## 🚀 How to Deploy to Netlify (Zero Errors)

### Option 1: Drag & Drop (Easiest)
1. In this project, open the `website/` folder.
2. Drag and drop the `website/` folder (or the root folder containing `index.html`) directly into the Netlify Dashboard (**Deploys** > **Drag and drop your site folder here**).
3. The site deploys instantly in 5 seconds with zero build steps!

### Option 2: Connect via GitHub
1. Push this repository to GitHub:
   ```bash
   git add .
   git commit -m "Fix appointment sync and Netlify configuration"
   git push origin main
   ```
2. In Netlify, click **Add new site** > **Import an existing project** > **GitHub**.
3. Select your repository.
4. Netlify will automatically detect `netlify.toml`:
   - **Build command**: *(leave blank)*
   - **Publish directory**: `.` or `website`
5. Click **Deploy**. Netlify will deploy cleanly without any build errors!

---

## 📁 File Structure

- `/website/`: Clean, standalone static website directory for Netlify deployment:
  - `index.html`: Web Portal SPA with Home, Login, Signup, Booking, Dashboard, and Profile.
  - `styles.css`: Complete responsive healthcare UI design.
  - `app.js`: Patient application logic with real-time appointment sync.
  - `firebase-config.js`: Universal real-time cloud database bridge (`UniversalClinicBridge`).
  - `doctor_portrait.jpg`: Doctor Balaji consultant photo.
  - `_redirects`: Netlify single-page application router.
  - `netlify.toml`: Netlify configuration with CORS and static publish rules.
- `/app/`: Android application source code (Kotlin + Jetpack Compose) for Dr. Balaji clinic management & patient app.
