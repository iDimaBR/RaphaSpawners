package com.github.idimabr.raphaspawners.objects;

public enum PermissionType {

    ADD_MOBSPAWNERS("Adicionar Geradores", 0),
    REMOVE_MOBSPAWNERS("Retirar Geradores", 1),
    REMOVE_GENERATOR("Remover Gerador", 2),
    TURN_GENERATOR("Alterar status do Gerador", 3),
    ADD_MEMBER("Adicionar membros", 4),
    REMOVE_MEMBER("Remover membros", 5),
    MANAGER_PERMISSION("Gerenciar permissões", 6),
    ACCESS_PANEL_GENERATOR("Acessar painel principal", 7);

    private String name;
    private int id;

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    PermissionType(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
