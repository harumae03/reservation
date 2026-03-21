import { useState } from 'react';
import { ZONE_LABELS, DURATION_OPTIONS } from '../../types/index.js';

export default function ReservationForm({ table, filters, onSubmit, onClose }) {
  const [customerName, setCustomerName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!customerName.trim()) {
      setError('Palun sisesta nimi');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      await onSubmit({ customerName: customerName.trim() });
    } catch (err) {
      setError(err.message || 'Broneeringu loomine ebaõnnestus');
    } finally {
      setSubmitting(false);
    }
  };

  const durationLabel = DURATION_OPTIONS.find(d => d.value === filters.duration)?.label || `${filters.duration} min`;

  return (
    <div className="reservation-form-overlay" onClick={onClose}>
      <div className="reservation-form-card" onClick={e => e.stopPropagation()}>
        <h2>Broneeringu kinnitamine</h2>
        <p className="subtitle">Täida oma andmed broneeringu lõpetamiseks</p>

        {/* Table info */}
        <div className="form-table-info">
          <div className="form-table-info-item">
            <span className="label">
              <span className="material-symbols-outlined">table_restaurant</span>
              Laud
            </span>
            <span className="value">Laud {table.tableNumber}</span>
          </div>
          <div className="form-table-info-item">
            <span className="label">
              <span className="material-symbols-outlined">groups</span>
              Kohti
            </span>
            <span className="value">{table.capacity} ({filters.partySize} külalist)</span>
          </div>
          <div className="form-table-info-item">
            <span className="label">
              <span className="material-symbols-outlined">calendar_today</span>
              Kuupäev
            </span>
            <span className="value">{formatDate(filters.date)}</span>
          </div>
          <div className="form-table-info-item">
            <span className="label">
              <span className="material-symbols-outlined">schedule</span>
              Aeg
            </span>
            <span className="value">{filters.time} ({durationLabel})</span>
          </div>
          <div className="form-table-info-item">
            <span className="label">
              <span className="material-symbols-outlined">map</span>
              Tsoon
            </span>
            <span className="value">{ZONE_LABELS[table.zone] || table.zone}</span>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="customerName">Kliendi nimi</label>
            <input
              id="customerName"
              type="text"
              placeholder="Sinu nimi"
              value={customerName}
              onChange={e => setCustomerName(e.target.value)}
              autoFocus
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button
            type="submit"
            className="submit-btn"
            disabled={submitting}
          >
            {submitting ? 'Broneerin...' : 'Kinnita broneering'}
          </button>

          <button
            type="button"
            className="cancel-btn"
            onClick={onClose}
          >
            Tagasi
          </button>
        </form>
      </div>
    </div>
  );
}

function formatDate(dateStr) {
  const date = new Date(dateStr + 'T00:00:00');
  return date.toLocaleDateString('et-EE', {
    weekday: 'short',
    day: 'numeric',
    month: 'long',
  });
}
