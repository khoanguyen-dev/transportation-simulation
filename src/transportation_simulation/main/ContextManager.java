package transportation_simulation.main;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import transportation_simulation.agent.IAgent;
import transportation_simulation.agent.DefaultAgent;
import transportation_simulation.environment.Building;
import transportation_simulation.contexts.AgentContext;
import transportation_simulation.contexts.BuildingContext;

public class ContextManager implements ContextBuilder<Object> {

	/*
	 * Pointers to contexts and projections (for convenience). Most of these can be made public, but the agent ones
	 * can't be because multi-threaded agents will simultaneously try to call 'move()' and interfere with each other. So
	 * methods like 'moveAgent()' are provided by ContextManager.
	 */

	private static Context<Object> mainContext;
	
	public static Context<Building> buildingContext;
	public static Geography<Building> buildingProjection;
	
	private static Context<IAgent> agentContext;
	private static Geography<IAgent> agentGeography;
	
	/**
	 * Get the geometry of the given agent. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 */
	public static synchronized Geometry getAgentGeometry(IAgent agent) {
		return ContextManager.agentGeography.getGeometry(agent);
	}

	@Override
	public Context<Object> build(Context<Object> context) {
		
		// Keep a useful static link to the main context
		mainContext = context;

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 50,
				50);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 50, 50));
		
		buildingContext = new BuildingContext();
		
		int buildingCount = 5;
		for (int i = 0; i < buildingCount; i++) {
			Building b = new Building(space, grid);
			b.setIdentifier(Integer.toString(i));
			buildingContext.add(new Building(space, grid));
		}
		
		mainContext.addSubContext(buildingContext);
		
		agentContext = new AgentContext();
		
		int agentCount = 50;
		for (int i = 0; i < agentCount; i++) {
			DefaultAgent agent = new DefaultAgent(space, grid);
			agentContext.add(agent);
			agent.setHome(buildingContext.getRandomObject());
			context.add(agent);
		}
		
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
		
		return mainContext;
	}
	
	

}
