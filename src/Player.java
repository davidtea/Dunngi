import java.util.ArrayList;

/**
 * Player class will hold an ArrayList of Piece. This will represent the player's hand. 
 * Players will drop pieces from their hand onto the board. Player will also hold a 
 * char color to indicate which player it is. Player class will allow the player to
 * hold the pieces they have captured and to drop those pieces onto the board. It will
 * be initialized with the 23 pieces each player starts out with and they will each have
 * the color of their respective players.
 */
public class Player 
{
	private ArrayList<Piece> hand = new ArrayList<Piece>(); 
	private char color;
	private int score;
	
	/**
	 * Player constructor takes in a char to differentiate which player it is, 
	 * black or white. The constructor will automatically generate the 23 pieces and 
	 * add them to their hands. 
	 * @param c color to represent each player (white or black)
	 */
	public Player(char c)
	{
		//starting hand
		color = c;
		score = 0;
		//creates each piece with the color of their respective players
		for(int i = 0; i < 8; i++)
			hand.add(new Piece(color, 'p', 3));
		
		hand.add(new Piece(color, 'a', 5));
		hand.add(new Piece(color, 'a', 5));
		hand.add(new Piece(color, 's', 8));
		hand.add(new Piece(color, 's', 8));
		hand.add(new Piece(color, 'k', 5));
		hand.add(new Piece(color, 'k', 5));
		hand.add(new Piece(color, 'u', 5));
		hand.add(new Piece(color, 'u', 5));
		hand.add(new Piece(color, 'l', 10));
		hand.add(new Piece(color, 'j', 7));
		hand.add(new Piece(color, 'm', 10));
		hand.add(new Piece(color, 'c', 100));
	}
	
	/**
	 * Adds the piece to the player's hand
	 * @param p Piece
	 */
	//when capturing a piece, call this
	public void addToHand(Piece p)
	{
		if(p.getColor() != color)
		{
			p.changeColor();
		}
		hand.add(p);
	}
	/**
	 * Function will take in a tower and position. The tower must be a tower that contains pieces
	 * that belong to both players. pos is the position of the piece belonging to the player that
	 * is attacking. The function will capture the piece directly above, since immobile strike 
	 * can only target pieces directly above or below.
	 * 
	 * @param tower the tower that holds the two different player pieces
	 * @param pos position of piece in tower belonging to attacking player
	 */
	
	public void captureAbove(Tower tower, int pos) 
	{
		score+=tower.getTop().getValue();
		addToHand(tower.remove(pos + 1)); //above the piece
	}
	
	/**
	 * Same as captureAbove() except it will target the piece blow instead. Must be different 
	 * colors.
	 * @param tower the tower that holds the two different player pieces
	 * @param pos position of piece in tower belonging to attacking player
	 */
	public void captureBelow(Tower tower, int pos)
	{
		addToHand(tower.remove(pos - 1)); //below the piece
	}
	
	/**
	 * Takes the attacked tower's top piece and adds it to the attacker's hand, then moves the attacking piece to the top of the attacked tower.
	 * @param attacker Attacking piece
	 * @param attacked Targeted piece
	 */
	public void capturePiece(Tower attacker, Tower attacked) //captures piece and moves piece to destination
	{
		score+=attacked.getTop().getValue();
		attacked.getTop().changeColor();
		addToHand(attacked.removeTop());
		attacked.add(attacker.removeTop());
	}
	
	/**
	 * Special capture function for ranged units.  The attacked piece
	 * is added to the attackers hand, but the attacker's piece doesn't move
	 * to the location where the captured piece was
	 * 
	 * @param attacker the tower that holds the attacking player
	 * @param attacked target tower
	 */
	public void rangedCapture(Tower attacker, Tower attacked)
	{
		score+=attacked.getTop().getValue();
		addToHand(attacked.removeTop());
	}
	
	/**
	 * returns the piece at the specified position in the hand
	 * @param pos position in hand
	 * @return piece at position
	 */
	//takes in an int from player to indicate piece position in hand
	public Piece getPiece(int pos)
	{
		if(pos > hand.size())
			return null;
		else	
			return hand.get(pos);
	}
	
	/**
	 * Reduces score by i
	 * @param i number 
	 */
	public void reclaimEffect(int i)
	{
		score -= i;	
	}
	
	/**
	 * removes the piece at hand position and returns it
	 * @param pos position in hand
	 * @return piece at position
	 */
	public Piece removePiece(int pos)
	{
		return hand.remove(pos);
	}
	
	/**
	 * returns current hand size
	 * @return current hand size
	 */
	public int getSize()
	{
		return hand.size();
	}
	
	/**
	 * Returns player's color (a char)
	 * @return color.
	 */
	public char getColor()
	{
		return color;
	}
	
	/**
	 * returns value of player's score
	 * @return score
	 */
	public int getScore()
	{
		return score;
	}
	
	/**
	 * printHand() will display what pieces the player currently has. Will use the same 
	 * formatting as Board's printBoard(). Each piece will be represented by a String
	 * character.
	 */
	public void printHand()
	{
		System.out.println("Player " + color + " Hand: ");
		for(Piece p: hand)
			System.out.printf("%2s ", p.getID());
		System.out.println();
		
		int i=0;
		for(Piece p: hand)
		{
			System.out.printf("%2d ", i);
			i++;
		}
		System.out.println();
	}
	
	/**
	 * Use this function to check when a player has successfully captured the enemy's commander. Will be called at the 
	 * end of each player's turn. Then when it returns true, the game ends and the victor is declared.
	 * @return Boolean, true if player holds enemy commander, false if no.
	 */
	public Boolean checkHandComm()
	{
		for(Piece p: hand)
		{
			if(p.getID() == 'c')
				return true;
		}
		return false;
	}
	
	/**
	 * Returns a string of a player's color
	 */
	public String toString()
	{
		String name = "";
		if (color == 'b')
			name = "Black";
		else 
			name = "White";
		return name;
	}
}
