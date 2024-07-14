package ore;

import ch.aplu.jgamegrid.Location;
import java.util.List;

/**
 * Abstract class for any OreSim Machines
 */
public abstract class OreSimMachine extends OreSimObject
{
    public int autoMovementIndex = 0;
    public int machineMoves = 0; // Counts total move blocks for this machine instance
    public int objectRemoved = 0; // Counts total removable objects this machine have removed (if applicable)
    protected List<String> controls = null;

    public OreSimMachine(boolean isRotatable, String filename, int nbSprites) {
        super(isRotatable, filename, nbSprites);
    }

    /**
     * Method to move pusher automatically based on the instructions input from properties file
     * @param controls
     */
    public void setupMachine(List<String> controls) {
        this.controls = controls;
    }

  /**
   * Automatically moves the specified machine based on the pre-defined sequence of movement
   * @param isFinished
   */
    public abstract void autoMoveNext(boolean isFinished);

    /**
     * Check if we can move the pusher into the location
     * @param location of the actor
     * @return bool
     */
    public abstract boolean canMove(Location location);

    /**
   * Tries to clear the specified object from game
   * @param object specific object
   * @return bool (True if object can be cleared, false otherwise)
   */
    public boolean clearObject(OreSimObject object) {
        // Retrieve a list of OreSimObjects at the current location
        Location currentLocation = object.getLocation();
        List<OreSimObject> oreSimObjects = this.getOreSimObjectsAt(currentLocation);

        // When objects are present at the current location
        for (OreSimObject oreSimObject : oreSimObjects) {
            Class<? extends OreSimObject> objectClass = object.getClass();
            if (objectClass.isInstance(oreSimObject)) {
                // Remove object
                return gameGrid.removeActor(oreSimObject);
            }
        }

        // When object is not present at current location
        return false;
    }

    /**
     * Moves the specified OreSimMachine in direction of move
     * @param machine
     * @param move
     * @return The next location of the specified machine
     */
    public Location moveMachine(OreSimMachine machine, String move) {
        Location next = null;

        switch (move)
        {
            case "L": // move left
                next = machine.getLocation().getNeighbourLocation(Location.WEST);
                machine.setDirection(Location.WEST);
                break;
            case "U": // move up
                next = machine.getLocation().getNeighbourLocation(Location.NORTH);
                machine.setDirection(Location.NORTH);
                break;
            case "R": // move right
                next = machine.getLocation().getNeighbourLocation(Location.EAST);
                machine.setDirection(Location.EAST);
                break;
            case "D": // move down
                next = machine.getLocation().getNeighbourLocation(Location.SOUTH);
                machine.setDirection(Location.SOUTH);
                break;
        }
        return next;
    }
}
