package com.craftmine;

import static org.lwjgl.opengl.GL11.*;

public class UI {
    private float buttonX;
    private float buttonY;
    private float buttonWidth;
    private float buttonHeight;
    private boolean isButtonHovered;
    private boolean showCoordinates = false;
    private Camera camera;
    private int buttonTexture;
    private float tintValue = 0f;  // 0-100 tint value

    public UI(Camera camera) {
        this.buttonWidth = 500;  // Increased from 300 to 400
        this.buttonHeight = 50;  // Made taller for the texture
        this.buttonX = 650;  // Will be adjusted in render
        this.buttonY = 345;  // Will be adjusted in render
        this.isButtonHovered = false;
        this.showCoordinates = false;
        this.camera = camera;
        this.buttonTexture = TextureLoader.loadTexture("assets/backtogamebutton.png");
    }

    public void setTint(float value) {
        this.tintValue = Math.max(0, Math.min(100, value)); // Clamp between 0-100
    }

    public float getTint() {
        return tintValue;
    }

    public void render(int windowWidth, int windowHeight) {
        setupOrthoProjection(windowWidth, windowHeight);
        
        // Save OpenGL state
        boolean depthEnabled = glIsEnabled(GL_DEPTH_TEST);
        boolean textureEnabled = glIsEnabled(GL_TEXTURE_2D);
        boolean blendEnabled = glIsEnabled(GL_BLEND);
        
        // Apply tint if any
        if (tintValue > 0) {
            glDisable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            float alpha = tintValue / 100f * 0.8f; // Max alpha of 0.8 at tint 100
            glColor4f(0.0f, 0.0f, 0.0f, alpha);
            glBegin(GL_QUADS);
            glVertex2f(0, 0);
            glVertex2f(windowWidth, 0);
            glVertex2f(windowWidth, windowHeight);
            glVertex2f(0, windowHeight);
            glEnd();
        }

        // Only render coordinates if enabled
        if (showCoordinates) {
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);  // White text
            String coords = String.format("X: %.1f Y: %.1f Z: %.1f", 
                camera.getDisplayX(), camera.getDisplayY(), camera.getDisplayZ());
            renderString(10, 50, coords);
        }
        
        // Restore OpenGL state
        if (depthEnabled) glEnable(GL_DEPTH_TEST); else glDisable(GL_DEPTH_TEST);
        if (textureEnabled) glEnable(GL_TEXTURE_2D); else glDisable(GL_TEXTURE_2D);
        if (blendEnabled) glEnable(GL_BLEND); else glDisable(GL_BLEND);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f); // Reset color
        
        // Restore matrix
        glPopMatrix();
    }

    public void renderPauseMenu(int windowWidth, int windowHeight) {
        setupOrthoProjection(windowWidth, windowHeight);
        
        // Save OpenGL state
        boolean depthEnabled = glIsEnabled(GL_DEPTH_TEST);
        boolean textureEnabled = glIsEnabled(GL_TEXTURE_2D);
        boolean blendEnabled = glIsEnabled(GL_BLEND);
        
        // Center the button
        buttonX = (windowWidth - buttonWidth) / 2;
        buttonY = (windowHeight - buttonHeight) / 2;
        
        // Draw dark overlay with tint
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        float alpha = tintValue / 100f * 0.8f; // Max alpha of 0.8 at tint 100
        glColor4f(0.0f, 0.0f, 0.0f, alpha);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(windowWidth, 0);
        glVertex2f(windowWidth, windowHeight);
        glVertex2f(0, windowHeight);
        glEnd();
        
        // Draw button with texture
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, buttonTexture);
        
        if (isButtonHovered) {
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);  // Full brightness when hovered
        } else {
            glColor4f(0.8f, 0.8f, 0.8f, 1.0f);  // Slightly dimmed when not hovered
        }
        
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f); glVertex2f(buttonX, buttonY);
        glTexCoord2f(1.0f, 0.0f); glVertex2f(buttonX + buttonWidth, buttonY);
        glTexCoord2f(1.0f, 1.0f); glVertex2f(buttonX + buttonWidth, buttonY + buttonHeight);
        glTexCoord2f(0.0f, 1.0f); glVertex2f(buttonX, buttonY + buttonHeight);
        glEnd();
        
        // Restore OpenGL state
        if (depthEnabled) glEnable(GL_DEPTH_TEST); else glDisable(GL_DEPTH_TEST);
        if (textureEnabled) glEnable(GL_TEXTURE_2D); else glDisable(GL_TEXTURE_2D);
        if (blendEnabled) glEnable(GL_BLEND); else glDisable(GL_BLEND);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f); // Reset color
        
        // Restore matrix
        glPopMatrix();
    }

    private void setupOrthoProjection(int windowWidth, int windowHeight) {
        // Save matrix
        glPushMatrix();
        
        // Switch to 2D
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, windowWidth, windowHeight, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    private void renderString(float x, float y, String text) {
        glPushMatrix();
        glTranslatef(x, y, 0);
        glScalef(3.0f, -3.0f, 1.0f);  // Negative Y scale to flip text right-side up
        glLineWidth(5.0f);  // Make lines thicker for bold effect
        for (char c : text.toCharArray()) {
            glBegin(GL_LINES);
            drawCharacter(c);
            glEnd();
            glTranslatef(12.0f, 0.0f, 0.0f);
        }
        glLineWidth(1.0f);  // Reset line width
        glPopMatrix();
    }

    private void drawCharacter(char c) {
        // Simple vector font implementation
        switch (c) {
            case 'X':
                glVertex2f(0, 0); glVertex2f(8, 12);  // Diagonal line from bottom-left to top-right
                glVertex2f(0, 12); glVertex2f(8, 0);  // Diagonal line from top-left to bottom-right
                break;
            case 'Y':
                glVertex2f(0, 12); glVertex2f(4, 6);  // Top-left to middle
                glVertex2f(8, 12); glVertex2f(4, 6);  // Top-right to middle
                glVertex2f(4, 6); glVertex2f(4, 0);   // Middle to bottom
                break;
            case 'Z':
                glVertex2f(0, 12); glVertex2f(8, 12); // Top horizontal
                glVertex2f(8, 12); glVertex2f(0, 0);  // Diagonal
                glVertex2f(0, 0); glVertex2f(8, 0);   // Bottom horizontal
                break;
            case ':':
                glVertex2f(4, 8); glVertex2f(4, 9);   // Top dot
                glVertex2f(4, 3); glVertex2f(4, 4);   // Bottom dot
                break;
            case '.':
                glVertex2f(4, 0); glVertex2f(4, 1);   // Single dot
                break;
            case '-':
                glVertex2f(2, 6); glVertex2f(6, 6);   // Middle horizontal line
                break;
            case ' ':
                break;  // Space character - just move the cursor
            default:
                // For numbers 0-9
                if (c >= '0' && c <= '9') {
                    drawNumber(c - '0');
                }
        }
    }

    private void drawNumber(int num) {
        switch (num) {
            case 0:
                glVertex2f(0, 0); glVertex2f(8, 0);   // Bottom
                glVertex2f(8, 0); glVertex2f(8, 12);  // Right
                glVertex2f(8, 12); glVertex2f(0, 12); // Top
                glVertex2f(0, 12); glVertex2f(0, 0);  // Left
                break;
            case 1:
                glVertex2f(4, 0); glVertex2f(4, 12);  // Vertical line
                break;
            case 2:
                glVertex2f(0, 12); glVertex2f(8, 12); // Top
                glVertex2f(8, 12); glVertex2f(8, 6);  // Right top
                glVertex2f(8, 6); glVertex2f(0, 6);   // Middle
                glVertex2f(0, 6); glVertex2f(0, 0);   // Left bottom
                glVertex2f(0, 0); glVertex2f(8, 0);   // Bottom
                break;
            case 3:
                glVertex2f(0, 12); glVertex2f(8, 12); // Top
                glVertex2f(8, 12); glVertex2f(8, 0);  // Right
                glVertex2f(8, 0); glVertex2f(0, 0);   // Bottom
                glVertex2f(0, 6); glVertex2f(8, 6);   // Middle
                break;
            case 4:
                glVertex2f(0, 12); glVertex2f(0, 6);  // Left top
                glVertex2f(0, 6); glVertex2f(8, 6);   // Middle
                glVertex2f(8, 12); glVertex2f(8, 0);  // Right
                break;
            case 5:
                glVertex2f(8, 12); glVertex2f(0, 12); // Top
                glVertex2f(0, 12); glVertex2f(0, 6);  // Left top
                glVertex2f(0, 6); glVertex2f(8, 6);   // Middle
                glVertex2f(8, 6); glVertex2f(8, 0);   // Right bottom
                glVertex2f(8, 0); glVertex2f(0, 0);   // Bottom
                break;
            case 6:
                glVertex2f(8, 12); glVertex2f(0, 12); // Top
                glVertex2f(0, 12); glVertex2f(0, 0);  // Left
                glVertex2f(0, 0); glVertex2f(8, 0);   // Bottom
                glVertex2f(8, 0); glVertex2f(8, 6);   // Right bottom
                glVertex2f(8, 6); glVertex2f(0, 6);   // Middle
                break;
            case 7:
                glVertex2f(0, 12); glVertex2f(8, 12); // Top
                glVertex2f(8, 12); glVertex2f(8, 0);  // Right
                break;
            case 8:
                glVertex2f(0, 0); glVertex2f(8, 0);   // Bottom
                glVertex2f(8, 0); glVertex2f(8, 12);  // Right
                glVertex2f(8, 12); glVertex2f(0, 12); // Top
                glVertex2f(0, 12); glVertex2f(0, 0);  // Left
                glVertex2f(0, 6); glVertex2f(8, 6);   // Middle
                break;
            case 9:
                glVertex2f(8, 0); glVertex2f(8, 12);  // Right
                glVertex2f(8, 12); glVertex2f(0, 12); // Top
                glVertex2f(0, 12); glVertex2f(0, 6);  // Left top
                glVertex2f(0, 6); glVertex2f(8, 6);   // Middle
                break;
        }
    }

    public void handleMousePosition(double xpos, double ypos) {
        isButtonHovered = xpos >= buttonX && xpos <= buttonX + buttonWidth &&
                         ypos >= buttonY && ypos <= buttonY + buttonHeight;
    }

    public boolean handleMouseClick(double xpos, double ypos) {
        return xpos >= buttonX && xpos <= buttonX + buttonWidth &&
               ypos >= buttonY && ypos <= buttonY + buttonHeight;
    }

    public void toggleCoordinates() {
        showCoordinates = !showCoordinates;
    }
}
