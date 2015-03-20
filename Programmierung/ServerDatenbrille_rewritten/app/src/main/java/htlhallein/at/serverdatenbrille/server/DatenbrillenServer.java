package htlhallein.at.serverdatenbrille.server;

import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.Charset;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.activityHandler.ActivityListener;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventListener;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventObject;
import htlhallein.at.serverdatenbrille.event.scroll.ScrollEventDirection;
import htlhallein.at.serverdatenbrille.event.scroll.ScrollEventHandler;
import htlhallein.at.serverdatenbrille.event.scroll.ScrollEventListener;
import htlhallein.at.serverdatenbrille.event.scroll.ScrollEventObject;
import htlhallein.at.serverdatenbrille.server.wifi.WifiApManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;

public class DatenbrillenServer implements ActivityListener, DatapointEventListener, ScrollEventListener {

    private static final int DEFAULT_PORT = 12345;
    private Server server;
    private WifiApManager myWifiManager;
    private Thread serverThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ScrollEventHandler.addListener(this);
        DatapointEventHandler.addListener(this);

        server = new Server(DEFAULT_PORT);
        serverThread = new Thread(this.server);
        serverThread.start();

        this.myWifiManager = new WifiApManager(MainActivity.getContext());

        Log.d("DatenbrillenServer", "WIFI-AP starting ...");
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).getString(MainActivity.getContext().getString(R.string.preferences_preference_wifihotspot_name), MainActivity.getContext().getString(R.string.preferences_preference_wifihotspot_name_default));
        wifiConfiguration.preSharedKey = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).getString(MainActivity.getContext().getString(R.string.preferences_preference_wifihotspot_password), MainActivity.getContext().getString(R.string.preferences_preference_wifihotspot_password_default));
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        myWifiManager.setWifiApEnabled(wifiConfiguration, true);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        Log.d("DatenbrillenServer", "Stopping Server ...");
        server.stop();

        Log.d("DatenbrillenServer", "WIFI-AP stopping ... ");
        myWifiManager.setWifiApEnabled(null, false);
    }

    @Override
    public void showQRCode() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void datapointEventOccurred(DatapointEventObject eventObject) {
        try {
            JSONObject object = new JSONObject();
            object.put("operationType", "HTML");
            byte[] jsonMessageBase64;
            try {
                jsonMessageBase64 = Base64.encode(eventObject.getHtmlText().getBytes(), Base64.NO_WRAP);
            } catch (Exception e) {
                Log.d("DatenbrillenServer", "Failed to base64 encode the message ...", e);
                return;
            }
            String htmlTextEncoded = new String(jsonMessageBase64);
            object.put("HTML", htmlTextEncoded);
            this.server.writeMessage(object.toString());
        } catch (Exception e) {
            Log.v("DatenbrillenServer", "JSON Object Error - Datapoint", e);
        }
    }

    @Override
    public void scrollEventOccurred(ScrollEventObject eventObject) {
        try {
            JSONObject object = new JSONObject();
            object.put("operationType", "SCROLL");
            if (eventObject.getDirection() == ScrollEventDirection.UP)
                object.put("direction", "UP");
            else
                object.put("direction", "DOWN");
            object.put("percent", eventObject.getPercent());
            this.server.writeMessage(object.toString());
        } catch (Exception e) {
            Log.d("DatenbrillenServer", "JSON Object Error - Scrollevent", e);
        }
    }


    protected class Server implements Runnable {
        private final int port;
        private final EventLoopGroup newClientListenerGroup;
        private final EventLoopGroup clientWorkerGroup;
        private final ServerInitializer serverInitializer;
        private final ChannelGroup channelGroup;
        private final ServerBootstrap serverBootstrap;

        public Server(int port) {
            this.port = port;
            this.newClientListenerGroup = new NioEventLoopGroup();
            this.clientWorkerGroup = new NioEventLoopGroup();
            this.serverInitializer = new ServerInitializer();
            this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

            this.serverBootstrap = new ServerBootstrap()
                    .group(newClientListenerGroup, clientWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(this.serverInitializer)
                    .childOption(ChannelOption.SO_REUSEADDR, true);
        }

        @Override
        public void run() {
            try {
                Log.d("DatenbrillenServer", "Starting Server ...");
                serverBootstrap.bind(this.port).sync().channel().closeFuture().sync();
                Log.d("DatenbrillenServer", "Stopped Server ...");
            } catch (InterruptedException e) {
                Log.d("DatenbrillenServer", "Server interrupted ... Why?", e);
            } finally {
                newClientListenerGroup.shutdownGracefully();
                clientWorkerGroup.shutdownGracefully();
                channelGroup.close();
                System.gc();
            }
        }

        public void stop() {
            Log.d("DatenbrillenServer", "Server Shutdown-progress started ...");
            // this.channelGroup.close();
            Future<?> newClient = newClientListenerGroup.shutdownGracefully();
            Future<?> clientWorker = clientWorkerGroup.shutdownGracefully();
            try {
                newClient.await();
                clientWorker.await();
            } catch (InterruptedException ignore) {
            }
            Log.d("DatenbrillenServer", "Server Shutdown-progress finished ...");
        }

        public void writeMessage(String message) {
            if (!message.endsWith("\n"))
                message += "\n";

            Log.d("DatenbrillenServer", "Sending data to \"" + this.channelGroup.size() + "\" clients");
            for (Channel channel : this.channelGroup) {
                channel.write(message);
                channel.flush();
            }
        }

        protected class ServerInitializer extends ChannelInitializer<SocketChannel> {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                pipeline.addLast("decoder", new StringDecoder(Charset.forName("UTF-8")));
                pipeline.addLast("encoder", new StringEncoder(Charset.forName("UTF-8")));

                pipeline.addLast("handler", new ServerChannelHandler());
            }
        }

        protected class ServerChannelHandler extends ChannelInboundHandlerAdapter {
            @Override
            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                channelGroup.add(ctx.channel());
                Log.d("DatenbrillenServer", "New Client connected! Clients: \"" + channelGroup.size() + "\"");
            }

            @Override
            public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                channelGroup.remove(ctx.channel());
                Log.d("DatenbrillenServer", "Client disconnected! Clients: \"" + channelGroup.size() + "\"");
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Log.d("DatenbrillenServer", "Message received: \"" + msg + "\"");
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                Log.d("DatenbrillenServer", "Exception occurred ... " + cause.getMessage(), cause);

                Channel source = ctx.channel();
                source.close();
                channelGroup.remove(source);
            }
        }
    }
}