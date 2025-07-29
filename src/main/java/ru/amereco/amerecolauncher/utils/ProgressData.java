/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.utils;

/**
 *
 * @author lanode
 */
public record ProgressData (
    int maxProgress,
    int progress,
    String stage,
    String step
) {
}