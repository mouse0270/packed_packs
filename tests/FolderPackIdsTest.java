import io.github.fishstiz.packed_packs.util.FolderPackIds;
import java.util.List;

public class FolderPackIdsTest {
    private static int checks;

    private static void equal(Object expected, Object actual) {
        checks++;
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }

    public static void main(String[] args) {
        String parent = "file/Outer/My Group";
        for (String name : List.of("Fresh Paintings.zip", "§e§lAllure 3D Plants§r§0.zip",
                "Farmer's Delight.zip", "Rails & Flowers.zip", "Unzipped Pack")) {
            equal("file/" + name, FolderPackIds.resolve("root/" + name, parent));
            equal(parent + "/" + name, FolderPackIds.resolve("relative/" + name, parent));
            equal("root/" + name, FolderPackIds.serialize("file/" + name, parent));
            equal("relative/" + name, FolderPackIds.serialize(parent + "/" + name, parent));
            equal("file/" + name, FolderPackIds.serialize("file/" + name, null));
            equal("file/" + name, FolderPackIds.resolve(
                    FolderPackIds.serialize("file/" + name, parent), parent));
        }
        for (String bad : List.of("root/", "root/..", "root/.", "root/../outside.zip",
                "root/sub/pack.zip", "root/sub\\pack.zip", "root/C:pack.zip", "relative/pack.zip")) {
            equal(false, FolderPackIds.isRoot(bad));
        }
        equal("root/../outside.zip", FolderPackIds.resolve("root/../outside.zip", parent));
        equal("continuity:default", FolderPackIds.resolve("continuity:default", parent));
        equal("continuity:default", FolderPackIds.serialize("continuity:default", parent));
        equal("file/Other/pack.zip", FolderPackIds.serialize("file/Other/pack.zip", parent));
        equal("file/GroupExtra/pack.zip", FolderPackIds.serialize("file/GroupExtra/pack.zip", "file/Group"));
        System.out.println("Passed " + checks + " root/relative reference checks.");
    }
}
