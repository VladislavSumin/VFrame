package ru.falseteam.vframe.redraw;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains redrawable and redraw data
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
public class Redrawer {
    private static final Set<Redrawable> redrawables = new HashSet<>();

    public static void redraw() {
        synchronized (redrawables) {
            for (Redrawable redrawable : redrawables) {
                redrawable.redraw();
            }
        }
    }

    public static void addRedrawable(Redrawable redrawable) {
        synchronized (redrawables) {
            redrawables.add(redrawable);
        }
    }

    public static void removeRedrawable(Redrawable redrawable) {
        synchronized (redrawables) {
            redrawables.remove(redrawable);
        }
    }
}
