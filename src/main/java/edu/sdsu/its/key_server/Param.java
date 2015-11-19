package edu.sdsu.its.key_server;

import com.google.gson.Gson;
import edu.sdsu.its.key_server.Models.User;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Parameter Management Endpoints
 *
 * @author Tom Paulus
 *         Created on 11/16/15.
 */
@Path("/param")
@Api(value = "/param", description = "Manage Application Parameters")
public class Param {
    /**
     * Create a new Parameter for an App
     *
     * @param auth            {@link String} Authorization Header
     * @param applicationName {@link String} Application Name to which the Parameter Belongs
     * @param payload         {@link String} POST Payload
     * @return {@link Response} Response to Request
     */
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Create Parameter",
            notes = "Create a new Parameter for an application. Values are encrypted before they are sent to the database in the background.",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "parameter added successfully"),
            @ApiResponse(code = 400, message = "incomplete request"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")

    })
    public Response addParam(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                             @QueryParam("app") @ApiParam(value = "Application Name", required = true) final String applicationName,
                             @ApiParam(value = "Param JSON \n{\n" +
                                     "  \"name\": \"p_name\",\n" +
                                     "  \"value\": \"p_value\"\n" +
                                     "}", required = true) final String payload) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [POST] /param/create - auth=\"%s\" & app=\"%s\" & payload=\"%s\"", auth, applicationName, payload));

        User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for POST request to /param/create");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for POST Request to /param/create");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (applicationName == null) {
            Logger.getLogger(getClass()).info("No App specified for POST Request to /param/create");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Application Specified\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for POST request to /param/create for " + User.getUsername());

            if (DB.getInstance().tableExists(applicationName)) {
                Gson gson = new Gson();
                edu.sdsu.its.key_server.Models.Param param = gson.fromJson(payload, edu.sdsu.its.key_server.Models.Param.class);

                DB.getInstance().createParam(applicationName, param);

                return Response.status(Response.Status.CREATED).entity(gson.toJson(param)).build();

            } else {
                Logger.getLogger(getClass()).debug("Application does not exist - App Name: " + applicationName);
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(String.format("Application with name \"%s\" does not exist", applicationName)).build();
            }
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for POST request to /param/create for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    /**
     * Update a Parameter for an App
     *
     * @param auth            {@link String} Authorization Header
     * @param applicationName {@link String} Application Name to which the Parameter Belongs
     * @param payload         {@link String} PUT Payload
     * @return {@link Response} Response to Request
     */
    @Path("update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Update Parameter",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 202, message = "parameter updated successfully"),
            @ApiResponse(code = 400, message = "incomplete request"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")

    })
    public Response updateParam(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                                @QueryParam("app") @ApiParam(value = "Application Name", required = true) final String applicationName,
                                @ApiParam(value = "Param JSON \n{\n" +
                                        "  \"name\": \"p_name\",\n" +
                                        "  \"value\": \"p_value\"\n" +
                                        "}", required = true) final String payload) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [PUT] /param/update - auth=\"%s\" & app=\"%s\" & payload=\"%s\"", auth, applicationName, payload));

        edu.sdsu.its.key_server.Models.User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for PUT request to /param/update");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for PUT Request to /param/update");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (applicationName == null) {
            Logger.getLogger(getClass()).info("No App specified for PUT Request to /param/update");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Application Specified\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for PUT request to /param/update for " + User.getUsername());

            if (DB.getInstance().tableExists(applicationName)) {
                Gson gson = new Gson();
                edu.sdsu.its.key_server.Models.Param param = gson.fromJson(payload, edu.sdsu.its.key_server.Models.Param.class);

                DB.getInstance().updateParam(applicationName, param);

                return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(param)).build();

            } else {
                Logger.getLogger(getClass()).debug("Application does not exist - App Name: " + applicationName);
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(String.format("Application with name \"%s\" does not exist", applicationName)).build();
            }
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for PUT request to /param/update for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    /**
     * Delete a Parameter for an App
     *
     * @param auth            {@link String} Authorization Header
     * @param applicationName {@link String} Application Name to which the Parameter Belongs
     * @param payload         {@link String} DELETE Payload
     * @return {@link Response} Response to Request
     */
    @Path("delete")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Delete Parameter",
            notes = "Parameter deletion is non-reversible",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 202, message = "parameter deleted successfully"),
            @ApiResponse(code = 400, message = "incomplete request"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")

    })
    public Response deleteParam(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                                @QueryParam("app") @ApiParam(value = "Application Name", required = true) final String applicationName,
                                @ApiParam(value = "Param JSON \n{\n" +
                                        "  \"name\": \"p_name\",\n" +
                                        "  \"value\": \"p_value\"\n" +
                                        "}", required = true) final String payload) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [DELETE] /param/delete - auth=\"%s\" & app=\"%s\" & payload=\"%s\"", auth, applicationName, payload));

        User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for DELETE request to /param/delete");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for DELETE Request to /param/delete");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (applicationName == null) {
            Logger.getLogger(getClass()).info("No App specified for DELETE Request to /param/delete");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Application Specified\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for DELETE request to /param/delete for " + User.getUsername());

            if (DB.getInstance().tableExists(applicationName)) {
                Gson gson = new Gson();
                edu.sdsu.its.key_server.Models.Param param = gson.fromJson(payload, edu.sdsu.its.key_server.Models.Param.class);

                DB.getInstance().deleteParam(applicationName, param);

                return Response.status(Response.Status.ACCEPTED).build();

            } else {
                Logger.getLogger(getClass()).debug("Application does not exist - App Name: " + applicationName);
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(String.format("\"message\": \"Application with name \"%s\" does not exist\"}", applicationName)).build();
            }
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for DELETE request to /param/delete for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    /**
     * List all Params for a Specified App
     *
     * @param auth            {@link String} Authorization Header
     * @param applicationName {@link String} Application Name to which the Parameter Belongs
     * @return {@link Response} Response to Request
     */
    @Path("list")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "List Parameters for Specified Application",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "list generated successfully"),
            @ApiResponse(code = 400, message = "incomplete request"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")

    })
    public Response listParams(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                               @QueryParam("app") @ApiParam(value = "Application Name", required = true) final String applicationName) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [GET] /param/list - auth=\"%s\" & app=\"%s\"", auth, applicationName));
        User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for GET request to /param/list");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (applicationName == null) {
            Logger.getLogger(getClass()).info("No App specified for GET Request to /param/list");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Application Specified\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for GET request to /param/list for " + User.getUsername());

            edu.sdsu.its.key_server.Models.Param[] params = DB.getInstance().listParams(applicationName);

            Gson gson = new Gson();
            return Response.status(Response.Status.OK).entity(gson.toJson(params)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for GET request to /param/list for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }
}
