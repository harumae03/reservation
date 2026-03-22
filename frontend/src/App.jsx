import { useState, useEffect, useCallback } from 'react';
import './App.css';
import FilterPanel from './components/FilterPanel/FilterPanel.jsx';
import FloorPlan from './components/FloorPlan/FloorPlan.jsx';
import RecommendationList from './components/RecommendationList/RecommendationList.jsx';
import ReservationForm from './components/ReservationForm/ReservationForm.jsx';
import DailySpecials from './components/DailySpecials/DailySpecials.jsx';
import { fetchTableStatuses, fetchRecommendations, createReservation, updateTablePosition } from './services/api.js';

function getInitialDateTime() {
  const now = new Date();
  const hour = now.getHours();

  // If restaurant is closed (before 11 or after 22), use next opening time
  let target;
  if (hour >= 22 || hour < 11) {
    target = new Date(now);
    if (hour >= 22) target.setDate(target.getDate() + 1);
    target.setHours(11, 0, 0, 0);
  } else {
    // Round up to next 30-minute slot
    target = new Date(now);
    const minutes = target.getMinutes();
    if (minutes <= 30) {
      target.setMinutes(30, 0, 0);
    } else {
      target.setHours(target.getHours() + 1, 0, 0, 0);
    }
    // If rounded time is past last slot (21:30), go to next day 11:00
    if (target.getHours() >= 22) {
      target.setDate(target.getDate() + 1);
      target.setHours(11, 0, 0, 0);
    }
  }

  const date = target.toISOString().split('T')[0];
  const time = `${String(target.getHours()).padStart(2, '0')}:${String(target.getMinutes()).padStart(2, '0')}`;
  return { date, time };
}

function App() {
  const initial = getInitialDateTime();

  const [filters, setFilters] = useState({
    date: initial.date,
    time: initial.time,
    partySize: 2,
    zone: '',
    preferences: [],
    duration: 120,
  });

  const [tableStatuses, setTableStatuses] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [selectedTable, setSelectedTable] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [searched, setSearched] = useState(false);
  const [adminMode, setAdminMode] = useState(false);

  const dateTime = `${filters.date}T${filters.time}:00`;

  // Load table statuses on init and when switching to admin mode
  useEffect(() => {
    loadTableStatuses();
  }, [adminMode]);

  const loadTableStatuses = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const statuses = await fetchTableStatuses(dateTime, filters.duration);
      setTableStatuses(statuses);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [dateTime, filters.duration]);

  const handleSearch = async () => {
    try {
      setLoading(true);
      setError(null);
      setSelectedTable(null);
      setRecommendations([]);

      // Load statuses and recommendations in parallel
      const [statuses, recs] = await Promise.all([
        fetchTableStatuses(dateTime, filters.duration),
        fetchRecommendations({
          dateTime,
          partySize: filters.partySize,
          duration: filters.duration,
          zone: filters.zone || undefined,
          preferences: filters.preferences.length ? filters.preferences : undefined,
        }),
      ]);

      setTableStatuses(statuses);
      setRecommendations(recs);
      setSearched(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleTableClick = (table) => {
    if (table.status === 'occupied') return;
    // Prevent booking a table that's too small for the party
    if (searched && table.capacity < filters.partySize) {
      setError(`Laud ${table.tableNumber} mahutab ${table.capacity} inimest, aga seltskond on ${filters.partySize}. Kasuta soovitusi laudade liitmiseks.`);
      return;
    }
    setSelectedTable(table);
    setShowForm(true);
  };

  const handleRecommendationClick = (rec) => {
    if (rec.merged) {
      // For merged recommendations, select the merged pair
      setSelectedTable({
        ...rec,
        status: 'selected',
        tableNumber: rec.mergedTableNumbers.join(' + '),
      });
      setShowForm(true);
    } else {
      // Find matching table in statuses
      const table = tableStatuses.find(t => t.id === rec.id);
      if (table) {
        setSelectedTable({ ...table, status: 'selected' });
        setShowForm(true);
      }
    }
  };

  const handleReservationSubmit = async (formData) => {
    try {
      setLoading(true);

      const reservationData = {
        customerName: formData.customerName,
        partySize: filters.partySize,
        dateTime,
        durationMinutes: filters.duration,
        preferences: filters.preferences.join(',') || null,
      };

      // Use tableIds for merged bookings, tableId for single
      if (selectedTable?.merged && selectedTable?.mergedTableIds) {
        reservationData.tableIds = selectedTable.mergedTableIds;
      } else {
        reservationData.tableId = selectedTable.id;
      }

      await createReservation(reservationData);

      setShowForm(false);
      setSelectedTable(null);
      setToast('Broneering kinnitatud!');
      setTimeout(() => setToast(null), 3000);

      // Refresh floor plan
      await handleSearch();
    } catch (err) {
      throw err; // Let the form handle the error display
    } finally {
      setLoading(false);
    }
  };

  // Admin drag handler
  const handleTableDrag = async (tableId, newPosX, newPosY) => {
    try {
      await updateTablePosition(tableId, newPosX, newPosY);
      // Update local state
      setTableStatuses(prev =>
        prev.map(t => t.id === tableId ? { ...t, posX: newPosX, posY: newPosY } : t)
      );
      setToast('Laua positsioon uuendatud');
      setTimeout(() => setToast(null), 2000);
    } catch (err) {
      setError('Positsiooni uuendamine ebaõnnestus: ' + err.message);
    }
  };

  // Build map of recommended table IDs -> rank for highlighting
  const recommendedMap = new Map();
  recommendations.forEach(r => {
    if (r.merged && r.mergedTableIds) {
      r.mergedTableIds.forEach(id => recommendedMap.set(id, r.rank));
    } else {
      recommendedMap.set(r.id, r.rank);
    }
  });

  // Collect merged pairs for connector lines on floor plan
  const mergedPairs = recommendations
    .filter(r => r.merged && r.mergedTableIds?.length === 2)
    .map(r => r.mergedTableIds);

  // Merge statuses with recommendation/selection info (including rank)
  const tablesWithVisualStatus = tableStatuses.map(t => {
    if (selectedTable?.merged && selectedTable?.mergedTableIds?.includes(t.id)) {
      return { ...t, status: 'selected' };
    }
    if (selectedTable?.id === t.id) return { ...t, status: 'selected' };
    if (t.status === 'available' && recommendedMap.has(t.id)) {
      return { ...t, status: 'recommended', rank: recommendedMap.get(t.id) };
    }
    return t;
  });

  return (
    <div className="app-layout">
      {/* Header */}
      <header className="app-header">
        <div className="app-header-inner">
          <div className="app-logo">Verdant Bistro</div>
          <nav className="app-nav">
            <a
              href="#"
              className={!adminMode ? 'active' : ''}
              onClick={(e) => { e.preventDefault(); setAdminMode(false); }}
            >
              Broneerimine
            </a>
            <a
              href="#"
              className={adminMode ? 'active' : ''}
              onClick={(e) => { e.preventDefault(); setAdminMode(true); }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '1rem', verticalAlign: 'middle', marginRight: '0.25rem' }}>
                admin_panel_settings
              </span>
              Admin
            </a>
            <button
              className="mobile-filter-toggle"
              onClick={() => setFiltersOpen(o => !o)}
            >
              <span className="material-symbols-outlined">tune</span>
            </button>
          </nav>
        </div>
      </header>

      {/* Filter Sidebar — hidden in admin mode */}
      {!adminMode && (
        <div className={`filter-sidebar-wrapper${filtersOpen ? ' open' : ''}`}>
          <FilterPanel
            filters={filters}
            onChange={setFilters}
            onSearch={() => { handleSearch(); setFiltersOpen(false); }}
            loading={loading}
          />
        </div>
      )}

      {/* Main: Floor Plan */}
      <main className={`main-content${adminMode ? ' main-content--admin' : ''}`}>
        <div className="floor-plan-header">
          <div>
            <h1>{adminMode ? 'Laudade paigutus' : 'Saali plaan'}</h1>
            <p>{adminMode ? 'Lohista laudu soovitud positsioonile' : 'Vali sobiv laud restorani plaanilt'}</p>
          </div>
          {!adminMode && (
            <div className="legend">
              <div className="legend-item">
                <span className="legend-dot legend-dot--available" /> Saadaval
              </div>
              <div className="legend-item">
                <span className="legend-dot legend-dot--occupied" /> Hõivatud
              </div>
              {recommendations.length > 0 && (
                <>
                  <div className="legend-item">
                    <span className="legend-dot legend-dot--recommended" /> Sobivaimad
                  </div>
                  <div className="legend-item">
                    <span className="legend-dot legend-dot--other" /> Teised soovitused
                  </div>
                </>
              )}
              {selectedTable && (
                <div className="legend-item">
                  <span className="legend-dot legend-dot--selected" /> Valitud
                </div>
              )}
            </div>
          )}
        </div>

        {error && <div className="error-message">{error}</div>}

        {loading && tableStatuses.length === 0 ? (
          <div className="loading-spinner">
            <span className="material-symbols-outlined">progress_activity</span>
            Laen saali plaani...
          </div>
        ) : (
          <FloorPlan
            tables={tablesWithVisualStatus}
            onTableClick={handleTableClick}
            mergedPairs={mergedPairs}
            adminMode={adminMode}
            onTableDrag={handleTableDrag}
          />
        )}

        {selectedTable && !showForm && !adminMode && (
          <div className="status-bar">
            <div className="status-bar-icon">
              <span className="material-symbols-outlined">info</span>
            </div>
            <div>
              <p className="title">Laud {selectedTable.tableNumber} valitud</p>
              <p className="desc">{selectedTable.capacity} kohta &middot; {selectedTable.zone}</p>
            </div>
          </div>
        )}
      </main>

      {/* Right Sidebar: Recommendations — hidden in admin mode */}
      {!adminMode && (
        <aside className="recommendations-panel">
          <h3>Soovitused</h3>
          {recommendations.length > 0 ? (
            <RecommendationList
              recommendations={recommendations}
              onSelect={handleRecommendationClick}
            />
          ) : searched ? (
            <div className="empty-state empty-state--no-results">
              <span className="material-symbols-outlined">search_off</span>
              <p>Valitud filtritega vabu laudu ei leitud</p>
              <p className="empty-state-hint">Proovi muuta kuupäeva, kellaaega või seltskonna suurust</p>
            </div>
          ) : (
            <div className="empty-state">
              <p>Kasuta filtreid ja vajuta &laquo;Otsi laudu&raquo;, et näha soovitusi</p>
            </div>
          )}
          <DailySpecials />
        </aside>
      )}

      {/* Reservation Form Modal */}
      {showForm && selectedTable && (
        <ReservationForm
          table={selectedTable}
          filters={filters}
          onSubmit={handleReservationSubmit}
          onClose={() => {
            setShowForm(false);
            setSelectedTable(null);
          }}
        />
      )}

      {/* Toast */}
      {toast && <div className="toast">{toast}</div>}
    </div>
  );
}

export default App;
