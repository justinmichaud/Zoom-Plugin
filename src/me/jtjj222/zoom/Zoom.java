package me.jtjj222.zoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Zoom extends JavaPlugin implements Listener {
	Material magicItem = Material.SPIDER_EYE;
	boolean leftMouseButton;
	boolean useOtherButton;
	boolean cancelInteract;
	boolean zoomOutOnDamage;
	String message;
	
	//Has all the user-defined zoom levels
	public List<Integer> zoomLevels = new ArrayList<Integer>();
	
	//Stores <playername, number of times they have clicked>
	//If they click, they zoom to the zoomLevels[click_count].
	public HashMap<String, Integer> playersZoomedIn = new HashMap<String, Integer>();
		
	public void onEnable() {
		//Save default configuration
		this.saveDefaultConfig();
		//Register events
		this.getServer().getPluginManager().registerEvents(this, this);
		//Get material from config
		String configMagicItem = this.getConfig().getString("MagicItem");
		
		Material m = Material.getMaterial(configMagicItem);
		if (m == null) {
			try {
				//ID support
				m = Material.getMaterial(Integer.parseInt(configMagicItem));
				if(m == null) throw new Exception(); //Throws exception, goes to error message.
			} catch (Exception e) {
				getLogger().log(Level.INFO, "Could not find item " + configMagicItem + ". Using SPIDER_EYE instead.");
				m = Material.SPIDER_EYE;
			}
		}
		magicItem = m;
		//Get mouse settings		
		String configLeftMouseButton = this.getConfig().getString("Mouse_Button");
		if (configLeftMouseButton.equalsIgnoreCase("left")) leftMouseButton = true;
		else if (configLeftMouseButton.equalsIgnoreCase("right")) leftMouseButton = false;
		else {
			getLogger().log(Level.INFO, "Could not find mouse button " + configLeftMouseButton + ". Using left instead.");
			leftMouseButton = true;
		}
		//Use other button?
		useOtherButton = getConfig().getBoolean("Use_Other_Button");
		//Interact options
		cancelInteract = getConfig().getBoolean("Cancel_Interact");
		//Damage options
		zoomOutOnDamage = getConfig().getBoolean("Zoom_Out_On_Damage");
		//Load zoom levels
		zoomLevels = new ArrayList<Integer>();
		
		for(Integer i : getConfig().getIntegerList("Zoom_Levels")) {
			if(i >= 0 && i <= 12) zoomLevels.add(i);
		}
		
		//Schedule checking tasks
		getServer().getScheduler().runTaskTimerAsynchronously(this, new CheckTask(this), 0, 10000);
		
		//Message
		message = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message"));
		
		getLogger().info("Version " + getDescription().getVersion() + " enabled!");
	}
	@Override
	public void onDisable() {
		getLogger().info("Stopping tasks...");
		getServer().getScheduler().cancelTasks(this);
		
		getLogger().info("Version " + getDescription().getVersion() + " disabled.");
	}
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerDamagedByEntity (EntityDamageEvent e) {
		if (!zoomOutOnDamage || playersZoomedIn.isEmpty()) return;
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (playersZoomedIn.containsKey(p.getName())) {
				setZoomLevel(p, 0);
			}
		}
	}

	public void onPlayerQuit(PlayerQuitEvent e) {
		setZoomLevel(e.getPlayer(), 0);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Action zoomInAirAction = leftMouseButton ? Action.LEFT_CLICK_AIR : Action.RIGHT_CLICK_AIR;
		Action zoomInBlockAction = leftMouseButton ? Action.LEFT_CLICK_BLOCK : Action.RIGHT_CLICK_BLOCK;
		Action zoomOutAirAction = leftMouseButton ? Action.RIGHT_CLICK_AIR : Action.LEFT_CLICK_AIR;
		Action zoomOutBlockAction = leftMouseButton ? Action.RIGHT_CLICK_BLOCK : Action.LEFT_CLICK_BLOCK;
		
		if (e.getMaterial() == magicItem) {
			if (e.getAction() == zoomInAirAction || e.getAction() == zoomInBlockAction) {
				//Zoom in!
				if (playersZoomedIn.containsKey(e.getPlayer().getName())) {
					//Consequent zoom
					setZoomLevel(e.getPlayer(), playersZoomedIn.get(e.getPlayer().getName()) + 1);
				} else {
					//First zoom
					setZoomLevel(e.getPlayer(), 1);
				}
				if(cancelInteract) e.setCancelled(true);
			} else if(useOtherButton && (e.getAction() == zoomOutAirAction || e.getAction() == zoomOutBlockAction)) {
				//Zoom out!
				if (playersZoomedIn.containsKey(e.getPlayer().getName())) {
					//Consequent zoom
					setZoomLevel(e.getPlayer(), playersZoomedIn.get(e.getPlayer().getName()) - 1);
				} else {
					//Zoom back to maximum level
					setZoomLevel(e.getPlayer(), zoomLevels.size());
				}
				if(cancelInteract) e.setCancelled(true);
			}
		}
	}
	void setZoomLevel(Player p, Integer clicks) {
		setZoomLevel(p, clicks, true);
	}
	void setZoomLevel(Player p, Integer clicks, boolean b) {
		if(p == null) return;
		
		if(clicks < 0) clicks = zoomLevels.size();
		
		if(clicks == 0 || clicks > zoomLevels.size()) {
			clicks = 0;
			p.removePotionEffect(PotionEffectType.SLOW);
			if(playersZoomedIn.containsKey(p.getName())) playersZoomedIn.remove(p.getName());
		} else {
			p.removePotionEffect(PotionEffectType.SLOW);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20000, zoomLevels.get(clicks - 1)));
			playersZoomedIn.put(p.getName(), clicks);
		}
		
		int zoomLevel = (clicks == 0 ? 0 : zoomLevels.get(clicks - 1));
		
		if(!message.equals("") && b) p.sendMessage(message.replace("%level%", "" + zoomLevel));
	}
}
