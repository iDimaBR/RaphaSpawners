package com.github.idimabr.raphaspawners.listeners;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.MobType;
import com.github.idimabr.raphaspawners.objects.PermissionType;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.objects.SpawnerLog;
import com.github.idimabr.raphaspawners.utils.NBTAPI;
import com.github.idimabr.raphaspawners.utils.SpawnerUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
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
import org.bukkit.inventory.PlayerInventory;

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
            player.sendMessage(plugin.getMessages().getString("WorldBlocked").replace("&","§"));
            return;
        }

        ItemStack item = e.getItemInHand();
        if(item == null) return;

        if(!NBTAPI.hasTag(item, "spawnertype")) {
            player.sendMessage(plugin.getMessages().getString("GeneratorNotFound").replace("&","§"));
            return;
        }

        Location blockLocation = block.getLocation();
        String stringType = NBTAPI.getTag(item, "spawnertype");
        EntityType type = EntityType.valueOf(stringType.toUpperCase());
        if(type.getEntityClass() == null) return;

        if(!player.hasPermission(plugin.getConfigEntities().getString(type.name() + ".Permission"))){
            e.setCancelled(true);
            player.sendMessage(plugin.getMessages().getString("Errors.NoPermissionTypeGenerator").replace("%spawner_type%", MobType.valueOf(type.name()).getName()).replace("&","§"));
            return;
        }

        List<Block> blocksNearby = getNearbyBlocks(block.getLocation(), plugin.getConfiguration().getInt("Generator.DistanceStack"));
        for (Block nearby : blocksNearby) {
            if (nearby.getType() != Material.MOB_SPAWNER) continue;

            final Spawner spawner = RaphaSpawners.getSpawners().get(nearby.getLocation());
            if (spawner == null) continue;
            if(spawner.getType() != type) continue;

            if(!spawner.getOwner().equals(player.getUniqueId()))
                if(spawner.getMembers().containsKey(player.getUniqueId())){
                    if(!spawner.memberHasPermission(PermissionType.ADD_MOBSPAWNERS, player.getUniqueId())){
                        player.sendMessage(plugin.getMessages().getString("Errors.NoHaveThePermission").replace("&","§").replace("%permission%", PermissionType.ADD_MOBSPAWNERS.getName()));
                        e.setCancelled(true);
                        return;
                    }
                }else{
                    player.sendMessage(plugin.getMessages().getString("Errors.NoHaveThePermission").replace("&","§").replace("%permission%", PermissionType.ADD_MOBSPAWNERS.getName()));
                    e.setCancelled(true);
                    return;
                }

            e.setCancelled(true);

            if(player.isSneaking() && item.getAmount() > 1){
                for(int i = 0;i < item.getAmount();i++){
                    if(spawner.getQuantity() >= limitStack) {
                        player.sendMessage(plugin.getMessages().getString("Errors.LimitExcedeed")
                                .replace("&","§")
                                .replace("%limitstack%", limitStack+"")
                        );
                        e.setCancelled(true);
                        return;
                    }

                    spawner.setQuantity(spawner.getQuantity() + 1);
                    ItemStack newHand = player.getItemInHand().clone();
                    newHand.setAmount(newHand.getAmount() - 1);
                    player.setItemInHand(newHand);

                    SpawnerLog log = new SpawnerLog("§eGeradores adicionados");
                    log.setLore(Arrays.asList(
                            "",
                            "§7Usuário: §f" + player.getName(),
                            "§7Adicionou: §f" + item.getAmount(),
                            "§7Data: §f" + log.getDate()
                    ));
                    spawner.addLog(log);

                    spawner.updateHologram();
                }

                player.sendMessage(plugin.getMessages().getString("GeneratorPlaced").replace("&","§").replace("%quantity_generators%", item.getAmount()+"").replace("%spawner_type%", spawner.getEntityName()));
                return;
            }

            if(spawner.getQuantity() >= limitStack) {
                player.sendMessage(plugin.getMessages().getString("Errors.LimitExcedeed")
                        .replace("&", "§")
                        .replace("%limitstack%", limitStack + "")
                );
                e.setCancelled(true);
                return;
            }

            spawner.setQuantity(spawner.getQuantity() + 1);
            spawner.updateHologram();

            ItemStack newHand = player.getItemInHand().clone();
            newHand.setAmount(newHand.getAmount() - 1);
            player.setItemInHand(newHand);

            player.sendMessage(plugin.getMessages().getString("GeneratorPlaced").replace("&","§").replace("%quantity_generators%", 1+"").replace("%spawner_type%", spawner.getEntityName()));
            return;
        }

        Spawner spawner = new Spawner(blockLocation, type, player.getUniqueId());
        if(player.isSneaking() && item.getAmount() > 1) {
            spawner.setQuantity(0);
            for (int i = 0; i < item.getAmount(); i++) {
                if (spawner.getQuantity() >= limitStack) {
                    player.sendMessage(plugin.getMessages().getString("Errors.LimitExcedeed")
                            .replace("&", "§")
                            .replace("%limitstack%", limitStack + "")
                    );
                    e.setCancelled(true);
                    return;
                }

                spawner.setQuantity(spawner.getQuantity() + 1);
                ItemStack newHand = player.getItemInHand().clone();
                newHand.setAmount(newHand.getAmount() - 1);
                player.setItemInHand(newHand);

                SpawnerLog log = new SpawnerLog("§eGeradores adicionados");
                log.setLore(Arrays.asList(
                        "",
                        "§7Usuário: §f" + player.getName(),
                        "§7Adicionou: §f" + item.getAmount(),
                        "§7Data: §f" + log.getDate()
                ));
                spawner.addLog(log);

                spawner.updateHologram();
            }
        }

        player.sendMessage(plugin.getMessages().getString("GeneratorPlaced").replace("&","§").replace("%quantity_generators%", spawner.getQuantity()+"").replace("%spawner_type%", spawner.getEntityName()));
    }

    @EventHandler
    public void onBreakSpawner(BlockBreakEvent e){
        Player player = e.getPlayer();
        Block block = e.getBlock();
        if(block.getType() != Material.MOB_SPAWNER) return;

        Location blockLocation = block.getLocation();

        CreatureSpawner cs = (CreatureSpawner) block.getState();
        EntityType type = cs.getSpawnedType();

        if(RaphaSpawners.getSpawners().containsKey(blockLocation)){

            e.setCancelled(true);

            if(plugin.getConfiguration().getBoolean("Generator.SilkTouch")) {
                if (!player.getItemInHand().getType().toString().contains("PICKAXE")) {
                    player.sendMessage(plugin.getMessages().getString("GeneratorSilkTouch").replace("&","§"));
                    return;
                }

                if (!player.getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                    player.sendMessage(plugin.getMessages().getString("GeneratorSilkTouch").replace("&","§"));
                    return;
                }
            }

            Spawner spawner = RaphaSpawners.getSpawners().get(blockLocation);

            if(!spawner.getOwner().equals(player.getUniqueId()))
                if(spawner.getMembers().containsKey(player.getUniqueId())) {
                    if (!spawner.memberHasPermission(PermissionType.REMOVE_GENERATOR, player.getUniqueId())) {
                        player.sendMessage(plugin.getMessages().getString("Errors.NoHaveThePermission").replace("&","§").replace("%permission%", PermissionType.REMOVE_GENERATOR.getName()));
                        return;
                    }
                }else{
                    player.sendMessage(plugin.getMessages().getString("Errors.NoPermission").replace("&","§"));
                    return;
                }

            if(!RaphaSpawners.getPlugin().getSQL().deleteSpawner(blockLocation)){
                player.sendMessage(plugin.getMessages().getString("Errors.RemoveGenerator").replace("&","§"));
                return;
            }

            ItemStack item = SpawnerUtils.getSpawner(type);

            if(spawner.getQuantity() == 1){

                spawner.deleteHologram();
                player.sendMessage(plugin.getMessages().getString("GeneratorBreak").replace("&","§").replace("%quantity_generators%", spawner.getQuantity()+"").replace("%spawner_type%", spawner.getEntityName()));
                blockLocation.getWorld().dropItemNaturally(blockLocation, item);

                block.setType(Material.AIR);
                RaphaSpawners.getSpawners().remove(blockLocation);
                accessInventory.remove(player.getUniqueId(), spawner);
                addMember.remove(player.getUniqueId(), spawner);
                removeGenerator.remove(player.getUniqueId(), spawner);
                addGenerator.remove(player.getUniqueId(), spawner);
                return;
            }

            spawner.setQuantity(spawner.getQuantity() - 1);
            spawner.updateHologram();
            player.sendMessage(plugin.getMessages().getString("GeneratorBreak").replace("&","§").replace("%quantity_generators%", spawner.getQuantity()+"").replace("%spawner_type%", spawner.getEntityName()));
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
                        player.sendMessage(plugin.getMessages().getString("Errors.NoHaveThePermission").replace("&","§").replace("%permission%", PermissionType.ACCESS_PANEL_GENERATOR.getName()));
                        player.closeInventory();
                        return;
                    }
                }else{
                    player.sendMessage(plugin.getMessages().getString("Errors.NoPermission").replace("&","§"));
                    player.closeInventory();
                    return;
                }

            e.setCancelled(true);

            accessInventory.put(player.getUniqueId(), spawner);
            RaphaSpawners.getPlugin().getManager().openPanel(player, spawner);
        }
    }

    @EventHandler
    public void onClickPlayerPermission(InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();
        if (inventory == null) return;
        if (!e.getView().getTitle().startsWith(plugin.getConfigMenu().getString("PermissionsMember.Title").replace("%member%", ""))) return;

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
            Optional<PermissionType> permission = Arrays.stream(PermissionType.values()).filter($ -> $.getID() == permissionID).findAny();
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
            RaphaSpawners.getPlugin().getManager().setPermissionPanel(Bukkit.getOfflinePlayer(uuidTarget), spawner, inventory);
        }
    }

    @EventHandler
    public void onClickPermissionInventory(InventoryClickEvent e){
        Inventory inventory = e.getClickedInventory();
        if(inventory == null) return;
        if(!e.getView().getTitle().equals(plugin.getConfigMenu().getString("Permissions.Title"))) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if(item == null) return;
        if(!item.hasItemMeta()) return;
        if(!item.getItemMeta().hasDisplayName()) return;

        Player player = (Player) e.getWhoClicked();
        if(!accessInventory.containsKey(player.getUniqueId())) return;

        Spawner spawner = accessInventory.get(player.getUniqueId());
        String nameItem = item.getItemMeta().getDisplayName();

        if(nameItem.contains(plugin.getConfigMenu().getString("Permissions.ItemStack.AddMember.Name").replace("&","§"))){
            if(!spawner.getOwner().equals(player.getUniqueId()))
                if(spawner.getMembers().containsKey(player.getUniqueId())) {
                    if (!spawner.memberHasPermission(PermissionType.ADD_MEMBER, player.getUniqueId())) {
                        player.sendMessage(plugin.getMessages().getString("Errors.NoHaveThePermission").replace("&","§").replace("&","§").replace("%permission%", PermissionType.ADD_MEMBER.getName()));
                        player.closeInventory();
                        return;
                    }
                }else{
                    player.sendMessage(plugin.getMessages().getString("Errors.NoPermission").replace("&","§"));
                    player.closeInventory();
                    return;
                }

            int limitMembers = plugin.getConfiguration().getInt("Generator.LimitMembers");

            if(spawner.getMembers().size() >= limitMembers){
                player.sendMessage(plugin.getMessages().getString("Errors.MaxMembers").replace("%max_members%", limitMembers+"").replace("&","§"));
                player.closeInventory();
                return;
            }

            addMember.put(player.getUniqueId(), spawner);
            for (String s : plugin.getMessages().getStringList("Prompts.AddMember")) {
                player.sendMessage(s.replace("&","§"));
            }
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
                player.sendMessage(plugin.getMessages().getString("Success.PlayerRemoveMember").replace("&","§").replace("%member%", target.getName()));
                e.setCurrentItem(null);
            }

        }
    }

    @EventHandler
    public void onClickTOp(InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();
        if (inventory == null) return;
        if(!e.getView().getTitle().equals(plugin.getConfigMenu().getString("TopGenerator.Title"))) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().hasDisplayName()) return;

        e.setCancelled(true);


        if(item.getItemMeta().getDisplayName().contains("Voltar")){
            Player player = (Player) e.getWhoClicked();
            if(!accessInventory.containsKey(player.getUniqueId())) return;

            Spawner spawner = accessInventory.get(player.getUniqueId());
            RaphaSpawners.getPlugin().getManager().openPanel(player, spawner);
            accessInventory.put(player.getUniqueId(), spawner);
            return;
        }
    }

    @EventHandler
    public void onClickDefaultInventory(InventoryClickEvent e){
        Inventory inventory = e.getClickedInventory();
        if(inventory == null) return;
        if(!e.getView().getTitle().equals(plugin.getConfigMenu().getString("Default.Title"))) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if(item == null) return;
        if(!item.hasItemMeta()) return;
        if(!item.getItemMeta().hasDisplayName()) return;

        Player player = (Player) e.getWhoClicked();
        if(!accessInventory.containsKey(player.getUniqueId())) return;

        ClickType click = e.getClick();
        Spawner spawner = accessInventory.get(player.getUniqueId());
        String action = NBTAPI.getTag(item, "action");

        switch(action){
            case "StatusGenerator":
                if(!spawner.getOwner().equals(player.getUniqueId()))
                    if(spawner.getMembers().containsKey(player.getUniqueId())) {
                        if (!spawner.memberHasPermission(PermissionType.TURN_GENERATOR, player.getUniqueId())) {
                            player.sendMessage(plugin.getMessages().getString("Errors.NoHaveThePermission").replace("&","§").replace("%permission%", PermissionType.TURN_GENERATOR.getName()));
                            player.closeInventory();
                            return;
                        }
                    }else{
                        player.sendMessage(plugin.getMessages().getString("Errors.NoPermission").replace("&","§"));
                        player.closeInventory();
                        return;
                    }

                spawner.setStatus(!spawner.getStatus());
                RaphaSpawners.getPlugin().getManager().setItemPanel(inventory, spawner);
                spawner.updateHologram();
                player.sendMessage(plugin.getMessages().getString("Actions.ChangeStatus").replace("&","§").replace("%spawner_status%", (spawner.getStatus() ? "§aAtivado" : "§cDesativado")));

                SpawnerLog log = new SpawnerLog("§eAlterou status do Gerador");
                log.setLore(Arrays.asList(
                        "",
                        "§7Usuário: §f" + player.getName(),
                        "§7Status: " + (spawner.getStatus() ? "§aAtivado" : "§cDesativado"),
                        "§7Data: §f" + log.getDate()
                ));
                spawner.addLog(log);

                break;
            case "StoreGenerator":
                if(!spawner.getOwner().equals(player.getUniqueId()))
                    if(spawner.getMembers().containsKey(player.getUniqueId())) {
                        if (!spawner.memberHasPermission(PermissionType.ADD_MOBSPAWNERS, player.getUniqueId())) {
                            player.sendMessage(plugin.getMessages().getString("Errors.NoHaveThePermission").replace("&","§").replace("%permission%", PermissionType.ADD_MOBSPAWNERS.getName()));
                            player.closeInventory();
                            return;
                        }
                    }else{
                        player.sendMessage(plugin.getMessages().getString("Errors.NoPermission").replace("&","§"));
                        player.closeInventory();
                        return;
                    }

                int limitStack = plugin.getConfiguration().getInt("Generator.LimitStack");

                if(click.isLeftClick()){
                    if(spawner.getQuantity() >= limitStack){
                        player.sendMessage(plugin.getMessages().getString("Errors.LimitExcedeed")
                                .replace("&","§")
                                .replace("%limitstack%", limitStack+"")
                        );
                        return;
                    }

                    for (String s : plugin.getMessages().getStringList("Prompts.AddGenerators")) {
                        player.sendMessage(s.replace("&","§"));
                    }
                    addGenerator.put(player.getUniqueId(), spawner);
                    removeGenerator.remove(player.getUniqueId());
                    return;
                }else{
                    for (String s : plugin.getMessages().getStringList("Prompts.RemoveGenerators")) {
                        player.sendMessage(s.replace("&","§"));
                    }
                    addGenerator.remove(player.getUniqueId());
                    removeGenerator.put(player.getUniqueId(), spawner);
                }
                player.closeInventory();
                break;
            case "Permissions":
                if(!spawner.getOwner().equals(player.getUniqueId()))
                    if(spawner.getMembers().containsKey(player.getUniqueId())) {
                        if (!spawner.memberHasPermission(PermissionType.MANAGER_PERMISSION, player.getUniqueId())) {
                            player.sendMessage(plugin.getMessages().getString("Errors.NoHaveThePermission").replace("&","§").replace("%permission%", PermissionType.MANAGER_PERMISSION.getName()));
                            player.closeInventory();
                            return;
                        }
                    }else{
                        player.sendMessage(plugin.getMessages().getString("Errors.NoPermission").replace("&","§"));
                        return;
                    }

                RaphaSpawners.getPlugin().getManager().openMenuPermissions(player, spawner);
                accessInventory.put(player.getUniqueId(), spawner);
                break;
            case "Logs":
                RaphaSpawners.getPlugin().getManager().openLogsPanel(player, spawner);
                accessInventory.put(player.getUniqueId(), spawner);
                break;
            case "TopGenerator":
                RaphaSpawners.getPlugin().getManager().openTopGeneratorPanel(player, spawner);
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
                player.sendMessage(plugin.getMessages().getString("Actions.ActionCanceled").replace("&","§"));
                return;
            }
            if (!StringUtils.isNumeric(message)) {
                player.sendMessage(plugin.getMessages().getString("Errors.NumberInvalid").replace("&","§"));
                return;
            }

            int amount = 0;

            try {
                amount = Integer.parseInt(message);
            }catch (NumberFormatException err){
                player.sendMessage(plugin.getMessages().getString("Errors.NumberInvalid").replace("&","§"));
                return;
            }

            if(amount == 0){
                player.sendMessage(plugin.getMessages().getString("Errors.NumberInvalid").replace("&","§"));
                return;
            }

            if(amount > 2304){
                player.sendMessage(plugin.getMessages().getString("Errors.AmountMoreThanMax").replace("&","§"));
                return;
            }

            Spawner spawner = addGenerator.get(player.getUniqueId());
            int limitStack = plugin.getConfiguration().getInt("Generator.LimitStack");

            if(!hasAmountSpawner(player, amount, spawner.getType().name())){
                player.sendMessage(plugin.getMessages().getString("Errors.NoSuficientGenerators").replace("&","§"));
                return;
            }

            if((spawner.getQuantity() + amount) > limitStack){
                player.sendMessage(plugin.getMessages().getString("Errors.AddGeneratorsMoreThanLimit").replace("&","§").replace("%quantity_generators%", (limitStack - spawner.getQuantity())+"").replace("%limitstack%", limitStack+""));
                return;
            }

            removeSpawnersFromPlayer(player, amount, spawner.getType().name());
            spawner.setQuantity(spawner.getQuantity() + amount);
            player.sendMessage(plugin.getMessages().getString("Success.StoreGenerators").replace("%quantity_generators%", amount+"").replace("&","§"));
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
        }

        if(removeGenerator.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            if (message.equalsIgnoreCase("cancelar")) {
                removeGenerator.remove(player.getUniqueId());
                player.sendMessage(plugin.getMessages().getString("Actions.ActionCanceled").replace("&","§"));
                return;
            }
            if (!StringUtils.isNumeric(message)) {
                player.sendMessage(plugin.getMessages().getString("Errors.NumberInvalid").replace("&","§"));
                return;
            }

            Spawner spawner = removeGenerator.get(player.getUniqueId());
            try {

                int amount = Integer.parseInt(message);
                if(amount == spawner.getQuantity()){
                    player.sendMessage(plugin.getMessages().getString("Errors.RemoveAllGenerators").replace("&","§"));
                    return;
                }

                if(amount > spawner.getQuantity()){
                    player.sendMessage(plugin.getMessages().getString("Errors.NoTakeSuficientGenerators").replace("%quantity_generators%", amount+"").replace("&","§"));
                    return;
                }

                for(int i = 0;i < amount;i++){
                    ItemStack item = SpawnerUtils.getSpawner(spawner.getType());
                    player.getInventory().addItem(item);
                }

                spawner.setQuantity(spawner.getQuantity() - amount);
                player.sendMessage(plugin.getMessages().getString("Success.TakeGenerators").replace("&","§").replace("%quantity_generators%", amount+""));
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
                player.sendMessage(plugin.getMessages().getString("Errors.NumberInvalid").replace("&","§"));
            }
        }

        if(addMember.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
            if (message.equalsIgnoreCase("cancelar")) {
                addMember.remove(player.getUniqueId());
                player.sendMessage(plugin.getMessages().getString("Actions.ActionCanceled").replace("&","§"));
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(message);
            if(target == null){
                player.sendMessage(plugin.getMessages().getString("Errors.PlayerNotFound").replace("&","§"));
                return;
            }

            if(!target.hasPlayedBefore()){
                player.sendMessage(plugin.getMessages().getString("Errors.PlayerNotFound").replace("&","§"));
                return;
            }

            if(target.getUniqueId().equals(player.getUniqueId())){
                player.sendMessage(plugin.getMessages().getString("Errors.NoAddMemberOwn").replace("&","§"));
                return;
            }

            Spawner spawner = addMember.get(player.getUniqueId());
            if(target.getUniqueId().equals(spawner.getOwner())){
                player.sendMessage(plugin.getMessages().getString("Errors.NoAddOwnerMember").replace("&","§"));
                return;
            }

            if(spawner.getMembers().containsKey(target.getUniqueId())){
                player.sendMessage(plugin.getMessages().getString("Errors.PlayerWasMember").replace("&","§"));
                return;
            }

            spawner.getMembers().put(target.getUniqueId(), new ArrayList<PermissionType>(){{
                add(PermissionType.ACCESS_PANEL_GENERATOR);
                add(PermissionType.TURN_GENERATOR);
            }});

            player.sendMessage(plugin.getMessages().getString("Success.PlayerAddedMember").replace("&","§").replace("%member%", target.getName()));

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
            player.sendMessage(plugin.getMessages().getString("Success.ClearLogs").replace("&","§"));
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

    public boolean removeSpawnersFromPlayer(Player player, int count, String type) {
        Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(Material.MOB_SPAWNER);

        int found = 0;
        for (ItemStack stack : ammo.values()) {
            if(!NBTAPI.hasTag(stack, "spawnertype")) continue;
            if(!NBTAPI.getTag(stack, "spawnertype").equals(type)) continue;
            found += stack.getAmount();
        }
        if (count > found)
            return false;

        for (Integer index : ammo.keySet()) {
            ItemStack stack = ammo.get(index);
            if(!NBTAPI.hasTag(stack, "spawnertype")) continue;
            if(!NBTAPI.getTag(stack, "spawnertype").equals(type)) continue;

            int removed = Math.min(count, stack.getAmount());
            count -= removed;

            if (stack.getAmount() == removed)
                player.getInventory().setItem(index, null);
            else
                stack.setAmount(stack.getAmount() - removed);

            if (count <= 0)
                break;
        }

        player.updateInventory();
        return true;
    }

    public boolean hasAmountSpawner(Player player, int needed, String type){
        PlayerInventory inventory = player.getInventory();
        int amount = 0;
        for (ItemStack itemStack : inventory) {
            if(itemStack != null && itemStack.getType() == Material.MOB_SPAWNER) {
                if(!NBTAPI.hasTag(itemStack, "spawnertype")) continue;
                if(!NBTAPI.getTag(itemStack, "spawnertype").equals(type)) continue;
                amount += itemStack.getAmount();
            }
        }

        return amount >= needed;
    }

    public boolean hasAmountSpawner2(Player player, int needed){
        return Arrays.stream(player.getInventory().getContents())
                .map(is -> is != null && is.getType() == Material.MOB_SPAWNER).count() >= needed;
    }
}
