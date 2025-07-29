/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.minecraft.fabric.models;

import com.google.gson.annotations.JsonAdapter;
import java.net.URI;
import java.util.List;

/**
 *
 * @author lanode
 */
public record FabricMeta(
    Loader loader,
    Loader intermediary,
    LauncherMeta launcherMeta
) {
    public record Loader(
        String maven,
        String version,
        boolean stable
    ) {}

    public record LauncherMeta(
        Libraries libraries,
        @JsonAdapter(MainClassAdapterFactory.class)
        MainClass mainClass
    ) {
        public record Libraries(
            List<Library> client,
            List<Library> common
        ) {
            public record Library(
                String name,
                URI url
            ) {}
        }       
    }
}