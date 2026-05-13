import { useEffect } from 'react'
import { MapContainer, TileLayer, Marker, Circle, Popup, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Fix default Leaflet marker icons (Vite + react-leaflet path issue).
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
})

// Custom red icon for current user location.
const meIcon = new L.Icon({
  iconUrl: 'data:image/svg+xml;utf8,' + encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="28" height="36" viewBox="0 0 28 36">
      <path fill="#dc2626" stroke="#7f1d1d" stroke-width="1.5"
        d="M14 2 C7 2 2 7 2 14 c0 8 12 20 12 20 s12-12 12-20 c0-7-5-12-12-12 z"/>
      <circle fill="#fff" cx="14" cy="13" r="4.5"/>
    </svg>`),
  iconSize: [28, 36],
  iconAnchor: [14, 36],
  popupAnchor: [0, -32],
})

function Recenter({ center }) {
  const map = useMap()
  useEffect(() => {
    if (center) map.setView(center, map.getZoom(), { animate: true })
  }, [center, map])
  return null
}

/**
 * Bản đồ Leaflet (OpenStreetMap) hiển thị offices + tuỳ chọn vị trí hiện tại.
 *
 * Props:
 * - offices: [{ id, name, latitude, longitude, radiusMeters, status }]
 * - currentPosition: [lat, lng] | null
 * - selectedOfficeId: number | null
 * - onSelectOffice: (id) => void          (optional, dùng khi cần chọn office)
 * - height: chiều cao map, mặc định 360px
 */
export default function OfficeMap({
  offices = [],
  currentPosition = null,
  selectedOfficeId = null,
  onSelectOffice,
  height = 360,
}) {
  // Center mặc định: vị trí hiện tại > office đầu tiên > giữa TPHCM
  const fallbackCenter = [10.7769, 106.7009] // TP. HCM
  const center = currentPosition
    || (offices.length > 0 ? [offices[0].latitude, offices[0].longitude] : fallbackCenter)

  return (
    <div className="rounded-xl overflow-hidden border border-gray-200 shadow-sm">
      <MapContainer center={center} zoom={15} style={{ height, width: '100%' }} scrollWheelZoom>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Recenter center={currentPosition || (offices[0] && [offices[0].latitude, offices[0].longitude])} />

        {offices.map((o) => (
          <div key={o.id}>
            <Circle
              center={[o.latitude, o.longitude]}
              radius={o.radiusMeters || 100}
              pathOptions={{
                color: o.id === selectedOfficeId ? '#7c3aed' : '#2563eb',
                fillColor: o.id === selectedOfficeId ? '#a78bfa' : '#60a5fa',
                fillOpacity: 0.18,
                weight: 2,
              }}
              eventHandlers={onSelectOffice ? { click: () => onSelectOffice(o.id) } : undefined}
            />
            <Marker position={[o.latitude, o.longitude]}>
              <Popup>
                <div className="text-sm">
                  <p className="font-semibold mb-1">{o.name}</p>
                  {o.address && <p className="text-gray-600 mb-1">{o.address}</p>}
                  <p className="text-gray-500">
                    Bán kính: <b>{o.radiusMeters}m</b>
                    {' '}— Trạng thái: <b>{o.status}</b>
                  </p>
                </div>
              </Popup>
            </Marker>
          </div>
        ))}

        {currentPosition && (
          <Marker position={currentPosition} icon={meIcon}>
            <Popup>Vị trí của bạn</Popup>
          </Marker>
        )}
      </MapContainer>
    </div>
  )
}
