package transportation_simulation.contexts;

import repast.simphony.context.DefaultContext;
import transportation_simulation.agent.IAgent;
import transportation_simulation.main.GlobalVars;

public class AgentContext extends DefaultContext<IAgent>{
	
	public AgentContext() {
		super(GlobalVars.CONTEXT_NAMES.AGENT_CONTEXT);
	}
	
}
