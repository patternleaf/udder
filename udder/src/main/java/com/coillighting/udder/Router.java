package com.coillighting.udder;

import java.util.HashMap;
import java.util.Map;

import com.coillighting.udder.effect.Effect;
import com.coillighting.udder.mix.Layer;
import com.coillighting.udder.mix.Mixable;
import com.coillighting.udder.mix.Mixer;

/** Route command names (ordinarily derives from HTTP request URLs) to their
 *  consumers in a Mixer tree.
 */
public class Router {

	protected HashMap<String, Effect> routes = null;

	public Router() {
		this.routes = new HashMap<String, Effect>();
	}

	public Effect get(String path) {
		return this.routes.get(path);
	}

	/** Add standard routes for the mixer, its layers, and its layers' effects
	 *  (if any).
	 */
	public void addRoutes(String token, Mixer mixer) {
		if(token == null) {
			throw new NullPointerException("Null routing token.");
		}

		String mixerKey = "/" + token + "";
		routes.put(mixerKey, mixer);

		int len = mixer.size();
		for(int i=0; i<len; i++) {
			Mixable layer = mixer.getLayer(i);
			String layerKey = mixerKey + "/layer" + i;
			routes.put(layerKey, layer);

			if(layer instanceof Layer) {
				Effect effect = ((Layer) layer).getEffect();
				if(effect != null) {
					String effectKey = layerKey + "/effect";
					routes.put(effectKey, effect);
				}
			}
		}
	}

	/** Router.routes contains non-threadsafe direct references to Mixers and
	 *  Layers. In order to share routing information with other threads, a new,
	 *  strictly symbolic table is required. The caller owns the returned map.
	 *  The keys of the symbolic routing table are identical to the keys of
	 *  the private Route.routes table, but the values are classes that are used
	 *  by the HttpServiceContainer to deserialize JSON commands into Java
	 *  command objects.
	 */
	public Map<String, Class> getCommandMap() {
		Map<String, Class> commandMap = new HashMap<String, Class>();

		for(Map.Entry<String, Effect> entry : this.routes.entrySet()) {
			Effect v = entry.getValue();
			Class stateClass = v.getStateClass();
			if(stateClass == null) {
				throw new NullPointerException("Null stateClass from " + v);
			} else {
				commandMap.put(entry.getKey(), stateClass);
			}
		}
		return commandMap;
	}

}
