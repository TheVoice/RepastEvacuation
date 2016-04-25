package jevacuation;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Follower {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public Follower(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start=1, interval=1)
	public void run() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, pt, Object.class, 1, 1);
		List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(false);
//		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		Exit exit = null;
		Guide guide = null;
		for(Object obj : space.getObjects()){
			if(obj instanceof Exit){
				if(exit == null)
					exit = (Exit) obj;
				else
					if(space.getDistance(space.getLocation(this), space.getLocation(obj))<space.getDistance(space.getLocation(this), space.getLocation(exit)))
						exit = (Exit) obj;
			}else if(obj instanceof Guide){
				if(guide == null)
					guide = (Guide) obj;
				else
					if(space.getDistance(space.getLocation(this), space.getLocation(obj))<space.getDistance(space.getLocation(this), space.getLocation(guide)))
						guide = (Guide) obj;
			}
		}
		
		GridPoint smallestDistancePoint = null ;
		double distance = Integer.MAX_VALUE;
		
		if(space.getDistance(space.getLocation(this), space.getLocation(exit))<space.getDistance(space.getLocation(this), space.getLocation(guide))){
			for(GridCell <Object> cell : gridCells) {
				if (cell.size()==0 && grid.getDistance(cell.getPoint(), grid.getLocation(exit)) < distance) {
					smallestDistancePoint = cell.getPoint();
					distance = grid.getDistance(cell.getPoint(), grid.getLocation(exit));
				}
			}
			if(smallestDistancePoint != null)
				moveTowards(smallestDistancePoint);
		}else{
			for(GridCell <Object> cell : gridCells) {
				if (cell.size()==0 && grid.getDistance(cell.getPoint(), grid.getLocation(guide)) < distance) {
					smallestDistancePoint = cell.getPoint();
					distance = grid.getDistance(cell.getPoint(), grid.getLocation(guide));
				}
			}
			if(smallestDistancePoint != null)
				moveTowards(smallestDistancePoint);
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
