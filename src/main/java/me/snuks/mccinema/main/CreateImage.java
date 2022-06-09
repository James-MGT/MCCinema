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
import java.util.ArrayList;

public class CreateImage implements CommandExecutor {

    private MCCinema plugin;
    private ArrayList<MapView> mapViews = new ArrayList<>();

    public CreateImage(MCCinema plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        Player player = (Player) sender;

        int width = 0;
        int height = 0;
        int extraWidth = 0;
        int extraHeight = 0;
        String urlLink = "";

        if(args.length != 1) {
            player.sendMessage(ChatColor.RED + "Incorrect syntax. Usage: /createimage [url]");
            return true;
        }

        urlLink = args[0];

        // Retrieve image from URL
        ImageIcon imageIcon = getImage(urlLink);

        // Calculate rows and columns of image and excess image in both axis
        width = (int) Math.ceil(imageIcon.getIconWidth() / 128f);
        height = (int) Math.ceil(imageIcon.getIconHeight() / 128f);
        extraWidth = (int) Math.ceil(imageIcon.getIconWidth() % 128f);
        extraHeight = (int) Math.ceil(imageIcon.getIconHeight() % 128f);

        player.sendMessage("Creating " + ChatColor.GREEN + width + "x" + height + ChatColor.WHITE + " screen with URL: " + ChatColor.GREEN + urlLink);

        // Get corner block
        Block cornerBlock = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);

        // Create screen and store each frame
        ItemFrame[] itemFrames = createScreen(width, height, cornerBlock, player.getWorld());

        // Sub images
        Image[][] subImages = new Image[width][height];

        // Splice parent image into 2D array of sub images of calculated width and height
        subImages = spliceImages(imageIcon, subImages, width, height, extraWidth, extraHeight);

        int count = 0;

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                itemFrames[count].setItem(createMap(player, subImages[i][j]));
                count++;
            }
        }

        return true;
    }

    public Image[][] spliceImages(ImageIcon parentImage, Image[][] subImages, int width, int height, int extraWidth, int extraHeight) {

        // This feels stupid but I cba to fix it - painting ImageIcon onto BufferedImage (only once tho)
        BufferedImage bufferedImage = new BufferedImage(parentImage.getIconWidth(), parentImage.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        parentImage.paintIcon(null, g, 0,0);
        g.dispose();

        // Account for extra parts of image that do not fill up a full 128x128 sub image, or it complains
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(i == width - 1 && j != height - 1) {
                    subImages[i][j] = bufferedImage.getSubimage(i * 128, j * 128, extraWidth, 128);
                }else if(i == width - 1 && j == height - 1) {
                    subImages[i][j] = bufferedImage.getSubimage(i * 128, j * 128, extraWidth, extraHeight);
                }else if(i != width - 1 && j == height - 1) {
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

    public ItemStack createMap(Player player, Image image) {

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

        // Add a custom MapRenderer
        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView map, MapCanvas canvas, Player player) {

                // Optimisation - only render once
                if(mapViews.contains(map)) {
                    return;
                }

                mapViews.add(map);

                // Black background until I center the image so it looks nicer
                for(int i = 0; i < 128; i++) {
                    for(int j = 0; j < 128; j++) {
                        canvas.setPixel(i, j, MapPalette.matchColor(0, 0, 0));
                    }
                }

                // Sub image
                canvas.drawImage(0, 0, image);
            }
        });

        // Set MapMeta's MapView to custom MapView
        mapMeta.setMapView(mapView);

        // Set new map's MapMeta to custom MapMeta
        map.setItemMeta(mapMeta);

        // Return custom map
        return map;
    }

    public ItemFrame[] createScreen(int width, int height, Block cornerBlock, World world) {

        ItemFrame[] itemFrames = new ItemFrame[width * height];
        int indexCounter = 0;

        // Fix this for different player facings
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                itemFrames[indexCounter] = (ItemFrame) world.spawnEntity(cornerBlock.getLocation().add(i, -j, 1), EntityType.ITEM_FRAME);
                indexCounter++;
            }
        }

        return itemFrames;
    }

}
