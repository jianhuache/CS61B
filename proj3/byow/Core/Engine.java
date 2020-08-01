package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.util.Random;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;
    private static final int TILE_SIZE = 16;

    private TETile[][] world = new TETile[WIDTH][HEIGHT];
    private StringBuilder record = new StringBuilder();
    private Position avatarPos = new Position(0, 0);

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        InputSource source = new KeyboardInputSource();
        boolean gameOver = false;
        ter.initialize(WIDTH, HEIGHT);
        drawStartMenu();
        while (!gameOver) {
            if (record.length() > 0) { // Make mouse display be real-time.
                ter.renderFrame(world);
                tileInfo(new Position((int) StdDraw.mouseX(), (int) StdDraw.mouseY()));
            }
            if (StdDraw.hasNextKeyTyped()) {
                char action = source.getNextKey();
                takeAction(source, action);
            }
        }
    }

    // Take the action based on input source type.
    private void takeAction(InputSource source, char action) {
        record.append(action);
        // Create a new world.
        if (action == 'N') {
            long seed = inputSeed(source);
            record.append(seed);
            record.append('S');
            Random random = new Random(seed);
            avatarPos = WorldGenerator.createWorld(world, random);
            // If interacts with string, do not render.
            if (source.getClass().equals(KeyboardInputSource.class)) {
                ter.renderFrame(world);
            }
        } else if (action == ':') { // Save and Quit.
            char nextAction = source.getNextKey();
            if (nextAction == 'Q') {
                record.deleteCharAt(record.length() - 1); // Remove ':' from record.
                save(record.toString()); // Save current world state to the file.
                if (source.getClass().equals(KeyboardInputSource.class)) {
                    System.exit(0);
                }
            }
        } else if (action == 'L') { // Load saved world.
            record.deleteCharAt(record.length() - 1); // Remove 'L' from the record.
            String savedRecord = load();
            if (savedRecord.equals("")) {
                System.exit(0); // Exit if no saved data.
            } else {
                world = interactWithInputString(savedRecord); // Load saved world.
                if (source.getClass().equals(KeyboardInputSource.class)) {
                    ter.renderFrame(world);
                }
            }
        } else if (action == 'W') { // Move avatar upwards if there is no wall.
            int x = avatarPos.x;
            int y = avatarPos.y + 1;
            if (world[x][y].equals(Tileset.FLOOR)) {
                world[x][y] = Tileset.AVATAR; // Update world.
                world[avatarPos.x][avatarPos.y] = Tileset.FLOOR;
                avatarPos = new Position(x, y); // Update avatar's position.
                if (source.getClass().equals(KeyboardInputSource.class)) {
                    ter.renderFrame(world);
                }
            }
        } else if (action == 'A') { // Move avatar to left if there is no wall.
            int x = avatarPos.x - 1;
            int y = avatarPos.y;
            if (world[x][y].equals(Tileset.FLOOR)) {
                world[x][y] = Tileset.AVATAR; // Update world.
                world[avatarPos.x][avatarPos.y] = Tileset.FLOOR;
                avatarPos = new Position(x, y); // Update avatar's position.
                if (source.getClass().equals(KeyboardInputSource.class)) {
                    ter.renderFrame(world);
                }
            }
        } else if (action == 'S') { // Move avatar downwards if there is no wall.
            int x = avatarPos.x;
            int y = avatarPos.y - 1;
            if (world[x][y].equals(Tileset.FLOOR)) {
                world[x][y] = Tileset.AVATAR; // Update world.
                world[avatarPos.x][avatarPos.y] = Tileset.FLOOR;
                avatarPos = new Position(x, y); // Update avatar's position.
                if (source.getClass().equals(KeyboardInputSource.class)) {
                    ter.renderFrame(world);
                }
            }
        } else if (action == 'D') { // Move avatar to right if there is no wall.
            int x = avatarPos.x + 1;
            int y = avatarPos.y;
            if (world[x][y].equals(Tileset.FLOOR)) {
                world[x][y] = Tileset.AVATAR; // Update world.
                world[avatarPos.x][avatarPos.y] = Tileset.FLOOR;
                avatarPos = new Position(x, y); // Update avatar's position.
                if (source.getClass().equals(KeyboardInputSource.class)) {
                    ter.renderFrame(world);
                }
            }
        }
    }

    // Save data to a file.
    private static void save(String record) {
        File f = new File("./save_data.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(record);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    // Load data from a file.
    private static String load() {
        File f = new File("./save_data.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (String) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        // In the case no file has been saved yet, we return an empty string.
        return "";
    }

    // Display the description of the tile pointed by mouse.
    private void tileInfo(Position mousePos) {
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.textLeft(1, HEIGHT - 1, world[mousePos.x][mousePos.y].description());
        StdDraw.show();
    }

    // Draw the start menu when player interacts with keyboard.
    private void drawStartMenu() {
        StdDraw.clear(StdDraw.BLACK);
        // Draw title.
        Font font = new Font("Monaco", Font.BOLD, 60);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT * 3 / 4, "CS61B: Project 3");
        // Draw menu options.
        font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 10, "New World (N)");
        StdDraw.text(WIDTH / 2, HEIGHT * 4 / 10, "Load World (L)");
        StdDraw.text(WIDTH / 2, HEIGHT * 3 / 10, "Quit (Q)");
        // Reset font size to TeRenderer's default size.
        font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);
        StdDraw.show();
    }

    // Help to generate the seed of the world.
    private long inputSeed(InputSource source) {
        // Display the typing interface.
        if (source.getClass().equals(KeyboardInputSource.class)) {
            drawSeed("");
        }
        // Display seed typed in.
        StringBuilder seedRecord = new StringBuilder();
        long seed = 0L;
        while (source.possibleNextInput()) {
            char next = source.getNextKey();
            if (next != 'S') {
                seed = seed * 10 + Character.getNumericValue(next);
                seedRecord.append(next);
                if (source.getClass().equals(KeyboardInputSource.class)) {
                    drawSeed(seedRecord.toString());
                    StdDraw.show();
                }
            } else {
                break;
            }
        }
        return seed;
    }

    // Display the seed typed by the player when interact with keyboard.
    private void drawSeed(String s) {
        StdDraw.clear(StdDraw.BLACK);
        // Draw instruction.
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT * 6 / 10, "Please type a seed, press 'S' to confirm");
        // Display seed typed.
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 10, s);
        // Reset font size to TeRenderer's default size.
        font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);
        StdDraw.show();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // DONE: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        InputSource source = new StringInputSource(input);
        while (source.possibleNextInput()) {
            char action = source.getNextKey();
            takeAction(source, action);
        }
        TETile[][] finalWorldFrame = world;
        return finalWorldFrame;
    }
}
