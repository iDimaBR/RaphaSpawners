package com.github.idimabr.raphaspawners.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class EnumUtils {
  
  public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String enumName) {
    try {
      Enum.valueOf(enumClass, enumName);
      return true;
    } catch (Throwable ex) {
      return false;
    } 
  }

  
  public static String serializeLocation(Location l) {
    return String.valueOf(l.getWorld().getName()) + ',' + 
      l.getX() + ',' + 
      l.getY() + ',' + 
      l.getZ() + ',' + 
      l.getYaw() + ',' + 
      l.getPitch();
  }
  
  public static Location deserializeLocation(String s) {
    String[] location = s.split(",");
    return new Location(
        Bukkit.getWorld(location[0]), 
        Double.parseDouble(location[1]), 
        Double.parseDouble(location[2]), 
        Double.parseDouble(location[3]), 
        Float.parseFloat(location[4]), 
        Float.parseFloat(location[5]));
  }
}
