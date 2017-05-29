package transportation_simulation.environment;

import transportation_simulation.exceptions.DuplicateIdentifierException;
import transportation_simulation.exceptions.NoIdentifierException;

/**
 * Interface for classes that can be identified. Useful for environment objects that must read an identifier
 * value from input data.
 */
public interface Identified {
	
	String getIdentifier() throws NoIdentifierException;

	void setIdentifier(String id) throws DuplicateIdentifierException;

}
