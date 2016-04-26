package jevacuation;

import java.awt.Point;
import java.util.ArrayList;
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
				if (cell.size()==0 && grid.getDistance(cell.getPoint(), grid.getLocation(exit)) < distance
						&& !isWallOnWayTo(grid.getLocation(exit))) {
					smallestDistancePoint = cell.getPoint();
					distance = grid.getDistance(cell.getPoint(), grid.getLocation(exit));
				}
			}
			if(smallestDistancePoint != null)
				moveTowards(smallestDistancePoint);
		}else{
			for(GridCell <Object> cell : gridCells) {
				if (cell.size()==0 && grid.getDistance(cell.getPoint(), grid.getLocation(guide)) < distance
						&& !isWallOnWayTo(grid.getLocation(guide))) {
					smallestDistancePoint = cell.getPoint();
					distance = grid.getDistance(cell.getPoint(), grid.getLocation(guide));
				}
			}
			if(smallestDistancePoint != null)
				moveTowards(smallestDistancePoint);
		}
	}
	
	private boolean isWallOnWayTo(GridPoint point) {
		int xSource = grid.getLocation(this).getX();
		int ySource = grid.getLocation(this).getY();
		
		List<Point> line = findLine(xSource, ySource, point.getX(), point.getY());
		for(Point p : line) {
			for(Object obj : grid.getObjectsAt(p.x, p.y)) {
				if(obj instanceof Wall) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void moveTowards (GridPoint pt) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
		space.moveByVector(this, 1, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) (myPoint.getX()+0.5), (int) (myPoint.getY()+0.5));
		
	}
	//http://www.sanfoundry.com/java-program-bresenham-line-algorithm/
	private List<Point> findLine(int x0, int y0, int x1, int y1) {
		List<Point> line = new ArrayList<>();
		int dx = Math.abs(x1-x0);
		int dy = Math.abs(y1-y0);
		
		int sx = x0<x1 ? 1 : -1;
		int sy = y0 < y1 ? 1: -1;
		int err = dx-dy;
		int e2;
		
		while(true) {
			line.add(new Point(x0, y0));
			if(x0==x1 && y0==y1) {
				break;
			}
			
			e2=2*err;
			if(e2 > -dy) {
				err -= dy;
				x0+=sx;
			}
			
			if(e2<dx) {
				err += dx;
				y0+=sy;
			}
		}
		return line;
		
	}
}
