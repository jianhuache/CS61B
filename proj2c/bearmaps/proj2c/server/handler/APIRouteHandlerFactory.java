package bearmaps.proj2c.server.handler;

import bearmaps.proj2c.server.handler.impl.*;

import java.util.HashMap;
import java.util.Map;


public class APIRouteHandlerFactory {

    public static final Map<String, APIRouteHandler> handlerMap;

    static {
        handlerMap = new HashMap<>();
        handlerMap.put("raster", new RasterAPIHandler());
        handlerMap.put("route", new RoutingAPIHandler());
        handlerMap.put("clear_route", new ClearRouteAPIHandler());
        handlerMap.put("search", new SearchAPIHandler());
        handlerMap.put("", new RedirectAPIHandler());
    }


}
