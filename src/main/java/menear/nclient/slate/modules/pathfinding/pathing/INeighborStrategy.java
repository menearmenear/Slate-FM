package menear.nclient.slate.modules.pathfinding.pathing;

import menear.nclient.slate.modules.pathfinding.wrapper.PathPosition;
import menear.nclient.slate.modules.pathfinding.wrapper.PathVector;

@FunctionalInterface
public interface INeighborStrategy {
    Iterable<PathVector> getOffsets();

    default Iterable<PathVector> getOffsets(PathPosition currentPosition) {
        return getOffsets();
    }
}
