package com.murphyy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DonateMenu extends JavaPlugin implements Listener {

    private List<ItemStack> items;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        items = (List<ItemStack>) config.getList("items");
        if (items == null) items = new ArrayList<>();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack clickedItem = event.getItem();

        if (clickedItem != null && clickedItem.getType() == Material.EMERALD && clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Donate Menu")) {
            event.setCancelled(true);
            openDonateMenu(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getView().getTitle().equalsIgnoreCase("Donate Menu")) {
            event.setCancelled(true);

            final Player finalPlayer = player;

            if (clickedItem != null && clickedItem.getType() == Material.LIME_WOOL && clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Add")) {
                finalPlayer.closeInventory();
                finalPlayer.sendMessage(ChatColor.GREEN + "Click on an item in your inventory to add it to the Donate Menu.");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        finalPlayer.setMetadata("donate_add", new FixedMetadataValue(DonateMenu.this, true));
                    }
                }.runTaskLater(this, 1L);
            } else if (clickedItem != null && clickedItem.getType() == Material.RED_WOOL && clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Remove")) {
                finalPlayer.sendMessage(ChatColor.RED + "Click on an item in the Donate Menu to remove it.");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        finalPlayer.setMetadata("donate_remove", new FixedMetadataValue(DonateMenu.this, true));
                    }
                }.runTaskLater(this, 1L);
            } else {
                if (event.getCurrentItem() != null && finalPlayer.hasMetadata("donate_remove") && !event.getCurrentItem().getType().equals(Material.GRAY_STAINED_GLASS_PANE)) {
                    removeDonateItem(event.getSlot());
                    finalPlayer.closeInventory();
                    openDonateMenu(finalPlayer);
                    finalPlayer.removeMetadata("donate_remove", this);
                }
            }
        } else if (player.hasMetadata("donate_add")) {
            event.setCancelled(true);
            player.removeMetadata("donate_add", this);
    
            if (clickedItem != null) {
                addDonateItem(items.size(), clickedItem.clone());
                player.closeInventory();
                openDonateMenu(player);
            }
        }
    }
    
    private void openDonateMenu(Player player) {
        Inventory donateMenu;
        if (player.isOp()) donateMenu = Bukkit.createInventory(null, 54, "Donate Menu");
        else donateMenu = Bukkit.createInventory(null, 45, "Donate Menu");
    
        for (int i = 0; i < items.size(); i++) {
            donateMenu.setItem(i, items.get(i));
        }
    
        for (int i = 36; i < 45; i++) {
            donateMenu.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
    
        if (player.isOp()) {
            ItemStack addWool = new ItemStack(Material.LIME_WOOL);
            ItemMeta addWoolMeta = addWool.getItemMeta();
            addWoolMeta.setDisplayName(ChatColor.BOLD + "Add");
            addWool.setItemMeta(addWoolMeta);
            donateMenu.setItem(48, addWool);
    
            ItemStack removeWool = new ItemStack(Material.RED_WOOL);
            ItemMeta removeWoolMeta = removeWool.getItemMeta();
            removeWoolMeta.setDisplayName(ChatColor.BOLD + "Remove");
            removeWool.setItemMeta(removeWoolMeta);
            donateMenu.setItem(50, removeWool);
        }
    
        player.openInventory(donateMenu);
    }
    
    private void addDonateItem(int slot, ItemStack item) {
        if (slot >= items.size()) items.add(item);
        else items.set(slot, item);
    
        getConfig().set("items", items);
        saveConfig();
    }
    
    private void removeDonateItem(int slot) {
        if (slot < items.size()) {
            items.remove(slot);
            getConfig().set("items", items);
            saveConfig();
        }
    }
    
    @Override
    public void onDisable() {
        getConfig().set("items", items);
        saveConfig();
    }
}    