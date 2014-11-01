package com.coillighting.udder;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;

import org.boon.json.JsonFactory;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

/** The HTTP controller for Udder's Simple-brand webserver.
 *  Receives requests, translates them into commands, and responds as needed.
 */
public class HttpServiceContainer implements Container {

    protected Queue<Command> queue; // feed requests to this queue
    protected Map<String, Class> commandMap; // translate JSON to command object
    protected int requestIndex = 0; // TEMP count requests to assist debugging
    protected boolean verbose = false;

    public HttpServiceContainer(Queue<Command> queue, Map<String, Class> commandMap) {
        if(queue==null) {
            throw new NullPointerException(
                "HttpServiceContainer requires a Queue for consuming commands.");
        } else if(commandMap==null) {
            throw new NullPointerException(
                "HttpServiceContainer requires a commandMap for dispatching commands.");
        }
        this.queue = queue;
        this.commandMap = commandMap;
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
    public Command createCommand(Request request) throws UnsupportedEncodingException {
        Command command = null;
        Query query = request.getQuery();
        Path path = request.getPath();
        String route = path.toString();
        Class stateClass = this.commandMap.get(route);
        if(stateClass == null) {
            this.log("No route for path: " + route);
            this.log("Available routes:\n    "
                + Util.join(Util.sorted(this.commandMap.keySet()), "\n    "));
        } else {
            String rawState = query.get("state");
            if(rawState == null) {
                this.log("The 'state' request param is required for " + route);
            } else {
                String json = URLDecoder.decode(rawState, "UTF-8");
                if(json == null) {
                    this.log("Failed to URL-decode a raw JSON string for " + route);
                }
                Object state = JsonFactory.fromJson(json, stateClass);
                if(state == null) {
                    this.log("Failed to deserialize a JSON command of length "
                        + json.length() + " for path " + route);
                } else if(state.getClass() == stateClass) {
                    command = new Command(route, state);
                } else {
                    this.log("Failed to convert a command of length "
                        + json.length() + " for path " + route + " into a "
                        + stateClass.getSimpleName() + ".");
                }
            }
        }
        return command;
    }

    public void handle(Request request, Response response) {

        // To see what is happening:
        // this.log(this.formatRequest(request));

        // NOTE For asynch response mode, see "asynchronous services" here:
        // http://www.simpleframework.org/doc/tutorial/tutorial.php

        try {
            int index = this.requestIndex;
            ++this.requestIndex; // Increment before possible exception.

            String response_body ="OK [" + index + "]";

            PrintStream body = response.getPrintStream();
            long time = System.currentTimeMillis();

            response.setValue("Content-Type", "text/plain");
            response.setValue("Server", "Udder/1.0 (Simple 4.0)");
            response.setValue("Access-Control-Allow-Origin", "*");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            body.println(response_body);
            body.close();

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
