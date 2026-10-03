package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class powerboundSMP extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("🔥 PowerboundSMP has initialized cleanly on a fresh, spotless slate!");

        // 1. Register custom progression crafting utilities
        netherite.registerNetheriteRecipe(this);
        mace.registerMaceRecipe(this);

        // 2. Register administrative commands
        this.getCommand("randomize").setExecutor(new RandomizeCommand());
        this.getCommand("joinmatch").setExecutor(new JoinMatchCommand());

        // 3. Register your active server antiXray alert watchdogs
        Bukkit.getPluginManager().registerEvents(new antiXray(), this);

        // 4. Register all your custom hero power listener class files
        Bukkit.getPluginManager().registerEvents(new ArcherPowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BeautifulStrangerPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new CheesecakePowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChronosPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChiwawaPowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DashPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new DreamerPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new FadeOutPowerListener(this), this);
        
        // FIXED: Removed the extra '(this)' block parameter so it perfectly compiles with your Slime code!
        Bukkit.getPluginManager().registerEvents(new SlimePowerListener(), this);
        
        Bukkit.getPluginManager().registerEvents(new FrostPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new GravityPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new LightPowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LightingPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new LockPotPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new MinerPowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PhoenixPowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RagdollPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ShadowPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new TankPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new TelepathyPowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TeleportPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new TimePowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VoidPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WitchPowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new VampirePowerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ParrotPowerListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("💤 PowerboundSMP has powered down safely.");
    }
}
