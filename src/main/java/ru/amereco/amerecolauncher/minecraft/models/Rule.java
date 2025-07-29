/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.minecraft.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 *
 * @author lanode
 */
public record Rule(
    Action action,
    Map<String, Boolean> features,
    OS os
) {
    public enum Action {
        @SerializedName("allow") ALLOW,
        @SerializedName("disallow") DISALLOW
    }

    public record OS(
        OSName name,
        String version,
        String arch
    ) {
        public enum OSName {
            @SerializedName("osx") OSX,
            @SerializedName("windows") WINDOWS,
            @SerializedName("linux") LINUX
        }
    }

    public boolean allows() {
        boolean shouldDisallow = action == Action.DISALLOW;

        if (os != null) {
            String currentOS = System.getProperty("os.name").toLowerCase();
            String currentArch = System.getProperty("os.arch").contains("64") ? "x64" : "x86";

            if (os.name() != null) {
                String osName = os.name().toString().toLowerCase();
                if (!currentOS.contains(osName)) {
                    return shouldDisallow;
                }
            }

            if (os.arch() != null && !os.arch().isEmpty()) {
                if (!os.arch().equals(currentArch)) {
                    return shouldDisallow;
                }
            }
        }

        return !shouldDisallow;
    }
}
