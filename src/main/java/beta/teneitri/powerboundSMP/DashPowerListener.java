package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class DashPowerListener implements Listener {

    private final HashMap<UUID, Long> dashCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 3000;

    @EventHandler
    public void onDashKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("dash")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (dashCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = dashCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.AQUA + " Dash is on cooldown for " + secondsLeft + "s!");
                return;
            }
        }

        Vector direction = player.getLocation().getDirection();
        Vector dashVelocity = direction.multiply(1.8).setY(0.2);
        player.setVelocity(dashVelocity);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);

        player.sendMessage(ChatColor.AQUA + " DASH!");
        dashCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
