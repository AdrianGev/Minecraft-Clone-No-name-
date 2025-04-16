package com.craftmine;

import static org.lwjgl.opengl.GL11.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MinecraftSkinProcessor {
    // Standard Minecraft skin dimensions
    private static final int SKIN_WIDTH = 64;
    private static final int SKIN_HEIGHT = 64;
    
    // UV coordinates for each part (normalized to 64x64 texture)
    private static final float[] HEAD_FRONT_UV = {
        8/64.0f, 8/64.0f,     // U1, V1
        16/64.0f, 16/64.0f    // U2, V2
    };
    
    private static final float[] HEAD_BACK_UV = {
        24/64.0f, 8/64.0f,    // U1, V1
        32/64.0f, 16/64.0f    // U2, V2
    };
    
    private static final float[] HEAD_RIGHT_UV = {
        0/64.0f, 8/64.0f,     // U1, V1
        8/64.0f, 16/64.0f     // U2, V2
    };
    
    private static final float[] HEAD_LEFT_UV = {
        16/64.0f, 8/64.0f,    // U1, V1
        24/64.0f, 16/64.0f    // U2, V2
    };
    
    private static final float[] HEAD_TOP_UV = {
        8/64.0f, 0/64.0f,     // U1, V1
        16/64.0f, 8/64.0f     // U2, V2
    };
    
    private static final float[] HEAD_BOTTOM_UV = {
        16/64.0f, 0/64.0f,    // U1, V1
        24/64.0f, 8/64.0f     // U2, V2
    };
    
    private static final float[] HEAD_OVERLAY_UV = {
        32/64.0f, 0/64.0f,   // U1, V1
        40/64.0f, 8/64.0f    // U2, V2
    };
    
    // Torso UV coordinates
    private static final float[] TORSO_UV = {
        20/64.0f, 20/64.0f,   // U1, V1
        28/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] TORSO_FRONT_UV = {
        20/64.0f, 20/64.0f,   // U1, V1
        28/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] TORSO_BACK_UV = {
        32/64.0f, 20/64.0f,   // U1, V1
        40/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] TORSO_RIGHT_UV = {
        28/64.0f, 20/64.0f,   // U1, V1
        32/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] TORSO_LEFT_UV = {
        16/64.0f, 20/64.0f,   // U1, V1
        20/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] TORSO_TOP_UV = {
        20/64.0f, 16/64.0f,   // U1, V1
        28/64.0f, 20/64.0f    // U2, V2
    };
    
    private static final float[] TORSO_BOTTOM_UV = {
        28/64.0f, 16/64.0f,   // U1, V1
        36/64.0f, 20/64.0f    // U2, V2
    };
    
    private static final float[] RIGHT_ARM_UV = {
        44/64.0f, 20/64.0f,   // U1, V1
        48/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] LEFT_ARM_UV = {
        36/64.0f, 52/64.0f,   // U1, V1
        40/64.0f, 64/64.0f    // U2, V2
    };
    
    private static final float[] RIGHT_LEG_UV = {
        4/64.0f, 20/64.0f,    // U1, V1
        8/64.0f, 32/64.0f     // U2, V2
    };
    
    private static final float[] LEFT_LEG_UV = {
        20/64.0f, 52/64.0f,   // U1, V1
        24/64.0f, 64/64.0f    // U2, V2
    };
    
    // Right arm UV coordinates
    private static final float[] RIGHT_ARM_FRONT_UV = {
        44/64.0f, 20/64.0f,   // U1, V1
        48/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] RIGHT_ARM_BACK_UV = {
        52/64.0f, 20/64.0f,   // U1, V1
        56/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] RIGHT_ARM_RIGHT_UV = {
        48/64.0f, 20/64.0f,   // U1, V1
        52/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] RIGHT_ARM_LEFT_UV = {
        40/64.0f, 20/64.0f,   // U1, V1
        44/64.0f, 32/64.0f    // U2, V2
    };
    
    // Left arm UV coordinates
    private static final float[] LEFT_ARM_FRONT_UV = {
        36/64.0f, 52/64.0f,   // U1, V1
        40/64.0f, 64/64.0f    // U2, V2
    };
    
    private static final float[] LEFT_ARM_BACK_UV = {
        44/64.0f, 52/64.0f,   // U1, V1
        48/64.0f, 64/64.0f    // U2, V2
    };
    
    private static final float[] LEFT_ARM_RIGHT_UV = {
        40/64.0f, 52/64.0f,   // U1, V1
        44/64.0f, 64/64.0f    // U2, V2
    };
    
    private static final float[] LEFT_ARM_LEFT_UV = {
        32/64.0f, 52/64.0f,   // U1, V1
        36/64.0f, 64/64.0f    // U2, V2
    };
    
    // Right leg UV coordinates
    private static final float[] RIGHT_LEG_FRONT_UV = {
        4/64.0f, 20/64.0f,    // U1, V1
        8/64.0f, 32/64.0f     // U2, V2
    };
    
    private static final float[] RIGHT_LEG_BACK_UV = {
        12/64.0f, 20/64.0f,   // U1, V1
        16/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] RIGHT_LEG_RIGHT_UV = {
        8/64.0f, 20/64.0f,    // U1, V1
        12/64.0f, 32/64.0f    // U2, V2
    };
    
    private static final float[] RIGHT_LEG_LEFT_UV = {
        0/64.0f, 20/64.0f,    // U1, V1
        4/64.0f, 32/64.0f     // U2, V2
    };
    
    // Left leg UV coordinates
    private static final float[] LEFT_LEG_FRONT_UV = {
        20/64.0f, 52/64.0f,   // U1, V1
        24/64.0f, 64/64.0f    // U2, V2
    };
    
    private static final float[] LEFT_LEG_BACK_UV = {
        28/64.0f, 52/64.0f,   // U1, V1
        32/64.0f, 64/64.0f    // U2, V2
    };
    
    private static final float[] LEFT_LEG_RIGHT_UV = {
        24/64.0f, 52/64.0f,   // U1, V1
        28/64.0f, 64/64.0f    // U2, V2
    };
    
    private static final float[] LEFT_LEG_LEFT_UV = {
        16/64.0f, 52/64.0f,   // U1, V1
        20/64.0f, 64/64.0f    // U2, V2
    };
    
    private int skinTexture;
    private BufferedImage skinImage;
    private ByteBuffer textureBuffer;
    private boolean hasLoadedSkin;

    public MinecraftSkinProcessor() {
        this.skinTexture = -1;
        this.hasLoadedSkin = false;
    }

    public boolean loadSkin(String filePath) {
        try {
            System.out.println("Attempting to load skin from: " + filePath);
            File skinFile = new File(filePath);
            if (!skinFile.exists()) {
                System.err.println("Skin file does not exist: " + filePath);
                return false;
            }
            skinImage = ImageIO.read(skinFile);

            // Verify skin dimensions
            if (skinImage.getWidth() != SKIN_WIDTH || skinImage.getHeight() != SKIN_HEIGHT) {
                System.err.println("Invalid skin dimensions. Expected 64x64, got " + 
                                 skinImage.getWidth() + "x" + skinImage.getHeight());
                return false;
            }

            // Convert the image to a format OpenGL can use
            textureBuffer = imageToBuffer(skinImage);
            
            // Delete existing texture if there is one
            if (skinTexture != -1) {
                glDeleteTextures(skinTexture);
            }
            
            // Generate and bind the texture
            skinTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, skinTexture);
            
            // Set texture parameters
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
            
            // Upload the texture to GPU
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, SKIN_WIDTH, SKIN_HEIGHT, 
                        0, GL_RGBA, GL_UNSIGNED_BYTE, textureBuffer);

            hasLoadedSkin = true;
            System.out.println("Skin loaded successfully. Texture ID: " + skinTexture);
            return true;
        } catch (IOException e) {
            System.err.println("Error loading skin: " + e.getMessage());
            e.printStackTrace();
            hasLoadedSkin = false;
            return false;
        }
    }

    public boolean hasSkin() {
        return hasLoadedSkin;
    }

    private ByteBuffer imageToBuffer(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                buffer.put((byte) (pixel & 0xFF));         // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }
        
        buffer.flip();
        return buffer;
    }

    public void bindSkinTexture() {
        if (hasLoadedSkin && skinTexture != -1) {
            glBindTexture(GL_TEXTURE_2D, skinTexture);
            System.out.println("Binding skin texture: " + skinTexture);
        } else {
            System.out.println("Cannot bind skin texture. HasLoadedSkin: " + hasLoadedSkin + ", TextureID: " + skinTexture);
        }
    }

    public void cleanup() {
        if (skinTexture != -1) {
            glDeleteTextures(skinTexture);
            skinTexture = -1;
        }
    }

    public float[] getHeadFrontUV() { return HEAD_FRONT_UV; }
    public float[] getHeadBackUV() { return HEAD_BACK_UV; }
    public float[] getHeadRightUV() { return HEAD_RIGHT_UV; }
    public float[] getHeadLeftUV() { return HEAD_LEFT_UV; }
    public float[] getHeadTopUV() { return HEAD_TOP_UV; }
    public float[] getHeadBottomUV() { return HEAD_BOTTOM_UV; }
    public float[] getHeadOverlayUV() { return HEAD_OVERLAY_UV; }
    public float[] getTorsoUV() { return TORSO_UV; }
    public float[] getTorsoFrontUV() { return TORSO_FRONT_UV; }
    public float[] getTorsoBackUV() { return TORSO_BACK_UV; }
    public float[] getTorsoRightUV() { return TORSO_RIGHT_UV; }
    public float[] getTorsoLeftUV() { return TORSO_LEFT_UV; }
    public float[] getTorsoTopUV() { return TORSO_TOP_UV; }
    public float[] getTorsoBottomUV() { return TORSO_BOTTOM_UV; }
    public float[] getRightArmUV() { return RIGHT_ARM_UV; }
    public float[] getLeftArmUV() { return LEFT_ARM_UV; }
    public float[] getRightLegUV() { return RIGHT_LEG_UV; }
    public float[] getLeftLegUV() { return LEFT_LEG_UV; }
    
    public float[] getRightArmFrontUV() { return RIGHT_ARM_FRONT_UV; }
    public float[] getRightArmBackUV() { return RIGHT_ARM_BACK_UV; }
    public float[] getRightArmRightUV() { return RIGHT_ARM_RIGHT_UV; }
    public float[] getRightArmLeftUV() { return RIGHT_ARM_LEFT_UV; }
    
    public float[] getLeftArmFrontUV() { return LEFT_ARM_FRONT_UV; }
    public float[] getLeftArmBackUV() { return LEFT_ARM_BACK_UV; }
    public float[] getLeftArmRightUV() { return LEFT_ARM_RIGHT_UV; }
    public float[] getLeftArmLeftUV() { return LEFT_ARM_LEFT_UV; }
    
    public float[] getRightLegFrontUV() { return RIGHT_LEG_FRONT_UV; }
    public float[] getRightLegBackUV() { return RIGHT_LEG_BACK_UV; }
    public float[] getRightLegRightUV() { return RIGHT_LEG_RIGHT_UV; }
    public float[] getRightLegLeftUV() { return RIGHT_LEG_LEFT_UV; }
    
    public float[] getLeftLegFrontUV() { return LEFT_LEG_FRONT_UV; }
    public float[] getLeftLegBackUV() { return LEFT_LEG_BACK_UV; }
    public float[] getLeftLegRightUV() { return LEFT_LEG_RIGHT_UV; }
    public float[] getLeftLegLeftUV() { return LEFT_LEG_LEFT_UV; }
    
    public void applyUVToQuad(float[] uvCoords) {
        glTexCoord2f(uvCoords[0], uvCoords[1]); // Top-left
        glTexCoord2f(uvCoords[2], uvCoords[1]); // Top-right
        glTexCoord2f(uvCoords[2], uvCoords[3]); // Bottom-right
        glTexCoord2f(uvCoords[0], uvCoords[3]); // Bottom-left
    }
}
