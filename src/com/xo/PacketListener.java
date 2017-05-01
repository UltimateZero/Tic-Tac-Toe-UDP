package com.xo;

import com.xo.core.GameCore.Player;

public interface PacketListener {

	void discoveryRequestReceived(Computer computer);
	void discoveryResponseReceived(Computer computer);
	void playRequestReceived(Computer computer);
	void playAcceptReceived(Computer computer);
	void playRejectReceived(Computer computer);
	void moveReceived(Computer computer, int row, int column, Player player);
}
