package menear.nclient.slate.modules.pathfinding.pathing.heuristic;

import menear.nclient.slate.modules.pathfinding.wrapper.PathPosition;

public interface IHeuristicStrategy {
    double calculate(HeuristicContext context);
    double calculateTransitionCost(PathPosition from, PathPosition to);
}
