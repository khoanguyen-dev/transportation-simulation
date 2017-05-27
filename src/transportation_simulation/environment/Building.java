package transportation_simulation.environment;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import transportation_simulation.agent.IAgent;;

public class Building {
	
	/** TODO: Change to coordinate system */
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	/** A list of agents who live here */
	private List<IAgent> agents;
	
	/**
	 * A unique identifier for buildings, usually set from the 'identifier' column in a shapefile
	 */
	private String identifier;
	
	/**
	 * The coordinates of the Building. This is also stored by the projection that contains this Building but it is
	 * useful to have it here too. As they will never change (buildings don't move) we don't need to worry about keeping
	 * them in sync with the projection.
	 */
	private Coordinate coords;

	public Building() {
		this.agents = new ArrayList<IAgent>();
	}
	
	public Building(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	public Coordinate getCoords() {
		return this.coords;
	}
	
	public void setCoords(Coordinate c) {
		this.coords = c;
	}
	
	/** TODO: Change to coordinate system */
	public void setSpaceandGrid(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	/** TODO: Change to coordinate system */
	public ContinuousSpace<Object> getSpace() {
		return space;
	}
	
	/** TODO: Change to coordinate system */
	public Grid<Object> getGrid() {
		return grid;
	}
	
	public String getIdentifier() throws Exception {
		if (this.identifier == null) {
			throw new Exception("This building has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		} else {
			return identifier;
		}
	}
	
	public void setIdentifier(String id) {
		this.identifier = id;
	}
	
	public void addAgent(IAgent a) {
		this.agents.add(a);
	}
	
	public List<IAgent> getAgents() {
		return this.agents;
	}

	@Override
	public String toString() {
		return "building: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Building))
			return false;
		Building b = (Building) obj;
		return this.identifier.equals(b.identifier);
	}
	
	/**
	 * Returns the hash code of this <code>Building</code>'s identifier string. 
	 
	@Override
	public int hashCode() {
		if (this.identifier==null) {
			//TODO: Implement logger
		}

		return this.identifier.hashCode();
	}
	*/
}