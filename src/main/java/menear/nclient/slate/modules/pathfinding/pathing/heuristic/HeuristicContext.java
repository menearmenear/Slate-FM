package menear.nclient.slate.modules.pathfinding.pathing.heuristic;

import menear.nclient.slate.modules.pathfinding.pathing.PathfindingProgress;
import menear.nclient.slate.modules.pathfinding.wrapper.PathPosition;

public final class HeuristicContext {
    public final PathfindingProgress pathfindingProgress;
    public final HeuristicWeights heuristicWeights;

    public HeuristicContext(PathfindingProgress pathfindingProgress, HeuristicWeights heuristicWeights) {
        this.pathfindingProgress = pathfindingProgress;
        this.heuristicWeights = heuristicWeights;
    }

    public HeuristicContext(PathPosition position, PathPosition startPosition,
                             PathPosition targetPosition, HeuristicWeights heuristicWeights) {
        this(new PathfindingProgress(startPosition, position, targetPosition), heuristicWeights);
    }

    public PathPosition position()      { return pathfindingProgress.current; }
    public PathPosition startPosition() { return pathfindingProgress.start; }
    public PathPosition targetPosition(){ return pathfindingProgress.target; }
}
