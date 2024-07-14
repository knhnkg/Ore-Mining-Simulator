package ore;

import ch.aplu.jgamegrid.*;
import ore.object.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Properties;

public class OreSim extends GameGrid implements GGKeyListener
{
  // ------------- Inner classes -------------
  public enum ElementType{
    OUTSIDE("OS"), EMPTY("ET"), BORDER("BD"),
    PUSHER("P"), BULLDOZER("B"), EXCAVATOR("E"), ORE("O"),
    ROCK("R"), CLAY("C"), TARGET("T");
    private String shortType;

    ElementType(String shortType) {
      this.shortType = shortType;
    }

    public String getShortType() {
      return shortType;
    }

    public static ElementType getElementByShortType(String shortType) {
      ElementType[] types = ElementType.values();
      for (ElementType type: types) {
        if (type.getShortType().equals(shortType)) {
          return type;
        }
      }

      return ElementType.EMPTY;
    }
  }
  // ------------- End of inner classes ------


  private MapGrid grid;
  public static final Color BORDER_COLOR = new Color(100, 100, 100);

  // Initialisation of OreSimObjects
  private Ore[] ores;
  private Target[] targets;
  private Pusher pusher;
  private Bulldozer bulldozer;
  private Excavator excavator;

  private boolean isFinished = false;
  private boolean isAutoMode;
  private Properties properties;
  private double gameDuration;
  private List<String> controls;
  private int movementIndex;
  private StringBuilder logResult = new StringBuilder();

  public OreSim(Properties properties, MapGrid grid)
  {
    super(grid.getNbHorzCells(), grid.getNbVertCells(), 30, false);
    this.grid = grid;
    nbHorzCells = grid.getNbHorzCells();
    nbVertCells = grid.getNbVertCells();
    this.properties = properties;

    ores = new Ore[grid.getNbOres()];
    targets = new Target[grid.getNbOres()];

    isAutoMode = properties.getProperty("movement.mode").equals("auto");
    gameDuration = Integer.parseInt(properties.getProperty("duration"));
    setSimulationPeriod(Integer.parseInt(properties.getProperty("simulationPeriod")));
    controls = Arrays.asList(properties.getProperty("machines.movements").split(","));
  }


  /**
   * Check the number of ores that are collected
   * @return The number of ores that are collected
   */
  private int checkOresDone() {
    int nbTarget = 0;
    for (int i = 0; i < grid.getNbOres(); i++)
    {
      if (ores[i].getIdVisible() == 1)
        nbTarget++;
    }

    return nbTarget;
  }


  /**
   * The main method to run the game
   * @param isDisplayingUI
   * @return String logs of the simulator
   */
  public String runApp(boolean isDisplayingUI) {
    GGBackground bg = getBg();
    drawBoard(bg);
    drawActors();
    addKeyListener(this);
    if (isDisplayingUI) {
      show();
    }

    if (isAutoMode) {
        doRun();
    }

    int oresDone = checkOresDone();
    double ONE_SECOND = 1000.0;
    while(oresDone < grid.getNbOres() && gameDuration >= 0) {
      try {
        Thread.sleep(simulationPeriod);
        double minusDuration = (simulationPeriod / ONE_SECOND);
        gameDuration -= minusDuration;
        String title = String.format("# Ores at Target: %d. Time left: %.2f seconds", oresDone, gameDuration);
        setTitle(title);
        if (isAutoMode) {
          if (pusher != null) {
            pusher.autoMoveNext(isFinished);
          }
          if (excavator != null) {
            excavator.autoMoveNext(isFinished);
          }
          if (bulldozer != null) {
            bulldozer.autoMoveNext(isFinished);
          }

          updateLogResult();
        }

        oresDone = checkOresDone();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    doPause();

    if (oresDone == grid.getNbOres()) {
      setTitle("Mission Complete. Well done!");
    } else if (gameDuration < 0) {
      setTitle("Mission Failed. You ran out of time");
    }

    updateStatistics();
    isFinished = true;
    return logResult.toString();
  }


  /**
   * Transform the list of actors to a string of location for a specific kind of actor.
   * @param actors
   * @return String of actor's location
   */
  private String actorLocations(List<Actor> actors) {
    StringBuilder stringBuilder = new StringBuilder();
    boolean hasAddedColon = false;
    boolean hasAddedLastComma = false;
    for (int i = 0; i < actors.size(); i++) {
      Actor actor = actors.get(i);
      if (actor.isVisible()) {
        if (!hasAddedColon) {
          stringBuilder.append(":");
          hasAddedColon = true;
        }
        stringBuilder.append(actor.getX() + "-" + actor.getY());
        stringBuilder.append(",");
        hasAddedLastComma = true;
      }
    }

    if (hasAddedLastComma) {
      stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
    }

    return stringBuilder.toString();
  }


  /**
   * Prints out movement and changes data to specified text file.
   * Calls at the end of runApp
   */
  private void updateStatistics() {
    File statisticsFile = new File("statistics.txt");
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(statisticsFile);

      fileWriter.write("Pusher-1 Moves: " + pusher.machineMoves +"\n");
      if (excavator != null) {
        fileWriter.write("Excavator-1 Moves: " + excavator.machineMoves + "\n");
        fileWriter.write("Excavator-1 Rock removed: " + excavator.objectRemoved + "\n");
      }
      if (bulldozer != null) {
        fileWriter.write("Bulldozer-1 Moves: " + bulldozer.machineMoves + "\n");
        fileWriter.write("Bulldozer-1 Clay removed: " + bulldozer.objectRemoved + "\n");
      }
    } catch (IOException e) {
      System.out.println("Cannot write to file - e: " + e.getLocalizedMessage());
    } finally {
      try {
        fileWriter.close();
      } catch (IOException e) {
        System.out.println("Cannot close file - e: " + e.getLocalizedMessage());
      }
    }
  }


  /**
   * Draw all different actors on the board: pusher, bulldozer, excavator, target, ore, rock, clay
   * Part of feature-making
   */
  private void drawActors() {
    int oreIndex = 0;
    int targetIndex = 0;

    for (int y = 0; y < nbVertCells; y++) {
      for (int x = 0; x < nbHorzCells; x++) {
        Location location = new Location(x, y);
        ElementType a = grid.getCell(location);
        if (a == ElementType.PUSHER) {
          pusher = new Pusher();
          addActor(pusher, location);
          pusher.setupMachine(controls);
        }
        if (a == ElementType.ORE) {
          ores[oreIndex] = new Ore();
          addActor(ores[oreIndex], location);
          oreIndex++;
        }
        if (a == ElementType.TARGET) {
          targets[targetIndex] = new Target();
          addActor(targets[targetIndex], location);
          targetIndex++;
        }
        if (a == ElementType.ROCK) {
          addActor(new Rock(), location);
        }
        if (a == ElementType.CLAY) {
          addActor(new Clay(), location);
        }
        if (a == ElementType.BULLDOZER) {
          bulldozer = new Bulldozer();
          addActor(bulldozer, location);
          bulldozer.setupMachine(controls);
        }
        if (a == ElementType.EXCAVATOR) {
          excavator = new Excavator();
          addActor(excavator, location);
          excavator.setupMachine(controls);
        }
      }
    }
    System.out.println("ores = " + Arrays.asList(ores));
    setPaintOrder(Target.class);
  }


  /**
   * Draw the basic board with outside color and border color
   * @param bg
   */
  private void drawBoard(GGBackground bg) {
    bg.clear(new Color(230, 230, 230));
    bg.setPaintColor(Color.darkGray);
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        Location location = new Location(x, y);
        ElementType a = grid.getCell(location);
        if (a != ElementType.OUTSIDE)
        {
          bg.fillCell(location, Color.lightGray);
        }
        if (a == ElementType.BORDER)  // Border
          bg.fillCell(location, BORDER_COLOR);
      }
    }
  }

  /**
   * The method is automatically called by the framework when a key is pressed. Based on the pressed key, the pusher
   *  will change the direction and move
   * @param evt
   * @return bool
   */
  @Override
  public boolean keyPressed(KeyEvent evt) {
    if (isFinished)
      return true;

    Location next = null;
    switch (evt.getKeyCode()) {
      case KeyEvent.VK_LEFT:
        next = pusher.getLocation().getNeighbourLocation(Location.WEST);
        pusher.setDirection(Location.WEST);
        break;
      case KeyEvent.VK_UP:
        next = pusher.getLocation().getNeighbourLocation(Location.NORTH);
        pusher.setDirection(Location.NORTH);
        break;
      case KeyEvent.VK_RIGHT:
        next = pusher.getLocation().getNeighbourLocation(Location.EAST);
        pusher.setDirection(Location.EAST);
        break;
      case KeyEvent.VK_DOWN:
        next = pusher.getLocation().getNeighbourLocation(Location.SOUTH);
        pusher.setDirection(Location.SOUTH);
        break;
    }

    Target curTarget = (Target) getOneActorAt(pusher.getLocation(), Target.class);
    if (curTarget != null) {
      curTarget.show();
    }

    if (next != null && pusher.canMove(next)) {
      pusher.setLocation(next);
      updateLogResult();
    }
    refresh();
    return true;
  }


  @Override
  public boolean keyReleased(KeyEvent evt) {
    return true;
  }

  /**
   * The method will generate a log result for all the movements of all actors
   * The log result will be tested against our expected output.
   * Your code will need to pass all the 3 test suites with 9 test cases.
   */
  private void updateLogResult() {
    movementIndex++;
    List<Actor> pushers = getActors(Pusher.class);
    List<Actor> ores = getActors(Ore.class);
    List<Actor> targets = getActors(Target.class);
    List<Actor> rocks = getActors(Rock.class);
    List<Actor> clays = getActors(Clay.class);
    List<Actor> bulldozers = getActors(Bulldozer.class);
    List<Actor> excavators = getActors(Excavator.class);

    logResult.append(movementIndex + "#");
    logResult.append(ElementType.PUSHER.getShortType()).append(actorLocations(pushers)).append("#");
    logResult.append(ElementType.ORE.getShortType()).append(actorLocations(ores)).append("#");
    logResult.append(ElementType.TARGET.getShortType()).append(actorLocations(targets)).append("#");
    logResult.append(ElementType.ROCK.getShortType()).append(actorLocations(rocks)).append("#");
    logResult.append(ElementType.CLAY.getShortType()).append(actorLocations(clays)).append("#");
    logResult.append(ElementType.BULLDOZER.getShortType()).append(actorLocations(bulldozers)).append("#");
    logResult.append(ElementType.EXCAVATOR.getShortType()).append(actorLocations(excavators)).append("#");

    logResult.append("\n");
  }
}
