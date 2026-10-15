import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToolbarModule } from 'primeng/toolbar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToolbarModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
