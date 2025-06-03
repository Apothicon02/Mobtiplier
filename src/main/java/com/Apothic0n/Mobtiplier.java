package com.Apothic0n;

import net.fabricmc.api.ModInitializer;

public class Mobtiplier implements ModInitializer {
    public static final String MODID = "mobtiplier";
    @Override
    public void onInitialize() {
        try {
            MobtiplierJsonReader.main();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}