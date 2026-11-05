package it.technoade.technoblocks.player;

/**
 * Hidden tag values that mark plugin items.
 */
public final class ItemProtection {

    public static final String SELECTOR = "selector";
    public static final String BLOCK_PREFIX = "block:";

    private ItemProtection() {
    }

    public static boolean isPluginTag(String tag) {
        return tag != null && (tag.equals(SELECTOR) || tag.startsWith(BLOCK_PREFIX));
    }

    /** @return the block key, or null if the tag is not a block tag */
    public static String blockKey(String tag) {
        if (tag == null || !tag.startsWith(BLOCK_PREFIX)) return null;
        return tag.substring(BLOCK_PREFIX.length());
    }

    /** True if any item involved in an inventory click is a plugin item. */
    public static boolean shouldCancelClick(String currentTag, String cursorTag, String hotbarSlotTag) {
        return isPluginTag(currentTag) || isPluginTag(cursorTag) || isPluginTag(hotbarSlotTag);
    }
}
