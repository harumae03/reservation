// Type definitions as JSDoc for IDE support without TypeScript

/**
 * @typedef {'TERRACE' | 'INDOOR_MAIN' | 'INDOOR_WINDOW' | 'PRIVATE_ROOM'} Zone
 */

/**
 * @typedef {'available' | 'occupied' | 'recommended' | 'selected'} TableStatus
 */

/**
 * @typedef {'CONFIRMED' | 'CANCELLED'} ReservationStatus
 */

/**
 * @typedef {Object} RestaurantTable
 * @property {number} id
 * @property {number} tableNumber
 * @property {number} capacity
 * @property {Zone} zone
 * @property {number} posX
 * @property {number} posY
 * @property {number} width
 * @property {number} height
 * @property {boolean} byWindow
 * @property {boolean} quiet
 * @property {boolean} nearPlayground
 */

/**
 * @typedef {RestaurantTable & { status: TableStatus }} TableWithStatus
 */

/**
 * @typedef {RestaurantTable & { score: number, rank: number, reason: string }} TableRecommendation
 */

/**
 * @typedef {Object} ReservationRequest
 * @property {number} tableId
 * @property {string} customerName
 * @property {number} partySize
 * @property {string} dateTime - ISO datetime string
 * @property {number} durationMinutes
 * @property {string} [preferences]
 */

/**
 * @typedef {Object} ReservationResponse
 * @property {number} id
 * @property {number} tableNumber
 * @property {Zone} tableZone
 * @property {number} tableCapacity
 * @property {string} customerName
 * @property {number} partySize
 * @property {string} startTime
 * @property {string} endTime
 * @property {number} durationMinutes
 * @property {ReservationStatus} status
 * @property {string} [preferences]
 * @property {string} createdAt
 */

/**
 * @typedef {Object} Filters
 * @property {string} date - YYYY-MM-DD
 * @property {string} time - HH:mm
 * @property {number} partySize
 * @property {Zone | ''} zone
 * @property {string[]} preferences
 * @property {number} duration
 */

// Zone display names (Estonian)
export const ZONE_LABELS = {
  TERRACE: 'Terrass',
  INDOOR_MAIN: 'Sisesaal',
  INDOOR_WINDOW: 'Aknakoht',
  PRIVATE_ROOM: 'Privaatruum',
};

export const PREFERENCE_OPTIONS = [
  { value: 'window', label: 'Akna ääres' },
  { value: 'quiet', label: 'Vaikne nurk' },
  { value: 'playground', label: 'Mängunurga lähedal' },
];

export const DURATION_OPTIONS = [
  { value: 60, label: '1 tund' },
  { value: 90, label: '1,5 tundi' },
  { value: 120, label: '2 tundi' },
  { value: 150, label: '2,5 tundi' },
  { value: 180, label: '3 tundi' },
];
