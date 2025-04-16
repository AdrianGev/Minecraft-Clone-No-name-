package com.craftmine;

public class CollisionHandler {
    private TerrainGeneration terrain;
    private boolean debugMode = false;
    private static final float PLAYER_HEIGHT = 1f;  // Total player height
    private static final float LEGS_HEIGHT = 0f;    // Height of legs segment
    private static final float GROUND_OFFSET = -1.4f;  // Keep player this many blocks above ground
    private static final float PLAYER_RADIUS = 0.3f;  // Player's collision radius

    public CollisionHandler(TerrainGeneration terrain) {
        this.terrain = terrain;
    }

    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
    }

    /**
     * Checks if a block type is solid (should block movement)
     */
    private boolean isSolidBlock(TerrainGeneration.BlockType block) {
        if (block == null) return false;  // Air is not solid
        
        switch (block) {
            case WATER:  // Can swim through water
            case LEAVES: // Can walk through leaves
                return false;
            case GRASS:
            case DIRT:
            case STONE:
            case SAND:
            case LOG:
            case COAL_ORE:
            case IRON_ORE:
            case DIAMOND_ORE:
                return true;
            default:
                return true;  // Unknown blocks are solid by default
        }
    }

    /**
     * Checks if a position is valid for movement
     */
    public boolean isPositionValid(float x, float y, float z) {
        if (terrain == null) {
            if (debugMode) System.out.println("No terrain set, allowing movement");
            return true;
        }

        // Check blocks in a radius around the player
        for (float offsetX = -PLAYER_RADIUS; offsetX <= PLAYER_RADIUS; offsetX += PLAYER_RADIUS) {
            for (float offsetZ = -PLAYER_RADIUS; offsetZ <= PLAYER_RADIUS; offsetZ += PLAYER_RADIUS) {
                float checkX = x + offsetX;
                float checkZ = z + offsetZ;

                // Convert to block coordinates
                int blockX = (int)Math.floor(checkX);
                int blockZ = (int)Math.floor(checkZ);
                
                // Check three levels: feet, legs, and head, offset by GROUND_OFFSET
                int feetY = (int)Math.floor(y + GROUND_OFFSET);
                int legsY = (int)Math.floor(y + GROUND_OFFSET + LEGS_HEIGHT);
                int headY = (int)Math.floor(y + GROUND_OFFSET + PLAYER_HEIGHT);

                // Check world bounds
                if (blockX < 0 || blockX >= TerrainGeneration.WORLD_SIZE ||
                    blockZ < 0 || blockZ >= TerrainGeneration.WORLD_SIZE ||
                    feetY < 0 || headY >= terrain.getMaxHeight()) {
                    if (debugMode) System.out.println("Position out of bounds: " + blockX + "," + feetY + "," + blockZ);
                    return false;
                }

                // Get blocks at all three levels
                TerrainGeneration.BlockType feetBlock = terrain.getBlock(blockX, blockZ, feetY);
                TerrainGeneration.BlockType legsBlock = terrain.getBlock(blockX, blockZ, legsY);
                TerrainGeneration.BlockType headBlock = terrain.getBlock(blockX, blockZ, headY);

                if (debugMode) {
                    System.out.println("Checking position: " + checkX + "," + y + "," + checkZ);
                    System.out.println("Block coordinates: " + blockX + "," + feetY + "," + blockZ);
                    System.out.println("Feet block: " + feetBlock + " (solid: " + isSolidBlock(feetBlock) + ")");
                    System.out.println("Legs block: " + legsBlock + " (solid: " + isSolidBlock(legsBlock) + ")");
                    System.out.println("Head block: " + headBlock + " (solid: " + isSolidBlock(headBlock) + ")");
                }

                // If any block in the radius is solid, prevent movement
                if (isSolidBlock(feetBlock) || isSolidBlock(legsBlock) || isSolidBlock(headBlock)) {
                    return false;
                }
            }
        }

        // All positions in radius are valid
        return true;
    }

    /**
     * Attempts to move to a new position, handling collisions
     */
    public float[] handleMovement(float currentX, float currentY, float currentZ,
                                float newX, float newY, float newZ) {
        float[] finalPosition = new float[]{currentX, currentY, currentZ};

        // Try horizontal movement first
        if (isPositionValid(newX, currentY, newZ)) {
            finalPosition[0] = newX;
            finalPosition[2] = newZ;
            if (debugMode) System.out.println("Horizontal movement allowed");
        } else if (debugMode) {
            System.out.println("Horizontal movement blocked");
        }

        // Then try vertical movement
        if (isPositionValid(finalPosition[0], newY, finalPosition[2])) {
            finalPosition[1] = newY;
            if (debugMode) System.out.println("Vertical movement allowed");
        } else if (debugMode) {
            System.out.println("Vertical movement blocked");
        }

        return finalPosition;
    }
}
