import React, { useState, useCallback } from 'react';
import TableShape from './TableShape.jsx';

const ZONE_AREAS = [
  { zone: 'TERRACE', label: 'Terrass', x: 70, y: 25, width: 830, height: 130, fill: '#e8f5e9', stroke: '#81c784', opacity: 0.45, labelColor: '#2e7d32' },
  { zone: 'INDOOR_WINDOW', label: 'Aknakoht (vasak)', x: 30, y: 185, width: 135, height: 370, fill: '#e3f2fd', stroke: '#90caf9', opacity: 0.35, labelColor: '#1565c0' },
  { zone: 'INDOOR_MAIN', label: 'Sisesaal', x: 200, y: 185, width: 620, height: 310, fill: '#f5f5f5', stroke: '#bdbdbd', opacity: 0.5, labelColor: '#616161' },
  { zone: 'INDOOR_WINDOW', label: 'Aknakoht (parem)', x: 860, y: 185, width: 130, height: 290, fill: '#e3f2fd', stroke: '#90caf9', opacity: 0.35, labelColor: '#1565c0' },
  { zone: 'PRIVATE_ROOM', label: 'Privaatruum', x: 600, y: 510, width: 380, height: 230, fill: '#fce4ec', stroke: '#f48fb1', opacity: 0.25, labelColor: '#ad1457' },
];

function clientToSVG(svg, clientX, clientY) {
  const pt = svg.createSVGPoint();
  pt.x = clientX;
  pt.y = clientY;
  return pt.matrixTransform(svg.getScreenCTM().inverse());
}

export default function FloorPlan({ tables, onTableClick, mergedPairs = [], adminMode = false, onTableDrag }) {
  const svgRef = React.useRef(null);
  const [dragState, setDragState] = useState(null);

  const handlePointerDown = useCallback((e, table) => {
    if (!adminMode || !svgRef.current) return;
    e.preventDefault();
    const svg = svgRef.current;
    const pt = clientToSVG(svg, e.clientX, e.clientY);
    setDragState({
      tableId: table.id,
      offsetX: pt.x - table.posX,
      offsetY: pt.y - table.posY,
      currentX: table.posX,
      currentY: table.posY,
    });
    svg.setPointerCapture?.(e.pointerId);
  }, [adminMode]);

  const handlePointerMove = useCallback((e) => {
    if (!dragState || !svgRef.current) return;
    const pt = clientToSVG(svgRef.current, e.clientX, e.clientY);
    setDragState(prev => ({
      ...prev,
      currentX: Math.max(0, Math.min(1040, pt.x - prev.offsetX)),
      currentY: Math.max(0, Math.min(770, pt.y - prev.offsetY)),
    }));
  }, [dragState]);

  const handlePointerUp = useCallback(() => {
    if (!dragState) return;
    if (onTableDrag) {
      onTableDrag(dragState.tableId, dragState.currentX, dragState.currentY);
    }
    setDragState(null);
  }, [dragState, onTableDrag]);

  // Build a lookup for tables by ID (for merged connector lines)
  const tableMap = new Map(tables.map(t => [t.id, t]));

  // Apply drag offset to the dragged table
  const displayTables = tables.map(t => {
    if (dragState && t.id === dragState.tableId) {
      return { ...t, posX: dragState.currentX, posY: dragState.currentY };
    }
    return t;
  });

  return (
    <div className="floor-plan-container">
      {adminMode && (
        <div className="admin-banner">
          <span className="material-symbols-outlined">admin_panel_settings</span>
          Administraatori vaade — lohista laudu paigutuse muutmiseks
        </div>
      )}
      <svg
        ref={svgRef}
        className={`floor-plan-svg${adminMode ? ' admin-mode' : ''}`}
        viewBox="0 0 1040 770"
        preserveAspectRatio="xMidYMid meet"
        onPointerMove={adminMode ? handlePointerMove : undefined}
        onPointerUp={adminMode ? handlePointerUp : undefined}
        onPointerLeave={adminMode ? handlePointerUp : undefined}
      >
        {/* Zone backgrounds */}
        {ZONE_AREAS.map((area, i) => (
          <g key={i}>
            <rect
              x={area.x}
              y={area.y}
              width={area.width}
              height={area.height}
              rx={16}
              fill={area.fill}
              opacity={area.opacity}
              stroke={area.stroke}
              strokeWidth={1.5}
              strokeDasharray="6 3"
            />
            <text
              x={area.x + 10}
              y={area.y - 5}
              fill={area.labelColor}
              fontSize={10}
              fontFamily="Inter, sans-serif"
              fontWeight={500}
              textTransform="uppercase"
              letterSpacing="0.05em"
            >
              {area.label}
            </text>
          </g>
        ))}

        {/* Decorative elements */}
        {/* Terrace plants */}
        <circle cx={55} cy={70} r={8} fill="#d3e8d7" opacity={0.6} />
        <circle cx={920} cy={70} r={8} fill="#d3e8d7" opacity={0.6} />
        <circle cx={55} cy={130} r={6} fill="#e2f6ba" opacity={0.5} />
        <circle cx={920} cy={130} r={6} fill="#e2f6ba" opacity={0.5} />

        {/* Window lines on left */}
        <line x1={30} y1={195} x2={30} y2={545} stroke="#abb4ab" strokeWidth={2} strokeDasharray="8 4" opacity={0.4} />
        {/* Window lines on right */}
        <line x1={990} y1={195} x2={990} y2={465} stroke="#abb4ab" strokeWidth={2} strokeDasharray="8 4" opacity={0.4} />

        {/* Playground area indicator */}
        <g opacity={0.4}>
          <rect x={110} y={600} width={120} height={80} rx={12} fill="#e2f6ba" opacity={0.5} />
          <text x={133} y={648} fill="#546536" fontSize={9} fontFamily="Inter, sans-serif">Mängunurk</text>
        </g>

        {/* Merged table connector lines */}
        {mergedPairs.map((pair, i) => {
          const t1 = tableMap.get(pair[0]);
          const t2 = tableMap.get(pair[1]);
          if (!t1 || !t2) return null;
          const cx1 = t1.posX + t1.width / 2;
          const cy1 = t1.posY + t1.height / 2;
          const cx2 = t2.posX + t2.width / 2;
          const cy2 = t2.posY + t2.height / 2;
          return (
            <line
              key={`merge-${i}`}
              x1={cx1} y1={cy1}
              x2={cx2} y2={cy2}
              stroke="#e6a54a"
              strokeWidth={3}
              strokeDasharray="8 4"
              opacity={0.7}
            />
          );
        })}

        {/* Tables */}
        {displayTables.map(table => (
          <TableShape
            key={table.id}
            table={table}
            onClick={() => !adminMode && onTableClick(table)}
            adminMode={adminMode}
            isDragging={dragState?.tableId === table.id}
            onPointerDown={adminMode ? (e) => handlePointerDown(e, table) : undefined}
          />
        ))}
      </svg>
    </div>
  );
}
