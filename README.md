udder
=====

HTTP server, animators, mixer, and Open Pixel Control (OPC) renderer for a 2014 public lighting installation at Boulder's Dairy Center for the Arts.


Getting Started
---------------

You need to install JDK 1.7+ and Maven 3.2.3+.

To help bootstrap development, this repository currently comes with a copy of Maven 3.2.3 plus several jars required by the application. Eventually these resources will disappear. If you've already installed the JDK and Maven on your devbox, you don't have to use them. Mac OS X users may `source env.sh` to get set up rapidly.

Once Maven and the JDK are installed in your current environment (see `env.sh` for examples), `cd udder` and run either the `build` script or `build_clean`.

After the build script succeeds, you may start the server using the `serve` script in the same directory. By default the server listens on [http://localhost:8080](http://localhost:8080) and attempt to renders at 100fps max.


Udder Architecture in a Nutshell
--------------------------------

The class com.coillighting.udder.ServicePipeline assembles the application components into a webserver capable of driving OPC RGB pixel lighting instruments. Here's how it works:

* A human operator makes HTTP requests from a web page in a browsers.
* A SimpleFramework (v5.1.5) server listens for incoming HTTP requests. Request payloads are curently expected to be JSON structures.
* A Boon (v0.23) JSON decoder converts each request payload into a Command object.
* Commands are inserted into a concurrent queue, the command queue.
* In one separate thread, a ShowRunner object runs a periodic event loop, one event per frame. In each frame, commands are drained from the queue and processed.
* A command might mutates the state of the ShowRunner or one of its children (Mixer, Animator, or Renderer)
* When there are no more commands to process, the ShowRunner animates, composites, and renders the current frame.
* The current frame is then inserted into a second concurrent queue, the output queue.
* In another separate thread, an OPCBroadcaster object blocks until the ShowRunner sends it a new frame via the output queue. Upon the arrival of a new frame, the OPCBroadcaster transmits the frame to a remote OPC listener.
* The remote OPC listener (probably a stock OPC daemon process) writes the contents of the frame to downstream RGB pixel devices, most likely an array of LED strips.
* Pixel devices emit photons, which enter the eyes of the audience.


Apache License, v2.0
====================

Copyright 2014 Michael Bissell

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
