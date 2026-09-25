package menear.nclient.slate.modules.pathfinding.provider;

import menear.nclient.slate.modules.pathfinding.pathing.context.EnvironmentContext;
import menear.nclient.slate.modules.pathfinding.wrapper.PathPosition;

public interface NavigationPointProvider {
    default NavigationPoint getNavigationPoint(PathPosition position) {
        return getNavigationPoint(position, null);
    }

    NavigationPoint getNavigationPoint(PathPosition position, EnvironmentContext environmentContext);
}
