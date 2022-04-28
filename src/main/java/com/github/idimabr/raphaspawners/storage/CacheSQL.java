package com.github.idimabr.raphaspawners.storage;

import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.adapter.AdapterSpawner;
import com.github.idimabr.raphaspawners.objects.Spawner;
import com.github.idimabr.raphaspawners.utils.SerializerUtils;
import org.bukkit.Location;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class CacheSQL{

    public static Spawner loadCache(){
        Connection connection = RaphaSpawners.getPlugin().getSQL().getConnectionMySQL();
        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM spawners")) {
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()){
                    Spawner spawner = AdapterSpawner.toSpawner(rs);
                    if(spawner == null)
                        System.out.println("Spawner não existe mais, pulando...");
                }
            }
        } catch (Exception error) {
            error.printStackTrace();
            System.out.println("Erro ao carregar em cache: " + error.getLocalizedMessage());
        }
        return null;
    }

    public static void saveCache(){
        Connection connection = RaphaSpawners.getPlugin().getSQL().getConnectionMySQL();

        for (Map.Entry<Location, Spawner> entry : RaphaSpawners.getSpawners().entrySet()) {
            Location location = entry.getKey();
            Spawner spawner = entry.getValue();

            String query = "UPDATE spawners SET quantity = ?, type = ?, members = ? WHERE location = ?";
            if(!RaphaSpawners.getPlugin().getSQL().isSpawner(location))
                query = "INSERT INTO spawners(`owner`,`location`,`quantity`,`type`,`members`) VALUES (?,?,?,?,?)";

            spawner.deleteHologram();

            try(PreparedStatement ps = connection.prepareStatement(query)) {
                if(query.contains("UPDATE")){
                    ps.setInt(1, spawner.getQuantity());
                    ps.setString(2, spawner.getType().name().toUpperCase());
                    ps.setString(3, SerializerUtils.convertMembers(spawner.getMembers()));
                    ps.setString(4, SerializerUtils.convertLocation(spawner.getLocation()));
                }else{
                    ps.setString(1, spawner.getOwner().toString());
                    ps.setString(2, SerializerUtils.convertLocation(spawner.getLocation()));
                    ps.setInt(3, spawner.getQuantity());
                    ps.setString(4, spawner.getType().name().toUpperCase());
                    ps.setString(5, SerializerUtils.convertMembers(spawner.getMembers()));
                }
                ps.executeUpdate();
            } catch (Exception error) {
                System.out.println("Erro ao salvar em cache: " + error.getLocalizedMessage());
            }
        }
    }
}
