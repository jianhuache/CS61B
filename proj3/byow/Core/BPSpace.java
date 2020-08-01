package byow.Core;

import java.util.Random;

/**
 * Binary Partition Space.
 */
public class BPSpace {

    private static final int MIN_SIZE = 6;

    private int width;
    private int height;
    Position pos; // Bottom left position.
    BPSpace leftChild;
    BPSpace rightChild;
    Room room;

    public BPSpace(Position pos, int width, int height) {
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.leftChild = null;
        this.rightChild = null;
        this.room = null;
    }

    public boolean partition(Random random) {
        // If already split, bail out.
        if (leftChild != null) {
            return false;
        }

        // Next direction to partition.
        boolean horizontal;
        if (height > width) {
            horizontal = true;
        } else if (width > height) {
            horizontal = false;
        } else {
            horizontal = random.nextBoolean();
        }

        // Make sure that both left(bottom) and right(top) child can have MIN_SIZE.
        int max = (horizontal ? height : width) - MIN_SIZE;
        // If area is too small to split, bail out.
        if (max <= MIN_SIZE) {
            return false;
        }

        // Generate split point.
        int split = random.nextInt(max);
        // Adjust split point so there is at least MIN_SIZE in both partitions.
        if (split < MIN_SIZE) {
            split = MIN_SIZE;
        }

        if (horizontal) {
            leftChild = new BPSpace(pos, width, split); // Bottom child.
            rightChild = new BPSpace(new Position(pos.x, pos.y + split),
                                     width, height - split); // Top child.
        } else {
            leftChild = new BPSpace(pos, split, height);
            rightChild = new BPSpace(new Position(pos.x + split, pos.y),
                                     width - split, height);
        }
        return true;
    }

    public void buildRoom(Random random) {
        // If current area has child areas, then we cannot build room in it,
        // instead, go to check its children.
        if (leftChild != null) {
            leftChild.buildRoom(random);
            rightChild.buildRoom(random);
        } else {
            int offsetX = (width - MIN_SIZE <= 0) ? 0 : random.nextInt(width - MIN_SIZE);
            int offsetY = (height - MIN_SIZE <= 0) ? 0 : random.nextInt(height - MIN_SIZE);

            // Room is at least one grid away from the left, right, top edges of current space.
            Position roomPos = new Position(pos.x + offsetX, pos.y + offsetY);
            int roomWidth = Math.max(random.nextInt(width - offsetX), MIN_SIZE);
            int roomHeight = Math.max(random.nextInt(height - offsetY), MIN_SIZE);
            room = new Room(roomPos, roomWidth, roomHeight);
        }
    }
}
