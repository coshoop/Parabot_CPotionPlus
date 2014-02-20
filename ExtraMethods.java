package Herblore;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.Random;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.input.Mouse;
import org.rev317.api.methods.Camera;
import org.rev317.api.methods.Menu;
import org.rev317.api.methods.Players;
import org.rev317.api.methods.Skill;
import org.rev317.api.wrappers.interactive.Npc;
import org.rev317.api.wrappers.scene.SceneObject;
import org.rev317.api.wrappers.scene.Tile;

/**
 * Library of some helper methods I made.
 * @author Collin 11
 */
public class ExtraMethods {

    private static Random random = new Random();

    /**
     * Rotates the cameras angle (not pitch) toward the given tile.
     * @param c Tile to turn the camera toward
     */
    public static void rotateCameraToward(Tile c) {
        int ix = c.getX();
        int yx = c.getY();
        int dx = Players.getLocal().getLocation().getX() - ix;
        int dy = Players.getLocal().getLocation().getY() - yx;
        double angle = Math.atan2(dy, dx); // radians

        // Just incase. Not sure if atan2 will given negative results
        if (angle < 0) {
            angle += Math.PI * 2;
        }

        // Convert to degrees and rotate the angle system
        int degrees = ((int) (angle * 180 / Math.PI) + 90) % 360;
        Camera.setRotation(degrees);
    }

    /**
     * Closes the shop if one is open.
     */
    public static boolean closeShop() {
        return Menu.interact("Close", new Point(451, 38));
    }

    /**
     * Some Objects don't always work when interacted with at their center
     * point. So this interaction method will jiggle the mouse (as needed) until
     * the mouse can properly interact with that object.
     *
     * @param interact The SceneObject to interact with.
     * @param command The command that you would pass to the Menu.interact
     * method
     * @param give The number of pixels (in X and Y) that the mouse may move in
     * either direction.
     */
    public static boolean jiggleMenuInteract(SceneObject interact, String command, int give) {
        Point inspect = interact.getModel().getCentralPoint();
        Mouse.getInstance().moveMouse(inspect.x, inspect.y);
        Point randPoint = inspect;
        for (int count = 0; Menu.getActionIndex(command) < 0; count++) {
            if (count > 10) {
                return false;
            }
            Time.sleep(100); // This is needed
            randPoint = new Point(inspect.x - give + random.nextInt(give * 2 + 1),
                    inspect.y - give + random.nextInt(give * 2 + 1));
            Mouse.getInstance().moveMouse(randPoint.x, randPoint.y);
        }
        Menu.interact(command, randPoint);
        return true;
    }

    /**
     * Uses tiles on the visible map (not minimap) to walk TOWARD a 
     * specified location. Don't use it for long walks. Not tested
     * that much.
     */
    public static void walkToward(Tile goal) {
        int goalX = goal.getX();
        int goalY = goal.getY();
        int currentX = Players.getLocal().getLocation().getX();
        int currentY = Players.getLocal().getLocation().getY();
        int difX = goalX - currentX;
        int difY = goalY - currentY;
        int walkToX;
        int walkToY;
        // Calculate where I need to walk to
        if (Math.abs(difX) < 3) {
            walkToX = goalX;
        } else {
            walkToX = currentX + 3 * (difX > 0 ? 1 : -1);
        }
        if (Math.abs(difY) < 3) {
            walkToY = goalY;
        } else {
            walkToY = currentY + 3 * (difY > 0 ? 1 : -1);
        }

        // Create the walk-to tile and walk there
        Tile walkToTile = new Tile(walkToX, walkToY);
        Menu.interact("Walk here", walkToTile.toScreen());
    }

    /**
     * Some Objects don't always work when interacted with at their center
     * point. So this interaction method will jiggle the mouse (as needed) until
     * the mouse can properly interact with that object.
     *
     * @param interact The SceneObject to interact with.
     * @param command The command that you would pass to the Menu.interact
     * method
     * @param give The number of pixels (in X and Y) that the mouse may move in
     * either direction.
     */
    public static boolean jiggleMenuInteract(Npc interact, String command, int give) {
        Point inspect = interact.getModel().getCentralPoint();
        Mouse.getInstance().moveMouse(inspect.x, inspect.y);
        Point randPoint = inspect;
        for (int count = 0; Menu.getActionIndex(command) < 0; count++) {
            if (count > 10) {
                return false;
            }
            Time.sleep(100); // This is needed
            randPoint = new Point(inspect.x - give + random.nextInt(give * 2 + 1),
                    inspect.y - give + random.nextInt(give * 2 + 1));
            Mouse.getInstance().moveMouse(randPoint.x, randPoint.y);
        }
        Menu.interact(command, randPoint);
        return true;
    }

    /**
     * Rotates the cameras angle (not pitch) toward the given SceneObject
     * @param c Scene object to point the camera toward
     */
    public static void rotateCameraToward(SceneObject c) {
        int ix = c.getLocation().getX();
        int yx = c.getLocation().getY();
        int dx = Players.getLocal().getLocation().getX() - ix;
        int dy = Players.getLocal().getLocation().getY() - yx;
        double angle = Math.atan2(dy, dx); // radians

        // Just incase. Not sure if atan2 will given negative results
        if (angle < 0) {
            angle += Math.PI * 2;
        }

        // Convert to degrees and rotate the angle system
        int degrees = ((int) (angle * 180 / Math.PI) + 90) % 360;
        Camera.setRotation(degrees);
    }

    /**
     * Calculates the distance between tiles a and b using the Pythagorean
     * theorem.
     */
    public static double distance(Tile a, Tile b) {
        double answer = Math.sqrt(Math.pow(a.getX() - b.getX(), 2)
                + Math.pow(a.getY() - b.getY(), 2));
        return answer;
    }

    /**
     * Rotates the camera the given number of degrees.
     * @param degrees
     */
    public static void rotateCamera(int degrees) {
        int deg = Camera.getAngle() + degrees;
        if (deg > 360) {
            deg %= 360;
        }
        while (deg < 0) {
            deg += 360;
        }
        Camera.setRotation(deg);
    }

    /**
     * Determines whether or not the given point is in the players play screen.
     * That is, the part of the screen which contains the map (not minimap)
     * Areas such as the chat box and inventory are considered out of bounds.
     */
    public static boolean inPlayScreenBounds(Point p) {
        return p.x >= 10 && p.x <= 510 && p.y >= 10 && p.y <= 330;
    }

    /**
     * Formats the given long to String "hh:mm:ss" format
     */
    public static String runTime(long i) {
        DecimalFormat nf = new DecimalFormat("00");
        long millis = System.currentTimeMillis() - i;
        long hours = millis / (1000 * 60 * 60);
        millis -= hours * (1000 * 60 * 60);
        long minutes = millis / (1000 * 60);
        millis -= minutes * (1000 * 60);
        long seconds = millis / 1000;
        return nf.format(hours) + ":" + nf.format(minutes) + ":"
                + nf.format(seconds);
    }

    /**
     * Paints a dynamic GUI. Create an instance of this class in your
     * onExecute(). In your paint method, you may call any of the setter methods
     * or paint methods. Before calling a paint method, it is required that you
     * call the method "void startPaint(Graphics g)" and pass your Paint methods
     * Graphics object.
     *
     * @author Collin 11
     * @version 1.0
     * @since 1/31/2014
     */
    public static final class Paint_System {

        // Instance variables
        private int recX, recY, wid, height, mode;
        private final int saveY;

        // Paint variables
        private Color backgroundColor = new Color(83, 77, 52, 150);
        private Color forgroundColor = Color.green;
        private int x, y;
        private Graphics g;
        private final long startTime;
        private String title;

        public static final int ANCHOR_UP = 0;
        public static final int ANCHOR_DOWN = 1;
        private static final int BUF = 5, SPACE = 15;
        private static final Font FONT = new Font("Verdana", Font.PLAIN, 12);
        private static final DecimalFormat C_FORMAT = new DecimalFormat("#,###");
        private static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 12);

        /**
         * Initializes a new Paint_System with the given variables
         *
         * @param x X-position
         * @param y Y-position
         * @param widPX the width in pixels
         * @param itemHeight the number of items you wish to add (you may add an
         * item by using any method that begins with 'paint')
         * @param anchorType ANCHOR_UP anchors the lower-left corner of this GUI
         * to the point given. ANCHOR_DOWN anchors the upper-left corner of this
         * GUI to the point given.
         */
        public Paint_System(int x, int y, int widPX, int itemHeight, int anchorType, String title) {
            startTime = System.currentTimeMillis();
            saveY = y;
            this.recX = x + BUF;
            changeItemHeight(itemHeight);
            this.wid = widPX;
            this.mode = anchorType;
            this.title = title;
        }

        /**
         * Change the number of items you plan on adding
         */
        public void changeItemHeight(int height) {
            this.height = (height + 1) * SPACE + 2 * BUF;
            this.recY = saveY - (this.height) + BUF;
        }

        /**
         * Resets variables. Call this method before calling any other paint
         * methods of this class.
         */
        public void startPaint(Graphics graphics) {
            g = graphics;
            x = recX;
            y = recY + SPACE - 4;
            paintBackground();
            paintTitle();
        }

        /**
         * Set the GUI background color to the given color
         */
        public void setBackgroundColor(Color color) {
            backgroundColor = color;
        }

        /**
         * Set the GUI text color to the given color
         */
        public void setForgroundColor(Color color) {
            forgroundColor = color;
        }

        /**
         * Paint the background
         */
        private void paintBackground() {
            g.setColor(backgroundColor);
            g.fillRoundRect(recX - BUF, recY - BUF, wid, height, BUF, BUF);
            g.setColor(Color.BLACK);
            g.drawRoundRect(recX - BUF, recY - BUF, wid, height, BUF, BUF);
        }

        /**
         * Paint the given String
         */
        public void paintStat(String label) {
            g.setColor(forgroundColor);
            g.drawString(label, x, y + 1);
            movePaintLocation();
        }

        /**
         * Paints the title using a special font
         */
        public void paintTitle() {
            g.setFont(TITLE_FONT);
            paintStat(title);
            g.setFont(FONT);
            g.setColor(forgroundColor);
        }

        /**
         * Paints the given stat and its label
         */
        public void paintStat(String label, long stat) {
            paintStat(label + format(stat));
        }

        /**
         * Paints the given stat as a per hour variable and its label
         */
        public void paintStatAndPerHour(String label, long stat) {
            double factor = 3600000.0 / ((System.currentTimeMillis() - startTime));
            paintStat(label + format(stat) + "(" + format((int) (stat * factor)) + ")");
        }

        /**
         * Paints the given stat as a per hour variable and its label
         */
        public void paintStatPerHour(String label, long stat) {
            double factor = 3600000.0 / ((System.currentTimeMillis() - startTime));
            paintStat(label, (int) (stat * factor));
        }

        /**
         * Paints the current run time in HH:MM:SS format
         */
        public void paintTime(String title) {
            paintStat(title + formatRunTime(startTime));
        }

        /**
         * Paints a visual level progress for the given Skill Displays the name
         * given, the % done for the skills current level, and the experience
         * until leveling.
         *
         * @param name The name you wish to display as the title of this
         * progress bar.
         * @param skill The skill that this progress bar should be for.
         */
        public void paintLevelProgressBar(String name, Skill skill) {
            int tempY = y - (SPACE - 4);
            int barWid = (wid - 2 * BUF);
            int xpForNextLevel = skill.getExperienceByLevel(skill.getLevel() + 1);
            int xpForCurrentLevel = skill.getExperienceByLevel(skill.getLevel());
            double percentDone;
            if (skill.getLevel() != 99) {
                percentDone = (skill.getExperience() - xpForCurrentLevel)
                        / (double) (xpForNextLevel - xpForCurrentLevel);
            } else {
                percentDone = 1;
            }
            int xpToLevel = skill.getRemaining();
            int greenpx = (int) (barWid * percentDone);
            g.setColor(new Color(0, 255, 0, 100));
            g.fillRect(x, tempY, greenpx, SPACE - 1);
            g.setColor(new Color(255, 0, 0, 50));
            g.fillRect(x + greenpx, tempY, barWid - greenpx, SPACE - 1);
            g.setColor(Color.black);
            g.drawRect(x, tempY, barWid, SPACE - 1);
            g.drawString(name + " " + (int) (percentDone * 100) + "% ("
                    + format(xpToLevel) + " xp)", x + BUF, tempY + SPACE - 3);
            movePaintLocation();
        }

        /**
         * Updates the location painting location depending on the MODE
         */
        private void movePaintLocation() {
            y += SPACE;
        }

        /**
         * Formats the given long to String "hh:mm:ss" format
         */
        private long tempMillis, tempHours, tempMinutes, tempSeconds;

        public String formatRunTime(long since) {
            DecimalFormat nf = new DecimalFormat("00");
            tempMillis = System.currentTimeMillis() - since;
            tempHours = tempMillis / (1000 * 60 * 60);
            tempMillis -= tempHours * (1000 * 60 * 60);
            tempMinutes = tempMillis / (1000 * 60);
            tempMillis -= tempMinutes * (1000 * 60);
            tempSeconds = tempMillis / 1000;
            return nf.format(tempHours) + ":" + nf.format(tempMinutes) + ":"
                    + nf.format(tempSeconds);
        }

        /**
         * Formats the given number to common runescape format.
         *
         * @param num Number to format
         * @return String format of the given number
         */
        public String format(long num) {
            String postfix = "";
            if (num >= 100_000_000) {
                num /= 1_000_000;
                postfix = "m";
            } else if (num >= 100_000) {
                num /= 1_000;
                postfix = "k";
            }
            return C_FORMAT.format(num) + postfix;
        }

    }

}
