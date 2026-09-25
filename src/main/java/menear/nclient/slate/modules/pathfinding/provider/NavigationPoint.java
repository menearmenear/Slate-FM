package menear.nclient.slate.modules.pathfinding.provider;

public interface NavigationPoint {
    boolean isTraversable();
    boolean hasFloor();
    double getFloorLevel();
    boolean isClimbable();
    boolean isLiquid();
}
