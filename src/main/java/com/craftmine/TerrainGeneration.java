package com.craftmine;

import java.util.Random;

public class TerrainGeneration {
    public enum BlockType {
        GRASS,
        DIRT,
        STONE,
        SAND,
        WATER,
        LOG,
        LEAVES,
        COAL_ORE,
        IRON_ORE,
        DIAMOND_ORE
    }

    public static final int WORLD_SIZE = 512;  // World dimensions (512x512)
    private static final int HALF_SIZE = WORLD_SIZE / 2;  // Half size for quadrant calculations
    private static final int MIN_HEIGHT = 9;  // Minimum total height (1 grass + 3 dirt + 5 stone)
    private static final int MAX_STONE_LAYERS = 15;
    private static final int MAX_DIRT_LAYERS = 7;
    private static final int MIN_DIRT_LAYERS = 3;
    private static final int MIN_STONE_LAYERS = 5;
    private static final int RIVER_WIDTH = 5;  // Width of the river
    private static final int RIVER_DEPTH = 3;  // Depth of the river
    private static final int RIVER_BANK_WIDTH = 3;  // Width of sand banks
    private static final int NUM_RIVERS = 3;  // Number of rivers to generate
    private static final double COAL_ORE_CHANCE = 0.03;  // 3% chance for coal ore
    private static final double IRON_ORE_CHANCE = 0.02;  // 2% chance for iron ore
    private static final double DIAMOND_ORE_CHANCE = 0.001;  // 0.1% chance for diamond ore
    
    private final int seed;
    private final Random random;
    private double[] gradients;
    private BlockType[][][] world;  // [x][z][y] for easier height access
    private int[][] heightMap = new int[WORLD_SIZE][WORLD_SIZE];
    private int[] riverPositions;  // Store river positions
    private int[][] riverPaths;    // Store river paths
    
    private static final int MAX_HEIGHT = MAX_STONE_LAYERS + MAX_DIRT_LAYERS + 1;
    
    public TerrainGeneration(int seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.world = new BlockType[WORLD_SIZE][WORLD_SIZE][MAX_STONE_LAYERS + MAX_DIRT_LAYERS + 1];
        this.riverPositions = new int[NUM_RIVERS];
        this.riverPaths = new int[NUM_RIVERS][WORLD_SIZE];
        initGradients();
        generateRiverPaths();
        generateWorld();
        
        // Generate caves after the basic terrain
        Caves caves = new Caves(this, seed);
        caves.generateCaves();
    }
    
    private void initGradients() {
        gradients = new double[WORLD_SIZE];
        for (int i = 0; i < WORLD_SIZE; i++) {
            gradients[i] = random.nextDouble() * 2 - 1;
        }
    }
    
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
    
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private double grad(int hash, double x, double z) {
        // 2D gradient function
        int h = hash & 15;
        double u = h < 8 ? x : z;
        double v = h < 4 ? z : (h == 12 || h == 14 ? x : 0);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
    
    private double noise(double x, double z) {
        // 2D Perlin noise implementation
        int xi = (int)Math.floor(x) & 255;
        int zi = (int)Math.floor(z) & 255;
        
        double xf = x - Math.floor(x);
        double zf = z - Math.floor(z);
        
        double u = fade(xf);
        double v = fade(zf);
        
        int aa = (int)gradients[xi] + zi;
        int ab = (int)gradients[xi] + zi + 1;
        int ba = (int)gradients[xi + 1] + zi;
        int bb = (int)gradients[xi + 1] + zi + 1;
        
        double x1 = lerp(
            grad(aa, xf, zf),
            grad(ba, xf - 1, zf),
            u);
        double x2 = lerp(
            grad(ab, xf, zf - 1),
            grad(bb, xf - 1, zf - 1),
            u);
        
        return lerp(x1, x2, v);
    }
    
    private void generateRiverPaths() {
        // Generate random starting positions for each river
        for (int i = 0; i < NUM_RIVERS; i++) {
            riverPositions[i] = 20 + random.nextInt(WORLD_SIZE - 40); // Keep away from edges
            
            // Generate path with slight variations
            int currentPos = riverPositions[i];
            for (int x = 0; x < WORLD_SIZE; x++) {
                riverPaths[i][x] = currentPos;
                // 33% chance to move the river by 1 block
                if (random.nextInt(3) == 0) {
                    currentPos += (random.nextBoolean() ? 1 : -1);
                    // Keep river within bounds
                    currentPos = Math.max(20, Math.min(WORLD_SIZE - 20, currentPos));
                }
            }
        }
    }
    
    private boolean isInRiver(int x, int z) {
        for (int i = 0; i < NUM_RIVERS; i++) {
            int riverZ = riverPaths[i][x];
            if (Math.abs(z - riverZ) <= RIVER_WIDTH / 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isNearRiver(int x, int z) {
        for (int i = 0; i < NUM_RIVERS; i++) {
            int riverZ = riverPaths[i][x];
            int distance = Math.abs(z - riverZ);
            if (distance <= RIVER_WIDTH / 2 + RIVER_BANK_WIDTH) {
                return true;
            }
        }
        return false;
    }
    
    private void generateWorld() {
        // First pass: Generate base terrain and rivers
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                // Generate height using multiple octaves of noise
                double nx = x * 0.025;
                double nz = z * 0.025;
                double height = 0;
                
                // First octave - large features (mountains and valleys)
                double amplitude = 1.0;
                double frequency = 1.0;
                height += noise(nx * frequency, nz * frequency) * amplitude;
                
                // Second octave - medium features (hills)
                amplitude *= 0.35;
                frequency *= 1.8;
                height += noise(nx * frequency, nz * frequency) * amplitude;
                
                // Third octave - small features (bumps)
                amplitude *= 0.15;
                frequency *= 1.9;
                height += noise(nx * frequency, nz * frequency) * amplitude;
                
                // Normalize height and smooth the result
                height = (height + 1) / 2;
                height = Math.pow(height, 1.3);
                
                // Calculate total height with smoother distribution
                int totalHeight = (int)(height * 12) + MIN_HEIGHT;
                
                // Handle river and river banks
                boolean inRiver = isInRiver(x, z);
                boolean nearRiver = !inRiver && isNearRiver(x, z);
                
                if (inRiver) {
                    totalHeight = Math.max(MIN_HEIGHT, totalHeight - 1); // Make river 1 block below terrain
                } else if (nearRiver) {
                    totalHeight = Math.max(MIN_HEIGHT, totalHeight);
                }
                
                totalHeight = Math.min(totalHeight, MAX_STONE_LAYERS + MAX_DIRT_LAYERS + 1);
                
                // Calculate layer heights
                int stoneHeight = Math.min(totalHeight - MIN_DIRT_LAYERS - 1, MAX_STONE_LAYERS);
                stoneHeight = Math.max(stoneHeight, MIN_STONE_LAYERS);
                
                // Fill the terrain array
                int y = 0;
                
                // Fill stone layers
                for (; y < stoneHeight; y++) {
                    double oreChance = random.nextDouble();
                    if (oreChance < DIAMOND_ORE_CHANCE) {
                        world[x][z][y] = BlockType.DIAMOND_ORE;
                    } else if (oreChance < IRON_ORE_CHANCE + DIAMOND_ORE_CHANCE) {
                        world[x][z][y] = BlockType.IRON_ORE;
                    } else if (oreChance < COAL_ORE_CHANCE + IRON_ORE_CHANCE + DIAMOND_ORE_CHANCE) {
                        world[x][z][y] = BlockType.COAL_ORE;
                    } else {
                        world[x][z][y] = BlockType.STONE;
                    }
                }
                
                if (inRiver) {
                    // Create riverbed (sand)
                    for (; y < totalHeight - RIVER_DEPTH + 1; y++) {
                        world[x][z][y] = BlockType.SAND;
                    }
                    // Fill with water
                    for (; y < totalHeight; y++) {
                        world[x][z][y] = BlockType.WATER;
                    }
                    y++;
                } else if (nearRiver) {
                    // Create sand banks
                    for (; y < totalHeight; y++) {
                        world[x][z][y] = BlockType.SAND;
                    }
                    y++;
                } else {
                    // Normal terrain
                    int dirtHeight = Math.min(totalHeight - stoneHeight - 1, MAX_DIRT_LAYERS);
                    for (; y < stoneHeight + dirtHeight; y++) {
                        world[x][z][y] = BlockType.DIRT;
                    }
                    // Add top layer
                    if (y < totalHeight) {
                        world[x][z][y] = BlockType.GRASS;
                        y++;
                    }
                }
                
                heightMap[x][z] = y - 1;
            }
        }
        
        // Second pass: Add sand around rivers
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                if (world[x][z][heightMap[x][z]] == BlockType.GRASS) {
                    // Check if near water (within 3 blocks)
                    boolean nearWater = false;
                    for (int dx = -3; dx <= 3 && !nearWater; dx++) {
                        for (int dz = -3; dz <= 3 && !nearWater; dz++) {
                            int nx = x + dx;
                            int nz = z + dz;
                            if (nx >= 0 && nx < WORLD_SIZE && nz >= 0 && nz < WORLD_SIZE) {
                                if (world[nx][nz][heightMap[nx][nz]] == BlockType.WATER) {
                                    nearWater = true;
                                }
                            }
                        }
                    }
                    if (nearWater) {
                        world[x][z][heightMap[x][z]] = BlockType.SAND;
                    }
                }
            }
        }
        
        // Third pass: Generate trees
        for (int x = 0; x < WORLD_SIZE; x++) {
            for (int z = 0; z < WORLD_SIZE; z++) {
                if (world[x][z][heightMap[x][z]] == BlockType.GRASS) {
                    // Tree generation chance (1%)
                    if (random.nextDouble() < 0.01) {
                        generateTree(x, z, heightMap[x][z] + 1);
                    }
                }
            }
        }
    }
    
    private void generateTree(int x, int z, int baseY) {
        // Check if we have enough space for the tree
        if (x < 2 || x >= WORLD_SIZE - 2 || z < 2 || z >= WORLD_SIZE - 2 || baseY >= MAX_STONE_LAYERS + MAX_DIRT_LAYERS + 1 - 6) {
            return;
        }
        
        // Generate trunk (4-6 blocks tall)
        int trunkHeight = 4 + random.nextInt(3);
        for (int y = baseY; y < baseY + trunkHeight; y++) {
            world[x][z][y] = BlockType.LOG;
        }
        
        // Generate leaves (3x3x3 cube with some random removals)
        int leavesBaseY = baseY + trunkHeight - 2;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    // Skip corners and some random leaves for natural look
                    if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                    if (Math.abs(dx) + Math.abs(dz) > 3) continue;
                    if (random.nextDouble() < 0.1) continue;
                    
                    world[x + dx][z + dz][leavesBaseY + dy] = BlockType.LEAVES;
                }
            }
        }
    }
    
    public BlockType getBlock(int x, int z, int y) {
        // Convert from world coordinates to array indices (no offset needed)
        if (x < 0 || x >= WORLD_SIZE || z < 0 || z >= WORLD_SIZE || y < 0 || y >= MAX_HEIGHT) {
            return null;
        }
        return world[x][z][y];
    }

    public void setBlock(int x, int z, int y, BlockType type) {
        // Convert from world coordinates to array indices (no offset needed)
        if (x < 0 || x >= WORLD_SIZE || z < 0 || z >= WORLD_SIZE || y < 0 || y >= MAX_HEIGHT) {
            return;
        }
        world[x][z][y] = type;
    }
    
    public int getMaxHeight() {
        return MAX_HEIGHT;
    }
}
