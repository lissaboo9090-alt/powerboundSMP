package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.UUID;

public class OmnipresencePowerListener implements Listener {

    private final HashMap<UUID, Long> omnipresenceCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 20000;

    @EventHandler
    public void onOmnipresenceKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("omnipresence")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (omnipresenceCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = omnipresenceCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.DARK_AQUA + " Omnipresence is on cooldown! ready in " + secondsLeft + "s");
                return;
            }
        }

        RayTraceResult rayTrace = player.getWorld().rayTrace(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                500.0,
                FluidCollisionMode.NEVER,
                true,
                0.0,
                entity -> !entity.equals(player)
        );

        Location targetLocation = null;

        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            Block hitBlock = rayTrace.getHitBlock();
            targetLocation = hitBlock.getLocation().add(0.5, 1.0, 0.5);
            targetLocation.setYaw(player.getLocation().getYaw());
            targetLocation.setPitch(player.getLocation().getPitch());
        }

        if (targetLocation != null) {
            Location originLocation = player.getLocation();

            originLocation.getWorld().playSound(originLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            originLocation.getWorld().spawnParticle(Particle.PORTAL, originLocation.add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.5);

            player.teleport(targetLocation);

            targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            targetLocation.getWorld().spawnParticle(Particle.DRAGON_BREATH, targetLocation.add(0, 1, 0), 40, 0.4, 0.5, 0.4, 0.1);

            player.sendMessage(ChatColor.DARK_AQUA + " OMNIPRESENCE: You have been teleported!!!");
            omnipresenceCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "You must look at a valid surface to warp there.");
        }
    }
}
