package nutty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class EchoServerHandler extends SimpleChannelInboundHandler<String> {

    // what does this do?
    private static final ChannelGroup channels =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final Map<String, String> userNameToChannelId =
            new ConcurrentHashMap<>();

    private static final Map<String, String> channelIdToUserName =
            new ConcurrentHashMap<>();

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.println("Removing channel- " + ctx.channel().remoteAddress());
        channels.remove(ctx.channel());
        final String userName = channelIdToUserName.get(ctx.channel().id().asLongText());
        userNameToChannelId.remove(userName);
        channelIdToUserName.remove(ctx.channel().id().asLongText());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("Adding channel- " + ctx.channel().remoteAddress());
        ctx.channel().writeAndFlush("Please enter a username");
        channels.add(ctx.channel());
    }

    @Override
    public void channelRead0(ChannelHandlerContext context, String message) {
        final Channel income = context.channel();

        final String channelId = income.id().asLongText();
        final String userName = channelIdToUserName.get(channelId);

        if (userName != null) {
            System.out.println("Server received: " + message + " from user: " + userName
                    + " at address " + income.remoteAddress());

            channels.forEach(channel -> {
                if (channel != income) {
                    channel.writeAndFlush(userName + ": " + message);
                }
            });

        } else {

            System.out.println("Registering new user " + income.remoteAddress());
            userNameToChannelId.computeIfAbsent(message, key -> channelId);

            if (channelId != userNameToChannelId.get(message)) {
                income.writeAndFlush("Username: " + message + " already exists. Please pick a new one");
            } else {
                channelIdToUserName.computeIfAbsent(channelId, key -> message);

                if (!channelIdToUserName.get(channelId).equals(message)) {
                    throw new RuntimeException("Something weird happened with clientId: "
                            + channelId + " userName " + userName
                            + " and existing username: "
                            + channelIdToUserName.get(channelId));
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        System.out.println(cause.getMessage());
        cause.printStackTrace();
        context.close();
    }

}
