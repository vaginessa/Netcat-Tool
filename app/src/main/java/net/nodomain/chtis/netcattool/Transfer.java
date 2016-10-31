package net.nodomain.chtis.netcattool;

/**
 * Created by christian on 31.10.16.
 */
import java.util.ArrayList;


class TransferException extends Exception{
	public TransferException() { super(); }
	public TransferException(String message) { super(message); }
	public TransferException(String message, Throwable cause) { super(message, cause); }
	public TransferException(Throwable cause) { super(cause); }
}

public class Transfer {
	public enum TransferType{
		UNKNOWN,
		PING,
		MESSAGE,
		FILE,
		MULT_FILES
	}


	TransferType type = TransferType.UNKNOWN;
	int count_files = 0, current = -1;
	ArrayList<String> name;
	ArrayList<Integer> data_lines;
	ArrayList<String> temp_location;

	public Transfer(){
	}

	public void addDataLine(String line){
		// TODO
	}

	public void finish(){
		// TODO
	}

	public void setHeader(String header, String val) throws TransferException{
		if(header.equals("name")){
			name.add(val);
			current++;
		}else if(header.equals("type")) {
			if (type != TransferType.UNKNOWN)
				throw new TransferException("multiple type definitions");

			if (val.equals("ping")) {
				type = TransferType.PING;
			} else if (val.equals("msg")) {
				type = TransferType.MESSAGE;
			} else if (val.equals("file")) {
				type = TransferType.FILE;
				count_files = 1;
			} else if (val.equals("files")) {
				type = TransferType.MULT_FILES;
			} else {
				throw new TransferException("unknown transfer type");
			}
		}else if(header.equals("count")){
			if(type != TransferType.MULT_FILES || count_files != 0){
				throw new TransferException("illegal count header");
			}
			try{
				count_files = Integer.parseInt(val);
			}catch(Exception e) {
				throw new TransferException("illegal parameter to count header");
			}
		}else if(header.equals("data")){
			try {
				int l = Integer.parseInt(val);
				if(l <= 0) throw new Exception();
				data_lines.add(l);
			}catch(Exception e) {
				throw new TransferException("illegal parameter to data header");
			}
		}else{
			throw new TransferException("unknown header");
		}
	}

	public boolean receivable(){
		switch(type){
		case PING:
			return true;
		case MESSAGE:
			return name.size() > 0;
		case FILE:
			// TODO
			return data_lines.size() > 0;
		case MULT_FILES:
			// TODO
			return count_files > 0 && data_lines.size() == count_files;
		default:
			// maybe replace with an appropriate exception
			return false;
		}
	}

	public boolean dataIncoming(){
		if(type == TransferType.FILE){
			//TODO
		}
		return false;
	}
}
