package sfmainframe;

public final class Coordinate {

    private int A;
    private int B;

    public static final Coordinate dummy = new Coordinate(Commons.NIL, Commons.NIL);


    public Coordinate(int a, int b) {
        A = a;
        B = b;
    }


    public void set(int a, int b) {
        A = a;
        B = b;
    }


    public int getA() {
        return A;
    }


    public void setA(int value) {
        A = value;
    }


    public int getB() {
        return B;
    }


    public void setB(int value) {
        B = value;
    }


    public Coordinate sum(Coordinate c) {
        return new Coordinate(A + c.A, B + c.B);
    }


    public Coordinate diff(Coordinate c) {
        return new Coordinate(A - c.A, B - c.B);
    }


    public boolean isValid() {
        return (A < 0 || B < 0);
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Coordinate))
            return false;
        Coordinate coord = (Coordinate) obj;
        return (coord.A == A && coord.B == B);
    }

}
