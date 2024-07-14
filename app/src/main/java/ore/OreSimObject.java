package ore;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;
import java.util.ArrayList;

/**
 * Abstract class for any OreSim interactable objects
 */
public abstract class OreSimObject extends Actor
{
    public OreSimObject(boolean isRotatable, String filename, int nbSprites) {
        super( isRotatable, filename, nbSprites);
    }
    public OreSimObject(String filename, int nbSprites) {
        super(filename, nbSprites);
    }
    public OreSimObject(String filename) {
        super(filename);
    }

    /**
     * Wrapper function for getActorsAt for future development
     * @param location on the board
     * @return oreSimObjects on the specified location
     */
    protected ArrayList<OreSimObject> getOreSimObjectsAt(Location location) {
        ArrayList<Actor> actors = gameGrid.getActorsAt(location);
        ArrayList<OreSimObject> oreSimObjects = new ArrayList<>();
        for (Actor actor : actors) {
            if (actor instanceof OreSimObject) {
                oreSimObjects.add((OreSimObject) actor);
            }
        }
        return oreSimObjects;
    }

    /**
     * Wrapper function for getOneActorAt for future development
     * @param location on the board
     * @param clazz specified class
     * @return oreSimObject specified if exists in location
     */
    protected OreSimObject getOneOreSimObjectAt(Location location, Class clazz) {
        ArrayList<Actor> list = gameGrid.getActorsAt(location, clazz);
        return list.isEmpty() ? null : (OreSimObject)list.get(0);
    }
}
