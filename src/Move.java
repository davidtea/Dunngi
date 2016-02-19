public class Move
{
	private int upDown, leftRight;
	
	/**
	 * Default constructor, sets both to 0
	 */
	public Move()
	{
		upDown = 0;
		leftRight = 0;
	}
	
	/**
	 * Checks if both Moves are equal
	 * @param m other Move
	 * @return true/false
	 */
	public boolean equals(Move m)
	{
		if (upDown == m.getUpDown() && leftRight == m.leftRight)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Changes upDown to x
	 * @param x new upDown
	 */
	public void setUpDown(int x)
	{
		upDown = x;
	}
	
	/**
	 * Changes leftRight to x
	 * @param x new leftRight
	 */
	public void setLeftRight(int x)
	{
		leftRight = x;
	}
	
	/**
	 * Returns value of upDown
	 * @return upDown
	 */
	public int getUpDown()
	{
		return upDown;
	}
	
	/**
	 * returns value of leftRight
	 * @return leftRight
	 */
	public int getLeftRight()
	{
		return leftRight;
	}
	
	/**
	 * Two argument constructor
	 * @param i row
	 * @param j column
	 */
	public Move(int i, int j)
	{
		upDown = i;
		leftRight = j;
	}
	
	/**
	 * string representation of move
	 * @return String of move
	 */
	public String toString()
	{
		String result = "(" + upDown + " " + leftRight +  ")";
		return result;
	}

}
