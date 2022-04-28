package com.github.idimabr.raphaspawners.utils;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.objects.MobType;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SpawnerUtils {

	public static ItemStack getSpawner(EntityType type){
		String name = RaphaSpawners.getPlugin().getConfiguration().getString("Generator.ItemStack.Name").replace("&","§");

		List<String> lore = new ArrayList<>();
		for (String s : RaphaSpawners.getPlugin().getConfiguration().getStringList("Generator.ItemStack.Lore")) {
			lore.add(
					s.replace("&","§")
							.replace("%spawner_type%", MobType.valueOf(type.name()).getName())
			);
		}
		
		ItemStack item = new ItemBuilder(Material.MOB_SPAWNER)
				.setName(name)
				.setLore(lore).toItemStack();

		item = NBTAPI.setNBTData(item, "spawnertype", type.name().toUpperCase());

		return item;
	}

	public static void disableAI(Entity bukkitEntity) {
		net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
		NBTTagCompound tag = nmsEntity.getNBTTag();
		if (tag == null) {
			tag = new NBTTagCompound();
		}
		nmsEntity.c(tag);
		tag.setInt("NoAI", 1);
		nmsEntity.f(tag);
	}
}