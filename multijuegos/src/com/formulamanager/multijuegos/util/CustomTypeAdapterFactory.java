package com.formulamanager.multijuegos.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<T>() {
            @Override
            public void write(com.google.gson.stream.JsonWriter out, T value) throws java.io.IOException {
                // Obtén la representación JSON del objeto usando el adaptador delegado
                JsonElement tree = delegate.toJsonTree(value);
                // Si el objeto es un JsonObject y contiene campos booleanos con valor false, los eliminamos
                if (tree.isJsonObject()) {
                    JsonObject jsonObject = tree.getAsJsonObject();
                    jsonObject.entrySet().removeIf(entry -> entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isBoolean() && !entry.getValue().getAsBoolean());
                }
                // Escribimos el resultado en el flujo de salida
                gson.toJson(tree, out);
            }

            @Override
            public T read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                return delegate.read(in);
            }
        };
    }
}