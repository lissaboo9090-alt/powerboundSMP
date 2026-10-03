package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MinerPowerListener implements Listener {

    public MinerPowerListener(org.bukkit.plugin.Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getScoreboardTags().contains("miner")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 2, false, false, false));
                }
            }
        }, 0L, 40L);
    }

    @EventHandler
    public void onMinerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getScoreboardTags().contains("miner")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 2, false, false, false));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("miner")) return;

        Block brokenBlock = event.getBlock();
        int oreCount = 0;
        int radius = 2; 
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block nearbyBlock = brokenBlock.getRelative(x, y, z);
                    if (nearbyBlock.getType().name().contains("ORE")) {
                        oreCount++;
                    }
                }
            }
        }

        if (oreCount > 0) {
            player.sendMessage(ChatColor.GOLD + " [Ore Radar] Found " + ChatColor.YELLOW + oreCount + ChatColor.GOLD + " nearby ores!");
        } else {
            player.sendMessage(ChatColor.GRAY + " [Ore Radar] No ores detected nearby.");
        }
    }
}
