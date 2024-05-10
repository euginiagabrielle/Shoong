

package com.mygdx.game;

import com.badlogic.gdx.Gdx;

public class rottenApple extends Character {
    private float rottenAppleTime;
    public rottenApple(String texturePath, float posX, float posY, float rottenAppleTime) {
        super(texturePath);
        setPosition(posX, posY);
        this.rottenAppleTime = rottenAppleTime;
        this.getSprite().setSize((this.getSprite().getWidth()) /15, this.getSprite().getHeight()/15); //mengatur ukuran apel
    }

    @Override
    public void handleMovement() {
        posY -= 400 * Gdx.graphics.getDeltaTime();  // Mengatur kecepatan apel
    }

    public float getRottenAppleTime() {
        return rottenAppleTime;
    }

    public void setRottenAppleTime(float time){
        rottenAppleTime = time;
    }
}
