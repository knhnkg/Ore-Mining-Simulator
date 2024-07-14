package ore.object;

import ch.aplu.jgamegrid.Location;
import ore.OreSimObject;
import java.awt.*;
import java.util.List;


/**
 * Represents an ore in OreSim.
 * This class extends the OreSimObject class.
 */
public class Ore extends OreSimObject
{
    public Ore()
    {
        super("sprites/ore.png",2);
    }

    /**
     * When the pusher pushes the ore in 1 direction, this method will be called to check if the ore can move in that direction
     *  and if it can move, then it changes the location
     * @param ore object
     * @return bool (True if ore can move in the direction, false otherwise)
     */
    public boolean moveOre(Ore ore, Color BORDER_COLOR)
    {
        Location next = ore.getNextMoveLocation();
        Color c = gameGrid.getBg().getColor(next);
        java.util.List<OreSimObject> objectsAtNext = getOreSimObjectsAt(next);
        Target target = (Target)getOneOreSimObjectAt(next, Target.class);

        // Check if next location is inside playing area and does not contain any other object other than target
        if ((!objectsAtNext.isEmpty() && target == null) || c.equals(BORDER_COLOR)) {
            return false;
        }

        // Test if there is another ore
        Ore neighbourOre =
                (Ore)getOneOreSimObjectAt(next, Ore.class);
        if (neighbourOre != null)
            return false;

        Location currentLocation = ore.getLocation();
        List<OreSimObject> oreSimObjects = getOreSimObjectsAt(currentLocation);
        for (OreSimObject oreSimObject : oreSimObjects) {
            if (oreSimObject instanceof Target currentTarget) {
                currentTarget.show();
                ore.show(0);
            }
        }

        // Move the ore
        ore.setLocation(next);

        // Check if we are at a target
        Target nextTarget = (Target) getOneOreSimObjectAt(next, Target.class);
        if (nextTarget != null) {
            ore.show(1);
            nextTarget.hide();
        } else {
            ore.show(0);
        }

        return true;
    }
}