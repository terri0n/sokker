package com.formulamanager.multijuegos.entity;

import java.lang.reflect.Modifier;

import com.formulamanager.multijuegos.util.CustomTypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EntityBase {
	public String toJson() {
		// Evito que se manden campos nulos/booleanos falsos
		Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT)
				.registerTypeAdapterFactory(new CustomTypeAdapterFactory())
				.create();
		return gson.toJson(this);
	}
}
