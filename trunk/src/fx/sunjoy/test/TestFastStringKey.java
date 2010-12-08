package fx.sunjoy.test;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class TestFastStringKey {
	public static void main(String[] args) throws Exception {
		String path = "c:/test/fast";
		if(args.length>0){
			path = args[0];
		}
		DiskTreap<FastString, Serializable> treap = new DiskTreap<FastString,Serializable>(64,new File(path),64<<20);
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		//String data = new String(buf.array());
		long t1 = System.currentTimeMillis();
		List<FastString> keys = new ArrayList<FastString>();
		for(int i=0;i<1000000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			//System.out.println(String.format("%010d", key));
			FastString realKey = new FastString("thing"+String.format("%010d", key));
			treap.put(realKey,buf.array());
			//String v = treap.get("thing"+i);
			if(i%100==0){
				keys.add(realKey);
				System.out.println("geting:"+i);
			}
		}
		for(FastString k: keys){
			System.out.println(treap.get(k));
		}
		System.out.println(System.currentTimeMillis()-t1);
	}
}
