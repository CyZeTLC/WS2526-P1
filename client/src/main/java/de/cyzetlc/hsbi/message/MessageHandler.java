package de.cyzetlc.hsbi.message;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.gui.screens.SettingsScreen;
import de.cyzetlc.hsbi.game.utils.json.JSONObject;
import lombok.Getter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

@Getter
public class MessageHandler {
    // It's creating a list of languages that the bot supports.
    private final LinkedList<String> languageKeys = new LinkedList<>(Arrays.asList("de", "en", "ru"));

    // It's a map that stores the prefix of each language.
    private final LinkedHashMap<String, String> prefixKeys = new LinkedHashMap<>();

    // It's a reference to the messages object in the config.json file.
    private final JSONObject messages;

    private String currentLanguageKey;

    public MessageHandler() {
        this.messages = Game.getInstance().getConfig().getObject().getJSONObject("messages");
        this.currentLanguageKey = Game.getInstance().getConfig().getObject().getString("language");
    }

    /**
     * This function takes a key and a prefix key, and adds the prefix key to the prefixKeys map.
     *
     * @param key The key to be prefixed.
     * @param prefixKey The key to be used as a prefix.
     */
    public void applyPrefix(String key, String prefixKey) {
        this.prefixKeys.put(key, prefixKey);
    }

    /**
     * It updates the config with the new language key and updates the cache
     *
     * @param languageKey The language key to apply.
     */
    public void applyLanguage(String languageKey) {
        if (this.languageKeys.contains(languageKey)) {
            this.currentLanguageKey = languageKey;
            Game.getInstance().getConfig().getObject().put("language", languageKey);
            Game.getInstance().getConfig().save();

            Game.getInstance().setMainMenuScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
            Game.getInstance().setSettingsScreen(new SettingsScreen(Game.getInstance().getScreenManager()));
            Game.getLogger().info("Language set to {}", languageKey);
        }
    }

    public String getMessageForLanguage(String key, String... args) {
        if (!this.currentLanguageKey.equals("de")) {
            return this.getStaticMessage(this.currentLanguageKey + "." + key, args);
        } else {
            return this.getStaticMessage(key, args);
        }
    }

    /**
     * If the key exists, replace the placeholders with the arguments and return the message. If the key doesn't exist,
     * return a message saying that the key wasn't found
     *
     * @param key The key of the message you want to get.
     * @return A string
     */
    public String getStaticMessage(String key, String... args) {
        String firstKey = key.split("\\.")[0];
        String message = "Not Found: " + key;

        if (this.messages.has(key)) {
            String content = this.messages.getString(key);
            for (int i = 0; i < args.length; i++) {
                content = content.replace("{" + i + "}", args[i]);
            }
            message = content;

            if (this.prefixKeys.containsKey(firstKey) && !key.equals(this.prefixKeys.get(firstKey))) {
                message = content.replace("%prefix%", this.getStaticMessage(this.prefixKeys.get(firstKey)));
            }
        }
        return message;
    }

    public void loadFromJson(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            this.messages.put(key, jsonObject.getString(key));
        }
    }
}
