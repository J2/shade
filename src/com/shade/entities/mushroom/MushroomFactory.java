package com.shade.entities.mushroom;

import java.util.LinkedList;

import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;

import com.crash.Body;
import com.shade.entities.Basket;
import com.shade.util.Geom;

public class MushroomFactory {

    /**
     * Corresponds to the Mushroom.Type enum.
     */
    private static final double[] distribution = { 0, .965, 0, .35 };
    private static final double PROPENSITY = .002;
    private static final float BASKET_THRESHOLD = 10000;

    /* Minimum number of mushrooms alive at any one time. */
    private int floor;
    private double propensity;
    /*
     * List of existing mushrooms. Mushrooms should remove themselves when
     * removed from the level.
     */
    private LinkedList<Mushroom> mushrooms;

    /**
     * @param floor The baseline number of mushrooms on the field.
     * @param propensity The likelihood to add a mushroom over the baseline.
     */
    public MushroomFactory(int floor) {
        this.floor = floor;
        this.propensity = PROPENSITY;
        mushrooms = new LinkedList<Mushroom>();
    }

    /**
     * Return true you should create a new mushroom.
     * 
     * @return
     */
    public boolean active() {
        if (mushrooms.size() < floor) {
            return true;
        }
        return (Math.random() <= propensity);
    }

    public Mushroom getMushroom(GameContainer c, Shape shadow, Body b)
            throws SlickException {
        try {
            float x = randomX(c, shadow);
            float y = randomY(c, shadow);
            if (x == -1 || y == -1
                    || Geom.distance2(x, y, b.getXCenter(), b.getYCenter()) < BASKET_THRESHOLD) {
                return null;
            }
            int t = randomType();
            Mushroom m = new Mushroom(x, y, getType(t), this);
            mushrooms.add(m);
            return m;
        } catch (MushroomFactoryException e) {
            throw new SlickException(e.getMessage());
        }
    }

    private float randomX(GameContainer c, Shape s)
            throws MushroomFactoryException {
        float x = -1;
        int numTries = 0;
        while ((x < 0 || x >= c.getWidth()) && numTries < 6) {
            x = (float) (s.getMaxX() - s.getMinX() * Math.random() * 0.5);
            x += s.getX();
            numTries++;
            if (numTries > 6) {
                throw new MushroomFactoryException("Can't find valid point.");
            }
        }
        return (numTries < 6) ? x : -1;
    }

    private float randomY(GameContainer c, Shape s)
            throws MushroomFactoryException {
        float y = -1;
        int numTries = 0;
        while ((y < 0 || y >= c.getHeight()) && numTries < 6) {
            y = (float) (s.getMaxY() - s.getMinY() * Math.random() * 0.5);
            y += s.getY();
            numTries++;
            if (numTries > 6) {
                throw new MushroomFactoryException("Can't find valid point.");
            }
        }
        return (numTries < 6) ? y : -1;
    }

    public void remove(Mushroom m) {
        mushrooms.remove(m);
    }

    private Mushroom.Types getType(int i) {
        Mushroom.Types[] types = Mushroom.Types.values();
        return types[i];
    }

    private int randomType() {
        double r = Math.random();

        double max = 0;
        for (int i = 0; i < distribution.length; i++) {
            max += distribution[i];
            if (r <= max)
                return i;
        }
        return 0; // should never reach here
    }

    @SuppressWarnings("serial")
    private class MushroomFactoryException extends Exception {

        public MushroomFactoryException(String message) {
            super(message);
        }
    }
}
