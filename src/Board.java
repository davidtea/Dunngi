/**
 * The Board class will be a 8x8 2D array of towers. The GUI will interact with the board.
 * Board will be initialized with empty towers. The board will have functions that will 
 * interact with the GUI to show the towers and the pieces in each tower. It will also
 * show the available movements/attacks each piece can make when they are selected. 
 * The Board is able to pass its current state to the other classes through a String
 * representation. 
 */
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


public class Board 
{
	private static Tower[][] towerMatrix = new Tower[9][9];
	public Boolean dropPhaseOver;
	
	/**
	 * Default Constructor for Board, calls initialize() and creates an
	 * empty 9x9 2D array of empty Towers.
	 */
	public Board()
	{
		initialize();
		dropPhaseOver = false;
	}
	
	//game functions
	
	/**
	 * Returns an array of Moves of all possible spots a specific piece that the player selected can drop. During 
	 * the game's initial phase, pieces can only be dropped on the first 3 rows of each player's side. Also, there 
	 * must be a pawn for each player in each column. During the initial stage, pieces can be stacked on each other
	 * but after the initial phase, pieces can only be dropped on empty spaces. After the initial phase, the range of
	 * possible drop spots extends from the player's side to just before the other player's first 3 rows. 
	 * @param player current player dropping
	 * @param piece piece that the player wants to drop
	 * @return ArrayList of moves that represent all possible spots that piece can be dropped.
	 */
	public ArrayList<Move> getDrops(Player player, int piece)
	{
		Piece temp = player.getPiece(piece);
		ArrayList<Move> moves = new ArrayList<Move>();
		
		if(dropPhaseOver == false) //initial phase
		{
			if(player.getColor() == 'b') //black player
			{
				for(int i=0; i<2; i++)
				{
					for (int j=0; j<8; j++)
					{
						if(temp.getID() == 'p' && checkRowForPawn(player, j) == false)
							moves.add(new Move(i,j));
						else if(temp.getID() != 'p' && accessTower(new Move(i,j)).getHeight() < 3)
							moves.add(new Move(i,j));
					}
				}
			}
			else //white player
			{
				for(int i = 6; i < 8; i++)
				{
					for (int j = 0; j < 8; j++)
					{
						if(temp.getID() == 'p' && checkRowForPawn(player, j) == false)
							moves.add(new Move(i,j));
						else if(temp.getID() != 'p' && accessTower(new Move(i,j)).getHeight() < 3)
							moves.add(new Move(i,j));
					}
				}
			}
		}
		else //after initial phase
		{
			if(player.getColor() == 'b') //black player
			{
				for(int i=0; i < 7; i++)
				{
					for (int j=0; j < 8; j++)
					{
						if(checkEmpty(new Move(i,j)))
								moves.add(new Move(i,j));
					}
				}
			}
			else //white player
			{
				for(int i = 2; i < 8; i++)
				{
					for (int j = 0; j < 8; j++)
					{
						if(checkEmpty(new Move(i,j)))
							moves.add(new Move(i,j));
					}
				}
			}
		}
//		System.out.println(moves.size());
		return moves;
	}
	
	/**
	 * finds a matching piece to "piece" and adds it to the top of the tower with position "pos",
	 * if piece cannot be found in the player's hand then it doesn't do anything
	 * @param player current player
	 * @param piece piece that the player wants to drop onto board, position of piece in hand 
	 * @param pos position of the tower on the board that the player wants to drop their piece
	 * @return true/false
	 */
	//will change to int later to check if piece can be found in hand, return -1 if no, 1 if it works
	public Boolean dropPiece(Player player, int piece, Move pos)
	{
		Piece temp = player.getPiece(piece);
		
		if(dropPhaseOver == true && checkEmpty(pos) == false) //dropPhase is over, square is occupied
			return false;
		else if(dropPhaseOver == false && accessTower(pos).getHeight() >= 3)
			return false;
		else if(temp.getID() == 'p' && checkRowForPawn(player, pos.getLeftRight()))
			return false;
		else	
		{
			player.removePiece(piece);
			addToBoard(temp, pos);
			return true;
		}
	}
	
	/**
	 * This method is called from the Game class in the method 'playerTurn'.  The
	 * player is prompted to input where they want to move their piece and the piece is moved if
	 * the input was in the availble move list.  If not the function calls itself recursively so 
	 * the player can input a new move.
	 * 
	 * @param attacker The player that is attacking
	 * @param moves ArrayList of available moves for the piece
	 * @param from Original location of the moving piece
	 */
	public void actionPiece(Player attacker, ArrayList<Move> moves, Move from)
	{
		Scanner in = new Scanner(System.in);
		Move to = new Move();
		Boolean repeat = true;
		
		System.out.print("Available Moves: ");
		for (Move m: moves)
			System.out.print(m);
		
		System.out.println();
		System.out.println("Where do you want to move to?");
		System.out.println("Row: ");
		to.setUpDown(in.nextInt());
		System.out.println("Col: ");
		to.setLeftRight(in.nextInt());
		
		for (Move i: moves)
		{
			if (to.equals(i))
			{
				if (checkEmpty(to.getUpDown(), to.getLeftRight()))
					towerMatrix[to.getUpDown()][to.getLeftRight()].add(towerMatrix[from.getUpDown()][from.getLeftRight()].removeTop());
				else
					attacker.capturePiece(towerMatrix[from.getUpDown()][from.getLeftRight()], towerMatrix[to.getUpDown()][to.getLeftRight()]);
				
				repeat = false;
			}
		}
		if (repeat == true)
		{
			System.out.println("***Invalid move, please try again***");
			actionPiece(attacker, moves, from);
		}
	}
	
	/**
	 * Same as the 'actionPiece' method above except with special modifications
	 * to accommadate the functionality of ranged pieces which have separate move
	 * sets for moving and capturing.
	 * 
	 * @param attacker The player that is attacking
	 * @param moves ArrayList of available moves for the piece
	 * @param attacks ArrayList of available attacks for the piece
	 * @param from Original location of the moving piece
	 */
	public void actionRangedPiece(Player attacker, ArrayList<Move> moves, ArrayList<Move> attacks, Move from)
	{
		Scanner in = new Scanner(System.in);
		Move to = new Move();
		Boolean repeat = true;
		
		System.out.print("Available Attacks: ");
		for (Move m: attacks)
			System.out.print(m);
		
		System.out.println();
		System.out.print("Available Moves: ");
		for (Move m: moves)
			System.out.print(m);
		
		System.out.println();
		System.out.println("Where do you want to move to?");
		System.out.println("Row: ");
		to.setUpDown(in.nextInt());
		System.out.println("Col: ");
		to.setLeftRight(in.nextInt());
		
		for (Move i: moves)
			if (to.equals(i))
			{
				if (checkEmpty(to.getUpDown(), to.getLeftRight()))
					towerMatrix[to.getUpDown()][to.getLeftRight()].add(towerMatrix[from.getUpDown()][from.getLeftRight()].removeTop());
				
				repeat = false;
			}
		for (Move i: attacks)
			if (to.equals(i))
			{
				if (checkIfEnemyOrAllyTowerNotFull(towerMatrix[from.getUpDown()][from.getLeftRight()].getTop(), to.getUpDown(), to.getLeftRight()))
				{
					attacker.rangedCapture(towerMatrix[from.getUpDown()][from.getLeftRight()], towerMatrix[to.getUpDown()][to.getLeftRight()]);
					System.out.println("Successful ranged attack");
					
					repeat = false;
				}
			}
		
		if (repeat == true)
		{
			System.out.println("***Invalid move, please try again***");
			actionRangedPiece(attacker, moves, attacks, from);
		}
	}
	
	/**
	 * calls capturePiece() from Player class. Captures the target tower's top piece and then moves the 
	 * attacking piece to the target's position.
	 * @param attacker The player that is attacking
	 * @param ally piece that the player will be using to attack
	 * @param enemy piece that the player is targeting
	 */
	public void capturePiece(Player attacker, Tower ally, Tower enemy) 
	{
		attacker.capturePiece(ally,enemy);
	}
	
	//Accessor Functions
	
	/**
	 * Allows other classes to access the towers in board. returns the tower at positon i, j
	 * @param i row number
	 * @param j column number
	 * @return Tower at (i,j)
	 */
	public static Tower getTower(int i, int j)
	{
		return towerMatrix[i][j];
	}
	
	/**
	 * Allows other classes to access the towers in board. returns the tower at pos
	 * @param pos position of tower
	 * @return Tower at pos
	 */
	public static Tower accessTower(Move pos)
	{
		return towerMatrix[pos.getUpDown()][pos.getLeftRight()];
	}
	
	/**
	 * Creates the 2D array of empty Towers for the Board. Each 9x9 spot has one empty tower.
	 */
	public void initialize()
	{
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				towerMatrix[i][j] = new Tower(i,j);
	}

	/**
	 * checkEmpty will check if a certain Tower on the board is 
	 * empty or not. r is used for the rows and c is used for the 
	 * columns. Returns a boolean that indicates whether the tower is empty or not.
	 * 
	 * @param r row number of the board
	 * @param c column number of the board
	 * @return boolean true/false if the tower is empty or not.
	 */
	public static boolean checkEmpty(int r, int c)
	{
		if (r > -1 && r < 8 && c > -1 && c < 8 && towerMatrix[r][c].getHeight() == 0)
			return true;
		return false;
	}
	
	/**
	 * Returns true if the target location is occupied by
	 * a tower whose top piece belongs to the opposing player.
	 * 
	 * @param p piece
	 * @param r row number of the board
	 * @param c column number of the board
	 * @return boolean returns true if the square is occupied by a tower whose top piece is a different color than the parameter 'p'.
	 */
	public static boolean checkIfEnemyOrAllyTowerNotFull(Piece p, int r, int c)
	{
		if (r > -1 && r < 8 && c > -1 && c < 8 && towerMatrix[r][c].getHeight() != 0)
		{
			if(towerMatrix[r][c].getTop().getColor() != p.getColor())
				return true;
			else if(towerMatrix[r][c].getTop().getColor() == p.getColor() && towerMatrix[r][c].getHeight() < 3 && !checkForCommander(r,c))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks the tower at position r, c for a commander.
	 * @param r row number
	 * @param c column number
	 * @return true/false
	 */
	public static boolean checkForCommander(int r, int c)
	{
		if (towerMatrix[r][c].getTop().getID() == 'c' || towerMatrix[r][c].getTop().getID() == 'C')
			return true;
		
		return false;
	}
	
	/**
	 * Checks the tower at position r, c for a spy
	 * @param r row number
	 * @param c column number
	 * @return true/false
	 */
	public static boolean checkForSpy(int r, int c)
	{
		if (towerMatrix[r][c].getHeight() != 0)
			if (towerMatrix[r][c].getTop().getID() == 's' || towerMatrix[r][c].getTop().getID() == 'S')
				return true;
		
		return false;
	}
	
	/**
	 * checks if the tower at the position r, c is full or not
	 * @param r row number
	 * @param c column number
	 * @return true/false
	 */
	public static boolean checkIfTowerNotFull(int r, int c)
	{
		if (r > -1 && r < 8 && c > -1 && c < 8 && towerMatrix[r][c].getHeight() != 0)
			if (towerMatrix[r][c].getHeight() < 3)
				return true;
		
		return false;
	}
	
	/**
	 * checkEmpty will check if a certain Tower on the board is 
	 * empty or not. r is used for the rows and c is used for the 
	 * columns. Returns a boolean that indicates whether the tower is empty or not.
	 * 
	 * @param position postion
	 * @return boolean true/false if the tower is empty or not.
	 */
	public boolean checkEmpty(Move position)
	{
		if (position.getUpDown() > -1 && position.getUpDown() < 9 && position.getLeftRight() > -1 && position.getLeftRight() < 9 && towerMatrix[position.getUpDown()][position.getLeftRight()].getHeight() == 0)
			return true;
		return false;
	}
	
	/**
	 * Checks the specified row for a pawn belonging to the specified player. Used during initial dropping phase.
	 * @param player Player that's dropping
	 * @param col column number
	 * @return true/false
	 */
	public Boolean checkRowForPawn(Player player, int col) //tells when a row has a pawn, for dropping phase
	{
		if (player.getColor() == 'w')
		{
			for(int i = 6; i < 8; i++)
				if(towerMatrix[i][col].checkPawn() == true)
					return true;
		}
		else if (player.getColor() == 'b')
		{
			for(int i = 0; i < 2; i++)
				if(towerMatrix[i][col].checkPawn() == true)
					return true;
		}
		return false;
	}
	
	/**
	 * checks to see if the top piece on the Tower at the indicated position is an archer or musketeer.
	 * @param position the target tower's position
	 * @return true if it is archer or musketeer, else false
	 */
	public boolean checkRanged(Move position)
	{
		if(accessTower(position).getTop().getID() == 'a' ||  accessTower(position).getTop().getID() == 'u')
			return true;
		else 
			return false;
	}
	

	/**
	 * Adds the piece to the selected position
	 * @param piece piece to be dropped
	 * @param pos position
	 */
	public void addToBoard(Piece piece, Move pos)
	{
		towerMatrix[pos.getUpDown()][pos.getLeftRight()].add(piece);
	}
	
	/**
	 * sets dropPhaseOver to true, indicating that the initial drop phase is over. This changes the dropping behavior.
	 */
	public void setDropPhaseOver()
	{
		dropPhaseOver = true;
	}
	
	/**
	 * returns a string representation of the board. Each tower is represented by its pieces' ids.
	 * @return String of board
	 */
	public String boardToString()
	//**We're actually not using this but keep it for now**
	//converts the board state to string
	//targeted piece parses the string in order to determine available moves
	{
		String boardString = "";
		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 8; j++)
				boardString+=(towerMatrix[i][j].getTowerID()+"/");
		boardString = boardString.substring(0, boardString.length()-1);
		return boardString;
	}
	
	
	/**
	 * Displays the entire Board and its towers to the console. Each piece has a unique 
	 * indicator and empty towers are left blank.
	 */
	public void printBoard()
	{
		for (int i = 0; i < 8; i++)
		{
			System.out.println();
			for (int j = 0; j < 8; j++)
				System.out.printf("%-8s ", towerMatrix[i][j].getTowerID());
			System.out.println();
		}
		System.out.println();
		System.out.println("******************************************************************");
		System.out.println();
	}
	
	/**
	 * Begins the initial dropping phase. Randomizes the piece and position dropped. Not used in actual game between players, only for testing.
	 * @param p Current player dropping
	 */
	public void playerInitializeDrop(Player p)
	{
		int piece;
		Move from = new Move();
		Random rn = new Random();
		ArrayList<Move> drops = new ArrayList<Move>();
		
		if(p.getSize() > 0)
		{
			System.out.println("It's " + p.toString() + "'s turn.");
			
			p.printHand();
			System.out.println("Which piece do you want to drop? (Piece number)");
			piece = rn.nextInt(p.getSize());
			System.out.println("Where do you want to drop? (2 ints, \"# #\")");
			
			
			drops = getDrops(p);
			from = drops.get(rn.nextInt(drops.size()-1));

			System.out.println(piece + ", " + from.toString());
			
			while(!dropPiece(p, piece, from))
			{
				System.out.println("Invalid spot to drop. Please try again.");
				p.printHand();
				System.out.println("Which piece do you want to drop? (Piece number)");
				piece = rn.nextInt(p.getSize());
				System.out.println("Where do you want to drop? (2 ints, \"# #\")");	
				
				drops = getDrops(p);
				from = drops.get(rn.nextInt(drops.size()- 1));
				
				System.out.println(piece + ", " + from.toString());
			}
		}
	}
	
	/**
	 * Returns an array of moves that represent the available positions a piece can be dropped. During initial drop phase, pieces can be dropped in the first two rows of the 
	 * respective players and can also be dropped on top of each other, excluding the commander. After the initial dropping phase, pieces can be dropped up to the other player's 
	 * side of the board, not including the other player's first two rows. Also, pieces can no longer be dropped on top of each other.
	 * @param p Current Player
	 * @return arraylist of available dropping spots
	 */
	public ArrayList<Move> getDrops(Player p)
	{
		ArrayList<Move> drops = new ArrayList<Move>();
		char color = p.getColor();
		
		if (dropPhaseOver == true)
		{
			if(color == 'w')
				for (int i = 2; i<8; i++)
					for (int j = 0; j<8; j++)
						if (checkEmpty(i,j))
							drops.add(new Move(i,j));
			
			if(color == 'b')
				for (int i = 0; i<6; i++)
					for (int j = 0; j<8; j++)
						if (checkEmpty(i,j))
							drops.add(new Move(i,j));
		}
		else
		{
			if(color == 'w')
				for (int i = 6; i<8; i++)
					for (int j = 0; j<8; j++)
						if (checkEmpty(i,j) || (checkIfTowerNotFull(i,j) && !checkForCommander(i,j)))
							drops.add(new Move(i,j));
			
			if(color == 'b')
				for (int i = 0; i<2; i++)
					for (int j = 0; j<8; j++)
						if (checkEmpty(i,j) || (checkIfTowerNotFull(i,j) && !checkForCommander(i,j)))
							drops.add(new Move(i,j));
			
			//drop phase has ended
		}
		return drops;
	}
	
	/**
	 * Gets all the possible spots a musketeer or archer can attack.
	 * @param t Tower with an archer or musketeer on top
	 * @return arraylist of possible attack spots
	 */
	//this function is only for checking what squares the command can move to
	public ArrayList<Move> getPotentialRangedAttacks(Tower t)
	{
		ArrayList<Move> attacks = new ArrayList<Move>();
		Piece p = t.getTop();
		
		switch(p.getID())
		{
			case 'a':
				attacks = archerAttackSquares(p, t.getRow(), t.getCol());
				break;
			case 'A':
				attacks = archerAttackSquares(p, t.getRow(), t.getCol());
				break;
			case 'u':
				attacks = muskAttackSquares(p, t.getRow(), t.getCol());
				break;
			case 'U':
				attacks = muskAttackSquares(p, t.getRow(), t.getCol());
				break;
			default:
				break;
		}
		return attacks;
	}
	
	/**
	 * Gets all the potential captures an archer or musketeer can make that turn. Different from getPotentialRangedAttacks, this only returns possible captures and not empty squares
	 * @param t Attacking Tower
	 * @return arraylist of targets
	 */
	public ArrayList<Move> getRangedAttacks(Tower t)
	{
		ArrayList<Move> attacks = new ArrayList<Move>();
		Piece p = t.getTop();
		
		switch(p.getID())
		{
			case 'a':
				attacks = archerCap(p, t.getRow(), t.getCol());
				break;
			case 'A':
				attacks = archerCap(p, t.getRow(), t.getCol());
				break;
			case 'u':
				attacks = muskCap(p, t.getRow(), t.getCol());
				break;
			case 'U':
				attacks = muskCap(p, t.getRow(), t.getCol());
				break;
			default:
				break;
		}
		return attacks;
	}
	 /**
	  * Gets a list of all possible moves a piece can make.
	  * @param t Tower containing piece that will be moving
	  * @return ArrayList of possible movement spots
	  */
	public ArrayList<Move> getMoves(Tower t) 
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		Piece p = t.getTop();
		
		switch (p.getID()) 
		{
		//white pieces
		case 'c':
			
			moves = comMove(p, t.getRow(), t.getCol());
			
			antiCheck(p, moves);
	
			break;
		case 'p':
			moves = pawnMove(p, t.getRow(), t.getCol());			
			break;
		case 'a':
			moves = archerMove(p, t.getRow(), t.getCol());
			break;
		case 'k':
			moves = knightMove(p, t.getRow(), t.getCol());
			break;
		case 'l':
			moves = ltMove(p, t.getRow(), t.getCol());
			break;
		case 'm':
			moves = marshalMove(p, t.getRow(), t.getCol());
			break;
		case 'j':
			moves = majorMove(p, t.getRow(), t.getCol());
			break;
		case 'u':
			moves = muskMove(p, t.getRow(), t.getCol());
			break;
		case 's':
			moves = spyMove(p, t.getRow(), t.getCol());
			break;
		//black pieces
		case 'C':
			moves = comMove(p, t.getRow(), t.getCol());
			antiCheck(p, moves);
			break;
		case 'P':
			moves = pawnMove(p, t.getRow(), t.getCol());
			break;
		case 'A':
			moves = archerMove(p, t.getRow(), t.getCol());
			break;
		case 'K':
			moves = knightMove(p, t.getRow(), t.getCol());
			break;
		case 'L':
			moves = ltMove(p, t.getRow(), t.getCol());
			break;
		case 'M':
			moves = marshalMove(p, t.getRow(), t.getCol());
			break;
		case 'J':
			moves = majorMove(p, t.getRow(), t.getCol());
			break;
		case 'U':
			moves = muskMove(p, t.getRow(), t.getCol());
			break;
		case 'S':
			moves = spyMove(p, t.getRow(), t.getCol());
			break;
		}
		return moves;
	}

	/**
	 * Prevents the COMMANDER from moving into check. It accomplishes this by
	 * finding all the possible moves of all pieces owned by the opponent of the
	 * owner of the moving COMMANDER. Returns the ArrayList of possible moves.
	 * 
	 * @param p The commander being moved
	 * @param moveList movelist of commander
	 * @return corrected movelist for commander.
	 */
	public static ArrayList<Move> antiCheck(Piece p, ArrayList<Move> moveList) {
		ArrayList<Move> attackedSquares = new ArrayList<Move>();
		ArrayList<Move> temp = new ArrayList<Move>();

		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++) 
			{
				//checks the color of the top of tower at square i, j
				if (!checkEmpty(i, j) && getTower(i, j).getTop().getColor() != p.getColor()) 
				{
					
					char tempID = Character.toLowerCase(getTower(i, j).getTop().getID());
					if (tempID == 'a' || tempID == 'u')
						temp = GUI.b.getPotentialRangedAttacks(getTower(i, j));
					else if (tempID == 'c')
					{
						comMove(getTower(i, j).getTop(),i,j);
					}
					else
						temp = GUI.b.getMoves(getTower(i, j));
					for (Move m : temp) 
						attackedSquares.add(m);
				}
			}
		}

		attackedSquares = deleteRepeats(attackedSquares);

		for (int j=0; j<attackedSquares.size(); j++)
			for (int i=0; i<moveList.size(); i++)
				if (moveList.get(i).equals(attackedSquares.get(j)))
					moveList.remove(moveList.get(i));
		
		return moveList;
	}
	/**
	 * Checks if an immobile strike can be performed
	 * @param t Tower
	 * @param turn current player
	 * @return true/false
	 */
	public boolean checkForImmobileStrike(Tower t, char turn)
	{
		if (t.getHeight() > 1  && t.getPiece(t.getHeight()-2).getColor() == turn)
		{
			if (t.getTop().getColor() != t.getPiece(t.getHeight()-2).getColor() && !checkForSpy(t.getRow(),t.getCol()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * checks if a pawn or spy can be reclaimed, when it reaches the other side's last two rows.
	 * @param t Tower
	 * @return true/false
	 */
	public boolean checkForReclaim(Tower t)
	{
		
		char id = Character.toLowerCase(t.getTop().getID());
		
		if (id == 'p' || id == 's')
		{
			if (t.getTop().getColor() == 'w')
			{
				if (t.getRow() < 2)
				{
					return true;
				}
			}
			else
			{
				if (t.getRow() > 5)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Supplements the function "antiCheck" by trimming the output ArrayList of
	 * all repeat Moves
	 * 
	 * @param myList The ArrayList to be trimmed
	 * @return Corrected arraylist without repeats
	 */
	public static ArrayList<Move> deleteRepeats(ArrayList<Move> myList) 
	{
		for (int i = 0; i < myList.size(); i++) 
		{
			for (int j = i+1; j < myList.size(); j++) 
			{
				if (myList.get(i).equals(myList.get(j)))
				{
					myList.remove(myList.get(i));
				}
			}
		}
		return myList;
	}
	
/*/*****************************************MOVELIST METHODS**********************************************/
	
	/**
	 * Moveset for Commander
	 * @param p Commander
	 * @param row row number
	 * @param col column number
	 * @return list of possible movements
	 */
	public static ArrayList<Move> comMove(Piece p, int row, int col) {
		ArrayList<Move> moves = new ArrayList<Move>();
		
		
		if (checkEmpty(row + 1, col) || checkIfEnemyOrAllyTowerNotFull(p, row + 1, col))
			moves.add(new Move(row + 1, col));
		if (checkEmpty(row + 1, col + 1) || checkIfEnemyOrAllyTowerNotFull(p, row + 1, col + 1))
			moves.add(new Move(row + 1, col + 1));
		if (checkEmpty(row + 1, col - 1) || checkIfEnemyOrAllyTowerNotFull(p, row + 1, col - 1))
			moves.add(new Move(row + 1, col - 1));
		if (checkEmpty(row - 1, col) || checkIfEnemyOrAllyTowerNotFull(p, row - 1, col))
			moves.add(new Move(row - 1, col));
		if (checkEmpty(row - 1, col + 1) || checkIfEnemyOrAllyTowerNotFull(p, row - 1, col + 1))
			moves.add(new Move(row - 1, col + 1));
		if (checkEmpty(row - 1, col - 1) || checkIfEnemyOrAllyTowerNotFull(p, row - 1, col - 1))
			moves.add(new Move(row - 1, col - 1));
		if (checkEmpty(row, col + 1) || checkIfEnemyOrAllyTowerNotFull(p, row, col + 1))
			moves.add(new Move(row, col + 1));
		if (checkEmpty(row, col - 1) || checkIfEnemyOrAllyTowerNotFull(p, row, col - 1))
			moves.add(new Move(row, col - 1));
		
		return moves;
	}
	
	/**
	 * Moveset for Pawn
	 * @param p Pawn
	 * @param row row number
	 * @param col column number
	 * @return list of all possible movements
	 */
	public ArrayList<Move> pawnMove(Piece p, int row, int col) {
		ArrayList<Move> moves = new ArrayList<Move>();

		// tier 1,2,3 moves
		if (p.getColor() == 'b') {
			if (checkEmpty(row + 1, col) || checkIfEnemyOrAllyTowerNotFull(p, row + 1, col))
				moves.add(new Move(row + 1, col));
		} else if (p.getColor() == 'w') {
			if (checkEmpty(row - 1, col) || checkIfEnemyOrAllyTowerNotFull(p, row - 1, col))
				moves.add(new Move(row - 1, col));
		}
		// tier 2 moves
		if (p.getTier() == 2) {
			if (checkEmpty(row, col + 1) || checkIfEnemyOrAllyTowerNotFull(p, row, col + 1))
				moves.add(new Move(row, col + 1));
			if (checkEmpty(row, col - 1) || checkIfEnemyOrAllyTowerNotFull(p, row, col - 1))
				moves.add(new Move(row, col - 1));
		}
		// tier 3 moves
		else if (p.getTier() == 3) {
			if (checkEmpty(row, col + 1) || checkIfEnemyOrAllyTowerNotFull(p, row, col + 1))
				moves.add(new Move(row, col + 1));
			if (checkEmpty(row, col - 1) || checkIfEnemyOrAllyTowerNotFull(p, row, col - 1))
				moves.add(new Move(row, col - 1));

			if (p.getColor() == 'b') {
				if (checkEmpty(row + 1, col + 1)
						|| checkIfEnemyOrAllyTowerNotFull(p, row + 1, col + 1))
					moves.add(new Move(row + 1, col + 1));
				if (checkEmpty(row + 1, col - 1)
						|| checkIfEnemyOrAllyTowerNotFull(p, row + 1, col - 1))
					moves.add(new Move(row + 1, col - 1));
			} else if (p.getColor() == 'w') {
				if (checkEmpty(row - 1, col + 1)
						|| checkIfEnemyOrAllyTowerNotFull(p, row - 1, col + 1))
					moves.add(new Move(row + 1, col + 1));
				if (checkEmpty(row - 1, col - 1)
						|| checkIfEnemyOrAllyTowerNotFull(p, row - 1, col - 1))
					moves.add(new Move(row + 1, col - 1));
			}
		}
		return moves;
	}

	/**
	 * Moveset for Knight
	 * @param p Knight
	 * @param row row number
	 * @param col column number
	 * @return list of possible movements
	 */
	public static ArrayList<Move> knightMove(Piece p, int row, int col)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		if (checkEmpty(row + 2, col + 1) || checkIfEnemyOrAllyTowerNotFull(p, row + 2, col + 1))
			moves.add(new Move(row + 2, col + 1));
		if (checkEmpty(row - 2, col + 1) || checkIfEnemyOrAllyTowerNotFull(p, row - 2, col + 1))
			moves.add(new Move(row - 2, col + 1));
		if (checkEmpty(row + 2, col - 1) || checkIfEnemyOrAllyTowerNotFull(p, row + 2, col - 1))
			moves.add(new Move(row + 2, col - 1));
		if (checkEmpty(row - 2, col - 1) || checkIfEnemyOrAllyTowerNotFull(p, row - 2, col - 1))
			moves.add(new Move(row - 2, col - 1));
		if (checkEmpty(row + 1, col + 2) || checkIfEnemyOrAllyTowerNotFull(p, row + 1, col + 2))
			moves.add(new Move(row + 1, col + 2));
		if (checkEmpty(row + 1, col - 2) || checkIfEnemyOrAllyTowerNotFull(p, row + 1, col - 2))
			moves.add(new Move(row + 1, col - 2));
		if (checkEmpty(row - 1, col + 2) || checkIfEnemyOrAllyTowerNotFull(p, row - 1, col + 2))
			moves.add(new Move(row - 1, col + 2));
		if (checkEmpty(row - 1, col - 2) || checkIfEnemyOrAllyTowerNotFull(p, row - 1, col - 2))
			moves.add(new Move(row - 1, col - 2));
		
		return moves;
	}
	
	/**
	 * Moveset for lieutenant
	 * @param p Lieutenant
	 * @param row row number
	 * @param col column number
	 * @return list of possible movements
	 */
	public static ArrayList<Move> ltMove(Piece p, int row, int col)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		if (p.getTier() == 1)
		{
			//check squares along each diagonal and breaks if there is a piece in the way
			for (int i = 1; i < 7; i++) 
			{
				if (checkEmpty(row+i, col+i))
					moves.add(new Move(row+i,col+i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row+i, col+i))
				{
					moves.add(new Move(row+i,col+i));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 7; i++) 
			{
				if (checkEmpty(row-i, col+i))
					moves.add(new Move(row-i, col+i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row-i, col+i))
				{
					moves.add(new Move(row-i, col+i));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 7; i++) 
			{
				if (checkEmpty(row+i, col-i))
					moves.add(new Move(row+i, col-i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row+i, col-i))
				{
					moves.add(new Move(row+i, col-i));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 7; i++) 
			{
				if (checkEmpty(row-i, col-i))
					moves.add(new Move(row-i, col-i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row-i, col-i))
				{
					moves.add(new Move(row-i, col-i));
					break;
				}
				else
					break;
			}
		}
		//tiers 1,2,3
		if (checkEmpty(row+1, col) || checkIfEnemyOrAllyTowerNotFull(p, row+1, col))
			moves.add(new Move(row+1,col));
		if (checkEmpty(row-1, col) || checkIfEnemyOrAllyTowerNotFull(p, row-1, col))
			moves.add(new Move(row-1,col));
		if (checkEmpty(row, col+1) || checkIfEnemyOrAllyTowerNotFull(p, row, col+1))
			moves.add(new Move(row,col+1));
		if (checkEmpty(row, col-1) || checkIfEnemyOrAllyTowerNotFull(p, row, col-1))
			moves.add(new Move(row,col-1));
		return moves;
	}
	
	/**
	 * Moveset for Marshall
	 * @param p Marshall
	 * @param row row number
	 * @param col column number
	 * @return list of possible movements
	 */
	public static ArrayList<Move> marshalMove(Piece p, int row, int col)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		if (p.getTier() == 1)
		{
			//checks squares in a line in front/back/left/right of the piece
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row+i, col))
					moves.add(new Move(row+i,col));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row+i, col))
				{
					moves.add(new Move(row+i,col));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row-i, col))
					moves.add(new Move(row-i,col));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row-i, col))
				{
					moves.add(new Move(row-i,col));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row, col-i))
					moves.add(new Move(row,col-i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row, col-i))
				{
					moves.add(new Move(row,col-i));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row, col+i))
					moves.add(new Move(row,col+i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row, col+i))
				{
					moves.add(new Move(row,col+i));
					break;
				}
				else
					break;
			}
		}
		//tiers 1,2,3
		if (checkEmpty(row+1, col+1) || checkIfEnemyOrAllyTowerNotFull(p, row+1, col+1))
			moves.add(new Move(row+1,col+1));
		if (checkEmpty(row+1, col-1) || checkIfEnemyOrAllyTowerNotFull(p, row+1, col-1))
			moves.add(new Move(row+1,col-1));
		if (checkEmpty(row-1, col+1) || checkIfEnemyOrAllyTowerNotFull(p, row-1, col+1))
			moves.add(new Move(row-1,col+1));
		if (checkEmpty(row-1, col-1) || checkIfEnemyOrAllyTowerNotFull(p, row-1, col-1))
			moves.add(new Move(row-1,col-1));
		
		return moves;
	}
	
	/**
	 * Moveset for Major
	 * @param p Major
	 * @param row row number
	 * @param col column number
	 * @return list of possible movements
	 */
	public static ArrayList<Move> majorMove(Piece p, int row, int col)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		if (p.getTier() == 1)
		{
			if (checkEmpty(row+1, col) || checkIfEnemyOrAllyTowerNotFull(p, row+1, col))
				moves.add(new Move(row+1,col));
			if (checkEmpty(row-1, col) || checkIfEnemyOrAllyTowerNotFull(p, row-1, col))
				moves.add(new Move(row-1,col));
			if (checkEmpty(row, col+1) || checkIfEnemyOrAllyTowerNotFull(p, row, col+1))
				moves.add(new Move(row,col+1));
			if (checkEmpty(row, col-1) || checkIfEnemyOrAllyTowerNotFull(p, row, col-1))
				moves.add(new Move(row,col-1));
			
			if (p.getColor() == 'w')
			{
				if (checkEmpty(row-1, col+1) || checkIfEnemyOrAllyTowerNotFull(p, row-1, col+1))
					moves.add(new Move(row-1,col+1));
				if (checkEmpty(row-1, col-1) || checkIfEnemyOrAllyTowerNotFull(p, row-1, col-1))
					moves.add(new Move(row-1,col-1));
			}
			else if (p.getColor() == 'b')
			{
				if (checkEmpty(row+1, col+1) || checkIfEnemyOrAllyTowerNotFull(p, row+1, col+1))
					moves.add(new Move(row+1,col+1));
				if (checkEmpty(row+1, col-1) || checkIfEnemyOrAllyTowerNotFull(p, row+1, col-1))
					moves.add(new Move(row+1,col-1));
			}
		}
		//tier 2 moves like a chess bishop
		else if (p.getTier() == 2)
		{
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row+i, col+i))
					moves.add(new Move(row+i,col+i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row+i, col+i))
				{
					moves.add(new Move(row+i,col+i));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row-i, col+i))
					moves.add(new Move(row-i, col+i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row-i, col+i))
				{
					moves.add(new Move(row-i, col+i));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row+i, col-i))
					moves.add(new Move(row+i, col-i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row+i, col-i))
				{
					moves.add(new Move(row+i, col-i));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row-i, col-i))
					moves.add(new Move(row-i, col-i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row-i, col-i))
				{
					moves.add(new Move(row-i, col-i));
					break;
				}
				else
					break;
			}
		}
		//tier 3 moves like a chess rook
		else if (p.getTier() == 3)
		{
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row+i, col))
					moves.add(new Move(row+i,col));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row+i, col))
				{
					moves.add(new Move(row+i,col));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row-i, col))
					moves.add(new Move(row-i,col));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row-i, col))
				{
					moves.add(new Move(row-i,col));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row, col-i))
					moves.add(new Move(row,col-i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row, col-i))
				{
					moves.add(new Move(row,col-i));
					break;
				}
				else
					break;
			}
			for (int i = 1; i < 8; i++) 
			{
				if (checkEmpty(row, col+i))
					moves.add(new Move(row,col+i));
				else if (checkIfEnemyOrAllyTowerNotFull(p, row, col+i))
				{
					moves.add(new Move(row,col+i));
					break;
				}
				else
					break;
			}
		}
		return moves;
	}
	
	/**
	 * moveset for Archer
	 * @param p Archer
	 * @param row row number
	 * @param col column number
	 * @return list of possible movements
	 */
	public static ArrayList<Move> archerMove(Piece p, int row, int col)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		if (p.getColor() == 'w')
		{
			if (checkEmpty(row-1, col) || (checkIfEnemyOrAllyTowerNotFull(p,row-1,col) && p.getColor() == towerMatrix[row-1][col].getTop().getColor()))
				moves.add(new Move(row-1,col));
		}
		else if (p.getColor() == 'b')
		{
			if (checkEmpty(row+1, col) || (checkIfEnemyOrAllyTowerNotFull(p,row+1,col) && p.getColor() == towerMatrix[row+1][col].getTop().getColor()))
				moves.add(new Move(row+1,col));

		}
		
		if (checkEmpty(row, col+1) || (checkIfEnemyOrAllyTowerNotFull(p,row,col+1) && p.getColor() == towerMatrix[row][col+1].getTop().getColor()))
			moves.add(new Move(row,col+1));
		if (checkEmpty(row, col-1) || (checkIfEnemyOrAllyTowerNotFull(p,row,col-1) && p.getColor() == towerMatrix[row][col-1].getTop().getColor()))
			moves.add(new Move(row,col-1));
		
		return moves;
	}
	
	/**
	 * Gets all potential captures for Archer
	 * @param p Archer
	 * @param row row number
	 * @param col column number
	 * @return list of potential captures
	 */
	public static ArrayList<Move> archerCap(Piece p, int row, int col)
	{
		ArrayList<Move> caps = new ArrayList<Move>();
		
		if (p.getColor() == 'w')
		{
			if(p.getTier() == 1)
			{
				if(checkIfEnemyOrAllyTowerNotFull(p,row-3,col) && p.getColor() != towerMatrix[row-3][col].getTop().getColor())
					caps.add(new Move(row-3,col));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-2,col+1) && p.getColor() != towerMatrix[row-2][col+1].getTop().getColor())
					caps.add(new Move(row-2,col+1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-2,col-1) && p.getColor() != towerMatrix[row-2][col-1].getTop().getColor())
					caps.add(new Move(row-2,col-1));
			}
			if(p.getTier() == 2)
			{
				if(checkIfEnemyOrAllyTowerNotFull(p,row-3,col) && p.getColor() != towerMatrix[row-3][col].getTop().getColor())
					caps.add(new Move(row-3,col));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-2,col+1) && p.getColor() != towerMatrix[row-2][col+1].getTop().getColor())
					caps.add(new Move(row-2,col+1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-2,col-1) && p.getColor() != towerMatrix[row-2][col-1].getTop().getColor())
					caps.add(new Move(row-2,col-1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-3,col+1) && p.getColor() != towerMatrix[row-3][col+1].getTop().getColor())
					caps.add(new Move(row-3,col+1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-3,col-1) && p.getColor() != towerMatrix[row-3][col-1].getTop().getColor())
					caps.add(new Move(row-3,col-1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-4,col) && p.getColor() != towerMatrix[row-4][col].getTop().getColor())
					caps.add(new Move(row-4,col));
			}
			if(p.getTier() == 3)
			{
				if(checkIfEnemyOrAllyTowerNotFull(p,row-3,col+1) && p.getColor() != towerMatrix[row-3][col+1].getTop().getColor())
					caps.add(new Move(row-3,col+1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-3,col-1) && p.getColor() != towerMatrix[row-3][col-1].getTop().getColor())
					caps.add(new Move(row-3,col-1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-3,col+2) && p.getColor() != towerMatrix[row-3][col+2].getTop().getColor())
					caps.add(new Move(row-3,col+2));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-3,col-2) && p.getColor() != towerMatrix[row-3][col-2].getTop().getColor())
					caps.add(new Move(row-3,col-2));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-4,col) && p.getColor() != towerMatrix[row-4][col].getTop().getColor())
					caps.add(new Move(row-4,col));
				if(checkIfEnemyOrAllyTowerNotFull(p,row-5,col) && p.getColor() != towerMatrix[row-5][col].getTop().getColor())
					caps.add(new Move(row-5,col));
			}
		}
		if (p.getColor() == 'b')
		{
			if(p.getTier() == 1)
			{
				if(checkIfEnemyOrAllyTowerNotFull(p,row+3,col) && p.getColor() != towerMatrix[row+3][col].getTop().getColor())
					caps.add(new Move(row+3,col));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+2,col+1) && p.getColor() != towerMatrix[row+2][col+1].getTop().getColor())
					caps.add(new Move(row+2,col+1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+2,col-1) && p.getColor() != towerMatrix[row+2][col-1].getTop().getColor())
					caps.add(new Move(row+2,col-1));
			}
			if(p.getTier() == 2)
			{
				if(checkIfEnemyOrAllyTowerNotFull(p,row+3,col) && p.getColor() != towerMatrix[row+3][col].getTop().getColor())
					caps.add(new Move(row+3,col));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+2,col+1) && p.getColor() != towerMatrix[row+2][col+1].getTop().getColor())
					caps.add(new Move(row+2,col+1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+2,col-1) && p.getColor() != towerMatrix[row+2][col-1].getTop().getColor())
					caps.add(new Move(row+2,col-1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+3,col+1) && p.getColor() != towerMatrix[row+3][col+1].getTop().getColor())
					caps.add(new Move(row+3,col+1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+3,col-1) && p.getColor() != towerMatrix[row+3][col-1].getTop().getColor())
					caps.add(new Move(row+3,col-1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+4,col) && p.getColor() != towerMatrix[row+4][col].getTop().getColor())
					caps.add(new Move(row+4,col));
			}
			if(p.getTier() == 3)
			{
				if(checkIfEnemyOrAllyTowerNotFull(p,row+3,col+1) && p.getColor() != towerMatrix[row+3][col+1].getTop().getColor())
					caps.add(new Move(row+3,col+1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+3,col-1) && p.getColor() != towerMatrix[row+3][col-1].getTop().getColor())
					caps.add(new Move(row+3,col-1));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+3,col+2) && p.getColor() != towerMatrix[row+3][col+2].getTop().getColor())
					caps.add(new Move(row+3,col+2));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+3,col-2) && p.getColor() != towerMatrix[row+3][col-2].getTop().getColor())
					caps.add(new Move(row+3,col-2));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+4,col) && p.getColor() != towerMatrix[row+4][col].getTop().getColor())
					caps.add(new Move(row+4,col));
				if(checkIfEnemyOrAllyTowerNotFull(p,row+5,col) && p.getColor() != towerMatrix[row+5][col].getTop().getColor())
					caps.add(new Move(row+5,col));
			}
		}
		return caps;
	}
	
	/**
	 * Returns all spots an archer can attack, used to prevent commanders moving to capture
	 * @param p Archer
	 * @param row row number
	 * @param col column number
	 * @return list of potential spots an Archer can attack
	 */
	//Returns squares that archer can attack, not actual moves.  This method is used to prevent the commander from coming under attack
	public static ArrayList<Move> archerAttackSquares(Piece p, int row, int col)
	{
		ArrayList<Move> caps = new ArrayList<Move>();
		
		if (p.getColor() == 'w')
		{
			if(p.getTier() == 1)
			{
				caps.add(new Move(row-3,col));
				caps.add(new Move(row-2,col+1));
				caps.add(new Move(row-2,col-1));
			}
			if(p.getTier() == 2)
			{
				caps.add(new Move(row-3,col));
				caps.add(new Move(row-2,col+1));
				caps.add(new Move(row-2,col-1));
				caps.add(new Move(row-3,col+1));
				caps.add(new Move(row-3,col-1));
				caps.add(new Move(row-4,col));
			}
			if(p.getTier() == 3)
			{
				caps.add(new Move(row-3,col+1));
				caps.add(new Move(row-3,col-1));
				caps.add(new Move(row-3,col+2));
				caps.add(new Move(row-3,col-2));
				caps.add(new Move(row-4,col));
				caps.add(new Move(row-5,col));
			}
		}
		if (p.getColor() == 'b')
		{
			if(p.getTier() == 1)
			{
				caps.add(new Move(row+3,col));
				caps.add(new Move(row+2,col+1));
				caps.add(new Move(row+2,col-1));
			}
			if(p.getTier() == 2)
			{
				caps.add(new Move(row+3,col));
				caps.add(new Move(row+2,col+1));
				caps.add(new Move(row+2,col-1));
				caps.add(new Move(row+3,col+1));
				caps.add(new Move(row+3,col-1));
				caps.add(new Move(row+4,col));
			}
			if(p.getTier() == 3)
			{
				caps.add(new Move(row+3,col+1));
				caps.add(new Move(row+3,col-1));
				caps.add(new Move(row+3,col+2));
				caps.add(new Move(row+3,col-2));
				caps.add(new Move(row+4,col));
				caps.add(new Move(row+5,col));
			}
		}
		return caps;
	}
	
	/**
	 * Moveset for Musketeer
	 * @param p Musketeer
	 * @param row row number
	 * @param col column number
	 * @return list of possible movements
	 */
	public static ArrayList<Move> muskMove(Piece p, int row, int col)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		if (checkEmpty(row, col+1) || (checkIfEnemyOrAllyTowerNotFull(p,row,col+1) && p.getColor() == towerMatrix[row][col+1].getTop().getColor()))
			moves.add(new Move(row,col+1));
		if (checkEmpty(row, col-1) || (checkIfEnemyOrAllyTowerNotFull(p,row,col-1) && p.getColor() == towerMatrix[row][col-1].getTop().getColor()))
			moves.add(new Move(row,col-1));
		
		return moves;
	}
	
	/**
	 * list of potential captures a musketeer can make that turn
	 * @param p Musketeer
	 * @param row row number
	 * @param col column number
	 * @return list of potential captures
	 */
	public static ArrayList<Move> muskCap(Piece p, int row, int col)
	{
		ArrayList<Move> caps = new ArrayList<Move>();
		
		if (p.getColor() == 'w')
		{
			if (p.getTier() == 1)
			{
				for (int i = 1; i < 4; i++)
				{
					if (checkIfEnemyOrAllyTowerNotFull(p,row-i,col) && p.getColor() != towerMatrix[row-i][col].getTop().getColor())
						caps.add(new Move(row-i, col));
					if (!checkEmpty(row-i,col))
						break;
				}
			}
			if (p.getTier() == 2)
			{
				for (int i = 1; i < 5; i++)
				{
					if (checkIfEnemyOrAllyTowerNotFull(p,row-i,col) && p.getColor() != towerMatrix[row-i][col].getTop().getColor())
						caps.add(new Move(row-i, col));
					if (!checkEmpty(row-i,col))
						break;
				}
			}
			if (p.getTier() == 3)
			{
				for (int i = 1; i < 6; i++)
				{
					if (checkIfEnemyOrAllyTowerNotFull(p,row-i,col) && p.getColor() != towerMatrix[row-i][col].getTop().getColor())
						caps.add(new Move(row-i, col));
					if (!checkEmpty(row-i,col))
						break;
				}
			}
		}
		if (p.getColor() == 'b')
		{
			if (p.getTier() == 1)
			{
				for (int i = 1; i < 4; i++)
				{
					if (checkIfEnemyOrAllyTowerNotFull(p,row+i,col) && p.getColor() != towerMatrix[row+i][col].getTop().getColor())
						caps.add(new Move(row+i, col));
					if (!checkEmpty(row+i,col))
						break;
				}
			}
			if (p.getTier() == 2)
			{
				for (int i = 1; i < 5; i++)
				{
					if (checkIfEnemyOrAllyTowerNotFull(p,row+i,col) && p.getColor() != towerMatrix[row+i][col].getTop().getColor())
						caps.add(new Move(row+i, col));
					if (!checkEmpty(row+i,col))
						break;
				}
			}
			if (p.getTier() == 3)
			{
				for (int i = 1; i < 6; i++)
				{
					if (checkIfEnemyOrAllyTowerNotFull(p,row+i,col) && p.getColor() != towerMatrix[row+i][col].getTop().getColor())
						caps.add(new Move(row+i, col));
					else if (!checkEmpty(row+i,col))
						break;
				}
			}
		}
		return caps;
	}
	
	/**
	 * List of all spots a musketeer can attack.
	 * @param p Musketeer 
	 * @param row row number
	 * @param col column number
	 * @return list of all possible attack spots
	 */
	//Returns squares that musket can attack, not actual moves.  This method is used preventing commander from coming under attack
	public static ArrayList<Move> muskAttackSquares(Piece p, int row, int col)
	{
		ArrayList<Move> caps = new ArrayList<Move>();
		
		if (p.getColor() == 'w')
		{
			if (p.getTier() == 1)
			{
				for (int i = 1; i < 4; i++)
				{
					caps.add(new Move(row-i, col));
					if (!checkEmpty(row-i,col))
						break;
				}
			}
			if (p.getTier() == 2)
			{
				for (int i = 1; i < 5; i++)
				{
					caps.add(new Move(row-i, col));
					if (!checkEmpty(row-i,col))
						break;
				}
			}
			if (p.getTier() == 3)
			{
				for (int i = 1; i < 6; i++)
				{
					caps.add(new Move(row-i, col));
					if (!checkEmpty(row-i,col))
						break;
				}
			}
		}
		if (p.getColor() == 'b')
		{
			if (p.getTier() == 1)
			{
				for (int i = 1; i < 4; i++)
				{
					caps.add(new Move(row+i, col));
					if (!checkEmpty(row+i,col))
						break;
				}
			}
			if (p.getTier() == 2)
			{
				for (int i = 1; i < 5; i++)
				{
					caps.add(new Move(row+i, col));
					if (!checkEmpty(row+i,col))
						break;
				}
			}
			if (p.getTier() == 3)
			{
				for (int i = 1; i < 6; i++)
				{
					caps.add(new Move(row+i, col));
					if (!checkEmpty(row+i,col))
						break;
				}
			}
		}
		return caps;
	}
	
	/**
	 * Moveset for Spy
	 * @param p Spy
	 * @param row row number
	 * @param col column number
	 * @return list of possible movements
	 */
	public static ArrayList<Move> spyMove(Piece p, int row, int col)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		
		if (p.getColor() == 'w')
		{
			if (checkIfEnemyOrAllyTowerNotFull(p,row-2,col+1) || checkEmpty(row-2,col+1))	
				moves.add(new Move(row-2, col+1));
			if (checkIfEnemyOrAllyTowerNotFull(p,row-2,col-1) || checkEmpty(row-2,col-1))
				moves.add(new Move(row-2, col-1));
			
			if (p.getTier() == 2 || p.getTier() == 3)
			{
				if (checkIfEnemyOrAllyTowerNotFull(p,row-3,col+1) || checkEmpty(row-3,col+1))
					moves.add(new Move(row-3, col+1));
				if (checkIfEnemyOrAllyTowerNotFull(p,row-3,col-1) || checkEmpty(row-3,col-1))
					moves.add(new Move(row-3, col-1));
			}
		}
		else if (p.getColor() == 'b')
		{	
			if (checkIfEnemyOrAllyTowerNotFull(p,row+2,col+1) || checkEmpty(row+2,col+1))
				moves.add(new Move(row+2, col+1));	
			if (checkIfEnemyOrAllyTowerNotFull(p,row+2,col-1) || checkEmpty(row+2,col-1))
				moves.add(new Move(row+2, col-1));
			
			if (p.getTier() == 2 || p.getTier() == 3)
			{
				if (checkIfEnemyOrAllyTowerNotFull(p,row+3,col+1) || checkEmpty(row+3,col+1))
					moves.add(new Move(row+3, col+1));
				if (checkIfEnemyOrAllyTowerNotFull(p,row+3,col-1) || checkEmpty(row+3,col-1))
					moves.add(new Move(row+3, col-1));
			}
		}
		return moves;
	}
}

