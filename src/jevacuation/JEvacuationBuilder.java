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
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.Direction;
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
		Parameters parameters = RunEnvironment.getInstance().getParameters();
		String mapName = parameters.getValueAsString("map");
		try {
			readMap(context, mapName);
			prepareGuides(context, mapName+"_guides");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		context.add(new Exit(space,grid));
//		context.add(new Exit(space,grid));
		//znaki (dwa typy: strza�ka, mapa), poprawi� klinowanie follower�w, (random walk), ogie�
		//por�wnanie wersji ze strza�kami i followerami w sprawozdaniu
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
		
		int followerCount = (int) parameters.getValue("followerCount");
//		for(int i=0; i<followerCount; i++) {
//			Follower foll = new Follower(space, grid);
//			context.add(foll);
//			NdPoint pt = space.getLocation(foll);
//			while(grid.getObjectAt((int) pt.getX(), (int) pt.getY()) != null) {
//				context.remove(foll);
//				context.add(foll);
//				pt = space.getLocation(foll);
//			}
//			grid.moveTo(foll, (int) pt.getX(), (int) pt.getY());
//		}
		
		int arrowFollowerCount = (int) parameters.getValue("followerCount");
		for(int i=0; i<followerCount; i++) {
			ArrowFollower foll = new ArrowFollower(space, grid);
			context.add(foll);
			NdPoint pt = space.getLocation(foll);
			while(grid.getObjectAt((int) pt.getX(), (int) pt.getY()) != null) {
				context.remove(foll);
				context.add(foll);
				pt = space.getLocation(foll);
			}
			grid.moveTo(foll, (int) pt.getX(), (int) pt.getY());
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
				switch (line.charAt(i)) {
					case '0': //normal, empty field
						break;
					case '1': {
						Wall wall = new Wall(space, grid);
						context.add(wall);
						space.moveTo(wall, i, height);
						NdPoint pt = space.getLocation(wall);
						grid.moveTo(wall, (int) (pt.getX()+0.5), (int) (pt.getY()+0.5));
						break;
					}
					case '^': {
						Arrow arrow = new Arrow(context, space, grid, i, height, Direction.NORTH);
						break;
					}
					case '>':{
						Arrow arrow = new Arrow(context, space, grid, i, height, Direction.EAST);
						break;
					}
					case '<':{
						Arrow arrow = new Arrow(context, space, grid, i, height, Direction.WEST);
						break;
					}
					case 'v': {
						Arrow arrow = new Arrow(context, space, grid, i, height, Direction.SOUTH);
						break;
					}
					case '*': { //come here sign
						ComeHereSign comeHereSign = new ComeHereSign(context, space, grid, i, height);
						break;
					}
					case 'X': { //todo - only one exit!
						ComeHereSign comeHereSign = new ComeHereSign(context, space, grid, i, height);
						exit = new Exit(space, grid);
						context.add(exit);
						space.moveTo(exit, i, height);
						NdPoint pt = space.getLocation(exit);
						grid.moveTo(exit, (int) (pt.getX()+0.5), (int) (pt.getY()+0.5));
					}
					default:
						String waypointId = String.valueOf(line.charAt(i));
						Waypoint waypoint = new Waypoint(context, space, grid, i, height);
						mapWaypoints.put(waypointId, waypoint);
						
				}
			}
			height--;
		}
		
	}
	
	private void prepareGuides(Context<Object> context, String guideMapName) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(guideMapName));
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
