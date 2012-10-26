package sfmainframe.board;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import sfmainframe.Coordinate;
import sfmainframe.Player;
import sfmainframe.ship.Gun;
import sfmainframe.ship.GunCompartment;

/**
 * Everyting related to the physical layout of the game board.
 * 
 * @author Pawel Kleczek
 * @version 0.1
 * @since 06-10-2012
 * 
 */

public class Board {

    public static final int WIDTH_MAX = 63;
    public static final int HEIGHT_MAX = 69;

    private Hex[][] map;


    public static int oddToNumber(int n) {
        if (n <= 2)
            return 0;
        else
            return n / 2;
    } // (1,n]


    public static int evenToNumber(int n) {
        return n / 2;
    } // [1,n]


    public int calculateCloserPoint(Coordinate point, RotateDirection direction) throws IllegalArgumentException
    /* zwraca wspolrzedne punktu lezacego tuz za krancem mapy na prostej */
    {
        int a = point.getA();
        int b = point.getB();

        switch (direction) {
        case N:
            return a * 100 + (a + 37 - evenToNumber(a));
        case NE:
            return 63 * 100 + (b - Math.abs(a - b));
        case SE:
            return 63 * 100;
        case S:
            return a * 100 + (a - oddToNumber(a));
        case SW:
            return 1 * 100 + (1 + Math.abs(a - b));
        case NW:
            return 1 * 100;
        default:
            throw new IllegalArgumentException();
        }
    }


    public static boolean isOnMap(Coordinate point) {
        int a = point.getA();
        int b = point.getB();

        if (a < 0 || a > 63 || b < 0 || b > 69)
            return false;

        if (b < a - oddToNumber(a) || b > a + 37 - evenToNumber(a))
            // warunek wynikajacy z geometrii planszy (zabkowania dolnej i
            // gornej krawedzi)
            return false;

        return true;
    }


    public static int getDistance(Coordinate source, Coordinate target) {
        // na pewno dziala dobrze dla zasiegu r<=5
        double dA = (target.getA() - source.getA()) * Math.sqrt(3.0) / 2.0; // kwestia
                                                                            // przelozenia
                                                                            // wspolrzednych
                                                                            // planszy
                                                                            // na
                                                                            // kartezjanskie
        double dB = target.getB() - source.getB() + (target.getA() - source.getA()) * (-0.5); // kwestia
                                                                                              // przelozenia
                                                                                              // wspolrzednych
                                                                                              // planszy
                                                                                              // na
                                                                                              // kartezjanskie

        double l = Math.sqrt(dA * dA + dB * dB);

        if (l - Math.floor(l) < 0.3)
            return (int) (l);
        else
            return (int) (l) + 1;
    }


    public Hex getHex(int a, int b) {
        return map[a][b];
    }


    public Hex getHex(Coordinate coord) {
        return getHex(coord.getA(), coord.getB());
    }


    public ObstacleReport isObstacleOnPath(Coordinate position, RotateDirection direction, int distance, Player owner,
            Set<Integer> ownerAllies)
    /*
     * Funkcja zwraca strukrutę zawierającą informacje o ewentualnej
     * przeszkodzie na trasie okrętu.
     */
    {
        ObstacleReport report = new ObstacleReport();

        int a = position.getA();
        int b = position.getB();

        Hex hex = null;

        for (int i = 1; i <= distance; i++) {
            report.distanceToObstacle = i;
            report.problemOccured = false;

            switch (direction) {
            case N:
                hex = map[a][b + i];
                break;
            case NE:
                hex = map[a + i][b + i];
                break;
            case SE:
                hex = map[a + i][b];
                break;
            case S:
                hex = map[a][b - i];
                break;
            case SW:
                hex = map[a - i][b - i];
                break;
            case NW:
                hex = map[a - i][b];
                break;
            }

            report.hexTerrainType = hex.terrain;
            if (hex.terrain != Terrain.WATER)
                report.problemOccured = true;

            report.hexOwner = hex.owner;
            if (!ownerAllies.contains(report.hexOwner.ordinal()))
                report.problemOccured = true;

            report.hexShip = hex.ship;
            if (report.hexShip != null)
                report.problemOccured = true;

            if (report.problemOccured)
                return report;
        }

        return report;
    }


    public Board(int width, int height) {
        map = new Hex[width][height];
    }


    public void readData(InputStream dis) throws IOException {
        for (int i = 0; i < map.length; i++)
            for (int j = 0; j < map[0].length; j++)
                map[i][j].decode(dis.read());
    }


    public void writeData(DataOutputStream dos) throws IOException {
        dos.writeInt(map.length);
        dos.writeInt(map[0].length);
        for (int i = 0; i < map.length; i++)
            for (int j = 0; j < map[0].length; j++)
                dos.writeInt(map[i][j].encode());
    }


    public boolean checkIfObstacleOnBulletPath(Coordinate source, Coordinate target, Gun gunType) {

        double dx = (target.getA() - source.getA()) * Math.sqrt(3.0) / 2.0; // kwestia
                                                                            // przelozenia
                                                                            // wspolrzednych
                                                                            // planszy
                                                                            // na
                                                                            // kartezjanskie
        double dy = target.getB() - source.getB() + (target.getA() - source.getA()) * (-0.5); // kwestia
                                                                                              // przelozenia
                                                                                              // wspolrzednych
                                                                                              // planszy
                                                                                              // na
                                                                                              // kartezjanskie

        double A = -dy / dx;
        double B = 1.0;
        double C = 0.0;

        /*
         * Przeszukiwanie wszystkich plytek w promieniu zasiegu dziala, lezace
         * po tej samej stronie osi A co okret-cel (krote znajduja sie na mapie)
         */
        for (int dA = -gunType.getRange(); dA <= gunType.getRange(); dA++) {
            for (int dB = -gunType.getRange(); dB <= gunType.getRange(); dB++) {
                int hexA = source.getA() + dA;
                int hexB = source.getB() + dB;

                if (Board.getDistance(source, target) > gunType.getRange()
                        || Board.getDistance(source, target) > Board.getDistance(source, new Coordinate(hexA, hexB)))
                    continue;
                if (hexA == target.getA() && hexB == target.getB())
                    continue;
                if (hexA < 0 || hexA > WIDTH_MAX)
                    continue;
                if (hexB < 0 || hexB > HEIGHT_MAX)
                    continue;
                if (dA * Math.abs(target.getA() - source.getA()) != (target.getA() - source.getA()) * Math.abs(dA))
                    continue; // heksy na zlej polprostej
                if (dA == 0 && dB == 0)
                    continue;

                double dhx = (double) (dA) * Math.sqrt(3.0) / 2.0; // kwestia
                                                                   // przelozenia
                                                                   // wspolrzednych
                                                                   // planszy
                                                                   // na
                                                                   // kartezjanskie
                double dhy = (double) (dB) + (double) (dA) * (-0.5); // kwestia
                                                                     // przelozenia
                                                                     // wspolrzednych
                                                                     // planszy
                                                                     // na
                                                                     // kartezjanskie

                // mozliwosc kolizji, gdy linia celowania przebiega w obrebie
                // danego heksu
                double d = Math.abs(A * dhx + B * dhy + C) / Math.sqrt(A * A + B * B);
                if (d < 0.5) {
                    if (getHex(hexA, hexB).terrain == Terrain.ISLAND || getHex(hexA, hexB).ship != null)
                        return true;
                }
            }
        }

        return false;
    }


    public GunCompartment calculateSourceGunCompartment(Coordinate deltaCoord, int targetRotation) {
        /*
         * Na podstawie kata miedzy wektorem B+, a wektorem celu (oraz obrotu
         * okretu-celu) oblicza, z jakiego przedzialu dzialowego powinnismy
         * strzelac
         */

        // wektory a-> i b-> maja wspolrzedne kartezjanskie tak, jakbysmy sie
        // poruszali po kratkach
        double a[] = { (double) (deltaCoord.getA()) * Math.sqrt(3) / 2, (double) (-0.5 * deltaCoord.getA()) };
        double b[] = { 0.0, (double) (deltaCoord.getB()) };

        double v[] = { 0.0, 1.0 };
        double u[] = { a[0] + b[0], a[1] + b[1] };

        double du = Math.sqrt(u[0] * u[0] + u[1] * u[1]);
        double dv = Math.sqrt(v[0] * v[0] + v[1] * v[1]);

        double sin = (u[0] * v[1] - u[1] * v[0]) / (du * dv);
        double cos = (u[0] * v[0] + u[1] * v[1]) / (du * dv);

        double fi = 0.0;
        if (sin >= 0.0)
            fi = Math.acos(cos);
        else
            fi = 2 * Math.PI - Math.acos(cos);

        fi = (fi - targetRotation * Math.PI / 3 + 2 * Math.PI) % (2 * Math.PI);

        // podane nizej wartosci w sposob umowny okreslaja udzial poszczegolnych
        // czesci w kadlubie okretu
        if (fi > 11.0 / 6.0 * Math.PI || fi < 1.0 / 3.0 * Math.PI)
            return GunCompartment.BOW;
        if (fi >= 1.0 / 3.0 * Math.PI && fi <= 2.0 / 3.0 * Math.PI)
            return GunCompartment.SIDE_R;
        if (fi > 2.0 / 3.0 * Math.PI && fi < 7.0 / 6.0 * Math.PI)
            return GunCompartment.STERN;
        if (fi >= 7.0 / 6.0 * Math.PI && fi <= 11.0 / 6.0 * Math.PI)
            return GunCompartment.SIDE_L;

        return GunCompartment.NONE;
    }


    /**
     * Na podstawie kata miedzy wektorem B+, a wektorem celu (oraz obrotu
     * okretu-celu) oblicza, w jaki przedzial dzialowy trafiamy
     * 
     * @param dA
     * @param dB
     * @return
     */
    public GunCompartment calculateCompartmentToAim(int dA, int dB, int targetRotation) {
        // FIXME: okret target strzela do source (?)

        // wektory a-> i b-> maja wspolrzedne kartezjanskie tak, jakbysmy sie
        // poruszali po kratkach
        double a[] = { (double) (dA) * Math.sqrt(3) / 2, (double) (-0.5 * dA) };
        double b[] = { 0.0, (double) (dB) };

        double v[] = { 0.0, 1.0 };
        double u[] = { a[0] + b[0], a[1] + b[1] };

        double du = Math.sqrt(u[0] * u[0] + u[1] * u[1]);
        double dv = Math.sqrt(v[0] * v[0] + v[1] * v[1]);

        double sin = (u[0] * v[1] - u[1] * v[0]) / (du * dv);
        double cos = (u[0] * v[0] + u[1] * v[1]) / (du * dv);

        double fi = 0.0;
        if (sin >= 0.0)
            fi = Math.acos(cos);
        else
            fi = 2 * Math.PI - Math.acos(cos);

        fi = (fi - targetRotation * Math.PI / 6 + 2 * Math.PI) % (2 * Math.PI);

        // podane nizej wartosci w sposob umowny okreslaja udzial poszczegolnych
        // czesci w kadlubie okretu
        if (fi > 11.0 / 6.0 * Math.PI || fi < 1.0 / 3.0 * Math.PI)
            return GunCompartment.BOW;
        if (fi >= 1.0 / 3.0 * Math.PI && fi <= 2.0 / 3.0 * Math.PI)
            return GunCompartment.SIDE_R;
        if (fi > 2.0 / 3.0 * Math.PI && fi < 7.0 / 6.0 * Math.PI)
            return GunCompartment.STERN;
        if (fi >= 7.0 / 6.0 * Math.PI && fi <= 11.0 / 6.0 * Math.PI)
            return GunCompartment.SIDE_L;

        return GunCompartment.NONE;
    }
}
