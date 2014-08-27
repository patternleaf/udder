udder
=====

HTTP server, animators, mixer, and Open Pixel Control (OPC) renderer for a 2014 public lighting installation at Boulder's Dairy Center for the Arts.


Getting Started
---------------

You need to install JDK 1.7+ and Maven 3.2.3+.

To help bootstrap development, this repository currently comes with a copy of Maven 3.2.3 plus several jars required by the application. Eventually these resources will disappear. If you've already installed the JDK and Maven on your devbox, you don't have to use them. Mac OS X users may `source env.sh` to get set up rapidly.

Once Maven and the JDK are installed in your current environment (see `env.sh` for examples), `cd udder` and run either the `build` script or `build_clean`.

After the build script succeeds, you may start the server using the `serve` script in the same directory. By default the server listens on [http://localhost:8080](http://localhost:8080) and attempts to render at 100fps max.


Udder Architecture in a Nutshell
--------------------------------

The class com.coillighting.udder.ServicePipeline assembles the application components into a webserver capable of driving OPC RGB pixel lighting instruments. The coarse grained pipeline has three stages, with network IO:

Network => HttpServiceContainer => ShowRunner => OpcTransmitter => Network

Details:
* A human **lighting designer** initiates HTTP **requests** from a web page in a browser.
* A SimpleFramework (v5.1.5) server listens for incoming HTTP **requests**. Request **payloads** are curently expected to be JSON structures.
* A Boon (v0.23) JSON decoder converts each request **payload** into a **command** object.
* **Commands** are inserted into a concurrent queue, the **command queue**.
* In one separate thread, a ShowRunner object runs a periodic event loop, one event per frame. In each frame, **commands** are drained from the **command queue** and processed.
* A **command** normally mutates the state of the ShowRunner or one of its children (the Mixer, an Animator, or the Renderer).
* When there are no more commands to process, the ShowRunner animates, composites, and renders the current **frame**.
* The current **frame** is then inserted into a second concurrent queue, the **output queue**.
* In another separate thread, an OpcTransmitter object blocks until the ShowRunner sends it a new **frame** via the **output queue**. Upon the arrival of a new frame, the OpcTransmitter transmits the frame via TCP/IP to a remote OPC listener.
* The remote OPC listener (normally a stock OPC daemon process) writes the contents of the incoming **frame** to downstream RGB **pixel devices**, normally an array of LED strips.
* **Pixel devices** emit **photons**.
* **Photons** enter the eyes of the audience and of the **lighting designer**. Thus the cycle is complete.

Important points:
* Udder has a **multithreaded** architecture, coupled by two concurrent queues.
* Data flows down **one** non-branching path, through the three linked stages of the pipeline.
* The ShowRunner processes commands and renders frames **asynchronously** with respect to incoming requests.
* The OpcTransmitter broadcasts frames **asynchronously** with respect to the renderer.


Dependency Links
----------------
Apache Maven:
* [Maven - official website](http://maven.apache.org/)
* [Maven - source repository](https://git-wip-us.apache.org/repos/asf?p=maven.git)

The Simple webserver:
* [Simple - official website](http://www.simpleframework.org/)
* [Simple - repository](http://sourceforge.net/projects/simpleweb/)

Boon, which we include for its JSON support:
* [Boon - tutorial (beware: some docs may be out of date)](https://github.com/RichardHightower/boon/wiki)
* [Boon - repository](https://github.com/RichardHightower/boon)

Open Pixel Control (OPC), our chosen network protocol for lighting control:
* [OPC - official website](http://openpixelcontrol.org/)
* [OPC - repository](https://github.com/zestyping/openpixelcontrol)

Micah Scott's Fade Candy device, the LED pixel driver we use:
* [FadeCandy - repository](https://github.com/scanlime/fadecandy)
* [FadeCandy - Sparkfun catalog page](https://www.sparkfun.com/products/12821)
* [FadeCandy - Adafruit catalog page](http://www.adafruit.com/products/1689)

The official API Javadocs for certain "special sauce" coupler components:
* [java.util.concurrent.ConcurrentLinkedQueue](http://docs.oracle.com/javase/7/docs/api/index.html?java/util/concurrent/ConcurrentLinkedQueue.html)
* [java.util.concurrent.LinkedBlockingQueue](http://docs.oracle.com/javase/7/docs/api/index.html?java/util/concurrent/LinkedBlockingQueue.html)

3rd party libraries are covered by their own licenses (mostly Apache 2.0, MIT, or equivalent). Everything else in this repository is released under the following license:


Apache License, v2.0
--------------------

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
