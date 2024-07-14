package ore.object;

import ch.aplu.jgamegrid.Location;
import ore.OreSim;
import ore.OreSimMachine;
import ore.OreSimObject;
import java.awt.*;
import static java.time.zone.ZoneRulesProvider.refresh;

/**
 * Represents an excavator in OreSim.
 * This class extends the OreSimMachine class.
 */
public class Excavator extends OreSimMachine
{
    public Excavator()
    {
        super(true, "sprites/excavator.png", 1);
    }

    /**
   * Automatically moves the specified machine based on the pre-defined sequence of movement
   * @param isFinished boolean
   */
    @Override
    public void autoMoveNext(boolean isFinished) {
        if (controls != null && this.autoMovementIndex < controls.size()) {
            String[] currentMove = controls.get(this.autoMovementIndex).split("-");
            String machineType = currentMove[0];
            String move = currentMove[1];
            this.autoMovementIndex++;

            if (isFinished)
                return;

            // Implementation for each machine
           if (machineType.equals("E")) {
                Location next = moveMachine(this, move);

                Rock curTarget = (Rock) gameGrid.getOneActorAt(this.getLocation(), Rock.class);
                if (curTarget != null){
                    curTarget.show();
                }
                if (next != null && canMove(next))
                {
                    this.setLocation(next);
                }
            }
            refresh();
        }
    }

    /**
     * Check if we can move a machine into the specified location
     * @param location (location on board)
     * @return bool (True is machine can move to the location, false otherwise)
     */
    @Override
    public boolean canMove(Location location) {
        Color c = gameGrid.getBg().getColor(location);


        // Check if next location is inside playing area
        if (c.equals(OreSim.BORDER_COLOR)) {
            return false;
        }

        // Check if there is an object at the specified location
        OreSimObject object = getOneOreSimObjectAt(location, OreSimObject.class);

        // If there is no object in next location, machine can move
        if (object == null) {
            machineMoves += 1;
            return true;
        }

        // Implementation for each machine
        if (object instanceof Rock rock) {
            machineMoves += 1;
            objectRemoved += 1;
            // Remove the rock
            return clearObject(rock);
        }

        return false;
    }
}