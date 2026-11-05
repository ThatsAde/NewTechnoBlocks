# TechnoBlocks 3.0: Rework completo con blocchi che si rompono nel tempo

Data: 2026-11-05
Repo: `ThatsAde/NewTechnoBlocks`, branch `rework`

Legenda: le voci marcate **[scelta del designer]** sono decisioni prese durante la stesura
e non discusse esplicitamente con l'autore; tutto il resto è stato concordato.

---

## 1. Intento

TechnoBlocks permette ai giocatori di piazzare blocchi temporanei nella hub, scelti da una GUI
aperta con un item "selettore". Oggi il blocco resta intatto per N secondi e poi sparisce di colpo.

Con la 3.0:

1. **Il blocco si rompe visibilmente nel tempo**: le crepe (block break animation, stadi 0-9)
   crescono dal piazzamento fino alla scadenza, poi il blocco sparisce. Le crepe sono inviate
   tramite pacchetti / API, il blocco non viene danneggiato davvero.
2. **Supporto reale da 1.8.8 a 1.21.x e 26.x** in un unico jar, tramite wrapper per versione
   scritti da noi (nessuna libreria esterna di pacchetti tipo ProtocolLib/PacketEvents).
3. **Rework completo di config e codice.** Il comportamento lato giocatore di GUI e selettore
   resta quello attuale.

Perché il rework: oggi il "supporto 1.8" è solo nominale (il plugin è compilato in Java 21 contro
Paper 1.21.8 con `api-version: '1.21.8'`, quindi non si carica su 1.8), il reload non aggiorna
selettore e messaggi (enum statici che leggono il config al class-load), gli item vengono riconosciuti
dal display name, `data.yml` viene scritto in modo sincrono a ogni piazzamento, e il comando
`reload` non ha permesso.

## 2. Obiettivi

- Crepe progressive su ogni blocco temporaneo, visibili a tutti i giocatori vicini.
- Durata configurabile **per singolo blocco** della GUI.
- Lista di **mondi** in cui il plugin è attivo.
- Un solo jar che funziona su Spigot e Paper da 1.8.8 a 1.21.x e 26.x.
- Wrapper NMS nostri per le versioni che non hanno un'API adeguata; API Bukkit per le versioni moderne
  (approccio ibrido).
- Codice riscritto pulito, reload che funziona davvero.

## 3. Non-obiettivi

- Cambiare l'esperienza della GUI/selettore (layout, flusso): si mantiene.
- Dipendere da ProtocolLib, PacketEvents, XSeries o simili.
- **[scelta del designer]** Supporto Folia.
- **[scelta del designer]** Supporto a versioni intermedie non-ultime di ogni minor
  (es. 1.9.0-1.9.2, 1.16.1-1.16.4): si supporta l'ultima patch di ogni minor da 1.8 a 1.18,
  1.19.2 e 1.19.3 (revisioni NMS distinte), e tutte le 1.19.4+.
- **[scelta del designer]** Migrazione automatica del vecchio `config.yml` (vedi §8.6).

## 4. Anti-obiettivi (sarebbe un fallimento anche se "funziona")

- Il jar non si carica su una delle versioni dichiarate.
- Blocchi temporanei che restano nella hub per sempre (crash, reload, chunk scaricati).
- Crepe che "saltano" tra blocchi diversi o spariscono a metà durata.
- Lag: niente task per-blocco con I/O sincrono sul main thread.

---

## 5. Comportamento lato giocatore

Invariato rispetto a oggi, tranne le crepe e i mondi:

1. Entrando (o rientrando/respawnando) in un **mondo abilitato**, il giocatore riceve il selettore
   nello slot configurato e il blocco selezionato (o il primo di default) nello slot dei blocchi.
2. Click destro col selettore → si apre la GUI; click su un blocco → se ha il permesso, quel blocco
   diventa il suo blocco attivo e viene messo nello slot; altrimenti messaggio "no perms".
3. Piazzando il blocco (non in creative) lo slot viene riempito di nuovo: lo stack non finisce mai.
4. Il blocco piazzato mostra le crepe che crescono; a fine `duration` sparisce.
5. Selettore e blocchi del plugin non si possono droppare, né spostare fuori dagli slot
   **[scelta del designer: blocco anche di click/drag nell'inventario sugli item del plugin]**.
6. Uscendo da un mondo abilitato verso uno non abilitato, selettore e blocchi del plugin
   vengono rimossi dall'inventario **[scelta del designer]**.

## 6. Le crepe

### 6.1 Progressione
- `stage = floor(elapsed / duration * 10)`, limitato a 0..9.
- Si invia un aggiornamento quando lo stage cambia.
- **Refresh periodico**: il client cancella da solo un'animazione che non riceve aggiornamenti per
  ~400 tick (20 s). Lo stage corrente viene reinviato ogni 5 s **[scelta del designer]** anche se
  non è cambiato, così le durate lunghe funzionano.
- Alla rimozione si invia lo "stage di reset" (-1 / progress 0) per pulire il client, poi il blocco
  viene ripristinato.

### 6.2 Chi le vede
- Tutti i giocatori nello stesso mondo entro `cracks.view-distance` blocchi (default 32)
  **[scelta del designer]**. Il client vanilla non disegna crepe oltre 32 blocchi (su 1.8 le
  cancella proprio), quindi valori più alti non servono; il refresh periodico le fa riapparire
  quando il giocatore si riavvicina.
- Chi entra nel raggio a metà rottura vede subito lo stage corrente (gestito naturalmente dal
  refresh periodico e da un invio immediato al join/teleport/cambio mondo).

### 6.3 ID dell'animazione
- Il client indicizza le animazioni per "source/breaker entity id": un nuovo pacchetto con lo stesso id
  **sposta** l'animazione. Quindi **ogni blocco temporaneo ha un id univoco**, preso da un contatore
  che parte da un valore alto (es. `Integer.MAX_VALUE` decrescente) per non collidere con entity id
  reali. **[scelta del designer]**

### 6.4 Implementazione per versione (approccio ibrido)
- **1.19.4+ (inclusa 26.x):** API Bukkit `Player#sendBlockDamage(Location, float progress, int sourceId)`.
  Nessun NMS → le versioni future funzionano senza nuovi moduli.
  Attenzione alla conversione: `progress = 0` significa reset, e CraftBukkit calcola lo stage come
  `(int)(9 * progress)`. Lo stage va convertito in un progress che ricada al centro dell'intervallo
  giusto (es. `(stage + 0.5f) / 9f`, limitato a 1) per evitare stage sbagliati per arrotondamento.
  > Nota tecnica: l'overload senza `sourceId` (1.17-1.19.3) usa l'id del giocatore stesso, quindi un
  > viewer vedrebbe le crepe su un solo blocco alla volta. Per questo 1.17-1.19.3 usano NMS.
  > Il builder deve verificare in quale versione è comparso l'overload con `sourceId` e spostare il
  > confine di conseguenza.
- **1.8.8 → 1.19.3:** un modulo NMS per revisione che invia il pacchetto block break animation
  (`PacketPlayOutBlockBreakAnimation` / `ClientboundBlockDestructionPacket`) con id, posizione, stage.

---

## 7. Architettura

### 7.1 Moduli Maven
Progetto multi-modulo, un solo jar finale shaded:

| Modulo | Compilato contro | Target bytecode |
|---|---|---|
| `api`: interfacce wrapper (`VersionAdapter` ecc.) | Spigot API 1.8.8 | Java 8 |
| `core`: tutta la logica del plugin | Spigot API 1.8.8 | Java 8 |
| `nms-v1_8_R3` (1.8.8) | Spigot 1.8.8 | Java 8 |
| `nms-v1_9_R2` (1.9.4) | Spigot 1.9.4 | Java 8 |
| `nms-v1_10_R1` (1.10.2) | Spigot 1.10.2 | Java 8 |
| `nms-v1_11_R1` (1.11.2) | Spigot 1.11.2 | Java 8 |
| `nms-v1_12_R1` (1.12.2) | Spigot 1.12.2 | Java 8 |
| `nms-v1_13_R2` (1.13.2) | Spigot 1.13.2 | Java 8 |
| `nms-v1_14_R1` (1.14.4) | Spigot 1.14.4 | Java 8 |
| `nms-v1_15_R1` (1.15.2) | Spigot 1.15.2 | Java 8 |
| `nms-v1_16_R3` (1.16.5) | Spigot 1.16.5 | Java 8 |
| `nms-v1_17_R1` (1.17.1) | Spigot 1.17.1 | Java 16 |
| `nms-v1_18_R2` (1.18.2) | Spigot 1.18.2 | Java 17 |
| `nms-v1_19_R1` (1.19.2) | Spigot 1.19.2 | Java 17 |
| `nms-v1_19_R2` (1.19.3) | Spigot 1.19.3 | Java 17 |
| `modern` (1.19.4+ e 26.x) | Spigot API 1.19.4 | Java 17 |
| `dist`: shading di tutto nel jar finale | nessuno | nessuno |

- I moduli con target > Java 8 vengono caricati **solo** sui server corrispondenti (che girano già
  su quel Java), quindi non rompono la 1.8.
- Il core non referenzia mai direttamente le classi dei moduli: le carica per nome via reflection.
- Dipendenze NMS: dal repository NMS di CodeMC (`https://repo.codemc.io/repository/nms/`) o, in
  alternativa, BuildTools in locale. Il builder sceglie e documenta il procedimento nel README.
- Build richiede JDK 21 (compila tutti i target con `--release`). **[scelta del designer]**

### 7.2 Selezione del wrapper all'avvio
Per capacità, non per stringa di versione:
1. Se esiste `Player#sendBlockDamage(Location, float, int)` → `modern`.
2. Altrimenti si legge la revisione NMS dal package CraftBukkit (`org.bukkit.craftbukkit.vX_Y_RZ`)
   e si carica il modulo corrispondente.
3. Se nessuno corrisponde → log chiaro con la versione rilevata e plugin disabilitato.

### 7.3 Interfaccia `VersionAdapter` (contenuto indicativo, il builder la affina)
- `sendCrack(Player viewer, int sourceId, Block block, int stage)`: stage -1 = reset.
- `getItemInHand(Player)`: main hand su 1.9+, `getItemInHand()` su 1.8.
- `tag(ItemStack, String value)` / `readTag(ItemStack)`: tag nascosto per riconoscere gli item del
  plugin: PersistentDataContainer da 1.14, NBT via NMS su 1.8-1.13.
- `parseItem(String spec)`: traduce il materiale del config nel materiale della versione (§8.3).
- Suoni ed eventuali altre differenze emerse durante l'implementazione.

### 7.4 Core (struttura indicativa)
- `TechnoBlocksPlugin` (main): bootstrap, nessuno stato `static` pubblico.
- `ConfigService`: carica `config.yml` / `messages.yml` in oggetti immutabili; il reload ricrea
  gli oggetti e ri-applica gli item ai giocatori online.
- `BlockCatalog`: i blocchi della GUI con durata, permesso, slot, item già costruito.
- `TemporaryBlockManager`: registro dei blocchi attivi; **un unico task ripetuto** (es. ogni 2 tick)
  che calcola gli stage, invia crepe e rimuove i blocchi scaduti (coda ordinata per scadenza).
  Niente `runTaskLater` per blocco. **[scelta del designer]**
- `SelectorGui`: GUI con `InventoryHolder` custom (non confronto sul titolo, funziona su tutte le versioni).
- `PlayerItemsService`: dà/toglie selettore e blocco in base al mondo.
- `StatsService` + hook PlaceholderAPI.
- `ActiveBlocksStore`: persistenza dei blocchi attivi per il recupero dopo crash.

---

## 8. Configurazione

### 8.1 `config.yml` (forma proposta)

```yaml
config-version: 3

# Mondi in cui il plugin è attivo
worlds:
  mode: whitelist        # whitelist | blacklist
  list:
    - hub

selector:
  slot: 8
  material: CHEST
  name: "&bBlock Selector"
  lore: []
  gui:
    title: "&bSelector"
    rows: 3

placed-blocks:
  slot: 4
  default-duration: 5    # secondi, usato se un blocco non specifica 'duration'

cracks:
  enabled: true
  view-distance: 32

blocks:
  obsidian:
    gui-slot: 10
    material: OBSIDIAN
    name: "&4Obsidian"
    lore:
      - "&7Amazing block"
    amount: 1
    duration: 10
    permission: "technoblocks.block.obsidian"
  stone:
    gui-slot: 12
    material: STONE
    name: "&cStone"
    lore:
      - "&7Amazing stone!"
    amount: 2
    duration: 5
  wool:
    gui-slot: 14
    material: WHITE_WOOL   # su 1.8-1.12 tradotto in WOOL:0
    name: "&bWool"
    amount: 3
```

- `permission` assente = blocco libero per tutti.
- `gui.rows` sostituisce `guisize`. **[scelta del designer]**
- Il primo blocco in ordine di config è il default.
- **Materiali ammessi [scelta del designer]:** solo blocchi pieni e "semplici". Al caricamento del
  config vengono rifiutati, con warning in console che nomina il blocco:
  - item che non sono blocchi;
  - blocchi soggetti a gravità (sabbia, ghiaia, concrete powder, incudini);
  - blocchi in due parti (porte, letti, piante alte);
  - blocchi che richiedono supporto (torce, tappeti, cartelli, bottoni, ecc.);
  - blocchi con inventario o tile entity (casse, shulker, fornaci, hopper, ecc.).
  Il controllo sta nel `VersionAdapter` (su 1.13+ si appoggia a `Material#isSolid`/`hasGravity`/BlockData,
  su legacy a una lista). In questo modo un blocco non può mai cadere, staccarsi o spezzarsi in due e
  restare nella hub per sempre.
- **Blocco scelto persistente [scelta del designer]:** il blocco selezionato da ogni giocatore viene
  salvato in `data.yml` (`selected.<uuid>: <chiave blocco>`) e riproposto al rientro e dopo i restart.
  Se quella chiave non esiste più o il giocatore ha perso il permesso, si torna al default.

### 8.2 `messages.yml`
Tutti i testi (inclusi quelli oggi hardcoded: info comando, reload, errori), codici colore `&`.
**[scelta del designer]**

### 8.3 Materiali cross-version
- Accettati sia nomi moderni (`WHITE_WOOL`) sia legacy con data value (`WOOL:0`).
- Su 1.13+ un nome legacy viene tradotto in moderno; su 1.8-1.12 un nome moderno viene tradotto
  in legacy tramite una tabella interna che copre almeno i blocchi colorati/varianti comuni
  (lana, vetro colorato, terracotta, legni, pietre). Materiale non riconosciuto → warning in console
  con il nome del blocco e il blocco viene saltato. **[scelta del designer: tabella nostra, niente XSeries]**

### 8.4 Aggiornamento automatico
Il plugin aggiunge al config le chiavi mancanti dopo un update, senza toccare i valori esistenti.
**[scelta del designer]** La sezione `blocks` è esclusa dal merge, così i blocchi d'esempio cancellati
dall'admin non ricompaiono. Il file viene riscritto solo se è stato aggiunto qualcosa. Sotto 1.18.1
Bukkit perde i commenti quando salva: è un compromesso accettato, e la console avvisa quando succede.

### 8.5 `plugin.yml`
- `api-version: 1.13` (ignorato su 1.8-1.12, evita la modalità legacy su 1.13+, accettato fino a 26.x).
- `softdepend: [PlaceholderAPI]`.

### 8.6 Vecchio config **[scelta del designer]**
Se all'avvio si trova un config con `config-version: 1`, viene rinominato in `config-old.yml`,
si genera il nuovo e si stampa un avviso in console.

---

## 9. Ciclo di vita del blocco temporaneo

1. `BlockPlaceEvent` (priorità `HIGH`, `ignoreCancelled = true`) con item del plugin in mano, in un mondo
   abilitato → si registra: posizione, stato **sostituito** (`getBlockReplacedState`, così acqua/erba
   alta vengono ripristinate invece di lasciare un buco), scadenza, sourceId. **[scelta del designer]**
2. Il task aggiorna le crepe come in §6.
3. Alla scadenza: reset crepe → ripristino dello stato sostituito → rimozione dal registro.
4. Il selettore non si può piazzare (piazzamento annullato, l'item torna nel suo slot).
5. I blocchi non del plugin non vengono toccati: la protezione della hub non è compito di TechnoBlocks.

Casi limite **[scelte del designer]**:
- **Rottura manuale (concordato):** in qualsiasi modalità diversa da creative (survival,
  adventure), TechnoBlocks annulla il `BlockBreakEvent` su qualsiasi blocco temporaneo, sia proprio
  sia altrui. In creative la rottura è permessa: niente drop, reset delle crepe, rimozione dal
  registro. Lo stesso vale se un altro plugin rimuove il blocco.
- Siccome in survival la rottura viene annullata, l'animazione di scavo del client può mescolarsi
  per un attimo con le nostre crepe. È un difetto solo visivo e accettato: il refresh periodico
  rimette lo stage corretto. **[scelta del designer]**
- Esplosioni: i blocchi temporanei coinvolti vengono rimossi dal registro, senza drop.
- Pistoni: estensione/retrazione che coinvolge un blocco temporaneo viene annullata.
- Chunk scaricato: il blocco resta nel registro; alla scadenza viene rimosso comunque
  (caricando il chunk solo se necessario, oppure alla prossima `ChunkLoadEvent`, a scelta del builder).
- Spegnimento / `/technoblocks reload` / disable: tutti i blocchi temporanei vengono ripristinati subito.
- **Crash:** i blocchi attivi sono salvati in `active-blocks.yml` (scrittura asincrona, con debounce).
  All'avvio ogni voce viene ripristinata **solo se** il blocco in quella posizione è ancora il materiale
  piazzato; poi il file viene svuotato.

---

## 10. Comandi e permessi

| Comando | Permesso | Cosa fa |
|---|---|---|
| `/technoblocks` | nessuno | Info plugin (testo da `messages.yml`) |
| `/technoblocks reload` | `technoblocks.admin` | Ricarica config/messaggi, ripristina i blocchi attivi, ri-dà gli item |
| `/technoblocks clear` | `technoblocks.admin` | Rimuove subito tutti i blocchi temporanei **[scelta del designer]** |

Il comando funziona anche da console (oggi solo da giocatore). Alias in `plugin.yml`: `tb`.
**[scelta del designer]** La voce del README "il comando si può cambiare dal config" non è mai stata
implementata e viene tolta dal README.

## 11. Statistiche e PlaceholderAPI
- Placeholder mantenuti: `%technoblocks_total%`, `%technoblocks_personal%`.
- Contatori in memoria; `data.yml` (stesso formato di oggi, compatibile) salvato in asincrono ogni
  5 minuti e al disable. **[scelta del designer]**
- Conteggio una volta per piazzamento riuscito (via evento del manager, non un secondo listener con
  hack anti-doppio-conteggio).

## 12. Versione e metadati **[scelte del designer]**
- Versione `3.0.0`, autore `TechnoAde`, pacchetto base `it.technoade.technoblocks`.
- README aggiornato: versioni supportate, nuovo config, comandi/permessi, come compilare (JDK 21 + repo NMS).

## 13. Verifica
- Test unitari (JUnit 5) sulla logica pura: calcolo stage, coda scadenze, parsing config, traduzione
  materiali, selezione del wrapper.
- Test manuale su server reali, almeno: **1.8.8, 1.12.2, 1.13.2, 1.16.5, 1.17.1, 1.19.3, 1.19.4,
  1.20.6, 1.21.x, 26.x** (Paper; Spigot almeno su 1.8.8 e 1.21.x). Per ognuno: il plugin si carica,
  GUI/selettore funzionano, le crepe crescono su più blocchi contemporaneamente visibili a un secondo
  giocatore, i blocchi spariscono, reload e restart puliscono tutto.

## 14. Lasciato al builder
- Nomi esatti di classi e package interni, forma finale dell'interfaccia `VersionAdapter`.
- Repo NMS vs BuildTools.
- Strategia per i chunk scaricati alla scadenza (§9).
- Intervallo del task (2 tick è un'indicazione).
- Contenuto esatto della tabella materiali legacy (§8.3), purché copra i casi comuni.
