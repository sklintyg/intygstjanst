package se.inera.certificate.migration.testutils.http;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class IntygHttpRequestHandler implements HttpRequestHandler {

    private IntygHttpRequestHandlerMode mode;
    
    public IntygHttpRequestHandler(IntygHttpRequestHandlerMode mode) {
        this.mode = mode;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext ctx) throws HttpException, IOException {
        
        switch (mode) {
        case COMCHECK:
            doComCheck(request, response, ctx);
            break;
        case SERVER_ERROR_500:
            response.setStatusCode(500);
            break;
        case HANDLE_POST:
            handlePost(request, response, ctx);
            break;
        default:
            response.setStatusCode(404);
            break;
        }
        
    }
    
    private void handlePost(HttpRequest request, HttpResponse response, HttpContext ctx) throws IOException {
        
        switch (request.getRequestLine().getMethod()) {
        case "POST":
            response.setStatusCode(200);
            StringEntity s = new StringEntity("blahonga");
            s.setContentType("application/json");
            response.setEntity(s);
            break;
        default:
            response.setStatusCode(405);
            response.setHeader("Allow", "POST");
            StringEntity body = new StringEntity(null);
            response.setEntity(body);
            break;
        }
        
    }

    private void doComCheck(HttpRequest request, HttpResponse response, HttpContext ctx) throws IOException {
        response.setStatusCode(200);
        StringEntity s = new StringEntity("Server is up");
        s.setContentType("text/plain");
        response.setEntity(s);
    }

    public enum IntygHttpRequestHandlerMode {
        COMCHECK,
        SERVER_ERROR_500,
        HANDLE_POST,
        NOT_FOUND_404;
    }
}
