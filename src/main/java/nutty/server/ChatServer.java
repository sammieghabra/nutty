package nutty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import nutty.handler.ChatServerHandler;

import java.net.InetSocketAddress;

public class ChatServer {

    private final int port;

    private ChatServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {

        int port;

        if (args.length != 1) {
            port = 9000;
        } else {
            port = Integer.parseInt(args[0]);
        }

        System.out.println("Starting host at port: " + port);
        new ChatServer(port).start();
    }

    private void start() throws Exception {
        final ChatServerHandler serverHandler = new ChatServerHandler();
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(120));
                            ch.pipeline().addLast("decoder", new StringDecoder());
                            ch.pipeline().addLast("encoder", new StringEncoder());
                            ch.pipeline().addLast(serverHandler);
                        }

                    });
            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

}
