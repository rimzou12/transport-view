import { DurationPipe } from './duration.pipe';

describe('DurationPipe', () => {
  const pipe = new DurationPipe();

  it('formats sub-hour durations in minutes only', () => {
    expect(pipe.transform(600)).toBe('10 min');
  });

  it('formats durations over an hour as hours and minutes', () => {
    expect(pipe.transform(5400)).toBe('1 h 30 min');
  });

  it('rounds to the nearest minute', () => {
    expect(pipe.transform(95)).toBe('2 min');
  });
});
