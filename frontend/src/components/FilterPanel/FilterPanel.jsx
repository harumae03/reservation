import { ZONE_LABELS, PREFERENCE_OPTIONS, DURATION_OPTIONS } from '../../types/index.js';

// Time slots: 11:00–21:30 in 30-minute steps
const TIME_SLOTS = [];
for (let h = 11; h <= 21; h++) {
  TIME_SLOTS.push(`${String(h).padStart(2, '0')}:00`);
  if (h <= 21) TIME_SLOTS.push(`${String(h).padStart(2, '0')}:30`);
}

export default function FilterPanel({ filters, onChange, onSearch, loading }) {
  const updateFilter = (key, value) => {
    onChange({ ...filters, [key]: value });
  };

  const togglePreference = (pref) => {
    const current = filters.preferences || [];
    const updated = current.includes(pref)
      ? current.filter(p => p !== pref)
      : [...current, pref];
    updateFilter('preferences', updated);
  };

  return (
    <aside className="filter-sidebar">
      <div className="filter-header">
        <h2>Filtrid</h2>
        <p>Otsi sobivat lauda</p>
      </div>

      {/* Date */}
      <div className="filter-group">
        <label className="filter-label">
          <span className="material-symbols-outlined">calendar_today</span>
          Kuupäev
        </label>
        <input
          type="date"
          className="filter-input"
          value={filters.date}
          onChange={e => updateFilter('date', e.target.value)}
        />
      </div>

      {/* Time */}
      <div className="filter-group">
        <label className="filter-label">
          <span className="material-symbols-outlined">schedule</span>
          Kellaaeg
        </label>
        <select
          className="filter-input filter-select"
          value={filters.time}
          onChange={e => updateFilter('time', e.target.value)}
        >
          {TIME_SLOTS.map(t => (
            <option key={t} value={t}>{t}</option>
          ))}
        </select>
      </div>

      {/* Party Size */}
      <div className="filter-group">
        <label className="filter-label">
          <span className="material-symbols-outlined">groups</span>
          Seltskonna suurus
        </label>
        <div className="party-size-group">
          {[1, 2, 3, 4, 5, 6, 7, 8].map(n => (
            <button
              key={n}
              className={`party-size-btn${filters.partySize === n ? ' active' : ''}`}
              onClick={() => updateFilter('partySize', n)}
            >
              {n}
            </button>
          ))}
        </div>
      </div>

      {/* Zone */}
      <div className="filter-group">
        <label className="filter-label">
          <span className="material-symbols-outlined">map</span>
          Tsoon
        </label>
        <select
          className="filter-input filter-select"
          value={filters.zone}
          onChange={e => updateFilter('zone', e.target.value)}
        >
          <option value="">Kõik tsoonid</option>
          {Object.entries(ZONE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
      </div>

      {/* Duration */}
      <div className="filter-group">
        <label className="filter-label">
          <span className="material-symbols-outlined">timer</span>
          Kestus
        </label>
        <select
          className="filter-input filter-select"
          value={filters.duration}
          onChange={e => updateFilter('duration', Number(e.target.value))}
        >
          {DURATION_OPTIONS.map(opt => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>

      {/* Preferences */}
      <div className="filter-group">
        <label className="filter-label">
          <span className="material-symbols-outlined">tune</span>
          Eelistused
        </label>
        <div className="preference-list">
          {PREFERENCE_OPTIONS.map(pref => (
            <label key={pref.value} className="preference-item">
              <input
                type="checkbox"
                checked={(filters.preferences || []).includes(pref.value)}
                onChange={() => togglePreference(pref.value)}
              />
              {pref.label}
            </label>
          ))}
        </div>
      </div>

      <button
        className="search-btn"
        onClick={onSearch}
        disabled={loading}
      >
        {loading ? 'Otsin...' : 'Otsi laudu'}
      </button>
    </aside>
  );
}
