package com.formulamanager.sokker.entity;

/**
 * A player display group. Default groups are shared (userId == null);
 * custom groups belong to one Asistente account.
 */
public final class GrupoJugador {
    private final int id;
    private final Integer userId;
    private final String code;
    private final String name;
    private final int sortOrder;

    public GrupoJugador(int id, Integer userId, String code, String name, int sortOrder) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public int getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getSortOrder() { return sortOrder; }
    public boolean isDefault() { return userId == null; }
}
