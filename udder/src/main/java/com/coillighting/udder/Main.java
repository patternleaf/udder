package com.coillighting.udder;

import com.coillighting.udder.DairyScene;
import com.coillighting.udder.ServicePipeline;


/** Application entrypoint.
 *  Start up a new Udder lighting server. Load the scene for the Boulder Dairy.
 */
public class Main {

	/** Arguments are currently ignored. TODO: configuration. */
    public static void main(String[] args) throws Exception {
        new ServicePipeline(DairyScene.create()).start();
    }

}
