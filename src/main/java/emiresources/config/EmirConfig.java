package emiresources.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import emiresources.EmiResources;
import net.xiaoyu233.fml.FishModLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Reads {@code config/emi-resources.json}, writing the defaults there on first run.
 * <p>
 * Values are read through the accessors rather than the fields so a malformed or missing file
 * degrades to the defaults instead of breaking the recipe screen.
 */
public final class EmirConfig {
    private static final String FILE_NAME = "emi-resources.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Values values;

    private EmirConfig() {
    }

    /** The serialised shape of the config file. */
    private static final class Values {
        /** Items shown per column on the mob and chest pages. */
        int itemsPerColumn = 4;
        /** Seconds an item is shown before the display cycles to the next one. */
        float cycleTimeSeconds = 2.0F;
        /** Extra Y levels drawn on either side of an ore's range, for context. */
        int extraYRange = 4;
    }

    private static synchronized Values values() {
        if (values == null) {
            values = load();
        }
        return values;
    }

    private static Values load() {
        File file = new File(FishModLoader.CONFIG_DIR, FILE_NAME);
        if (file.isFile()) {
            Reader reader = null;
            try {
                reader = new FileReader(file);
                Values loaded = GSON.fromJson(reader, Values.class);
                if (loaded != null) {
                    return loaded;
                }
                EmiResources.LOGGER.warn("{} was empty; using defaults.", FILE_NAME);
            } catch (Exception e) {
                EmiResources.LOGGER.warn("Could not read {}; using defaults.", FILE_NAME, e);
            } finally {
                close(reader);
            }
            return new Values();
        }

        Values defaults = new Values();
        save(file, defaults);
        return defaults;
    }

    private static void save(File file, Values toSave) {
        Writer writer = null;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                EmiResources.LOGGER.warn("Could not create {}; not writing a default config.", parent);
                return;
            }
            writer = new FileWriter(file);
            GSON.toJson(toSave, writer);
        } catch (Exception e) {
            EmiResources.LOGGER.warn("Could not write a default {}.", FILE_NAME, e);
        } finally {
            close(writer);
        }
    }

    private static void close(java.io.Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // Nothing useful to do if closing a config file fails.
        }
    }

    /** @return items per column on the mob and chest pages, at least 1. */
    public static int getItemsPerColumn() {
        return Math.max(1, values().itemsPerColumn);
    }

    /** @return how long each cycled item is shown, in milliseconds. */
    public static int getCycleTimeMillis() {
        return Math.max(250, (int) (values().cycleTimeSeconds * 1000.0F));
    }

    /** @return extra Y levels drawn either side of an ore's range, clamped to something sensible. */
    public static int getExtraYRange() {
        return Math.max(0, Math.min(values().extraYRange, 32));
    }
}
