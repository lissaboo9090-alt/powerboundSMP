package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class ServerProgressionListener implements Listener {

    public enum ProgressionStage {
        GRACE_PERIOD,    // 0 to 30 mins: No PvP, Only Leather/Gold/Chain
        IRON_AGE,        // 30 mins to 1.5 hours: PvP enabled, Iron Allowed
        DIAMOND_AGE,     // 1.5 hours to 3.5 hours: Diamond Allowed
        NETHERITE_AGE,   // 3.5 hours to 2 days: Netherite Allowed
        END_GAME         // After 2 days: Nether opens, full access
    }

    private ProgressionStage currentStage = ProgressionStage.GRACE_PERIOD;

    public void setServerStage(ProgressionStage stage) {
        this.currentStage = stage;
        Bukkit.broadcastMessage(ChatColor.GOLD + "=== SERVER PROGRESSION UPDATED ===");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "The server has transitioned to: " + ChatColor.RED + stage.name());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

        if (currentStage == ProgressionStage.GRACE_PERIOD) {
            event.setCancelled(true);
            event.getDamager().sendMessage(ChatColor.RED + " PvP is disabled during the 30-minute grace period!");
        }
    }

    @EventHandler
    public void onArmorCraft(CraftItemEvent event) {
        Material resultType = event.getRecipe().getResult().getType();
        String typeName = resultType.name();

        if (currentStage == ProgressionStage.GRACE_PERIOD && typeName.startsWith("IRON_")) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot craft Iron gear yet!");
            return;
        }

        if ((currentStage == ProgressionStage.GRACE_PERIOD || currentStage == ProgressionStage.IRON_AGE) 
                && typeName.startsWith("DIAMOND_")) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot craft Diamond gear yet!");
            return;
        }
        if (currentStage != ProgressionStage.NETHERITE_AGE && currentStage != ProgressionStage.END_GAME 
                && (typeName.startsWith("NETHERITE_") || resultType == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.RED + "Netherite alloy mechanics are currently locked!");
        }
    }

    @EventHandler
    public void onPortalTravel(PlayerPortalEvent event) {
        World.Environment targetDim = event.getTo().getWorld().getEnvironment();

        if (targetDim == World.Environment.NETHER && currentStage != ProgressionStage.END_GAME) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "The Nether dimension portal is sealed until Day 2!");
        }
    }
}
