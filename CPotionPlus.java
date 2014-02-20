package Herblore;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import org.parabot.core.ui.components.LogArea;
import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.input.Keyboard;
import org.parabot.environment.input.Mouse;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.api.events.MessageEvent;
import org.rev317.api.events.listeners.MessageListener;
import org.rev317.api.methods.Bank;
import org.rev317.api.methods.Camera;
import org.rev317.api.methods.Game;
import org.rev317.api.methods.Inventory;
import org.rev317.api.methods.Menu;
import org.rev317.api.methods.Npcs;
import org.rev317.api.methods.Players;
import org.rev317.api.methods.SceneObjects;
import org.rev317.api.methods.Skill;
import org.rev317.api.wrappers.hud.Item;
import org.rev317.api.wrappers.interactive.Npc;
import org.rev317.api.wrappers.scene.SceneObject;
import org.rev317.api.wrappers.scene.Tile;
import Herblore.ExtraMethods.Paint_System;

/**
 *
 * @author Collin 11
 */
@ScriptManifest(author = "Collin", category = Category.HERBLORE, description = "Makes and alchs pots", name = "CPotionPlus", servers = {"PKHonor"}, version = 1.0)
public class CPotionPlus extends Script implements Paintable, MessageListener {

    // Core variables
    public static ArrayList<Strategy> strategies = new ArrayList<>();
    
    static Paint_System paint;

    // Paint variables
    static int initialHerbXP;
    static int initialMagicXP;
    static int initialGold;
    static int alched = 0;
    static int pkpGained = 0;
    static String status = null;

    // Flags
    static boolean FLAG_buyPotions = false;
    static boolean FLAG_goToBank = false;
    static boolean FLAG_inRandom = false;
    static boolean FLAG_bank = false;
    static boolean withdraw_check = false;

    // Bob event variables
    static final int PORTAL_NONE = -1, PORTAL_NORTH = 0, PORTAL_EAST = 1,
            PORTAL_SOUTH = 2, PORTAL_WEST = 3, PORTAL_CENTER = 4;
    int portal = PORTAL_NONE;

    // IDs
    static int runeId = 561;
    static int[] notedPotionIds = {2435, 3041};
    static int[] notedSecondIds = {16117, 16081};
    static final int ITEM_PRAYER_POT = 2434, ITEM_BONEMEAL = 6810,
            ITEM_DUST = 9594, ITEM_MAGIC_POT = 3040, ITEM_SUPER_PRAYER = 14596,
            ITEM_EXTREME_MAGIC = 14600;

    @Override
    public boolean onExecute() {
        paint = new Paint_System(547, 465, 190, 7, Paint_System.ANCHOR_DOWN, "CPotionPlus");
        paint.setForgroundColor(new Color(253, 255, 0));
        strategies.add(new MakePotsStrat());
        strategies.add(new BankStrat());
        strategies.add(new AlchPotsStrat());
        strategies.add(new BuySuppliesStrat());
        strategies.add(new GoToBankStrat());
        strategies.add(new HandleRandomsStrat());
        initialHerbXP = Skill.HERBLORE.getExperience();
        initialMagicXP = Skill.MAGIC.getExperience();
        initialGold = Inventory.getCount(true, 995);
        LogArea.log("CPotionPlus has started.");
        provide(strategies);
        return true;
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void messageReceived(MessageEvent m) {
        if (m.getType() != MessageEvent.TYPE_PLAYER) {
            String message = m.getMessage();
            if (message.startsWith("You received 1")) {
                pkpGained++;
            } else if (message.contains("portal")) {
                if (message.contains("NORTH")) {
                    portal = PORTAL_NORTH;
                } else if (message.contains("EAST")) {
                    portal = PORTAL_EAST;
                } else if (message.contains("SOUTH")) {
                    portal = PORTAL_SOUTH;
                } else if (message.contains("WEST")) {
                    portal = PORTAL_WEST;
                } else {
                    portal = PORTAL_CENTER;
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        // Paint woodcutting stats
        int herb = Skill.HERBLORE.getExperience() - initialHerbXP;
        int magic = Skill.MAGIC.getExperience() - initialMagicXP;
        int goldGained = Inventory.getCount(true, 995) - initialGold;
        paint.startPaint(g);
        paint.paintTime("Time ran: ");
        paint.paintStat("Status: " + status);
        paint.paintStatAndPerHour("Pkp:", pkpGained);
        paint.paintStatAndPerHour("Num: ", alched);
        paint.paintStatAndPerHour("Herb: ", herb);
        paint.paintStatAndPerHour("Magic: ", magic);
        paint.paintStatAndPerHour("Gold: ", goldGained);
    }

    public static int numPotsCanMake() {
        return Math.min(Inventory.getCount(ITEM_MAGIC_POT, ITEM_PRAYER_POT), Inventory.getCount(ITEM_BONEMEAL, ITEM_DUST));
    }

    private static class BankStrat implements Strategy {

        @Override
        public boolean activate() {
            int potionAmount = Inventory.getCount(ITEM_EXTREME_MAGIC, ITEM_SUPER_PRAYER);
            boolean act = Game.isLoggedIn() && !FLAG_buyPotions && !FLAG_goToBank
                    && !FLAG_inRandom && ((potionAmount == 0 && numPotsCanMake() == 0) || FLAG_bank);
            return act;
        }

        static String WOB1 = "Withdraw All-but-one";

        @Override
        public void execute() {
            status = "Banking";
            if (Bank.isOpen()) {
                Bank.depositAllExcept(995, 561);
                FLAG_bank = false;
                int prayerCount = Math.min(Bank.getCount(ITEM_PRAYER_POT), Bank.getCount(ITEM_BONEMEAL));
                int magicCount = Math.min(Bank.getCount(ITEM_MAGIC_POT), Bank.getCount(ITEM_DUST));
                if (prayerCount > 13) {
                    bankWithdraw13(ITEM_PRAYER_POT);
                    Time.sleep(200);
                    bankWithdraw13(ITEM_BONEMEAL);
                } else if (magicCount > 13) {
                    bankWithdraw13(ITEM_MAGIC_POT);
                    Time.sleep(200);
                    bankWithdraw13(ITEM_DUST);
                } else {
                    FLAG_buyPotions = true;
                }
                Bank.close();
            } else {
                Camera.setPitch(true);
                Bank.open();
                for (int i = 0; i < 100; i++) {
                    if (Bank.isOpen()) {
                        break;
                    } else {
                        Time.sleep(20);
                    }
                }
            }
        }

        private void bankWithdraw13(int ID) {
            if (!withdraw_check) {
                Bank.withdraw(ID, 13);
                Time.sleep(1000);
                withdraw_check = true;
            } else {
                Bank.getItem(ID).interact("Withdraw 13");
            }
            Time.sleep(200);
        }
    }

    private static class GoToBankStrat implements Strategy {

        @Override
        public boolean activate() {
            return Game.isLoggedIn() && FLAG_goToBank && !FLAG_inRandom;
        }

        @Override
        public void execute() {
            status = "Going to bank";
            Menu.interact("Magic", new Point(743, 188));
            Time.sleep(100);
            Menu.interact("Cast", new Point(570, 287));
            Time.sleep(4000);
            SceneObject[] banks = SceneObjects.getNearest(2213);
            try {
                SceneObject bank = banks[0];
                Camera.turnTo(bank);
                bank.interact("Use-quickly");
                FLAG_goToBank = false;
            } catch (Exception e) {
                LogArea.error("Banking error 1");
            }
        }

    }

    private static class AlchPotsStrat implements Strategy {

        int[] potionIDs = {14596, 14600};
        long lastAlchTime = System.currentTimeMillis();
        static int ALCH_WAIT_TIME = -50;

        @Override
        public boolean activate() {
            int canAlchCount = Inventory.getCount(potionIDs);
            boolean act = Game.isLoggedIn() && !FLAG_buyPotions && !FLAG_goToBank
                    && !FLAG_inRandom && canAlchAtm() && canAlchCount > 0 && !Bank.isOpen();
            return act;
        }

        @Override
        public void execute() {
            status = "Alching";
            Mouse.getInstance().click(400, 350, true);
            Menu.interact("Magic", new Point(743, 188));
            Menu.interact("Cast", new Point(668, 336));
            Item[] itemsToAlch = Inventory.getItems(potionIDs);
            try {
                Item itemToAlch = itemsToAlch[0];
                itemToAlch.interact("Cast");
                lastAlchTime = System.currentTimeMillis();
                alched++;
            } catch (Exception e) {
                Time.sleep(100);
                Mouse.getInstance().click(400, 350, true);
            }
        }

        public boolean canAlchAtm() {
            return System.currentTimeMillis() - lastAlchTime > ALCH_WAIT_TIME;
        }
    }

    private static class BuySuppliesStrat implements Strategy {

        Point[] potionBuyLocations = {new Point(188, 83), new Point(424, 83)};
        Point[] secondBuyLocations = {new Point(377, 132), new Point(93, 178)};
        Point runeBuyLocation = new Point(94, 130);
        int STORE_runesID = 546;
        int STORE_potionsID = 581;
        int STORE_secondsID = 561;

        @Override
        public boolean activate() {
            boolean act = Game.isLoggedIn() && FLAG_buyPotions
                    && !FLAG_inRandom;
            return act;
        }

        @Override
        public void execute() {
            status = "Buying";
            boolean hasRunes = Inventory.getCount(true, runeId) >= 200;
            boolean hasPots = Inventory.getCount(true, notedPotionIds) >= 200;
            boolean hasSecond = Inventory.getCount(true, notedSecondIds) >= 200;
            Camera.setPitch(true);
            if (!inStore()) {
                goToStore();
            } else if (!hasPots) {
                buyPots();
            } else if (!hasSecond) {
                buySecondaries();
            } else if (!hasRunes) {
                buyRunes();
            } else {
                FLAG_buyPotions = false;
                FLAG_goToBank = true;
            }

        }

        static Rectangle storeBounds = new Rectangle(3076, 3508, 3084 - 3076, 3513 - 3508);

        public static boolean inStore() {
            Tile currentTile = Players.getLocal().getLocation();
            Point currentPoint = new Point(currentTile.getX(), currentTile.getY());
            return storeBounds.contains(currentPoint);
        }

        public void goToStore() {
            Keyboard.getInstance().sendKeys("::shops");
            Time.sleep(3000);
        }

        public void buyRunes() {
            openShop(STORE_runesID, "Trade");
            // Buy runes
            Menu.interact("Buy 100", runeBuyLocation);
            Time.sleep(300);
            Menu.interact("Buy 100", runeBuyLocation);
            Time.sleep(300);
            ExtraMethods.closeShop();
        }

        public void buyPots() {
            openShop(STORE_potionsID, "Trade");
            for (int i = 0; i < notedPotionIds.length; i++) {
                try {
                    int numPots = Inventory.getCount(true, notedPotionIds[i]);
                    if (numPots < 100) {
                        // Buy more pots
                        Menu.interact("Buy 100", potionBuyLocations[i]);
                        Time.sleep(500);
                    }
                } catch (Exception e) {
                    // Shop closed. Just ignore
                }
            }
            ExtraMethods.closeShop();
        }

        public void buySecondaries() {
            openShop(STORE_secondsID, "Trade");
            for (int i = 0; i < notedSecondIds.length; i++) {
                try {
                    int numSeconds = Inventory.getCount(true, notedSecondIds[i]);
                    if (numSeconds < 100) {
                        // Buy more seconds
                        Menu.interact("Buy 100", secondBuyLocations[i]);
                        Time.sleep(500);
                    }
                } catch (Exception e) {
                    // Shop closed. Just ignore
                }
            }
            ExtraMethods.closeShop();
        }

        public void openShop(int shopID, String openAction) {
            // Open the shop
            Npc[] NPCs = Npcs.getNearest(shopID);
            if (NPCs.length == 0) {
                LogArea.error("Buying Inconsistency 1: " + shopID + " < > " + openAction);
            }
            try {
                Npc NPC = NPCs[0];
                while (!ExtraMethods.inPlayScreenBounds(NPC.getLocation().toScreen())
                        || !ExtraMethods.inPlayScreenBounds(NPC.getCenterPointOnScreen())) {
                    ExtraMethods.rotateCamera(10);
                }
                //NPC.interact(openAction);
                ExtraMethods.jiggleMenuInteract(NPC, openAction, 10);
                Time.sleep(300);
                for (int i = 0; i < 5000; i += 50) {
                    if (Players.getLocal().getAnimation() == -1) {
                        break;
                    }
                    Time.sleep(50);
                }
            } catch (Exception e) {
                LogArea.error("Buying error 1: " + shopID + " < > " + openAction);
            }
        }

    }

    /**
     * Strategy will cut magic logs with an equipped adz axe
     */
    private static class MakePotsStrat implements Strategy {

        static int makeWait = 5000;
        static long lastMakeTime = System.currentTimeMillis() - makeWait * 2;

        private boolean canMakeAgain() {
            return System.currentTimeMillis() - lastMakeTime > makeWait;
        }

        @Override
        public boolean activate() {
            return Game.isLoggedIn() && !FLAG_buyPotions && !FLAG_goToBank && !FLAG_inRandom && !Bank.isOpen()
                    && canMakeAgain() && numPotsCanMake() > 0
                    && Inventory.getCount(ITEM_SUPER_PRAYER, ITEM_EXTREME_MAGIC) == 0;
        }

        @Override
        public void execute() {
            status = "Making pots";
            Menu.interact("Inventory", new Point(641, 183));
            Time.sleep(100);
            int prayerCount = Math.min(Inventory.getCount(ITEM_BONEMEAL), Inventory.getCount(ITEM_PRAYER_POT));
            int magicCount = Math.min(Inventory.getCount(ITEM_DUST), Inventory.getCount(ITEM_MAGIC_POT));
            Item use = null, on = null;
            String useAction = null;
            try {
                if (prayerCount > 1) {
                    use = Inventory.getItems(ITEM_BONEMEAL)[0];
                    on = Inventory.getItems(ITEM_PRAYER_POT)[0];
                    useAction = "Use Bonemeal ->";
                } else if (magicCount > 1) {
                    use = Inventory.getItems(ITEM_DUST)[0];
                    on = Inventory.getItems(ITEM_MAGIC_POT)[0];
                    useAction = "Use Ground mud runes ->";
                } else {
                    FLAG_bank = true;
                    return;
                }

                use.interact("Use");
                on.interact(useAction);
                Time.sleep(500);
                Mouse.getInstance().click(263, 446, true);
                lastMakeTime = System.currentTimeMillis();
            } catch (Exception e) {
                FLAG_bank = true;
            }
        }
    }

    private static int[] randomNPCList = {3117, 410, 1091};

    private boolean inRandom() {
        try {
            Npc[] nearbyNpcs = Npcs.getNearest(randomNPCList);
            if (nearbyNpcs.length == 0) {
                return false;
            }
            try {

                double distance = ExtraMethods.distance(nearbyNpcs[0].getLocation(), Players.getLocal().getLocation());
                return distance <= 1;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Random exception 1");
            return false;
        }
    }

    public class HandleRandomsStrat implements Strategy {

        @Override
        public boolean activate() {
            if (Game.isLoggedIn()) {
                if (inRandom()) {
                    FLAG_inRandom = true;
                    return true;
                }
            }
            FLAG_inRandom = false;
            return false;
        }

        @Override
        public void execute() {
            Npc[] nearbyNpcs = Npcs.getNearest(randomNPCList);
            try {
                Npc random = nearbyNpcs[0];
                LogArea.log("Random found: " + random.getName() + "," + random.getDef().getId());
                Time.sleep(1000);
                int ID = random.getDef().getId();

                // Make sure the random NPC is close to ensure 
                // it's mine (redundant for safety)
                double distance = ExtraMethods.distance(random.getLocation(), Players.getLocal().getLocation());
                if (distance <= 1) {
                    // Determine which NPC it is and act accordingly
                    switch (ID) {
                        case 3117:
                        case 410:
                            Time.sleep(1000);
                            random.interact("Talk-to");
                            Time.sleep(1000);
                            Mouse.getInstance().click(306, 448, true);
                            FLAG_inRandom = false;
                            break;
                        case 1091:
                            LogArea.error("Bob event detected! You must complete it.");
                            java.awt.Toolkit.getDefaultToolkit().beep();
                            Time.sleep(500);
                            java.awt.Toolkit.getDefaultToolkit().beep();
                            Time.sleep(500);
                            java.awt.Toolkit.getDefaultToolkit().beep();
                            Time.sleep(5000);
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }

    }

}
