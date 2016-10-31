package net.nodomain.chtis.netcattool;

import android.support.annotation.NonNull;
import android.widget.MultiAutoCompleteTextView;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import javax.security.auth.Destroyable;

/**
 * Created by christian on 13.10.16.
 */

class Connection{
	public final Socket sock;
	public final String addr;
	public final int port;
	private boolean active = true;

	public Connection(String addr, int port, Socket sock){
		this.sock = sock;
		this.port = port;
		this.addr = addr;
	}

	public boolean setActive(boolean active){
		this.active = active;
		return true;
	}
	public boolean isActive(){
		return active;
	}
	public void destroy(){
		try {
			sock.close();
		}catch(IOException e){
			//ignore
		}
	}
}

public class ConnectionManager implements Destroyable{
	private ArrayList<Connection> list = new ArrayList<>();
	private Semaphore lock = new Semaphore(1);

	public ConnectionManager(){
	}

	private Socket existing(String addr, int port) throws SocketException{
		for (Connection c : list) {
			if (addr.equals(c.addr) && c.port == port) {
				if (c.sock.isClosed()) {
					list.remove(c);
					return null;
				}
				if(c.isActive()){
					throw new SocketException();
				}
				return c.sock;
			}
		}
		return null;
	}

	private void addSocket(String addr, int port, Socket sock){
		list.add(new Connection(addr, port, sock));
	}

	public void addSocket(Socket sock){
		String addr = sock.getInetAddress().getHostAddress();
		int port = sock.getPort();

		try {
			lock.acquire();
			addSocket(addr, port, sock);
		}catch(InterruptedException e){
			// ignore
		}finally {
			lock.release();
		}
	}
	public void closeSocket(Socket sock){
		try {
			lock.acquire();
			for(Connection c : list){
				if(c.sock == sock){
					c.setActive(false);
				}
			}
		}catch(InterruptedException e){
			// ignore
		}finally {
			lock.release();
		}

	}

	public Socket getSocket(String addr, int port) throws IOException{
		Socket sock;
		try {
			lock.acquire();
			sock = existing(addr, port);
			if(sock == null) {
				sock = new Socket(addr, port);
				addSocket(addr, port, sock);
			}
		}catch(Exception e){
			// ignore
			sock = null;
		}finally {
			lock.release();
		}

		return sock;
	}

	public void destroy(){
		try {
			lock.acquire();
			for(Connection c : list){
				c.destroy();
			}
			list.clear();
		}catch(InterruptedException e){
			// ignore
		}finally {
			lock.release();
			lock = null;
		}
	}
	public boolean isDestroyed(){
		return lock == null;
	}
}
