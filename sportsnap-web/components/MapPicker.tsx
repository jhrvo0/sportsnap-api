"use client";

import { useEffect, useState, useRef, useMemo } from "react";
import { MapContainer, TileLayer, Marker, useMap, useMapEvents } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { Input } from "./Input";

// Fix for default marker icons in Next.js + Leaflet
const DefaultIcon = L.icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41],
});
L.Marker.prototype.options.icon = DefaultIcon;

type Props = {
  latitude: number;
  longitude: number;
  onChange?: (lat: number, lng: number) => void;
  onAddressFound?: (address: string) => void;
  readOnly?: boolean;
  height?: string;
};

// Component to handle map center changes dynamically
function MapCenterUpdater({ center }: { center: [number, number] }) {
  const map = useMap();
  useEffect(() => {
    map.setView(center, map.getZoom());
  }, [center, map]);
  return null;
}

// Component to handle map clicks for dropping the pin
function MapClickHandler({ onChange, readOnly, onMapClick }: { onChange?: (lat: number, lng: number) => void, readOnly: boolean, onMapClick?: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(e) {
      if (!readOnly && onChange) {
        onChange(e.latlng.lat, e.latlng.lng);
        if (onMapClick) onMapClick(e.latlng.lat, e.latlng.lng);
      }
    },
  });
  return null;
}

type Suggestion = {
  place_id: number;
  lat: string;
  lon: string;
  display_name: string;
};

export default function MapPicker({ latitude, longitude, onChange, onAddressFound, readOnly = false, height = "300px" }: Props) {
  const [searchQuery, setSearchQuery] = useState("");
  const [suggestions, setSuggestions] = useState<Suggestion[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [position, setPosition] = useState<[number, number]>([latitude || -23.5505, longitude || -46.6333]);

  // Debounce timeout ref
  const debounceRef = useRef<NodeJS.Timeout>();

  useEffect(() => {
    if (latitude && longitude && !isNaN(latitude) && !isNaN(longitude)) {
      setPosition([latitude, longitude]);
    }
  }, [latitude, longitude]);

  const markerRef = useRef<L.Marker>(null);
  
  async function fetchAddress(lat: number, lon: number) {
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`);
      if (res.ok) {
        const data = await res.json();
        if (data.display_name) {
          setSearchQuery(data.display_name);
          if (onAddressFound) onAddressFound(data.display_name);
        }
      }
    } catch (err) {
      console.error("Erro no reverse geocode", err);
    }
  }

  const eventHandlers = useMemo(
    () => ({
      dragend() {
        const marker = markerRef.current;
        if (marker != null && onChange) {
          const { lat, lng } = marker.getLatLng();
          setPosition([lat, lng]);
          onChange(lat, lng);
          fetchAddress(lat, lng);
        }
      },
    }),
    [onChange]
  );

  async function fetchSuggestions(query: string) {
    if (!query.trim()) {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }
    
    setIsSearching(true);
    try {
      const res = await fetch(`/api/geocode?q=${encodeURIComponent(query)}`);
      if (res.ok) {
        const data = await res.json();
        setSuggestions(data);
        setShowSuggestions(true);
      }
    } catch (err) {
      console.error("Erro ao buscar endereço", err);
    } finally {
      setIsSearching(false);
    }
  }

  function handleSearchChange(e: React.ChangeEvent<HTMLInputElement>) {
    const val = e.target.value;
    setSearchQuery(val);

    if (debounceRef.current) clearTimeout(debounceRef.current);
    
    debounceRef.current = setTimeout(() => {
      fetchSuggestions(val);
    }, 500);
  }

  function handleSelectSuggestion(s: Suggestion) {
    const lat = parseFloat(s.lat);
    const lon = parseFloat(s.lon);
    setPosition([lat, lon]);
    setSearchQuery(s.display_name);
    setShowSuggestions(false);
    if (onChange) onChange(lat, lon);
  }

  if (readOnly && (isNaN(latitude) || isNaN(longitude))) {
     return <div className="bg-ink-100 flex items-center justify-center text-ink-400 text-xs" style={{ height, borderRadius: '1.5rem' }}>Localização indisponível</div>
  }

  return (
    <div className="flex flex-col gap-3 w-full relative">
      {!readOnly && (
        <div className="relative z-[1001]">
          <Input 
            className="w-full h-11 text-sm"
            placeholder="Digite o endereço e escolha na lista..." 
            value={searchQuery} 
            onChange={handleSearchChange} 
            onFocus={() => { if (suggestions.length > 0) setShowSuggestions(true); }}
          />
          {isSearching && (
            <div className="absolute right-3 top-3 h-4 w-4 rounded-full border-2 border-accent border-t-transparent animate-spin" />
          )}
          
          {showSuggestions && suggestions.length > 0 && (
            <div className="absolute top-full left-0 right-0 mt-2 bg-white rounded-2xl shadow-xl border border-ink-100 overflow-hidden max-h-60 overflow-y-auto z-[1001]">
              <ul className="divide-y divide-ink-100">
                {suggestions.map(s => (
                  <li 
                    key={s.place_id} 
                    className="p-3 text-sm text-ink-700 hover:bg-ink-50 cursor-pointer transition-colors"
                    onClick={() => handleSelectSuggestion(s)}
                  >
                    <span className="mr-2 opacity-50">📍</span>
                    {s.display_name}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
      
      {/* Overlay to close suggestions when clicking outside */}
      {!readOnly && showSuggestions && (
        <div className="fixed inset-0 z-[1000]" onClick={() => setShowSuggestions(false)} />
      )}
      
      <div style={{ height }} className="w-full rounded-[1.5rem] overflow-hidden border border-ink-200 z-0 relative isolate">
        <MapContainer center={position} zoom={15} scrollWheelZoom={true} style={{ height: "100%", width: "100%", zIndex: 0 }}>
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <MapCenterUpdater center={position} />
          {!readOnly && <MapClickHandler onChange={onChange} readOnly={readOnly} onMapClick={fetchAddress} />}
          <Marker 
            position={position} 
            draggable={!readOnly} 
            eventHandlers={!readOnly ? eventHandlers : undefined}
            ref={markerRef}
          />
        </MapContainer>
        {!readOnly && (
           <div className="absolute bottom-3 left-1/2 -translate-x-1/2 bg-white/95 backdrop-blur-md px-4 py-2 rounded-full border border-ink-100 text-[11px] font-bold text-center shadow-lg z-[999] pointer-events-none text-ink-900 shadow-accent/10">
             👆 Arraste o pino para ajustar
           </div>
        )}
      </div>
    </div>
  );
}
