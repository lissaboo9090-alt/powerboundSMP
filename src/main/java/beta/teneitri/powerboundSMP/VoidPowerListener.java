package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class VoidPowerListener implements Listener {

    private final HashMap<UUID, Long> riftCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 25000; 
    @EventHandler
    public void onBedrockMine(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("void")) return;
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.BEDROCK) {
                if (player.getInventory().getItemInMainHand().getType().name().contains("PICKAXE")) {
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.BEDROCK, 1));
                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);
                    block.getWorld().spawnParticle(Particle.SQUID_INK, block.getLocation().add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0.02);
                }
            }
        }
    }

    @EventHandler
    public void onVoidKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("void")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (riftCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = riftCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.DARK_PURPLE + " Void Rift is on cool down! Wait " + secondsLeft + "s");
                return;
            }
        }

        double radius = 6.0;
        boolean openedRift = false;
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.8f, 0.5f);

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity target = (LivingEntity) entity;
                openedRift = true;

                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false, true));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1, false, false, true));

                Location lowerLoc = target.getLocation().add(0, -4, 0);
                target.teleport(lowerLoc);
                target.getWorld().spawnParticle(Particle.SQUID_INK, target.getLocation().add(0, 1, 0), 25, 0.3, 0.5, 0.3, 0.05);

                if (target instanceof Player) {
                    ((Player) target).sendMessage(ChatColor.DARK_PURPLE + "You were dragged downward by a spatial Void Rift!");
                }
            }
        }

        if (openedRift) {
            player.sendMessage(ChatColor.DARK_PURPLE + "VOID RIFT: You ruptured space and pulled enemies down!");
            player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 35, 0.5, 0.2, 0.5, 0.02);
            riftCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "The void opened, but nothing was close enough to fall in.");
        }
    }
}
