package netvis.view.util.jogl.comets;

import netvis.view.util.jogl.gameengine.Position;

import java.util.*;

public class Comet {
    private Queue<Position> tail;
    private List<Position> trace;
    private boolean tracefinished = false;
    private double posx;
    private double posy;
    private double velocityx;
    private double velocityy;
    private double colr;
    private double colg;
    private double colb;
    private double tilt;
    private double kx = 0.3;
    private double ky = 0.3;

    public Comet (int amp, double tt) {
        super();

        tail = new LinkedList<Position>();
        trace = new ArrayList<Position>();
        Random rand = new Random();
        double randspeed = rand.nextDouble() * 10 + 10;
        tilt = tt;
        posx = (int) Math.floor(Math.sin(tilt) * amp);
        posy = (int) Math.floor(Math.cos(tilt) * amp);
        velocityx = Math.sin(tilt + Math.PI/2) * randspeed;
        velocityy = Math.cos(tilt + Math.PI/2) * randspeed;
    }

    public final Collection<Position> getTail() {
        return tail;
    }

    public List<Position> getTrace() {
        return trace;
    }

    public int getx() {
        return (int) posx;
    }

    public int gety() {
        return (int) posy;
    }

    public double getTilt() {
        return tilt;
    }

    public void step(long time) {
        int x = (int) Math.floor(posx);
        int y = (int) Math.floor(posy);

        // Add an entry to keep track of the tail
        tail.add(new Position(x, y));

        // Keep only the last 20
        while (tail.size() > 20) {
            tail.poll();
        }

        if (!tracefinished || trace.size() < 100) {
            trace.add(new Position(x, y));

            // Do the angular sorting
            Collections.sort(trace, new Comparator<Position> () {
                @Override
                public int compare(Position o1, Position o2) {
                    if (angle(o1.x, o1.y) < angle(o2.x, o2.y)) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

            // Check whether the distances are small enough
            if (trace.size() >= 15) {
                tracefinished = true;
                Position oldp = trace.get(0);
                for (int i = 1; i < trace.size(); i++) {
                    Position newp = trace.get(i);
                    if (Math.abs(oldp.x - newp.x) > 10 || Math.abs(oldp.y - newp.y) > 10) {
                        // Fail - the trace is not finished
                        tracefinished = false;
                        break;
                    }
                    oldp = newp;
                }

                // Check the edge ones
                Position first = trace.get(0);
                Position last = trace.get(trace.size()-1);
                if (Math.abs (first.x - last.x) > 10 || Math.abs (first.y - last.y) > 10) {
                    tracefinished = false;
                }
            }
        }

        //System.out.println("Number of tail  elements : " + tail.size());
        //System.out.println("Number of trace elements : " + trace.size());

        // Update the position
        posx += velocityx * (time / 30.0);
        posy += velocityy * (time / 30.0);

        forcesAct(time);
    }

    public double angle(double posx, double posy) {
        return Math.atan2(posy, posx);
    }

    public void forcesAct(long time) {
        // Harmonic oscillator model
        velocityx -= (posx) * time/3000.0 * kx;
        velocityy -= (posy) * time/3000.0 * ky;

        //System.out.println("Time : " + time);

        // Gravitational model - maybe a better choice? - There is a lot of escaping though
        // Force is proportional to the 1/distance^2
        //double distance = Math.sqrt(Math.pow(posx, 2) + Math.pow(posy, 2));
        //velocityx -= posx * 10000 / Math.pow(distance, 3); // * kx;
        //velocityy -= posy * 10000 / Math.pow(distance, 3);
    }

    public void simulateTrace () {}
}
