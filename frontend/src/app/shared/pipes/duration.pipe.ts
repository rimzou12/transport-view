import { Pipe, type PipeTransform } from '@angular/core';

@Pipe({ name: 'duration', pure: true })
export class DurationPipe implements PipeTransform {
  transform(totalSeconds: number): string {
    const totalMinutes = Math.round(totalSeconds / 60);
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    if (hours === 0) {
      return `${minutes} min`;
    }
    return `${hours} h ${minutes} min`;
  }
}
