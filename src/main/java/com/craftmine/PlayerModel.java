package com.craftmine;

import static org.lwjgl.opengl.GL11.*;

public class PlayerModel {
    private MinecraftSkinProcessor skinProcessor;
    private float rotationY = 0;
    private boolean isInGame = false;  // Add flag for in-game model
    
    // Standard Minecraft dimensions (in blocks)
    private static final float HEAD_SIZE = 8.0f;
    private static final float BODY_WIDTH = 8.0f;
    private static final float BODY_HEIGHT = 12.0f;
    private static final float BODY_DEPTH = 4.0f;
    private static final float ARM_WIDTH = 4.0f;
    private static final float ARM_HEIGHT = 12.0f;
    private static final float LEG_WIDTH = 4.0f;
    private static final float LEG_HEIGHT = 12.0f;
    
    public PlayerModel(MinecraftSkinProcessor skinProcessor) {
        this.skinProcessor = skinProcessor;
    }
    
    public void setRotationY(float rotation) {
        this.rotationY = rotation;
    }
    
    public void setInGame(boolean inGame) {
        this.isInGame = inGame;
    }
    
    public void render(float scale) {
        if (skinProcessor == null) {
            System.out.println("Cannot render: skinProcessor is null");
            return;
        }
        
        System.out.println("=== Starting Player Model Render ===");
        
        glPushMatrix();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        
        // Apply model rotation
        glRotatef(rotationY, 0, 1, 0);
        
        // Scale the entire model
        glScalef(scale, scale, scale);
        
        // Bind texture
        skinProcessor.bindSkinTexture();
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Render head with all faces
        renderHead(scale);
        
        // Body
        glPushMatrix();
        glTranslatef(-BODY_WIDTH/2, ARM_HEIGHT, -BODY_DEPTH/2);
        renderTorso(BODY_WIDTH, BODY_HEIGHT, BODY_DEPTH);
        glPopMatrix();
        
        // Right Arm
        glPushMatrix();
        glTranslatef(-BODY_WIDTH/2 - ARM_WIDTH, ARM_HEIGHT, -ARM_WIDTH/2);
        renderLimb(ARM_WIDTH, ARM_HEIGHT, ARM_WIDTH, true, true);
        glPopMatrix();
        
        // Left Arm
        glPushMatrix();
        glTranslatef(BODY_WIDTH/2, ARM_HEIGHT, -ARM_WIDTH/2);
        renderLimb(ARM_WIDTH, ARM_HEIGHT, ARM_WIDTH, true, false);
        glPopMatrix();
        
        // Right Leg - moved right by half leg width
        glPushMatrix();
        glTranslatef(-LEG_WIDTH/2 - LEG_WIDTH/2, 0, -LEG_WIDTH/2);
        renderLimb(LEG_WIDTH, LEG_HEIGHT, LEG_WIDTH, false, true);
        glPopMatrix();
        
        // Left Leg - moved right by half leg width
        glPushMatrix();
        glTranslatef(LEG_WIDTH/2 - LEG_WIDTH/2, 0, -LEG_WIDTH/2);
        renderLimb(LEG_WIDTH, LEG_HEIGHT, LEG_WIDTH, false, false);
        glPopMatrix();
        
        glPopMatrix();
        
        System.out.println("=== Player Model Render Complete ===");
    }
    
    public void renderWithHeadRotation(float scale, float headYaw, float headPitch) {
        if (skinProcessor == null) {
            System.out.println("Cannot render: skinProcessor is null");
            return;
        }
        
        glPushMatrix();
        glEnable(GL_TEXTURE_2D);
        
        // Scale the entire model
        glScalef(scale, scale, scale);
        
        // Bind texture
        skinProcessor.bindSkinTexture();
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Render head with rotation
        glPushMatrix();
        if (isInGame) {
            glTranslatef(0.0f, HEAD_SIZE - 8.0f, 0.0f);  // Lower head position for in-game model
        } else {
            glTranslatef(0.0f, HEAD_SIZE + BODY_HEIGHT, 0.0f);  // Original position for home screen
        }
        glRotatef(headYaw, 0.0f, 1.0f, 0.0f);  // Apply yaw
        glRotatef(headPitch, 1.0f, 0.0f, 0.0f);  // Apply pitch
        renderHead(1.0f);
        glPopMatrix();
        
        // Render body parts (no rotation)
        renderBody(1.0f);
        renderArms(1.0f);
        renderLegs(1.0f);
        
        glPopMatrix();
    }
    
    private void renderHead(float scale) {
        glPushMatrix();
        // Move head above body but lower than before
        glTranslatef(-HEAD_SIZE/2, BODY_HEIGHT + ARM_HEIGHT, -HEAD_SIZE/2);
        
        // Front face - moved forward
        glPushMatrix();
        glTranslatef(0, 0, HEAD_SIZE);
        renderFace(HEAD_SIZE, HEAD_SIZE, 0, skinProcessor.getHeadFrontUV(), true);
        glPopMatrix();
        
        // Back face
        glPushMatrix();
        glTranslatef(HEAD_SIZE, 0, 0);
        glRotatef(180, 0, 1, 0);
        renderFace(HEAD_SIZE, HEAD_SIZE, 0, skinProcessor.getHeadBackUV(), true);
        glPopMatrix();
        
        // Right face
        glPushMatrix();
        glTranslatef(HEAD_SIZE, 0, HEAD_SIZE);
        glRotatef(90, 0, 1, 0);
        renderFace(HEAD_SIZE, HEAD_SIZE, 0, skinProcessor.getHeadRightUV(), true);
        glPopMatrix();
        
        // Left face
        glPushMatrix();
        glTranslatef(0, 0, 0);
        glRotatef(-90, 0, 1, 0);
        renderFace(HEAD_SIZE, HEAD_SIZE, 0, skinProcessor.getHeadLeftUV(), true);
        glPopMatrix();
        
        // Top face
        glPushMatrix();
        glTranslatef(0, HEAD_SIZE, HEAD_SIZE);
        glRotatef(-90, 1, 0, 0);
        renderFace(HEAD_SIZE, HEAD_SIZE, 0, skinProcessor.getHeadTopUV(), true);
        glPopMatrix();
        
        // Bottom face
        glPushMatrix();
        glTranslatef(0, 0, 0);
        glRotatef(90, 1, 0, 0);
        renderFace(HEAD_SIZE, HEAD_SIZE, 0, skinProcessor.getHeadBottomUV(), true);
        glPopMatrix();
        
        glPopMatrix();
    }
    
    private void renderTorso(float width, float height, float depth) {
        // Front face
        glPushMatrix();
        glTranslatef(0, 0, depth);
        renderFace(width, height, 0, skinProcessor.getTorsoFrontUV(), true);
        glPopMatrix();
        
        // Back face
        glPushMatrix();
        glTranslatef(width, 0, 0);
        glRotatef(180, 0, 1, 0);
        renderFace(width, height, 0, skinProcessor.getTorsoBackUV(), true);
        glPopMatrix();
        
        // Right face
        glPushMatrix();
        glTranslatef(width, 0, depth);
        glRotatef(90, 0, 1, 0);
        renderFace(depth, height, 0, skinProcessor.getTorsoRightUV(), true);
        glPopMatrix();
        
        // Left face
        glPushMatrix();
        glRotatef(-90, 0, 1, 0);
        renderFace(depth, height, 0, skinProcessor.getTorsoLeftUV(), true);
        glPopMatrix();
        
        // Top face
        glPushMatrix();
        glTranslatef(0, height, depth);
        glRotatef(-90, 1, 0, 0);
        renderFace(width, depth, 0, skinProcessor.getTorsoTopUV(), true);
        glPopMatrix();
        
        // Bottom face
        glPushMatrix();
        glRotatef(90, 1, 0, 0);
        renderFace(width, depth, 0, skinProcessor.getTorsoBottomUV(), true);
        glPopMatrix();
    }
    
    private void renderLimb(float width, float height, float depth, boolean isArm, boolean isRight) {
        // Front face
        glPushMatrix();
        glTranslatef(0, 0, depth);
        renderFace(width, height, 0, isArm ? 
            (isRight ? skinProcessor.getRightArmFrontUV() : skinProcessor.getLeftArmFrontUV()) :
            (isRight ? skinProcessor.getRightLegFrontUV() : skinProcessor.getLeftLegFrontUV()), 
            true);
        glPopMatrix();
        
        // Back face
        glPushMatrix();
        glTranslatef(width, 0, 0);
        glRotatef(180, 0, 1, 0);
        renderFace(width, height, 0, isArm ? 
            (isRight ? skinProcessor.getRightArmBackUV() : skinProcessor.getLeftArmBackUV()) :
            (isRight ? skinProcessor.getRightLegBackUV() : skinProcessor.getLeftLegBackUV()), 
            true);
        glPopMatrix();
        
        // Right face
        glPushMatrix();
        glTranslatef(width, 0, depth);
        glRotatef(90, 0, 1, 0);
        renderFace(depth, height, 0, isArm ? 
            (isRight ? skinProcessor.getRightArmRightUV() : skinProcessor.getLeftArmRightUV()) :
            (isRight ? skinProcessor.getRightLegRightUV() : skinProcessor.getLeftLegRightUV()), 
            true);
        glPopMatrix();
        
        // Left face
        glPushMatrix();
        glRotatef(-90, 0, 1, 0);
        renderFace(depth, height, 0, isArm ? 
            (isRight ? skinProcessor.getRightArmLeftUV() : skinProcessor.getLeftArmLeftUV()) :
            (isRight ? skinProcessor.getRightLegLeftUV() : skinProcessor.getLeftLegLeftUV()), 
            true);
        glPopMatrix();
        
        // Top face
        glPushMatrix();
        glTranslatef(0, height, depth);
        glRotatef(-90, 1, 0, 0);
        renderFace(width, depth, 0, isArm ? 
            (isRight ? skinProcessor.getRightArmFrontUV() : skinProcessor.getLeftArmFrontUV()) :
            (isRight ? skinProcessor.getRightLegFrontUV() : skinProcessor.getLeftLegFrontUV()), 
            true);
        glPopMatrix();
        
        // Bottom face
        glPushMatrix();
        glRotatef(90, 1, 0, 0);
        renderFace(width, depth, 0, isArm ? 
            (isRight ? skinProcessor.getRightArmFrontUV() : skinProcessor.getLeftArmFrontUV()) :
            (isRight ? skinProcessor.getRightLegFrontUV() : skinProcessor.getLeftLegFrontUV()), 
            true);
        glPopMatrix();
    }
    
    private void renderFace(float width, float height, float depth, float[] uvCoords, boolean rotate180) {
        float u1 = uvCoords[0];
        float v1 = uvCoords[1];
        float u2 = uvCoords[2];
        float v2 = uvCoords[3];
        
        if (rotate180) {
            // Swap UV coordinates to rotate texture 180 degrees
            float temp = u1;
            u1 = u2;
            u2 = temp;
            temp = v1;
            v1 = v2;
            v2 = temp;
        }
        
        glBegin(GL_QUADS);
        glTexCoord2f(u1, v1);
        glVertex3f(0, 0, depth);
        glTexCoord2f(u2, v1);
        glVertex3f(width, 0, depth);
        glTexCoord2f(u2, v2);
        glVertex3f(width, height, depth);
        glTexCoord2f(u1, v2);
        glVertex3f(0, height, depth);
        glEnd();
    }
    
    private void renderBox(float width, float height, float depth, float[] uvCoords) {
        float u1 = uvCoords[0];
        float v1 = uvCoords[1];
        float u2 = uvCoords[2];
        float v2 = uvCoords[3];
        
        glBegin(GL_QUADS);
        
        // Front face (rotated 180 degrees)
        glTexCoord2f(u2, v2);
        glVertex3f(0, 0, depth);
        glTexCoord2f(u1, v2);
        glVertex3f(width, 0, depth);
        glTexCoord2f(u1, v1);
        glVertex3f(width, height, depth);
        glTexCoord2f(u2, v1);
        glVertex3f(0, height, depth);
        
        // Back face (rotated 180 degrees)
        glTexCoord2f(u1, v2);
        glVertex3f(0, 0, 0);
        glTexCoord2f(u2, v2);
        glVertex3f(width, 0, 0);
        glTexCoord2f(u2, v1);
        glVertex3f(width, height, 0);
        glTexCoord2f(u1, v1);
        glVertex3f(0, height, 0);
        
        // Top face (rotated 180 degrees)
        glTexCoord2f(u2, v2);
        glVertex3f(0, height, 0);
        glTexCoord2f(u1, v2);
        glVertex3f(width, height, 0);
        glTexCoord2f(u1, v1);
        glVertex3f(width, height, depth);
        glTexCoord2f(u2, v1);
        glVertex3f(0, height, depth);
        
        // Bottom face (rotated 180 degrees)
        glTexCoord2f(u2, v1);
        glVertex3f(0, 0, depth);
        glTexCoord2f(u1, v1);
        glVertex3f(width, 0, depth);
        glTexCoord2f(u1, v2);
        glVertex3f(width, 0, 0);
        glTexCoord2f(u2, v2);
        glVertex3f(0, 0, 0);
        
        // Right face (rotated 180 degrees)
        glTexCoord2f(u1, v2);
        glVertex3f(width, 0, 0);
        glTexCoord2f(u2, v2);
        glVertex3f(width, 0, depth);
        glTexCoord2f(u2, v1);
        glVertex3f(width, height, depth);
        glTexCoord2f(u1, v1);
        glVertex3f(width, height, 0);
        
        // Left face (rotated 180 degrees)
        glTexCoord2f(u2, v2);
        glVertex3f(0, 0, 0);
        glTexCoord2f(u1, v2);
        glVertex3f(0, 0, depth);
        glTexCoord2f(u1, v1);
        glVertex3f(0, height, depth);
        glTexCoord2f(u2, v1);
        glVertex3f(0, height, 0);
        
        glEnd();
    }
    
    private void renderBody(float scale) {
        // Body
        glPushMatrix();
        glTranslatef(-BODY_WIDTH/2, ARM_HEIGHT, -BODY_DEPTH/2);
        renderTorso(BODY_WIDTH, BODY_HEIGHT, BODY_DEPTH);
        glPopMatrix();
    }
    
    private void renderArms(float scale) {
        // Right Arm
        glPushMatrix();
        glTranslatef(-BODY_WIDTH/2 - ARM_WIDTH, ARM_HEIGHT, -ARM_WIDTH/2);
        renderLimb(ARM_WIDTH, ARM_HEIGHT, ARM_WIDTH, true, true);
        glPopMatrix();
        
        // Left Arm
        glPushMatrix();
        glTranslatef(BODY_WIDTH/2, ARM_HEIGHT, -ARM_WIDTH/2);
        renderLimb(ARM_WIDTH, ARM_HEIGHT, ARM_WIDTH, true, false);
        glPopMatrix();
    }
    
    private void renderLegs(float scale) {
        // Right Leg - moved right by half leg width
        glPushMatrix();
        glTranslatef(-LEG_WIDTH/2 - LEG_WIDTH/2, 0, -LEG_WIDTH/2);
        renderLimb(LEG_WIDTH, LEG_HEIGHT, LEG_WIDTH, false, true);
        glPopMatrix();
        
        // Left Leg - moved right by half leg width
        glPushMatrix();
        glTranslatef(LEG_WIDTH/2 - LEG_WIDTH/2, 0, -LEG_WIDTH/2);
        renderLimb(LEG_WIDTH, LEG_HEIGHT, LEG_WIDTH, false, false);
        glPopMatrix();
    }
    
    public void updateRotation(float deltaY) {
        rotationY += deltaY;
        if (rotationY > 360) rotationY -= 360;
        if (rotationY < 0) rotationY += 360;
    }
}
