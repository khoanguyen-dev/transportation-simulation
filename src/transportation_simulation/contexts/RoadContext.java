package transportation_simulation.contexts;

import repast.simphony.context.DefaultContext;
import transportation_simulation.environment.Road;
import transportation_simulation.main.GlobalVars;

public class RoadContext extends DefaultContext<Road> {
	
	public RoadContext() {
		super(GlobalVars.CONTEXT_NAMES.ROAD_CONTEXT);
	}

}
