import { ZONE_LABELS } from '../../types/index.js';

export default function RecommendationList({ recommendations, onSelect }) {
  return (
    <div className="recommendation-list">
      {recommendations.map((rec, index) => (
        <div
          key={rec.id}
          className={`rec-card${index === 0 ? ' rec-card--top' : ''}`}
          onClick={() => onSelect(rec)}
        >
          <div className="rec-card-header">
            <span className={`rec-badge ${index === 0 ? 'rec-badge--top' : 'rec-badge--other'}`}>
              {rec.score}% Match
            </span>
            {index === 0 && (
              <span
                className="material-symbols-outlined"
                style={{ color: 'var(--primary)', fontVariationSettings: "'FILL' 1" }}
              >
                stars
              </span>
            )}
          </div>

          <h4>Laud {rec.tableNumber} — {ZONE_LABELS[rec.zone] || rec.zone}</h4>
          <p>{rec.reason}</p>
          <p style={{ marginTop: '0.25rem', fontSize: '0.75rem', color: 'var(--outline)' }}>
            {rec.capacity} kohta
          </p>

          <div className="rec-card-footer">
            <span>{ZONE_LABELS[rec.zone]}</span>
            <button
              className={`rec-select-btn ${index === 0 ? 'rec-select-btn--primary' : 'rec-select-btn--primary'}`}
              onClick={(e) => {
                e.stopPropagation();
                onSelect(rec);
              }}
            >
              Vali
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
