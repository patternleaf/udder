package com.coillighting.udder;

import java.io.PrintStream;
import java.util.Queue;

import org.boon.json.JsonFactory;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import com.coillighting.udder.Command;


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

    public void handle(Request request, Response response) {
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
            Command simCommand = new Command(index);
            String json = JsonFactory.toJson(simCommand);
            this.log(json);
            Command command = JsonFactory.fromJson(json, Command.class);

            boolean accepted = this.queue.offer(command);
            if(this.verbose) {
                if(accepted) {
                    this.log(response_body);
                } else {
                    this.log("Request " + index + " dropped. No room in queue.");
                }
            }

        } catch(ClassCastException e) {
            this.log("Error (malformed JSON command?): " + e.toString());

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
            this.log("Error: " + e.toString());
            e.printStackTrace(); // FIXME see above
        }
    }

    public void log(Object message) {
        System.err.println(message);
    }

}
