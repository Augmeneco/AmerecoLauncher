/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.utils;

import java.util.function.Consumer;

/**
 *
 * @author lanode
 */
public class ProgressSupplier {
    protected int maxProgress;
    protected int progress;
    protected String stage;
    protected String step;
    protected Consumer<ProgressData> onProgress;
    
    public void setOnProgress(Consumer<ProgressData> callback) {
        this.onProgress = callback;
    }
    
    protected void updateStage(String stage) {
        this.stage = stage;
        if (onProgress != null) {
            onProgress.accept(new ProgressData(maxProgress, progress, stage, step));
        }
    }
    
    protected void updateStep(String step) {
        this.step = step;
        if (onProgress != null) {
            onProgress.accept(new ProgressData(maxProgress, progress, stage, step));
        }
    }
    
    protected void updateStepAndIncProgress(String step) {
        progress++;
        updateStep(step);
    }
}
