package netvis.view.util.jogl.gameengine;

public class Units {
    public static Position findSpotAround (int size) {
        int outerring = 1;
        while (dimToCap(outerring) <= size) {
            outerring += 1;
        }

        int shift = size - dimToCap(outerring - 1);

        return new Position (outerring, shift);
    }

    public static Position actualRingAndShift(int dim, Position rs) {
        int ring  = rs.x;
        int shift = rs.y;

        ring  = (ring-1) * (2*dim - 1) + 1;
        shift *= (2*dim - 1);

        Position p = new Position (ring, shift);

        return p;
    }

    public static int dimToCap (int dim) {
        if (dim == 0) return 0;
        return 3*(dim*dim - dim)+1;
    }

    public static Position coordinateByRingAndShift(int dim, int ring, int shift) {
        int comp = -1;
        int rel = 0;
        if (ring != 1) {
            rel = (shift % (ring-1));
            if (rel != 0) {
                comp = shift / (ring-1);
            }
            rel = Math.min (Math.abs (ring - 1 - rel), rel);
        }

        Position rs = actualRingAndShift(dim, new Position(ring, shift));

        ring  = rs.x;
        shift = rs.y;

        Position p = new Position (0,0);

        // First go to the right ring
        p.y += (ring-1);
        p.x -= (ring-1)/2; // Rounded down (look at the Fig 1)

        // Now move around the ring to find the right spot
        int [] xstages = new int [] {+1, +1,  0, -1, -1, 0, +1, +1,  0}; // last three repeat
        int [] ystages = new int [] {0,  -1, -1,  0, +1, +1, 0, -1, -1};

        int [] xcompens = new int [] { 0, -1, -1,  0, +1, +1,  0, -1, -1};
        int [] ycompens = new int [] {-1,  0, +1, +1,  0, -1, -1,  0, +1};

        int stageid = 0;
        int dx = xstages[0];
        int dy = ystages[0];

        int delta = ((ring-1)/2);

        // This changes the adjacency style
        //shift += delta;

        // Look at the Fig 2
        for (int i=0; i < shift; i++) {
            if ((i-delta) % (ring-1) == 0) {
                // Switch to the next stage
                stageid += 1;
                dx = xstages[stageid];
                dy = ystages[stageid];
            }

            p.x += dx;
            p.y += dy;
        }

        // Apply compensation
        if (comp != -1) {
            p.x += rel * (dim-1) * xcompens[comp];
            p.y += rel * (dim-1) * ycompens[comp];
        }

        return p;
    }

    public static Position positionByCoordinate(int base, Position coord) {
        // Converts the position from the skewed Euclidean to Euclidean
        Position pos = new Position (0,0);

        pos.x += Math.round(coord.x * Math.sqrt(3.0) * base + coord.y * Math.sqrt(3.0) * base * Math.cos(Math.PI/3));
        pos.y += Math.round(coord.y * Math.sqrt(3.0) * base * Math.sin(Math.PI/3));

        return pos;
    }

    public static Position metaPositionByCoordinate(int dim, int base, Position pos) {
        return Units.positionByCoordinate (base*(2*dim-1), pos);
    }

    public static Position coordinateByPosition(int base, Position pos) {
        double r3 = Math.sqrt(3.0);
        Position p = new Position(0, 0);

        p.y = (int) Math.round (pos.y / (base * r3 * Math.sin(Math.PI/3)));
        p.x = (int) Math.round ((pos.x - p.y * base * r3 * Math.cos(Math.PI/3)) / (base * r3));

        return p;
    }

    public static Position metaCoordinateByPosition(int dim, int base, Position pos) {
        return Units.coordinateByPosition (base*(2*dim-1), pos);
    }
}
