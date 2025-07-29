/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.minecraft.models;

import java.util.Map;

/**
 *
 * @author lanode
 */
public record AssetIndex(
    Map<String, Asset> objects
) {
    public record Asset(
        String hash,
        int size
    ) {}
}