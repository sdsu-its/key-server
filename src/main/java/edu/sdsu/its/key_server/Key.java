package edu.sdsu.its.key_server;

import com.google.gson.Gson;
import edu.sdsu.its.key_server.Models.User;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API Key Management Endpoints
 *
 * @author Tom Paulus
 *         Created on 11/16/15.
 */
@Path("/key")
@Api(value = "/key", description = "Manage API Keys, used to access the DataBase")
public class Key {
    /**
     * Create a new API Key
     *
     * @param auth    {@link String} Authorization Header
     * @param payload {@link String} POST Payload
     * @return {@link Response} Response to Request
     */
    @Path("issue")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Issues API Key",
            notes = "API Key will be returned in the response JSON",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "api key created successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")
    })
    public Response issueKey(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                             @ApiParam(value = "Application JSON \n{\n" +
                                     "  \"application_name\": \"My App\",\n" +
                                     "  \"permissions\": \"ALL\"\n" +
                                     "}", required = true) final String payload) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [POST] /key/issue - auth=\"%s\" & payload=\"%s\"", auth, payload));
        User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for POST request to /key/issue");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for POST Request to /key/issue");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for POST request to /key/issue for " + User.getUsername());

            Gson gson = new Gson();
            edu.sdsu.its.key_server.Models.Key key = gson.fromJson(payload, edu.sdsu.its.key_server.Models.Key.class);

            DB.getInstance().requestAPIKey(key);

            return Response.status(Response.Status.CREATED).entity(gson.toJson(key)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for POST request to /key/issue for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    /**
     * Update API Key
     *
     * @param auth    {@link String} Authorization Header
     * @param payload {@link String} PUT Payload
     * @return {@link Response} Response to Request
     */
    @Path("update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Update API Key",
            notes = "Updated API Key will be returned in the response JSON",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 202, message = "api key updated successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")
    })
    public Response updateKey(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                              @ApiParam(value = "Application JSON \n{" +
                                      "  \"application_key\": \"my_app_key\"," +
                                      "  \"application_name\": \"My App\"," +
                                      "  \"permissions\": \"ALL\"" +
                                      "}", required = true) final String payload) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [PUT] /key/update - auth=\"%s\" & payload=\"%s\"", auth, payload));
        User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for PUT request to /key/update");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for PUT Request to /key/update");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for PUT request to /key/update for " + User.getUsername());

            Gson gson = new Gson();
            edu.sdsu.its.key_server.Models.Key key = gson.fromJson(payload, edu.sdsu.its.key_server.Models.Key.class);

            DB.getInstance().updateAPIKey(key);

            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(key)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for PUT request to /key/update for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    /**
     * Revoke API Key
     *
     * @param auth    {@link String} Authorization Header
     * @param payload {@link String} DELETE Payload
     * @return {@link Response} Response to Request
     */
    @Path("revoke")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Delete API Key",
            notes = "API Key deletion is reversible, deleting the key simply revokes all access. " +
                    "To restore the key, simply PUT the key with the updated permissions.",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 202, message = "api key deleted successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")

    })
    public Response deleteKey(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                              @ApiParam(value = "Key JSON \n{\n" +
                                      "  \"application_key\": \"key_value\",\n" +
                                      "}", required = true) final String payload) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [DELETE] /key/revoke - auth=\"%s\" & payload=\"%s\"", auth, payload));
        User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for DELETE request to /key/revoke");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for DELETE Request to /key/revoke");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for DELETE request to /key/revoke for " + User.getUsername());

            Gson gson = new Gson();
            edu.sdsu.its.key_server.Models.Key key = gson.fromJson(payload, edu.sdsu.its.key_server.Models.Key.class);
            key.setPermissions("NO");

            DB.getInstance().updateAPIKey(key);

            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for DELETE request to /key/revoke for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    /**
     * List all API Keys
     *
     * @param auth {@link String} Authorization Header
     * @return {@link Response} Response to Request
     */
    @Path("list")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "List all API Keys",
            notes = "List all API Keys, the application names Associated to them, and their respective permissions",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "list generated successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct")
    })
    public Response listKeys(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [GET] /key/list - auth=\"%s\"", auth));
        edu.sdsu.its.key_server.Models.User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for GET request to /key/list");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for GET request to /key/list for " + User.getUsername());

            edu.sdsu.its.key_server.Models.Key[] keys = DB.getInstance().listAPIKeys();

            Gson gson = new Gson();
            return Response.status(Response.Status.OK).entity(gson.toJson(keys)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for GET request to /key/list for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }
}
