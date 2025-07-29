/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.minecraft.models;

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 *
 * @author lanode
 */
public record VersionManifest(
    Latest latest,
    List<VersionInfo> versions
) {
    public record VersionInfo(
        String id,
        String type,
        URI url,
        Date time,
        Date releaseTime
    ) {}

    public record Latest(
        String release,
        String snapshot
    ) {}
}