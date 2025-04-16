package com.craftmine;

public class Camera {
    private CollisionHandler collisionHandler;
    private float x, y, z;
    private float yaw, pitch;
    // Base movement speed – adjust as needed
    private float speed = 0.25f;
    private boolean collisionsEnabled = true;  // Default to true
    // Physics properties
    private boolean isJumping = false;
    private float verticalVelocity = 0.0f;
    private boolean isOnGround = false;
    private boolean isCrouching = false;
    private float crouchOffset = 0.0f;
    private static final float GRAVITY = -20.0f;
    private static final float JUMP_FORCE = 8.0f;
    private static final float MAX_CROUCH_OFFSET = 0.5f;
    private static final float CROUCH_SPEED = 8.0f; 
    private float displayX, displayY, displayZ;

    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        // Start with yaw = -90 to look along negative Z
        this.yaw = -90.0f;
        this.pitch = 0.0f;
        updateDisplayCoordinates();
    }

    // Update camera angles based on mouse movement
    public void processMouseMovement(float xoffset, float yoffset) {
        yaw += xoffset;
        pitch -= yoffset;  // Subtract so that moving the mouse up increases pitch

        // Clamp the pitch value to prevent screen flipping
        if (pitch > 89.0f) {
            pitch = 89.0f;
        }
        if (pitch < -89.0f) {
            pitch = -89.0f;
        }
    }

    public void updatePhysics(float deltaTime, boolean isCreativeMode) {
        if (!isCreativeMode) {
            // Apply gravity
            verticalVelocity += GRAVITY * deltaTime;
            float newY = y + verticalVelocity * deltaTime;

            // Check if we can move vertically
            if (collisionHandler != null && collisionHandler.isPositionValid(x, newY - crouchOffset, z)) {
                y = newY;
                isOnGround = !collisionHandler.isPositionValid(x, y - crouchOffset - 0.1f, z);
            } else {
                verticalVelocity = 0;
                isOnGround = true;
            }

            // Simple crouch handling
            if (isCrouching) {
                crouchOffset = Math.min(crouchOffset + CROUCH_SPEED * deltaTime, MAX_CROUCH_OFFSET);
                System.out.println("Crouching - offset: " + crouchOffset);
            } else {
                crouchOffset = Math.max(crouchOffset - CROUCH_SPEED * deltaTime, 0);
                System.out.println("Standing - offset: " + crouchOffset);
            }
        }
        updateDisplayCoordinates();
    }

    public void setCrouching(boolean crouching) {
        if (this.isCrouching != crouching) {
            System.out.println("Crouch state changed to: " + crouching);
            this.isCrouching = crouching;
        }
    }

    private void updateDisplayCoordinates() {
        displayX = x;
        displayY = y - crouchOffset;
        displayZ = z;
        if (crouchOffset > 0) {
            System.out.println("Display Y: " + displayY + " (raw Y: " + y + ", offset: " + crouchOffset + ")");
        }
    }

    public void jump() {
        if (isOnGround && !isCrouching) {  // Can only jump if on ground and not crouching
            verticalVelocity = JUMP_FORCE;
            isOnGround = false;
        }
    }

    public void move(float dx, float dy, float dz) {
        if (dx == 0 && dy == 0 && dz == 0) return;

        float yawRad = (float)Math.toRadians(yaw);
        float newX = x, newY = y, newZ = z;

        if (dz != 0) {
            newX += (float)(Math.cos(yawRad) * dz);
            newZ += (float)(Math.sin(yawRad) * dz);
        }
        
        if (dx != 0) {
            newX += (float)(Math.cos(yawRad + Math.PI/2) * dx);
            newZ += (float)(Math.sin(yawRad + Math.PI/2) * dx);
        }

        if (dy != 0) {
            newY += dy;
        }

        // Check collisions using display position (including crouch)
        boolean canMove = !collisionsEnabled || collisionHandler == null;
        if (!canMove) {
            float checkY = newY - crouchOffset; // Use crouched position for checks
            canMove = collisionHandler.isPositionValid(newX, checkY, newZ);
        }

        if (canMove) {
            x = newX;
            y = newY;
            z = newZ;
        }
        updateDisplayCoordinates();
    }

    // Get display coordinates (offset from center)
    public float getDisplayX() {
        return displayX;  // No offset needed, coordinates are already in world space
    }

    public float getDisplayY() {
        return displayY;
    }

    public float getDisplayZ() {
        return displayZ;  // No offset needed, coordinates are already in world space
    }

    // Get actual world coordinates
    public float getX() { return displayX; }
    public float getY() { return displayY; }
    public float getZ() { return displayZ; }

    // Getters for camera orientation.
    public float getYaw() { return yaw; }

    public float[] getDirection() {
        float[] direction = new float[3];
        float yawRad = (float)Math.toRadians(yaw);
        float pitchRad = (float)Math.toRadians(pitch);
        
        direction[0] = (float)(Math.cos(yawRad) * Math.cos(pitchRad));  // x
        direction[1] = (float)Math.sin(pitchRad);                       // y
        direction[2] = (float)(Math.sin(yawRad) * Math.cos(pitchRad));  // z
        
        return direction;
    }

    public float getPitch() { return pitch; }

    // Setters for camera position and rotation
    public void setX(double x) { this.x = (float)x; updateDisplayCoordinates(); }
    public void setY(double y) { this.y = (float)y; updateDisplayCoordinates(); }
    public void setZ(double z) { this.z = (float)z; updateDisplayCoordinates(); }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    
    public void setPosition(double x, double y, double z) {
        this.x = (float)x;
        this.y = (float)y;
        this.z = (float)z;
        updateDisplayCoordinates();
    }
    
    public void setRotation(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public void setCollisionHandler(CollisionHandler handler) {
        this.collisionHandler = handler;
    }

    public void setCollisionsEnabled(boolean enabled) {
        this.collisionsEnabled = enabled;
    }

    public boolean isCollisionsEnabled() {
        return collisionsEnabled;
    }
}
