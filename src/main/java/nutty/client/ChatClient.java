package nutty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import nutty.handler.ChatClientHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class ChatClient {
    private final String host;
    private final int port;

    public ChatClient(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public void start() throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(80));
                            ch.pipeline().addLast("decoder", new StringDecoder());
                            ch.pipeline().addLast("encoder", new StringEncoder());
                            ch.pipeline().addLast(new ChatClientHandler());
                        }
                    });
            ChannelFuture f = b.connect().sync();
            Channel channel = f.channel();

            BufferedReader in = new BufferedReader(new InputStreamReader
                    (System.in));

            while (true) {
                channel.writeAndFlush(in.readLine());
            }

            //f.channel().closeFuture().sync();
        } finally {
            // Shuts down the thread pools and the release of all resources
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        String host;
        int port;

        if (args.length != 2) {
            // just some defaults
            host = "localhost";
            port = 9000;
        } else {
            port = Integer.parseInt(args[0]);
            host = args[1];
        }

        new ChatClient(host, port).start();
    }
}
