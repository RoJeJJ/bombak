package com.roje.test.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class NettyClient implements Runnable{


    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                0,2,-2,2));
                        pipeline.addLast("codec", new ClientMessageCodec());
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast(new ClientTestHandler());
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,2000);
        ChannelFuture future = bootstrap.connect("127.0.0.1",9000).awaitUninterruptibly();
        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
            log.info("连接关闭");
        }
    }

    @PostConstruct
    public void start(){
        new Thread(this).start();
    }

    class ClientMessageCodec extends ByteToMessageCodec<InboundMessage> {

        @Override
        protected void encode(ChannelHandlerContext ctx, InboundMessage msg, ByteBuf out) throws Exception {
            out.writeShort(msg.totalLen());
            out.writeInt(msg.getSerial());
            out.writeLong(msg.getTimestamp());
            out.writeShort(msg.getServiceType());
            out.writeShort(msg.getMessageId());
            out.writeBytes(msg.getContent());
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            OutboundMessage message = new OutboundMessage();
            message.setSerial(in.readInt());
            message.setMessageId(in.readShort());
            message.setServiceType(in.readShort());
            message.setTimestamp(in.readLong());
            message.setErrorCode(in.readInt());
            int len = in.readableBytes();
            byte[] bytes = new byte[len];
            in.readBytes(bytes);
            message.setContent(bytes);
            out.add(message);
        }
    }
}
