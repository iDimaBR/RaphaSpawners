package com.github.idimabr.raphaspawners.listeners;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.PermissionType;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.objects.SpawnerLog;
import com.github.idimabr.raphaspawners.utils.ItemBuilder;
import com.github.idimabr.raphaspawners.utils.NBTAPI;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class SpawnerListener implements Listener {

    public static Map<UUID, Spawner> accessInventory = Maps.newHashMap();
    private Map<UUID, Spawner> addGenerator = Maps.newHashMap();
    private Map<UUID, Spawner> removeGenerator = Maps.newHashMap();
    private Map<UUID, Spawner> addMember = Maps.newHashMap();

    private RaphaSpawners plugin;

    public SpawnerListener(RaphaSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlaceSpawner(BlockPlaceEvent e){
        Player player = e.getPlayer();
        Block block = e.getBlockPlaced();
        if(block == null) return;
        if(block.getType() != Material.MOB_SPAWNER) return;

        int limitStack = plugin.getConfiguration().getInt("Generator.LimitStack");

        if(plugin.getConfiguration().getStringList("WorldBlackList").contains(block.getWorld().getName())){
            player.sendMessage("§cOs geradores estão desativados nesse mundo.");
            return;
        }

        ItemStack item = e.getItemInHand();
        if(item == null) return;

        if(!item.hasItemMeta()){
            player.sendMessage("§cGerador não encontrado.");
            e.setCancelled(true);
            return;
        }

        if(!item.getItemMeta().hasDisplayName()){
            player.sendMessage("§cGerador não encontrado.");
            e.setCancelled(true);
            return;
        }

        if(!item.getItemMeta().getDisplayName().contains("Gerador de Monstros")){
            player.sendMessage("§cGerador não encontrado.");
            e.setCancelled(true);
            return;
        }

        Location blockLocation = block.getLocation();

        if(!NBTAPI.hasTag(item, "spawnertype")) return;
        String stringType = NBTAPI.getTag(item, "spawnertype");

        EntityType type = EntityType.valueOf(stringType.toUpperCase());
        if(type.getEntityClass() == null) return;

        for (Block nearby : getNearbyBlocks(block.getLocation(), plugin.getConfiguration().getInt("Generator.DistanceStack"))) {
            if (nearby.getType() != Material.MOB_SPAWNER) continue;

            final Spawner spawner = RaphaSpawners.getSpawners().get(nearby.getLocation());
            if (spawner == null) continue;
            if(spawner.getType() != type) continue;

            if(!spawner.getOwner().equals(player.getUniqueId()))
                if(spawner.getMembers().containsKey(player.getUniqueId())){
                    if(!spawner.memberHasPermission(PermissionType.ADD_MOBSPAWNERS, player.getUniqueId())){
                        player.sendMessage("§cVocê não tem permissão para '§7" + PermissionType.ADD_MOBSPAWNERS.getName() + "§c' nesse gerador.");
                        e.setCancelled(true);
                        return;
                    }
                }else{
                    player.sendMessage("§cVocê não tem permissão para '§7" + PermissionType.ADD_MOBSPAWNERS.getName() + "§c' nesse gerador.");
                    e.setCancelled(true);
                    return;
                }

            block.getState().setType(Material.AIR);
            block.setType(Material.AIR);

            if(player.isSneaking() && item.getAmount() > 1){
                spawner.setQuantity(spawner.getQuantity() + item.getAmount());
                spawner.updateHologram();
                player.setItemInHand(null);
                player.sendMessage("§aColocou todos geradores de uma vez!");

                SpawnerLog log = new SpawnerLog("§eGeradores adicionados");
                log.setLore(Arrays.asList(
                        "",
                        "§7Usuário: §f" + player.getName(),
                        "§7Adicionou: §f" + item.getAmount(),
                        "§7Data: §f" + log.getDate()
                ));
                spawner.addLog(log);

                return;
            }

            spawner.setQuantity(spawner.getQuantity() + 1);
            spawner.updateHologram();
            player.sendMessage("§aGerador antigo aumentado!");
            return;
        }

        Spawner spawner = new Spawner(blockLocation, type, player.getUniqueId());
        if(player.isSneaking() && item.getAmount() > 1){
            spawner.setQuantity(item.getAmount());
            spawner.updateHologram();
            player.setItemInHand(null);

            SpawnerLog log = new SpawnerLog("§eGeradores adicionados");
            log.setLore(Arrays.asList(
                    "",
                    "§7Usuário: §f" + player.getName(),
                    "§7Adicionou: §f" + item.getAmount(),
                    "§7Data: §f" + log.getDate()
            ));
            spawner.addLog(log);
            return;
        }

        player.sendMessage("§aNovo gerador colocado!");
    }

    @EventHandler
    public void onBreakSpawner(BlockBreakEvent e){
        Player player = e.getPlayer();
        Block block = e.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;

        if(plugin.getConfiguration().getBoolean("Generator.SilkTouch")) {
            if (!player.getItemInHand().getType().toString().contains("PICKAXE")) {
                player.sendMessage("§cUtilize uma picareta com Toque Suave para remover o gerador.");
                return;
            }

            if (!player.getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                player.sendMessage("§cUtilize uma picareta com Toque Suave para remover o gerador.");
                return;
            }
        }

        Location blockLocation = block.getLocation();

        CreatureSpawner cs = (CreatureSpawner) block.getState();
        EntityType type = cs.getSpawnedType();

        if(RaphaSpawners.getSpawners().containsKey(blockLocation)){
            Spawner spawner = RaphaSpawners.getSpawners().get(blockLocation);
            e.setCancelled(true);

            if(!spawner.getOwner().equals(player.getUniqueId()))
                if(spawner.getMembers().containsKey(player.getUniqueId())) {
                    if (!spawner.memberHasPermission(PermissionType.REMOVE_GENERATOR, player.getUniqueId())) {
                        player.sendMessage("§cVocê não tem permissão para '§7" + PermissionType.REMOVE_GENERATOR.getName() + "§c' nesse gerador.");
                        return;
                    }
                }else{
                    player.sendMessage("§cVocê não tem permissão nesse gerador.");
                    return;
                }

            if(!RaphaSpawners.getPlugin().getSQL().deleteSpawner(blockLocation)){
                player.sendMessage("§cOcorreu um erro ao remover seu spawner");
                return;
            }

            block.getState().setType(Material.AIR);
            block.setType(Material.AIR);
            spawner.deleteHologram();

            ItemStack item = new ItemBuilder(Material.MOB_SPAWNER, spawner.getQuantity())
                    .setName("§eGerador de Monstros")
                    .setLore(
                            "§7Tipo: §f" + spawner.getEntityName()
                    ).toItemStack();
            item = NBTAPI.setNBTData(item, "spawnertype", type.name().toUpperCase());

            player.sendMessage("§aQuebrou spawner de §fx" + spawner.getQuantity() + " " + spawner.getEntityName());
            RaphaSpawners.getSpawners().remove(blockLocation);
            accessInventory.remove(player.getUniqueId(), spawner);
            addMember.remove(player.getUniqueId(), spawner);
            removeGenerator.remove(player.getUniqueId(), spawner);
            addGenerator.remove(player.getUniqueId(), spawner);
            blockLocation.getWorld().dropItemNaturally(blockLocation, item);
        }
    }

    @EventHandler
    public void onInteractSpawners(PlayerInteractEvent e){
        Block block = e.getClickedBlock();
        if(block == null) return;
        if(block.getType() != Material.MOB_SPAWNER) return;

        Location blockLocation = block.getLocation();

        if(RaphaSpawners.getSpawners().containsKey(blockLocation)) {
            if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

            Spawner spawner = RaphaSpawners.getSpawners().get(blockLocation);
            Player player = e.getPlayer();

            if(!spawner.getOwner().equals(player.getUniqueId()))
                if(spawner.getMembers().containsKey(player.getUniqueId())) {
                    if (!spawner.memberHasPermission(PermissionType.ACCESS_PANEL_GENERATOR, player.getUniqueId())) {
                        player.sendMessage("§cVocê não tem permissão para '§7" + PermissionType.ACCESS_PANEL_GENERATOR.getName() + "§c' nesse gerador.");
                        player.closeInventory();
                        return;
                    }
                }else{
                    player.sendMessage("§cVocê não tem permissão nesse gerador.");
                    player.closeInventory();
                    return;
                }


            accessInventory.put(player.getUniqueId(), spawner);
            RaphaSpawners.getPlugin().getManager().openPanel(player, spawner);
        }
    }

    @EventHandler
    public void onClickPlayerPermission(InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();
        if (inventory == null) return;
        if (!e.getView().getTitle().startsWith("Permissões de")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().hasDisplayName()) return;

        Player player = (Player) e.getWhoClicked();

        if (!SpawnerListener.accessInventory.containsKey(player.getUniqueId())) return;

        Spawner spawner = accessInventory.get(player.getUniqueId());
        if(item.getItemMeta().getDisplayName().equalsIgnoreCase("§cVoltar")){
            RaphaSpawners.getPlugin().getManager().openPanel(player, spawner);
            accessInventory.put(player.getUniqueId(), spawner);
            return;
        }

        if(NBTAPI.hasTag(item, "permissionID")){
            final boolean hasPermission = Boolean.parseBoolean(NBTAPI.getTag(item, "hasPermission"));
            final int permissionID = Integer.parseInt(NBTAPI.getTag(item, "permissionID"));
            final UUID uuidTarget = UUID.fromString(NBTAPI.getTag(item, "permissionUUID"));
            Optional<PermissionType> permission = Arrays.stream(PermissionType.values()).filter($ -> $.getSlot() == permissionID).findAny();
            if(!permission.isPresent()) return;

            if (hasPermission) {
                spawner.removeMemberPermission(uuidTarget, permission.get());

                SpawnerLog log = new SpawnerLog("§eRemoveu permissão");
                log.setLore(Arrays.asList(
                        "",
                        "§7Usuário: §f" + player.getName(),
                        "§7Membro: " + Bukkit.getOfflinePlayer(uuidTarget).getName(),
                        "§7Permissão: §f" + permission.get().getName(),
                        "§7Data: §f" + log.getDate()
                ));
                spawner.addLog(log);

            } else {
                spawner.addMemberPermission(uuidTarget, permission.get());

                SpawnerLog log = new SpawnerLog("§eAdicionou permissão");
                log.setLore(Arrays.asList(
                        "",
                        "§7Usuário: §f" + player.getName(),
                        "§7Membro: " + Bukkit.getOfflinePlayer(uuidTarget).getName(),
                        "§7Permissão: §f" + permission.get().getName(),
                        "§7Data: §f" + log.getDate()
                ));
                spawner.addLog(log);
            }
            RaphaSpawners.getPlugin().getManager().setPermissionPanel(player, Bukkit.getOfflinePlayer(uuidTarget), spawner, inventory);
        }
    }

    @EventHandler
    public void onClickPermissionInventory(InventoryClickEvent e){
        Inventory inventory = e.getClickedInventory();
        if(inventory == null) return;
        if(!e.getView().getTitle().equals("Permissões do Spawner")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if(item == null) return;
        if(!item.hasItemMeta()) return;
        if(!item.getItemMeta().hasDisplayName()) return;

        Player player = (Player) e.getWhoClicked();
        if(!accessInventory.containsKey(player.getUniqueId())) return;

        Spawner spawner = accessInventory.get(player.getUniqueId());
        String nameItem = item.getItemMeta().getDisplayName();

        if(nameItem.contains("Adicionar Membro")){
            if(!spawner.getOwner().equals(player.getUniqueId()))
                if(spawner.getMembers().containsKey(player.getUniqueId())) {
                    if (!spawner.memberHasPermission(PermissionType.ADD_MEMBER, player.getUniqueId())) {
                        player.sendMessage("§cVocê não tem permissão para '§7" + PermissionType.ADD_MEMBER.getName() + "§c' nesse gerador.");
                        player.closeInventory();
                        return;
                    }
                }else{
                    player.sendMessage("§cVocê não tem permissão nesse gerador.");
                    player.closeInventory();
                    return;
                }

            addMember.put(player.getUniqueId(), spawner);
            player.sendMessage("");
            player.sendMessage("§aQual jogador deseja adicionar?");
            player.sendMessage("§7Digite o nome ou §c'cancelar'§7.");
            player.sendMessage("");
            player.closeInventory();
            return;
        }

        if(nameItem.equalsIgnoreCase("§cVoltar")){
            RaphaSpawners.getPlugin().getManager().openPanel(player, spawner);
            accessInventory.put(player.getUniqueId(), spawner);
            return;
        }

        if(NBTAPI.hasTag(item, "member")){
            String member = NBTAPI.getTag(item, "member");

            OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(member));
            if(target == null) return;

            if(e.getClick().isRightClick()){
                RaphaSpawners.getPlugin().getManager().openPermissionsPlayer(player, target, spawner);
                accessInventory.put(player.getUniqueId(), spawner);
                return;
            }
            if(e.getClick().isLeftClick()){
                spawner.getMembers().remove(target.getUniqueId());
                player.sendMessage("§aVocê removeu do Gerador o membro '§f" + target.getName() + "§a'.");
                e.setCurrentItem(null);
            }

        }
    }

    @EventHandler
    public void onClickDefaultInventory(InventoryClickEvent e){
        Inventory inventory = e.getClickedInventory();
        if(inventory == null) return;
        if(!e.getView().getTitle().equals("Gerenciar Spawner")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if(item == null) return;
        if(!item.hasItemMeta()) return;
        if(!item.getItemMeta().hasDisplayName()) return;

        Player player = (Player) e.getWhoClicked();
        if(!accessInventory.containsKey(player.getUniqueId())) return;

        ClickType click = e.getClick();
        Spawner spawner = accessInventory.get(player.getUniqueId());
        String nameItem = item.getItemMeta().getDisplayName();

        switch(nameItem){
            case "§eStatus do Gerador":
                if(!spawner.getOwner().equals(player.getUniqueId()))
                    if(spawner.getMembers().containsKey(player.getUniqueId())) {
                        if (!spawner.memberHasPermission(PermissionType.TURN_GENERATOR, player.getUniqueId())) {
                            player.sendMessage("§cVocê não tem permissão para '§7" + PermissionType.TURN_GENERATOR.getName() + "§c' nesse gerador.");
                            player.closeInventory();
                            return;
                        }
                    }else{
                        player.sendMessage("§cVocê não tem permissão nesse gerador.");
                        player.closeInventory();
                        return;
                    }

                spawner.setStatus(!spawner.getStatus());
                RaphaSpawners.getPlugin().getManager().setItemPanel(player, inventory, spawner);
                spawner.updateHologram();
                player.sendMessage("§aO gerador foi " + (spawner.getStatus() ? "ativado" : "desativado"));

                SpawnerLog log = new SpawnerLog("§eAlterou status do Gerador");
                log.setLore(Arrays.asList(
                        "",
                        "§7Usuário: §f" + player.getName(),
                        "§7Status: " + (spawner.getStatus() ? "§aAtivado" : "§cDesativado"),
                        "§7Data: §f" + log.getDate()
                ));
                spawner.addLog(log);

                break;
            case "§eArmazenar geradores":
                if(!spawner.getOwner().equals(player.getUniqueId()))
                    if(spawner.getMembers().containsKey(player.getUniqueId())) {
                        if (!spawner.memberHasPermission(PermissionType.ADD_MOBSPAWNERS, player.getUniqueId())) {
                            player.sendMessage("§cVocê não tem permissão para '§7" + PermissionType.ADD_MOBSPAWNERS.getName() + "§c' nesse gerador.");
                            player.closeInventory();
                            return;
                        }
                    }else{
                        player.sendMessage("§cVocê não tem permissão nesse gerador.");
                        player.closeInventory();
                        return;
                    }

                if(click.isLeftClick()){
                    player.sendMessage("");
                    player.sendMessage("§aQual a quantidade de geradores que deseja armazenar?");
                    player.sendMessage("§7Digite no chat o número para adicionar ou digite '§ccancelar§7'.");
                    player.sendMessage("");
                    addGenerator.put(player.getUniqueId(), spawner);
                }else{
                    player.sendMessage("");
                    player.sendMessage("§aQual a quantidade de geradores que deseja retirar?");
                    player.sendMessage("§7Digite no chat o número para retirar ou digite '§ccancelar§7'.");
                    player.sendMessage("");
                    removeGenerator.put(player.getUniqueId(), spawner);
                }
                player.closeInventory();
                break;
            case "§ePermissões do Spawner":
                if(!spawner.getOwner().equals(player.getUniqueId()))
                    if(spawner.getMembers().containsKey(player.getUniqueId())) {
                        if (!spawner.memberHasPermission(PermissionType.MANAGER_PERMISSION, player.getUniqueId())) {
                            player.sendMessage("§cVocê não tem permissão para '§7" + PermissionType.MANAGER_PERMISSION.getName() + "§c' nesse gerador.");
                            player.closeInventory();
                            return;
                        }
                    }else{
                        player.sendMessage("§cVocê não tem permissão nesse gerador.");
                        player.closeInventory();
                        return;
                    }

                RaphaSpawners.getPlugin().getManager().openMenuPermissions(player, spawner);
                accessInventory.put(player.getUniqueId(), spawner);
                break;
            case "§eRegistros de alterações":
                RaphaSpawners.getPlugin().getManager().openLogsPanel(player, spawner);
                accessInventory.put(player.getUniqueId(), spawner);
                break;
        }

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();
        String message = e.getMessage();
        if(addGenerator.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            if (message.equalsIgnoreCase("cancelar")) {
                addGenerator.remove(player.getUniqueId());
                player.sendMessage("§cAção cancelada.");
                return;
            }
            if (!StringUtils.isNumeric(message)) {
                player.sendMessage("§cDigite um número válido ou cancele.");
                return;
            }
            ItemStack item = player.getItemInHand();
            if(item == null){
                player.sendMessage("§cSegure os spawners em sua mão.");
                return;
            }
            if(item.getType() != Material.MOB_SPAWNER){
                player.sendMessage("§cSegure os spawners em sua mão.");
                return;
            }

            Spawner spawner = addGenerator.get(player.getUniqueId());
            try {
                int amount = Integer.parseInt(message);
                if(amount > item.getAmount()){
                    player.sendMessage("§cQuantidade de spawners na mão insuficiente");
                    return;
                }

                spawner.setQuantity(spawner.getQuantity() + amount);
                player.sendMessage("§aForam armazenados §f" + amount + "§a geradores.");
                item.setAmount(item.getAmount() - amount);
                player.setItemInHand(item);
                addGenerator.remove(player.getUniqueId());

                SpawnerLog log = new SpawnerLog("§eGeradores adicionados");
                log.setLore(Arrays.asList(
                        "",
                        "§7Usuário: §f" + player.getName(),
                        "§7Adicionou: §f" + amount,
                        "§7Data: §f" + log.getDate()
                ));
                spawner.addLog(log);

                Bukkit.getScheduler().runTask(RaphaSpawners.getPlugin(), spawner::updateHologram);
            }catch (NumberFormatException error){
                player.sendMessage("§cDigite um número válido ou cancele.");
            }
        }

        if(removeGenerator.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            if (message.equalsIgnoreCase("cancelar")) {
                removeGenerator.remove(player.getUniqueId());
                player.sendMessage("§cAção cancelada.");
                return;
            }
            if (!StringUtils.isNumeric(message)) {
                player.sendMessage("§cDigite um número válido ou cancele.");
                return;
            }

            Spawner spawner = removeGenerator.get(player.getUniqueId());
            try {

                int amount = Integer.parseInt(message);
                if(amount == spawner.getQuantity()){
                    player.sendMessage("§cVocê não pode remover todos os spawners.");
                    return;
                }

                if(amount > spawner.getQuantity()){
                    player.sendMessage("§cNão há " + amount + " spawners para retirar.");
                    return;
                }

                for(int i = 0;i <= amount;i++){
                    ItemStack item = new ItemBuilder(Material.MOB_SPAWNER)
                            .setName("§eGerador de Monstros")
                            .setLore(
                                    "§7Tipo: §f" + spawner.getEntityName()
                            ).toItemStack();
                    item = NBTAPI.setNBTData(item, "spawnertype", spawner.getType().name().toUpperCase());

                    player.getInventory().addItem(item);
                }

                spawner.setQuantity(spawner.getQuantity() - amount);
                player.sendMessage("§aForam retirados §f" + amount + "§a geradores para você.");
                removeGenerator.remove(player.getUniqueId());

                SpawnerLog log = new SpawnerLog("§eGeradores retirados");
                log.setLore(Arrays.asList(
                        "",
                        "§7Usuário: §f" + player.getName(),
                        "§7Retirou: §f" + amount,
                        "§7Data: §f" + log.getDate()
                ));
                spawner.addLog(log);

                Bukkit.getScheduler().runTask(RaphaSpawners.getPlugin(), spawner::updateHologram);
            }catch (NumberFormatException error){
                player.sendMessage("§cDigite um número válido ou cancele.");
            }
        }

        if(addMember.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            if (message.equalsIgnoreCase("cancelar")) {
                addMember.remove(player.getUniqueId());
                player.sendMessage("§cAção cancelada.");
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(message);
            if(target == null){
                player.sendMessage("§cJogador não encontrado");
                return;
            }

            if(!target.hasPlayedBefore()){
                player.sendMessage("§cJogador não encontrado");
                return;
            }

            if(target.getUniqueId().equals(player.getUniqueId())){
                player.sendMessage("§cVocê não pode adicionar si mesmo como um membro.");
                return;
            }

            Spawner spawner = addMember.get(player.getUniqueId());
            if(target.getUniqueId().equals(spawner.getOwner())){
                player.sendMessage("§cVocê não pode adicionar o proprietário como um membro.");
                return;
            }

            if(spawner.getMembers().containsKey(target.getUniqueId())){
                player.sendMessage("§cEsse jogador já está adicionado aos membros desse gerador.");
                return;
            }

            spawner.getMembers().put(target.getUniqueId(), new ArrayList<PermissionType>(){{
                add(PermissionType.ACCESS_PANEL_GENERATOR);
                add(PermissionType.TURN_GENERATOR);
            }});

            player.sendMessage("§aJogador §f" + target.getName() + " §aadicionado.");

            SpawnerLog log = new SpawnerLog("§eMembro adicionado");
            log.setLore(Arrays.asList(
                    "",
                    "§7Usuário: §f" + player.getName(),
                    "§7Adicionou: §f" + target.getName(),
                    "§7Data: §f" + log.getDate()
                    ));
            spawner.addLog(log);

            addMember.remove(player.getUniqueId());

            RaphaSpawners.getPlugin().getManager().openPermissionsPlayer(player, target, spawner);
            accessInventory.put(player.getUniqueId(), spawner);
        }
    }

    @EventHandler
    public void onClickLogs(InventoryClickEvent e){
        Inventory inventory = e.getClickedInventory();
        if(inventory == null) return;
        if(!e.getView().getTitle().equals("Logs")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if(item == null) return;
        if(!item.hasItemMeta()) return;
        if(!item.getItemMeta().hasDisplayName()) return;

        Player player = (Player) e.getWhoClicked();
        if(!accessInventory.containsKey(player.getUniqueId())) return;

        Spawner spawner = accessInventory.get(player.getUniqueId());
        if(item.getItemMeta().getDisplayName().equalsIgnoreCase("§cVoltar")){
            RaphaSpawners.getPlugin().getManager().openPanel(player, spawner);
            accessInventory.put(player.getUniqueId(), spawner);
        }else if(item.getItemMeta().getDisplayName().equalsIgnoreCase("§cLimpar logs")){
            spawner.clearLogs();
            RaphaSpawners.getPlugin().getManager().openPanel(player, spawner);
            accessInventory.put(player.getUniqueId(), spawner);
            player.sendMessage("§aLogs foram limpadas com sucesso.");
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e){
        accessInventory.remove(e.getPlayer().getUniqueId());
    }

    private List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = Lists.newArrayList();
        Block blockInit = location.getBlock();
        for(int x = -radius; x <= radius; x++) {
            for(int y = -radius; y <= radius; y++) {
                for(int z = -radius; z <= radius; z++) {
                    Block block = blockInit.getRelative(x, y, z);
                    if(block.getType() != Material.MOB_SPAWNER) continue;
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }
}
