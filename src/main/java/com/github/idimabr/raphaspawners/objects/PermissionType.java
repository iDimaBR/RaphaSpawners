package com.github.idimabr.raphaspawners.objects;


public enum PermissionType {

    ADD_MOBSPAWNERS("Adicionar Geradores", 10),
    REMOVE_MOBSPAWNERS("Retirar Geradores", 19),
    REMOVE_GENERATOR("Remover Gerador", 12),
    TURN_GENERATOR("Alterar status do Gerador", 21),
    ADD_MEMBER("Adicionar membros", 14),
    REMOVE_MEMBER("Remover membros", 23),
    MANAGER_PERMISSION("Gerenciar permissões", 16),
    ACCESS_PANEL_GENERATOR("Acessar painel principal", 25);

    private String name;
    private int slot;

    public String getName() {
        return name;
    }

    public int getSlot() {
        return slot;
    }

    PermissionType(String name, int slot) {
        this.name = name;
        this.slot = slot;
    }
}
