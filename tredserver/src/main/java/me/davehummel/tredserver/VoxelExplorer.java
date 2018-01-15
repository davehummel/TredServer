package me.davehummel.tredserver;


import me.davehummel.tredserver.web.WebSocketServer;

public class VoxelExplorer
{


    public static void main1( String[] args )
    {

        WebSocketServer server = new WebSocketServer();

        Runnable launchServer = () -> {
            server.start();
        };
        new Thread(launchServer).start();

    }
}
