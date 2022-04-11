package com.github.idimabr.raphaspawners.managers;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.listeners.SpawnerListener;
import com.github.idimabr.raphaspawners.objects.PermissionType;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.objects.SpawnerLog;
import com.github.idimabr.raphaspawners.utils.ItemBuilder;
import com.github.idimabr.raphaspawners.utils.NBTAPI;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpawnerManager {

    private RaphaSpawners plugin;

    public SpawnerManager(RaphaSpawners plugin) {
        this.plugin = plugin;
    }

    public void openPanel(Player player, Spawner spawner){
        if(spawner == null) return;

        Inventory inventory = Bukkit.createInventory(null, 3 * 9, "Gerenciar Spawner");

        setItemPanel(player, inventory, spawner);
        player.openInventory(inventory);
    }

    public void openMenuPermissions(Player player, Spawner spawner){
        if(spawner == null) return;

        Inventory inventory = Bukkit.createInventory(null, 6 * 9, "Permissões do Spawner");

        int i = 0;
        for (Map.Entry<UUID, List<PermissionType>> entry : spawner.getMembers().entrySet()) {
            if(player.getUniqueId().equals(entry.getKey())) continue;

            ItemStack item = new ItemBuilder(Material.SKULL_ITEM)
                    .setDurability((short) 3)
                    .setName("§e" + Bukkit.getOfflinePlayer(entry.getKey()).getName())
                    .setLore(
                            "§7Clique §fesquerdo §7para remover esse jogador.",
                            "§7Clique §fdireito §7para gerenciar as permissões deste jogador.")
                    .toItemStack();
            item = NBTAPI.setNBTData(item, "member", entry.getKey().toString());

            inventory.setItem(i, item);
            i++;
        }

        inventory.setItem(48, new ItemBuilder(Material.ARROW).setName("§cVoltar").toItemStack());

        inventory.setItem(50,
                new ItemBuilder(Material.NETHER_STAR)
                        .setName("§aAdicionar Membro")
                        .setLore(
                                "§7Clique para adicionar membros ao gerador"
                        ).toItemStack());

        player.openInventory(inventory);
    }

    public void openPermissionsPlayer(Player player, OfflinePlayer target, Spawner spawner){
        Inventory inventory = Bukkit.createInventory(null, 6 * 9, "Permissões de " + target.getName());
        if(!spawner.getMembers().containsKey(target.getUniqueId())) return;

        setPermissionPanel(player, target, spawner, inventory);
        player.openInventory(inventory);
    }

    public void setPermissionPanel(Player player, OfflinePlayer target, Spawner spawner, Inventory inventory){
        List<PermissionType> permissions = spawner.getMembers().get(target.getUniqueId());

        for (PermissionType permission : PermissionType.values()) {
            ItemStack item = new ItemBuilder(permissions.contains(permission) ? Material.SLIME_BALL : Material.BARRIER)
                    .setName("§f" + permission.getName())
                    .setLore(
                            "§7Clique para alternar a permissão desse jogador.",
                            "§7Status: " + (permissions.contains(permission) ? "§aPermitido" : "§cNão permitido")
                    )
                    .toItemStack();
            item = NBTAPI.setNBTData(item, "hasPermission", String.valueOf( permissions.contains(permission) ));
            item = NBTAPI.setNBTData(item, "permissionUUID", target.getUniqueId().toString());
            item = NBTAPI.setNBTData(item, "permissionID", permission.getSlot()+"");

            inventory.setItem(permission.getSlot(), item);
        }

        inventory.setItem(49, new ItemBuilder(Material.ARROW).setName("§cVoltar").toItemStack());
    }

    public void setItemPanel(Player player, Inventory inventory, Spawner spawner){
        OfflinePlayer owner = Bukkit.getOfflinePlayer(spawner.getOwner());

        inventory.setItem(10,
                new ItemBuilder(Material.NETHER_STAR)
                        .setName("§ePermissões do Spawner")
                        .setLore("",
                                "§7Clique para gerenciar as permissões do gerador"
                        ).toItemStack()
        );

        inventory.setItem(11,
                new ItemBuilder(Material.SKULL_ITEM)
                        .setDurability((short) 3)
                        .setSkullOwner(player.getName())
                        .setName("§eProprietário")
                        .setLore("§7Dono: §f" + owner.getName(),
                                "§7Status: " + (owner.isOnline() ? "§aOnline" : "§cOffline"),
                                "",
                                "§7O jogador que colocar o gerador",
                                "§7torna-se o principal proprietário."
                        ).toItemStack()
        );

        inventory.setItem(13,
                new ItemBuilder(Material.BOOK)
                        .setName("§eRegistros de alterações")
                        .setLore("",
                                "§7Clique para abrir suas logs"
                        ).toItemStack()
        );

        inventory.setItem(14,
                new ItemBuilder(Material.MINECART)
                        .setName("§eArmazenar geradores")
                        .setLore("",
                                "§7Tipo: §f" + spawner.getEntityName(),
                                "§7Quantidade: §f" + spawner.getQuantity(),
                                "",
                                "§7Clique §fesquerdo §7para adicionar Geradores",
                                "§7Clique §fdireito §7para remover Geradores"
                        ).toItemStack()
        );

        inventory.setItem(15,
                new ItemBuilder(Material.MOB_SPAWNER)
                        .setName("§eStatus do Gerador")
                        .setLore("",
                                "§7Status: §f" + (spawner.getStatus() ? "§aAtivado" : "§cDesativado"),
                                "",
                                "§7Clique para " + (spawner.getStatus() ? "ativar" : "desativar")
                        ).toItemStack()
        );
    }

    // logs

    public void openLogsPanel(Player player, Spawner spawner){
        if(spawner == null) return;

        Inventory inventory = Bukkit.createInventory(null, 6 * 9, "Logs");

        int i = 0;
        for (SpawnerLog spawnerLog : spawner.getLogs()) {
            inventory.setItem(i,
                    new ItemBuilder(Material.PAPER)
                            .setName(spawnerLog.getTitle())
                            .setLore(spawnerLog.getLore())
                            .toItemStack());
            i++;
        }

        inventory.setItem(48, new ItemBuilder(Material.ARROW).setName("§cVoltar").toItemStack());
        inventory.setItem(50, new ItemBuilder(Material.REDSTONE).setName("§cLimpar logs").toItemStack());
        player.openInventory(inventory);
    }

    public void setAI(Entity bukkitEntity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        nmsEntity.c(tag);
        tag.setInt("NoAI", 0);
        nmsEntity.f(tag);
    }
}
