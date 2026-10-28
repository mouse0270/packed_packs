package io.github.fishstiz.packed_packs.util;

/** Metadata references are distinct from Minecraft's canonical file/ pack IDs. */
public final class FolderPackIds {
    private FolderPackIds() {}

    public static boolean isRoot(String id) {
        if (!id.startsWith("root/")) return false;
        String name = id.substring(5);
        return !name.isBlank() && !name.equals(".") && !name.equals("..")
                && !name.contains("/") && !name.contains("\\") && !name.contains(":");
    }

    public static String resolve(String id, String parentId) {
        if (id.startsWith("relative/")) return parentId + id.substring(8);
        if (isRoot(id)) return "file/" + id.substring(5);
        return id;
    }

    public static String serialize(String id, String parentId) {
        if (parentId == null || !id.startsWith("file/")) return id;
        if (id.startsWith(parentId + "/")) return "relative/" + id.substring(parentId.length() + 1);
        String root = "root/" + id.substring(5);
        return isRoot(root) ? root : id;
    }
}
