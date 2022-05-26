package com.zpedroo.market.mysql;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.market.objects.MarketItem;
import com.zpedroo.market.objects.PlayerData;
import com.zpedroo.market.utils.encoder.Base64Encoder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class DBManager {

    public void saveAuctionData(MarketItem marketItem) {
        executeUpdate("REPLACE INTO `" + DBConnection.SALES_TABLE + "` (`id`, `seller_uuid`, `serialized_item`, `expiration_date_in_millis`, `currency`, `price`) VALUES " +
                "('" + marketItem.getId() + "', " +
                "'" + marketItem.getSellerUniqueId() + "', " +
                "'" + Base64Encoder.itemStackArrayToBase64(new ItemStack[] { marketItem.getItem() }) + "', " +
                "'" + marketItem.getExpirationDateInMillis() + "', " +
                "'" + marketItem.getCurrency().getFileName() + "', " +
                "'" + marketItem.getPrice() + "');");
    }

    public void savePlayerData(PlayerData data) {
        executeUpdate("REPLACE INTO `" + DBConnection.PLAYERS_TABLE + "` (`uuid`, `items_to_collect`) VALUES " +
                "('" + data.getUniqueId() + "', " +
                "'" + Base64Encoder.itemStackArrayToBase64(data.getItemsToCollect().toArray(new ItemStack[0])) + "');");
    }

    public Map<Long, MarketItem> getAuctionsFromDatabase() {
        Map<Long, MarketItem> auctions = new HashMap<>(32);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.SALES_TABLE + "`;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                long auctionId = result.getLong(1);
                UUID sellerUniqueId = UUID.fromString(result.getString(2));
                ItemStack item = Base64Encoder.itemStackArrayFromBase64(result.getString(3))[0];
                long expirationDateInMillis = result.getLong(4);
                Currency currency = CurrencyAPI.getCurrency(result.getString(5));
                BigInteger price = result.getBigDecimal(6).toBigInteger();

                auctions.put(auctionId, new MarketItem(auctionId, sellerUniqueId, item, currency, price, expirationDateInMillis));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return auctions;
    }

    public PlayerData getPlayerDataFromDatabase(Player player) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.PLAYERS_TABLE + "` WHERE `uuid`='" + player.getUniqueId() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                List<ItemStack> itemsToCollect = new LinkedList<>(Arrays.asList(Base64Encoder.itemStackArrayFromBase64(result.getString(2))));

                return new PlayerData(player.getUniqueId(), itemsToCollect);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return new PlayerData(player.getUniqueId(), new ArrayList<>(4));
    }

    public void deleteSale(long saleId) {
        executeUpdate("DELETE FROM `" + DBConnection.SALES_TABLE + "` WHERE `id`='" + saleId + "';");
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, null, null, statement);
        }
    }

    private void closeConnection(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void createTable() {
        executeUpdate("CREATE TABLE IF NOT EXISTS `" + DBConnection.SALES_TABLE + "` (`id` BIGINT, `seller_uuid` VARCHAR(255)," +
                "`serialized_item` LONGTEXT, `expiration_date_in_millis` BIGINT, `currency` LONGTEXT, `price` DECIMAL(40,0)," +
                " PRIMARY KEY(`id`));");
        executeUpdate("CREATE TABLE IF NOT EXISTS `" + DBConnection.PLAYERS_TABLE + "` (`uuid` VARCHAR(255), `items_to_collect` LONGTEXT, PRIMARY KEY(`uuid`));");
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }
}