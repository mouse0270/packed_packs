# Personal fork: root pack references

Based on upstream Packed Packs v2.3.1+1.21.1 (MIT), for Minecraft 1.21.1.
Personal build version: 2.3.1-root.4+1.21.1.

The pack context menu always includes Copy to Clipboard, even with developer
mode off. It copies the canonical pack ID (for example `file/Fresh Paintings.zip`),
matching the original action. It appears once in either mode.

Grouped file packs show a small badge at the bottom-left of their pack icon:
a blue house for root references, or a gold folder for relative packs. Hover
over the badge to see the storage type and full file path. The badge does not
transfer or reorder packs when clicked. Built-in packs and ungrouped root packs
have no location badge.

Keep launcher-managed ZIPs directly in `resourcepacks`, and reference them from
`resourcepacks/My Group/packed_packs.folderpack.json`:

```json
{
  "module": true,
  "packIds": [
    "root/Fresh Paintings.zip",
    "relative/Another Pack.zip"
  ]
}
```

`root/` means the main resource-pack directory, not the Minecraft instance
directory or an additional source directory. Only immediate root children are
accepted. Ordinary unpacked resource packs also work. Folder groups themselves
cannot be virtually nested via `root/`, preventing cycles.

Root references become virtual children of the group in the Packed Packs UI.
With `module: true`, they are grouped in both available and selected lists;
open the group to inspect/reorder them. Minecraft still loads their normal
`file/Filename.zip` IDs. Relative references and built-in pack IDs continue to
work. No pack is physically moved by the mod.

Reordering and renaming in the GUI preserve root references. Temporarily missing
root references survive order saves. If multiple groups claim a root pack, the
lexicographically first folder ID wins; a warning is logged for duplicate claims.
Nested groups receive updated children too.

Use the Packed Packs screen for grouping; Minecraft's original pack screen does
not display virtual folders. Unlocked (`module: false`) groups retain upstream
behavior: enabled children may appear individually in the selected column.

## Launcher updates

Keeping files at the root makes them visible to launcher scanning, but does not
guarantee project identification or available updates. If an update changes a
ZIP's filename, update the `root/` entry to match. Matching is exact, including
formatting characters. Close the Packed Packs screen before editing JSON, then
reopen it; metadata is cached while the screen is open.

Do not leave a second copy inside the group unless you intentionally want two
packs: physical children are discovered even when omitted from `packIds`.

## Build and checks

Requires Java 21. On Windows, set `JAVA_HOME`, then run:

```powershell
./tests/test-root-references.ps1
./gradlew.bat :neoforge:build
```

The standalone reference tests check resolution, save/load round trips, Unicode,
mixed relative/root IDs, built-in IDs, and rejected traversal/subdirectory paths.
The full build checks compilation and packages the NeoForge mod. These checks
do not replace an in-game test of the GUI and resource loading.

## In-game test

1. Start/restart the instance and open Options > Resource Packs (Packed Packs).
2. Open the group. Check that root entries appear once and in JSON order.
3. Enable the group and apply; verify its textures load.
4. Reorder a root entry, close/reopen the screen, and check that its JSON ID
   still starts with `root/`.
5. Confirm the launcher can see the ZIP in the root resource-pack list.

Replace the original mod JAR with the personal JAR; do not enable both at once.
Keep backups of the original JAR and any JSON/files you migrate for testing.
