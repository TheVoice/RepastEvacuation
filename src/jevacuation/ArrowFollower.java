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

public class ArrowFollower {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private double currentDirection;
	private boolean followingArrows;
	
	
	public ArrowFollower(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.followingArrows = false;
	}
	
	@ScheduledMethod(start=1, interval=1)
	public void run() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, pt, Object.class, 1, 1);
		List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(false);

		if(!followingArrows){//search for the closes arrow
			Arrow arrow = null;
			for(Object obj : space.getObjects()){
				if(obj instanceof Arrow){
					if(arrow == null)
						arrow = (Arrow) obj;
					else
						if(space.getDistance(space.getLocation(this), space.getLocation(obj))<space.getDistance(space.getLocation(this), space.getLocation(arrow)))
							arrow = (Arrow) obj;
				}
			}
			moveInDirection(arrow.getDirection());
		}else{
			moveInDirection(currentDirection);
		}
	}

	private void moveInDirection(double direction) {
		NdPoint myPoint = space.getLocation(this);
		space.moveByVector(this, 1, direction, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) (myPoint.getX()+0.5), (int) (myPoint.getY()+0.5));
	}

}
