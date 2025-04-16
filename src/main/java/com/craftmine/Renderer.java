package com.craftmine;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private Camera camera;
    private TerrainGeneration terrain;
    private int grassTopTexture;
    private int grassSideTexture;
    private int dirtTexture;
    private int stoneTexture;
    private int waterTexture;
    private int sandTexture;
    private int logSideTexture;
    private int logTopTexture;
    private int leavesTexture;
    private int coalOreTexture;
    private int ironOreTexture;
    private int diamondOreTexture;

    public Renderer(Camera camera) {
        this.camera = camera;
        this.terrain = new TerrainGeneration(123); // You can change the seed
        
        // Enable texture state before loading
        glEnable(GL_TEXTURE_2D);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        
        // Load textures
        loadTextures();
        
        // Verify textures loaded correctly
        verifyTextures();
    }

    private void loadTextures() {
        // Delete any existing textures first
        deleteTextures();
        
        // Load new textures
        grassTopTexture = TextureLoader.loadTexture("assets/grassblocktop.jpg");
        grassSideTexture = TextureLoader.loadTexture("assets/grassblockside.png");
        dirtTexture = TextureLoader.loadTexture("assets/dirtblock.jpg");
        stoneTexture = TextureLoader.loadTexture("assets/stoneblock.png");
        waterTexture = TextureLoader.loadTexture("assets/water.jpg");
        sandTexture = TextureLoader.loadTexture("assets/sand.png");
        logSideTexture = TextureLoader.loadTexture("assets/oaklogside.jpg");  // Oak log side texture
        logTopTexture = TextureLoader.loadTexture("assets/oaklogtop.jpg");    // Oak log top texture
        leavesTexture = TextureLoader.loadTexture("assets/oakleaves.png");    // Oak leaves texture
        coalOreTexture = TextureLoader.loadTexture("assets/coalore.png");     // Coal ore texture
        ironOreTexture = TextureLoader.loadTexture("assets/ironorefinal.png");     // Iron ore texture
        diamondOreTexture = TextureLoader.loadTexture("assets/diamondore.png"); // Diamond ore texture
    }
    
    private void deleteTextures() {
        // Delete existing textures if they exist
        if (grassTopTexture > 0) glDeleteTextures(grassTopTexture);
        if (grassSideTexture > 0) glDeleteTextures(grassSideTexture);
        if (dirtTexture > 0) glDeleteTextures(dirtTexture);
        if (stoneTexture > 0) glDeleteTextures(stoneTexture);
        if (waterTexture > 0) glDeleteTextures(waterTexture);
        if (sandTexture > 0) glDeleteTextures(sandTexture);
        if (logSideTexture > 0) glDeleteTextures(logSideTexture);
        if (logTopTexture > 0) glDeleteTextures(logTopTexture);
        if (leavesTexture > 0) glDeleteTextures(leavesTexture);
        if (coalOreTexture > 0) glDeleteTextures(coalOreTexture);
        if (ironOreTexture > 0) glDeleteTextures(ironOreTexture);
        if (diamondOreTexture > 0) glDeleteTextures(diamondOreTexture);
    }
    
    private void verifyTextures() {
        // Verify each texture was loaded successfully
        if (grassTopTexture == 0 || grassSideTexture == 0 || dirtTexture == 0 ||
            stoneTexture == 0 || waterTexture == 0 || sandTexture == 0 ||
            logSideTexture == 0 || logTopTexture == 0 || leavesTexture == 0 ||
            coalOreTexture == 0 || ironOreTexture == 0 || diamondOreTexture == 0) {
            throw new RuntimeException("Failed to load one or more textures");
        }
    }

    private boolean isBlockVisible(int x, int z, int y) {
        // Check if block exists at this position
        if (terrain.getBlock(x, z, y) == null) {
            return false;
        }

        // Helper function to check if a block is either null (air) or water
        TerrainGeneration.BlockType blockType;

        // Check all six sides
        // Top
        blockType = y + 1 >= terrain.getMaxHeight() ? null : terrain.getBlock(x, z, y + 1);
        if (blockType == null || blockType == TerrainGeneration.BlockType.WATER) {
            return true;
        }
        // Bottom
        blockType = y - 1 < 0 ? null : terrain.getBlock(x, z, y - 1);
        if (blockType == null || blockType == TerrainGeneration.BlockType.WATER) {
            return true;
        }
        // North
        blockType = z + 1 >= TerrainGeneration.WORLD_SIZE ? null : terrain.getBlock(x, z + 1, y);
        if (blockType == null || blockType == TerrainGeneration.BlockType.WATER) {
            return true;
        }
        // South
        blockType = z - 1 < 0 ? null : terrain.getBlock(x, z - 1, y);
        if (blockType == null || blockType == TerrainGeneration.BlockType.WATER) {
            return true;
        }
        // East
        blockType = x + 1 >= TerrainGeneration.WORLD_SIZE ? null : terrain.getBlock(x + 1, z, y);
        if (blockType == null || blockType == TerrainGeneration.BlockType.WATER) {
            return true;
        }
        // West
        blockType = x - 1 < 0 ? null : terrain.getBlock(x - 1, z, y);
        if (blockType == null || blockType == TerrainGeneration.BlockType.WATER) {
            return true;
        }

        // If we get here, all sides are covered by non-water blocks
        return false;
    }

    private void drawBlock(float x, float y, float z, TerrainGeneration.BlockType blockType) {
        float size = 1.0f;

        // Select textures based on block type
        int topTex, sideTex, bottomTex;
        switch (blockType) {
            case GRASS:
                topTex = grassTopTexture;
                sideTex = grassSideTexture;
                bottomTex = dirtTexture;
                break;
            case DIRT:
                topTex = dirtTexture;
                sideTex = dirtTexture;
                bottomTex = dirtTexture;
                break;
            case STONE:
                topTex = stoneTexture;
                sideTex = stoneTexture;
                bottomTex = stoneTexture;
                break;
            case SAND:
                topTex = sandTexture;
                sideTex = sandTexture;
                bottomTex = sandTexture;
                glColor3f(1.0f, 1.0f, 1.0f);  // No tint needed since we have proper texture
                break;
            case LOG:
                topTex = logTopTexture;     // Top of log
                sideTex = logSideTexture;   // Sides of log
                bottomTex = logTopTexture;  // Bottom of log (same as top)
                break;
            case LEAVES:
                topTex = leavesTexture;
                sideTex = leavesTexture;
                bottomTex = leavesTexture;
                break;
            case WATER:
                topTex = waterTexture;
                sideTex = waterTexture;
                bottomTex = waterTexture;
                glColor4f(0.2f, 0.3f, 0.9f, 0.7f);  // Slightly less transparent blue
                break;
            case COAL_ORE:
                topTex = coalOreTexture;
                sideTex = coalOreTexture;
                bottomTex = coalOreTexture;
                break;
            case IRON_ORE:
                topTex = ironOreTexture;
                sideTex = ironOreTexture;
                bottomTex = ironOreTexture;
                break;
            case DIAMOND_ORE:
                topTex = diamondOreTexture;
                sideTex = diamondOreTexture;
                bottomTex = diamondOreTexture;
                break;
            default:
                return;
        }

        // Draw the block faces
        drawBlockFaces(x, y, z, size, topTex, sideTex, bottomTex);

        // Reset color
        if (blockType == TerrainGeneration.BlockType.SAND || blockType == TerrainGeneration.BlockType.WATER) {
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void drawBlockFaces(float x, float y, float z, float size, int topTex, int sideTex, int bottomTex) {
        // Top face
        glBindTexture(GL_TEXTURE_2D, topTex);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(x - size/2, y + size/2, z - size/2);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(x + size/2, y + size/2, z - size/2);
        glTexCoord2f(1.0f, 1.0f); glVertex3f(x + size/2, y + size/2, z + size/2);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(x - size/2, y + size/2, z + size/2);
        glEnd();

        // Side faces
        glBindTexture(GL_TEXTURE_2D, sideTex);
        glBegin(GL_QUADS);
        // Front
        glTexCoord2f(0.0f, 1.0f); glVertex3f(x - size/2, y - size/2, z + size/2);
        glTexCoord2f(1.0f, 1.0f); glVertex3f(x + size/2, y - size/2, z + size/2);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(x + size/2, y + size/2, z + size/2);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(x - size/2, y + size/2, z + size/2);
        // Back
        glTexCoord2f(1.0f, 1.0f); glVertex3f(x - size/2, y - size/2, z - size/2);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(x - size/2, y + size/2, z - size/2);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(x + size/2, y + size/2, z - size/2);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(x + size/2, y - size/2, z - size/2);
        // Right
        glTexCoord2f(1.0f, 1.0f); glVertex3f(x + size/2, y - size/2, z - size/2);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(x + size/2, y + size/2, z - size/2);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(x + size/2, y + size/2, z + size/2);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(x + size/2, y - size/2, z + size/2);
        // Left
        glTexCoord2f(0.0f, 1.0f); glVertex3f(x - size/2, y - size/2, z - size/2);
        glTexCoord2f(1.0f, 1.0f); glVertex3f(x - size/2, y - size/2, z + size/2);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(x - size/2, y + size/2, z + size/2);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(x - size/2, y + size/2, z - size/2);
        glEnd();

        // Bottom face
        glBindTexture(GL_TEXTURE_2D, bottomTex);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f); glVertex3f(x - size/2, y - size/2, z - size/2);
        glTexCoord2f(1.0f, 1.0f); glVertex3f(x + size/2, y - size/2, z - size/2);
        glTexCoord2f(1.0f, 0.0f); glVertex3f(x + size/2, y - size/2, z + size/2);
        glTexCoord2f(0.0f, 0.0f); glVertex3f(x - size/2, y - size/2, z + size/2);
        glEnd();
    }

    public void render() {
        // Save the current matrix
        glPushMatrix();
        
        // Reset the matrix
        glLoadIdentity();
        
        // Apply camera rotation
        glRotatef(camera.getPitch(), 1.0f, 0.0f, 0.0f);
        glRotatef(camera.getYaw(), 0.0f, 1.0f, 0.0f);
        
        // Apply camera translation
        glTranslatef(-camera.getX(), -camera.getY(), -camera.getZ());

        // Enable texturing
        glEnable(GL_TEXTURE_2D);
        
        // First render pass - solid blocks
        glDisable(GL_BLEND);
        renderBlocks(false);
        
        // Second render pass - transparent blocks
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        renderBlocks(true);
        
        // Restore the matrix
        glPopMatrix();
    }
    
    private void renderBlocks(boolean transparentPass) {
        // Get camera position to determine which chunks to render
        int camX = (int)camera.getX();
        int camZ = (int)camera.getZ();
        
        // Render a 48x48 area around the camera
        int renderDistance = 48;
        
        // Calculate bounds once
        int minX = Math.max(0, camX - renderDistance);
        int maxX = Math.min(TerrainGeneration.WORLD_SIZE - 1, camX + renderDistance);
        int minZ = Math.max(0, camZ - renderDistance);
        int maxZ = Math.min(TerrainGeneration.WORLD_SIZE - 1, camZ + renderDistance);
        
        // Pre-calculate some values
        float cameraDistanceThreshold = renderDistance * renderDistance;
        
        // Render blocks from front to back for better transparency
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // Skip blocks too far from camera
                float dx = x - camX;
                float dz = z - camZ;
                if (dx * dx + dz * dz > cameraDistanceThreshold) {
                    continue;
                }
                
                // Render each block in the column
                for (int y = 0; y < terrain.getMaxHeight(); y++) {
                    TerrainGeneration.BlockType block = terrain.getBlock(x, z, y);
                    if (block != null) {
                        boolean isTransparent = block == TerrainGeneration.BlockType.WATER;
                        if (isTransparent == transparentPass && isBlockVisible(x, z, y)) {
                            drawBlock(x, y, z, block);
                        }
                    }
                }
            }
        }
    }

    public TerrainGeneration getTerrain() {
        return terrain;
    }
}
