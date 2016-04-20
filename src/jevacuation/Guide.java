package jevacuation;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Guide {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private List<Waypoint> waypoints;
	
	public Guide(ContinuousSpace<Object> space, Grid<Object> grid, List<Waypoint> waypoints) {
		this.space = space;
		this.grid = grid;
		this.waypoints = waypoints;
	}
	
	@ScheduledMethod(start=1, interval=1)
	public void run() {
		
		if(!waypoints.isEmpty()) {
			Waypoint waypoint = waypoints.get(0);
			GridPoint gridWaypoint = new GridPoint(waypoint.getX(), waypoint.getY());
			moveTowards(gridWaypoint);
			
			GridPoint pt = grid.getLocation(this);
			if(pt.getX()==gridWaypoint.getX() && pt.getY()==gridWaypoint.getY()) {
				waypoints.remove(0);
			}
		}
	}
	
	public void moveTowards (GridPoint pt) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
		space.moveByVector(this, 1, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) (myPoint.getX()+0.5), (int) (myPoint.getY()+0.5));
		
	}

}
