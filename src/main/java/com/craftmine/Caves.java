package com.craftmine;

import java.util.Random;

public class Caves {
    private static final int MIN_CAVE_HEIGHT = 5;
    private static final double CAVE_START_CHANCE = 0.02; // Increased chance
    private static final int MIN_LENGTH = 200; // Increased minimum length
    private static final int MAX_LENGTH = 500; // Increased maximum length
    private static final int MIN_RADIUS = 2;
    private static final int MAX_RADIUS = 5; // Slightly increased max radius
    private static final double BRANCH_CHANCE = 0.15; // Chance to create a branch
    private static final int MAX_BRANCHES = 3; // Maximum number of branches per cave
    private static final int MIN_SPHERES = 20; // Minimum number of spheres per cave
    private static final double COAL_ORE_CHANCE = 0.03;  // 3% chance for coal ore
    private static final double IRON_ORE_CHANCE = 0.02;  // 2% chance for iron ore
    private static final double DIAMOND_ORE_CHANCE = 0.001;  // 0.1% chance for diamond ore

    private final Random random;
    private final TerrainGeneration terrain;

    public Caves(TerrainGeneration terrain, int seed) {
        this.terrain = terrain;
        this.random = new Random(seed);
    }

    public void generateCaves() {
        // Generate caves starting from the surface
        for (int x = 0; x < TerrainGeneration.WORLD_SIZE; x++) {
            for (int z = 0; z < TerrainGeneration.WORLD_SIZE; z++) {
                if (random.nextDouble() < CAVE_START_CHANCE && !hasTreeNearby(x, z) && !hasRiverNearby(x, z)) {
                    int startY = terrain.getMaxHeight() - 10; // Start a bit below surface
                    generateCave(x, startY, z);
                }
            }
        }
    }

    private boolean hasTreeNearby(int x, int z) {
        // Check a larger area around the point for any tree blocks
        int checkRadius = 6; // Increased radius for better tree protection
        for (int dx = -checkRadius; dx <= checkRadius; dx++) {
            for (int dz = -checkRadius; dz <= checkRadius; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;
                
                // Skip if out of bounds
                if (checkX < 0 || checkX >= TerrainGeneration.WORLD_SIZE || 
                    checkZ < 0 || checkZ >= TerrainGeneration.WORLD_SIZE) {
                    continue;
                }
                
                // Check the column for any tree blocks
                for (int y = 0; y < terrain.getMaxHeight(); y++) {
                    TerrainGeneration.BlockType block = terrain.getBlock(checkX, checkZ, y);
                    if (block == TerrainGeneration.BlockType.LOG || 
                        block == TerrainGeneration.BlockType.LEAVES) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasRiverNearby(int x, int z) {
        // Check a larger area around the point for any sand blocks (river)
        int checkRadius = 8;
        for (int dx = -checkRadius; dx <= checkRadius; dx++) {
            for (int dz = -checkRadius; dz <= checkRadius; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;
                
                // Skip if out of bounds
                if (checkX < 0 || checkX >= TerrainGeneration.WORLD_SIZE || 
                    checkZ < 0 || checkZ >= TerrainGeneration.WORLD_SIZE) {
                    continue;
                }
                
                // Check the column for any sand blocks
                for (int y = 0; y < terrain.getMaxHeight(); y++) {
                    if (terrain.getBlock(checkX, checkZ, y) == TerrainGeneration.BlockType.SAND) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isUnderTree(int x, int y, int z) {
        // Check the column above for any tree blocks
        for (int checkY = y; checkY < terrain.getMaxHeight(); checkY++) {
            TerrainGeneration.BlockType block = terrain.getBlock(x, z, checkY);
            if (block == TerrainGeneration.BlockType.LOG || 
                block == TerrainGeneration.BlockType.LEAVES) {
                return true;
            }
        }
        return false;
    }

    private void generateCave(int startX, int startY, int startZ) {
        // Determine cave length
        int length = MIN_LENGTH + random.nextInt(MAX_LENGTH - MIN_LENGTH + 1);
        generateCaveSegment(startX, startY, startZ, length, 0);
    }

    private void generateCaveSegment(int startX, int startY, int startZ, int length, int branchDepth) {
        if (branchDepth >= MAX_BRANCHES) return;
    
        double x = startX;
        double y = startY;
        double z = startZ;
        
        double dx = (random.nextDouble() - 0.5) * 1.5;
        double dy = -0.4 - random.nextDouble() * 0.3;
        double dz = (random.nextDouble() - 0.5) * 1.5;
        
        double magnitude = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= magnitude;
        dy /= magnitude;
        dz /= magnitude;
    
        int totalSpheresCarved = 0;
        int lastRadius = MIN_RADIUS;
        
        for (int step = 0; step < length; step++) {
            if (y <= MIN_CAVE_HEIGHT + 3) break;
    
            if (hasRiverNearby((int) x, (int) z)) {
                dx += (startX - x) * 0.05;
                dz += (startZ - z) * 0.05;
            }
    
            magnitude = Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= magnitude;
            dy /= magnitude;
            dz /= magnitude;
    
            int radius = (int) (MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * (random.nextDouble() * 0.7 + 0.3));
    
            if (terrain.getBlock((int)x, (int)z, (int)y) == TerrainGeneration.BlockType.SAND) {
                x += dx * lastRadius;
                y += dy * lastRadius;
                z += dz * lastRadius;
                continue;
            }
    
            int blocksCarved = carveSphere((int)x, (int)y, (int)z, radius);
            if (blocksCarved > 0) {
                totalSpheresCarved++;
                lastRadius = radius;
            }
    
            if (step > 10 && totalSpheresCarved < 3) break;
    
            if (totalSpheresCarved > 50 && random.nextDouble() < 0.15) break;
    
            if (step % 2 == 0) {
                dx += (random.nextDouble() - 0.5) * 0.2;
                dy += (random.nextDouble() - 0.3) * 0.15;
                dz += (random.nextDouble() - 0.5) * 0.2;
    
                magnitude = Math.sqrt(dx * dx + dy * dy + dz * dz);
                dx /= magnitude;
                dy /= magnitude;
                dz /= magnitude;
            }
    
            x += dx * lastRadius;
            y += dy * lastRadius;
            z += dz * lastRadius;
        }
    }

    private int carveSphere(int centerX, int centerY, int centerZ, int radius) {
        int radiusSq = radius * radius;
        int blocksCarved = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radiusSq) {
                        int worldX = centerX + x;
                        int worldY = centerY + y;
                        int worldZ = centerZ + z;
                        
                        // Check world bounds
                        if (worldX >= 0 && worldX < TerrainGeneration.WORLD_SIZE &&
                            worldY >= MIN_CAVE_HEIGHT && worldY < terrain.getMaxHeight() &&
                            worldZ >= 0 && worldZ < TerrainGeneration.WORLD_SIZE) {
                            
                            // Skip if this block is under a tree
                            if (isUnderTree(worldX, worldY, worldZ)) {
                                continue;
                            }
                            
                            TerrainGeneration.BlockType block = terrain.getBlock(worldX, worldZ, worldY);
                            if (block != null && 
                                block != TerrainGeneration.BlockType.WATER &&
                                block != TerrainGeneration.BlockType.LOG &&
                                block != TerrainGeneration.BlockType.LEAVES) {
                                terrain.setBlock(worldX, worldZ, worldY, null);
                                blocksCarved++;
                            }
                        }
                    }
                }
            }
        }
        return blocksCarved;
    }
}
