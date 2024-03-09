package com.formulamanager.multijuegos.entity;

import com.google.gson.Gson;

public class EntityBase {
	public String toJson() {
		return new Gson().toJson(this);
	}
}
