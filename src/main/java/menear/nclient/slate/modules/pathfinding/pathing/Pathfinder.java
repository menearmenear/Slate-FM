package menear.nclient.slate.modules.pathfinding.pathing;

import menear.nclient.slate.modules.pathfinding.pathing.context.EnvironmentContext;
import menear.nclient.slate.modules.pathfinding.pathing.result.PathfinderResult;
import menear.nclient.slate.modules.pathfinding.wrapper.PathPosition;
import java.util.concurrent.CompletionStage;

public interface Pathfinder {
    default CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target) {
        return findPath(start, target, null);
    }

    CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target,
                                               EnvironmentContext context);

    void abort();
}
