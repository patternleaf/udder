package com.coillighting.udder;

import com.coillighting.udder.DairyScene;
import com.coillighting.udder.ServicePipeline;


/** Start up a new Udder lighting server. Load the scene for the Boulder Dairy.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        new ServicePipeline(DairyScene.create()).start();
    }

}
