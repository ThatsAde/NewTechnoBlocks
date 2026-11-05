package it.technoade.technoblocks.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataStoreTest {

    @TempDir
    File dir;

    @Test
    void legacyDataYmlStillReadable() throws IOException {
        UUID id = UUID.randomUUID();
        File file = new File(dir, "data.yml");
        FileOutputStream out = new FileOutputStream(file);
        out.write(("total-placed: 7\nplayers:\n  " + id + ": 3\n").getBytes(StandardCharsets.UTF_8));
        out.close();
        DataStore store = new DataStore(file);
        store.load();
        assertEquals(7, store.total());
        assertEquals(3, store.personal(id));
        assertNull(store.selected(id));
    }

    @Test
    void incrementAndSelectionPersist() {
        UUID id = UUID.randomUUID();
        File file = new File(dir, "data.yml");
        DataStore store = new DataStore(file);
        store.load();
        store.increment(id);
        store.increment(id);
        store.select(id, "obsidian");
        store.saveSync();

        DataStore reloaded = new DataStore(file);
        reloaded.load();
        assertEquals(2, reloaded.total());
        assertEquals(2, reloaded.personal(id));
        assertEquals("obsidian", reloaded.selected(id));
    }
}
