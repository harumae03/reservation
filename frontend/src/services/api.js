const API_BASE = '/api';

/**
 * Fetches all tables with available/occupied status at the given time.
 * @param {string} dateTime - ISO datetime string
 * @param {number} [duration=120]
 * @returns {Promise<import('../types/index.js').TableWithStatus[]>}
 */
export async function fetchTableStatuses(dateTime, duration = 120) {
  const params = new URLSearchParams({ dateTime, duration: String(duration) });
  const res = await fetch(`${API_BASE}/tables/status?${params}`);
  if (!res.ok) throw await buildError(res);
  return res.json();
}

/**
 * Fetches table recommendations based on filters.
 * @param {Object} params
 * @param {string} params.dateTime
 * @param {number} params.partySize
 * @param {number} [params.duration=120]
 * @param {string} [params.zone]
 * @param {string[]} [params.preferences]
 * @returns {Promise<import('../types/index.js').TableRecommendation[]>}
 */
export async function fetchRecommendations({ dateTime, partySize, duration = 120, zone, preferences }) {
  const params = new URLSearchParams({
    dateTime,
    partySize: String(partySize),
    duration: String(duration),
  });
  if (zone) params.set('zone', zone);
  if (preferences?.length) {
    preferences.forEach(p => params.append('preferences', p));
  }
  const res = await fetch(`${API_BASE}/tables/recommend?${params}`);
  if (!res.ok) throw await buildError(res);
  return res.json();
}

/**
 * Creates a new reservation.
 * @param {import('../types/index.js').ReservationRequest} data
 * @returns {Promise<import('../types/index.js').ReservationResponse>}
 */
export async function createReservation(data) {
  const res = await fetch(`${API_BASE}/reservations`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw await buildError(res);
  return res.json();
}

/**
 * Cancels a reservation.
 * @param {number} id
 * @returns {Promise<void>}
 */
export async function cancelReservation(id) {
  const res = await fetch(`${API_BASE}/reservations/${id}`, { method: 'DELETE' });
  if (!res.ok) throw await buildError(res);
}

/**
 * Fetches daily meal specials from TheMealDB.
 * @returns {Promise<{name: string, category: string, area: string, imageUrl: string}[]>}
 */
export async function fetchDailySpecials() {
  const res = await fetch(`${API_BASE}/daily-specials`);
  if (!res.ok) return [];
  return res.json();
}

async function buildError(res) {
  try {
    const body = await res.json();
    const error = new Error(body.message || 'Serveriviga');
    error.status = res.status;
    return error;
  } catch {
    return new Error(res.status === 409
      ? 'Laud on juba broneeritud'
      : res.status === 400
        ? 'Vigased andmed'
        : 'Serveriviga');
  }
}
