package me.davehummel.tredserver.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.*;

/**
 * Created by dmhum_000 on 8/9/2015.
 */
public class WebSocketServer {

    static final int PORT = 8080;
  //  private final SerialSession serial;
    private final Set<Channel> channels = new HashSet<>();
    private final List<WebSocketServerListener> listeners = new ArrayList<>();

//    public WebSocketServer(SerialSession session) {
//        this.serial = session;
//    }

    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketServerInitializer(this));

            Channel ch = b.bind(PORT).sync().channel();

            System.out.println("Open your web browser and navigate to " +
                    "http://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

   synchronized public void broadcast(String text){
        for(Iterator<Channel> iter = channels.iterator(); iter.hasNext() ; ){
            Channel channel = iter.next();
            if (channel.isOpen()) {
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }else {
                iter.remove();
                System.err.printf("Closed channel %s", channel);
            }
        }
    }

    public void addListener(WebSocketServerListener listener){
        listeners.add(listener);
    }

    void addChannel(Channel channel){
        channels.add(channel);
    }

    void receive(String text){
        for (WebSocketServerListener listener:listeners){
            listener.receive(text);
        }

    }

}
