package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Tree {
    private int x, y;
    private int width, height;
    private boolean flipped; // Whether the tree is flipped horizontally
    private static BufferedImage treeImage;

    static {
        // Load tree image once for all Tree instances
        try (var is = Tree.class.getResourceAsStream("/tree.png")) {
            treeImage = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Tree(int x, int y, int width, int height, boolean flipped) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.flipped = flipped;
    }

    public void draw(Graphics g, int xOffset, int yOffset) {
        int drawX = x - xOffset;
        int drawY = y - yOffset;

        if (flipped) {
            g.drawImage(treeImage, drawX + width, drawY, -width, height, null);
        } else {
            g.drawImage(treeImage, drawX, drawY, width, height, null);
        }
    }
    
    
}
