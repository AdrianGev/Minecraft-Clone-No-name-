package com.craftmine;

import static org.lwjgl.opengl.GL11.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

public class HomeScreen {
    private int backgroundTexture;
    private int singleplayerTexture;
    private int multiplayerTexture;
    private int optionsTexture;
    private int chooseSkinTexture;
    private int gamemodeCreativeTexture;
    private int gamemodeSurvivalTexture;
    private float buttonX;
    private float buttonY;
    private float buttonWidth;
    private float wideButtonWidth;  // For singleplayer and multiplayer buttons
    private float buttonHeight;
    private float buttonSpacing;
    private boolean isSingleplayerHovered;
    private boolean isMultiplayerHovered;
    private boolean isOptionsHovered;
    private boolean isChooseSkinHovered;
    private boolean isGamemodeHovered;
    private boolean isCreativeMode = true;  // Default to creative mode
    private Camera camera;
    private Renderer renderer;
    private UI ui;
    private MinecraftSkinProcessor skinProcessor;
    private PlayerModel playerModel;
    private float lastMouseX;
    private boolean isDragging;
    private int windowWidth;
    private int windowHeight;

    public HomeScreen() {
        this.buttonWidth = 200;
        this.wideButtonWidth = 400;  // Twice as wide
        this.buttonHeight = 50;
        this.buttonSpacing = 20;
        this.isSingleplayerHovered = false;
        this.isMultiplayerHovered = false;
        this.isOptionsHovered = false;
        this.isChooseSkinHovered = false;
        this.isGamemodeHovered = false;
        this.skinProcessor = new MinecraftSkinProcessor();
        this.playerModel = new PlayerModel(skinProcessor);
        this.isDragging = false;
        loadTextures();
        
        // Try multiple possible paths for the default skin
        String[] possiblePaths = {
            "src/main/resources/assets/steve.png",
            "assets/steve.png",
            System.getProperty("user.dir") + "/src/main/resources/assets/steve.png",
            System.getProperty("user.dir") + "/assets/steve.png"
        };
        
        boolean skinLoaded = false;
        for (String path : possiblePaths) {
            System.out.println("Trying to load skin from: " + path);
            File skinFile = new File(path);
            if (skinFile.exists()) {
                System.out.println("Found skin file at: " + path);
                if (skinProcessor.loadSkin(path)) {
                    System.out.println("Successfully loaded skin from: " + path);
                    skinLoaded = true;
                    break;
                }
            }
        }
        
        if (!skinLoaded) {
            System.out.println("Failed to load skin from any location. Paths tried:");
            for (String path : possiblePaths) {
                System.out.println("- " + path);
            }
        }
    }

    private void loadTextures() {
        backgroundTexture = TextureLoader.loadTexture("assets/homescreen.png");
        singleplayerTexture = TextureLoader.loadTexture("assets/singleplayerbutton.png");
        multiplayerTexture = TextureLoader.loadTexture("assets/multiplayerbutton.png");
        optionsTexture = TextureLoader.loadTexture("assets/optionsbutton.png");
        chooseSkinTexture = TextureLoader.loadTexture("assets/choosenewskinbutton.png");
        gamemodeCreativeTexture = TextureLoader.loadTexture("assets/gamemodecreativebutton.png");
        gamemodeSurvivalTexture = TextureLoader.loadTexture("assets/gamemodesurvivalbutton.png");
    }

    private void setPerspectiveProjection(float fovY, float aspect, float zNear, float zFar) {
        float f = (float) (1.0f / Math.tan(fovY * Math.PI / 360.0f));
        float[] perspective = new float[16];
        
        perspective[0] = f / aspect;
        perspective[5] = f;
        perspective[10] = (zFar + zNear) / (zNear - zFar);
        perspective[11] = -1.0f;
        perspective[14] = (2.0f * zFar * zNear) / (zNear - zFar);
        perspective[15] = 0.0f;
        
        glLoadMatrixf(perspective);
    }

    public void render(int windowWidth, int windowHeight) {
        // Store window dimensions
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        // Save OpenGL state
        boolean depthEnabled = glIsEnabled(GL_DEPTH_TEST);
        boolean textureEnabled = glIsEnabled(GL_TEXTURE_2D);
        
        // Set up OpenGL state for 2D rendering
        glDisable(GL_DEPTH_TEST);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, windowWidth, windowHeight, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        // Calculate total height of all buttons including spacing
        float totalButtonsHeight = (4 * buttonHeight) + (3 * buttonSpacing);
        
        // Center the buttons vertically and horizontally
        buttonX = (windowWidth - wideButtonWidth) / 2;  // Use wide button width for centering
        buttonY = (windowHeight - totalButtonsHeight) / 2;

        // Draw background
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, backgroundTexture);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f); glVertex2f(0, 0);
        glTexCoord2f(1.0f, 0.0f); glVertex2f(windowWidth, 0);
        glTexCoord2f(1.0f, 1.0f); glVertex2f(windowWidth, windowHeight);
        glTexCoord2f(0.0f, 1.0f); glVertex2f(0, windowHeight);
        glEnd();

        // Draw Singleplayer button (wide)
        drawButton(buttonX, buttonY, singleplayerTexture, isSingleplayerHovered, wideButtonWidth);
        
        // Draw Multiplayer button (wide)
        drawButton(buttonX, buttonY + buttonHeight + buttonSpacing, multiplayerTexture, isMultiplayerHovered, wideButtonWidth);
        
        // Draw Options button (normal width, centered) and Choose Skin button
        float optionsX = (windowWidth - (2 * buttonWidth + 20)) / 2;  // Center both buttons with 20px spacing
        drawButton(optionsX, buttonY + (2 * (buttonHeight + buttonSpacing)), optionsTexture, isOptionsHovered, buttonWidth);
        drawButton(optionsX + buttonWidth + 20, buttonY + (2 * (buttonHeight + buttonSpacing)), chooseSkinTexture, isChooseSkinHovered, buttonWidth);

        // Draw Gamemode button (normal width, centered)
        float gamemodeY = buttonY + (3 * (buttonHeight + buttonSpacing));
        drawButton(optionsX, gamemodeY, isCreativeMode ? gamemodeCreativeTexture : gamemodeSurvivalTexture, isGamemodeHovered, buttonWidth);

        glDisable(GL_TEXTURE_2D);

        // After drawing all 2D elements, render the 3D player model
        if (skinProcessor != null && skinProcessor.hasSkin()) {
            System.out.println("=== Starting 3D Model Render ===");
            System.out.println("Window dimensions: " + windowWidth + "x" + windowHeight);
            
            // Save matrices
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            
            // Set up 3D rendering
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_CULL_FACE);  // Enable face culling
            glCullFace(GL_BACK);     // Cull back faces
            
            // Clear only depth buffer
            glClear(GL_DEPTH_BUFFER_BIT);
            
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            
            // Set up perspective with a wider field of view
            float aspectRatio = (float) windowWidth / windowHeight;
            setPerspectiveProjection(45.0f, aspectRatio, 0.1f, 1000.0f);
            
            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();
            
            // Position camera
            glTranslatef(0.0f, -100.0f, -400.0f);
            
            // Render the player model
            glPushMatrix();
            glTranslatef(200.0f, 100.0f, 0.0f); // Position model
            glRotatef(45.0f, 0.0f, 1.0f, 0.0f); // Rotate for better view
            float modelScale = windowHeight / 400.0f;
            playerModel.render(modelScale);
            glPopMatrix();
            
            // Restore matrices and states
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
            glPopMatrix();
            
            glPopAttrib();
            
            System.out.println("=== 3D Model Render Complete ===");
        } else {
            System.out.println("Not rendering model. SkinProcessor: " + (skinProcessor != null) + 
                             ", HasSkin: " + (skinProcessor != null ? skinProcessor.hasSkin() : "null"));
        }

        // Restore OpenGL state
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        
        if (depthEnabled) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }
        if (textureEnabled) {
            glEnable(GL_TEXTURE_2D);
        }
    }

    private void drawButton(float x, float y, int texture, boolean isHovered, float width) {
        glBindTexture(GL_TEXTURE_2D, texture);
        
        // Set color based on hover state
        if (isHovered) {
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);  // Full brightness when hovered
        } else {
            glColor4f(0.8f, 0.8f, 0.8f, 1.0f);  // Slightly dimmed when not hovered
        }

        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f); glVertex2f(x, y);
        glTexCoord2f(1.0f, 0.0f); glVertex2f(x + width, y);
        glTexCoord2f(1.0f, 1.0f); glVertex2f(x + width, y + buttonHeight);
        glTexCoord2f(0.0f, 1.0f); glVertex2f(x, y + buttonHeight);
        glEnd();
    }

    public void handleMousePosition(double mouseX, double mouseY) {
        // Update model rotation if dragging
        if (isDragging) {
            float deltaX = (float) mouseX - lastMouseX;
            playerModel.updateRotation(deltaX * 0.5f);
        }
        lastMouseX = (float) mouseX;

        // Check Singleplayer button (wide)
        isSingleplayerHovered = isMouseOverButton(mouseX, mouseY, buttonY, wideButtonWidth);
        
        // Check Multiplayer button (wide)
        isMultiplayerHovered = isMouseOverButton(mouseX, mouseY, buttonY + buttonHeight + buttonSpacing, wideButtonWidth);
        
        // Check Options button (normal width)
        float optionsX = (windowWidth - (2 * buttonWidth + 20)) / 2;
        isOptionsHovered = mouseX >= optionsX && mouseX <= optionsX + buttonWidth &&
                          mouseY >= buttonY + (2 * (buttonHeight + buttonSpacing)) && 
                          mouseY <= buttonY + (2 * (buttonHeight + buttonSpacing)) + buttonHeight;

        // Check Choose Skin button
        isChooseSkinHovered = mouseX >= optionsX + buttonWidth + 20 && mouseX <= optionsX + 2 * buttonWidth + 20 &&
                             mouseY >= buttonY + (2 * (buttonHeight + buttonSpacing)) && 
                             mouseY <= buttonY + (2 * (buttonHeight + buttonSpacing)) + buttonHeight;

        // Check Gamemode button (normal width)
        float gamemodeY = buttonY + (3 * (buttonHeight + buttonSpacing));
        isGamemodeHovered = mouseX >= optionsX && mouseX <= optionsX + buttonWidth &&
                           mouseY >= gamemodeY && mouseY <= gamemodeY + buttonHeight;
    }

    public boolean handleMouseClick(double mouseX, double mouseY) {
        // Handle model rotation
        if (mouseX > windowWidth * 0.6) {  // Only in the right portion of the screen
            isDragging = true;
            lastMouseX = (float) mouseX;
            return false;
        }

        // Check Choose Skin button
        float optionsX = (windowWidth - (2 * buttonWidth + 20)) / 2;
        float skinButtonX = optionsX + buttonWidth + 20;
        float buttonYPos = buttonY + (2 * (buttonHeight + buttonSpacing));
        if (mouseX >= skinButtonX && mouseX <= skinButtonX + buttonWidth &&
            mouseY >= buttonYPos && mouseY <= buttonYPos + buttonHeight) {
            openFinderFileDialog();
            return false;
        }

        // Check Gamemode button first
        float gamemodeY = buttonY + (3 * (buttonHeight + buttonSpacing));
        if (mouseX >= optionsX && mouseX <= optionsX + buttonWidth &&
            mouseY >= gamemodeY && mouseY <= gamemodeY + buttonHeight) {
            isCreativeMode = !isCreativeMode;  // Toggle gamemode
            return false;  // Don't start the game
        }
        
        // Then check singleplayer button
        if (isSingleplayerHovered) {
            return true;  // This will trigger the game start in Game.java
        }
        
        return false;
    }

    public void handleMouseRelease() {
        isDragging = false;
    }

    private boolean isMouseOverButton(double mouseX, double mouseY, float buttonYPos, float width) {
        return mouseX >= buttonX && mouseX <= buttonX + width &&
               mouseY >= buttonYPos && mouseY <= buttonYPos + buttonHeight;
    }

    public boolean isCreativeMode() {
        return isCreativeMode;
    }

    public MinecraftSkinProcessor getSkinProcessor() {
        return skinProcessor;
    }

    private void openFinderFileDialog() {
        try {
            String[] command = { "osascript", "-e",
                "set chosenFile to choose file\n" +
                "POSIX path of chosenFile"
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String filePath = reader.readLine();
            process.waitFor();

            if (filePath != null) {
                System.out.println("Selected file (macOS): " + filePath);
                if (skinProcessor.loadSkin(filePath)) {
                    System.out.println("Skin loaded successfully!");
                } else {
                    System.out.println("Failed to load skin. Make sure it's a valid 64x64 Minecraft skin.");
                }
            } else {
                System.out.println("No file selected.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
