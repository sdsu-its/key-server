package edu.sdsu.its.key_server;

import edu.sdsu.its.key_server.Models.User;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * Publicly Accessible Endpoints
 * No Authentication is Required for Echo.
 * A valid API key is required to access app parameters.
 *
 * @author Tom Paulus
 *         Created on 11/2/15.
 */

@Path("/")
@Api(value = "", description = "Primary API Functions")
public class Web {
    protected static User decodeAuth(String authHeader) {
        if (authHeader == null) {
            return new User("", "");
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(authHeader.split(" ")[1].getBytes("UTF-8")));
            String[] parts = decoded.split(":");
            String username = parts[0];
            String password = parts[1];

            return new User(username, password);
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(Web.class).error("Invalid Encoding of Auth Header", e);

            return new edu.sdsu.its.key_server.Models.User("", "");
        }
    }

    @Path("echo")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            value = "Echos an input parameter ",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "text successfully echo-ed"),
            @ApiResponse(code = 400, message = "error during echo")
    })
    public Response echo(@QueryParam("m") @ApiParam(value = "Message to Echo", required = true) final String message) {
        Logger.getLogger(getClass()).info("Received Request: [GET] ECHO - m = " + message);

        String output = "echo: " + message;
        return Response.status(Response.Status.OK).entity(output).build();
    }

    @Path("param")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            value = "Return a Parameter from the Applications Database",
            notes = "Value is returned as plain text",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "parameter returned"),
            @ApiResponse(code = 401, message = "API key not allowed to access the specified app"),
            @ApiResponse(code = 404, message = "error finding parameter")
    })
    public Response getParam(@QueryParam("key") @ApiParam(value = "API Key to be used for Authentication", required = true) final String apiKey,
                             @QueryParam("app") @ApiParam(value = "Application desired parameter is associated with", required = true) final String applicationName,
                             @QueryParam("name") @ApiParam(value = "Name of Desired Parameter", required = true) final String parameterName) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [GET] PARAM - key=\"%s\" & app=\"%s\" & name=\"%s\"", apiKey, applicationName, parameterName));

        if (DB.getInstance().keyIsAllowed(apiKey, applicationName)) {
            if (DB.getInstance().paramExists(applicationName, parameterName)) {
                return Response.status(Response.Status.OK).entity(DB.getInstance().getParam(applicationName, parameterName).getDecryptedValue()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("%s does not exists in the application %s.", parameterName, applicationName)).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity(String.format("The supplied API key does not have sufficient permissions to access %s.", applicationName)).build();
        }
    }
}
