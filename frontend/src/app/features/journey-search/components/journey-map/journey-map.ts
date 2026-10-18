import { ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input, viewChild } from '@angular/core';
import type { ElementRef } from '@angular/core';
import { Map as MaplibreMap, Marker, NavigationControl, Popup, setWorkerUrl, type GeoJSONSource } from 'maplibre-gl';
import type { Itinerary } from '@/shared/models/itinerary';
import type { Stop } from '@/shared/models/stop';

const TUNIS_CENTER: [number, number] = [10.1815, 36.8065];
const DEFAULT_ZOOM = 12;
const SELECTED_ROUTE_LAYER = 'selected-itinerary-route';
const SELECTED_ROUTE_SOURCE = 'selected-itinerary-route-source';

// MapLibre 6 doesn't inline its worker and can't reliably resolve import.meta.url through
// Angular's esbuild bundler, so the worker must be pointed at an explicit, self-hosted URL
// (copied into the build output via angular.json's assets config) before the first map loads.
setWorkerUrl('/maplibre-gl-worker.mjs');

@Component({
  selector: 'app-journey-map',
  template: `<div #mapContainer class="journey-map" role="region" aria-label="Map of Tunis showing the selected journey"></div>`,
  styleUrl: './journey-map.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JourneyMap {
  readonly origin = input<Stop | null>(null);
  readonly destination = input<Stop | null>(null);
  readonly selectedItinerary = input<Itinerary | null>(null);

  private readonly mapContainer = viewChild.required<ElementRef<HTMLDivElement>>('mapContainer');
  private readonly destroyRef = inject(DestroyRef);
  private map: MaplibreMap | undefined;
  private originMarker: Marker | undefined;
  private destinationMarker: Marker | undefined;

  constructor() {
    effect((onCleanup) => {
      const map = this.createMap();
      onCleanup(() => map.remove());
    });

    effect(() => this.updateMarkers());
    effect(() => this.updateRoute());

    this.destroyRef.onDestroy(() => this.map?.remove());
  }

  private createMap(): MaplibreMap {
    const map = new MaplibreMap({
      container: this.mapContainer().nativeElement,
      style: 'https://basemaps.cartocdn.com/gl/positron-gl-style/style.json',
      center: TUNIS_CENTER,
      zoom: DEFAULT_ZOOM,
    });
    map.addControl(new NavigationControl(), 'top-right');
    this.map = map;
    return map;
  }

  private updateMarkers(): void {
    const map = this.map;
    /* istanbul ignore if -- defensive: the map-creation effect always runs first, so this effect never observes an unset map */
    if (!map) {
      return;
    }

    this.originMarker?.remove();
    this.destinationMarker?.remove();

    this.originMarker = this.createMarker(map, this.origin(), '#1a73e8');
    this.destinationMarker = this.createMarker(map, this.destination(), '#d93025');
  }

  private createMarker(map: MaplibreMap, stop: Stop | null, color: string): Marker | undefined {
    if (!stop) {
      return undefined;
    }
    return new Marker({ color })
      .setLngLat([stop.longitude, stop.latitude])
      .setPopup(new Popup().setText(stop.name))
      .addTo(map);
  }

  private updateRoute(): void {
    const map = this.map;
    /* istanbul ignore if -- defensive: the map-creation effect always runs first, so this effect never observes an unset map */
    if (!map) {
      return;
    }

    const itinerary = this.selectedItinerary();
    const geoJsonSource: GeoJSON.FeatureCollection = {
      type: 'FeatureCollection',
      features: (itinerary?.legs ?? []).map((leg) => ({
        type: 'Feature',
        properties: {},
        geometry: {
          type: 'LineString',
          coordinates: [
            [leg.fromStopLongitude, leg.fromStopLatitude],
            [leg.toStopLongitude, leg.toStopLatitude],
          ],
        },
      })),
    };

    const applyRoute = () => this.applyRouteSource(map, geoJsonSource);
    if (map.isStyleLoaded()) {
      applyRoute();
    } else {
      map.once('load', applyRoute);
    }
  }

  private applyRouteSource(map: MaplibreMap, data: GeoJSON.FeatureCollection): void {
    const existingSource = map.getSource(SELECTED_ROUTE_SOURCE) as GeoJSONSource | undefined;
    if (existingSource) {
      existingSource.setData(data);
      return;
    }

    map.addSource(SELECTED_ROUTE_SOURCE, { type: 'geojson', data });
    map.addLayer({
      id: SELECTED_ROUTE_LAYER,
      type: 'line',
      source: SELECTED_ROUTE_SOURCE,
      paint: { 'line-color': '#1a73e8', 'line-width': 4 },
    });
  }
}
