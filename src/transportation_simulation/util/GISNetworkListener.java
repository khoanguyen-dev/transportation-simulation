package transportation_simulation.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.projection.ProjectionEvent;
import repast.simphony.space.projection.ProjectionListener;

/**
 * A ProjectionListener implementation for managing Repast network edges in a
 * Repast geography projection.  This listener responds to both geography event
 * and network events.
 *
 */
public class GISNetworkListener implements ProjectionListener {

	Context context;
	Network network;
	Geography geography;
	GeometryFactory fac = new GeometryFactory();
	
	public GISNetworkListener(Context c, Geography g, Network n) {
		context = c;
		network = n;
		geography = g;
		
		network.addProjectionListener(this);
		geography.addProjectionListener(this);
	}
	
	@Override
	public void projectionEventOccurred(ProjectionEvent evt) {
	
		// When an object is moved in the geography, its network edges positions 
		// should be updated if the object has edges.
		if (evt.getType() == ProjectionEvent.OBJECT_MOVED){
			Iterable<RepastEdge> edges = network.getEdges(evt.getSubject());

			if (edges != null){
				for (RepastEdge e : edges){
					// Get the existing geometry for this edge
					MultiLineString lineFeature = (MultiLineString)geography.getGeometry(e);
					
					Coordinate sourceCoord = geography.getGeometry(e.getSource()).getCoordinate();
					Coordinate targetCoord = geography.getGeometry(e.getTarget()).getCoordinate();

					Coordinate coords[] = lineFeature.getCoordinates();
					
					// Update the edge coordinates based on the source and target object 
					// (agent) coordinates.
					coords[0].setCoordinate(sourceCoord);
					coords[1].setCoordinate(targetCoord);
				}
			}
		}
			
		// When a Repast network edge is added, create a new MultiLineString geometry
		// to represent the edge in the geography.
		else if (evt.getType() == ProjectionEvent.EDGE_ADDED){	
			RepastEdge e = (RepastEdge)evt.getSubject();
			
			Coordinate sourceCoord = geography.getGeometry(e.getSource()).getCoordinate();
			Coordinate targetCoord = geography.getGeometry(e.getTarget()).getCoordinate();
			
			LineString lineString = fac.createLineString(new Coordinate[]{sourceCoord, 
					targetCoord});
			
			MultiLineString mls = fac.createMultiLineString(new LineString[]{lineString});
			
			context.add(e);
			geography.move(e, mls);
		}
		
		// When a Repast edge remove event occurs, remove the edge geometry from the 
		// geography and the context.  This should also occur automatically when agents
		// are removed from a context or network.
		else if (evt.getType() == ProjectionEvent.EDGE_REMOVED){
			RepastEdge e = (RepastEdge)evt.getSubject();
			
			geography.move(e, null);
			context.remove(e);			
		}
	}
}
