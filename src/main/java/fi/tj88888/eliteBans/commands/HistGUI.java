package fi.tj88888.eliteBans.commands;

import fi.tj88888.eliteBans.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.UUID;

import static fi.tj88888.eliteBans.utils.PlayerUtils.getPlayerUUID;

public class HistGUI implements CommandExecutor {
    private final DatabaseManager databaseManager;

    public HistGUI(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        if (!sender.hasPermission("elitebans.command.hist")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /histgui <player>");
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = getPlayerUUID(targetName);

        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        Player player = (Player) sender;
        openMainMenu(player, targetName, targetUUID);
        return true;
    }

    private void openMainMenu(Player player, String targetName, UUID targetUUID) {
        Inventory inv = Bukkit.createInventory(null, 27,
                ChatColor.DARK_PURPLE + targetName + "'s History" + "||" + targetUUID.toString());
        // Fill all slots with gray glass panels first
        ItemStack filler = createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // Create menu items
        ItemStack bans = createMenuItem(Material.RED_CONCRETE, "Bans", "Click to view ban history");
        ItemStack mutes = createMenuItem(Material.YELLOW_CONCRETE, "Mutes", "Click to view mute history");
        ItemStack warns = createMenuItem(Material.ORANGE_CONCRETE, "Warns", "Click to view warn history");

        // Set items in inventory
        inv.setItem(11, bans);
        inv.setItem(13, mutes);
        inv.setItem(15, warns);

        player.openInventory(inv);
    }

    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}