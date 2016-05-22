package jevacuation;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.Direction;

public class Arrow {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int x;
	private int y;
	private double direction;
	
	public Arrow(Context context, ContinuousSpace<Object> space, Grid<Object> grid, int x, int y, double d) {
		context.add(this);
		this.space = space;
		this.grid = grid;
		this.x = x;
		this.y = y;
		this.direction = d;
		
		space.moveTo(this, x, y);
		NdPoint pta = space.getLocation(this);
		grid.moveTo(this, (int) (pta.getX()+0.5), (int) (pta.getY()+0.5));
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public double getDirection(){
		return direction;
	}

}
