package fi.tj88888.eliteBans.listeners;

import fi.tj88888.eliteBans.database.DatabaseManager;
import fi.tj88888.eliteBans.models.Punishment;
import fi.tj88888.eliteBans.utils.DateUtil;
import fi.tj88888.eliteBans.utils.LogUtil;
import fi.tj88888.eliteBans.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static fi.tj88888.eliteBans.utils.PlayerUtils.formatRemainingTime;

public class HistoryGUIListener implements Listener {
    private final DatabaseManager databaseManager;
    private final Map<UUID, PunishmentPardonData> pendingPardons = new HashMap<>();

    public HistoryGUIListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private static class PunishmentPardonData {
        final UUID targetUUID;
        final String punishmentType;

        PunishmentPardonData(UUID targetUUID, String punishmentType) {
            this.targetUUID = targetUUID;
            this.punishmentType = punishmentType;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("'s History")) {
            return;
        }

        event.setCancelled(true);

        if (!event.getWhoClicked().hasPermission("elitebans.command.hist")) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "You don't have permission to view histories!");
            return;
        }
        String[] titleParts = event.getView().getTitle().split("\\|\\|");
        if (titleParts.length < 2) return;
        UUID targetUUID = UUID.fromString(titleParts[1]);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        if (clicked.getType() == Material.BARRIER &&
                clicked.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Go Back")) {
            if (titleParts.length >= 2) {
                Player player = (Player) event.getWhoClicked();
                openMainMenu(player, Bukkit.getOfflinePlayer(targetUUID).getName(), targetUUID);
            }
            return;
        }
        if (clicked.getType() == Material.RED_CONCRETE) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null &&
                    meta.getDisplayName().equals(ChatColor.WHITE + "Bans") &&
                    meta.getLore() != null &&
                    meta.getLore().contains("Click to view ban history")) {
                createBanHistoryGUI((Player) event.getWhoClicked(), targetUUID);
            }
        }
        if (clicked.getType() == Material.YELLOW_CONCRETE) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null &&
                    meta.getDisplayName().equals(ChatColor.WHITE + "Mutes") &&
                    meta.getLore() != null &&
                    meta.getLore().contains("Click to view mute history")) {
                createMuteHistoryGUI((Player) event.getWhoClicked(), targetUUID);
            }
        }
        if (clicked.getType() == Material.ORANGE_CONCRETE) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null &&
                    meta.getDisplayName().equals(ChatColor.WHITE + "Warns") &&
                    meta.getLore() != null &&
                    meta.getLore().contains("Click to view warn history")) {
                createWarnHistoryGUI((Player) event.getWhoClicked(), targetUUID);
            }
        }
        if (event.getClick() == ClickType.RIGHT && event.getCurrentItem() != null &&
                event.getCurrentItem().getType() == Material.GREEN_CONCRETE) {
            Player player = (Player) event.getWhoClicked();
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            String punishmentType = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

            if (!player.hasPermission("elitebans.command.unban")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to pardon punishments!");
                return;
            }

            pendingPardons.put(player.getUniqueId(), new PunishmentPardonData(targetUUID, punishmentType));
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Please type the reason in chat:");
        }

    }

    public void createBanHistoryGUI(Player viewer, UUID targetUUID) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.DARK_PURPLE + Bukkit.getOfflinePlayer(targetUUID).getName() + "'s History" + "||" + targetUUID.toString());

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.WHITE + "Go Back");
        backMeta.setLore(Arrays.asList("Click to return to main menu"));
        backButton.setItemMeta(backMeta);
        gui.setItem(4, backButton);

        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                gui.setItem(i, border);
            }
            gui.setItem(i + 45, border);
        }

        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);
        List<Punishment> historicalPunishments = databaseManager.getPunishmentHistory(targetUUID);
        List<Punishment> allPunishments = new ArrayList<>();
        allPunishments.addAll(activePunishments);
        allPunishments.addAll(historicalPunishments);

        allPunishments.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

        int slot = 9;
        for (Punishment punishment : allPunishments) {
            if (punishment.getType().equalsIgnoreCase("BAN") ||
                    punishment.getType().equalsIgnoreCase("TBAN")) {
                if (slot >= 45) break;
                boolean isActive = activePunishments.contains(punishment);
                ItemStack item = createPunishmentItem(punishment, isActive);
                gui.setItem(slot, item);
                slot++;
            }
        }

        viewer.openInventory(gui);
    }

    public void createMuteHistoryGUI(Player viewer, UUID targetUUID) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.DARK_PURPLE + Bukkit.getOfflinePlayer(targetUUID).getName() + "'s History" + "||" + targetUUID.toString());

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.WHITE + "Go Back");
        backMeta.setLore(Arrays.asList("Click to return to main menu"));
        backButton.setItemMeta(backMeta);
        gui.setItem(4, backButton);

        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                gui.setItem(i, border);
            }
            gui.setItem(i + 45, border);
        }

        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);
        List<Punishment> historicalPunishments = databaseManager.getPunishmentHistory(targetUUID);
        List<Punishment> allPunishments = new ArrayList<>();
        allPunishments.addAll(activePunishments);
        allPunishments.addAll(historicalPunishments);

        allPunishments.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

        int slot = 9;
        for (Punishment punishment : allPunishments) {
            if (punishment.getType().equalsIgnoreCase("MUTE") ||
                    punishment.getType().equalsIgnoreCase("TMUTE")) {
                if (slot >= 45) break;
                boolean isActive = activePunishments.contains(punishment);
                ItemStack item = createPunishmentItem(punishment, isActive);
                gui.setItem(slot, item);
                slot++;
            }
        }

        viewer.openInventory(gui);
    }

    private ItemStack createPunishmentItem(Punishment punishment, boolean active) {
        ItemStack item = new ItemStack(active ? Material.GREEN_CONCRETE : Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + punishment.getType());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.LIGHT_PURPLE + "Reason: " + ChatColor.WHITE + punishment.getReason());
        lore.add(ChatColor.LIGHT_PURPLE + "Issued By: " + ChatColor.WHITE + punishment.getIssuedByName());
        lore.add(ChatColor.LIGHT_PURPLE + "Issued At: " + ChatColor.WHITE + DateUtil.formatDate(punishment.getTimestamp()));

        if (!punishment.getType().equalsIgnoreCase("WARN")) {
            lore.add(ChatColor.LIGHT_PURPLE + "Duration: " + ChatColor.WHITE + punishment.getDurationText());
            if (punishment.getExpirationTime() > 0) {
                lore.add(ChatColor.LIGHT_PURPLE + "Expires In: " + ChatColor.WHITE + formatRemainingTime(punishment.getExpirationTime()));
            }
        }
        if (active) {
            lore.add("");
            lore.add(ChatColor.RED + "Right-click to pardon");
        }


        if (punishment.getUnbannedByName() != null && !punishment.getType().equalsIgnoreCase("WARN")) {
            if (punishment.getType().equalsIgnoreCase("MUTE") || punishment.getType().equalsIgnoreCase("TMUTE")) {
                lore.add(ChatColor.LIGHT_PURPLE + "Unmuted By: " + ChatColor.WHITE + punishment.getUnbannedByName());
                lore.add(ChatColor.LIGHT_PURPLE + "UnmuteCommand Reason: " + ChatColor.WHITE + punishment.getUnbanReason());
                lore.add(ChatColor.LIGHT_PURPLE + "Unmuted At: " + ChatColor.WHITE + DateUtil.formatDate(punishment.getUnbanTimestamp()));
            } else {
                lore.add(ChatColor.LIGHT_PURPLE + "Unbanned By: " + ChatColor.WHITE + punishment.getUnbannedByName());
                lore.add(ChatColor.LIGHT_PURPLE + "UnbanCommand Reason: " + ChatColor.WHITE + punishment.getUnbanReason());
                lore.add(ChatColor.LIGHT_PURPLE + "Unbanned At: " + ChatColor.WHITE + DateUtil.formatDate(punishment.getUnbanTimestamp()));
            }
        } else if (!active && !punishment.getType().equalsIgnoreCase("WARN")) {
            lore.add(ChatColor.LIGHT_PURPLE + "Punishment Status: " + ChatColor.WHITE + "Expired");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void openMainMenu(Player player, String targetName, UUID targetUUID) {
        Inventory inv = Bukkit.createInventory(null, 27,
                ChatColor.DARK_PURPLE + targetName + "'s History" + "||" + targetUUID.toString());
        ItemStack filler = createMenuItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        ItemStack bans = createMenuItem(Material.RED_CONCRETE, "Bans", "Click to view ban history");
        ItemStack mutes = createMenuItem(Material.YELLOW_CONCRETE, "Mutes", "Click to view mute history");
        ItemStack warns = createMenuItem(Material.ORANGE_CONCRETE, "Warns", "Click to view warn history");

        inv.setItem(11, bans);
        inv.setItem(13, mutes);
        inv.setItem(15, warns);

        player.openInventory(inv);
    }

    public void createWarnHistoryGUI(Player viewer, UUID targetUUID) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.DARK_PURPLE + Bukkit.getOfflinePlayer(targetUUID).getName() + "'s History" + "||" + targetUUID.toString());

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.WHITE + "Go Back");
        backMeta.setLore(Arrays.asList("Click to return to main menu"));
        backButton.setItemMeta(backMeta);
        gui.setItem(4, backButton);

        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                gui.setItem(i, border);
            }
            gui.setItem(i + 45, border);
        }

        List<Punishment> activePunishments = databaseManager.getActivePunishments(targetUUID);
        List<Punishment> historicalPunishments = databaseManager.getPunishmentHistory(targetUUID);
        List<Punishment> allPunishments = new ArrayList<>();
        allPunishments.addAll(activePunishments);
        allPunishments.addAll(historicalPunishments);

        allPunishments.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

        int slot = 9;
        for (Punishment punishment : allPunishments) {
            if (punishment.getType().equalsIgnoreCase("WARN")) {
                if (slot >= 45) break;
                boolean isActive = activePunishments.contains(punishment);
                ItemStack item = createPunishmentItem(punishment, isActive);
                gui.setItem(slot, item);
                slot++;
            }
        }

        viewer.openInventory(gui);
    }

    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PunishmentPardonData pardonData = pendingPardons.remove(player.getUniqueId());
        if (pardonData != null) {
            event.setCancelled(true);
            String reason = event.getMessage();
            Punishment punishment = databaseManager.getPunishment(pardonData.targetUUID);
            if (punishment != null && punishment.getType().equalsIgnoreCase(pardonData.punishmentType)) {
                LogUtil.debug("Punishment found for " + pardonData.targetUUID + " with reason: " + reason);

                databaseManager.archivePunishment(
                        punishment,
                        Bukkit.getOfflinePlayer(pardonData.targetUUID).getName(),
                        player.getUniqueId(),
                        player.getName(),
                        reason,
                        pardonData.punishmentType
                );
                String targetName = Bukkit.getOfflinePlayer(pardonData.targetUUID).getName();
                String unmutedByName = player.getName();
                databaseManager.removePunishment(pardonData.targetUUID, pardonData.punishmentType.toLowerCase(), targetName);
                String actionType = pardonData.punishmentType.contains("ban") ? "unbanned" : "unmuted";
                String unmuteMessage = (MessageUtil.getColoredMessage("messages.player-unmuted",
                        "&7(Silent) &d%player%&f has been unmuted by &d%unmuter%&f Reason: &d%reason%",
                        "%player%", targetName,
                        "%unmuter%", unmutedByName,
                        "%reason%", reason));
                String tbanMessage = (MessageUtil.getColoredMessage("messages.player-unbanned",
                        "&7(Silent) &d%player%&f has been unbanned by &d%unbanner%&f Reason: &d%reason%",
                        "%player%", targetName,
                        "%unbanner%", unmutedByName,
                        "%reason%", reason));
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("elitebans.command.base")) {
                        if(actionType.contains("ban")) {
                            players.sendMessage(tbanMessage);
                        } else {
                            players.sendMessage(unmuteMessage);
                        }
                    }
                }
            } else {
                LogUtil.debug("Punishment not found for " + pardonData.targetUUID);
            }
        }
    }


}