package com.craftmine;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.lwjgl.opengl.GL11.*;

public class TextureLoader {
    private static final String[] TEXTURE_FILES = {
        "grassblocktop.jpg",
        "grassblockside.png",
        "dirtblock.jpg",
        "stoneblock.png",
        "water.jpg",
        "sand.png",
        "oaklogside.jpg",
        "oaklogtop.jpg",
        "oakleaves.png",
        "coalore.png",
        "ironorefinal.png",
        "diamondore.png"
    };

    public static void reloadAllTextures() {
        // Delete all existing textures
        for (String textureFile : TEXTURE_FILES) {
            try {
                loadTexture("assets/" + textureFile);
            } catch (Exception e) {
                System.err.println("Failed to reload texture: " + textureFile);
                e.printStackTrace();
            }
        }
    }

    public static int loadTexture(String resourcePath) {
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        try {
            // Load resource from classpath
            InputStream inputStream = TextureLoader.class.getResourceAsStream("/" + resourcePath);
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }

            // Create temporary file
            Path tempFile = Files.createTempFile("texture", ".png");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();

            // Load texture from temporary file
            ByteBuffer data = STBImage.stbi_load(tempFile.toString(), width, height, channels, 4);
            if (data == null) {
                throw new RuntimeException("Failed to load texture: " + resourcePath + ", reason: " + STBImage.stbi_failure_reason());
            }

            // Create and bind texture
            int textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);

            // Set texture parameters for pixel-perfect rendering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

            // Upload texture data
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

            // Clean up
            STBImage.stbi_image_free(data);
            Files.delete(tempFile);

            // Unbind texture
            glBindTexture(GL_TEXTURE_2D, 0);

            return textureId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + resourcePath, e);
        }
    }
}
