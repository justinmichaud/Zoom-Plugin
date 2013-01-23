package me.jtjj222.zoom;

public class CheckTask implements Runnable {
	private Zoom plugin;
	
	public CheckTask(Zoom p) {
		plugin = p;
	}
	
	@Override
	public void run() {
		if(!plugin.playersZoomedIn.isEmpty())
			for(Object p : plugin.playersZoomedIn.keySet().toArray())
				try {
					String name = (String) p;
					plugin.setZoomLevel(plugin.getServer().getPlayer(name), plugin.playersZoomedIn.get(name), false);
				} catch (Exception e) {} //Exception should never happen, but... better safe than sorry :)
	}
}
