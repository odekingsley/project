package fr.upem.jarret.server;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import fr.upem.http.HttpException;
import fr.upem.http.HttpRequestHeader;
import fr.upem.http.non.blocking.HttpReaderNonBlocking;
import fr.upem.http.non.blocking.HttpRequest;
import fr.upem.jarret.server.ResponseBuilder.Code;
import fr.upem.jarret.server.Command.CommandManager;
import fr.upem.jarret.server.Command.CommandVisitor;


public class JarretServer {



	private class Context {
		private boolean inputClosed = false;
		private final ByteBuffer in = ByteBuffer.allocate(BUF_SIZE);
		private final ArrayDeque<ByteBuffer> outs = new ArrayDeque<>();
		private final SelectionKey key;
		private final SocketChannel sc;
		private final HttpReaderNonBlocking reader = new HttpReaderNonBlocking(in);



		public Context(SelectionKey key) {
			this.key = key;
			this.sc = (SocketChannel) key.channel();
		}

		public void doRead() throws IOException {
			if(sc.read(in) == -1){
				inputClosed=true;
			}
			process();
			updateInterestOps();
		}

		public void doWrite() throws IOException {
			ByteBuffer out = outs.peek();
			out.flip();
			sc.write(out);
			out.compact();
			process();
			updateInterestOps();
		}

		private void process() {
			if(inputClosed){
				silentlyClose(sc);
				return;
			}

			if( ! outs.isEmpty() && outs.peek().position() == 0){
				outs.remove();
			}

			try {
				reader.readRequest().ifPresent(this::manage);
			} catch (HttpException e) {
				new ResponseBuilder(Code.BadRequest).get().forEach(outs::offer);
			}





		}
		private void manage(HttpRequest request) {
			HttpRequestHeader header = request.getHeader();
			Optional<Route> optional = router.get(header.getMethod(), header.getRessource());
			if(optional.isPresent()){
				optional.get().manage(request).forEach(outs::offer);
				return;
			}
			new ResponseBuilder(Code.BadRequest).get().forEach(outs::offer);

		}

		private void updateInterestOps(){
			int interestOps = 0 ;
			if(in.position() != 0 && !inputClosed){
				interestOps |= SelectionKey.OP_READ;
			}
			if(outs.isEmpty() && outs.peek().position() != 0){
				interestOps |= SelectionKey.OP_WRITE;
			}
			key.interestOps(interestOps);

		}

	}

	private static final int BUF_SIZE = 512;
	private final ServerSocketChannel serverSocketChannel;
	private final Selector selector;

	public ServerSocketChannel getServerSocketChannel() {
		return serverSocketChannel;
	}

	public Selector getSelector() {
		return selector;
	}

	public Set<SelectionKey> getSelectedKeys() {
		return selectedKeys;
	}


	private final Set<SelectionKey> selectedKeys;
	private final Router router;
	private final CommandManager manager;
	public JarretServer(int port,Router router,CommandManager manager) throws IOException {
		this.router = router;
		this.manager = manager;
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector = Selector.open();
		selectedKeys = selector.selectedKeys();
	}

	public void launch() throws IOException {
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		while (!Thread.interrupted()) {
			printKeys();
			System.out.println("Starting select");
			selector.select();
			System.out.println("Select finished");
			printSelectedKey();
			processSelectedKeys();
			selectedKeys.clear();
		}
	}

	private void processSelectedKeys() throws IOException {
		for (SelectionKey key : selectedKeys) {
			if (key.isValid() && key.isAcceptable()) {
				doAccept(key);
			}
			try {
				Context cntxt= (Context) key.attachment();
				if (key.isValid() && key.isWritable()) {
					cntxt.doWrite();
				}
				if (key.isValid() && key.isReadable()) {
					cntxt.doRead();
				}
			} catch (IOException e) {
				silentlyClose(key.channel());
			}
		}
	}

	private void doAccept(SelectionKey key) throws IOException {
		SocketChannel sc = serverSocketChannel.accept();
		sc.configureBlocking(false);
		SelectionKey clientKey = sc.register(selector,SelectionKey.OP_READ);
		clientKey.attach(new Context(clientKey));
	}

	public void close() throws IOException {
		selector.wakeup();
		selector.close();
		serverSocketChannel.close();

	}

	public void startCommandListener(InputStream in) {
		new Thread(() -> {
			Scanner scanner = new Scanner(in);
			while(scanner.hasNext()){
				String string = scanner.next();
				Optional<CommandVisitor> optional = manager.get(string);
				if(! optional.isPresent()){
					System.err.println("Unknown command "+string);
					continue;
				}
				CommandVisitor visitor = optional.get();
				visitor.visit(this);
			}
			scanner.close();
		}).start();
	}

	private static void silentlyClose(SelectableChannel sc) {
		if (sc==null)
			return;
		try {
			sc.close();
		} catch (IOException e) {
			// silently ignore
		}
	}



	private static void usage() {
		System.out.println("ServerSumNew <listeningPort>");
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		if(args.length != 1) {
			usage();
			return;
		}
	}

	/***
	 *  Theses methods are here to help understanding the behavior of the selector
	 ***/

	private String interestOpsToString(SelectionKey key){
		if (!key.isValid()) {
			return "CANCELLED";
		}
		int interestOps = key.interestOps();
		ArrayList<String> list = new ArrayList<>();
		if ((interestOps&SelectionKey.OP_ACCEPT)!=0) list.add("OP_ACCEPT");
		if ((interestOps&SelectionKey.OP_READ)!=0) list.add("OP_READ");
		if ((interestOps&SelectionKey.OP_WRITE)!=0) list.add("OP_WRITE");
		return String.join("|",list);
	}

	public void printKeys() {
		Set<SelectionKey> selectionKeySet = selector.keys();
		if (selectionKeySet.isEmpty()) {
			System.out.println("The selector contains no key : this should not happen!");
			return;
		}
		System.out.println("The selector contains:");
		for (SelectionKey key : selectionKeySet){
			SelectableChannel channel = key.channel();
			if (channel instanceof ServerSocketChannel) {
				System.out.println("\tKey for ServerSocketChannel : "+ interestOpsToString(key));
			} else {
				SocketChannel sc = (SocketChannel) channel;
				System.out.println("\tKey for Client "+ remoteAddressToString(sc) +" : "+ interestOpsToString(key));
			}
		}
	}

	private String remoteAddressToString(SocketChannel sc) {
		try {
			return sc.getRemoteAddress().toString();
		} catch (IOException e){
			return "???";
		}
	}

	private void printSelectedKey() {
		if (selectedKeys.isEmpty()) {
			System.out.println("There were not selected keys.");
			return;
		}
		System.out.println("The selected keys are :"); 
		for (SelectionKey key : selectedKeys) {
			SelectableChannel channel = key.channel();
			if (channel instanceof ServerSocketChannel) {
				System.out.println("\tServerSocketChannel can perform : " + possibleActionsToString(key));
			} else {
				SocketChannel sc = (SocketChannel) channel;
				System.out.println("\tClient " + remoteAddressToString(sc) + " can perform : " + possibleActionsToString(key));
			}

		}
	}

	private String possibleActionsToString(SelectionKey key) {
		if (!key.isValid()) {
			return "CANCELLED";
		}
		ArrayList<String> list = new ArrayList<>();
		if (key.isAcceptable()) list.add("ACCEPT");
		if (key.isReadable()) list.add("READ");
		if (key.isWritable()) list.add("WRITE");
		return String.join(" and ",list);
	}

}