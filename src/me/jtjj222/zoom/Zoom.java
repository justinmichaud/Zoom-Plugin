package me.jtjj222.zoom;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	ItemStack telescope;
	Boolean leftMouseButton;
	
	//Stores <playername,number of times they have clicked>
	//If they click once, zoom in a small amount. Twice, zoom in a bit more. ... 5 times, zoom out.
	public HashMap<String,Integer> playersZoomedIn = new HashMap<String,Integer>();
		
	public void onEnable() {
		
		this.saveDefaultConfig();
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		// init magic item --- comp500
		String configMagicItem = this.getConfig().getString("MagicItem");
		Material mat = Material.getMaterial(configMagicItem.toUpperCase());
		Boolean isTelescope = false;
		if (!configMagicItem.equals("telescope") && mat == null){
			isTelescope = true;
			getLogger().log(Level.INFO, "Could not find item " + configMagicItem + ". Using Telescope instead.");
		} else {
			if (configMagicItem.equals("telescope")){
				isTelescope = true;
			}
		}
		if (isTelescope){
			initTelescope();
		} else {
			ItemStack stack = new ItemStack(mat, 1);
			magicItem = stack;
		}
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
		if (e.getPlayer().hasPermission("Zoom.zoom")){
			if (magicItem.equals(e.getItem())) {
				if (magicItem == telescope) {
					e.setCancelled(true);
				}
		if (e.getAction() == airAction || e.getAction() == blockAction) {
			//making them have one of it could be done here
				e.setCancelled(true);
				if (playersZoomedIn.containsKey(e.getPlayer().getName()) ) {
					//They have used the command before
					int timesClicked = playersZoomedIn.get(e.getPlayer().getName());
					
					if (timesClicked >= 4) {
						playersZoomedIn.remove(e.getPlayer().getName());
						removeZoom(e.getPlayer());
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
			}
		}	} else {e.getPlayer().sendMessage("You don't have permission!");}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("givezoomer") || cmd.getName().equalsIgnoreCase("givezoom") || cmd.getName().equalsIgnoreCase("zoom") || cmd.getName().equalsIgnoreCase("zoomer") || cmd.getName().equalsIgnoreCase("zoomr")){ // If the player typed /basic then do the following...
			if (!(sender instanceof Player)) {
				sender.sendMessage("[Zoom] This command can only be run by a player.");
			} else {
				Player player = (Player) sender;
				if (player.hasPermission("Zoom.command")){
				player.sendMessage("[Zoom] Giving you the zoomer...");
				player.getInventory().addItem(magicItem);
				} else {player.sendMessage("[Zoom] You don't have permission!");}
			}
			return true;
		}
		return false; 
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
	private void initTelescope(){ // Telescope addon
		telescope = new ItemStack(374, 1);
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
		magicItem = telescope;
	}
}
