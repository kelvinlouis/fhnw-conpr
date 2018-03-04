package as;

import javafx.scene.paint.Color;

import static as.Mandelbrot.mandel;

/**
 * Created by Kelvin on 24-Feb-18.
 */
public class RangePainter implements Runnable {
    private int xStart;
    private int xEnd;
    private double reMin;
    private double imMax;
    private double step;
    private PixelPainter painter;
    private CancelSupport cancel;

    public RangePainter(int xStart, int xEnd, double reMin, double imMax, double step, PixelPainter painter, CancelSupport cancel) {
        this.xStart = xStart;
        this.xEnd = xEnd;
        this.reMin = reMin;
        this.imMax = imMax;
        this.step = step;
        this.painter = painter;
        this.cancel = cancel;
    }

    @Override
    public void run() {
        for (int x = xStart; x < xEnd && !cancel.isCancelled(); x++) { // x-axis
            double re = reMin + x * step; // map pixel to complex plane
            for (int y = 0; y < Mandelbrot.IMAGE_LENGTH; y++) { // y-axis
                double im = imMax - y * step; // map pixel to complex plane

                int iterations = mandel(new Complex(re, im));
                painter.paint(x, y, Mandelbrot.getColor(iterations));
            }
        }
    }
}
