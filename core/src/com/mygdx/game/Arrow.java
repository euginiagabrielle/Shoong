package com.mygdx.game;

import com.badlogic.gdx.Gdx;

public class Arrow extends Character {
    int damage;
    public Arrow(String texturePath, float posX, float posY) {
        super(texturePath);
        this.getSprite().setSize((this.getSprite().getWidth()) / 12f, this.getSprite().getHeight()/12f); // Perkecil ukuran laser
        this.setPosition(posX/2, posY);
        this.damage = 50;

    }

    @Override
    public void handleMovement() {
        posY += 1000 * Gdx.graphics.getDeltaTime(); //mengatur kecepatan laser
    }

    public int getDamage() {
        return damage;
    }
}
