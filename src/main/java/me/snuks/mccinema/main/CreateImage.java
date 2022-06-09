package me.snuks.mccinema.main;

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
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

public class CreateImage implements CommandExecutor {

    private MCCinema plugin;

    public CreateImage(MCCinema plugin) {
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
        int width = 0;
        int height = 0;
        int extraWidth = 0;
        int extraHeight = 0;
        String urlLink = "";

        // Syntax validation
        if(args.length != 1) {
            player.sendMessage(ChatColor.RED + "Incorrect syntax. Usage: /createscreen [url]");
            return true;
        }

        // Set width, height and URL parameters based on number of arguments provided
        urlLink = args[0];

        // Retrieve image from URL
        ImageIcon imageIcon = getImage(urlLink);

        // Calculate rows and columns of image
        width = (int) Math.ceil(imageIcon.getIconWidth() / 128f);
        height = (int) Math.ceil(imageIcon.getIconHeight() / 128f);
        extraWidth = (int) Math.ceil(imageIcon.getIconWidth() % 128f);
        extraHeight = (int) Math.ceil(imageIcon.getIconHeight() % 128f);

        // Player feedback message
        player.sendMessage("Creating " + ChatColor.GREEN + width + "x" + height + ChatColor.WHITE + " screen with URL: " + ChatColor.GREEN + urlLink);

        // Get corner block
        Block cornerBlock = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);

        // Create screen and store each frame
        ItemFrame[] itemFrames = createScreen(width, height, cornerBlock, player.getWorld());

        // 2D array of sub images
        Image[][] subImages = new Image[width][height];

        // Splice parent image into 2D array of sub images of calculated width and height
        subImages = spliceImages(imageIcon, subImages, width, height, extraWidth, extraHeight);

        int count = 0;

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                itemFrames[count].setItem(createMap(player, subImages[i][j], 0, 0));
                count++;
            }
        }

        return true;
    }

    public Image[][] spliceImages(ImageIcon parentImage, Image[][] subImages, int width, int height, int extraWidth, int extraHeight) {

        BufferedImage bufferedImage = new BufferedImage(parentImage.getIconWidth(), parentImage.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        parentImage.paintIcon(null, g, 0,0);
        g.dispose();

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(i == width - 1 && j != height - 1) {
                    subImages[i][j] = bufferedImage.getSubimage(i * 128, j * 128, extraWidth, 128);
                }else if(i == width - 1 && j == height - 1) {
                    subImages[i][j] = bufferedImage.getSubimage(i * 128, j * 128, extraWidth, extraHeight);
                }else if(j == height - 1 && i != width - 1) {
                    subImages[i][j] = bufferedImage.getSubimage(i * 128, j * 128, 128, extraHeight);
                }else{
                    subImages[i][j] = bufferedImage.getSubimage(i * 128, j * 128, 128, 128);
                }
            }
        }

        return subImages;
    }

    public ImageIcon getImage(String urlLink) {

        ImageIcon imageIcon;

        try {
            imageIcon = new ImageIcon(new URL(urlLink));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return imageIcon;
    }

    public ItemStack createMap(Player player, Image image, int x, int y) {

        // Create map ItemStack
        ItemStack map = new ItemStack(Material.FILLED_MAP);

        // Get MapMeta from MapView
        MapMeta mapMeta = (MapMeta) map.getItemMeta();

        // Get MapView from new Map
        MapView mapView = Bukkit.createMap(player.getWorld());

        // Configure MapView
        mapView.setScale(MapView.Scale.CLOSEST);

        // Clear renderers from current MapView
        mapView.getRenderers().clear();

        // Add a custom MapRendered
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView map, MapCanvas canvas, Player player) {
                for(int i = 0; i < 128; i++) {
                    for(int j = 0; j < 128; j++) {
                        canvas.setPixel(i, j, MapPalette.matchColor(0, 0, 0));
                    }
                }
                canvas.drawImage(0, 0, image);
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

}
