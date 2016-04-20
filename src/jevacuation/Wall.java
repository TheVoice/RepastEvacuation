package jevacuation;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Wall {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public Wall(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}

}
