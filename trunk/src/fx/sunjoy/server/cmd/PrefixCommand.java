package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class PrefixCommand extends AbstractCommand{

	@Override
	public void execute(DiskTreap<FastString, byte[]> diskTreap,
			String command, byte[] body, BufferedOutputStream os)
			throws Exception {
		String[] stuff = command.split(" ");
		String prefix = stuff[1];
		Integer limit = Integer.parseInt(stuff[2]);
		Map<FastString, byte[]> result = diskTreap.prefix(new FastString(prefix),limit,null,true);
		for(Entry<FastString,byte[]> e :result.entrySet()){
			byte[] realvalue = new byte[e.getValue().length - 4] ;
			System.arraycopy(e.getValue(), 4, realvalue, 0, realvalue.length) ;
			os.write((e.getKey()+",\t"+new String(realvalue)+"\r\n").getBytes());
		}
		os.write(("END\r\n").getBytes());
	}

}
