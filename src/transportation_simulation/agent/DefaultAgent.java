package transportation_simulation.agent;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import transportation_simulation.environment.Building;
import transportation_simulation.environment.Route;
import transportation_simulation.main.ContextManager;

public class DefaultAgent implements IAgent{
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	private Building home; // Where the agent lives
	private Route route; // An object to move the agent around the world

	private boolean goingHome = false; // Whether the agent is going to or from their home

	private static int uniqueID = 0;
	private int id;

	public DefaultAgent() {
		this.id = uniqueID++;
	}
	
	public DefaultAgent(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.id = uniqueID++;
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() throws Exception {
		if (this.route == null) {
			this.goingHome = false; // Must be leaving home
			// Choose a new building to go to
			Building b = ContextManager.buildingContext.getRandomObject();
			this.route = new Route(this, b.getCoords(), b);
		}
		if (!this.route.atDestination()) {
			this.route.travel();
		} else {
			// Have reached destination, now either go home or onto another building
			this.route.getDestinationBuilding().addAgent(this);
			if (this.goingHome) {
				this.goingHome = false;
				Building b = ContextManager.buildingContext.getRandomObject();
				this.route = new Route(this, b.getCoords(), b);
			} else {
				this.goingHome = true;
				this.route = new Route(this, this.home.getCoords(), this.home);
			}

		}
	}

	public Grid<Object> getGrid() {
		return grid;
	}
	
	public ContinuousSpace<Object> getSpace() {
		return space;
	}
	
	public void setHome(Building home) {
		this.home = home;
	}
	
	public Building getHome() {
		return this.home;
	}
	
	public List<String> getTransportAvailable() {
		return null;
	}
	
	public String toString() {
		return "Agent " + this.id;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultAgent))
			return false;
		DefaultAgent b = (DefaultAgent) obj;
		return this.id == b.id;
	}
	
	public int hashCode() {
		return this.id;
	}

	/**
	 * There will be no inter-agent communication so these agents can be executed simultaneously in separate threads.
	 */
	@Override
	public boolean isThreadable() {
		return true;
	}

	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {		
	}
}