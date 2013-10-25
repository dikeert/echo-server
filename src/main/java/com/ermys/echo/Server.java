package com.ermys.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.base.Throwables.propagate;
import static java.nio.channels.SelectionKey.*;

/**
 * This is simple, example non-blocking nio echo-server.
 * It is writes back everything it's got
 *
 * @author ANER0310
 *         Date: 10/24/13
 */
public class Server implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(Server.class);

	private final int port;
	private final ByteBuffer buffer;

	@Inject
	public Server(@Named("server.port") int port,
				  @ReadBuffer ByteBuffer buffer)
	{
		this.port = port;
		this.buffer = buffer;
	}

	@Override
	public void run() {
		Selector selector = null;
		ServerSocketChannel server = null;

		try {
			log.debug("Starting server...");

			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(port));
			server.configureBlocking(false);
			server.register(selector, OP_ACCEPT);

			log.debug("Server ready, now ready to accept connections");
			loop(selector, server);

		} catch (Throwable e) {
			log.error("Server failure", e);
			propagate(e);
		} finally {
			try {
				selector.close();
				server.socket().close();
				server.close();
			} catch (Exception e) {
				// do nothing - server failed
			}
		}
	}

	private void loop(Selector selector, ServerSocketChannel server) throws IOException {
		while(true){
			selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

			while(keys.hasNext()){
				SelectionKey key = keys.next();
				keys.remove();

				if(key.isConnectable()){
					log.debug("Connectable detected");
					((SocketChannel) key.channel()).finishConnect();
				} else if(key.isAcceptable()){
					acceptOp(key, selector, server);
				} else if(key.isReadable()){
					readOp(key);
				} else if(key.isWritable()){
					writeOp(key);
				}
			}
		}
	}

	private void acceptOp(SelectionKey key, Selector selector, ServerSocketChannel server) throws IOException {
		SocketChannel client = server.accept();

		log.debug("Acceptable detected, incoming client: {}", client.getRemoteAddress());

		client.configureBlocking(false);
		client.register(selector, OP_READ);
	}

	private void readOp(SelectionKey key){
		log.debug("Data received, going to read them");
		SocketChannel channel = (SocketChannel) key.channel();

		String result = read(channel);

		if(nullToEmpty(result).isEmpty()){
			return;
		} else{
			key.attach(result);
			key.interestOps(OP_WRITE);
		}
	}

	private void writeOp(SelectionKey key) throws IOException {
		String toWrite = nullToEmpty((String) key.attachment());
		if(write((SocketChannel) key.channel(), toWrite)){
			key.interestOps(OP_READ);
		} else{
			key.channel().close();
			key.cancel();
		}
	}

	String read(SocketChannel channel){
		buffer.clear();
		int numRead = -1;

		try {
			numRead = channel.read(buffer);

			if(numRead == -1){
				log.debug("Connection closed by: {}", channel.getRemoteAddress());
				channel.close();
				return "";
			}

			byte[] data = new byte[numRead];
			System.arraycopy(buffer.array(), 0, data, 0, numRead);
			String result = new String(data, "UTF-8");
			log.debug("Got [{}] from [{}]", result, channel.getRemoteAddress());
			return result;
		} catch (IOException e) {
			log.error("Unable to read from channel", e);
			try {
				channel.close();
			} catch (IOException e1) {
				//nothing to do, channel dead
			}
		}

		return "";
	}

	boolean write(SocketChannel channel, String content){
		try {
			channel.write(ByteBuffer.wrap(content.getBytes()));
			return true;
		} catch (IOException e) {
			log.error("Unable to write content", e);
			try {
				channel.close();
			} catch (IOException e1) {
				//dead channel, nothing to do
			}
			return false;
		}
	}
}