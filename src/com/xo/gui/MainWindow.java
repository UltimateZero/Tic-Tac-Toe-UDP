package com.xo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.xo.Computer;
import com.xo.PacketListener;
import com.xo.core.GameCore;
import com.xo.core.GameCore.Player;
import com.xo.core.GameCore.WinType;
import com.xo.net.Server;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private JButton[][] buttons;
	private JList<Computer> players;
	private DefaultListModel<Computer> listModel;
	private Player currentPlayer = Player.O;
	private Computer currentOpponent = null;
	private boolean myTurn = false;
	private GameCore core;
	private Server server;

	public MainWindow() throws HeadlessException {
		super();
		
		initGUI();
		core = new GameCore();
		server = new Server(new ServerPacketListener());
		server.startListening();

		startDiscoveryThread();
	}

	private void initGUI() {
		setTitle("Tic-Tac-Toe Net");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel boardPanel = new JPanel();
		boardPanel.setLayout(new GridLayout(3, 3));
		boardPanel.setBorder(BorderFactory.createLineBorder(Color.gray, 3));
		boardPanel.setBackground(Color.white);
		listModel = new DefaultListModel<>();
		players = new JList<>(listModel); // players list
		players.setFixedCellWidth(100);
		
		buttons = new JButton[3][3];
		BoardButtonActionListener boardListener = new BoardButtonActionListener();
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				buttons[row][col] = new JButton();
				buttons[row][col].setActionCommand(row + "," + col);
				buttons[row][col].setText(" ");
				buttons[row][col].setBackground(Color.white);
				buttons[row][col].setFocusable(false);
				buttons[row][col].addActionListener(boardListener);
				boardPanel.add(buttons[row][col]);
			}
		}

		players.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (!players.isEnabled())
					return;
				if (evt.getClickCount() == 2) {
					Computer computer = players.getSelectedValue();
					try {
						players.setEnabled(false);
						server.sendPlayRequest(computer);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		mainPanel.add(players, BorderLayout.WEST);
		mainPanel.add(boardPanel, BorderLayout.CENTER);
		getContentPane().add(mainPanel);
		pack();
		setSize(400, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void startDiscoveryThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						server.sendDiscoveryRequest();
						Thread.sleep(1000 * 10);
					}

				} catch (InterruptedException | SocketException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void resetBoard() {
		currentOpponent = null;
		currentPlayer = Player.O;
		players.setEnabled(true);
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				buttons[row][col].setText(" ");
				buttons[row][col].setEnabled(true);
			}
		}
	}

	class ServerPacketListener implements PacketListener {

		@Override
		public void discoveryRequestReceived(Computer computer) {
			if (!listModel.contains(computer))
				listModel.addElement(computer);
			try {
				System.out.println("Received discovery request: " + computer);
				server.sendDiscoveryResponse(computer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void discoveryResponseReceived(Computer computer) {
			if (!listModel.contains(computer))
				listModel.addElement(computer);
		}

		@Override
		public void playRequestReceived(Computer computer) {
			
			//already in game
			if(currentOpponent != null) {
				try {
					server.sendPlayReject(computer);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			JOptionPane.showMessageDialog(null, "Received request from " + computer.toString());
			try {
				int result = JOptionPane.showConfirmDialog(null, computer+" requests a game. Accept?", "Request", JOptionPane.YES_NO_CANCEL_OPTION);
				if(result == JOptionPane.YES_OPTION){
					server.sendPlayAccept(computer);
					myTurn = false;
					currentOpponent = computer;
					players.setEnabled(false);
				}
				else {
					server.sendPlayReject(computer);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void playAcceptReceived(Computer computer) {
			JOptionPane.showMessageDialog(null, computer.toString() + " accepted your request");
			myTurn = true;
			currentOpponent = computer;
			players.setEnabled(false);
		}

		@Override
		public void playRejectReceived(Computer computer) {
			JOptionPane.showMessageDialog(null, computer + " rejected your request");
			currentOpponent = null;
			players.setEnabled(true);
		}

		@Override
		public void moveReceived(Computer computer, int row, int column, Player player) {
			makeMove(row, column, player, false);
			myTurn = true;
		}

	}

	private void makeMove(int row, int col, Player player, boolean send) {
		JButton btn = buttons[row][col];
		try {
			WinType winType = core.addCell(row, col, currentPlayer);
			btn.setText(currentPlayer.toString());
			btn.setEnabled(false);

			if (send)
				server.sendMove(row, col, currentPlayer, currentOpponent);

			if (winType == WinType.Nothing) {

			} else {
				// Game Over
				switch (winType) {
				case Draw:
					JOptionPane.showMessageDialog(null, "DRAW");
					break;
				case FirstColumn:
					JOptionPane.showMessageDialog(null, currentPlayer + " win " + winType);
					break;
				case FirstDiagonal:
					JOptionPane.showMessageDialog(null, currentPlayer + " win " + winType);
					break;
				case FirstRow:
					JOptionPane.showMessageDialog(null, currentPlayer + " win " + winType);
					break;
				case SecondColumn:
					JOptionPane.showMessageDialog(null, currentPlayer + " win " + winType);
					break;
				case SecondDiagonal:
					JOptionPane.showMessageDialog(null, currentPlayer + " win " + winType);
					break;
				case SecondRow:
					JOptionPane.showMessageDialog(null, currentPlayer + " win " + winType);
					break;
				case ThirdColumn:
					JOptionPane.showMessageDialog(null, currentPlayer + " win " + winType);
					break;
				case ThirdRow:
					JOptionPane.showMessageDialog(null, currentPlayer + " win " + winType);
					break;
				default:
					break;

				}
				core.newGame();
				resetBoard();
			}

			//Toggle players
			if (currentPlayer == Player.O)
				currentPlayer = Player.X;
			else
				currentPlayer = Player.O;

		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	class BoardButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (currentOpponent == null) {
//				JOptionPane.showMessageDialog(null, "Game have not started");
				return;
			}
			if (!myTurn)
				return;
			System.out.println(e.getActionCommand());
			int row = Integer.parseInt(e.getActionCommand().split(",")[0]);
			int col = Integer.parseInt(e.getActionCommand().split(",")[1]);

			makeMove(row, col, currentPlayer, true);
			myTurn = false;
		}
	}

}
