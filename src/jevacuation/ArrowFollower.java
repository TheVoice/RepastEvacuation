package jevacuation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.Direction;
import repast.simphony.space.SpatialException;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class ArrowFollower {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private double currentDirection;
	private boolean followingArrows;
	private Set seenComeHereSignsSet = new HashSet();
	
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
		
		
		
		
		ComeHereSign comeHereSign = null;
		GridPoint smallestDistancePoint = null ;
		double distance = Integer.MAX_VALUE;
		for(Object obj : space.getObjects()){
			if(obj instanceof ComeHereSign && 
					grid.getDistance(grid.getLocation(obj), grid.getLocation(this)) < 8 && 
					!seenComeHereSignsSet.contains(obj)) {
				
				for(GridCell <Object> cell : gridCells) {
					if (cell.size()==0 && grid.getDistance(cell.getPoint(), grid.getLocation(obj)) < distance
							&& !isWallOnWayTo(grid.getLocation(obj))) {
						smallestDistancePoint = cell.getPoint();
						distance = grid.getDistance(cell.getPoint(), grid.getLocation(obj));
						comeHereSign = (ComeHereSign) obj;
					}
				}
				
			}
			if(obj instanceof Exit) {
				//check if agent is close enough to exit
				GridCellNgh<Object> nghCreator2 = new GridCellNgh<Object>(grid, pt, Object.class, 1, 1);
				List<GridCell<Object>> gridCells2 = nghCreator.getNeighborhood(true);
				for(GridCell<Object> cell : gridCells2) {
					if(grid.getLocation(obj).equals(cell.getPoint())) {
						ContextUtils.getContext(this).remove(this);
						return;
					}
				}
			}
		}
		
		if(smallestDistancePoint!=null) {
			//check if agent is close enough to comeHereSign
			GridCellNgh<Object> nghCreator2 = new GridCellNgh<Object>(grid, pt, Object.class, 1, 1);
			List<GridCell<Object>> gridCells2 = nghCreator.getNeighborhood(true);
			for(GridCell<Object> cell : gridCells2) {
				if(grid.getLocation(comeHereSign).equals(cell.getPoint())) {
					seenComeHereSignsSet.add(comeHereSign);
				}
			}
			moveTowards(smallestDistancePoint);
			return;
		}
		

		if(!followingArrows){//search for the closes arrow
			Arrow arrow = null;
			for(Object obj : space.getObjects()){
				
				if(obj instanceof Arrow){
					if(arrow == null && !isWallOnWayTo(grid.getLocation(obj)))
						arrow = (Arrow) obj;
					else
						if(arrow!=null && space.getDistance(space.getLocation(this), space.getLocation(obj)) 
								<space.getDistance(space.getLocation(this), space.getLocation(arrow)) && !isWallOnWayTo(grid.getLocation(obj))) {
							
							arrow = (Arrow) obj;
						}
							
				}
				
			}
			if(arrow!=null)
				moveInDirection(arrow.getDirection());
			else 
				moveInDirection(currentDirection);
		}else{
			moveInDirection(currentDirection);
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

	private void moveInDirection(double direction) {
		NdPoint myPoint = space.getLocation(this);
		GridPoint gp = grid.getLocation(this);
		
		try {
//			NdPoint ndPoint = space.moveByVector(this, 1, direction, 0);
//			for(Object obj : makeCollection(space.getObjectsAt(ndPoint.getX(), ndPoint.getY()))) {
//				if(obj instanceof Wall || obj instanceof Arrow) {
//					space.moveTo(this, myPoint.getX(), myPoint.getY());
//					return;
//				}
//			}
			GridPoint gp1 = grid.moveByVector(this, 1, direction, 0);
			for(Object obj : makeCollection(grid.getObjectsAt(gp1.getX(), gp1.getY()))) {
				if(obj instanceof Wall || obj instanceof Arrow) {
					grid.moveTo(this, gp.getX(), gp.getY());
					return;
				}
			}
		} catch(SpatialException e) {
			System.out.println(e);
		}
		
		
		
		try {
			gp = grid.getLocation(this);
			space.moveTo(this, gp.getX(), gp.getY());
		} catch(SpatialException e) {
			System.out.println(e);
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
	
	public static <E> Collection<E> makeCollection(Iterable<E> iter) {
	    Collection<E> list = new ArrayList<E>();
	    for (E item : iter) {
	        list.add(item);
	    }
	    return list;
	}

}
