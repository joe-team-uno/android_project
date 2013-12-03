package team.uno.marblegame;

import java.util.Collections;
import java.util.Arrays;

import team.uno.marblegame.R.color;

import android.graphics.Color;
import android.graphics.Paint;

//Got MazeGenerator From http://rosettacode.org/wiki/Maze_generation#JavaScript
//Modified By: Jed Oesterreich
/*
 * recursive backtracking algorithm
 * shamelessly borrowed from the ruby at
 * http://weblog.jamisbuck.org/2010/12/27/maze-generation-recursive-backtracking
 */
public class MazeGenerator {
	private final int x;
	private final int y;
	private final int[][] m_Maze;
	private Paint m_Wall;
	
	public Paint Wall()
	{
		return m_Wall;
	}
 
	public int[][] Maze()
	{
		return m_Maze;
	}
	
	public MazeGenerator(int x, int y) {
		this.x = x;
		this.y = y;
		m_Maze = new int[this.x][this.y];
		m_Wall = new Paint(Paint.ANTI_ALIAS_FLAG);
		//m_Wall.setColor(color.wall_color);
		m_Wall.setColor(Color.WHITE);
		//m_Wall.setStrokeWidth(R.dimen.wall_thickness);
		m_Wall.setStrokeWidth(2.5f);
		generateMaze(0, 0);
	}
	
	public void display() {
		for (int i = 0; i < y; i++) {
			// draw the north edge
			for (int j = 0; j < x; j++) {
				System.out.print((m_Maze[j][i] & 1) == 0 ? "+---" : "+   ");
			}
			System.out.println("+");
			// draw the west edge
			for (int j = 0; j < x; j++) {
				System.out.print((m_Maze[j][i] & 8) == 0 ? "|   " : "    ");
			}
			System.out.println("|");
		}
		// draw the bottom line
		for (int j = 0; j < x; j++) {
			System.out.print("+---");
		}
		System.out.println("+");
	}
 
	private void generateMaze(int cx, int cy) {
		DIR[] dirs = DIR.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (DIR dir : dirs) {
			int nx = cx + dir.dx;
			int ny = cy + dir.dy;
			if (between(nx, x) && between(ny, y)
					&& (m_Maze[nx][ny] == 0)) {
				m_Maze[cx][cy] |= dir.bit;
				m_Maze[nx][ny] |= dir.opposite.bit;
				generateMaze(nx, ny);
			}
		}
	}
 
	private static boolean between(int v, int upper) {
		return (v >= 0) && (v < upper);
	}
 
	private enum DIR {
		N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
		private final int bit;
		private final int dx;
		private final int dy;
		private DIR opposite;
 
		// use the static initializer to resolve forward references
		static {
			N.opposite = S;
			S.opposite = N;
			E.opposite = W;
			W.opposite = E;
		}
 
		private DIR(int bit, int dx, int dy) {
			this.bit = bit;
			this.dx = dx;
			this.dy = dy;
		}
	}; 
	
}
