/**
 * Jest can't resolve the real maplibre-gl package (it ships ESM-only, no "require" export
 * condition) and it needs WebGL, which jsdom doesn't provide. This manual mock stands in for
 * it in every spec that transitively imports JourneyMap, recording constructor calls so tests
 * can assert on them without touching the real rendering engine.
 */
export const markerInstances: FakeMarker[] = [];
export const mapInstances: FakeMap[] = [];

export class FakeMarker {
  setLngLat = jest.fn().mockReturnThis();
  setPopup = jest.fn().mockReturnThis();
  addTo = jest.fn().mockReturnThis();
  remove = jest.fn();

  constructor() {
    markerInstances.push(this);
  }
}

export class FakeMap {
  addControl = jest.fn();
  addSource = jest.fn();
  addLayer = jest.fn();
  getSource = jest.fn().mockReturnValue(undefined);
  isStyleLoaded = jest.fn().mockReturnValue(true);
  once = jest.fn();
  remove = jest.fn();

  constructor() {
    mapInstances.push(this);
  }
}

export class Popup {
  setText = jest.fn().mockReturnThis();
}

export const Map = FakeMap;
export const Marker = FakeMarker;
export const NavigationControl = jest.fn();
export const setWorkerUrl = jest.fn();
