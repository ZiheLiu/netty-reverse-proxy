package com.ziheliu.frontend;

import com.ziheliu.common.protocol.ProxyMessage;
import com.ziheliu.common.protocol.ProxyMessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendHandler extends ChannelInboundHandlerAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(BackendHandler.class);

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ProxyMessage proxyMessage = (ProxyMessage) msg;
    switch (proxyMessage.getType()) {
      case ProxyMessageType.BACKEND_CONNECT:
        handleBackendConnect(ctx, proxyMessage);
        break;
      case ProxyMessageType.CLIENT_DISCONNECT:
        handleClientDisconnect(proxyMessage);
        break;
      case ProxyMessageType.SERVER_DATA:
        handleResponseData(proxyMessage);
        break;
      default:
        LOGGER.error("ProxyMessage.type is invalid: {}", proxyMessage.getType());
        System.exit(-1);
    }

    super.channelRead(ctx, msg);
  }

  private void handleBackendConnect(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
    LOGGER.info("Backend connects to to watch port#{}", proxyMessage.getClientPort());

    ChannelManager.putBackendCtx(proxyMessage.getClientPort(), ctx);
    proxyMessage.getData().release();
  }

  private void handleClientDisconnect(ProxyMessage proxyMessage) {
    LOGGER.info("Client channelId#{} disconnects", proxyMessage.getClientChannelId());

    ChannelManager.getClientCtx(proxyMessage.getClientChannelId()).close();
    ChannelManager.removeClientCtx(proxyMessage.getClientChannelId());

    proxyMessage.getData().release();
  }

  private void handleResponseData(ProxyMessage proxyMessage) {
    ChannelManager.getClientCtx(proxyMessage.getClientChannelId())
        .writeAndFlush(proxyMessage.getData());
  }
}
