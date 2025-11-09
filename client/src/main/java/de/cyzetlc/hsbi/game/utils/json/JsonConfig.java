package de.cyzetlc.hsbi.game.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonConfig {
    private JSONObject object;
    private Gson gson;
    private File file;

    public JsonConfig(String file) {
        this.file = new File(file);
        this.gson = (new GsonBuilder()).setPrettyPrinting().create();
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
                this.object = new JSONObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.object = new JSONObject(new String(Files.readAllBytes(Paths.get(this.file.toURI())), String.valueOf(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public JsonConfig(JSONObject jsonObject) {
        this.gson = (new GsonBuilder()).setPrettyPrinting().create();
        this.object = jsonObject;
    }

    public <T> T load(Class<T> clazz) {
        return this.gson.fromJson(this.object.toString(), clazz);
    }

    public void save(){
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            try {
                writer.write(this.object.toString());
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getObject() {
        return object;
    }

    public File getFile() {
        return file;
    }
}
