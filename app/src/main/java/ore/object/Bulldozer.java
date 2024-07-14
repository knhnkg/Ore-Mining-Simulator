package ore.object;

import ch.aplu.jgamegrid.Location;
import ore.OreSim;
import ore.OreSimMachine;
import ore.OreSimObject;
import java.awt.*;
import static java.time.zone.ZoneRulesProvider.refresh;

/**
 * Represents a bulldozer in OreSim.
 * This class extends the OreSimMachine class.
 */
public class Bulldozer extends OreSimMachine {
    public Bulldozer() {
        super(true, "sprites/bulldozer.png", 1);
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
           if (machineType.equals("B")) {
                Location next = moveMachine(this, move);

                Clay curTarget = (Clay) gameGrid.getOneActorAt(this.getLocation(), Clay.class);
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
        if (object instanceof Clay clay) {
            machineMoves += 1;
            objectRemoved += 1;
            // Remove the clay
            return clearObject(clay);
        }

        return false;
    }
}