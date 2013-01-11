package me.jtjj222.zoom;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Zoom extends JavaPlugin implements Listener{
 	
	ItemStack magicItem;
	Boolean leftMouseButton;
	
	//Stores <playername,number of times they have clicked>
	//If they click once, zoom in a small amount. Twice, zoom in a bit more. ... 5 times, zoom out.
	public HashMap<String,Integer> playersZoomedIn = new HashMap<String,Integer>();
		
	public void onEnable() {
		
		this.saveDefaultConfig();
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		// init telescope --- comp500
		ItemStack telescope = new ItemStack(374, 1);
    		telescope.setData(new MaterialData(374));
    		ItemMeta im = telescope.getItemMeta();
    		im.setDisplayName("Telescope");
  		im.addEnchant(Enchantment.getById(34), 10, true);
    		telescope.setItemMeta(im);

    		ShapedRecipe tele = new ShapedRecipe(telescope);
    		tele.shape(new String[] { " SG", "S  ", "   " });
    		tele.setIngredient('G', Material.GLASS);
    		tele.setIngredient('S', Material.STICK);
    		getServer().addRecipe(tele);
		
		//String configMagicItem = this.getConfig().getString("MagicItem");
		magicItem = telescope;
		
		String configLeftMouseButton = this.getConfig().getString("Mouse_Button");
		if (configLeftMouseButton.contains("left")) leftMouseButton = true;
		else if (configLeftMouseButton.contains("right")) leftMouseButton = false;
		else {
			getLogger().log(Level.INFO, "Could not find mouse button " + configLeftMouseButton + ". Using right instead.");
			leftMouseButton = false;
		}
		
	}
	
	
	@EventHandler (priority=EventPriority.MONITOR)
	public void onPlayerDamagedByEntity (EntityDamageEvent e) {
		if (playersZoomedIn.isEmpty()) return;
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (playersZoomedIn.containsKey(p.getName())) {
				playersZoomedIn.remove(p.getName());
				removeZoom(p);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		Action airAction = leftMouseButton ? Action.LEFT_CLICK_AIR : Action.RIGHT_CLICK_AIR;
		Action blockAction = leftMouseButton ? Action.LEFT_CLICK_BLOCK : Action.RIGHT_CLICK_BLOCK;
		
		if (e.getAction() == airAction || e.getAction() == blockAction) {
			//getLogger().log(Level.INFO, "TheLog2");
			if (e.getItem().getType() == magicItem.getType()) {
				//getLogger().log(Level.INFO, "TheLog3");
								
				if (playersZoomedIn.containsKey(e.getPlayer().getName()) ) {
					
					//They have used the command before
					int timesClicked = playersZoomedIn.get(e.getPlayer().getName());
					
					if (timesClicked >= 4) {
						playersZoomedIn.remove(e.getPlayer().getName());
						removeZoom(e.getPlayer());
						e.setCancelled(true);
						return;
					}
					
					playersZoomedIn.put(e.getPlayer().getName(), timesClicked + 1);
					
					if (timesClicked == 3) zoom4(e.getPlayer());
					else if (timesClicked == 2) zoom3(e.getPlayer());
					else if (timesClicked == 1) zoom2(e.getPlayer());
					
				}
				
				else {
					
					playersZoomedIn.put(e.getPlayer().getName(), 1);
					zoom1(e.getPlayer());
				}
				e.setCancelled(true);
			}
		}	
	}
	
	public void zoom1(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,20000, 3));
	}
	
	public void zoom2(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,20000, 8));		
	}
	
	public void zoom3(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,20000, 10));		
	}
	
	public void zoom4(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,20000, 12));
	}	
	
	private void removeZoom(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);		
	}
}
