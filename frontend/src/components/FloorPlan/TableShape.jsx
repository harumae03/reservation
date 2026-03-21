const STATUS_COLORS = {
  available: { fill: '#e8eee9', stroke: '#a0b0a3', text: '#4a5a4e', chair: '#d0ddd2', chairStroke: '#a0b0a3' },
  occupied: { fill: '#e8e8e8', stroke: '#b0b0b0', text: '#888', chair: '#d5d5d5', chairStroke: '#b0b0b0' },
  recommended_top: { fill: '#4caf50', stroke: '#2e7d32', text: '#fff', chair: '#66bb6a', chairStroke: '#2e7d32' },
  recommended: { fill: '#ffddb6', stroke: '#e6a54a', text: '#5d4037', chair: '#ffe0b2', chairStroke: '#e6a54a' },
  selected: { fill: '#516356', stroke: '#324338', text: '#eafeed', chair: '#738678', chairStroke: '#324338' },
};

export default function TableShape({ table, onClick }) {
  const isTopRecommended = table.status === 'recommended' && table.rank && table.rank <= 3;
  const colorKey = isTopRecommended ? 'recommended_top' : table.status;
  const colors = STATUS_COLORS[colorKey] || STATUS_COLORS.available;
  const isClickable = table.status !== 'occupied';
  const isHighlighted = table.status === 'recommended' || table.status === 'selected';
  const isOccupied = table.status === 'occupied';

  const cx = table.posX + table.width / 2;
  const cy = table.posY + table.height / 2;

  const chairs = generateChairs(table);

  return (
    <g
      onClick={isClickable ? onClick : undefined}
      style={{ cursor: isClickable ? 'pointer' : 'default', opacity: isOccupied ? 0.5 : 1 }}
      role={isClickable ? 'button' : undefined}
      tabIndex={isClickable ? 0 : undefined}
    >
      {/* Glow effect for recommended/selected */}
      {isHighlighted && (
        <rect
          x={table.posX - 5}
          y={table.posY - 5}
          width={table.width + 10}
          height={table.height + 10}
          rx={14}
          fill="none"
          stroke={table.status === 'recommended' ? '#e6a54a' : '#516356'}
          strokeWidth={2.5}
          opacity={0.35}
          style={{ animation: table.status === 'recommended' ? 'pulse-glow 2s ease-in-out infinite' : 'none' }}
        />
      )}

      {/* Shadow under table */}
      {!isOccupied && (
        <rect
          x={table.posX + 2}
          y={table.posY + 2}
          width={table.width}
          height={table.height}
          rx={10}
          fill="rgba(0,0,0,0.06)"
        />
      )}

      {/* Table surface */}
      <rect
        x={table.posX}
        y={table.posY}
        width={table.width}
        height={table.height}
        rx={10}
        fill={colors.fill}
        stroke={colors.stroke}
        strokeWidth={isHighlighted ? 2 : 1.5}
      />

      {/* Occupied cross-hatch pattern */}
      {isOccupied && (
        <>
          <line
            x1={table.posX + 8} y1={table.posY + 8}
            x2={table.posX + table.width - 8} y2={table.posY + table.height - 8}
            stroke="#bbb" strokeWidth={1} opacity={0.3}
          />
          <line
            x1={table.posX + table.width - 8} y1={table.posY + 8}
            x2={table.posX + 8} y2={table.posY + table.height - 8}
            stroke="#bbb" strokeWidth={1} opacity={0.3}
          />
        </>
      )}

      {/* Chairs */}
      {chairs.map((chair, i) => (
        <circle
          key={i}
          cx={chair.x}
          cy={chair.y}
          r={6}
          fill={colors.chair}
          stroke={colors.chairStroke}
          strokeWidth={1}
        />
      ))}

      {/* Table number & capacity */}
      <text
        x={cx}
        y={cy - 4}
        textAnchor="middle"
        fill={colors.text}
        fontSize={12}
        fontWeight={700}
        fontFamily="Inter, sans-serif"
      >
        T{table.tableNumber}
      </text>
      <text
        x={cx}
        y={cy + 10}
        textAnchor="middle"
        fill={colors.text}
        fontSize={9}
        fontFamily="Inter, sans-serif"
        opacity={0.75}
      >
        {table.capacity} kohta
      </text>

      {/* "PARIM" badge for rank 1 recommended */}
      {table.status === 'recommended' && table.rank === 1 && (
        <g>
          <rect
            x={cx - 20}
            y={table.posY - 18}
            width={40}
            height={16}
            rx={8}
            fill="#516356"
          />
          <text
            x={cx}
            y={table.posY - 7}
            textAnchor="middle"
            fill="#eafeed"
            fontSize={8}
            fontWeight={700}
            fontFamily="Inter, sans-serif"
          >
            PARIM
          </text>
        </g>
      )}
    </g>
  );
}

function generateChairs(table) {
  const chairs = [];
  const { posX, posY, width, height, capacity } = table;
  const cx = posX + width / 2;
  const cy = posY + height / 2;
  const pad = 10;

  if (capacity <= 2) {
    chairs.push({ x: cx, y: posY - pad });
    chairs.push({ x: cx, y: posY + height + pad });
  } else if (capacity <= 4) {
    chairs.push({ x: cx, y: posY - pad });
    chairs.push({ x: cx, y: posY + height + pad });
    chairs.push({ x: posX - pad, y: cy });
    chairs.push({ x: posX + width + pad, y: cy });
  } else if (capacity <= 6) {
    const third = width / 3;
    chairs.push({ x: posX + third, y: posY - pad });
    chairs.push({ x: posX + 2 * third, y: posY - pad });
    chairs.push({ x: posX + third, y: posY + height + pad });
    chairs.push({ x: posX + 2 * third, y: posY + height + pad });
    chairs.push({ x: posX - pad, y: cy });
    chairs.push({ x: posX + width + pad, y: cy });
  } else {
    const quarter = width / 4;
    chairs.push({ x: posX + quarter, y: posY - pad });
    chairs.push({ x: cx, y: posY - pad });
    chairs.push({ x: posX + 3 * quarter, y: posY - pad });
    chairs.push({ x: posX + quarter, y: posY + height + pad });
    chairs.push({ x: cx, y: posY + height + pad });
    chairs.push({ x: posX + 3 * quarter, y: posY + height + pad });
    chairs.push({ x: posX - pad, y: cy });
    chairs.push({ x: posX + width + pad, y: cy });
  }

  return chairs.slice(0, capacity);
}
