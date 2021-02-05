package org.swe.w2020.schneiderf99.client;
import java.util.*;
import java.util.stream.*;

import org.swe.w2020.schneiderf99.client.Types.Field;
import org.swe.w2020.schneiderf99.client.Types.PlayerPosition;
import org.swe.w2020.schneiderf99.client.Types.Terrain;

/**
 * Generator for specification conforming half maps.
 * There is a formal proof for the algorithms correctness.
 * @author Fabian Schneider
 *
 */
public class MapGenerator {


    
	/**
	 * generates a completely random half map adhering to the spcifications
	 * @return a valid random half map
	 */
    public static Collection<Field> generateHalfMap() {
    	return generateHalfMaps().findFirst().get();
    }
    
    public static Stream<Collection<Field>> generateHalfMaps(){
    	return getMaps().map(m -> {
    		Set<Field> halfMap = new HashSet<>();
        	
        	// generate the map
        	List<List<Terrain>> map = m;
        	
        	// randomly place the castle on a grass field
        	int grassCount = (int) intercalate(map).stream().filter((x) -> x == Terrain.GRASS).count();
        	int castle = new Random().nextInt(grassCount);
        	int grassCounter = 0;
        	for(int x = 0; x < map.size(); x++) {
        		for (int y = 0; y < map.get(x).size(); y++) {
        			boolean castleHere = map.get(x).get(y) == Terrain.GRASS && grassCounter++ == castle ? true : false;
        			halfMap.add(new Field(x,y,map.get(x).get(y), false,false,false,castleHere,PlayerPosition.NO_PLAYER)); 
        		}
        	}
        	
        	return halfMap;
    	});
    }
    
    /**
     * Returns an infinite stream of random maps
     * @return infinite stream of random maps
     */
    public static Stream<List<List<Terrain>>> getMaps(){
    	final Random r = new Random();
        
        // final size
        final int sizex = 8;
        final int sizey = 4;
        
        // generator for random maps
        Stream<List<List<Terrain>>> allEnvs =
            Stream.generate(() -> {
            	// an empty map
                List<List<Terrain>> res = new ArrayList<>();
                
                // an empty strech of the map
                List<Terrain> lt = new ArrayList<>();
                
                for(int i = 0 ; i < sizex; i++ ){
                    for(int j = 0 ; j < sizey; j++ ){
                    	
                    	// get a random number from 0 to 2 and add that to the code of a
                    	// this results in either a,b or c corresponding to the terrain types
                        switch (r.nextInt(3) + (byte)'a'){
                          case 'a':
                        	  // grass can be added without restrictions
                        	  lt.add(Terrain.GRASS);
                        	  break;
                          case 'b':
                        	  // water can only be added when it does not form any isles
                        	  // see the proof for details how tis filter works
                            if(i>0 && j>0 && j<(sizey-1) && 
                                    (  res.get(i-1).get(j-1) == Terrain.WATER
                                    || res.get(i-1).get(j+1) == Terrain.WATER)){
                                lt.add(Terrain.GRASS);
                            } else if (i>0 && j == 0 &&
                                       res.get(i-1).get(j+1) == Terrain.WATER) {
                                lt.add(Terrain.GRASS);
                            } else if (i>0 && j == sizey-1 &&
                                       res.get(i-1).get(j-1) == Terrain.WATER){
                                lt.add(Terrain.GRASS);
                            } else if (i==sizex-1 && res.get(i-1).get(j) == Terrain.WATER){
                                lt.add(Terrain.GRASS);
                            } else if (j == sizey-1 && lt.get(j-1) == Terrain.WATER){
                                lt.add(Terrain.GRASS);
                            } else {

                                lt.add(Terrain.WATER);
                            }
                            break;
                          case 'c':
                        	  // mountains can be added without restrictions
                            lt.add(Terrain.MOUNTAIN);
                            break;
                        }
                    }
                    res.add(lt);
                    lt=new ArrayList<>();
                }
                return res;
            });

        // filtering the maps according to the specifications
        return allEnvs.parallel()
              .filter(env -> Collections.frequency(intercalate(env), Terrain.MOUNTAIN) >= 3 )			// minimum count of mountains
              .filter(env -> Collections.frequency(intercalate(env), Terrain.GRASS) >= 15)				// minimum count of grass
              .filter(env -> Collections.frequency(intercalate(env), Terrain.WATER) >= 4 )				// minimum count of water
              .filter(env -> Collections.frequency(env.get(0), Terrain.WATER) <= 1 )					// water on small edge
              .filter(env -> Collections.frequency(env.get(env.size()-1), Terrain.WATER) <= 1 )			// water on small edge
              .filter(env -> Collections.frequency(getRow(0, env), Terrain.WATER) <= 3)					// water on big edge
              .filter(env -> Collections.frequency(getRow(env.get(0).size()-1, env), Terrain.WATER) <= 3);//water on big edge
    }
    
    /**
     * generates a random half map without a fort.
     * @return a random terrain map
     */
    public static List<List<Terrain>> generateMap(){
    	return getMaps().findFirst().get();// get only one map
        // return it; get is save because the generator keeps on generating until a map is found
		// there is not formal proof for termination of the algorithm
		// but there are examples of valid maps so it will terminate eventually
    }
    

    /**
     * concattenates lists in a list to a single list
     * (intercalate []) in haskell
     * @param <X> the type of the elements in the lists
     * @param l the list to be intercalated
     * @return a flattened list
     */
  private static <X> List<X> intercalate(List<List<X>> l){
    List<X> res = new LinkedList<>();
    for(List<X> ls : l){
      res.addAll(ls);
    }
    return res;
  }


  /**
   * retrieves a row in a matrix e.g. every 2nd element of the lists inside map
   * note that the term row instead of column was used because of the indexing scheme of the game map 
   * @param <X> the type of the lists elements
   * @param x the index of the row
   * @param map the matrix
   * @return a row of the matrix
   */
  private static <X> List<X> getRow(int x, List<List<X>> map){
    List<X> res = new LinkedList<>();
    for (List<X> l : map){
        res.add(l.get(x));
    }
    return res;
  }
}

