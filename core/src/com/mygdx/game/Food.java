package com.mygdx.game;

public abstract class Food extends Character {
    protected int health;

    public Food(String texturePath, int health, float posX, float posY) {
        super(texturePath);
        this.health = health;
        setPosition(posX, posY);
        this.getSprite().setSize(this.getSprite().getWidth() / 11, this.getSprite().getHeight() / 11); //mengatur ukuran monster
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public void handleMovement() {
    }
}