package menear.nclient.slate.modules.pathfinding.pathing.processing.context;

import menear.nclient.slate.modules.pathfinding.pathing.configuration.PathfinderConfiguration;
import menear.nclient.slate.modules.pathfinding.pathing.context.EnvironmentContext;
import menear.nclient.slate.modules.pathfinding.provider.NavigationPointProvider;
import menear.nclient.slate.modules.pathfinding.wrapper.PathPosition;
import java.util.Map;

public interface SearchContext {
    PathPosition getStartPathPosition();
    PathPosition getTargetPathPosition();
    PathfinderConfiguration getPathfinderConfiguration();
    NavigationPointProvider getNavigationPointProvider();
    Map<String, Object> getSharedData();
    EnvironmentContext getEnvironmentContext();
}
