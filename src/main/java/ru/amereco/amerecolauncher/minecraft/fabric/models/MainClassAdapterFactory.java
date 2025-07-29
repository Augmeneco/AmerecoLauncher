/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.amereco.amerecolauncher.minecraft.fabric.models;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.google.gson.stream.*;
import java.io.IOException;

/**
 *
 * @author lanode
 */
public class MainClassAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return (TypeAdapter<T>) new GuidAdapter(gson);
    }

    public class GuidAdapter extends TypeAdapter<MainClass> {
        private final Gson gson;
        public GuidAdapter(Gson gson) {
            this.gson = gson;
        }
        @Override
        public void write(JsonWriter jsonWriter, MainClass guid) throws IOException {
            throw new RuntimeException("Not implemented");
        }
        @Override
        public MainClass read(JsonReader jsonReader) throws IOException {
            switch (jsonReader.peek()) {
                case STRING:
                    // only a String, create the object
                    return new MainClass(jsonReader.nextString());
                case BEGIN_OBJECT:
                    // full object, forward to Gson
                    return gson.fromJson(jsonReader, MainClass.class);
                default:
                    throw new RuntimeException("Expected object or string, not " + jsonReader.peek());
            }
        }
    }
}

