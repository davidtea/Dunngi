

import java.util.ArrayList;
public class Piece 
{
	private int tier;
	private char id;
	private char color;
	private int value;
	
	/**
	 * Triple argument constructor for Piece
	 * @param c color
	 * @param type piece id
	 * @param v point value
	 */
	public Piece(char c, char type, int v) 
	{
		color = c;
		id = type;
		value = v;
	}
	
	/**
	 * returns color
	 * @return color
	 */
	public char getColor()
	{
		return color;
	}
	
	/**
	 * returns point value
	 * @return value
	 */
	public int getValue()
	{
		return value;
	}
	
	/**
	 * changes color between black and white
	 */
	public void changeColor()
	{
		if (color == 'b')
			color = 'w';
		else 
			color = 'b';
	}
	
	/**
	 * returns piece id, changes depending on color as well
	 * @return id
	 */
	public char getID()
	{
		if (color == 'b')
			id = Character.toUpperCase(id);
		else
			id = Character.toLowerCase(id);
		return id;
	}
	
	/**
	 * return tower height of this piece
	 * @return tier
	 */
	public int getTier() 
	{
		return tier;
	}
	
	/**
	 * changes tier of piece
	 * @param x new tier
	 */
	public void changeTier(int x)
	{
		tier = x;
	}
}
