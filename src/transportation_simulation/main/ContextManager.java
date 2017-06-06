package transportation_simulation.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import transportation_simulation.agent.IAgent;
import transportation_simulation.agent.DefaultAgent;
import transportation_simulation.environment.Building;
import transportation_simulation.environment.Junction;
import transportation_simulation.environment.NetworkEdgeCreator;
import transportation_simulation.environment.Road;
import transportation_simulation.util.SpatialIndexManager;
import transportation_simulation.contexts.AgentContext;
import transportation_simulation.contexts.BuildingContext;
import transportation_simulation.contexts.JunctionContext;
import transportation_simulation.contexts.RoadContext;

public class ContextManager implements ContextBuilder<Object> {

	/*
	 * A logger for this class. Note that there is a static block that is used to configure all logging for the model
	 * (at the bottom of this file).
	 */
	private static Logger LOGGER = Logger.getLogger(ContextManager.class.getName());
	
	private static Properties properties;
	
	/*
	 * Pointers to contexts and projections (for convenience). Most of these can be made public, but the agent ones
	 * can't be because multi-threaded agents will simultaneously try to call 'move()' and interfere with each other. So
	 * methods like 'moveAgent()' are provided by ContextManager.
	 */

	private static Context<Object> mainContext;
	
	// building context and projection cab be public (thread safe) because buildings only queried
	public static Context<Building> buildingContext;
	public static Geography<Building> buildingProjection;

	public static Context<Road> roadContext;
	public static Geography<Road> roadProjection;

	public static Context<Junction> junctionContext;
	public static Geography<Junction> junctionGeography;
	public static Network<Junction> roadNetwork;

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
		
		SimulationLogging.init();
		
		// Keep a useful static link to the main context
		mainContext = context;
		
		// This is the name of the 'root'context
		// mainContext.setId(GlobalVars.CONTEXT_NAMES.MAIN_CONTEXT);

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
		
		// Create the buildings - context and geography projection
		buildingContext = new BuildingContext();
		buildingProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
				GlobalVars.CONTEXT_NAMES.BUILDING_GEOGRAPHY, buildingContext,
				new GeographyParameters<Building>(new SimpleAdder<Building>()));
		int buildingCount = 5;
		for (int i = 0; i < buildingCount; i++) {
			Building b = new Building(space, grid);
			b.setIdentifier(Integer.toString(i));
			buildingContext.add(b);
		}
		mainContext.addSubContext(buildingContext);
		SpatialIndexManager.createIndex(buildingProjection, Building.class);
		LOGGER.log(Level.FINER, "Read " + buildingContext.getObjects(Building.class).size() + " buildings");
		
		// Create the Roads - context and geography
		roadContext = new RoadContext();
		roadProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.ROAD_GEOGRAPHY, roadContext,
					new GeographyParameters<Road>(new SimpleAdder<Road>()));
		mainContext.addSubContext(roadContext);
		SpatialIndexManager.createIndex(roadProjection, Road.class);
		LOGGER.log(Level.FINER, "Read " + roadContext.getObjects(Road.class).size() + " roads");
		
		// Create road network
		
		// 1.junctionContext and junctionGeography
		junctionContext = new JunctionContext();
		mainContext.addSubContext(junctionContext);
		junctionGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
						GlobalVars.CONTEXT_NAMES.JUNCTION_GEOGRAPHY, junctionContext,
						new GeographyParameters<Junction>(new SimpleAdder<Junction>()));
		
		// 2. roadNetwork
		NetworkBuilder<Junction> builder = new NetworkBuilder<Junction>(GlobalVars.CONTEXT_NAMES.ROAD_NETWORK,
				junctionContext, false);
		builder.setEdgeCreator(new NetworkEdgeCreator<Junction>());
		roadNetwork = builder.buildNetwork();
		
		// Add the junctions to a spatial index (couldn't do this until the road network had been created).
		SpatialIndexManager.createIndex(junctionGeography, Junction.class);

		testEnvironment();
		
		agentContext = new AgentContext();
		
		int agentCount = 50;
		for (int i = 0; i < agentCount; i++) {
			Building b = buildingContext.getRandomObject();
			DefaultAgent agent = new DefaultAgent(b.getSpace(), b.getGrid());
			agentContext.add(agent);
			agent.setHome(b);
			b.addAgent(agent);
			agentContext.add(agent);
		}
		
		mainContext.addSubContext(agentContext);
		
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
		
		
		
		return mainContext;
	}

	/**
	 * Read the properties file and add properties. Will check if any properties have been included on the command line
	 * as well as in the properties file, in these cases the entries in the properties file are ignored in preference
	 * for those specified on the command line.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readProperties() throws FileNotFoundException, IOException {

		File propFile = new File("./repastcity.properties");
		if (!propFile.exists()) {
			throw new FileNotFoundException("Could not find properties file in the default location: "
					+ propFile.getAbsolutePath());
		}

		LOGGER.log(Level.FINE, "Initialising properties from file " + propFile.toString());

		ContextManager.properties = new Properties();

		FileInputStream in = new FileInputStream(propFile.getAbsolutePath());
		ContextManager.properties.load(in);
		in.close();

		// See if any properties are being overridden by command-line arguments
		for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
			String k = (String) e.nextElement();
			String newVal = System.getProperty(k);
			if (newVal != null) {
				// The system property has the same name as the one from the
				// properties file, replace the one in the properties file.
				LOGGER.log(Level.INFO, "Found a system property '" + k + "->" + newVal
						+ "' which matches a NeissModel property '" + k + "->" + properties.getProperty(k)
						+ "', replacing the non-system one.");
				properties.setProperty(k, newVal);
			}
		} // for
		return;
	} // readProperties
	
	public static int sizeOfIterable(Iterable<?> i) {
		int size = 0;
		Iterator<?> it = i.iterator();
		while (it.hasNext()) {
			size++;
			it.next();
		}
		return size;
	}
	
	/**
	 * Get the value of a property in the properties file. If the input is empty or null or if there is no property with
	 * a matching name, throw a RuntimeException.
	 * 
	 * @param property
	 *            The property to look for.
	 * @return A value for the property with the given name.
	 */
	public static String getProperty(String property) {
		if (property == null || property.equals("")) {
			throw new RuntimeException("getProperty() error, input parameter (" + property + ") is "
					+ (property == null ? "null" : "empty"));
		} else {
			String val = ContextManager.properties.getProperty(property);
			if (val == null || val.equals("")) { // No value exists in the
													// properties file
				throw new RuntimeException("checkProperty() error, the required property (" + property + ") is "
						+ (property == null ? "null" : "empty"));
			}
			return val;
		}
	}
	
	private void testEnvironment() {
		// TODO Auto-generated method stub
		
	}
	

}
