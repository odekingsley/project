package fr.upem.http.non.blocking;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.function.Supplier;

import fr.upem.http.HttpException;



public class FakeServer {
	private static final int BUF_SIZE = 512;
	private class Context {
        private final ByteBuffer in = ByteBuffer.allocate(BUF_SIZE);
        private final ByteBuffer out = ByteBuffer.allocate(BUF_SIZE);
        private final SelectionKey key;
        private final SocketChannel sc;
        private final HttpReaderNonBlocking reader = new HttpReaderNonBlocking(in);


        
        public Context(SelectionKey key) {
            this.key = key;
            this.sc = (SocketChannel) key.channel();
        }

        public void doRead() throws IOException {
            if(sc.read(in) == -1){
                sc.close();
            }
            process();
            updateInterestOps();
        }

        public void doWrite() throws IOException {
            out.flip();
            sc.write(out);
            out.compact();
            process();
            updateInterestOps();
        }

        private void process(){
            try {
				reader.readRequest().ifPresent(h -> {
					synchronized (monitor) {
						request = () -> h;
						monitor.notify();
					}
					
				});
			} catch (HttpException e) {
				synchronized (monitor) {
					request = () -> {
						throw new UncheckedIOException(e);
					};
					monitor.notify();
				}
				
			}
        }

        private void updateInterestOps(){
           key.interestOps(SelectionKey.OP_READ);
        }
	}
	
	
	private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final Object monitor  = new Object();
    private Supplier <HttpRequest> request;
    
    
    
    public Supplier<HttpRequest> getRequest() throws InterruptedException{
    	synchronized (monitor) {
    		while(request == null){
    			monitor.wait();
    		}
    		Supplier<HttpRequest> r = request;
    		request = null;
    		return r;
		}
    	
    }
    
    
    public FakeServer(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
    }
    
    public void launch() throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        while (!Thread.interrupted()) {
           
            selector.select();
            
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
            
            selectedKeys.clear();
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

	private static void silentlyClose(SelectableChannel sc) {
        if (sc==null)
            return;
        try {
            sc.close();
        } catch (IOException e) {
            // silently ignore
        }
    }
}
