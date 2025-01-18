package io.github.game;

public class PathConnection {
    public String fromPathId;     // ID izhodne proge
    public String toPathId;       // ID ciljne proge
    public boolean fromReversed;  // Ali je izhodna proga obrnjena
    public boolean toReversed;    // Ali je ciljna proga obrnjena

    public PathConnection(String fromPathId, String toPathId,
                          boolean fromReversed, boolean toReversed) {
        this.fromPathId = fromPathId;
        this.toPathId = toPathId;
        this.fromReversed = fromReversed;
        this.toReversed = toReversed;
    }
}
