package me.snuks.mccinema.mccinema;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class CreateScreen implements CommandExecutor {

    private MCCinema plugin;
    public CreateScreen(MCCinema plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Check the command sender is a player and not the console
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        // Safely cast the sender to a Player object
        Player player = (Player) sender;

        // Default width and height values create 4x3 screen
        int width = 4;
        int height = 3;
        String urlLink = "";

        // Syntax validation
        if(args.length != 1 && args.length != 3) {
            player.sendMessage(ChatColor.RED + "Incorrect syntax. Usage: /createscreen <width> <height> [url]");
            return true;
        }

        // Set width, height and URL parameters based on number of arguments provided
        if(args.length == 1) {
            urlLink = args[0];
        }else{
            width = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
            urlLink = args[2];
        }

        // Player feedback message
        player.sendMessage("Creating " + ChatColor.GREEN + width + "x" + height + ChatColor.WHITE + " screen with URL: " + ChatColor.GREEN + urlLink);

        // Get corner block
        Block cornerBlock = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);

        // Create screen and store each frame
        ItemFrame[] itemFrames = createScreen(width, height, cornerBlock, player.getWorld());

        for(int i = 0; i < itemFrames.length; i++) {
            itemFrames[i].setItem(createMap(player, urlLink));
        }

        return true;
    }

    public Image getImage(String urlLink) {

        ImageIcon imageIcon = null;
        try {
            imageIcon = new ImageIcon(new URL(urlLink));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Image image = imageIcon.getImage();

        return imageIcon.getImage();
    }

    public ItemStack createMap(Player player, String link) {

        // Create map ItemStack
        ItemStack map = new ItemStack(Material.FILLED_MAP);

        // Get MapMeta from MapView
        MapMeta mapMeta = (MapMeta) map.getItemMeta();

        // Get MapView from MapMeta
        MapView mapView = Bukkit.createMap(player.getWorld());

        // Configure MapView
        mapView.setScale(MapView.Scale.CLOSEST);

        // Clear renderers from current MapView
        mapView.getRenderers().clear();

        // Add a custom MapRendered
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView map, MapCanvas canvas, Player player) {
                if(getImage(link) != null) {
                    canvas.drawImage(0, 0, getImage(link));
                }else{
                    player.sendMessage(ChatColor.RED + "Invalid image URL.");
                }
            }
        });

        // Set MapMeta to custom MapView
        mapMeta.setMapView(mapView);

        // Set new map's MapMeta to custom MapMeta
        map.setItemMeta(mapMeta);

        // Return custom map
        return map;
    }

    public ItemFrame[] createScreen(int width, int height, Block cornerBlock, World world) {

        ItemFrame[] itemFrames = new ItemFrame[width * height];
        int indexCounter = 0;

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                itemFrames[indexCounter] = (ItemFrame) world.spawnEntity(cornerBlock.getLocation().add(i, -j, 1), EntityType.ITEM_FRAME);
                indexCounter++;
            }
        }

        return itemFrames;
    }

    // Just messing around - really laggy
    public void loopColors(ItemFrame[] itemFrames) {

        Random random = new Random();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                int r = random.nextInt(255);
                int g = random.nextInt(255);
                int b = random.nextInt(255);
                for(int i = 0; i < itemFrames.length; i++) {
                    if(itemFrames[i] != null) {
                        MapMeta mapMeta = (MapMeta) itemFrames[i].getItem().getItemMeta();
                        MapView mapView = mapMeta.getMapView();
                        mapView.getRenderers().clear();
                        updateRenderer(mapView, r, g, b);
                        mapMeta.setMapView(mapView);
                        itemFrames[i].getItem().setItemMeta(mapMeta);
                    }
                }

            }
        }, 0, 20);

    }

    public MapView updateRenderer(MapView mapView, int r, int g, int b) {

        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView map, MapCanvas canvas, Player player) {
                for(int i = 0; i < 128; i++) {
                    for(int j = 0; j < 128; j++) {
                        canvas.setPixel(i, j, MapPalette.matchColor(new Color(r, g, b)));
                    }
                }
            }
        });

        return mapView;
    }

}
