package it.technoade.technoblocks.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Colored texts from messages.yml.
 */
public final class Messages {

    private final Map<String, String> single = new HashMap<String, String>();
    private final Map<String, List<String>> lists = new HashMap<String, List<String>>();

    private Messages() {
    }

    public static Messages load(ConfigurationSection yml) {
        Messages m = new Messages();
        for (String key : yml.getKeys(false)) {
            if (yml.isList(key)) {
                m.lists.put(key, SettingsLoader.colorAll(yml.getStringList(key)));
            } else if (yml.isString(key)) {
                m.single.put(key, SettingsLoader.color(yml.getString(key)));
            }
        }
        return m;
    }

    /**
     * @param placeholderPairs alternating placeholder name and value, e.g. "count", "3" replaces %count%
     * @return the message, or the key itself when missing
     */
    public String get(String key, String... placeholderPairs) {
        String text = single.get(key);
        if (text == null) return key;
        return replace(text, placeholderPairs);
    }

    public List<String> getList(String key, String... placeholderPairs) {
        List<String> raw = lists.get(key);
        if (raw == null) return Collections.singletonList(get(key, placeholderPairs));
        List<String> out = new java.util.ArrayList<String>(raw.size());
        for (String line : raw) out.add(replace(line, placeholderPairs));
        return out;
    }

    private static String replace(String text, String... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            text = text.replace("%" + pairs[i] + "%", pairs[i + 1]);
        }
        return text;
    }
}
