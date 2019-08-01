package game.model;

/**
 * Stores row and column coordinates, overrides equals method to get used
 * in iterations.
 * @author student
 */
public class Coord {
	
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	int x;
	int y;
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public boolean equals(Object coord) {
		
		boolean result;

		if ((coord == null) || (getClass() != coord.getClass())){
	        result = false;
	    } else {
	    	Coord otherCoord = (Coord)coord;
	        result = getX() == otherCoord.getX() && getY() == otherCoord.getY();
	    }

		return result;
	}	
}
