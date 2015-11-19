package edu.sdsu.its.key_server;

import com.google.gson.Gson;
import edu.sdsu.its.key_server.Models.User;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Application Management Endpoints
 *
 * @author Tom Paulus
 *         Created on 11/16/15.
 */
@Path("/app")
@Api(value = "/app", description = "Manage Applications, Folders for Parameters")
public class App {
    /**
     * Create a new Application
     *
     * @param auth    {@link String} Authorization Header
     * @param payload {@link String} POST Payload
     * @return {@link Response} Response to Request
     */
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Create Application",
            notes = "an application acts like a folder, holding parameters associated with the app.",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "application created successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied"),
            @ApiResponse(code = 403, message = "username or password is not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")

    })
    public Response createApp(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                              @ApiParam(value = "Application JSON \n{\n" +
                                      "  \"name\": \"app_name\",\n" +
                                      "}", required = true) final String payload) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [POST] /app/create - auth=\"%s\" & payload=\"%s\"", auth, payload));
        User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for POST request to /app/create");
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for POST Request to /app/create");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for POST request to /app/create for " + User.getUsername());

            Gson gson = new Gson();
            edu.sdsu.its.key_server.Models.App app = gson.fromJson(payload, edu.sdsu.its.key_server.Models.App.class);

            DB.getInstance().createApp(app.getName());

            return Response.status(Response.Status.CREATED).entity(gson.toJson(app)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for POST request to /app/create " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    /**
     * Delete Application
     *
     * @param auth    {@link String} Authorization Header
     * @param payload {@link String} DELETE Payload
     * @return {@link Response} Response to Request
     */
    @Path("delete")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Delete Application",
            notes = "application deletion is non-reversible.",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 202, message = "application successfully deleted"),
            @ApiResponse(code = 401, message = "username or password not supplied"),
            @ApiResponse(code = 406, message = "no payload sent with request")
    })
    public Response deleteApp(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                              @ApiParam(value = "Application JSON \n{\n" +
                                      "  \"name\": \"app_name\",\n" +
                                      "}", required = true) final String payload) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [DELETE] /app/delete - auth=\"%s\" & payload=\"%s\"", auth, payload));
        edu.sdsu.its.key_server.Models.User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for DELETE request to /app/delete");
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for DELETE Request to /app/delete");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for POST request to /app/delete for " + User.getUsername());

            Gson gson = new Gson();
            edu.sdsu.its.key_server.Models.App app = gson.fromJson(payload, edu.sdsu.its.key_server.Models.App.class);

            DB.getInstance().deleteApp(app.getName());

            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for DELETE request to /app/delete for " + User.getUsername());
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    @Path("list")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "List Applications",
            notes = "Lists all registered applications as a JSON array.",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "list generated successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or is not correct")
    })
    public Response listApps(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [GET] app/list - auth=\"%s\"", auth));
        User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for GET request to /app/list");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for GET request to /app/list for " + User.getUsername());

            edu.sdsu.its.key_server.Models.App[] apps = DB.getInstance().listApps();

            Gson gson = new Gson();
            return Response.status(Response.Status.OK).entity(gson.toJson(apps)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for GET request to /app/list for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

}
