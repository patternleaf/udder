udder
=====

HTTP server, animators, mixer, and Open Pixel Control (OPC) renderer for a 2014 public lighting installation at Boulder's Dairy Center for the Arts.

Getting Started
---------------

You need to install JDK 1.7 or later and Maven 3.2.3 or later.

To help bootstrap development, this repository currently comes with a copy of Maven 3.2.3 and several required jars. Eventually these resources will disappear. If you've already installed the JDK and Maven on your devbox, you don't have to use them. Mac users may `source env.sh` to get set up rapidly.

Once Maven and the JDK are installed in your current environment (see env.sh for examples), `cd udder` and run the `build` or `build_clean` scripts.

After the build script succeeds, there is a `serve` script in the same directory. By default the server listens on [link](http://localhost:8080) and attempt to renders at 100fps max.


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
