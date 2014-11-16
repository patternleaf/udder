package com.coillighting.udder.infrastructure;

import java.io.*;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Queue;

import org.boon.json.JsonFactory;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import com.coillighting.udder.Util;

/** The HTTP controller for Udder's Simple-brand webserver.
 *  Receives requests, translates them into commands, and responds as needed.
 */
public class HttpServiceContainer implements Container {

    protected boolean verbose = true;
    protected Queue<Command> queue; // feed requests to this queue
    protected Map<String, Class> commandMap; // translate JSON to command object
    protected int requestIndex = 0; // Count requests to assist debugging (for now)

    public HttpServiceContainer(Queue<Command> queue, Map<String, Class> commandMap) {
        if(queue == null) {
            throw new NullPointerException(
                    "HttpServiceContainer requires a Queue for consuming commands.");
        } else if(commandMap == null) {
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
        return verbose;
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

    /** Return a Command object bearing a payload relevant to the given route,
     * or die trying and throw an exception. Never return null.
     *
     * Example command-line test query:
     * curl --data "state={\"r\":1.0,\"g\":0.5,\"b\":0.25}" localhost:8080/mixer0/layer0
     */
    public Command createCommand(Request request)
            throws UnsupportedEncodingException,
            RoutingException,
            CommandParserException,
            ClassCastException // often a Boon JSON parser exception in disguise
    {
        Command command;
        Query query = request.getQuery();
        Path path = request.getPath();
        String route = path.toString();
        Class stateClass = this.commandMap.get(route);

        if (stateClass == null) {
            throw new RoutingException("No route for path: " + route);

        } else {
            String rawState = query.get("state");
            if (rawState == null) {
                throw new CommandParserException("The 'state' request param is required for " + route);

            } else {
                String json = URLDecoder.decode(rawState, "UTF-8");
                if (json == null) {
                    throw new CommandParserException("Failed to URL-decode a raw JSON string for " + route);

                } else {
                    // This works fine, but the JsonFactory for some reason wants a
                    // Class<T>, not a plain class. Causes an unchecked conversion warning.
                    Object state = JsonFactory.fromJson(json, stateClass);
                    if (state == null) {
                        throw new CommandParserException(
                                "Failed to deserialize a JSON command of length "
                                        + json.length() + " for path " + route);

                    } else if (state.getClass() == stateClass) {
                        return new Command(route, state);

                    } else {
                        throw new CommandParserException(
                                "Failed to convert a command of length "
                                        + json.length() + " for path " + route + " into a "
                                        + stateClass.getSimpleName() + ".");
                    }
                }
            }
        }
    }

    /** Dispatch a GET or POST request to the appropriate handler.
     * FUTURE Explore Simple's asynchronous response mode.
     * See "asynchronous services" here:
     * http://www.simpleframework.org/doc/tutorial/tutorial.php
     */
    public void handle(Request request, Response response) {
        // To see what is happening to your requests:
        //     this.log(this.formatRequest(request));
        try {
            String method = request.getMethod();
            if(method.equals("POST") || method.equals("PUT")) {
                this.handlePost(request, response);
            } else if(method.equals("GET")) {
                this.handleGet(request, response);
            } else {
                this.handleUnsupportedMethod(request, response);
            }
        } catch(Throwable t) {
            // Desperately try to stay afloat, but don't try to write
            // the response, because this error may have arisen from an
            // attempt to respond.
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            this.log("Uncaught error in request " + (requestIndex - 1)
                    + ": " + t + '\n' + sw);
        }
    }

    private void handleUnsupportedMethod(Request request, Response response) {
        int index = this.requestIndex;
        ++this.requestIndex; // Increment before any possible exception.
        response.setStatus(Status.METHOD_NOT_ALLOWED);
        String responseBody = "UNSUPPORTED_METHOD " + index;
        this.respond(response, responseBody);
    }

    private void handleGet(Request request, Response response) {
        int index = this.requestIndex;
        ++this.requestIndex; // Increment before any possible exception.
        response.setStatus(Status.OK);
        String responseBody = "TODO " + index;
        this.respond(response, responseBody);
    }

    private void handlePost(Request request, Response response) {
        int index = this.requestIndex;
        ++this.requestIndex; // Increment before any possible exception.

        String responseBody;
        try {
            Command command = this.createCommand(request);

            if(command == null) {
                throw new NullPointerException("Unreachable code: comand is null.");
            } else {
                boolean accepted = this.queue.offer(command);
                if(accepted) {
                    response.setStatus(Status.OK);
                    responseBody = "OK " + index;
                    if(this.verbose) this.log(responseBody);
                } else {
                    // For some reason, Status doesn't know about RFC 6585.
                    response.setCode(429);
                    response.setDescription("Too Many Requests");
                    responseBody = "DROPPED " + index;
                    if(this.verbose) this.log("Request " + index + " dropped. No room in queue.");
                }
            }

        } catch (RoutingException e) {
            String routes = Util.join(Util.sorted(this.commandMap.keySet()), "\n    ");
            response.setStatus(Status.NOT_FOUND);
            responseBody = "NOT_FOUND " + index;
            this.log(e.getMessage() + "\nAvailable routes:\n    " + routes);

        } catch (UnsupportedEncodingException e) {
            response.setStatus(Status.BAD_REQUEST);
            responseBody = "UNSUPPORTED_ENCODING " + index;
            if(this.verbose) this.log(e.getMessage());

        } catch (ClassCastException e) {
            this.log("Error (malformed JSON?) in request " + index + ": " + e.toString());

            // Save you hours of misguided debugging by explaining this error.
            // Example of an incomprehensible parser error, with no real indication
            // of the problem: I tried to parse "foo" and caught an error saying,
            // "java.lang.ClassCastException: org.boon.core.value.CharSequenceValue
            // cannot be cast to com.coillighting.udder.Command".
            this.log("Note that ClassCastExceptions are sometimes really just "
                    + "JSON syntax errors, but the Boon parser's error messages "
                    + "are inarticulate.");

            response.setStatus(Status.BAD_REQUEST);
            responseBody = "PARSE_CAST_ERROR " + index;

        } catch(CommandParserException e) {
            response.setStatus(Status.BAD_REQUEST);
            responseBody = "PARSE_ERROR " + index;
            if(this.verbose) this.log("Failed to parse a valid command from request " + index);

        } catch(Exception e) {
            response.setStatus(Status.BAD_REQUEST);
            responseBody = "UNEXPECTED_ERROR " + index;

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.log("Unexpected error in request " + index + ": " + e + '\n' + sw);
        }

        this.respond(response, responseBody);
    }

    private void addResponseHeader(Response response) {
        long time = System.currentTimeMillis();
        response.setValue("Content-Type", "text/plain");
        response.setValue("Server", "Udder/1.0 (Simple 4.0)");
        response.setValue("Access-Control-Allow-Origin", "*");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
    }

    private boolean respond(Response response, String responseBody) {
        this.addResponseHeader(response);
        try {
            PrintStream body = response.getPrintStream();
            body.println(responseBody);
            body.close();
            return true;
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.log("Error writing response body: " + e + '\n' + sw);
            return false;
        }
    }

    public void log(Object message) {
        System.out.println(message);
    }

}


class RoutingException extends Exception {
    public RoutingException(String message) { super(message); }
}


class CommandParserException extends Exception {
    public CommandParserException(String message) { super(message); }
}
