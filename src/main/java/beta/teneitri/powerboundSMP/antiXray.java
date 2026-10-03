package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.UUID;

public class antiXray implements Listener {

    private final HashMap<UUID, Integer> diamondTracker = new HashMap<>();
    private final HashMap<UUID, Integer> netheriteTracker = new HashMap<>();
    private final HashMap<UUID, Long> timeWindows = new HashMap<>();

    private final long ONE_MINUTE_MS = 60000; 
    private final int ALERT_THRESHOLD = 5; 

    @EventHandler
    public void onOreMine(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (player.getScoreboardTags().contains("miner")) return;

        boolean isDiamond = blockType == Material.DIAMOND_ORE || blockType == Material.DEEPSLATE_DIAMOND_ORE;
        boolean isNetherite = blockType == Material.ANCIENT_DEBRIS;

        if (!isDiamond && !isNetherite) return;

        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (!timeWindows.containsKey(uuid) || (currentTime - timeWindows.get(uuid) > ONE_MINUTE_MS)) {
            timeWindows.put(uuid, currentTime);
            diamondTracker.put(uuid, 0);
            netheriteTracker.put(uuid, 0);
        }

        int currentDiamonds = diamondTracker.getOrDefault(uuid, 0);
        int currentNetherite = netheriteTracker.getOrDefault(uuid, 0);

        if (isDiamond) {
            currentDiamonds++;
            diamondTracker.put(uuid, currentDiamonds);
            if (currentDiamonds > ALERT_THRESHOLD) {
                triggerAdminAlert(player, "DIAMOND", currentDiamonds);
            }
        } else {
            currentNetherite++;
            netheriteTracker.put(uuid, currentNetherite);
            if (currentNetherite > ALERT_THRESHOLD) {
                triggerAdminAlert(player, "ANCIENT DEBRIS", currentNetherite);
            }
        }
    }

    private void triggerAdminAlert(Player suspect, String oreName, int count) {
        String alertMessage = ChatColor.RED + "[Anti-Xray] " + ChatColor.YELLOW + suspect.getName() 
                + ChatColor.RED + " has mined " + ChatColor.GOLD + count + " " + oreName + ChatColor.RED + " ores in under 60 seconds!";
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.isOp() || onlinePlayer.hasPermission("smp.admin.alerts")) {
                onlinePlayer.sendMessage(alertMessage);
            }
        }
        
        Bukkit.getLogger().warning("[Anti-Xray] ALERT: " + suspect.getName() + " mined " + count + " " + oreName + " in under a minute.");
    }
}
