package nutty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * Called after the connection to the server is established
     * @param context
     */
    @Override
    public void channelActive(ChannelHandlerContext context) {
        // when notified that the channel is active, sends a message
        context.writeAndFlush(Unpooled.copiedBuffer("Netty rocks", CharsetUtil.UTF_8));
    }

    /**
     * called when a message is received from the server
     * @param context
     * @param buf
     */
    @Override
    public void channelRead0(ChannelHandlerContext context, ByteBuf buf) {

         System.out.println("Client received: " + buf.toString(CharsetUtil.UTF_8));
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
