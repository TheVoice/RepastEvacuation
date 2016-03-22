package jevacuation;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import jevacuation.Exit;

public class Human {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	

	public Human(ContinuousSpace<Object> space,Grid<Object> grid){
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1,interval = 1)
	public void step(){
		//GridPoint pt = grid.getLocation(this);
		
		Exit closestExit = null;
		for(Object obj : space.getObjects()){
			if(obj instanceof Exit){
				if(closestExit == null)
					closestExit = (Exit) obj;
				else
					if(space.getDistance(space.getLocation(this), space.getLocation(obj))<space.getDistance(space.getLocation(this), space.getLocation(closestExit)))
						closestExit = (Exit) obj;
			}
		}
		moveTowards(closestExit);
	}
	
	public void moveTowards(Exit exit){
		GridPoint dest = grid.getLocation(exit);
		if(!dest.equals(grid.getLocation(this))){
			NdPoint myPoint = space.getLocation(this);
			NdPoint destPoint = new NdPoint(dest.getX(),dest.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, destPoint);
			space.moveByVector(this, 1, angle,0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(),(int)myPoint.getY());
		}
	}
}
