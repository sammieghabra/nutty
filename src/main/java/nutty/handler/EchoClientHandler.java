package nutty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<String> {

    /**
     * called when a message is received from the server
     * @param context
     * @param str
     */
    @Override
    public void channelRead0(ChannelHandlerContext context, String str) {
         System.out.println(str);
    }

    /**
     * Called if an exception is raised during processing
     * @param context
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();
        context.close();
    }
}
