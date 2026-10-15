import { TestBed } from '@angular/core/testing';
import { mapInstances, markerInstances } from 'maplibre-gl';
import { JourneyMap } from './journey-map';
import type { Itinerary } from '@/shared/models/itinerary';
import type { Stop } from '@/shared/models/stop';

const origin: Stop = { id: 'STOP_A', name: 'Tunis Marine', latitude: 36.8008, longitude: 10.1815 };
const destination: Stop = { id: 'STOP_B', name: 'Marsa Plage', latitude: 36.87917, longitude: 10.325 };
const itinerary: Itinerary = {
  legs: [
    {
      mode: 'METRO',
      fromStopName: origin.name,
      fromStopLatitude: origin.latitude,
      fromStopLongitude: origin.longitude,
      toStopName: destination.name,
      toStopLatitude: destination.latitude,
      toStopLongitude: destination.longitude,
      departure: '08:00',
      arrival: '08:30',
      routeShortName: 'ligne TGM',
    },
  ],
  totalDurationSeconds: 1800,
  transferCount: 0,
};

function setUpTestBed() {
  markerInstances.length = 0;
  mapInstances.length = 0;
  return TestBed.configureTestingModule({ imports: [JourneyMap] }).compileComponents();
}

describe('JourneyMap', () => {
  beforeEach(setUpTestBed);

  it('creates a map centered on Tunis', async () => {
    const fixture = TestBed.createComponent(JourneyMap);
    await fixture.whenStable();

    expect(mapInstances).toHaveLength(1);
  });

  it('places markers for the origin and destination', async () => {
    const fixture = TestBed.createComponent(JourneyMap);
    fixture.componentRef.setInput('origin', origin);
    fixture.componentRef.setInput('destination', destination);
    await fixture.whenStable();

    expect(markerInstances).toHaveLength(2);
    expect(markerInstances[0].setLngLat).toHaveBeenCalledWith([origin.longitude, origin.latitude]);
    expect(markerInstances[1].setLngLat).toHaveBeenCalledWith([destination.longitude, destination.latitude]);
  });
});

describe('JourneyMap route rendering', () => {
  beforeEach(setUpTestBed);

  it('draws the selected itinerary as a route line', async () => {
    const fixture = TestBed.createComponent(JourneyMap);
    fixture.componentRef.setInput('selectedItinerary', itinerary);
    await fixture.whenStable();

    const map = mapInstances[0];
    expect(map.addSource).toHaveBeenCalledWith('selected-itinerary-route-source', expect.objectContaining({ type: 'geojson' }));
    expect(map.addLayer).toHaveBeenCalled();
  });

  it('updates the existing route source instead of re-adding it on a later change', async () => {
    const fixture = TestBed.createComponent(JourneyMap);
    fixture.componentRef.setInput('selectedItinerary', itinerary);
    await fixture.whenStable();

    const map = mapInstances[0];
    const fakeSource = { setData: jest.fn() };
    map.getSource.mockReturnValue(fakeSource);

    fixture.componentRef.setInput('selectedItinerary', null);
    await fixture.whenStable();

    expect(fakeSource.setData).toHaveBeenCalled();
    expect(map.addSource).toHaveBeenCalledTimes(1);
  });

  it('waits for the style to load before drawing the route when it is not ready yet', async () => {
    const fixture = TestBed.createComponent(JourneyMap);
    await fixture.whenStable();
    mapInstances[0].isStyleLoaded.mockReturnValue(false);

    fixture.componentRef.setInput('selectedItinerary', itinerary);
    await fixture.whenStable();

    const map = mapInstances[0];
    expect(map.once).toHaveBeenCalledWith('load', expect.any(Function));
    map.once.mock.calls[0][1]();
    expect(map.addSource).toHaveBeenCalled();
  });
});
