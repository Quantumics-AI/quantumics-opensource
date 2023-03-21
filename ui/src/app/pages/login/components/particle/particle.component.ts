import { Component, AfterViewInit, ViewChild, ElementRef, ChangeDetectionStrategy } from '@angular/core';
import { Ball } from './ball.model';

@Component({
  selector: 'app-particle',
  templateUrl: './particle.component.html',
  styleUrls: ['./particle.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ParticleComponent implements AfterViewInit {

  @ViewChild('particleCanvas', { static: false }) particalCanvas: ElementRef;
  public context: CanvasRenderingContext2D;

  readonly TAU = 2 * Math.PI;
  times = [];
  balls = [];
  lastTime = Date.now();

  canvasWidth;
  canvasHeight;

  ngAfterViewInit(): void {
    this.particalCanvas.nativeElement.width = 1000;
    this.particalCanvas.nativeElement.height = 1000;

    this.canvasWidth = this.particalCanvas.nativeElement.width;
    this.canvasHeight = this.particalCanvas.nativeElement.height

    this.context = (<HTMLCanvasElement>this.particalCanvas.nativeElement).getContext('2d');
    this.setupCanvas();
  }

  setupCanvas() {
    for (var i = 0; i < this.canvasWidth * this.canvasHeight / (125 * 125); i++) {
      this.balls.push(new Ball(Math.random() * this.canvasWidth, Math.random() * this.canvasHeight, 0, 0, this.canvasWidth, this.canvasHeight));
    }

    this.loop();
  }

  loop() {
    requestAnimationFrame(this.loop.bind(this));


    this.context.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
    this.update();
    this.draw();

  }

  update() {

    let diff = Date.now() - this.lastTime;
    for (var frame = 0; frame * 16.6667 < diff; frame++) {
      for (var index = 0; index < this.balls.length; index++) {

        this.balls[index].update(this.canvasWidth, this.canvasHeight);
      }
    }
    this.lastTime = Date.now();
  }


  draw() {
    this.context.globalAlpha = 1;
    this.context.fillStyle = 'transparent';
    this.context.fillRect(0, 0, this.canvasWidth, this.canvasHeight);
    for (var index = 0; index < this.balls.length; index++) {
      var ball = this.balls[index];
      ball.draw(this.context, this.particalCanvas);
      this.context.beginPath();
      for (var index2 = this.balls.length - 1; index2 > index; index2 += -1) {
        var ball2 = this.balls[index2];
        var dist = Math.hypot(ball.x - ball2.x, ball.y - ball2.y);
        if (dist < 150) {
          this.context.strokeStyle = "#ffffff";
          this.context.globalAlpha = 1 - (dist / 150);
          this.context.lineWidth = 1;
          this.context.moveTo((0.5 + ball.x) | 0, (0.5 + ball.y) | 0);
          this.context.lineTo((0.5 + ball2.x) | 0, (0.5 + ball2.y) | 0);
        }
      }
      this.context.stroke();
    }
  }
}