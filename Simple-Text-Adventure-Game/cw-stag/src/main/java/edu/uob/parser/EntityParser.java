package edu.uob.parser;

import edu.uob.entity.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;
import edu.uob.entity.Character;

public class EntityParser {
    private HashMap<String, Location> locationMap;
    private Location startLocation;
    private Location storeroom;
    private HashMap<String, GameEntity> entityMap; // map to all entities including locations

    public EntityParser(File entitiesFile) throws FileNotFoundException, ParseException {
        this.locationMap = new HashMap<>();
        this.entityMap = new HashMap<>();
        this.startLocation = null;
        this.storeroom = null;

        Parser parser = new Parser();
        FileReader reader = new FileReader(entitiesFile);
        parser.parse(reader);
        Graph wholeDocument = parser.getGraphs().get(0);
        List<Graph> sections = wholeDocument.getSubgraphs();

        // The locations will always be in the first subgraph
        List<Graph> locationsGraphs = sections.get(0).getSubgraphs();
        this.parseLocationsGraphs(locationsGraphs);

        // The paths will always be in the second subgraph
        // The paths between locations are presented in the form of directed "arrows" (what Computer Scientists call 'edges').
        List<Edge> paths = sections.get(1).getEdges();
        this.parsePaths(paths);
    }

    private void parseLocationsGraphs(List<Graph> locationsGraphs) {
        for (Graph locationGraph : locationsGraphs) {
            Node locationDetails = locationGraph.getNodes(false).get(0);
            String locationName = Normaliser.normalizeString(locationDetails.getId().getId());
            String locationDescription = locationDetails.getAttribute("description");

            Location location = new Location(locationName, locationDescription);
            location.setLivingRoom(locationName);
            this.locationMap.put(locationName, location);
            this.entityMap.put(locationName, location); // entityMap contains all mapping including locations

            // The start location will always be the first location that appears in the entities file.
            if (this.startLocation == null) {
                this.startLocation = location;
            }
            if (this.storeroom == null && locationName.equals("storeroom")) {
                this.storeroom = location;
            }

            // Extract sub-graphs in this location graph
            List<Graph> nonLocationGraphs = locationGraph.getSubgraphs();
            this.parseEntitiesInLocation(nonLocationGraphs, location);
        }
    }

    private void parseEntitiesInLocation(List<Graph> nonLocationGraphs, Location location) {
        for (Graph nonLocationGraph : nonLocationGraphs) {
            String entityGraphName = nonLocationGraph.getId().getId().toLowerCase();

            // Process entities (nodes) in this sub-graph
            List<Node> entitiesNodes = nonLocationGraph.getNodes(false);
            for (Node entityNode : entitiesNodes) {
                String entityName = Normaliser.normalizeString(entityNode.getId().getId());
                String entityDescription = entityNode.getAttribute("description");

                GameEntity entity = null;

                switch (entityGraphName) {
                    case "characters":
                        entity = new Character(entityName, entityDescription);
                        entity.setLivingRoom(location.getName());
                        location.addCharacter(entityName, entity);
                        this.entityMap.put(entityName, entity);
                        break;
                    case "artefacts":
                        entity = new Artefact(entityName, entityDescription);
                        entity.setLivingRoom(location.getName());
                        location.addArtefact(entityName, entity);
                        this.entityMap.put(entityName, entity);
                        break;
                    case "furniture":
                        entity = new Furniture(entityName, entityDescription);
                        entity.setLivingRoom(location.getName());
                        location.addFurniture(entityName,entity);
                        this.entityMap.put(entityName, entity);
                        break;
                    default:
                        StringBuilder errorString = new StringBuilder();
                        errorString.append("Unknown entity: ").append(entityName);
                        throw new RuntimeException(errorString.toString());
                }
            }
        }
    }

    private void parsePaths(List<Edge> paths) {
        for (Edge path : paths) {
            String fromLocation = Normaliser.normalizeString(path.getSource().getNode().getId().getId());
            String toLocation = Normaliser.normalizeString(path.getTarget().getNode().getId().getId());

            // Add the path leading from source location to target location into fromLocation
            if (this.locationMap.containsKey(fromLocation)) {
                this.locationMap.get(fromLocation).addPath(toLocation);
            }
        }
    }

    public HashMap<String, Location> getLocationMap() {
        return this.locationMap;
    }

    public Location getStartLocation() {
        return this.startLocation;
    }

    public Location getStoreroom() {
        return this.storeroom;
    }

    /** entityMap maps to all entities including locations */
    public HashMap<String, GameEntity> getEntityMap() {
        return this.entityMap;
    }

    public boolean generateStoreroom() {
        if (this.storeroom != null) {
            return false;
        }
        this.storeroom = new Location("storeroom", "A auto-generated storeroom for entities not placed in the game.");
        this.locationMap.put("storeroom", this.storeroom);
        this.entityMap.put("storeroom", this.storeroom);
        return true;
    }
}
