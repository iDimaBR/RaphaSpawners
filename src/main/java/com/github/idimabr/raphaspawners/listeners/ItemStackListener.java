package com.github.idimabr.raphaspawners.listeners;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.Collection;
import java.util.stream.Collectors;

public class ItemStackListener implements Listener {

    public void spawnItem(ItemSpawnEvent e) {
        Item item = e.getEntity();

        ItemStack itemstack = item.getItemStack();
        Collection<Entity> entities = item.getWorld().getNearbyEntities(item.getLocation(), 10,10,10);

        boolean otherEntity = false;
        if(!entities.isEmpty()) {
            for (Entity entity : entities.stream().filter(en -> en.getType().equals(item.getType())).collect(Collectors.toList())) {
                if(!entity.hasMetadata("itemAmount")) continue;

                item.remove();
                otherEntity = true;

                int amount = entity.getMetadata("itemAmount").get(0).asInt() + itemstack.getAmount();
                entity.setMetadata("itemAmount", new FixedMetadataValue(RaphaSpawners.getPlugin(), amount));
                return;
            }
        }

        if(!otherEntity){
            item.setMetadata("itemAmount", new FixedMetadataValue(RaphaSpawners.getPlugin(), itemstack.getAmount()));
        }
    }

    public void getItem(PlayerPickupItemEvent e){
        Item item = e.getItem();
        Player player = e.getPlayer();
        if(!isDrop(item)) return;

        final PlayerInventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) return;

        int dropAmount = getDropAmount(item);
        if (dropAmount == 0) return;

        final ItemStack itemStack = item.getItemStack();
        final int maxStackSize = itemStack.getMaxStackSize();

        for (ItemStack content : inventory.getContents()) {
            if (content != null && content.getType() != Material.AIR) continue;
            if (dropAmount <= maxStackSize) {
                itemStack.setAmount(dropAmount);
                inventory.addItem(itemStack);
                System.out.println("Quantidade 0: " + dropAmount);
                dropAmount = 0;
                continue;
            }
            itemStack.setAmount(maxStackSize);
            inventory.addItem(itemStack);
            dropAmount -= maxStackSize;
            System.out.println("Quantidade 1: " + dropAmount);
        }
        if(dropAmount == 0){
            System.out.println("Quantidade 2: " + dropAmount);
            item.remove();
            return;
        }
        System.out.println("Quantidade 3: " + dropAmount);
        item.setMetadata("itemAmount", new FixedMetadataValue(RaphaSpawners.getPlugin(), dropAmount));
    }

    public boolean isDrop(Item item){
        return item.hasMetadata("itemAmount");
    }

    public int getDropAmount(Item item){
        if(!isDrop(item)) return 0;

        return item.getMetadata("itemAmount").get(0).asInt();
    }
}
