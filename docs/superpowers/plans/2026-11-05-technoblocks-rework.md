# TechnoBlocks 3.0 Rework Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Riscrivere TechnoBlocks come plugin multi-modulo (1.8.8 → 26.x, un solo jar) in cui i blocchi temporanei mostrano crepe crescenti prima di sparire.

**Architecture:** `api` definisce `VersionAdapter`; due basi astratte (`common-legacy` per 1.8-1.12, `common-flat` per 1.13+) implementano la logica condivisa; 13 moduli NMS e un modulo `modern` (API `sendBlockDamage` con sourceId, 1.19.4+) completano l'adapter; `core` (Java 8) contiene tutta la logica del plugin e carica l'adapter per nome via reflection; `dist` produce il jar shaded.

**Tech Stack:** Java (release 8/16/17), Maven multi-modulo + Maven Wrapper, Spigot API/NMS da CodeMC, PlaceholderAPI (softdepend), JUnit 5.

Nota: i test unitari girano senza server Bukkit, quindi niente `Bukkit.*`, `getItemMeta()` né `createBlockData()` nel codice testato.

**Spec:** `docs/superpowers/specs/2026-11-05-technoblocks-rework-design.md`: leggerla prima di iniziare; le sezioni citate come §N si riferiscono a lei.

## Global Constraints

- Ambiente di build: JDK 22 in `C:\Program Files\Java\jdk-22` (sul PATH c'è un JRE 8: impostare sempre `JAVA_HOME`). Maven non è installato: si usa `./mvnw`.
- Comando di build standard (Git Bash, dalla root): `JAVA_HOME="/c/Program Files/Java/jdk-22" ./mvnw -B -q package`
- `api`, `core`, `common-legacy`, `common-flat`, moduli NMS 1.8-1.16: `maven.compiler.release=8`. `nms-v1_17_R1`: 16. `nms-v1_18_R2`, `nms-v1_19_R1`, `nms-v1_19_R2`, `modern`: 17.
- Codice `core`/`api`/`common-*`: niente `var`, record, switch expression, `instanceof` pattern, `List.of`, API Bukkit successive alla 1.8.8 (core compila contro `spigot-api:1.8.8-R0.1-SNAPSHOT`).
- Nessuna libreria esterna di pacchetti/materiali (ProtocolLib, PacketEvents, XSeries).
- Pacchetto base `it.technoade.technoblocks`; versione `3.0.0`; jar finale `dist/target/TechnoBlocks-3.0.0.jar`.
- `plugin.yml`: `api-version: 1.13`, `softdepend: [PlaceholderAPI]`, comando `technoblocks` alias `tb`, permesso `technoblocks.admin` (default op).
- Repository: Spigot `https://hub.spigotmc.org/nexus/content/repositories/snapshots/`, NMS `https://repo.codemc.io/repository/nms/` (artifact `org.spigotmc:spigot:<ver>-R0.1-SNAPSHOT`), PAPI `https://repo.extendedclip.com/releases/` (`me.clip:placeholderapi:2.11.6`).
- Crepe: stage 0..9, reset = -1; refresh ogni 5000 ms; `view-distance` default 32; sourceId da contatore che parte da `Integer.MAX_VALUE` e decresce.
- Tag item: chiave `technoblocks`; valore `selector` per il selettore, `block:<chiave>` per i blocchi.
- Nessuno stato `static` mutabile nel core (eccetto costanti).

## Review Focus

1. **Item del plugin spostati con mezzi "laterali"** (tasto numerico sulla hotbar, shift-click, drag, swap mano secondaria F, drop da GUI aperta): devono restare nei loro slot. → Task 9, `ItemProtectionTest`.
2. **Due blocchi piazzati vicini nello stesso tick (anche da giocatori diversi)**: ognuno ha il suo sourceId e le sue crepe. → Task 8, `TemporaryBlockRegistryTest.uniqueSourceIds`.
3. **Blocco temporaneo registrato in una posizione già occupata da un altro temporaneo** (es. dopo rottura in creative non notificata): la nuova voce sostituisce la vecchia e la vecchia non viene "ripristinata" sopra la nuova. → Task 8, `TemporaryBlockRegistryTest.samePositionReplaces`.
4. **Config sporco**: `duration` 0/negativa/assente, `gui-slot` fuori dalle righe o duplicato, `gui-slot` uguale allo slot, materiale sconosciuto o vietato, `blocks` vuoto. Il plugin parte comunque, con warning chiari, e con `blocks` vuoto non dà il blocco. → Task 7, `SettingsLoaderTest`.
5. **Restart con un mondo non ancora caricato** (es. Multiverse): le voci di `active-blocks.yml` di quel mondo restano nel file invece di andare perse. → Task 8, `ActiveBlocksStoreTest.unknownWorldKept`.

---

## File Structure

```
pom.xml                                   parent (moduli, repo, versioni, plugin management)
mvnw, mvnw.cmd, .mvn/wrapper/maven-wrapper.properties
api/      src/main/java/it/technoade/technoblocks/api/{VersionAdapter,MaterialSpec}.java
common-legacy/  .../common/legacy/{LegacyAdapter,LegacyMaterials}.java  (+ test)
common-flat/    .../common/flat/{FlatAdapter,BlockRules}.java          (+ test)
nms/v1_8_R3 … nms/v1_19_R2/  .../nms/vX_Y_RZ/AdapterImpl.java           (13 moduli)
modern/   .../modern/ModernAdapter.java
core/     src/main/java/it/technoade/technoblocks/
            TechnoBlocksPlugin.java
            adapter/{AdapterResolver,AdapterLoader}.java
            config/{PluginSettings,BlockEntry,SelectorSettings,WorldFilter,SettingsLoader,ConfigUpdater,Messages}.java
            block/{BlockPos,CrackMath,TemporaryBlock,TemporaryBlockRegistry,TemporaryBlockManager,ActiveBlocksStore}.java
            data/DataStore.java
            player/{PlayerItemsService,ItemProtection}.java
            gui/{SelectorGui,SelectorHolder}.java
            listener/{PlacementListener,ProtectionListener,PlayerListener,InventoryListener}.java
            command/TechnoBlocksCommand.java
            hook/PlaceholderHook.java
          src/main/resources/{plugin.yml,config.yml,messages.yml}
          src/test/java/...                (test unitari)
dist/pom.xml                              shade → TechnoBlocks-3.0.0.jar
README.md
```
La vecchia `src/` alla root viene eliminata (resta nella history git).

---

### Task 1: Scheletro multi-modulo, Maven Wrapper, `api`, risoluzione adapter

**Files:**
- Delete: `src/` (root), `pom.xml` vecchio (riscritto)
- Create: `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.gitignore` (`target/`, `.idea/`, `*.iml`)
- Create: `api/pom.xml`, `api/src/main/java/it/technoade/technoblocks/api/VersionAdapter.java`, `.../api/MaterialSpec.java`
- Create: `core/pom.xml`, `core/src/main/java/it/technoade/technoblocks/adapter/AdapterResolver.java`
- Create: `dist/pom.xml`
- Test: `core/src/test/java/it/technoade/technoblocks/adapter/AdapterResolverTest.java`

**Interfaces:**
- Produces:
```java
public interface VersionAdapter {
    void sendCrack(Player viewer, int sourceId, Location location, int stage); // stage -1..9, -1 = reset
    ItemStack getItemInHand(Player player);
    ItemStack tag(ItemStack item, String value);       // ritorna una copia taggata
    String readTag(ItemStack item);                    // null se assente/item null
    MaterialSpec parseMaterial(String spec);           // null se sconosciuto
    boolean isAllowedBlock(MaterialSpec spec);         // §8.1 "Materiali ammessi"
    ItemStack createItem(MaterialSpec spec, int amount);
    String serializeState(BlockState state);
    void restoreState(Block block, String serialized); // senza fisica
    boolean matches(Block block, MaterialSpec spec);
}
public final class MaterialSpec { // immutabile
    public MaterialSpec(Material material, byte data);
    public Material getMaterial(); public byte getData();
    // equals/hashCode su entrambi i campi; toString() = "NAME:data"
}
public final class AdapterResolver {
    public static final String MODERN = "it.technoade.technoblocks.modern.ModernAdapter";
    /** @return nome classe adapter o null se non supportato */
    public static String resolve(boolean hasSourceIdDamageApi, String craftBukkitPackage);
}
```
Classi NMS: `it.technoade.technoblocks.nms.<rev>.AdapterImpl` dove `<rev>` ∈ `v1_8_R3, v1_9_R2, v1_10_R1, v1_11_R1, v1_12_R1, v1_13_R2, v1_14_R1, v1_15_R1, v1_16_R3, v1_17_R1, v1_18_R2, v1_19_R1, v1_19_R2`.

- [ ] **Step 1: Maven Wrapper.** Scaricare `https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper-distribution/3.3.2/maven-wrapper-distribution-3.3.2-only-script.zip` in una cartella vuota dello scratchpad, estrarre `mvnw`, `mvnw.cmd` nella root e creare `.mvn/wrapper/maven-wrapper.properties` con `distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip` e `distributionType=only-script`.

- [ ] **Step 2: Parent `pom.xml`.** `groupId it.technoade`, `artifactId technoblocks-parent`, `version 3.0.0`, `packaging pom`. Moduli: `api, common-legacy, common-flat, nms/v1_8_R3 … nms/v1_19_R2, modern, core, dist` (in questo task dichiarare solo `api, core, dist`; i task successivi aggiungono i propri). Properties: `maven.compiler.release=8`, encoding UTF-8. pluginManagement: compiler 3.13.0 (con `-Xlint:-options`), surefire 3.2.5, shade 3.6.0. dependencyManagement: `spigot-api 1.8.8-R0.1-SNAPSHOT` provided, `junit-jupiter 5.10.2` test. Repository come in Global Constraints.

- [ ] **Step 3: `api` e `core` pom** (core dipende da `api` e da spigot-api 1.8.8 provided, PAPI provided, junit test). `dist` dipende da `core` (+ in seguito tutti i moduli adapter), shade senza dependency-reduced-pom, `finalName TechnoBlocks-${project.version}`, include solo artifact `it.technoade:*`.

- [ ] **Step 4: Scrivere il test che fallisce**
```java
class AdapterResolverTest {
    @Test void modernWhenApiPresent() {
        assertEquals(AdapterResolver.MODERN, AdapterResolver.resolve(true, "org.bukkit.craftbukkit"));
        assertEquals(AdapterResolver.MODERN, AdapterResolver.resolve(true, "org.bukkit.craftbukkit.v1_20_R3"));
    }
    @Test void legacyRevisionFromPackage() {
        assertEquals("it.technoade.technoblocks.nms.v1_8_R3.AdapterImpl",
                AdapterResolver.resolve(false, "org.bukkit.craftbukkit.v1_8_R3"));
        assertEquals("it.technoade.technoblocks.nms.v1_19_R2.AdapterImpl",
                AdapterResolver.resolve(false, "org.bukkit.craftbukkit.v1_19_R2"));
    }
    @Test void unsupportedRevision() {
        assertNull(AdapterResolver.resolve(false, "org.bukkit.craftbukkit.v1_7_R4"));
        assertNull(AdapterResolver.resolve(false, "org.bukkit.craftbukkit.v1_16_R1"));
        assertNull(AdapterResolver.resolve(false, "org.bukkit.craftbukkit"));
    }
}
```

- [ ] **Step 5: Eseguire** `JAVA_HOME="/c/Program Files/Java/jdk-22" ./mvnw -B -q -pl core -am test` → FAIL (classe mancante).

- [ ] **Step 6: Implementare** `VersionAdapter`, `MaterialSpec`, `AdapterResolver` (set fisso delle 13 revisioni; revisione = ultimo segmento del package se inizia con `v`).

- [ ] **Step 7: Eseguire** lo stesso comando → PASS; poi il comando di build standard → BUILD OK.

- [ ] **Step 8: Commit** `build: multi-module skeleton, api and adapter resolver`

---

### Task 2: `common-legacy` (base 1.8-1.12)

**Files:**
- Create: `common-legacy/pom.xml` (dipende da `api`, spigot-api 1.8.8 provided)
- Create: `common-legacy/src/main/java/it/technoade/technoblocks/common/legacy/LegacyAdapter.java`, `LegacyMaterials.java`
- Test: `common-legacy/src/test/java/it/technoade/technoblocks/common/legacy/LegacyMaterialsTest.java`

**Interfaces:**
- Consumes: `VersionAdapter`, `MaterialSpec` (Task 1)
- Produces: `public abstract class LegacyAdapter implements VersionAdapter` che implementa tutto tranne `sendCrack`, `tag`, `readTag`. `public final class LegacyMaterials { static MaterialSpec parse(String spec); static boolean isAllowed(MaterialSpec spec); }`

Regole:
- `parse`: trim + upper-case. `NAME:n` → `Material.getMaterial(NAME)` con data n. Altrimenti tabella moderno→legacy, altrimenti `Material.getMaterial(NAME)` con data 0. `null` se non risolto o data fuori 0..15.
- Tabella (colori nell'ordine `WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK` = data 0..15): `<COLOR>_WOOL→WOOL`, `<COLOR>_STAINED_GLASS→STAINED_GLASS`, `<COLOR>_TERRACOTTA→STAINED_CLAY`, `<COLOR>_CONCRETE→CONCRETE` (solo se il Material esiste); `TERRACOTTA→HARD_CLAY`; legni `OAK,SPRUCE,BIRCH,JUNGLE,ACACIA,DARK_OAK` (0..5): `<W>_PLANKS→WOOD`, `OAK/SPRUCE/BIRCH/JUNGLE_LOG→LOG:0..3`, `ACACIA/DARK_OAK_LOG→LOG_2:0..1`; `GRANITE→STONE:1, POLISHED_GRANITE:2, DIORITE:3, POLISHED_DIORITE:4, ANDESITE:5, POLISHED_ANDESITE:6`; `STONE_BRICKS→SMOOTH_BRICK:0`; `BRICKS→BRICK`; `END_STONE→ENDER_STONE`; `QUARTZ_BLOCK→QUARTZ_BLOCK:0`; `SEA_LANTERN`, `SLIME_BLOCK`→`SEA_LANTERN`, `SLIME_BLOCK`.
- `isAllowed`: `isBlock() && isSolid() && !hasGravity()` e nome non in deny list: `CHEST, TRAPPED_CHEST, ENDER_CHEST, FURNACE, BURNING_FURNACE, DISPENSER, DROPPER, HOPPER, BEACON, BREWING_STAND, JUKEBOX, MOB_SPAWNER, ENCHANTMENT_TABLE, ANVIL, TNT, CAKE_BLOCK, BED_BLOCK, SIGN_POST, WALL_SIGN, STANDING_BANNER, WALL_BANNER, PISTON_BASE, PISTON_STICKY_BASE, PISTON_EXTENSION, PISTON_MOVING_PIECE, COMMAND, NOTE_BLOCK, DAYLIGHT_DETECTOR, DAYLIGHT_DETECTOR_INVERTED, CAULDRON, FLOWER_POT, SKULL, IRON_DOOR_BLOCK, WOODEN_DOOR` + ogni nome che termina in `_DOOR`, `_SHULKER_BOX`, `_FENCE_GATE`, `_PLATE`, `STAIRS`, `SLAB`, `STEP`.
- `LegacyAdapter`: `getItemInHand` → `player.getItemInHand()` (deprecato ma presente fino a 1.12); `createItem` → `new ItemStack(mat, amount, (short) data)`; `serializeState` → `NAME:rawData`; `restoreState` → `block.setTypeIdAndData(mat.getId(), data, false)`; `matches` → tipo e `getData()` uguali.

- [ ] **Step 1: Test che fallisce**
```java
class LegacyMaterialsTest {
    @Test void modernColorName() { assertEquals(new MaterialSpec(Material.WOOL, (byte) 14), LegacyMaterials.parse("red_wool")); }
    @Test void legacyWithData() { assertEquals(new MaterialSpec(Material.STAINED_GLASS, (byte) 3), LegacyMaterials.parse("STAINED_GLASS:3")); }
    @Test void plainName() { assertEquals(new MaterialSpec(Material.OBSIDIAN, (byte) 0), LegacyMaterials.parse("OBSIDIAN")); }
    @Test void woods() { assertEquals(new MaterialSpec(Material.LOG_2, (byte) 1), LegacyMaterials.parse("DARK_OAK_LOG")); }
    @Test void unknownOrBadData() { assertNull(LegacyMaterials.parse("NOT_A_BLOCK")); assertNull(LegacyMaterials.parse("WOOL:16")); assertNull(LegacyMaterials.parse("WOOL:x")); }
    @Test void concreteMissingOn188() { assertNull(LegacyMaterials.parse("RED_CONCRETE")); }
    @Test void allowed() { assertTrue(LegacyMaterials.isAllowed(new MaterialSpec(Material.STONE, (byte) 0))); }
    @Test void denied() {
        for (Material m : new Material[]{Material.SAND, Material.GRAVEL, Material.CHEST, Material.TORCH, Material.CARPET,
                Material.WOODEN_DOOR, Material.BED_BLOCK, Material.SIGN_POST, Material.ANVIL, Material.DIAMOND, Material.WOOD_STAIRS, Material.STEP})
            assertFalse(LegacyMaterials.isAllowed(new MaterialSpec(m, (byte) 0)), m.name());
    }
}
```
- [ ] **Step 2:** Aggiungere `common-legacy` ai moduli del parent; `./mvnw -B -q -pl common-legacy -am test` → FAIL.
- [ ] **Step 3:** Implementare `LegacyMaterials` e `LegacyAdapter`.
- [ ] **Step 4:** Stesso comando → PASS.
- [ ] **Step 5: Commit** `feat: legacy adapter base and material table`

---

### Task 3: `common-flat` (base 1.13+)

**Files:**
- Create: `common-flat/pom.xml` (dipende da `api`, `spigot-api:1.14.4-R0.1-SNAPSHOT` provided, release 8)
- Create: `.../common/flat/FlatAdapter.java`, `.../common/flat/BlockRules.java`
- Test: `common-flat/src/test/java/it/technoade/technoblocks/common/flat/BlockRulesTest.java`

**Interfaces:**
- Produces: `public abstract class FlatAdapter implements VersionAdapter` che implementa tutto tranne `sendCrack`. `public final class BlockRules { static boolean isAllowed(Material m); }`

Regole:
- `parseMaterial`: `NAME:n` → `Bukkit.getUnsafe().fromLegacy(new MaterialData(Material.getMaterial("LEGACY_" + NAME), (byte) n))`; altrimenti `Material.matchMaterial(NAME)`; scartare i `Material` con nome che inizia con `LEGACY_`. Data sempre 0.
- `BlockRules.isAllowed`: `isBlock() && isSolid() && !hasGravity()` e nome non in: `CHEST, TRAPPED_CHEST, ENDER_CHEST, BARREL, FURNACE, BLAST_FURNACE, SMOKER, DISPENSER, DROPPER, HOPPER, BEACON, BREWING_STAND, JUKEBOX, SPAWNER, ENCHANTING_TABLE, TNT, CAKE, PISTON, STICKY_PISTON, PISTON_HEAD, MOVING_PISTON, COMMAND_BLOCK, CHAIN_COMMAND_BLOCK, REPEATING_COMMAND_BLOCK, NOTE_BLOCK, DAYLIGHT_DETECTOR, CAULDRON, FLOWER_POT, LECTERN, BELL, CAMPFIRE, SOUL_CAMPFIRE, CONDUIT, RESPAWN_ANCHOR, LODESTONE, BEEHIVE, BEE_NEST, CRAFTER, DECORATED_POT, CHISELED_BOOKSHELF, VAULT, TRIAL_SPAWNER` + suffissi `_DOOR, _BED, _SIGN, _BANNER, SHULKER_BOX, _FENCE_GATE, _PRESSURE_PLATE, _STAIRS, _SLAB, _TRAPDOOR, _HEAD, _SKULL, ANVIL, _CANDLE_CAKE, _COPPER_BULB, CHEST`. Nomi non esistenti nella 1.14.4 si confrontano come stringhe (nessun riferimento diretto all'enum).
- `FlatAdapter`: `getItemInHand` → `getInventory().getItemInMainHand()`; `tag/readTag` → `PersistentDataContainer` con `NamespacedKey(plugin, "technoblocks")` (costruttore `FlatAdapter(Plugin plugin)`: vedi nota sotto); `createItem` → `new ItemStack(mat, amount)`; `serializeState` → `state.getBlockData().getAsString()`; `restoreState` → `block.setBlockData(Bukkit.createBlockData(s), false)`; `matches` → stesso `Material`.
- **Costruttore adapter:** tutti gli adapter hanno un costruttore pubblico `(org.bukkit.plugin.Plugin plugin)`; `LegacyAdapter` lo accetta e lo ignora. Aggiornare Task 2 se già fatto senza.

- [ ] **Step 1: Test che fallisce**
```java
class BlockRulesTest {
    @Test void allowed() { for (Material m : new Material[]{Material.STONE, Material.OBSIDIAN, Material.RED_WOOL, Material.GLASS, Material.OAK_PLANKS}) assertTrue(BlockRules.isAllowed(m), m.name()); }
    @Test void denied() { for (Material m : new Material[]{Material.SAND, Material.RED_CONCRETE_POWDER, Material.CHEST, Material.BARREL, Material.TORCH,
            Material.WHITE_CARPET, Material.OAK_DOOR, Material.RED_BED, Material.OAK_SIGN, Material.ANVIL, Material.DIAMOND, Material.OAK_STAIRS,
            Material.STONE_SLAB, Material.SHULKER_BOX, Material.RED_SHULKER_BOX, Material.TALL_GRASS}) assertFalse(BlockRules.isAllowed(m), m.name()); }
}
```
- [ ] **Step 2:** Aggiungere il modulo al parent; `./mvnw -B -q -pl common-flat -am test` → FAIL.
- [ ] **Step 3:** Implementare `BlockRules`, `FlatAdapter`.
- [ ] **Step 4:** Stesso comando → PASS.
- [ ] **Step 5: Commit** `feat: flat (1.13+) adapter base`

---

### Task 4: Moduli NMS 1.8-1.12

**Files:**
- Create: `nms/v1_8_R3`, `nms/v1_9_R2`, `nms/v1_10_R1`, `nms/v1_11_R1`, `nms/v1_12_R1`: ognuno con `pom.xml` (dipende da `common-legacy` e `org.spigotmc:spigot:<ver>-R0.1-SNAPSHOT` provided, versioni 1.8.8, 1.9.4, 1.10.2, 1.11.2, 1.12.2) e `src/main/java/it/technoade/technoblocks/nms/<rev>/AdapterImpl.java`
- Modify: `pom.xml` (moduli), `dist/pom.xml` (dipendenze)

**Interfaces:**
- Consumes: `LegacyAdapter(Plugin)` (Task 2)
- Produces: `public final class AdapterImpl extends LegacyAdapter` con costruttore `(Plugin)`.

Implementazione (identica nei 5 moduli salvo package `net.minecraft.server.<rev>` / `org.bukkit.craftbukkit.<rev>`):
- `sendCrack`: `((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(new PacketPlayOutBlockBreakAnimation(sourceId, new BlockPosition(x, y, z), stage))`.
- `tag`: `CraftItemStack.asNMSCopy`, `NBTTagCompound` (`hasTag/getTag/setTag`), `setString("technoblocks", value)`, `CraftItemStack.asBukkitCopy`. `readTag`: `null` se item null/AIR o chiave assente (`hasKey`).
- `getItemInHand` su 1.9+: override con `getInventory().getItemInMainHand()`.

- [ ] **Step 1:** Creare i 5 moduli, aggiungerli al parent e a `dist`.
- [ ] **Step 2:** `JAVA_HOME=... ./mvnw -B -q -pl dist -am package` → BUILD OK (se un nome NMS non compila, verificarlo con `javap -cp <jar in ~/.m2> <classe>` e adeguare).
- [ ] **Step 3:** `unzip -l dist/target/TechnoBlocks-3.0.0.jar | grep AdapterImpl` → 5 righe.
- [ ] **Step 4: Commit** `feat: NMS adapters 1.8-1.12`

---

### Task 5: Moduli NMS 1.13-1.19.3

**Files:**
- Create: `nms/v1_13_R2` (1.13.2), `nms/v1_14_R1` (1.14.4), `nms/v1_15_R1` (1.15.2), `nms/v1_16_R3` (1.16.5): release 8; `nms/v1_17_R1` (1.17.1): release 16; `nms/v1_18_R2` (1.18.2), `nms/v1_19_R1` (1.19.2), `nms/v1_19_R2` (1.19.3): release 17. Tutti dipendono da `common-flat`.
- Modify: `pom.xml`, `dist/pom.xml`

**Interfaces:**
- Consumes: `FlatAdapter(Plugin)` (Task 3)
- Produces: `AdapterImpl extends FlatAdapter`, costruttore `(Plugin)`.

Implementazione:
- 1.13-1.16: come Task 4 (`net.minecraft.server.<rev>.PacketPlayOutBlockBreakAnimation`, `playerConnection.sendPacket`).
- 1.17-1.19.3 (nomi Spigot-mapped, il jar CodeMC è quello di runtime): `net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation`, `net.minecraft.core.BlockPosition`; connessione: 1.17.1 `getHandle().b.sendPacket(p)`, 1.18.2/1.19.x `getHandle().b.a(p)`. Verificare con `javap` se non compila.
- `v1_13_R2` fa override di `tag/readTag` via NBT NMS (PDC non esiste in 1.13), come Task 4 con i nomi 1.13.

- [ ] **Step 1:** Creare gli 8 moduli, aggiungerli a parent e `dist`.
- [ ] **Step 2:** Build `-pl dist -am package` → BUILD OK.
- [ ] **Step 3:** `unzip -l dist/target/TechnoBlocks-3.0.0.jar | grep -c AdapterImpl` → 13.
- [ ] **Step 4: Commit** `feat: NMS adapters 1.13-1.19.3`

---

### Task 6: Modulo `modern` (1.19.4+ / 26.x)

**Files:**
- Create: `modern/pom.xml` (dipende da `common-flat`, `spigot-api:1.19.4-R0.1-SNAPSHOT` provided, release 17), `modern/src/main/java/it/technoade/technoblocks/modern/ModernAdapter.java`
- Test: `modern/src/test/java/it/technoade/technoblocks/modern/ModernAdapterTest.java`

**Interfaces:**
- Produces: `public final class ModernAdapter extends FlatAdapter` costruttore `(Plugin)`; `static float stageToProgress(int stage)`.

- `sendCrack`: `viewer.sendBlockDamage(location, stageToProgress(stage), sourceId)`.
- `stageToProgress`: `stage < 0 → 0f`; altrimenti `Math.min(1f, (stage + 0.5f) / 9f)` (CraftBukkit fa `(int)(9 * progress)`, 0 = reset).

- [ ] **Step 1: Test che fallisce**
```java
class ModernAdapterTest {
    @Test void resetIsZero() { assertEquals(0f, ModernAdapter.stageToProgress(-1)); }
    @Test void roundTripsThroughCraftBukkitFormula() {
        for (int s = 0; s <= 9; s++) assertEquals(s, (int) (9 * ModernAdapter.stageToProgress(s)), "stage " + s);
        assertTrue(ModernAdapter.stageToProgress(0) > 0f);
    }
}
```
- [ ] **Step 2:** Aggiungere a parent e `dist`; `./mvnw -B -q -pl modern -am test` → FAIL.
- [ ] **Step 3:** Implementare.
- [ ] **Step 4:** PASS; build `dist` OK.
- [ ] **Step 5: Commit** `feat: modern adapter using sendBlockDamage with source id`

---

### Task 7: Configurazione (settings, messaggi, updater)

**Files:**
- Create: `core/src/main/java/it/technoade/technoblocks/config/{PluginSettings,BlockEntry,SelectorSettings,WorldFilter,SettingsLoader,ConfigUpdater,Messages}.java`
- Create: `core/src/main/resources/{config.yml,messages.yml}` (config esattamente come §8.1 della spec, con commenti in inglese su ogni chiave)
- Test: `core/src/test/java/it/technoade/technoblocks/config/{SettingsLoaderTest,ConfigUpdaterTest,WorldFilterTest}.java`, `core/src/test/java/it/technoade/technoblocks/FakeAdapter.java` (implementa `VersionAdapter`: `parseMaterial` via `Material.matchMaterial`, `isAllowedBlock` = `isBlock()&&isSolid()&&!hasGravity()&&!=CHEST`, il resto no-op/null)

**Interfaces:**
- Consumes: `VersionAdapter`, `MaterialSpec`
- Produces (immutabili, getter con lo stesso nome dei campi):
```java
final class WorldFilter { WorldFilter(boolean whitelist, Set<String> worlds); boolean isEnabled(String worldName); } // case-insensitive
final class SelectorSettings { int slot; MaterialSpec material; String name; List<String> lore; String guiTitle; int guiRows; } // testi già colorati
final class BlockEntry { String key; int guiSlot; MaterialSpec material; String name; List<String> lore; int amount; long durationMillis; String permission /*null = libero*/; }
// Nessun ItemStack nei settings: ItemMeta richiede un server (assente nei test). Gli item li costruisce PlayerItemsService (Task 9).
final class PluginSettings { WorldFilter worlds; SelectorSettings selector; int placedSlot; boolean cracksEnabled; int viewDistance; LinkedHashMap<String, BlockEntry> blocks; BlockEntry defaultBlock() /*primo o null*/; }
final class SettingsLoader { static PluginSettings load(ConfigurationSection root, VersionAdapter adapter, Logger log); }
final class ConfigUpdater {
    /** aggiunge le chiavi mancanti di defaults esclusa la sezione "blocks"; @return true se ha modificato cfg */
    static boolean merge(ConfigurationSection cfg, ConfigurationSection defaults);
    static boolean isOldFormat(ConfigurationSection cfg); // config-version < 3 o assente con chiave "itemselector"
}
final class Messages { static Messages load(ConfigurationSection yml); String get(String key, String... placeholderPairs); List<String> getList(String key); }
```
Regole del loader (ognuna con warning che nomina chiave e valore):
- `duration` assente → `default-duration`; `<= 0` → `default-duration`; `default-duration <= 0` → 5.
- `gui.rows` fuori 1..6 → 3. `gui-slot` fuori `0..rows*9-1` o già usato → blocco saltato.
- `selector.slot`/`placed-blocks.slot` fuori 0..8 → 8 / 4; se uguali → `placed-blocks.slot` diventa il primo slot libero partendo da 4.
- materiale `null` → saltato; `!isAllowedBlock` → saltato ("not a simple solid block").
- `amount` fuori 1..64 → 1. Colori `&` tradotti in `§` su nome, lore, titolo GUI.
- `view-distance` < 1 → 32.

Chiavi `messages.yml` (default, inglese): `block-selected: "&bBlock selected!"`, `no-permission: "&cYou don't have permission to use this block."`, `command-no-permission: "&cYou don't have permission."`, `reload-done: "&bTechnoBlocks reloaded."`, `clear-done: "&bRemoved %count% temporary blocks."`, `usage: "&7Usage: /%label% [reload|clear]"`, `info:` lista di 6 righe con lo stesso contenuto dell'attuale `Commands.java` (versione da `%version%`, link Discord `https://discord.gg/RPyF7Hjvc9`).

- [ ] **Step 1: Test che falliscono** (YAML costruito con `YamlConfiguration.loadConfiguration(new StringReader(...))`)
```java
class SettingsLoaderTest {
    @Test void perBlockDurationAndDefault()      // obsidian duration 10 → 10_000; stone senza duration con default 5 → 5_000
    @Test void invalidDurationFallsBack()       // duration: 0 e -3 → default
    @Test void slotOutOfRowsSkipped()           // rows 3, gui-slot 27 → blocco assente, warning
    @Test void duplicateSlotSkipsSecond()       // due blocchi con gui-slot 10 → resta il primo in ordine
    @Test void unknownAndDisallowedMaterialSkipped() // "NOT_REAL" e "SAND" → assenti
    @Test void emptyBlocksGivesNullDefault()    // blocks: {} → defaultBlock() == null, nessuna eccezione
    @Test void selectorAndPlacedSlotCollision() // entrambi 4 → placedSlot != 4
    @Test void colorsTranslated()               // "&4Obsidian" → "§4Obsidian"
    @Test void firstBlockIsDefault()            // ordine di config preservato
}
class ConfigUpdaterTest {
    @Test void addsMissingKeys()                // cfg senza "cracks" → merge true, cracks.view-distance == 32
    @Test void doesNotRestoreDeletedExampleBlocks() // cfg con blocks: {mine:...} → dopo merge nessun "blocks.obsidian"
    @Test void keepsUserValues()                // view-distance 10 resta 10
    @Test void noChangeReturnsFalse()
    @Test void detectsOldFormat()               // config-version: 1 + itemselector → true; config-version 3 → false
}
class WorldFilterTest {
    @Test void whitelist() @Test void blacklist() @Test void caseInsensitive()
}
```
(Il corpo di ogni test asserisce esattamente quanto scritto nel commento.)
- [ ] **Step 2:** `./mvnw -B -q -pl core -am test` → FAIL.
- [ ] **Step 3:** Implementare le classi e le risorse.
- [ ] **Step 4:** PASS.
- [ ] **Step 5: Commit** `feat: new configuration model, loader and updater`

---

### Task 8: Blocchi temporanei (registro, crepe, persistenza)

**Files:**
- Create: `core/src/main/java/it/technoade/technoblocks/block/{BlockPos,CrackMath,TemporaryBlock,TemporaryBlockRegistry,TemporaryBlockManager,ActiveBlocksStore}.java`
- Test: `core/src/test/java/it/technoade/technoblocks/block/{CrackMathTest,TemporaryBlockRegistryTest,ActiveBlocksStoreTest}.java`

**Interfaces:**
- Consumes: `VersionAdapter`, `MaterialSpec`, `PluginSettings`
- Produces:
```java
final class BlockPos { BlockPos(String world, int x, int y, int z); getters; equals/hashCode; static BlockPos of(Block b); String serialize() /* "world;x;y;z" */; static BlockPos parse(String s) /* null se invalido */; }
final class CrackMath {
    static final long REFRESH_MILLIS = 5000L;
    static int stage(long elapsedMillis, long durationMillis); // floor(elapsed/duration*10) limitato 0..9; duration<=0 → 9
    static boolean shouldSend(int stage, int lastSentStage, long now, long lastSentAt); // stage cambiato o now-lastSentAt >= REFRESH_MILLIS
}
final class TemporaryBlock { BlockPos pos; MaterialSpec placed; String replacedState; long placedAt; long expiresAt; int sourceId; int lastSentStage = -1; long lastSentAt; }
final class TemporaryBlockRegistry {
    TemporaryBlock add(BlockPos pos, MaterialSpec placed, String replacedState, long now, long durationMillis); // sostituisce eventuale voce stessa pos; assegna sourceId
    TemporaryBlock get(BlockPos pos); TemporaryBlock remove(BlockPos pos);
    List<TemporaryBlock> pollExpired(long now); // rimuove e ritorna, ordinati per scadenza
    List<TemporaryBlock> drainAll(); Collection<TemporaryBlock> all(); int size();
}
final class ActiveBlocksStore {
    ActiveBlocksStore(File file);
    static String encode(TemporaryBlock b);   // "world;x;y;z|<placed toString>|<replacedState>"
    List<String> readLines();                 // sync
    void writeLinesAsync(Plugin p, List<String> lines); void writeLinesSync(List<String> lines);
}
final class TemporaryBlockManager {
    TemporaryBlockManager(Plugin plugin, VersionAdapter adapter, ActiveBlocksStore store);
    void start(PluginSettings s); void applySettings(PluginSettings s);
    void register(Block placed, BlockState replaced, MaterialSpec spec, long durationMillis);
    boolean isTemporary(Block b);
    void removeNow(Block b);                  // reset crepe + ripristino + rimozione
    void forget(Block b);                     // reset crepe + rimozione senza ripristino (rotto in creative)
    int restoreAll();                         // ritorna quanti; usato da reload/clear/disable
    void resendTo(Player p);                  // invia lo stage corrente di tutti i blocchi nel raggio
    void recoverFromCrash();                  // §9 "Crash"
    void stop();
}
```
Comportamento del manager:
- Un task sync ogni 2 tick: `pollExpired(now)` → per ognuno: crepe -1 ai viewer, poi `restoreState` **solo se** `adapter.matches(block, placed)`; poi per tutti i rimanenti, se `cracksEnabled`, calcolo `stage` e `shouldSend` → invio a ogni giocatore nello stesso mondo con `distanceSquared <= viewDistance²`.
- Chunk non caricato alla scadenza: si ripristina comunque (il `getBlockAt` carica il chunk). **[scelta del piano]**
- Persistenza: ogni modifica del registro segna `dirty`; un task sync ogni 40 tick, se `dirty`, crea lo snapshot di `encode` e chiama `writeLinesAsync`. `stop()`/`restoreAll()` su disable scrive sync la lista vuota.
- `recoverFromCrash()` (chiamato 1 tick dopo l'enable): per ogni riga, se il mondo non è caricato la riga viene conservata; altrimenti se `matches` → `restoreState`; poi riscrive solo le righe conservate.
- sourceId: contatore che parte da `Integer.MAX_VALUE` e decresce (nel registry).

- [ ] **Step 1: Test che falliscono**
```java
class CrackMathTest {
    @Test void stages() { assertEquals(0, CrackMath.stage(0, 10_000)); assertEquals(0, CrackMath.stage(999, 10_000));
        assertEquals(1, CrackMath.stage(1_000, 10_000)); assertEquals(9, CrackMath.stage(9_999, 10_000)); assertEquals(9, CrackMath.stage(50_000, 10_000)); }
    @Test void zeroDuration() { assertEquals(9, CrackMath.stage(0, 0)); }
    @Test void sendOnChangeOrRefresh() { assertTrue(CrackMath.shouldSend(3, 2, 1000, 900)); assertFalse(CrackMath.shouldSend(3, 3, 5899, 900));
        assertTrue(CrackMath.shouldSend(3, 3, 5900, 900)); }
}
class TemporaryBlockRegistryTest {
    @Test void uniqueSourceIds()      // due add su pos adiacenti stesso now → sourceId diversi, entrambi > 1_000_000_000
    @Test void samePositionReplaces() // add A, add B stessa pos → size 1, get == B, pollExpired dopo scadenza di A ma non di B → vuoto
    @Test void pollExpiredOrderedAndRemoves() // durate 3s,1s,2s → poll a now+5s ritorna 1s,2s,3s e size 0
    @Test void drainAllEmpties()
}
class ActiveBlocksStoreTest {
    @Test void encodeFormat()         // "hub;1;64;-2|OBSIDIAN:0|minecraft:air"
    @Test void writeThenReadRoundTrip() // writeLinesSync + readLines su @TempDir
    @Test void unknownWorldKept()     // testa la funzione pura usata da recoverFromCrash:
                                      // static List<String> linesToKeep(List<String> lines, Predicate<String> worldLoaded)
                                      // → mantiene solo le righe con mondo non caricato e scarta le righe malformate
}
```
(`linesToKeep` va in `ActiveBlocksStore` come `static`; aggiungerla all'interfaccia sopra.)
- [ ] **Step 2:** `./mvnw -B -q -pl core -am test` → FAIL.
- [ ] **Step 3:** Implementare le classi pure, poi `TemporaryBlockManager`.
- [ ] **Step 4:** PASS.
- [ ] **Step 5: Commit** `feat: temporary block registry, crack progression and crash recovery`

---

### Task 9: Giocatori, GUI, dati e listener

**Files:**
- Create: `core/src/main/java/it/technoade/technoblocks/data/DataStore.java`
- Create: `core/src/main/java/it/technoade/technoblocks/player/{PlayerItemsService,ItemProtection}.java`
- Create: `core/src/main/java/it/technoade/technoblocks/gui/{SelectorGui,SelectorHolder}.java`
- Create: `core/src/main/java/it/technoade/technoblocks/listener/{PlacementListener,ProtectionListener,PlayerListener,InventoryListener}.java`
- Test: `core/src/test/java/it/technoade/technoblocks/player/ItemProtectionTest.java`, `core/src/test/java/it/technoade/technoblocks/data/DataStoreTest.java`

**Interfaces:**
- Consumes: Task 7 (`PluginSettings`, `BlockEntry`, `Messages`), Task 8 (`TemporaryBlockManager`), `VersionAdapter`
- Produces:
```java
final class DataStore { // data.yml: total-placed, players.<uuid>: int, selected.<uuid>: key
    DataStore(File file); void load(); int total(); int personal(UUID u); void increment(UUID u);
    String selected(UUID u); void select(UUID u, String key);
    void saveAsync(Plugin p); void saveSync(); // snapshot su main thread
}
final class ItemProtection {
    static final String SELECTOR = "selector"; static final String BLOCK_PREFIX = "block:";
    static boolean isPluginTag(String tag);                    // SELECTOR o inizia con BLOCK_PREFIX
    static String blockKey(String tag);                        // null se non è un blocco
    static boolean shouldCancelClick(String currentTag, String cursorTag, String hotbarSlotTag); // true se uno qualsiasi è tag del plugin
}
final class PlayerItemsService {
    PlayerItemsService(VersionAdapter adapter, DataStore data);
    void apply(Player p, PluginSettings s);   // mondo abilitato → give, altrimenti strip
    void give(Player p, PluginSettings s); void strip(Player p);
    BlockEntry selectedEntry(Player p, PluginSettings s); // selected se esiste e permesso ok, altrimenti default
    ItemStack taggedBlockItem(BlockEntry e); ItemStack taggedSelector(PluginSettings s); // adapter.createItem + nome/lore via ItemMeta + adapter.tag
}
final class SelectorHolder implements InventoryHolder { }  // marker
final class SelectorGui { static Inventory open(Player p, PluginSettings s, PlayerItemsService items); }
```
Comportamento dei listener (tutti `ignoreCancelled = true` salvo dove indicato):
- `PlacementListener` (`BlockPlaceEvent`, `HIGH`): tag dell'item in mano (`e.getItemInHand()`). `selector` → cancel. `block:<k>`: se mondo non abilitato o `k` non nel catalogo → cancel; altrimenti `manager.register(e.getBlockPlaced(), e.getBlockReplacedState(), entry.material, entry.durationMillis)`, `data.increment`, e se non creative un task 1 tick dopo rimette `taggedBlockItem(selectedEntry)` in `placedSlot`.
- `ProtectionListener`: `BlockBreakEvent` (`HIGH`) su blocco temporaneo → non-creative: cancel; creative: `manager.forget(block)`. `EntityExplodeEvent` e `BlockExplodeEvent`: togliere i temporanei da `blockList()` e chiamare `removeNow`. `BlockPistonExtendEvent`/`BlockPistonRetractEvent`: cancel se un blocco di `getBlocks()` è temporaneo.
- `PlayerListener`: join → `apply` + `resendTo` 1 tick dopo; respawn, `PlayerChangedWorldEvent`, `PlayerTeleportEvent` → idem 1 tick dopo; quit → nulla (selezione persistente). `PlayerInteractEvent` right-click con `selector` → cancel + `SelectorGui.open`. `PlayerDropItemEvent` con tag plugin → cancel.
- `InventoryListener`: `InventoryClickEvent`: se top inventory holder è `SelectorHolder`: cancel sempre; se click nella GUI su un blocco: permesso (`no-permission`) → `data.select`, metti item in `placedSlot`, `block-selected`, chiudi. Altrimenti, se giocatore non in creative e `shouldCancelClick(tag(current), tag(cursor), tag(item nello slot hotbarButton se >=0))` → cancel. `InventoryDragEvent` con item del plugin → cancel. Swap mano secondaria (1.9+, assente su 1.8): registrare a runtime via `Class.forName("org.bukkit.event.player.PlayerSwapHandItemsEvent")` con `PluginManager.registerEvent(..., EventExecutor, ...)`; cancel (cast a `Cancellable`) se l'item in mano principale ha tag del plugin; `ClassNotFoundException` → ignorare.

- [ ] **Step 1: Test che falliscono**
```java
class ItemProtectionTest {
    @Test void tags() { assertTrue(ItemProtection.isPluginTag("selector")); assertTrue(ItemProtection.isPluginTag("block:stone"));
        assertFalse(ItemProtection.isPluginTag(null)); assertFalse(ItemProtection.isPluginTag("other")); assertEquals("stone", ItemProtection.blockKey("block:stone")); assertNull(ItemProtection.blockKey("selector")); }
    @Test void hotbarNumberKeySwapIsCancelled() { assertTrue(ItemProtection.shouldCancelClick(null, null, "block:stone")); }
    @Test void cursorOrCurrent() { assertTrue(ItemProtection.shouldCancelClick("selector", null, null)); assertTrue(ItemProtection.shouldCancelClick(null, "block:x", null)); }
    @Test void foreignItemsFree() { assertFalse(ItemProtection.shouldCancelClick(null, "other", null)); }
}
class DataStoreTest {
    @Test void legacyDataYmlStillReadable()   // file con "total-placed: 7\nplayers:\n  <uuid>: 3\n" → total 7, personal 3
    @Test void incrementAndSelectionPersist() // increment + select, saveSync, nuovo DataStore.load → valori uguali
}
```
- [ ] **Step 2:** `./mvnw -B -q -pl core -am test` → FAIL.
- [ ] **Step 3:** Implementare `DataStore`, `ItemProtection`, `PlayerItemsService`, GUI, listener.
- [ ] **Step 4:** PASS.
- [ ] **Step 5: Commit** `feat: player items, selector GUI, data store and listeners`

---

### Task 10: Plugin main, caricamento adapter, comando, PlaceholderAPI

**Files:**
- Create: `core/src/main/java/it/technoade/technoblocks/TechnoBlocksPlugin.java`, `adapter/AdapterLoader.java`, `command/TechnoBlocksCommand.java`, `hook/PlaceholderHook.java`
- Create: `core/src/main/resources/plugin.yml` (filtering Maven attivo solo su questo file per `${project.version}`)

**Interfaces:**
- Consumes: tutto quanto sopra.
- Produces: `final class AdapterLoader { static VersionAdapter load(Plugin p) /* null se non supportato */; }`

Comportamento:
- `AdapterLoader.load`: `hasSourceIdDamageApi` = esiste `Player.class.getMethod("sendBlockDamage", Location.class, float.class, int.class)`; package = `Bukkit.getServer().getClass().getPackage().getName()`; `AdapterResolver.resolve` → `Class.forName(name).getConstructor(Plugin.class).newInstance(p)`. Qualsiasi `Throwable` → log severe con `Bukkit.getVersion()` + package e ritorna `null`.
- `onEnable`: adapter `null` → log e `setEnabled(false)`. Altrimenti: config (`saveDefaultConfig`; se `ConfigUpdater.isOldFormat` → rinomina `config-old.yml` + warning; poi `merge` con il default nel jar e `save` solo se `true`; warning "comments may be lost" se il server è < 1.18.1: dedurlo dall'assenza di `ConfigurationSection#getComments` via reflection), `messages.yml` (stesso merge, sezione esclusa: nessuna), `SettingsLoader.load`, `DataStore.load`, manager `start`, listener, comando, `PlaceholderHook` solo se `PluginManager.getPlugin("PlaceholderAPI") != null`, `apply` ai giocatori online, `recoverFromCrash` 1 tick dopo, `DataStore.saveAsync` ogni 6000 tick.
- `onDisable`: `manager.restoreAll()`, `manager.stop()`, `data.saveSync()`.
- `reload()`: `restoreAll`, ricarica config/messaggi/settings, `manager.applySettings`, `strip`+`apply` a tutti gli online.
- Comando (anche da console): nessun argomento → righe `info`; `reload`/`clear` richiedono `technoblocks.admin` (`command-no-permission`), `clear-done` con `%count%` = valore di `restoreAll`; argomento sconosciuto → `usage`. Tab-complete `reload`, `clear` per chi ha il permesso.
- `PlaceholderHook` (identifier `technoblocks`, author `TechnoAde`, version plugin, `persist() = true`): `total`, `personal`.

- [ ] **Step 1:** Implementare le classi e `plugin.yml`.
- [ ] **Step 2:** Build standard → BUILD OK, tutti i test PASS.
- [ ] **Step 3:** `unzip -p dist/target/TechnoBlocks-3.0.0.jar plugin.yml` → contiene `version: '3.0.0'`, `api-version: 1.13`, `main: it.technoade.technoblocks.TechnoBlocksPlugin`, alias `tb`.
- [ ] **Step 4:** `javap -v -cp dist/target/TechnoBlocks-3.0.0.jar it.technoade.technoblocks.TechnoBlocksPlugin | grep major` → `major version: 52` (Java 8).
- [ ] **Step 5: Commit** `feat: plugin bootstrap, adapter loading, commands and placeholders`

---

### Task 11: README e verifica sui server

**Files:**
- Modify: `README.md` (riscritto: versioni supportate 1.8.8-1.21.x/26.x con la lista delle patch supportate (§3 della spec), feature, config d'esempio, comandi/permessi, placeholder, "How to build": JDK 21+ e `./mvnw package`, output `dist/target/TechnoBlocks-3.0.0.jar`; togliere la frase sul comando rinominabile)

- [ ] **Step 1:** Scrivere il README.
- [ ] **Step 2: Test manuale** con il jar su server Paper (Spigot su 1.8.8 e 1.21.x): **1.8.8, 1.12.2, 1.13.2, 1.16.5, 1.17.1, 1.19.3, 1.19.4, 1.20.6, 1.21.x, 26.x**. Checklist per ognuno, da annotare in una tabella nel messaggio di consegna:
  - si abilita senza errori e logga l'adapter scelto;
  - selettore + blocco dati in un mondo abilitato, rimossi passando a uno non abilitato;
  - GUI: selezione, permesso negato, item non spostabili (tasto numerico, shift, drag, F, drop);
  - tre blocchi piazzati di fila: crepe indipendenti visibili da un secondo giocatore, spariscono a fine durata (testare anche una durata di 30 s per il refresh);
  - survival non rompe, creative rompe senza drop; pistone e TNT non li spostano/droppano;
  - `/tb reload` e `/tb clear` ripuliscono; kill del processo con blocchi attivi → al riavvio ripuliti.
- [ ] **Step 3: Commit** `docs: README for 3.0`
