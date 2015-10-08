package org.tourgune.mdp.airbnb.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TestNonBlocking {

	public static void main(String[] args) {
		try {
			ByteBuffer writeBuffer = ByteBuffer.allocate(1).put((byte) 97);
			ByteBuffer readBuffer = ByteBuffer.allocate(1);
			SocketChannel sc = SocketChannel.open(new InetSocketAddress("10.10.0.116", 54000));
			sc.configureBlocking(false);
			Selector selector = Selector.open();
			
			sc.register(selector, SelectionKey.OP_READ);
			
			sc.write(ByteBuffer.wrap(new byte[]{97,97,97}));
			
			for (;sc.isOpen();) {
				if (selector.select() == 0)
					continue;
				Set<SelectionKey> keys = selector.selectedKeys();
				for (Iterator<SelectionKey> keyIt = keys.iterator(); keyIt.hasNext();) {
					SelectionKey key = keyIt.next();
					if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
						int read = sc.read(readBuffer);
						readBuffer.clear();
						if (read > 0) {
							System.out.println(read + " bytes were read: " + new String(readBuffer.array()));
						}
						if (read == -1) {
							sc.close();
						}
					}
					keyIt.remove();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
