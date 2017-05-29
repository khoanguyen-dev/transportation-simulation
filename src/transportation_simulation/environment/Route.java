package transportation_simulation.environment;

import com.vividsolutions.jts.geom.Coordinate;

import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;
import transportation_simulation.agent.DefaultAgent;
import transportation_simulation.main.ContextManager;

public class Route {
	
	//private AgentInterface agent;
	private DefaultAgent agent;
	private Coordinate destination;
	private Building destinationBuilding;

	public Route(DefaultAgent agent, Coordinate destination, Building destinationBuilding) {
		this.destination = destination;
		this.agent = agent;
		this.destinationBuilding = destinationBuilding;
	}

	/**
	 * Determine whether or not the person associated with this Route is at their destination. Compares their current
	 * coordinates to the destination coordinates (must be an exact match).
	 * 
	 * @return True if the person is at their destination
	 */
	public boolean atDestination() {
		GridPoint pt = destinationBuilding.getGrid().getLocation(destinationBuilding);
		return pt.equals(agent.getGrid().getLocation(agent));
		//return ContextManager.getAgentGeometry(this.agent).getCoordinate().equals(this.destination);
	}
	
	public Building getDestinationBuilding() {
		return this.destinationBuilding;
	}

	public void travel() {
		destinationBuilding.removeAgent(agent);
		GridPoint pt = destinationBuilding.getGrid().getLocation(destinationBuilding);
		ContinuousSpace<Object> space = agent.getSpace();
		if (!pt.equals(agent.getGrid().getLocation(agent))) {
			NdPoint myPoint = space.getLocation(agent);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
					otherPoint);
			space.moveByVector(agent, 1, angle, 0);
			myPoint = space.getLocation(agent);
			agent.getGrid().moveTo(agent, (int) myPoint.getX(), (int) myPoint.getY());
		}
	}

}