package com.roje.bombak.gate.service;

import com.roje.bombak.common.service.ServerService;
import com.roje.bombak.common.thread.NamedThreadFactory;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.gate.config.GateProperties;
import com.roje.bombak.gate.handler.GateMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author pc
 */
@Slf4j
@Component
public class GateNettyTcpServiceImpl implements ServerService {

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private final GateProperties gateProperties;

    private final EventExecutor executor = new DefaultEventExecutor(new NamedThreadFactory("netty"));

    private GateMessageHandler gateMessageHandler;

    private EventExecutorGroup biz;

    public GateNettyTcpServiceImpl(
            GateProperties gateProperties,
            GateMessageHandler gateMessageHandler) {
        this.gateProperties = gateProperties;
        this.gateMessageHandler = gateMessageHandler;
    }


    @Override
    public void stop() {
        int quietPeriod = 5;
        int timeout = 30;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        if (boss != null) {
            boss.shutdownGracefully(quietPeriod, timeout, timeUnit);
        }
        if (worker != null) {
            worker.shutdownGracefully(quietPeriod, timeout, timeUnit);
        }
        if (biz != null) {
            biz.shutdownGracefully(quietPeriod, timeout, timeUnit);
        }
    }

    @Override
    public void start() {
        executor.execute(this::run);
    }

    private void run() {
        boss = new NioEventLoopGroup(1, new NamedThreadFactory("boss"));
        worker = new NioEventLoopGroup(0, new NamedThreadFactory("worker"));
        biz = new DefaultEventExecutorGroup(gateProperties.getNetty().getExecutorThreadPoolSize(),
                new NamedThreadFactory("user"));
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ProtobufVarint32FrameDecoder())
                                .addLast(new ProtobufDecoder(ServerMsg.C2SMessage.getDefaultInstance()))
                                .addLast(new ProtobufVarint32LengthFieldPrepender())
                                .addLast(new ProtobufEncoder())
                                .addLast("log", new LoggingHandler(LogLevel.INFO))
                                .addLast("idle", new IdleStateHandler(
                                        gateProperties.getNetty().getReaderIdleTimeSeconds(),
                                        gateProperties.getNetty().getWriterIdleTimeSeconds(), gateProperties.getNetty().getAllIdleTimeSeconds()))
                                .addLast(biz, "gateMessage", gateMessageHandler);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        int port = gateProperties.getNetty().getPort();
        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("tcp server started,listen port:{}", port);


            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("tcp server start failed", e);
        } finally {
            log.info("tcp server stop");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
