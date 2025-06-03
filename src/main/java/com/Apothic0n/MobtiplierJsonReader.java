package com.Apothic0n;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MobtiplierJsonReader {
    public static int multiplier = 0;
    public static void main() throws Exception {
        makeConfig(Path.of(FabricLoader.getInstance().getConfigDir() + "/mobtiplier.json"));
    }

    public static void makeConfig(Path path) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!Files.exists(path)) {
            Files.write(path, ("{\n" +
                    "    \"mobSpawnMultiplier\": 10\n" +
                    "}").getBytes());
        }
        JsonReader reader = new JsonReader(new FileReader(path.toString()));
        JsonObject data = gson.fromJson(reader, JsonObject.class);

        multiplier = data.get("mobSpawnMultiplier").getAsInt();
    }
}
