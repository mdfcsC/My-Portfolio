package edu.uob;

import edu.uob.action.GameAction;
import edu.uob.entity.GameEntity;
import edu.uob.entity.Location;
import edu.uob.entity.Player;
import edu.uob.parser.ActionParser;
import edu.uob.parser.EntityParser;

import java.util.*;

public class GameState {
    private EntityParser entityParser;
    private ActionParser actionParser;

    private HashMap<String, Player> playersHashMap;
    private int maxHealth;

    public GameState(EntityParser entityParser, ActionParser actionParser) {
        this.entityParser = entityParser;
        this.actionParser = actionParser;
        this.playersHashMap = new HashMap<>();
        this.maxHealth = 3;
    }

    public HashMap<String, Player> getPlayersHashMap() {
        return this.playersHashMap;
    }

    public void addNewPlayer(String playerName) {
        Location startLocation = this.entityParser.getStartLocation();
        startLocation.addPlayer(playerName);
        Player newPlayer = new Player(playerName, this.maxHealth, startLocation);
        this.playersHashMap.put(playerName, newPlayer);
    }

    public Location getStartLocation() {
        return this.entityParser.getStartLocation();
    }

    public Location getStoreroom() {
        return this.entityParser.getStoreroom();
    }

    public HashMap<String, Location> getAllLocations() {
        return this.entityParser.getLocationMap();
    }

    public HashMap<String, GameEntity> getAllEntities() {
        return this.entityParser.getEntityMap();
    }

    public HashSet<GameAction> getAllActions() {
        return this.actionParser.getGameActions();
    }
}
