package as;

import javafx.application.Platform;
import javafx.beans.value.WritableStringValue;

import java.util.ArrayList;

/**
 * Created by Kelvin on 24-Feb-18.
 */
public class Progress implements Runnable {
    private PixelPainter painter;
    private Plane plane;
    private WritableStringValue millis;
    private CancelSupport cancelSupport;

    public Progress(PixelPainter painter, Plane plane, WritableStringValue millis, CancelSupport cancelSupport) {
        this.painter = painter;
        this.plane = plane;
        this.millis = millis;
        this.cancelSupport = cancelSupport;
    }

    @Override
    public void run() {
        double start = System.currentTimeMillis();
        // Replace the following line with Mandelbrot.computeParallel(...)
        try {
            ArrayList<Thread> ts = Mandelbrot.computeParallel(painter, plane, cancelSupport);
            for (Thread t: ts) {
                t.join();
            }
        } catch(InterruptedException e) {}

        double end = System.currentTimeMillis();
        Platform.runLater(() -> millis.set((end - start) + "ms"));
    }
}
