package net.nodomain.chtis.netcattool;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by christian on 14.10.16.
 */

public class ReceiveThread extends Thread {
	private Socket sock;
	BufferedReader in;
	PrintWriter out;

	ReceiveThread(Socket sock){
		this.sock = sock;
	}

	@Override
	public void interrupt(){
		super.interrupt();
		try {
			sock.close();
		}catch(IOException e){
			// we can ignore
		}
	}

	private enum FSMState{
		NEW_TRANSFER,
		WAIT_FOR_HEADERS,
		WAIT_FOR_TRANSFER,
		READ_TRANSFER,
		FINISH_TRANSFER
	}


	private FSMState state = FSMState.NEW_TRANSFER;
	private Transfer tr = null;

	private void fsm(String line, int pos) throws TransferException {
		String header = line.substring(0, pos).toLowerCase();
		String val = line.substring(pos + 1);

		switch(state){
		case NEW_TRANSFER:
			if(!header.equals("type")) {
				throw new TransferException("no type header!");
			}
			tr = new Transfer();
			tr.setHeader(header,val);
			state = FSMState.WAIT_FOR_HEADERS;
			break;
		case WAIT_FOR_HEADERS:
			tr.setHeader(header,val);
			if(tr.receivable()){
				state = FSMState.WAIT_FOR_TRANSFER;
			}
			break;
		case WAIT_FOR_TRANSFER:
			tr.setHeader(header,val);
			if(header.equals("data")){
				state = FSMState.READ_TRANSFER;
			}
			break;
		case READ_TRANSFER:
			if(header.length() == 0){
				tr.addDataLine(val);
			}else{
				if(tr.receivable()){
					state = FSMState.READ_TRANSFER;
				}else {
					state = FSMState.NEW_TRANSFER;
				}
				tr.finish();
			}
		}
	}

	@Override
	public void run(){
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream());
		}catch(IOException e){
			return;
		}
		try {
			while (!this.isInterrupted() && !sock.isClosed()) {
				String line;

				try {
					line = in.readLine();
				} catch (IOException e) {
					// ignore
					continue;
				}
				Log.i("ReceiveThread",line);
				if (line != null) {
					int pos = line.indexOf(':');
					Log.i("ReceiveThread", "line != null. pos = "+pos);
					if (pos >= 0 && pos < line.length()) {
						// valid data
						fsm(line, pos);
					}
				}
			}
		}catch(TransferException e){
			Log.i("ReceiveThread", "TransferException: "+e.toString());
		}
	}
}
