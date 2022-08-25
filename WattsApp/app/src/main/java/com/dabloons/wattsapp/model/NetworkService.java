package com.dabloons.wattsapp.model;

import java.net.InetAddress;

public class NetworkService {
    private String type;
    private String name;
    private int port;
    private InetAddress host;

    public NetworkService(String type, String name, int port, InetAddress host) {
        this.type = type;
        this.name = name;
        this.port = port;
        this.host = host;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getHost() {
        return host;
    }

    public void setHost(InetAddress host) {
        this.host = host;
    }
}
