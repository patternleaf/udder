package com.coillighting.udder;

import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

import org.boon.json.JsonFactory;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import com.coillighting.udder.Command;


/** The HTTP controller for Udder's Simple-brand webserver.
 *  Receives requests, translates them into commands, and responds as needed.
 */
public class HttpServiceContainer implements Container {

    private Queue<Command> queue; // feed requests to this queue
    private boolean verbose = false;
    private int requestIndex = 0; // TEMP count requests to assist debugging

    public HttpServiceContainer(Queue<Command> queue) {
        if(queue==null) {
            throw new NullPointerException(
                "HelloWorldServer requires a Queue for consuming commands.");
        }
        this.queue = queue;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean getVerbose() {
        return this.verbose;
    }

    /** Format the HTTP request path as a multiline string. */
    public String formatRequest(Request request) {
        Query query = request.getQuery();

        // poss. optimization, but how to use it?:
        boolean persistent = request.isKeepAlive();
        Path path = request.getPath();
        String directory = path.getDirectory();
        String name = path.getName();
        String[] segments = path.getSegments();

        return "Query:     " + query + (persistent ? " (persistent)\n" : "\n")
            + "Path:      " + path + " (" + segments.length + " segments)\n"
            + "Directory: " + directory + "\n"
            + "Name:      " + name + "\n";
    }

    // Brainstorming: URL examples (TODO)
    // Send JSON nulls to keep old values.
    // /mixer0/layer0/effect?state=...json...
    // /mixer0/layer1/effect?state=...json...
    // /mixer0/timer?state=...json... ?? or does this belong to a top-level timer? probably.
    // /mixer0?state=...json...  // synchronized switchup of levels

    /** TODO This is a dairy-specific routing table. Figure out how to make it
     *  generic, or move it into a DairyScene-specific class. For now we'll
     *  hardcode this for simplicity.
     *
     *  Example command-line test query:
     *  curl --data "state={\"r\":1.0,\"g\":0.5,\"b\":0.25}" localhost:8080/mixer0/layer0
     */
    public Command createCommand(Request request) {
        Command command = null;
        Object state = null;
        Integer destination = null; // EXPERIMENTAL!

        Query query = request.getQuery();
        String rawState = query.get("state");
        String json = URLDecoder.decode(rawState);
        Path path = request.getPath();
        String directory = path.getDirectory();
        String[] segments = path.getSegments();

        if(segments.length >= 1) {
            if(segments[0].equals("mixer0")) {
                destination = -1;
                if(segments.length >= 2) {

                    // TODO pass in layer effects' stateclass array to this object
                    // instead of hardcoding layer
                    Class stateClass = null;
                    String key = segments[1];
                    if(key.equals("layer0")) {
                        // Color or color cycling modulation.
                        // Route to the Background layer.
                        stateClass = MonochromeEffect.getStateClass();
                        destination = 0;
                    } else if(key.equals("layer1")) {
                        // Color gradient or color cycling automation.
                        // Route to the Rainbow Stupidity layer.
                        // TODO: gradient, not monochrome here.
                        stateClass = MonochromeEffect.getStateClass();
                        destination = 1;
                    } else if(key.equals("layer2")) {
                        // Route eric's pixels to the External Input layer.
                        stateClass = RasterEffect.getStateClass();
                        destination = 2;
                    } else if(key.equals("layer3")) {
                        // Color or color cycling modulation.
                        // Route to the Gel layer.
                        stateClass = MonochromeEffect.getStateClass();
                        destination = 3;
                    }

                    if(stateClass != null) {
                        state = JsonFactory.fromJson(json, stateClass);
                    }
                }
            } else if(segments[0].equals("timer0")) {
                // Timebase modulation. Route to ShowRunner.
                destination = -2;
            }
        }

        if(state == null) {
            this.log("No route for path: " + path);
        } else {
            // TODO - route the command to a specific layer
            command = new Command(state, destination);
            this.log("command=Command("+command.getValue()+", " + command.getDestination() + ")"); // TEMP
        }
        return command;
    }

    public void handle(Request request, Response response) {

        // To see what is happening:
        // this.log(this.formatRequest(request));

        // NOTE For asynch response mode, see "asynchronous services" here:
        // http://www.simpleframework.org/doc/tutorial/tutorial.php

        this.log("handle()");
        try {
            final int index = this.requestIndex;
            ++this.requestIndex;

            final String response_body =
                "Hello, this is the Simple webserver. [" + index + "]";

            PrintStream body = response.getPrintStream();
            long time = System.currentTimeMillis();

            response.setValue("Content-Type", "text/plain");
            response.setValue("Server", "HelloWorld/1.0 (Simple 4.0)");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            body.println(response_body);
            body.close();

            // Simulate parsing of a request payload
            // Command simCommand = new Command(index);
            // String json = JsonFactory.toJson(simCommand);
            // this.log(json);
            // Command command = JsonFactory.fromJson(json, Command.class);

            Command command = this.createCommand(request);
            if(command == null) {
                // TODO error response - 404
            } else {
                boolean accepted = this.queue.offer(command);
                if(this.verbose) {
                    if(accepted) {
                        this.log(response_body);
                    } else {
                        this.log("Request " + index + " dropped. No room in queue.");
                    }
                }
            }
        } catch(ClassCastException e) {
            // TODO attempt to respond with error message + code?
            this.log("Error (malformed JSON?): " + e.toString());

            // TODO log this, don't print it, this runs in a separate thread
            e.printStackTrace();

            // Save you hours of misguided debugging by explaining this error.
            // Example of an incomprehensible parser error, with no real indication
            // of the problem: I tried to parse "foo" and caught an error saying,
            // "java.lang.ClassCastException: org.boon.core.value.CharSequenceValue
            // cannot be cast to com.coillighting.udder.Command".
            this.log("Note that ClassCastExceptions are sometimes really just "
                + "JSON syntax errors, but the Boon parser's error messages "
                + "are inarticulate.");

        } catch(Exception e) {
            // TODO attempt to respond with error message + code?
            this.log("Error: " + e.toString());
            e.printStackTrace(); // FIXME see above
        }
    }

    public void log(Object message) {
        System.err.println(message);
    }

}
