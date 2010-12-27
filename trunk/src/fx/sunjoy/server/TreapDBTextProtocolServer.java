package fx.sunjoy.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.server.cmd.AfterCommand;
import fx.sunjoy.server.cmd.BeforeCommand;
import fx.sunjoy.server.cmd.BulkGetCommand;
import fx.sunjoy.server.cmd.DelCommand;
import fx.sunjoy.server.cmd.GetCommand;
import fx.sunjoy.server.cmd.KMaxCommand;
import fx.sunjoy.server.cmd.KMinCommand;
import fx.sunjoy.server.cmd.LenCommand;
import fx.sunjoy.server.cmd.PrefixCommand;
import fx.sunjoy.server.cmd.RangeCommand;
import fx.sunjoy.server.cmd.SetCommand;
import fx.sunjoy.server.cmd.SyncCommand;
import fx.sunjoy.utils.FastString;


class Msg{
	String command;
	byte[] body;
}

public class TreapDBTextProtocolServer {
	

	private String replicationRole = null ;
	
	private DiskTreap<FastString, byte[]> diskTreap;

	ServerSocket serverSocket;
	int port;
	
	private boolean stopped = false;
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public TreapDBTextProtocolServer(DiskTreap<FastString, byte[]> _diskTreap,int _port){
		this.diskTreap = _diskTreap;
		this.port = _port;
	}
	
	private void handleConn(final Socket clientSocket){
		try {
			BufferedInputStream is = new BufferedInputStream(clientSocket.getInputStream());
			BufferedOutputStream os = new BufferedOutputStream(clientSocket.getOutputStream());
			for(;;){
					Msg msg = parseMessage(is);
					if(msg==null)break;
					try {
						if(msg.command.startsWith("get ")){
							new GetCommand().execute(diskTreap,msg.command,msg.body, os);
						}else if(msg.command.startsWith("set ")){
							if(replicationRole != null && replicationRole.equalsIgnoreCase("Slave"))
							{
								os.write("Slave cloud not do set operation!\r\n".getBytes()) ;
								os.write("END\r\n".getBytes()) ;
							}
							else
							{
								new SetCommand().execute(diskTreap,msg.command,msg.body, os);
							}
						}else if(msg.command.startsWith("prefix ")){
							new PrefixCommand().execute(diskTreap,msg.command,msg.body, os);
						}else if(msg.command.startsWith("range ")){
							new RangeCommand().execute(diskTreap,msg.command,msg.body, os);
						}else if(msg.command.startsWith("kmin ")){
							new KMinCommand().execute(diskTreap,msg.command,msg.body, os);
						}else if(msg.command.startsWith("kmax ")){
							new KMaxCommand().execute(diskTreap,msg.command,msg.body, os);
						}else if(msg.command.startsWith("len")){
							new LenCommand().execute(diskTreap,msg.command,msg.body, os);
						}else if(msg.command.startsWith("delete ")){
							if(replicationRole != null && replicationRole.equals("Slave"))
							{
								os.write("Slave cloud not do delete operation!\r\n".getBytes()) ;
								os.write("END\r\n".getBytes()) ;
							}
							else
							{
								new DelCommand().execute(diskTreap,msg.command,msg.body, os);
							}
						}else if(msg.command.startsWith("sync ")){
							new SyncCommand().execute(diskTreap,msg.command,msg.body, os) ;
						}else if(msg.command.startsWith("before ")){
							new BeforeCommand().execute(diskTreap,msg.command,msg.body, os);
						}else if(msg.command.startsWith("after ")){
							new AfterCommand().execute(diskTreap,msg.command,msg.body, os);
						}else if(msg.command.startsWith("bulkget ")){
							new BulkGetCommand().execute(diskTreap,msg.command,msg.body, os);
						}else{
							os.write("ERROR\r\n".getBytes());
						}
					} catch (Exception e) {
						e.printStackTrace();
						os.write("ERROR\r\n".getBytes());
					}
					os.flush();
			}
		} catch (IOException e1) {
			System.out.println("client "+clientSocket+" leave."+e1.getMessage());
		}
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("client "+clientSocket+" leave.");
	}
	
	private Msg parseMessage(BufferedInputStream is) throws IOException {
		Msg msg = new Msg();
		byte[] buf = new byte[1024];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int ct = 0;
		while ((ct = is.read(buf, 0, buf.length)) != -1) {
			bos.write(buf, 0, ct);
			if(buf[ct-2]=='\r' && buf[ct-1]=='\n'){
				break;
			}
		}
		if(ct==-1)return null;
		byte[] content = bos.toByteArray();
		int j = 0;
		for(int i=0;i<content.length;i++){
			if(content[i]=='\r'){
				j = i;
				break;
			}
		}
		msg.command = new String(Arrays.copyOf(content, j));
		String[] stuff = msg.command.split(" ");
		if(stuff.length==5){// not read operation
			int readCount = content.length-(j+4);
			int shouldReadCount = Integer.parseInt(stuff[4]);
			while(readCount<shouldReadCount){
				ct = is.read(buf, 0, buf.length);
				if(ct==-1)break;
				bos.write(buf, 0, ct);
				readCount += ct;
			}
			content = bos.toByteArray();
			msg.body = Arrays.copyOfRange(content, j+2,content.length-2);
		}
		return msg;
	}

	public void run() throws IOException{
		serverSocket = new ServerSocket(port);
		while(!stopped){
			final Socket clientSocket = serverSocket.accept();
			System.out.println("client:"+clientSocket+" connected.");
			//clientSocket.setKeepAlive(true);
			pool.execute(
					new Runnable(){
						public void run(){
							handleConn(clientSocket);
						}
					}
			);
		}
	}

	public synchronized void close(){
		stopped = true;
		pool.shutdown();
	}
	
	public void setReplicationRole(String replicationRole) {
		this.replicationRole = replicationRole;
	}
}



