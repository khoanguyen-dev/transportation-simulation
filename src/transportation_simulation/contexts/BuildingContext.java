package transportation_simulation.contexts;

import repast.simphony.context.DefaultContext;
import transportation_simulation.environment.Building;
import transportation_simulation.main.GlobalVars;

public class BuildingContext extends DefaultContext<Building> {
	
	public BuildingContext() {
		super(GlobalVars.CONTEXT_NAMES.BUILDING_CONTEXT);
	}

}