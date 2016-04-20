package jevacuation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.StrictBorders;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;

public class JEvacuationBuilder implements ContextBuilder<Object> {
	private static int WIDTH = 10;
	private static int HEIGHT = 10;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Exit exit;
	private List<Waypoint> waypoints = new ArrayList<Waypoint>();
	private Map<String, Waypoint> mapWaypoints = new HashMap<String, Waypoint>();

	@Override
	public Context build(Context<Object> context) {
		context.setId("jevacuation");
		
		
//		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
//		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.StrictBorders(), 50,50);
//		
//		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
//		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new repast.simphony.space.grid.StrictBorders(),new SimpleGridAdder<Object>(),true,50,50));
		
		try {
			readMap(context, "misc/map1.txt");
			prepareWaypoints(context);
			prepareGuides(context);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		context.add(new Exit(space,grid));
//		context.add(new Exit(space,grid));
		
		for(Object obj : context){
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(),(int)pt.getY());
		}
		
		int humanCount = 20;
		for(int i=0; i<humanCount; i++) {
			Human human = new Human(space, grid);
			context.add(human);
			NdPoint pt = space.getLocation(human);
			while(grid.getObjectAt((int) pt.getX(), (int) pt.getY()) != null) {
				context.remove(human);
				context.add(human);
				pt = space.getLocation(human);
			}
			grid.moveTo(human, (int) pt.getX(), (int) pt.getY());
			
			
		}
		
		return context;
	}
	
	private void readMap(Context<Object> context, String string) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(string));
		WIDTH = Integer.parseInt(lines.get(0));
		HEIGHT = Integer.parseInt(lines.get(1));
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new StrictBorders(), WIDTH, HEIGHT);
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new repast.simphony.space.grid.StrictBorders(), new SimpleGridAdder<Object>(), true, WIDTH, HEIGHT));
		int height = HEIGHT-1;
		
		for(String line : lines.subList(2, lines.size())) {
			for(int i=0; i<WIDTH; i++) {
				if(line.charAt(i)=='1') {
					Wall wall = new Wall(space, grid);
					context.add(wall);
					space.moveTo(wall, i, height);
					NdPoint pt = space.getLocation(wall);
					grid.moveTo(wall, (int) (pt.getX()+0.5), (int) (pt.getY()+0.5));
				}
				if(line.charAt(i)=='2') {
					exit = new Exit(space, grid);
					context.add(exit);
					space.moveTo(exit, i, height);
					NdPoint pt = space.getLocation(exit);
					grid.moveTo(exit, (int) (pt.getX()+0.5), (int) (pt.getY()+0.5));
				}
				
			}
			height--;
		}
		
	}
	
	private void prepareWaypoints(Context<Object> context) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("misc/waypoint1.txt"));
		for(String line : lines) {
			String[] parts = line.split(",");
			int x = Integer.parseInt(parts[1]);
			int y = Integer.parseInt(parts[2]);
			Waypoint waypoint = new Waypoint(context, space, grid, x, y);
			mapWaypoints.put(parts[0], waypoint);
		}
	}
	
	private void prepareGuides(Context<Object> context) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("misc/guides1.txt"));
		int SIZE = Integer.parseInt(lines.get(0));
		for(int i=1; i<=SIZE; i++) {
			String[] parts = lines.get(2*i-1).split(",");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			String[] chars = lines.get(2*i).split(",");
			List<Waypoint> waypoints = new ArrayList<Waypoint>();
			for(String character : chars) {
				waypoints.add(mapWaypoints.get(character));
			}
			
			Guide guide = new Guide(space, grid, waypoints);
			context.add(guide);
			space.moveTo(guide, x, y);
			NdPoint pts = space.getLocation(guide);
			grid.moveTo(guide, (int) (pts.getX()+0.5), (int) (pts.getY()+0.5));
		}
	}

}
