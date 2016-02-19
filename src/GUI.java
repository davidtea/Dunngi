import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;



public class GUI extends JApplet implements MouseListener, MouseMotionListener
{
	static Board b = new Board();
	static Player black = new Player('b');
	static Player white = new Player('w');
	static char turn;
	
	private JPanel[][] squares = new JPanel[8][8];
	private JPanel[][] blackHandSquares = new JPanel[2][10];
	private JPanel[][] whiteHandSquares = new JPanel[2][10];
	private JLabel[][] blackPieces = new JLabel[2][10];
	private JLabel[][] whitePieces = new JLabel[2][10];
	private JLabel[][][] piece = new JLabel[8][8][3];
	private JLabel movingPiece;
	private JPanel board;
	private JPanel blackHand;
	private JPanel whiteHand;
	private JPanel scoreBoard;
	private JLabel message, whiteScore, blackScore;
	private JLayeredPane pane;
	private Move from;
	private int xAdjust, yAdjust, droppingPiece;
	private char droppingPlayer;

	private Image[][] pieceImages = new Image[2][9];
	private final int ARCH = 0, COMM = 1, KNIG = 2, LT = 3, MAJOR = 4, MARSH = 5, PAWN = 6, RIFLE = 7, SPY = 8;
	private final int B = 0, W = 1;	

	/**
	 * initializes the applet. Creates the images, the boards, hands, score board, and puts them into the applet frame.
	 */
	public void init(){
		
		Scanner in = new Scanner(System.in);
		
		b.printBoard();
		turn = 'w';
		
		/*while (black.getSize() > 0 || white.getSize() > 0)
		{
			if (turn == 'w')
			{
				b.playerInitializeDrop(white);
				turn = 'b';
			}
			else
			{
				b.playerInitializeDrop(black);
				turn = 'w';
			}
			b.printBoard();
		}
		b.dropPhaseOver = true;*/
		
		createImages();
		initialize();
		initializeBlackHand();
		initializeWhiteHand();
		initializeScoreBoard();
		setImages();
		setSize(1300, 1000);
		add(pane);
		setVisible(true);
	}
	
	/**
	 * Initializes the board. Creates the frame, mouse listeners, borders, and colors for the board.
	 */
	public void initialize()
	{
		pane = new JLayeredPane();
		
		getContentPane().add(pane);
		pane.setPreferredSize(new Dimension(1300,1000));
		pane.addMouseListener(this);
		pane.addMouseMotionListener(this);
		
		board = new JPanel(new GridLayout(0,8)) {

			/**
			 * Override the preferred size to return the largest it can, in a
			 * square shape. Must (must, must) be added to a GridBagLayout as
			 * the only component (it uses the parent as a guide to size) with
			 * no GridBagConstaint (so it is centered).
			 */
			@Override
			public final Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				Dimension prefSize = null;
				Component c = getParent();
				if (c == null) {
					prefSize = new Dimension((int) d.getWidth(),
							(int) d.getHeight());
				} else if (c != null && c.getWidth() > d.getWidth()
						&& c.getHeight() > d.getHeight()) {
					prefSize = c.getSize();
				} else {
					prefSize = d;
				}
				int w = (int) prefSize.getWidth();
				int h = (int) prefSize.getHeight();
				// the smaller of the two sizes
				int s = (w > h ? h : w);
				return new Dimension(s, s);
			}
        };
        
        board.setBorder(new CompoundBorder(new EmptyBorder(8,8,8,8), new LineBorder(Color.BLACK)));
        Color wood = new Color(210,165,70);
        board.setBackground(Color.WHITE);
        JPanel boardConstrain = new JPanel(new GridBagLayout());	
        boardConstrain.setBackground(Color.WHITE);
        boardConstrain.add(board);
        boardConstrain.setBounds(100, 145, 700, 700);
        pane.add(boardConstrain, JLayeredPane.DEFAULT_LAYER);
           
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares[i].length; j++) {
                JPanel pan = new JPanel();
                pan.setLayout(new GridLayout(0,1));
                pan.setBackground(wood);
                pan.setBorder(new LineBorder(Color.BLACK));
                pan.setPreferredSize(new Dimension(70,70));
                squares[j][i] = pan;
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                        board.add(squares[i][j]);
            }
        }
    }
	
	/**
	 * Creates the panel that displays the player's scores. Will also display current player's turn in this.
	 */
	public void initializeScoreBoard()
	{
		scoreBoard = new JPanel(new GridLayout(0,1));
		scoreBoard.setPreferredSize(new Dimension(350,400));
		scoreBoard.setBounds(950, 75, 350, 400);
		scoreBoard.setBackground(Color.WHITE);
		scoreBoard.setBorder(new LineBorder(Color.BLACK));
		pane.add(scoreBoard, JLayeredPane.DEFAULT_LAYER);
		
		
		message = new JLabel("		*******WELCOME TO GUNGI********");
		scoreBoard.add(message);
		whiteScore = new JLabel("	White has: "+ white.getScore()+" points");
		blackScore = new JLabel("	Black has: "+ black.getScore()+" points");
		scoreBoard.add(whiteScore);
		scoreBoard.add(blackScore);
	}
	
	/**
	 * Creates the frame to hold the black player's pieces
	 */
	public void initializeBlackHand()
	{
		blackHand = new JPanel(new GridLayout(0,10));
		blackHand.setPreferredSize(new Dimension(900,140));
		blackHand.setBorder(new CompoundBorder(new EmptyBorder(8,8,8,8), new LineBorder(Color.BLACK)));
		JPanel handConstrain = new JPanel(new GridBagLayout());
		handConstrain.setBackground(Color.WHITE);
		handConstrain.add(blackHand);
		handConstrain.setBounds(20, 0, 900, 140);
		pane.add(handConstrain, JLayeredPane.DEFAULT_LAYER);
		
		for (int i = 0; i < 20; i++)
		{
			JPanel pan = new JPanel();
			pan.setLayout(new GridLayout(0,1));
			pan.setBackground(Color.WHITE);
			pan.setPreferredSize(new Dimension(70,70));
			blackHandSquares[i/10][i-(i/10)*10] = pan;
		}
		
		for (int i = 0; i < 20; i++)
			blackHand.add(blackHandSquares[i/10][i-(i/10)*10]);
	}
	
	/**
	 * creates the frame to hold the white player's pieces
	 */
	public void initializeWhiteHand()
	{
		whiteHand = new JPanel(new GridLayout(0,10));
		whiteHand.setPreferredSize(new Dimension(900,140));
		whiteHand.setBorder(new CompoundBorder(new EmptyBorder(8,8,8,8), new LineBorder(Color.BLACK)));
		JPanel handConstrain = new JPanel(new GridBagLayout());
		handConstrain.setBackground(Color.WHITE);
		handConstrain.add(whiteHand);
		handConstrain.setBounds(20, 850, 900, 140);
		pane.add(handConstrain, JLayeredPane.DEFAULT_LAYER);
		
		for (int i = 0; i < 20; i++)
		{
			JPanel pan = new JPanel();
			pan.setLayout(new GridLayout(0,1));
			pan.setBackground(Color.WHITE);
			pan.setPreferredSize(new Dimension(70,70));
			whiteHandSquares[i/10][i-(i/10)*10] = pan;
		}
		
		for (int i = 0; i < 20; i++)
			whiteHand.add(whiteHandSquares[i/10][i-(i/10)*10]);
	}

	/**
	 * Highlights all the possible spots a piece can drop in yellow
	 * @param p Piece to be dropped
	 */
	public void highlightDrops(Player p)
	{
		ArrayList<Move> drops =  b.getDrops(p);
		
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				for(Move m: drops)
					if (i == m.getUpDown() && j == m.getLeftRight())
						squares[i][j].setBorder(BorderFactory.createMatteBorder(7,7,7,7,Color.YELLOW));
	}
	
	/**
	 * Highlights all the possible spots a piece can move in blue
	 * @param r row number of piece
	 * @param c column number of piece
	 */
	public void highlightMoves(int r, int c)
	{
		ArrayList<Move> moves =  b.getMoves(b.getTower(r,c));
		ArrayList<Move> rangedAttacks = b.getRangedAttacks( b.getTower(r,c));
		
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
			{
				for(Move m: moves)
				{
					if (i == m.getUpDown() && j == m.getLeftRight())
					{
						squares[i][j].setBorder(BorderFactory.createMatteBorder(7,7,7,7,Color.RED));
					}
					
				}
				for(Move m: rangedAttacks)
				{
					if (i == m.getUpDown() && j == m.getLeftRight())
					{
						System.out.println();
						squares[i][j].setBorder(BorderFactory.createMatteBorder(7,7,7,7,Color.BLUE));
					}
				}
				if(b.checkForReclaim(b.getTower(r,c)))
				{
					squares[r][c].setBorder(BorderFactory.createMatteBorder(7,7,7,7,Color.GREEN));
				}
			}
	}
	
	/**
	 * Will indicate that the selected piece can perform an immobile strike. Will highlight its square red.
	 * @param r row number
	 * @param c column number
	 */
	public void highlightImmobileStrike(int r, int c)
	{
		if(b.checkForImmobileStrike(b.getTower(r,c), turn))
		{
			squares[r][c].setBorder(BorderFactory.createMatteBorder(7,7,7,7,Color.RED));
		}
	}
	
	/**
	 * Displays all the pieces on the board and in the hands. Will also update scores and indicate whose turn it is since this function will be called every turn.
	 */
	public void setImages()
	{
		for (int i = 0; i <  white.getSize(); i++)
		{
			whiteHandSquares[i/10][i-(i/10)*10].removeAll();
			switch( white.getPiece(i).getID())
			{
				case 'a':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][ARCH]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
				case 'c':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][COMM]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
				case 'j':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][MAJOR]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
				case 'k':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][KNIG]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
				case 'l':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][LT]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
				case 'm':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][MARSH]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
				case 'p':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][PAWN]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
				case 's':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][SPY]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
				case 'u':
					whitePieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[W][RIFLE]));
					whiteHandSquares[i/10][i-(i/10)*10].add(whitePieces[i/10][i-(i/10)*10]);
					break;
			}
			whiteHandSquares[i/10][i-(i/10)*10].revalidate();
		}
		
		//trims the end of white hand so unwanted pieces aren't displayed
		for (int i = 19; i >=  white.getSize(); i--)
		{
			whitePieces[i/10][i-(i/10)*10] = null;
			whiteHandSquares[i/10][i-(i/10)*10].removeAll();
			whiteHandSquares[i/10][i-(i/10)*10].revalidate();
		}
	
		for (int i = 0; i <  black.getSize(); i++)
		{
			blackHandSquares[i/10][i-(i/10)*10].removeAll();
				switch( black.getPiece(i).getID())
				{
				case 'A':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][ARCH]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
				case 'C':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][COMM]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
				case 'J':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][MAJOR]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
				case 'K':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][KNIG]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
				case 'L':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][LT]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
				case 'M':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][MARSH]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
				case 'P':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][PAWN]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
				case 'S':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][SPY]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
				case 'U':
					blackPieces[i/10][i-(i/10)*10] = new JLabel(new ImageIcon(pieceImages[B][RIFLE]));
					blackHandSquares[i/10][i-(i/10)*10].add(blackPieces[i/10][i-(i/10)*10]);
					break;
			}	
			blackHandSquares[i/10][i-(i/10)*10].revalidate();
		}
		
		//trims the end of black hand so unwanted pieces aren't displayed
		for (int i = 19; i >=  black.getSize(); i--)
		{
			blackPieces[i/10][i-(i/10)*10] = null;
			blackHandSquares[i/10][i-(i/10)*10].removeAll();
			blackHandSquares[i/10][i-(i/10)*10].revalidate();
		}
		
		for (int i = 0; i < 8; i ++)
			for (int j = 0; j < 8; j ++)
			{
				squares[i][j].removeAll();
				squares[i][j].setBorder(new LineBorder(Color.BLACK));
				if( b.getTower(i,j).getTop() != null)
				{
					for(int k =  b.getTower(i,j).getHeight()-1; k >= 0; k--)
					{
						switch( b.getTower(i,j).getPiece(k).getID())
						{
							case 'a':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][ARCH]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'c':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][COMM]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'j':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][MAJOR]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'k':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][KNIG]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'l':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][LT]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'm':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][MARSH]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'p':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][PAWN]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 's':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][SPY]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'u':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[W][RIFLE]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'A':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][ARCH]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'C':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][COMM]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'J':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][MAJOR]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'K':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][KNIG]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'L':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][LT]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'M':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][MARSH]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'P':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][PAWN]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'S':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][SPY]));
								squares[i][j].add(piece[i][j][k]);
								break;
							case 'U':
								piece[i][j][k] = new JLabel(new ImageIcon(pieceImages[B][RIFLE]));
								squares[i][j].add(piece[i][j][k]);
								break;
						}
					}
				}
				else
				{
					piece[i][j][0] = null;
					piece[i][j][1] = null;
					piece[i][j][2] = null;
				}
				
				squares[i][j].revalidate();
			}
		
		if( turn == 'b')
			message.setText("                PLAYER BLACKS TURN");
		else if( turn == 'w') 
			message.setText("                PLAYER WHITES TURN");
		if( white.getScore() >= 100)
		{
			message.setText("	             PLAYER WHITE WINS!!!");
			 gameOver();
		}
		else if( black.getScore() >= 100)
		{
			message.setText("	             PLAYER BLACK WINS!!!");
			 gameOver();
		}
		
		whiteScore.setText("          WHITE SCORE: "+ white.getScore());
		blackScore.setText("          BLACK SCORE: "+ black.getScore());
	}
	
	/**
	 * Mouse listeners for the game. Uses click and drag to make moves. When clicked, will show all possible moves that piece can make or drops if it is in a hand. Dragging a piece
	 * to a possible movement spot does that move.
	 */
	@Override
	public void mouseDragged(MouseEvent e) 
	{
		if (movingPiece == null) return;
		movingPiece.setLocation(e.getX() + xAdjust, e.getY() + yAdjust);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		movingPiece = null;
		Component c; 
		
		//board pieces
		if (e.getY() >= 145 && e.getY() <= 845)
		{
			if ( b.dropPhaseOver == true)
			{
				c = board.getComponentAt(e.getX()-100, e.getY()-145);
				xAdjust = c.getX() - e.getX() + 100;
				yAdjust = c.getY() - e.getY() + 145;
				
				for (int i = 0; i<8; i++){
					for (int j = 0; j<8; j++){
						if (squares[i][j] == c){
							if (! b.checkEmpty(i,j))
							{
								if(b.getTower(i,j).getTop().getColor() ==  turn)
								{
									movingPiece = piece[i][j][ b.getTower(i,j).getHeight()-1];
									movingPiece.setLocation(c.getX()+100, c.getY()+145);
									pane.add(movingPiece, JLayeredPane.DRAG_LAYER);
									from = new Move(i,j);
									highlightMoves(i,j);
								}
								//attacking on the tower (immobile strike)
								else if (b.checkForImmobileStrike(b.getTower(i,j), turn))
								{
									movingPiece = piece[i][j][ b.getTower(i,j).getHeight()-2];
									movingPiece.setLocation(c.getX()+100, c.getY()+145);
									pane.add(movingPiece, JLayeredPane.DRAG_LAYER);
									from = new Move(i,j);
									highlightImmobileStrike(i,j);
								}
							}
						}
					}
				}
			}
		}
		
		//black hand pieces
		else if (e.getY() >= 0 && e.getY() <= 140 &&  turn == 'b')
		{
			c = blackHand.getComponentAt(e.getX()-20, e.getY());
			xAdjust = c.getX() - e.getX() + 20;
			yAdjust = c.getY() - e.getY();
			
			for (int i = 0; i< black.getSize(); i++){
				if (blackHandSquares[i/10][i-(i/10)*10] == c)
				{
					movingPiece = blackPieces[i/10][i-(i/10)*10];
					movingPiece.setLocation(c.getX()+20, c.getY());
					pane.add(movingPiece, JLayeredPane.DRAG_LAYER);
					highlightDrops( black);
					from = null;
					droppingPiece = i;
					droppingPlayer = 'b';
				}
			}
		}
		
		//white hand pieces
		else if (e.getY() >= 850 && e.getY() <= 990 &&  turn == 'w')
		{
			c = whiteHand.getComponentAt(e.getX()-20, e.getY()-850);
			xAdjust = c.getX() - e.getX() + 20;
			yAdjust = c.getY() - e.getY() + 850;
			
			for (int i = 0; i< white.getSize(); i++){
				if (whiteHandSquares[i/10][i-(i/10)*10] == c)
				{
					movingPiece = whitePieces[i/10][i-(i/10)*10];
					movingPiece.setLocation(c.getX()+20, c.getY() + 850);
					pane.add(movingPiece, JLayeredPane.DRAG_LAYER);
					highlightDrops( white);
					from = null;
					droppingPiece = i;
					droppingPlayer = 'w';
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		if (movingPiece == null) return;
		movingPiece.setVisible(false);
		Component c = board.getComponentAt(e.getX()-100, e.getY()-145);
		
		if (c instanceof JPanel){
			for (int i = 0; i<8; i++){
				for (int j = 0; j<8; j++){
					if(squares[i][j] == c){
						if (from != null)
						{
							if (from.equals(new Move(i,j)) &&  b.checkForImmobileStrike(b.getTower(i,j),turn))
								 immobileStrike(from);
							else if (b.checkForReclaim(b.accessTower(from)) && from.equals(new Move(i,j)))
							{
								System.out.println("TEST");
								reclaim(from);
							}
							else if (b.accessTower(from).getTop().getColor() == turn)
								 makeMove(from, new Move(i,j));
						}
						else if (droppingPlayer == 'b')
							 dropPiece(black, droppingPiece, new Move(i,j));
						else if (droppingPlayer == 'w')
							 dropPiece(white, droppingPiece, new Move(i,j));
					}
				}
			}
		}
		 b.printBoard();
		setImages();
	}
	
	/**
	 * Drops the selected piece at the selected location on the board. Possible spots changes depending on if during initial dropping phase or not.
	 * @param p current Player
	 * @param piece piece to drop
	 * @param to position to drop to
	 */
	public static void dropPiece(Player p, int piece, Move to)
	{
		ArrayList<Move> drops = b.getDrops(p);
		for(Move i: drops)
			if (to.equals(i))
			{
				b.accessTower(to).add(p.removePiece(piece));
				switchTurns();
			}
		scoreKeep();
		
		//initial drop phase is over
		if (b.dropPhaseOver == false && black.getSize() == 0 && white.getSize() == 0)
			b.dropPhaseOver = true;
	}
	
	/**
	 * Reclaim special effect. Takes your piece and adds it back to your hand and removes 10 points from the other player.
	 * @param from position of piece
	 */
	public static void reclaim(Move from)
	{
		Piece p = b.accessTower(from).getTop();
		if (p.getColor() == 'w')
		{
			white.addToHand(b.accessTower(from).remove(b.accessTower(from).getHeight()-1));
			black.reclaimEffect(10);
		}
		else
		{
			black.addToHand(b.accessTower(from).remove(b.accessTower(from).getHeight()-1));
			white.reclaimEffect(10);
		}
	}
	/**
	 * Function to do an immobile strike.
	 * @param from piece position
	 */
	//Attack enemy pieces on the same tower.  Only works for attacking above right now
	public static void immobileStrike(Move from)
	{
		Piece attackingPiece = b.accessTower(from).getPiece(b.accessTower(from).getHeight()-2);
		
		if (attackingPiece.getColor() == 'w')
			white.captureAbove(b.accessTower(from), b.accessTower(from).getHeight()-2);
		else if (attackingPiece.getColor() == 'b')
			black.captureAbove(b.accessTower(from), b.accessTower(from).getHeight()-2);
		
		switchTurns();
		scoreKeep();
	}
	
	/**
	 * Function that handles piece movement. 
	 * @param from piece to move
	 * @param to position to move to
	 */
	public static void makeMove(Move from, Move to)
	{
		Piece movingPiece = b.accessTower(from).getTop();
		ArrayList<Move> moves = b.getMoves(b.accessTower(from));	
		ArrayList<Move> attacks = b.getRangedAttacks(b.accessTower(from));
		
		//non-ranged pieces
		for (Move i: moves)
		{
			if (to.equals(i))
			{
				if (b.checkEmpty(to.getUpDown(), to.getLeftRight()))
					b.accessTower(to).add(b.accessTower(from).removeTop());
				//check if the moving piece is the same color as the piece at target location
				else if (movingPiece.getColor() == b.accessTower(to).getTop().getColor())
					b.accessTower(to).add(b.accessTower(from).removeTop());
				else
					if (movingPiece.getColor() == 'w')
						white.capturePiece(b.accessTower(from), b.accessTower(to));
					else
						black.capturePiece(b.accessTower(from), b.accessTower(to));
				
				switchTurns();
			}
		}
		
		//ranged pieces
		for (Move i: attacks)
		{
			if(to.equals(i))
			{
				if (movingPiece.getColor() == 'w')
					white.rangedCapture(b.accessTower(from), b.accessTower(to));
				else
					black.rangedCapture(b.accessTower(from), b.accessTower(to));
				
				switchTurns();
			}
		}
		scoreKeep();
	}

	/**
	 * Grabs images from Imgur.com to use so that program doesn't require copies of images to run.
	 */
	private final void createImages() {
		try	
		{
			URL blackArcher = new URL("http://i.imgur.com/Ryo1s5s.png");
			BufferedImage bArch = ImageIO.read(blackArcher);
			pieceImages[B][ARCH] = bArch;

			URL blackComm = new URL("http://i.imgur.com/kMFJn37.png");
			BufferedImage bComm = ImageIO.read(blackComm);
			pieceImages[B][COMM] = bComm;

			URL blackKnight = new URL("http://i.imgur.com/uuL2PYE.png");
			BufferedImage bKnig = ImageIO.read(blackKnight);
			pieceImages[B][KNIG] = bKnig;

			URL blackLt = new URL("http://i.imgur.com/Frfn7NH.png");
			BufferedImage bLt = ImageIO.read(blackLt);
			pieceImages[B][LT] = bLt;

			URL blackMajor = new URL("http://i.imgur.com/kpnjLOq.png");
			BufferedImage bMajor = ImageIO.read(blackMajor);
			pieceImages[B][MAJOR] = bMajor;

			URL blackMarshall = new URL("http://i.imgur.com/p4aiW0R.png");
			BufferedImage bMarsh = ImageIO.read(blackMarshall);
			pieceImages[B][MARSH] = bMarsh;

			URL blackPawn = new URL("http://i.imgur.com/gQmlLvd.png");
			BufferedImage bPawn = ImageIO.read(blackPawn);
			pieceImages[B][PAWN] = bPawn;

			URL blackRifle = new URL("http://i.imgur.com/XYzJuev.png");
			BufferedImage bRifle = ImageIO.read(blackRifle);
			pieceImages[B][RIFLE] = bRifle;

			URL blackSpy = new URL("http://i.imgur.com/zFcyUC9.png");
			BufferedImage bSpy = ImageIO.read(blackSpy);
			pieceImages[B][SPY] = bSpy;

			URL whiteArcher = new URL("http://i.imgur.com/q8CWPLG.png");
			BufferedImage wArch = ImageIO.read(whiteArcher);
			pieceImages[W][ARCH] = wArch;

			URL whiteComm = new URL("http://i.imgur.com/0uM3bEC.png");
			BufferedImage wComm = ImageIO.read(whiteComm);
			pieceImages[W][COMM] = wComm;

			URL whiteKnight = new URL("http://i.imgur.com/BqEpqHs.png");
			BufferedImage wKnig = ImageIO.read(whiteKnight);
			pieceImages[W][KNIG] = wKnig;

			URL whiteLt = new URL("http://i.imgur.com/5yuSBa5.png");
			BufferedImage wLt = ImageIO.read(whiteLt);
			pieceImages[W][LT] = wLt;

			URL whiteMajor = new URL("http://i.imgur.com/HhTDpRC.png");
			BufferedImage wMajor = ImageIO.read(whiteMajor);
			pieceImages[W][MAJOR] = wMajor;

			URL whiteMarshall = new URL("http://i.imgur.com/QgtCXog.png");
			BufferedImage wMarsh = ImageIO.read(whiteMarshall);
			pieceImages[W][MARSH] = wMarsh;

			URL whitePawn = new URL("http://i.imgur.com/tGyaU2m.png");
			BufferedImage wPawn = ImageIO.read(whitePawn);
			pieceImages[W][PAWN] = wPawn;

			URL whiteRifle = new URL("http://i.imgur.com/Pw0SwWK.png");
			BufferedImage wRifle = ImageIO.read(whiteRifle);
			pieceImages[W][RIFLE] = wRifle;

			URL whiteSpy = new URL("http://i.imgur.com/Z0U6cDD.png");
			BufferedImage wSpy = ImageIO.read(whiteSpy);
			pieceImages[W][SPY] = wSpy;
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** 
	 * displays the points each player has in console
	 */
	public static void scoreKeep()
	{
		System.out.println("	White has: " + white.getScore());
		System.out.println("	Black has: " + black.getScore());
	}
	
	/**
	 * switches turn between black and white after each successful move
	 */
	public static void switchTurns()
	{
		if (turn == 'w')
			turn = 'b';
		else
			turn = 'w';
	}
	
	/**
	 * Makes it so that no player can make any more moves when game is over.
	 */
	public static void  gameOver()
	{
		turn = 'a';
	}
}
