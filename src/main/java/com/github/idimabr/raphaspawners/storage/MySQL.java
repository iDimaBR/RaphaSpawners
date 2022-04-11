package com.github.idimabr.raphaspawners.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.github.idimabr.raphaspawners.RaphaSpawners;
import com.github.idimabr.raphaspawners.utils.ConfigUtil;
import com.github.idimabr.raphaspawners.utils.SerializerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class MySQL {

	private RaphaSpawners plugin;
	private ConfigUtil config;
	private Connection connection;
	private PreparedStatement smt;

	public MySQL(RaphaSpawners plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfiguration();
		try {
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName);
			String host = config.getString("MySQL.Host"); 
			String database = config.getString("MySQL.Database");
			String url = "jdbc:mysql://" + host + "/" + database;
			String username = config.getString("MySQL.Username");
			String password = config.getString("MySQL.Password");
			connection = DriverManager.getConnection(url, username, password);
			plugin.getLogger().info("§aConexão com banco de dados foi estabelecida.");
		} catch (Exception e) {
			plugin.getLogger().info("§cOcorreu um erro no banco de dados");
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
	
	public void createTable() {
		try {
			String QUERY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS spawners(`id` int(11) NOT NULL AUTO_INCREMENT, `owner` varchar(36) NOT NULL, `location` varchar(36) NOT NULL, `quantity` int(15) NOT NULL, `type` varchar(36) NOT NULL,`members` longtext, PRIMARY KEY (`id`,`location`))";
			smt = connection.prepareStatement(QUERY_TABLE_CREATE);
			smt.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().info("Erro na criação de tabela: " + e.getLocalizedMessage());
			plugin.getPluginLoader().disablePlugin(plugin);
		}
	}

	public Connection getConnectionMySQL() {
		return connection;
	}
	
	public boolean isSpawner(Location location){
		try {
			smt = connection.prepareStatement("SELECT `location` FROM spawners WHERE `location` = ?");
			smt.setString(1, SerializerUtils.convertLocation(location));
			ResultSet result = smt.executeQuery();
			if(result.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean deleteSpawner(Location location){
		try {
			smt = connection.prepareStatement("DELETE FROM spawners WHERE `location` = ?");
			smt.setString(1, SerializerUtils.convertLocation(location));
			smt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public ResultSet getString(String coluna, String valor) {
		try {
			String sql = "SELECT * FROM spawners WHERE ? = ?";
			smt = connection.prepareStatement(sql);
			smt.setString(1, coluna);
			smt.setString(2, valor);
			return smt.executeQuery();
		} catch (SQLException e) {
			Bukkit.getLogger().info("Método getString retornou nullo");
		}
		return null;
	}

	public void executeUpdateMySQL(String query) {
		try {
			smt = connection.prepareStatement(query);
			smt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean close() {
		try {
			if(getConnectionMySQL() != null) {
				getConnectionMySQL().close();
			}else {
				return false;
			}
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public Connection restart() {
		close();
		return getConnectionMySQL();
	}
}