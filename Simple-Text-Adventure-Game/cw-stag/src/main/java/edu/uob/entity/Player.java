package edu.uob.entity;

import java.util.HashMap;

public class Player extends GameEntity{
    private int maxHealth;
    private int health;
    // is it really a good idea to use HashMap rather than HashSet?
    private HashMap<String, GameEntity> inventory;
    private Location currentLocation;

    public Player(String playerName, int maxHealth, Location startLocation) {
        super(playerName, String.format("Yourself, %s, a human playing the game. ", playerName), EntityType.PLAYER);
        this.maxHealth = maxHealth;
        this.health = this.maxHealth;
        this.inventory = new HashMap<>();
        this.currentLocation = startLocation;
    }

    public int getHealth() {
        return this.health;
    }

    public boolean damageHealth(int amount) {
        if (this.health <= 0 || amount <= 0 || amount > this.maxHealth) {
            return false;
        }
        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }
        return true;
    }

    public boolean restoreHealth(int amount) {
        if (this.health <= 0 || amount <= 0 || amount > this.maxHealth) {
            return false;
        }
        this.health += amount;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
        return true;
    }

    public HashMap<String, GameEntity> getInventory() {
        return this.inventory;
    }

    // for game entity that already exists in the game system, just move between locations and player's inventory
    public void pushInventory(GameEntity gameEntity) {
        gameEntity.setLivingRoom(null);
        this.inventory.put(gameEntity.getName(), gameEntity);
    }

    public GameEntity popInventory(String name) {
        GameEntity entityToPop = this.inventory.get(name);
        this.inventory.remove(name);
        return entityToPop;
    }

    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    /** When a player's health runs out (i.e. when it becomes zero)
     * <br>they should lose all the items in their inventory (which are dropped in the location where they ran out of health).
     * <br>The player should then be transported to the start location of the game and their health level restored to full (i.e. 3)
     */
    public void resetStatus(Location resetLocation) {
        for (GameEntity belonging : this.inventory.values()) {
            this.currentLocation.pushArtefact(belonging);
        }
        this.health = this.maxHealth;
        this.inventory.clear();
        this.currentLocation.removePlayer(super.getName());
        this.currentLocation = resetLocation;
        this.currentLocation.addPlayer(super.getName());
    }
}
