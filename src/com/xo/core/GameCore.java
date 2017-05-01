package com.xo.core;

import java.util.Arrays;

public class GameCore {

	private static char EMPTY = '#';

	public enum Player {
		X, O;
		
		public char toChar() {
			if (this == Player.X) return 'X';
			else return 'O';
		}
	}

	public enum WinType {
		FirstRow, SecondRow, ThirdRow, 
		FirstColumn, SecondColumn, ThirdColumn, 
		FirstDiagonal, SecondDiagonal,
		Draw, 
		Nothing
	}

	private char[][] board;

	public GameCore() {
		board = new char[3][3];
		newGame();
	}

	public void newGame() {
		for (char[] row : board)
			Arrays.fill(row, EMPTY);
	}
	
	public WinType addCell(int row, int column, Player player) throws Exception {
		if (board[row][column] != EMPTY) {
			throw new Exception("Cell already taken!");
		}
		char playerChar = player.toChar();
		board[row][column] = playerChar;

		if (board[0][0] == playerChar && board[0][1] == playerChar && board[0][2] == playerChar) {
			return WinType.FirstRow;
		} else if (board[1][0] == playerChar && board[1][1] == playerChar && board[1][2] == playerChar) {
			return WinType.SecondRow;
		} else if (board[2][0] == playerChar && board[2][1] == playerChar && board[2][2] == playerChar) {
			return WinType.ThirdRow;
		}
		//
		if (board[0][0] == playerChar && board[1][0] == playerChar && board[2][0] == playerChar) {
			return WinType.FirstColumn;
		} else if (board[0][1] == playerChar && board[1][1] == playerChar && board[2][1] == playerChar) {
			return WinType.SecondColumn;
		} else if (board[0][2] == playerChar && board[1][2] == playerChar && board[2][2] == playerChar) {
			return WinType.ThirdColumn;
		}
		//
		else if (board[0][0] == playerChar && board[1][1] == playerChar && board[2][2] == playerChar) {
			return WinType.FirstDiagonal;
		} else if (board[0][2] == playerChar && board[1][1] == playerChar && board[2][0] == playerChar) {
			return WinType.SecondDiagonal;
		}
		else if (isDraw()) {
			return WinType.Draw;
		}
		else {
			return WinType.Nothing;
		}

	}

	private boolean isDraw() {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (board[row][col] == EMPTY)
					return false;
			}
		}
		return true;
	}

	public void printBoard() {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				System.out.print(board[row][col]);
			}
			System.out.println();
		}
		System.out.println();
	}
}
