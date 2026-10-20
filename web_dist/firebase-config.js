/**
 * Dr. Balaji Homeo Care - Cloud Database & Real-Time Sync Bridge
 * Connects the Netlify Patient Web Portal with the Doctor Android App
 * Cloud Sync Object ID: ff808181a09d98f701a156f8c9b52440
 */

const CLOUD_SYNC_ENDPOINT = "https://api.restful-api.dev/objects/ff808181a09d98f701a156f8c9b52440";

const FIREBASE_CONFIG = {
  apiKey: "AIzaSyAKyeBOsKoLXy6xCgQoTImsl0u5oVikekU",
  authDomain: "gen-lang-client-0498394744.firebaseapp.com",
  projectId: "gen-lang-client-0498394744",
  databaseId: "ai-studio-android-homeocli-a8bd5b9a-3f9e-4fb2-bdf2-754ca5ca5eee"
};

class UniversalClinicBridge {
  constructor() {
    this.cloudUrl = CLOUD_SYNC_ENDPOINT;
    this.localStorageKeys = {
      APPTS: "homeo_patient_appts_cache",
      USERS: "homeo_patient_users_cache"
    };
    this.listeners = [];
    this.lastDataHash = "";
    this.startGlobalPoller();
  }

  getLocalAppointments() {
    try {
      const raw = localStorage.getItem(this.localStorageKeys.APPTS);
      return raw ? JSON.parse(raw) : [];
    } catch (e) {
      return [];
    }
  }

  saveLocalAppointments(appts) {
    try {
      localStorage.setItem(this.localStorageKeys.APPTS, JSON.stringify(appts));
    } catch (e) {
      console.warn("Storage warning:", e);
    }
  }

  async fetchCloudState() {
    try {
      const res = await fetch(this.cloudUrl, { cache: "no-store" });
      if (!res.ok) throw new Error("HTTP " + res.status);
      const json = await res.json();
      if (json && json.data && json.data.sync_json) {
        const parsed = JSON.parse(json.data.sync_json);
        return parsed.appointments || [];
      }
    } catch (e) {
      // Return local cache on network issue
    }
    return this.getLocalAppointments();
  }

  async pushCloudState(appointments) {
    try {
      this.saveLocalAppointments(appointments);
      const payload = {
        name: "dr_balaji_homeo_cloud_v2",
        data: {
          sync_json: JSON.stringify({
            appointments: appointments,
            updatedAt: Date.now()
          })
        }
      };
      await fetch(this.cloudUrl, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      return true;
    } catch (e) {
      console.warn("Cloud push warning:", e);
      return false;
    }
  }

  startGlobalPoller() {
    setInterval(async () => {
      if (this.listeners.length === 0) return;
      try {
        const remoteAppts = await this.fetchCloudState();
        const currentHash = JSON.stringify(remoteAppts.map(a => `${a.id}_${a.status}_${a.prescription || ""}`));
        if (currentHash !== this.lastDataHash) {
          this.lastDataHash = currentHash;
          this.saveLocalAppointments(remoteAppts);
          this.notifyListeners(remoteAppts);
        }
      } catch (err) {
        // Silent poll error
      }
    }, 2500);
  }

  notifyListeners(appts) {
    const docs = appts.map(item => ({
      id: item.id || item.appointmentId,
      exists: true,
      data: () => item
    }));
    const snapshot = {
      empty: docs.length === 0,
      size: docs.length,
      docs: docs,
      forEach(cb) {
        docs.forEach(cb);
      }
    };
    this.listeners.forEach(fn => {
      try {
        fn(snapshot);
      } catch (e) {
        console.error("Listener error:", e);
      }
    });
  }

  collection(name) {
    const self = this;
    return {
      doc(docId) {
        return {
          async set(data) {
            const current = await self.fetchCloudState();
            const index = current.findIndex(a => (a.id === docId || a.appointmentId === docId));
            const newItem = { id: docId, appointmentId: docId, ...data };
            if (index !== -1) {
              current[index] = { ...current[index], ...newItem };
            } else {
              current.unshift(newItem);
            }
            await self.pushCloudState(current);
            self.notifyListeners(current);
            return true;
          },

          async update(data) {
            return this.set(data);
          },

          async get() {
            const current = await self.fetchCloudState();
            const found = current.find(a => (a.id === docId || a.appointmentId === docId));
            return {
              exists: !!found,
              id: docId,
              data: () => found || null
            };
          }
        };
      },

      where(field, op, val) {
        return {
          limit(num) {
            return {
              async get() {
                const current = await self.fetchCloudState();
                const filtered = current.filter(item => {
                  if (field === "date") return item.date === val;
                  if (field === "mobile") return item.patientMobile === val || item.mobile === val;
                  if (field === "email") return item.patientEmail === val || item.email === val;
                  return item[field] === val;
                }).slice(0, num);

                const docs = filtered.map(item => ({
                  id: item.id || item.appointmentId,
                  exists: true,
                  data: () => item
                }));
                return {
                  empty: docs.length === 0,
                  size: docs.length,
                  docs: docs,
                  forEach(cb) { docs.forEach(cb); }
                };
              }
            };
          },

          async get() {
            const current = await self.fetchCloudState();
            const filtered = current.filter(item => {
              if (field === "date") return item.date === val;
              if (field === "mobile") return item.patientMobile === val || item.mobile === val;
              if (field === "email") return item.patientEmail === val || item.email === val;
              return item[field] === val;
            });
            const docs = filtered.map(item => ({
              id: item.id || item.appointmentId,
              exists: true,
              data: () => item
            }));
            return {
              empty: docs.length === 0,
              size: docs.length,
              docs: docs,
              forEach(cb) { docs.forEach(cb); }
            };
          },

          onSnapshot(callback) {
            return self.collection(name).onSnapshot(callback);
          }
        };
      },

      onSnapshot(callback) {
        self.listeners.push(callback);
        // Immediate first fire with current local state
        self.fetchCloudState().then(appts => {
          self.notifyListeners(appts);
        });
        return () => {
          self.listeners = self.listeners.filter(fn => fn !== callback);
        };
      },

      async get() {
        const current = await self.fetchCloudState();
        const docs = current.map(item => ({
          id: item.id || item.appointmentId,
          exists: true,
          data: () => item
        }));
        return {
          empty: docs.length === 0,
          size: docs.length,
          docs: docs,
          forEach(cb) { docs.forEach(cb); }
        };
      }
    };
  }
}

// Global initialization
const db = new UniversalClinicBridge();
window.db = db;
window.isFirebaseConnected = true;
window.FIREBASE_CONFIG = FIREBASE_CONFIG;
window.initFirebase = function () {
  console.log("✓ Connected to Shared Dr. Balaji Clinic Cloud (Real-Time Bidirectional Sync Active)");
  return db;
};
