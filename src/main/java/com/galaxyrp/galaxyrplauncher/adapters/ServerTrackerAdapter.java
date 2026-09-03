package com.galaxyrp.galaxyrplauncher.adapters;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerTrackerAdapter {
    private static final int DEFAULT_PORT = 29070;
    private static final int SOCKET_TIMEOUT_MILLIS = 3000;
    private static final Pattern PLAYER_PATTERN =
            Pattern.compile("^\\s*-?\\d+\\s+-?\\d+\\s+\"(.*)\"\\s*$");

    public JediAcademyServerInfo getServerInfo(String ipAddress) throws IOException {
        ServerAddress serverAddress = parseServerAddress(ipAddress);
        InetAddress address = InetAddress.getByName(serverAddress.host());
        byte[] request = "\u00ff\u00ff\u00ff\u00ffgetstatus\n".getBytes(StandardCharsets.ISO_8859_1);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
            socket.send(new DatagramPacket(request, request.length, address, serverAddress.port()));

            byte[] responseBuffer = new byte[8192];
            DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
            try {
                socket.receive(response);
            } catch (SocketTimeoutException exception) {
                throw new SocketTimeoutException(
                        "No Jedi Academy server response received from " + ipAddress);
            }

            if (response.getLength() < 4) {
                throw new IOException("Malformed Jedi Academy server response");
            }
            String responseText = new String(
                    response.getData(), response.getOffset() + 4, response.getLength() - 4, StandardCharsets.UTF_8);
            return parseServerInfo(responseText);
        }
    }

    private JediAcademyServerInfo parseServerInfo(String response) throws IOException {
        if (!response.startsWith("statusResponse")) {
            throw new IOException("Unexpected Jedi Academy server response");
        }

        String hostName = parseHostName(response);
        List<String> playerNames = new ArrayList<>();

        for (String line : response.split("\\R")) {
            Matcher playerMatcher = PLAYER_PATTERN.matcher(line);
            if (playerMatcher.matches()) {
                playerNames.add(playerMatcher.group(1));
            }
        }

        return new JediAcademyServerInfo(hostName, playerNames.size(), playerNames);
    }

    private String parseHostName(String statusSection) {
        String[] lines = statusSection.split("\\R", 2);
        if (lines.length < 2) {
            return "";
        }

        String[] serverVariables = lines[1].split("\\\\", -1);
        for (int index = 1; index + 1 < serverVariables.length; index += 2) {
            if ("sv_hostname".equals(serverVariables[index])) {
                return serverVariables[index + 1];
            }
        }
        return "";
    }

    private ServerAddress parseServerAddress(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Server IP address must not be blank");
        }

        String host = value.trim();
        int port = DEFAULT_PORT;

        if (host.startsWith("[")) {
            int closingBracket = host.indexOf(']');
            if (closingBracket < 0) {
                throw new IllegalArgumentException("Invalid bracketed server address: " + value);
            }
            String portPart = host.substring(closingBracket + 1);
            host = host.substring(1, closingBracket);
            if (!portPart.isEmpty()) {
                if (!portPart.startsWith(":")) {
                    throw new IllegalArgumentException("Invalid server port: " + value);
                }
                port = parsePort(portPart.substring(1), value);
            }
        } else {
            int separator = host.lastIndexOf(':');
            if (separator > 0 && host.indexOf(':') == separator) {
                String portPart = host.substring(separator + 1);
                if (portPart.chars().allMatch(Character::isDigit)) {
                    port = parsePort(portPart, value);
                    host = host.substring(0, separator);
                }
            }
        }

        return new ServerAddress(host, port);
    }

    private int parsePort(String value, String originalAddress) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Server port must be between 1 and 65535: " + originalAddress);
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid server port: " + originalAddress, exception);
        }
    }

    public record JediAcademyServerInfo(
            String hostName,
            int playerCount,
            List<String> playerNames) {
        public JediAcademyServerInfo {
            playerNames = Collections.unmodifiableList(new ArrayList<>(playerNames));
        }
    }

    private record ServerAddress(String host, int port) {
    }
}
