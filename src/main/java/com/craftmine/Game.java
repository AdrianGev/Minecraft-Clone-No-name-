package com.craftmine;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game {
    private long window;
    private Camera camera;
    private Renderer renderer;
    private UI ui;
    private HomeScreen homeScreen;
    private MinecraftSkinProcessor skinProcessor;  // Add skin processor
    private PlayerModel playerModel;  // Add player model
    private boolean[] keys;
    private boolean[] lastKeys;  // Store last keys state for toggle functionality
    private boolean isPaused = false;
    private boolean isInGame = false;
    private boolean isCreativeMode = true;  // Default to creative mode
    private boolean isThirdPerson = false;  // Track third-person mode state
    private float[] thirdPersonLockPoint = new float[3];  // Store camera target position
    private boolean wasJPressed = false;  // Track J key state
    private double lastX = 800, lastY = 375;
    private boolean firstMouse = true;
    private float mouseSensitivity = 0.1f;
    private double lastPausedX, lastPausedY;  // Store mouse position when paused
    private double lastPausedCameraX, lastPausedCameraY, lastPausedCameraZ;  // Store camera position when paused
    private float lastPausedCameraPitch, lastPausedCameraYaw;  // Store camera rotation when paused

    private static final int WIDTH = 1600;
    private static final int HEIGHT = 750;

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        window = glfwCreateWindow(WIDTH, HEIGHT, "Craftmine", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidmode != null) {
            glfwSetWindowPos(
                window,
                (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2
            );
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSwapInterval(1);
        glfwShowWindow(window);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(0.529f, 0.808f, 0.922f, 0.0f);

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS && isInGame) {
                isPaused = !isPaused;
                if (isPaused) {
                    ui.setTint(50); // Set tint to 50 when paused
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    // Store current mouse position and camera state
                    lastPausedX = lastX;
                    lastPausedY = lastY;
                    lastPausedCameraX = camera.getX();
                    lastPausedCameraY = camera.getY();
                    lastPausedCameraZ = camera.getZ();
                    lastPausedCameraPitch = camera.getPitch();
                    lastPausedCameraYaw = camera.getYaw();
                } else {
                    ui.setTint(0); // Clear tint when unpaused
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    // Center cursor and reset mouse tracking
                    glfwSetCursorPos(window, WIDTH/2, HEIGHT/2);
                    lastX = WIDTH/2;
                    lastY = HEIGHT/2;
                    firstMouse = true;
                    // Restore camera position and rotation
                    camera.setX(lastPausedCameraX);
                    camera.setY(lastPausedCameraY);
                    camera.setZ(lastPausedCameraZ);
                    camera.setPitch(lastPausedCameraPitch);
                    camera.setYaw(lastPausedCameraYaw);
                }
            }
            if (key >= 0 && key < 1024) {
                if (action == GLFW_PRESS) {
                    if (key == GLFW_KEY_M) {
                        ui.toggleCoordinates();
                    }
                    keys[key] = true;
                    System.out.println("Key pressed: " + key);  // Debug print
                } else if (action == GLFW_RELEASE) {
                    keys[key] = false;
                }
            }
        });

        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (!isInGame) {
                homeScreen.handleMousePosition(xpos, ypos);
            } else if (!isPaused) {
                if (firstMouse) {
                    lastX = xpos;
                    lastY = ypos;
                    firstMouse = false;
                    return;  // Skip processing on first mouse event
                }

                float xoffset = (float) (xpos - lastX);
                float yoffset = (float) (lastY - ypos);
                lastX = xpos;
                lastY = ypos;

                // Process mouse movement for camera in both first and third person
                camera.processMouseMovement(xoffset * mouseSensitivity, yoffset * mouseSensitivity);
                
                if (isThirdPerson) {
                    // Update camera position to follow player in third person
                    updateThirdPersonCamera();
                }
            } else {
                ui.handleMousePosition(xpos, ypos);
            }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (!isInGame) {
                if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                    double[] xpos = new double[1];
                    double[] ypos = new double[1];
                    glfwGetCursorPos(window, xpos, ypos);
                    if (homeScreen.handleMouseClick(xpos[0], ypos[0])) {
                        startGame();
                    }
                }
            } else if (isPaused && button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                glfwGetCursorPos(window, xpos, ypos);
                if (ui.handleMouseClick(xpos[0], ypos[0])) {
                    isPaused = false;
                    ui.setTint(0); // Reset tint when clicking back to game
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    // Center cursor and reset mouse tracking
                    glfwSetCursorPos(window, WIDTH/2, HEIGHT/2);
                    lastX = WIDTH/2;
                    lastY = HEIGHT/2;
                    firstMouse = true;
                    // Restore camera position and rotation
                    camera.setX(lastPausedCameraX);
                    camera.setY(lastPausedCameraY);
                    camera.setZ(lastPausedCameraZ);
                    camera.setPitch(lastPausedCameraPitch);
                    camera.setYaw(lastPausedCameraYaw);
                }
            }
        });

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    private void startGame() {
        System.out.println("Starting game...");

        isInGame = true;
        isPaused = false;
        isCreativeMode = homeScreen.isCreativeMode();
        System.out.println("Game mode: " + (isCreativeMode ? "Creative" : "Survival"));
        
        // Transfer skin processor and player model from home screen
        this.skinProcessor = homeScreen.getSkinProcessor();
        System.out.println("Transferring skin processor: " + (this.skinProcessor != null ? "success" : "failed"));
        if (this.skinProcessor != null) {
            System.out.println("Has skin: " + this.skinProcessor.hasSkin());
        }
        this.playerModel = new PlayerModel(skinProcessor);
        System.out.println("Created player model: " + (this.playerModel != null ? "success" : "failed"));
        
        // Initialize game objects - start player closer to ground
        camera = new Camera(TerrainGeneration.WORLD_SIZE / 2, 20, TerrainGeneration.WORLD_SIZE / 2);
        renderer = new Renderer(camera);
        ui = new UI(camera);

        // Setup collision handler (initially enabled)
        CollisionHandler collisionHandler = new CollisionHandler(renderer.getTerrain());
        collisionHandler.setDebugMode(true);
        camera.setCollisionHandler(collisionHandler);
        camera.setCollisionsEnabled(true);  // Explicitly enable collisions
        ui.setTint(0);

        // Clear any leftover state from homescreen
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.529f, 0.808f, 0.922f, 0.0f);
        ui.setTint(0);

        // Hide cursor for game
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        // Center cursor and reset mouse tracking
        glfwSetCursorPos(window, WIDTH/2, HEIGHT/2);
        lastX = WIDTH/2;
        lastY = HEIGHT/2;
        firstMouse = true;
        // Final initialization steps
        System.out.println("Game started successfully");
        ui.setTint(0); // Ensure tint is cleared at the very end
    }

    public void run() {
        init();

        float aspectRatio = (float) WIDTH / HEIGHT;
        long lastTime = System.nanoTime();
        
        // Initialize game objects after OpenGL context is created
        keys = new boolean[1024];  // Initialize keys array
        lastKeys = new boolean[1024];  // Initialize last keys array
        homeScreen = new HomeScreen();

        // Set initial cursor mode to normal for home screen
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        while (!glfwWindowShouldClose(window)) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f; // Convert to seconds
            lastTime = currentTime;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (!isInGame) {
                // Render home screen
                homeScreen.render(WIDTH, HEIGHT);
            } else {
                // Set up 3D projection for game
                glMatrixMode(GL_PROJECTION);
                glLoadIdentity();
                glFrustum(-aspectRatio, aspectRatio, -1.0f, 1.0f, 1.0f, 1000.0f);
                glMatrixMode(GL_MODELVIEW);
                glLoadIdentity();

                // Ensure proper OpenGL state for 3D rendering
                glEnable(GL_DEPTH_TEST);
                glEnable(GL_TEXTURE_2D);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                if (!isPaused) {
                    handleInput();
                    camera.updatePhysics(deltaTime, isCreativeMode);
                }
                
                renderer.render();

                // Render player model in world space
                if (skinProcessor != null && skinProcessor.hasSkin()) {
                    System.out.println("=== Rendering Player Model ===");
                    
                    // Set in-game flag for correct head positioning
                    playerModel.setInGame(true);
                    
                    // Save current matrices and states
                    glPushAttrib(GL_ALL_ATTRIB_BITS);
                    glMatrixMode(GL_PROJECTION);
                    glPushMatrix();
                    glLoadIdentity();
                    
                    // Use the same perspective as the world
                    glFrustum(-aspectRatio, aspectRatio, -1.0f, 1.0f, 1.0f, 1000.0f);
                    
                    glMatrixMode(GL_MODELVIEW);
                    glPushMatrix();
                    glLoadIdentity();
                    
                    // Apply camera transformation
                    glRotatef(camera.getPitch(), 1.0f, 0.0f, 0.0f);
                    glRotatef(camera.getYaw() + 180.0f, 0.0f, 1.0f, 0.0f);
                    glTranslatef((float)-camera.getX(), (float)-camera.getY(), (float)-camera.getZ());
                    
                    // Enable states for model rendering
                    glEnable(GL_DEPTH_TEST);
                    glEnable(GL_TEXTURE_2D);
                    glEnable(GL_CULL_FACE);
                    glCullFace(GL_BACK);
                    
                    // Position model in world space
                    float playerX, playerY, playerZ;
                    if (isThirdPerson) {
                        // In third person, use the stored position
                        playerX = thirdPersonLockPoint[0];
                        playerY = thirdPersonLockPoint[1];  
                        playerZ = thirdPersonLockPoint[2];
                    } else {
                        // In first person, follow the camera
                        playerX = (float)camera.getX();
                        playerY = (float)camera.getY() - 3.0f;
                        playerZ = (float)camera.getZ();
                    }
                    
                    // Calculate position in front of the player
                    float angle = (float)Math.toRadians(camera.getYaw());
                    
                    float offsetX = (float)Math.sin(angle) * 2.0f;  // 2 blocks in front
                    float offsetZ = (float)Math.cos(angle) * 2.0f;
                    
                    glTranslatef(playerX + offsetX, playerY, playerZ + offsetZ);
                    float modelYaw = camera.getYaw();  // Body rotates with camera
                    
                    glRotatef(modelYaw, 0.0f, 1.0f, 0.0f);
                    glScalef(0.15f, 0.15f, 0.15f);
                    
                    // Render the model with head rotation
                    float headYaw = 0;  // Head stays aligned with body since body rotates
                    float headPitch = camera.getPitch();  // Head pitch follows camera
                    
                    skinProcessor.bindSkinTexture();
                    playerModel.renderWithHeadRotation(1.0f, headYaw, headPitch);
                    
                    // Restore states
                    glPopMatrix();
                    glMatrixMode(GL_PROJECTION);
                    glPopMatrix();
                    glPopAttrib();
                    
                    glMatrixMode(GL_MODELVIEW);
                } else {
                    if (skinProcessor == null) {
                        System.out.println("Not rendering player model: skinProcessor is null");
                    } else {
                        System.out.println("Not rendering player model: no skin loaded");
                    }
                }

                // Now render UI (coordinates and pause menu if needed)
                glDisable(GL_DEPTH_TEST);
                glDisable(GL_TEXTURE_2D);
                if (isPaused) {
                    ui.renderPauseMenu(WIDTH, HEIGHT);  // Render pause menu
                } else {
                    ui.render(WIDTH, HEIGHT);  // Render coordinates
                }
                glEnable(GL_DEPTH_TEST);
                glEnable(GL_TEXTURE_2D);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();

            // Update last keys state
            System.arraycopy(keys, 0, lastKeys, 0, keys.length);
        }

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void handleInput() {
        // Handle third-person toggle with J key
        boolean isJPressed = keys[GLFW_KEY_J];
        if (isJPressed && !wasJPressed) {
            if (!isThirdPerson) {
                // Switching to third person - store current position
                thirdPersonLockPoint[0] = (float)camera.getX();
                thirdPersonLockPoint[1] = (float)camera.getY();
                thirdPersonLockPoint[2] = (float)camera.getZ();
                isThirdPerson = true;
                // Initialize camera position for third person
                updateThirdPersonCamera();
            } else {
                // Switching to first person - move camera to lock point and restore original rotation
                camera.setPosition(
                    thirdPersonLockPoint[0],
                    thirdPersonLockPoint[1],
                    thirdPersonLockPoint[2]
                );
                isThirdPerson = false;
            }
        }
        wasJPressed = isJPressed;

        if (!isInGame || isPaused) return;

        // Toggle collisions with C key
        if (keys[GLFW_KEY_C] && !lastKeys[GLFW_KEY_C]) {
            camera.setCollisionsEnabled(!camera.isCollisionsEnabled());
            System.out.println("Collisions " + (camera.isCollisionsEnabled() ? "enabled" : "disabled"));
        }

        // Calculate move speed based on mode and crouch state
        float moveSpeed;
        boolean isCrouching = !isCreativeMode && (keys[GLFW_KEY_LEFT_SHIFT] || keys[GLFW_KEY_RIGHT_SHIFT]);
        if (isCreativeMode) {
            moveSpeed = 1.0f;
        } else if (isCrouching) {
            moveSpeed = 0.125f;  // Crouch speed (half of normal survival speed)
        } else {
            moveSpeed = 0.25f;   // Normal survival speed (quarter of creative speed)
        }

        float dx = 0.0f, dy = 0.0f, dz = 0.0f;

        // Movement only if not crouching in survival mode, or always in creative mode
        if (isCreativeMode || !isCrouching) {
            if (isThirdPerson) {
                // Third-person movement (relative to camera direction)
                float moveAngle = camera.getYaw();  // Use camera yaw for movement direction
                if (keys[GLFW_KEY_W]) {
                    dx -= moveSpeed * (float)Math.sin(Math.toRadians(moveAngle));
                    dz -= moveSpeed * (float)Math.cos(Math.toRadians(moveAngle));
                }
                if (keys[GLFW_KEY_S]) {
                    dx += moveSpeed * (float)Math.sin(Math.toRadians(moveAngle));
                    dz += moveSpeed * (float)Math.cos(Math.toRadians(moveAngle));
                }
                if (keys[GLFW_KEY_A]) {
                    dx -= moveSpeed * (float)Math.sin(Math.toRadians(moveAngle + 90));
                    dz -= moveSpeed * (float)Math.cos(Math.toRadians(moveAngle + 90));
                }
                if (keys[GLFW_KEY_D]) {
                    dx += moveSpeed * (float)Math.sin(Math.toRadians(moveAngle + 90));
                    dz += moveSpeed * (float)Math.cos(Math.toRadians(moveAngle + 90));
                }
            } else {
                // First-person movement (regular movement)
                if (keys[GLFW_KEY_A]) {
                    dz = -moveSpeed;
                }
                if (keys[GLFW_KEY_D]) {
                    dz = moveSpeed;
                }
                if (keys[GLFW_KEY_W]) {
                    dx = -moveSpeed;
                }
                if (keys[GLFW_KEY_S]) {
                    dx = moveSpeed;
                }
            }
        }
        
        // Handle jumping in survival mode or flying in creative mode
        if (keys[GLFW_KEY_SPACE]) {
            if (isCreativeMode) {
                dy = moveSpeed;
            } else if (!isCrouching) {  // Can't jump while crouching
                if (isThirdPerson) {
                    // In third person, apply jump to lock point
                    camera.jump();
                    thirdPersonLockPoint[1] = (float)camera.getY();
                } else {
                    camera.jump();
                }
            }
        }
        
        // Handle crouching in survival mode or flying down in creative mode
        if (!isCreativeMode) {
            camera.setCrouching(isCrouching);
        } else if (keys[GLFW_KEY_LEFT_SHIFT] || keys[GLFW_KEY_RIGHT_SHIFT]) {
            dy = -moveSpeed;
        }

        if (dx != 0 || dy != 0 || dz != 0) {
            if (isThirdPerson) {
                // In third person, move the lock point instead of the camera
                thirdPersonLockPoint[0] += dx;
                if (isCreativeMode) thirdPersonLockPoint[1] += dy;
                thirdPersonLockPoint[2] += dz;
                updateThirdPersonCamera();
            } else {
                camera.move(dx, dy, dz);
            }
        }
        
        // Update physics only in first person or if we need to update the lock point height
        if (!isThirdPerson || !isCreativeMode) {
            camera.updatePhysics(1.0f / 60.0f, isCreativeMode);
            if (isThirdPerson) {
                // Keep lock point updated with physics in third person
                thirdPersonLockPoint[1] = (float)camera.getY();
                updateThirdPersonCamera();
            }
        }
    }

    private void updateThirdPersonCamera() {
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();
        
        // Limit the pitch range for third-person to avoid extreme angles
        if (pitch > 60.0f) pitch = 60.0f;
        if (pitch < -60.0f) pitch = -60.0f;
        camera.setPitch(pitch);
        
        // Calculate distance based on pitch to avoid clipping into ground at extreme angles
        float distance = 5.0f;
        
        // Calculate camera position with orbital rotation
        // This keeps the camera at a fixed distance but allows orbiting around the player
        float horizontalDistance = distance * (float)Math.cos(Math.toRadians(pitch));
        float verticalDistance = distance * (float)Math.sin(Math.toRadians(pitch));
        
        // Position camera with orbital rotation
        float cameraX = thirdPersonLockPoint[0] - horizontalDistance * (float)Math.sin(Math.toRadians(yaw));
        float cameraY = thirdPersonLockPoint[1] + verticalDistance + 4.0f; // Increased vertical offset by 3 blocks (from 1.0f to 4.0f)
        float cameraZ = thirdPersonLockPoint[2] - horizontalDistance * (float)Math.cos(Math.toRadians(yaw));
        
        // Update camera position
        camera.setPosition(cameraX, cameraY, cameraZ);
    }

    public static void main(String[] args) {
        new Game().run();
    }
}
