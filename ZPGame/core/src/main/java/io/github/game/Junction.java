package io.github.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Junction {
    private Vector2 position;
    private Array<PathConnection> connections;  // Vse možne povezave med progami
    private Array<String> connectedPaths;       // ID-ji vseh prog, ki se stikajo v danem križišču

    public Junction(Vector2 position) {
        this.position = position;
        this.connections = new Array<>();
        this.connectedPaths = new Array<>();
    }

    public void addConnection(PathConnection connection) {
        connections.add(connection);
        if (!connectedPaths.contains(connection.fromPathId, false)) {
            connectedPaths.add(connection.fromPathId);
        }
        if (!connectedPaths.contains(connection.toPathId, false)) {
            connectedPaths.add(connection.toPathId);
        }
    }

    // Vrne vse možne poti iz trenutne proge
    public Array<PathConnection> getAvailableConnections(String currentPathId, boolean isReversed) {
        Array<PathConnection> available = new Array<>();
        for (PathConnection conn : connections) {
            if (conn.fromPathId.equals(currentPathId) && conn.fromReversed == isReversed) {
                available.add(conn);
            }
        }
        return available;
    }

    public Vector2 getPosition() {
        return position;
    }
}

