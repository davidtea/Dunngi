
import java.util.ArrayList;
/**
 * Tower class will hold two integers to indicate its position on the board. 
 * It will also hold an ArrayList of pieces to show which pieces are currently
 * in the tower. There can be a maximum of 3 pieces per Tower and a minimum of 0.
 *
 */
public class Tower 
{
	private int row, col;
	//row for row number, col for column number
	private ArrayList<Piece> t = new ArrayList<Piece>();
	//ArrayList to hold the pieces in each Tower
	
	/**
	 * Constructor for Tower. Takes in two integers to determine its position on the board.
	 * Tower will not move around the board, only pieces will. Pieces will move from one 
	 * tower to the next. 
	 * @param r row number
	 * @param c column number
	 */
	public Tower(int r, int c)
	{
		row = r;
		col = c;
	}
	
	/**
	 * add function takes in a piece and adds it to the Tower's ArrayList. It will
	 * be used to take in sub class pieces and add it to the tower. This allows the towers
	 * to "stack" pieces. The top most piece is the only one able to move in the tower
	 * and the tower's height determines its moveset and attack range.
	 * @param p Piece to be added to the tower
	 */
	public void add(Piece p)
	{
		//when an enemy removes the other's piece, we need to change the piece's color. We should 
		//here and change if needed.
		t.add(p);
		p.changeTier(t.size());
	}
	
	/**
	 * returns column number of the tower
	 * @return col
	 */
	public int getCol()
	{
		return col;
	}
	
	/**
	 * returns row number of the tower
	 * @return row
	 */
	public int getRow()
	{
		return row;
	}
	
	/**
	 * Removes a certain piece from the tower's ArrayList. Return the piece, so it can
	 * be added to its new destination. Then there will be no need to create and delete
	 * pieces.
	 * @param piece position of piece in tower(0-2)
	 * @return the removed piece
	 */
	public Piece remove(int piece)
	{
		
		return t.remove(piece);
	}
	
	/**
	 * Removes the top piece from the tower and returns it
	 * @return top piece
	 */
	public Piece removeTop()
	{
		return t.remove(t.size()-1);
	}
	
	/**
	 * Takes the top piece from this tower and adds it to the top of the indicated tower
	 * @param T target tower
	 */
	public void addToTower(Tower T)
	{
		T.add(remove(t.size()-1)); //takes the top piece and moves it to the indicated tower.
	}
	
	/**
	 * Function will return a string that represents the pieces in the tower.
	 * Used in displaying the board's current condition.
	 * @return String representation of the tower's pieces
	 */
	public String getTowerID()
	//returns the string code for a tower in the form of 1st-2nd-3rd or # for empty
	{
		String towerString = "";
		if (t.size() == 0) //if tower is empty
			return towerString + "#";
		else
		{
			for(int i = 0; i < t.size(); i++)
			{
					towerString+=(t.get(i).getID()+"-");
			}
			towerString = towerString.substring(0, towerString.length()-1);
			return towerString;
		}
	}
	
	/**
	 * Checks if there's a pawn in this tower
	 * @return true/false
	 */
	public Boolean checkPawn() //checks if there's a pawn in the tower
	{

		for(int i=0; i < t.size(); i++)
		{
			if(t.get(i).getID() == 'p')
				return true;
		}
		return false;
	}
	
	/**
	 * returns the piece at the indicated position in the tower
	 * @param i position in tower
	 * @return piece
	 */
	public Piece getPiece(int i) //needed to check which piece belongs to who when in same tower
	{
		return t.get(i);
	}
	
	/**
	 * returns the top piece of this tower
	 * @return top piece
	 */
	public Piece getTop()
	{
		if(t.size() <= 0)
			return null;
		else
			return t.get(t.size()-1);
	}
	
	/**
	 * function will return an integer that represents how many pieces are currently in the tower.
	 * @return Integer represents the tower's current number of pieces within
	 */
	public int getHeight()
	{
		return t.size();
	}
}
