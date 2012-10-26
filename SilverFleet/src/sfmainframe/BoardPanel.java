package sfmainframe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import sfmainframe.board.Board;
import sfmainframe.board.RotateDirection;
import sfmainframe.gui.DisplayMode;
import sfmainframe.gui.Tabs;
import sfmainframe.ship.Ship;
import sfmainframe.ship.marines.CommanderState;

public class BoardPanel extends JPanel {

    /**
     * Height of the horizontal bar; in pixels.
     */
    private static final int A_BAR_HEIGHT = 26;

    /**
     * Width of the vertical bar; in pixels.
     */
    private static final int B_BAR_WIDTH = 54;
    // XXX: wzór? wytłumaczenie?
    /**
     * A coefficient used to calculate vertical shift of a B-bar related to the
     * the mouse shift.
     */
    private static final double BBAR_SHIFT_X = -0.575;

    /**
     * Height of a hex. Used to determine centers of hexes on the screen.
     */
    private static final double HEX_HEIGHT = 34.0;

    /**
     * Hex radius in pixels.
     */
    private static final int HEX_RADIUS = 15;

    /**
     * Width of a hex. Used to determine centers of hexes on the screen.
     */
    private static final double HEX_WIDTH = 29.5;

    /**
     * Position of the left upper corner of the map (relative to the left upper
     * corner of the board panel), in pixels.
     */
    private static final Point MAP_CORNER = new Point(80, 50);

    /**
     * Offset of the left-most upper-most hex (relative to the corner of the
     * map); in pixels.
     */
    private static final Point HEX_A1_B38_OFFSET = new Point(MAP_CORNER.x + 21, MAP_CORNER.y + 20);

    /**
     * Dimension of the visible part of the map; in pixels.
     */
    private static final Dimension MAP_DIMENSION = new Dimension(604, 412);

    /**
     * Size of the separator (either horizontal or vertical); in pixels.
     */
    private static final int SEPARATOR = 10;

    /**
     * Default UID.
     */
    private static final long serialVersionUID = 1L;

    private final BufferedImage aBarImage;

    private final BufferedImage bBarImage;

    private Point boardDragOffset = new Point(0, 0);

    /**
     * ID of the mouse button which was pressed.
     */
    private int buttonPressed = -1;

    /**
     * ID of a ship which is currently in the clipboard.
     */
    private Integer clipBoardShipID = null;

    // TODO: co dokładnie oznaczają te współrzędne? jakie współrzędne ma [0,0]?
    /**
     * Coordinates of hexes' centers.
     */
    private final Point[][] hexCoords = new Point[Board.WIDTH_MAX + 1][Board.HEIGHT_MAX + 1];

    private final BufferedImage mapImage;

    /**
     * Position of the mouse when any of buttons was pressed.
     */
    private Point pressedMousePosition = new Point(0, 0);


    // TODO
    // private static final Map<Player, Color> playerColors = new
    // HashMap<Player, Color>().put(P, arg1);

    // case PASADENA: shipColor = Color.green;
    // case ELMETH: shipColor = Color.magenta;
    // case SIDONIA: shipColor = Color.orange;
    // case PLEENSY: shipColor = Color.pink;
    // case HAMPSHIRE: shipColor = Color.white;
    // case DISCASTER: shipColor = Color.yellow;
    // case DELACROIX: shipColor = Color.cyan;
    // case LEPPO: shipColor = Color.lightGray;
    // case NONE: shipColor = Color.darkGray;

    public BoardPanel() throws IOException {
        boardDragOffset = new Point(0, 450);

        for (int i = 1; i <= Board.WIDTH_MAX; i++)
            for (int j = 1; j <= Board.HEIGHT_MAX; j++) {
                hexCoords[i][j].x = (int) ((i - 1) * HEX_WIDTH);
                hexCoords[i][j].y = (int) ((38 - j) * HEX_HEIGHT + (i - 1) * HEX_HEIGHT / 2);
            }

        mapImage = ImageIO.read(MainBoard.class.getResource("images/hex.gif"));
        aBarImage = ImageIO.read(MainBoard.class.getResource("images/abar.gif"));
        bBarImage = ImageIO.read(MainBoard.class.getResource("images/bbar.gif"));

        addMouseListener(new MyMouseListener());
        addMouseMotionListener(new MyMouseMotionListener());

        setFocusable(true);
        requestFocus();
    }


    /**
     * Creates an arrow-shaped marker centered in the given hex's coordinates.
     * 
     * @param c
     *            Hex's position.
     * @return Arrow-shaped marker centered in the given hex's coordinates.
     */
    private GeneralPath getArrowShape(Coordinate c) {
        final Point[] crds = { new Point(0, -13), new Point(5, -3), new Point(-5, -3) };
        final int a = c.getA();
        final int b = c.getB();

        GeneralPath ship = new GeneralPath(GeneralPath.WIND_EVEN_ODD, crds.length);

        ship.moveTo(crds[0].x + hexCoords[a][b].x - boardDragOffset.x + HEX_A1_B38_OFFSET.x, crds[0].y
                + hexCoords[a][b].y - boardDragOffset.y + HEX_A1_B38_OFFSET.y);
        for (int i = 1; i < crds.length; i++) {
            ship.lineTo(crds[i].x + hexCoords[a][b].x - boardDragOffset.x + HEX_A1_B38_OFFSET.x, crds[i].y
                    + hexCoords[a][b].y - boardDragOffset.y + HEX_A1_B38_OFFSET.y);
        }
        ship.closePath();

        return ship;
    }


    public int getClipBoardShip() {
        return clipBoardShipID;
    }


    public Dimension getPreferredSize() {
        return new Dimension(780, 515);
    }


    public void paintComponent(Graphics g) {
        // super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /* hexBoard */
        g.drawImage(mapImage, MAP_CORNER.x, MAP_CORNER.y, MAP_CORNER.x + MAP_DIMENSION.width, MAP_CORNER.y
                + MAP_DIMENSION.height, boardDragOffset.x, boardDragOffset.y, boardDragOffset.x + MAP_DIMENSION.width,
                boardDragOffset.y + MAP_DIMENSION.height, null);

        /* aBar (upper) */
        g.drawImage(aBarImage, MAP_CORNER.x, MAP_CORNER.y - A_BAR_HEIGHT - SEPARATOR, MAP_CORNER.x
                + MAP_DIMENSION.width, MAP_CORNER.y - SEPARATOR, boardDragOffset.x, 0, boardDragOffset.x
                + MAP_DIMENSION.width, A_BAR_HEIGHT, null);

        /* aBar (lower) */
        g.drawImage(aBarImage, MAP_CORNER.x, MAP_CORNER.y + MAP_DIMENSION.height + SEPARATOR, MAP_CORNER.x
                + MAP_DIMENSION.width, MAP_CORNER.y + MAP_DIMENSION.height + A_BAR_HEIGHT + SEPARATOR,
                boardDragOffset.x, 0, boardDragOffset.x + MAP_DIMENSION.width, A_BAR_HEIGHT, null);

        /* bBar (left) */
        g.drawImage(bBarImage, MAP_CORNER.x - B_BAR_WIDTH - SEPARATOR, MAP_CORNER.y, MAP_CORNER.x - SEPARATOR,
                MAP_CORNER.y + MAP_DIMENSION.height, 0, (int) (1116 + boardDragOffset.y + BBAR_SHIFT_X
                        * boardDragOffset.x), B_BAR_WIDTH, (int) (1116 + boardDragOffset.y + BBAR_SHIFT_X
                        * boardDragOffset.x + MAP_DIMENSION.height), null);

        /* bBar (right) */
        g.drawImage(bBarImage, MAP_CORNER.x + MAP_DIMENSION.width + SEPARATOR, MAP_CORNER.y, MAP_CORNER.x
                + MAP_DIMENSION.width + B_BAR_WIDTH + SEPARATOR, MAP_CORNER.y + MAP_DIMENSION.height, 0,
                (int) (719 + boardDragOffset.y + BBAR_SHIFT_X * boardDragOffset.x), B_BAR_WIDTH, (int) (719
                        + boardDragOffset.y + BBAR_SHIFT_X * boardDragOffset.x + MAP_DIMENSION.height), null);

        /* wyswietlanie okretow */
        g.setClip(MAP_CORNER.x, MAP_CORNER.y, MAP_DIMENSION.width, MAP_DIMENSION.height);

        Boolean commanderOnBoard = Boolean.FALSE;
        final Font[] commanderFonts = { new Font("Arial", Font.PLAIN, 12), new Font("Verdana", Font.BOLD, 12) };

        for (int sID = 0; sID < Commons.SHIPS_MAX; sID++) {
            commanderOnBoard = Boolean.FALSE;
            Ship ship = MainBoard.game.getShip(sID);

            Player shipOwner = MainBoard.game.getShip(sID).getOwner();
            if (shipOwner != Player.NONE
                    && (MainBoard.game.getShip(sID).getCommanderOnBoardState(shipOwner) == CommanderState.READY || MainBoard.game
                            .getShip(sID).getCommanderOnBoardState(shipOwner) == CommanderState.USED))
                commanderOnBoard = Boolean.TRUE;

            if (!ship.isOnGameBoard())
                continue;

            Color shipColor = Color.red;

            // XXX : Map<Player, Color>
            switch (MainBoard.game.getShip(sID).getOwner()) {
            case PASADENA:
                shipColor = Color.green;
                break;
            case ELMETH:
                shipColor = Color.magenta;
                break;
            case SIDONIA:
                shipColor = Color.orange;
                break;
            case PLEENSY:
                shipColor = Color.pink;
                break;
            case HAMPSHIRE:
                shipColor = Color.white;
                break;
            case DISCASTER:
                shipColor = Color.yellow;
                break;
            case DELACROIX:
                shipColor = Color.cyan;
                break;
            case LEPPO:
                shipColor = Color.lightGray;
                break;
            case NONE:
                shipColor = Color.darkGray;
                break;
            }

            if (MainBoard.getBoardPanelMode() == DisplayMode.DEPLOY_MODE && sID == clipBoardShipID)
                shipColor = Color.red;

            Font selectedCommanderFont = commanderOnBoard ? commanderFonts[0] : commanderFonts[1];

            Point stringOffset = new Point();
            switch (ship.getRotation()) {
            case N:
                stringOffset = new Point(-6, 10);
                break;
            case NE:
                stringOffset = new Point(-11, 7);
                break;
            case SE:
                stringOffset = new Point(-10, 0);
                break;
            case S:
                stringOffset = new Point(-7, 0);
                break;
            case SW:
                stringOffset = new Point(-2, 0);
                break;
            case NW:
                stringOffset = new Point(-1, 8);
                break;
            }

            drawShip(g2, sID, ship.getPosition(), ship.getRotation(), shipColor, selectedCommanderFont, stringOffset);
        }
    }


    /**
     * Draw a rotated ship marker.
     * 
     * @param g2
     * @param shipID
     * @param c
     * @param rot
     * @param shipColor
     * @param selectedCommanderFont
     * @param stringOffset
     */
    private void drawShip(Graphics2D g2, int shipID, Coordinate c, RotateDirection rot, Color shipColor,
            Font selectedCommanderFont, Point stringOffset) {
        final int a = c.getA();
        final int b = c.getB();

        final double angle = rot.ordinal() * Math.PI / 3;
        final Point transformCenter = new Point(HEX_A1_B38_OFFSET.x - boardDragOffset.x + hexCoords[a][b].x,
                HEX_A1_B38_OFFSET.y - boardDragOffset.y + hexCoords[a][b].y);

        g2.transform(AffineTransform.getRotateInstance(angle, transformCenter.x, transformCenter.y));
        g2.setPaint(shipColor);
        g2.fill(getArrowShape(c));
        g2.transform(AffineTransform.getRotateInstance(-angle, transformCenter.x, transformCenter.y));

        g2.setPaint(Color.black);
        g2.setFont(selectedCommanderFont);
        g2.drawString(String.valueOf(shipID), transformCenter.x + stringOffset.x, transformCenter.y + stringOffset.y);
    }


    public void resetClipBoardShipID() {
        clipBoardShipID = null;
    }

    private class MyMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            buttonPressed = e.getButton();

            int dx = e.getX() + boardDragOffset.x - HEX_A1_B38_OFFSET.x;
            int dy = e.getY() + boardDragOffset.y - HEX_A1_B38_OFFSET.y;

            if (MainBoard.getBoardPanelMode() == DisplayMode.DEPLOY_MODE) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    clipBoardShipID = null;
                    repaint();
                    MainBoard.addMessage("Ship deselected.\n");
                    MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
                    MainBoard.makeHeaderLabel();
                }
            }

            if (e.getButton() == MouseEvent.BUTTON2) {
                pressedMousePosition = e.getPoint();
            }

            if (e.getButton() == MouseEvent.BUTTON1) {
                int i = 0;
                int j = 0;

                hexSearch: for (i = 1; i <= Board.WIDTH_MAX; i++) {
                    for (j = 1; j <= Board.HEIGHT_MAX; j++) {
                        if ((dx - hexCoords[i][j].x) * (dx - hexCoords[i][j].x) + (dy - hexCoords[i][j].y)
                                * (dy - hexCoords[i][j].y) <= HEX_RADIUS * HEX_RADIUS) {
                            break hexSearch;
                        }
                    }
                }

                if (i <= Board.WIDTH_MAX) {
                    if (MainBoard.getBoardPanelMode() == DisplayMode.DEPLOY_MODE) {
                        if (clipBoardShipID == null) {
                            Integer shipID = MainBoard.game.getBoard().getHex(i - 1, j - 1).ship;
                            if (shipID != null && MainBoard.isSelectable(shipID)) {
                                clipBoardShipID = shipID;
                                MainBoard.setSelectedShip(shipID, Tabs.MOVEMENT);
                                repaint();
                                MainBoard.addMessage("Selected ship: " + shipID + "\n");
                                MainBoard.makeHeaderLabel();
                            }
                        } else {
                            if (MainBoard.isDeployable(clipBoardShipID, new Coordinate(i - 1, j - 1))) {
                                MainBoard.moveShipToPosition(clipBoardShipID, new Coordinate(i - 1, j - 1));
                                MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
                                MainBoard.addMessage("Ship deployed.\n");
                                clipBoardShipID = null;
                                MainBoard.makeHeaderLabel();
                            }

                            repaint();
                        }
                    } else if (MainBoard.getBoardPanelMode() == DisplayMode.BOARDING_MODE) {
                        // mozliwosc operowania tylko w obrebie aktualnie
                        // przetwarzanego statku
                    } else {
                        Integer shipID = MainBoard.game.getBoard().getHex(i - 1, j - 1).ship;
                        if (shipID == null) {
                            MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
                            MainBoard.addMessage("Selected ship: none\n");
                            MainBoard.makeHeaderLabel();
                        } else {
                            MainBoard.setSelectedShip(shipID, Tabs.MOVEMENT);
                            MainBoard.addMessage("Selected ship: " + shipID + "\n");
                            MainBoard.makeHeaderLabel();
                        }
                    }
                }
            }
        }


        public void mouseReleased(MouseEvent e) {
            buttonPressed = -1;
        }
    }

    private class MyMouseMotionListener extends MouseAdapter {

        public void mouseDragged(MouseEvent e) {
            if (buttonPressed == MouseEvent.BUTTON2) {
                if (boardDragOffset.x + e.getX() - pressedMousePosition.x > 0
                        && boardDragOffset.x + e.getX() - pressedMousePosition.x < mapImage.getWidth()
                                - MAP_DIMENSION.width) {
                    boardDragOffset.x += e.getX() - pressedMousePosition.x;
                    pressedMousePosition.x = e.getX();
                }
                if (boardDragOffset.y + e.getY() - pressedMousePosition.y > 0
                        && boardDragOffset.y + e.getY() - pressedMousePosition.y < mapImage.getHeight()
                                - MAP_DIMENSION.height) {
                    boardDragOffset.y += e.getY() - pressedMousePosition.y;
                    pressedMousePosition.y = e.getY();
                }

                repaint();
            }
        }
    }
}