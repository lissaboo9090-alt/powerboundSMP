package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class ArcherPowerListener implements Listener {

    private final Plugin plugin;
    private final HashMap<UUID, Long> grappleCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 7000;

    public ArcherPowerListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!player.getScoreboardTags().contains("archer")) return;
        if (!(event.getProjectile() instanceof Arrow)) return;

        Arrow originalArrow = (Arrow) event.getProjectile();
        Vector velocity = originalArrow.getVelocity();

        for (int i = -1; i <= 1; i++) {
            if (i == 0) continue;

            Arrow extraArrow = player.launchProjectile(Arrow.class);
            Vector rotatedVelocity = velocity.clone();
            double angle = Math.toRadians(i * 10);
            
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double x = rotatedVelocity.getX() * cos - rotatedVelocity.getZ() * sin;
            double z = rotatedVelocity.getX() * sin + rotatedVelocity.getZ() * cos;
            
            rotatedVelocity.setX(x);
            rotatedVelocity.setZ(z);
            
            extraArrow.setVelocity(rotatedVelocity);
            extraArrow.setShooter(player);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 0.5f);
    }

    @EventHandler
    public void onArcherKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("archer")) return;

        Material mainHand = player.getInventory().getItemInMainHand().getType();
        if (mainHand == Material.BOW || mainHand == Material.CROSSBOW) {
            event.setCancelled(true);

            long currentTime = System.currentTimeMillis();
            if (grappleCooldowns.containsKey(player.getUniqueId())) {
                long timeLeft = grappleCooldowns.get(player.getUniqueId()) - currentTime;
                if (timeLeft > 0) {
                    double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                    player.sendMessage(ChatColor.GREEN + "Grappling Arrow is on cooldown for " + secondsLeft + "s!");
                    return;
                }
            }

            Arrow grappleArrow = player.launchProjectile(Arrow.class);
            grappleArrow.setMetadata("grapple", new FixedMetadataValue(plugin, true));
            grappleArrow.setVelocity(player.getLocation().getDirection().multiply(2.5));

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1.0f, 1.5f);
            player.sendMessage(ChatColor.GREEN + "Grappling Arrow fired!");
            grappleCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();

        if (arrow.hasMetadata("grapple") && arrow.getShooter() instanceof Player) {
            Player player = (Player) arrow.getShooter();
            
            Location arrowLoc = arrow.getLocation();
            Location playerLoc = player.getLocation();

            Vector pullVector = arrowLoc.toVector().subtract(playerLoc.toVector());
            pullVector.normalize().multiply(1.7);
            pullVector.setY(pullVector.getY() + 0.3);

            player.setVelocity(pullVector);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.2f);
            arrow.remove();
        }
    }
}
