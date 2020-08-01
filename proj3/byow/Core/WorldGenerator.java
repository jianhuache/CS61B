package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Create a random world with rooms connected by hallways.
 */
public class WorldGenerator {

    // Create a world, return the position of avatar's birthplace
    public static Position createWorld(TETile[][] world, Random random) {
        setWorldBackgroundAsWalls(world); // Fill whole world with wall tiles.
        Queue<Room> rooms = placeRoomsToWorld(world, random); // Carve rooms at random place.
        connectRoomsInWorld(world, rooms, random); // Connect rooms with hallways has no walls.
        removeRedundantWalls(world); // Cleverly make hallways have walls.
        return addAvatar(world, rooms, random); // Add an avatar to the world.
    }

    // Add an avatar to the world, within a random room.
    // Return position of the avatar.
    private static Position addAvatar(TETile[][] world, Queue<Room> rooms, Random random) {
        List<Room> birthPlaces = new ArrayList<>();
        for (Room room : rooms) {
            birthPlaces.add(room);
        }

        int i = random.nextInt(birthPlaces.size());
        Room birthRoom = birthPlaces.get(i);
        // Make sure that avatar does not born in wall.
        int avatarX = birthRoom.pos.x + 1 + random.nextInt(birthRoom.width - 2);
        int avatarY = birthRoom.pos.y + 1 + random.nextInt(birthRoom.height - 2);
        world[avatarX][avatarY] = Tileset.AVATAR;

        return new Position(avatarX, avatarY);
    }

    // Connect rooms using floor tile hallways with no walls.
    // Note that hallway is built either from left or from bottom.
    private static void connectRoomsInWorld(TETile[][] world, Queue<Room> rooms, Random random) {
        Queue<Room> toBeConnected = new LinkedList<>();
        // If set toBeConnected = rooms directly, rooms will also be altered after following loop.
        for (Room room : rooms) {
            toBeConnected.offer(room);
        }

        while (toBeConnected.size() > 1) { // Make sure that all rooms are connected.
            Room roomA = toBeConnected.poll();
            Room roomB = toBeConnected.poll();

            // Calculate center position of each room.
            Position roomACentre = new Position(roomA.pos.x + (roomA.width / 2),
                                                roomA.pos.y + (roomA.height / 2));
            Position roomBCentre = new Position(roomB.pos.x + (roomB.width / 2),
                                                roomB.pos.y + (roomB.height / 2));

            // Choose left room's x position as horizontal hallway's start x position.
            // Choose lower room's y position as horizontal hallway's start y position.
            int horzStartX = Math.min(roomACentre.x, roomBCentre.x);
            int horzEndX = Math.max(roomACentre.x, roomBCentre.x);
            int horzLength = horzEndX - horzStartX;
            int horzStartY = Math.min(roomACentre.y, roomBCentre.y);
            Position horzHallwayPos = new Position(horzStartX, horzStartY);

            // Choose lower room's y position as vertical hallway's start y position.
            // Choose higher room's x position as vertical hallway's start x position.
            int vertStartY = Math.min(roomACentre.y, roomBCentre.y);
            int vertEndY = Math.max(roomACentre.y, roomBCentre.y);
            int vertLength = vertEndY - vertStartY;
            int vertStartX = roomACentre.y >= roomBCentre.y ? roomACentre.x : roomBCentre.x;
            Position vertHallwayPos = new Position(vertStartX, vertStartY);

            // Build hallways only using floor tiles.
            addFloorRowToWorld(world, horzLength, horzHallwayPos);
            addFloorColToWorld(world, vertLength, vertHallwayPos);

            // Pick a random room from two polled rooms, reinsert it into queue,
            // thus guaranteeing already connected rooms can be connected to other rooms.
            int i = random.nextInt(2);
            switch (i) {
                default:
                case 0:
                    toBeConnected.offer(roomA);
                    break;
                case 1:
                    toBeConnected.offer(roomB);
                    break;
            }
        }
    }

    // Place random number, random size, random position rooms to the world,
    // and return a queue of rooms prepared for connection.
    private static Queue<Room> placeRoomsToWorld(TETile[][] world, Random random) {
        List<BPSpace> space = new LinkedList<>();
        Queue<BPSpace> queue = new LinkedList<>();
        Queue<Room> rooms = new LinkedList<>();
        // height is less than world actual height, save place for HUD.
        BPSpace root = new BPSpace(new Position(0, 0), world.length, world[0].length - 1);
        space.add(root);
        queue.offer(root);

        int num = 15 + random.nextInt(10); // A suitable number.
        while (num > 0 && !queue.isEmpty()) {
            BPSpace toPartition = queue.poll();
            if (toPartition.partition(random)) {
                space.add(toPartition.leftChild);
                space.add(toPartition.rightChild);
                // Try to partition every child.
                queue.offer(toPartition.leftChild);
                queue.offer(toPartition.rightChild);
            }
            num -= 1;
        }
        root.buildRoom(random);
        for (BPSpace subspace : space) {
            if (subspace.room != null) {
                addRoomToWorld(world, subspace.room);
                rooms.offer(subspace.room);
            }
        }
        return rooms;
    }

    // Add a room to the world.
    private static void addRoomToWorld(TETile[][] world, Room room) {
        Position pos = room.pos;
        int width = room.width;
        int height = room.height;

        Position botWallPos = pos;
        Position topWallPos = new Position(pos.x, pos.y + height - 1);
        Position leftWallPos = pos;
        Position rightWallPos = new Position(pos.x + width - 1, pos.y);

        addWallRowToWorld(world, width, botWallPos);
        addWallRowToWorld(world, width, topWallPos);
        addWallColToWorld(world, height, leftWallPos);
        addWallColToWorld(world, height, rightWallPos);

        for (int h = 1; h < height - 1; h += 1) {
            Position floorPos = new Position(pos.x + 1, pos.y + h);
            addFloorRowToWorld(world, width - 2, floorPos);
        }
    }

    // Add a row of wall tile to the world, from left to right.
    private static void addWallRowToWorld(TETile[][] world, int length, Position pos) {
        addTileRowToWorld(world, length, pos, Tileset.WALL);
    }

    // Add a column of wall tile to the world, from bottom to up.
    private static void addWallColToWorld(TETile[][] world, int length, Position pos) {
        addTileColToWorld(world, length, pos, Tileset.WALL);
    }

    // Add a row of floor tile to the world, from left to right.
    private static void addFloorRowToWorld(TETile[][] world, int length, Position pos) {
        addTileRowToWorld(world, length, pos, Tileset.FLOOR);
    }

    // Add a column of floor tile to the world, from bottom to up.
    private static void addFloorColToWorld(TETile[][] world, int length, Position pos) {
        addTileColToWorld(world, length, pos, Tileset.FLOOR);
    }

    // Add a row of specific tile with specific length to the world, from left to right.
    private static void addTileRowToWorld(TETile[][] world, int length, Position pos, TETile tile) {
        for (int i = 0; i < length; i += 1) {
            world[pos.x + i][pos.y] = tile;
        }
    }

    // Add a column of specific tile with specific length to the world, from bottom to up.
    private static void addTileColToWorld(TETile[][] world, int length, Position pos, TETile tile) {
        for (int i = 0; i < length; i += 1) {
            world[pos.x][pos.y + i] = tile;
        }
    }

    // Set world background as wall tiles.
    private static void setWorldBackgroundAsWalls(TETile[][] world) {
        for (int x = 0; x < world.length; x += 1) {
            for (int y = 0; y < world[0].length; y += 1) {
                world[x][y] = Tileset.WALL;
            }
        }
    }

    // Replace redundant walls with nothing.
    // If a wall's all eight neighbours are walls, it is redundant.
    // Cleverly make hallways have walls.
    private static void removeRedundantWalls(TETile[][] world) {
        int[][] neighbours = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0},
                                         {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int x = 0; x < world.length; x += 1) {
            for (int y = 0; y < world[0].length; y += 1) {
                if (world[x][y].equals(Tileset.WALL)) {
                    int floorNum = 0;
                    for (int[] nbr : neighbours) {
                        int adjX = x + nbr[0];
                        int adjY = y + nbr[1];
                        if (adjX >= 0 && adjX < world.length) {
                            if (adjY >= 0 && adjY < world[0].length) {
                                if (world[adjX][adjY].equals(Tileset.FLOOR)) {
                                    floorNum += 1;
                                }
                            }
                        }
                    }
                    // Check whether current wall has floor neighbours.
                    // If not, it is redundant, replace it with nothing.
                    if (floorNum == 0) {
                        world[x][y] = Tileset.NOTHING;
                    }
                }
            }
        }
    }
}
