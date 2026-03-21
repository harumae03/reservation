import TableShape from './TableShape.jsx';

const ZONE_AREAS = [
  { zone: 'TERRACE', label: 'Terrass', x: 80, y: 30, width: 850, height: 140, fill: '#e8f5e9', stroke: '#81c784', opacity: 0.45, labelColor: '#2e7d32' },
  { zone: 'INDOOR_WINDOW', label: 'Aknakoht (vasak)', x: 30, y: 185, width: 140, height: 320, fill: '#e3f2fd', stroke: '#90caf9', opacity: 0.35, labelColor: '#1565c0' },
  { zone: 'INDOOR_MAIN', label: 'Sisesaal', x: 190, y: 185, width: 560, height: 320, fill: '#f5f5f5', stroke: '#bdbdbd', opacity: 0.5, labelColor: '#616161' },
  { zone: 'INDOOR_WINDOW', label: 'Aknakoht (parem)', x: 820, y: 185, width: 140, height: 250, fill: '#e3f2fd', stroke: '#90caf9', opacity: 0.35, labelColor: '#1565c0' },
  { zone: 'PRIVATE_ROOM', label: 'Privaatruum', x: 590, y: 460, width: 360, height: 200, fill: '#fce4ec', stroke: '#f48fb1', opacity: 0.25, labelColor: '#ad1457' },
];

export default function FloorPlan({ tables, onTableClick }) {
  return (
    <div className="floor-plan-container">
      <svg
        className="floor-plan-svg"
        viewBox="0 0 1020 680"
        preserveAspectRatio="xMidYMid meet"
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
        <circle cx={65} cy={75} r={8} fill="#d3e8d7" opacity={0.6} />
        <circle cx={965} cy={75} r={8} fill="#d3e8d7" opacity={0.6} />
        <circle cx={65} cy={135} r={6} fill="#e2f6ba" opacity={0.5} />
        <circle cx={965} cy={135} r={6} fill="#e2f6ba" opacity={0.5} />

        {/* Window lines on left */}
        <line x1={30} y1={195} x2={30} y2={495} stroke="#abb4ab" strokeWidth={2} strokeDasharray="8 4" opacity={0.4} />
        {/* Window lines on right */}
        <line x1={960} y1={195} x2={960} y2={425} stroke="#abb4ab" strokeWidth={2} strokeDasharray="8 4" opacity={0.4} />

        {/* Playground area indicator */}
        <g opacity={0.4}>
          <rect x={100} y={520} width={120} height={80} rx={12} fill="#e2f6ba" opacity={0.5} />
          <text x={125} y={568} fill="#546536" fontSize={9} fontFamily="Inter, sans-serif">Mängunurk</text>
        </g>

        {/* Tables */}
        {tables.map(table => (
          <TableShape
            key={table.id}
            table={table}
            onClick={() => onTableClick(table)}
          />
        ))}
      </svg>
    </div>
  );
}
