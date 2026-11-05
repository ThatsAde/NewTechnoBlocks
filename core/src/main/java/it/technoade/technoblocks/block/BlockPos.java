package it.technoade.technoblocks.block;

import org.bukkit.block.Block;

/**
 * World name plus block coordinates; safe to keep after the chunk unloads.
 */
public final class BlockPos {

    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public BlockPos(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockPos of(Block block) {
        return new BlockPos(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    /** "world;x;y;z" */
    public String serialize() {
        return world + ";" + x + ";" + y + ";" + z;
    }

    /** @return the position, or null if the text is malformed */
    public static BlockPos parse(String text) {
        String[] parts = text.split(";");
        if (parts.length != 4 || parts[0].isEmpty()) return null;
        try {
            return new BlockPos(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockPos)) return false;
        BlockPos other = (BlockPos) o;
        return x == other.x && y == other.y && z == other.z && world.equals(other.world);
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return serialize();
    }
}
