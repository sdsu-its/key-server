package edu.sdsu.its.key_server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * User Management Endpoints
 *
 * @author Tom Paulus
 *         Created on 11/16/15.
 */
@Path("/user")
@Api(value = "/user", description = "Manage Admin Users")
public class User {
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Create Admin User",
            notes = "Create a new user account to administer the Key Server.",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "user created successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")

    })
    public Response addUser(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                            @ApiParam(value = "User JSON \n{\n" +
                                    "  \"username\": \"my_uname\",\n" +
                                    "  \"password\": \"my_base_64_encoded_password\",\n" +
                                    "  \"email\": \"me@example.org\"\n" +
                                    "}", required = true) final String payload) {

        Logger.getLogger(getClass()).info(String.format("Received Request: [POST] /user/create - auth=\"%s\" & payload=\"%s\"", auth, payload));
        edu.sdsu.its.key_server.Models.User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for POST request to user/create");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for POST Request to user/create");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for POST request to user/create for " + User.getUsername());

            final GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = builder.create();

            edu.sdsu.its.key_server.Models.User user = gson.fromJson(payload, edu.sdsu.its.key_server.Models.User.class);
            user.updatePassword();

            DB.getInstance().createUser(user);
            user.clearPassword();

            return Response.status(Response.Status.CREATED).entity(gson.toJson(user)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for POST request to user/create for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    @Path("update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Update Admin User",
            notes = "Updated User, minus their password, will be returned in the response JSON",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 202, message = "user updated successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")
    })
    public Response updateUser(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                               @ApiParam(value = "Application JSON \n{\n" +
                                       "  \"username\": \"my_uname\",\n" +
                                       "  \"password\": \"my_base_64_encoded_password\",\n" +
                                       "  \"email\": \"me@example.org\"\n" +
                                       "}", required = true) final String payload) {

        Logger.getLogger(getClass()).info(String.format("Received Request: [PUT] /user/update - auth=\"%s\" & payload=\"%s\"", auth, payload));
        edu.sdsu.its.key_server.Models.User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for PUT request to user/update");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for PUT Request to user/update");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for PUT request to user/update for " + User.getUsername());

            final GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = builder.create();

            edu.sdsu.its.key_server.Models.User user = gson.fromJson(payload, edu.sdsu.its.key_server.Models.User.class);
            user.updatePassword();

            DB.getInstance().updateUser(user);
            user.clearPassword();

            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(user)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for PUT request to user/update for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }

    @Path("delete")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Delete User",
            notes = "user deletion is non-reversible.",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 202, message = "user deleted successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or not not correct"),
            @ApiResponse(code = 406, message = "no payload sent with request")
    })
    public Response deleteUser(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth,
                               @ApiParam(value = "Application JSON \n{\n" +
                                       "  \"username\": \"my_uname\"\n" +
                                       "}", required = true) final String payload) {

        Logger.getLogger(getClass()).info(String.format("Received Request: [DELETE] /user/delete - auth=\"%s\" & payload=\"%s\"", auth, payload));
        edu.sdsu.its.key_server.Models.User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for DELETE request to user/delete");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (payload == null) {
            Logger.getLogger(getClass()).info("No Payload for DELETE Request to user/delete");
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"message\": \"No Payload Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for DELETE request to user/delete for " + User.getUsername());

            final GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = builder.create();

            edu.sdsu.its.key_server.Models.User user = gson.fromJson(payload, edu.sdsu.its.key_server.Models.User.class);
            user.updatePassword();

            DB.getInstance().deleteUser(user);

            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for DELETE request to user/delete for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }


    @Path("list")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "List all API Administrators",
            notes = "List all API Administrators, their names, emails, and usernames",
            response = Response.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "list generated successfully"),
            @ApiResponse(code = 401, message = "username or password not supplied or not correct")
    })
    public Response listUsers(@HeaderParam("authorization") @ApiParam(value = "Basic HTTP Auth Header", required = true) final String auth) {
        Logger.getLogger(getClass()).info(String.format("Received Request: [GET] user/list - auth=\"%s\"", auth));
        edu.sdsu.its.key_server.Models.User User = Web.decodeAuth(auth);

        if (User == null) {
            Logger.getLogger(getClass()).info("Username or Password not Supplied for GET request to /user/list");
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password not Supplied\"}").build();
        }
        if (DB.getInstance().isAdmin(User)) {
            Logger.getLogger(getClass()).info("Authorization PASSED for GET request to /user/list for " + User.getUsername());

            edu.sdsu.its.key_server.Models.User[] apps = DB.getInstance().listUsers();

            final GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = builder.create();

            return Response.status(Response.Status.OK).entity(gson.toJson(apps)).build();
        } else {
            Logger.getLogger(getClass()).info("Authorization FAILED for GET request to /user/list for " + User.getUsername());
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\": \"Username or Password is incorrect\"}").build();
        }
    }
}