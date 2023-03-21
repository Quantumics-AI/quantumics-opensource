export const TAU = 2 * Math.PI;

export class Ball {
    x: number;
    y: number;
    startX: number;
    startY: number;
    vel;

    constructor(startX, startY, startVelX, startVelY, canvasWidth, canvasHeight) {
        this.startX = startX;
        this.startY = startY;
        this.x = startX || Math.random() * canvasWidth;
        this.y = startY || Math.random() * canvasHeight;
        this.vel = {
            x: startVelX || Math.random() * .5,
            y: startVelY || Math.random() * .5
        };

    }

    update(canvasWidth, canvasHeight) {

        if (this.x > canvasWidth + 50 || this.x < -50) {
            this.vel.x = -this.vel.x;
        }
        if (this.y > canvasHeight + 50 || this.y < -50) {
            this.vel.y = -this.vel.y;
        }

        if (this.x > this.startX + 50 || this.x < this.startX - 50) {
            this.vel.x = -this.vel.x;
        }
        if (this.y > this.startY + 50 || this.y < this.startY - 50) {
            this.vel.y = -this.vel.y;
        }
        this.x += this.vel.x;
        this.y += this.vel.y;
    }

    draw(ctx, can) {
        ctx.beginPath();
        ctx.globalAlpha = 1;
        ctx.fillStyle = '#5cbdaa';
        ctx.arc((0.5 + this.x) | 0, (0.5 + this.y) | 0, 6, 0, TAU, false);
        ctx.fill();
    }
}
