package transportation_simulation.contexts;

import repast.simphony.context.DefaultContext;
import transportation_simulation.environment.Junction;
import transportation_simulation.main.GlobalVars;

public class JunctionContext extends DefaultContext<Junction> {
	
	public JunctionContext() {
		super(GlobalVars.CONTEXT_NAMES.JUNCTION_CONTEXT);
	}

}
