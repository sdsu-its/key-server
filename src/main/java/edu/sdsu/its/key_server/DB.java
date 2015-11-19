package edu.sdsu.its.key_server;

import edu.sdsu.its.key_server.Models.App;
import edu.sdsu.its.key_server.Models.Key;
import edu.sdsu.its.key_server.Models.Param;
import edu.sdsu.its.key_server.Models.User;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Communicate with a PostgreSQL Database.
 *
 * @author Tom Paulus
 *         Created on 11/2/15.
 */
public class DB {
    private static DB instance;

    /**
     * Create DB Instance.
     * Will create the table if it does not exist.
     * The DB stores the name of the file, its size, its path, and the last time it was indexed.
     */
    private DB() {
        if (!(tableExists("api_keys") && tableExists("users"))) {
            initDB();
        }
    }

    /**
     * Connect to DB.
     *
     * @return {@link Connection} The Connection to the Database
     */
    private static Connection getConnection() {
        System.out.println(System.getenv());
        String username = System.getenv("DB_User");
        String password = System.getenv("DB_Pass");
        String dbUrl = "jdbc:postgresql://" + System.getenv("DB_Host") + "/" + System.getenv("DB_Name");
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("ssl", "true");
        props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(dbUrl, props);
        } catch (ClassNotFoundException e) {
            Logger.getLogger(getInstance().getClass()).fatal("Driver not found", e);
            System.exit(1);
            return null;
        } catch (SQLException e) {
            Logger.getLogger(getInstance().getClass()).error("Problem Initializing Driver");
            System.exit(1);
            return null;
        }
    }

    /**
     * We only want one instance of the DB to insure that there are no conflicts.
     *
     * @return {@link DB} DB instance
     */
    public static synchronized DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }

        return instance;
    }

    /**
     * Execute SQL Statement
     *
     * @param sql {@link String} SQL Statement to run
     * @throws SQLException
     */
    private void executeStatement(final String sql) throws SQLException {
        assert getConnection() != null;

        Statement statement = getConnection().createStatement();
        Logger.getLogger(getClass()).info("Executing SQL Command - " + sql);
        statement.execute(sql);

        statement.close();
    }

    /**
     * Check if the Table Exists in the DB
     *
     * @param tableName {@link String} Table Name
     * @return If the supplied tableName exists
     */
    public boolean tableExists(final String tableName) {
        assert getConnection() != null;
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT EXISTS(\n" +
                    "    SELECT 1\n" +
                    "    FROM information_schema.tables\n" +
                    "    WHERE table_name = '" + tableName + "');");

            resultSet.next();
            boolean result = resultSet.getBoolean("exists");
            statement.close();

            return result;


        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem Connecting to DB to check for Table Existence", e);
            return false;
        }
    }

    /**
     * Initialize the DB if the desired Key and Users tables do not yet exist.
     */
    private void initDB() {
        try {

            final String users_table_sql = "CREATE TABLE users (" +
                    "  user_name TEXT," +
                    "  email     TEXT," +
                    "  password  TEXT," +
                    "  created   TIMESTAMP," +
                    "  updated   TIMESTAMP" +
                    ");";

            executeStatement(users_table_sql);

            final String api_keys_table_sql = "CREATE TABLE api_keys (" +
                    "  application_key  TEXT," +
                    "  application_name TEXT," +
                    "  permissions      TEXT," +
                    "  created          TIMESTAMP," +
                    "  updated          TIMESTAMP" +
                    ");";
            executeStatement(api_keys_table_sql);

            final String app_schema_sql = "CREATE SCHEMA apps;";
            executeStatement(app_schema_sql);

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem Initializing DB", e);
        }
    }

    /**
     * Check if the User is an Admin
     *
     * @param User {@link User} User information from the Authentication Header
     * @return If the User has Admin Permissions
     */
    public boolean isAdmin(final User User) {
        assert getConnection() != null;
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT password FROM users WHERE user_name = '" + User.getUsername() + "';");
            if (!resultSet.next()) {                            //if rs.next() returns false
                Logger.getLogger(getClass()).info(String.format("User %s was not found in DB.", User.getUsername()));
                return false;
            }
            String password_hash = resultSet.getString("password");

            return User.getPasswordHash().equals(password_hash);

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error(String.format("Problem Accessing User Information from DB for %s", User.getUsername()), e);
            return false;
        }
    }

    public void createUser(final User user) {
        try {
            final String new_user_sql = "INSERT INTO users VALUES (\n" +
                    "    '" + user.getUsername() + "',\n" +
                    "    '" + user.getEmail() + "',\n" +
                    "    '" + user.getPasswordHash() + "',\n" +
                    "    now(),\n" +
                    "    now()\n" +
                    ")";
            executeStatement(new_user_sql);

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem creating new User", e);
        }
    }

    public User updateUser(final User user) {
        try {
            String update_user_sql = "UPDATE users SET ";
            if (user.getEmail() != null) {
                update_user_sql += "email='" + user.getEmail() + "'";
            }
            if (user.getPasswordHash() != null) {
                update_user_sql += ",\n password='" + user.getPasswordHash() + "'";
            }
            update_user_sql += "\n, updated=now() WHERE user_name='" + user.getUsername() + "';";

            executeStatement(update_user_sql);
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem updating User", e);
        }

        return this.getUser(user.getUsername());
    }

    public void deleteUser(final User user) {
        try {
            final String delete_user_sql = "DELETE FROM users WHERE user_name='" + user.getUsername() + "';";
            executeStatement(delete_user_sql);
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem deleting User", e);
        }
    }

    public User[] listUsers() {
        assert getConnection() != null;
        try {
            Statement statement = getConnection().createStatement();
            final String list_users_sql = "SELECT * FROM users ORDER BY user_name;";
            Logger.getLogger(getClass()).info("Executing SQL Query - " + list_users_sql);
            ResultSet resultSet = statement.executeQuery(list_users_sql);

            List<User> users = new ArrayList<User>();

            while (resultSet.next()) {
                users.add(new User(resultSet.getString("user_name"), "", resultSet.getString("email")));
            }
            statement.close();

            User[] user_array = new User[users.size()];
            for (int i = 0; i < users.size(); i++) {
                user_array[i] = users.get(i);
            }

            return user_array;

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem Listing Users in DB", e);
            return new User[0];
        }
    }

    public User getUser(final String username) {
        assert getConnection() != null;

        try {
            final String get_user_sql = "SELECT * FROM users WHERE user_name='" + username + "';";
            Statement statement = getConnection().createStatement();
            Logger.getLogger(getClass()).info("Executing SQL Query - " + get_user_sql);
            ResultSet resultSet = statement.executeQuery(get_user_sql);
            if (!resultSet.next()) {                            //if rs.next() returns false
                Logger.getLogger(getClass()).info(String.format("User %s was not found", username));
                return new User("", "", "");
            }

            return new User(resultSet.getString("user_name"), "", resultSet.getString("email"));
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem retrieving user from DB", e);
            return new User("", "", "");
        }
    }

    /**
     * Create Application Table
     *
     * @param app_name {@link String} App Name to Create
     */
    public void createApp(final String app_name) {
        try {

            final String create_app_sql = "CREATE TABLE apps." + app_name + " (" +
                    "  parameter_name  TEXT," +
                    "  parameter_value TEXT," +
                    "  created         TIMESTAMP," +
                    "  updated         TIMESTAMP" +
                    ");";
            executeStatement(create_app_sql);

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error(String.format("Problem Creating new App with name '%s' in DB", app_name), e);
        }
    }

    /**
     * Delete Application
     *
     * @param app_name {@link String} Application to Delete
     */
    public void deleteApp(final String app_name) {
        try {
            final String delete_app_sql = "DROP TABLE apps." + app_name + ";";
            executeStatement(delete_app_sql);
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem Deleting App in DB", e);
        }
    }

    /**
     * List all Applications
     *
     * @return {@link App[]} All Applications
     */
    public App[] listApps() {
        assert getConnection() != null;
        try {
            Statement statement = getConnection().createStatement();
            final String list_apps_sql = "SELECT table_name\n" +
                    "FROM information_schema.tables\n" +
                    "WHERE table_schema = 'apps'\n" +
                    "ORDER BY table_name;";
            Logger.getLogger(getClass()).info("Executing SQL Query - " + list_apps_sql);

            ResultSet resultSet = statement.executeQuery(list_apps_sql);

            List<App> apps = new ArrayList<App>();

            while (resultSet.next()) {
                apps.add(new App(resultSet.getString("table_name")));
            }
            statement.close();

            App[] apps_array = new App[apps.size()];
            for (int i = 0; i < apps.size(); i++) {
                apps_array[i] = apps.get(i);
            }

            return apps_array;

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem Listing Apps in DB", e);
            return new App[0];
        }
    }

    /**
     * Check if a Parameter exists for an app
     *
     * @param app_name   {@link String} Application Name
     * @param param_name {@link String} Parameter Name
     * @return If the Parameter Exists
     */
    public boolean paramExists(final String app_name, final String param_name) {
        assert getConnection() != null;
        try {
            final String check_param_sql = "SELECT exists(SELECT 1 FROM apps." + app_name + " WHERE parameter_name='" + param_name + "')";

            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(check_param_sql);
            if (!resultSet.next()) {                            //if rs.next() returns false
                Logger.getLogger(getClass()).info(String.format("Param %s was not found for %s.", param_name, app_name));
                return false;
            }

            return resultSet.getBoolean("exists");
        } catch (SQLException e) {
            Logger.getLogger(getClass()).info("Problem Connecting to DB to check for Param Existence", e);
            return false;
        }
    }

    /**
     * Create Application Parameter
     *
     * @param app_name {@link String} Application Name
     * @param param    {@link Param} Parameter to Create
     */
    public void createParam(final String app_name, final Param param) {
        try {
            final String create_param_sql = "INSERT INTO apps." + app_name + " VALUES (\n" +
                    "    '" + param.getName() + "',\n" +
                    "    '" + param.getEncryptedValue() + "',\n" +
                    "     now(),\n" +
                    "     now()\n" +
                    ")";
            executeStatement(create_param_sql);

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem Creating Parameter in DB", e);
        }
    }

    /**
     * Update Application Parameter
     *
     * @param app_name {@link String} Application Name
     * @param param    {@link Param} Parameter to Update
     * @return {@link Param} Full Param Info
     */
    public Param updateParam(final String app_name, final Param param) {
        try {
            String update_param_sql = "UPDATE apps." + app_name + " SET \n";

            if (param.getName() != null) {
                update_param_sql += "parameter_name = '" + param.getName() + "'";
            }
            if (param.getDecryptedValue() != null) {
                update_param_sql += ", \nparameter_value = '" + param.getEncryptedValue() + "'";
            }
            update_param_sql += ", \nupdated = now() WHERE parameter_name = '" + param.getName() + "' ;";

            executeStatement(update_param_sql);
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem updating Parameter in DB.", e);
        }

        return this.getParam(app_name, param.getName());
    }

    /**
     * Delete Application Parameter
     *
     * @param app_name {@link String} Application Name
     * @param param    {@link Param} Parameter to Delete
     */
    public void deleteParam(final String app_name, final Param param) {
        try {
            final String delete_param_sql = "DELETE FROM apps." + app_name + " WHERE parameter_name = '" + param.getName() + "';";
            executeStatement(delete_param_sql);
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error(String.format("Problem Deleting Param with name '%s' from %s", param.getName(), app_name), e);
        }
    }

    /**
     * Get Application Parameter
     *
     * @param app_name   {@link String} Application Name
     * @param param_name {@link String} Parameter Name
     * @return {@link Param} Parameter
     */
    public Param getParam(final String app_name, final String param_name) {
        assert getConnection() != null;
        try {
            final String get_param_sql = "SELECT * FROM apps." + app_name + " WHERE parameter_name='" + param_name + "';";

            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(get_param_sql);
            resultSet.next();

            return new Param(param_name, resultSet.getString("parameter_value"));
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error(String.format("Problem getting Parameter with name '%s' from DB", param_name), e);
            return null;
        }
    }

    /**
     * List all Parameters for the Specified App
     *
     * @param app_name {@link String} Application Name
     * @return {@link Param[]} All params for the specified App
     */
    public Param[] listParams(final String app_name) {
        assert getConnection() != null;
        try {
            Statement statement = getConnection().createStatement();
            final String list_param_sql = "SELECT * " +
                    "FROM apps." + app_name + " " +
                    "ORDER BY parameter_name;";
            Logger.getLogger(getClass()).info("Executing SQL Query - " + list_param_sql);
            ResultSet resultSet = statement.executeQuery(list_param_sql);

            List<Param> params = new ArrayList<Param>();

            while (resultSet.next()) {
                params.add(new Param(resultSet.getString("parameter_name"), resultSet.getString("parameter_value")));
            }
            statement.close();

            Param[] params_array = new Param[params.size()];
            for (int i = 0; i < params.size(); i++) {
                params_array[i] = params.get(i);
            }

            return params_array;

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem Listing Keys in DB", e);
            return new Param[0];
        }
    }

    /**
     * Create new API Key
     *
     * @param key {@link Key} Key to Generate
     */
    public void requestAPIKey(final Key key) {
        try {
            final String new_key_sql = "INSERT INTO api_keys VALUES (\n" +
                    "    '" + key.getApplication_key() + "',\n" +
                    "    '" + key.getApplication_name() + "',\n" +
                    "    '" + key.getPermissions() + "',\n" +
                    "    now(),\n" +
                    "    now()\n" +
                    ")";
            executeStatement(new_key_sql);

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem issuing new API Key", e);
        }
    }

    /**
     * Update API Key
     *
     * @param key {@link Key} Key to Update
     * @return {@link Key} Updated API Key
     */
    public Key updateAPIKey(final Key key) {
        if (!Arrays.asList(listAPIKeys()).contains(key)) {
            Logger.getLogger(getClass()).info("Update Key was called for a key that does not exist yet. API Key: " + key.getApplication_key());
            return new Key("", "", "");
        }

        try {
            String update_key_sql = "UPDATE api_keys SET ";

            if (key.getApplication_name() != null) {
                update_key_sql += "application_name = '" + key.getApplication_name() + "'";
            }
            if (key.getPermissions() != null) {
                update_key_sql += ", \npermissions = '" + key.getPermissions() + "'";
            }
            update_key_sql += " updated = now() WHERE application_key = '" + key.getApplication_key() + "';";

            executeStatement(update_key_sql);

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem updating API key", e);
        }
        return this.getAPIKey(key.getApplication_key());
    }

    /**
     * Get API Key
     *
     * @param apiKey {@link String} API Key
     * @return {@link Key} Full Key Object
     */
    public Key getAPIKey(final String apiKey) {
        assert getConnection() != null;
        try {
            final String get_param_sql = "SELECT * FROM api_keys WHERE application_key='" + apiKey + "';";

            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(get_param_sql);
            resultSet.next();

            return new Key(apiKey, resultSet.getString("application_name"), resultSet.getString("permissions"));
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error(String.format("Problem getting API Key Info for key '%s' from DB", apiKey), e);
            return null;
        }
    }

    /**
     * List all API Keys
     *
     * @return {@link Key[]} All API Keys
     */
    public Key[] listAPIKeys() {
        assert getConnection() != null;
        try {
            Statement statement = getConnection().createStatement();
            final String list_api_keys_sql = "SELECT * " +
                    "FROM api_keys " +
                    "ORDER BY created;";
            Logger.getLogger(getClass()).info("Executing SQL Query - " + list_api_keys_sql);
            ResultSet resultSet = statement.executeQuery(list_api_keys_sql);

            List<Key> keys = new ArrayList<Key>();

            while (resultSet.next()) {
                keys.add(new Key(resultSet.getString("application_key"), resultSet.getString("application_name"), resultSet.getString("permission")));
            }
            statement.close();

            Key[] keys_array = new Key[keys.size()];
            for (int i = 0; i < keys.size(); i++) {
                keys_array[i] = keys.get(i);
            }

            return keys_array;

        } catch (SQLException e) {
            Logger.getLogger(getClass()).error("Problem Listing Keys in DB", e);
            return new Key[0];
        }
    }

    /**
     * Check if the supplied API key is allowed to access the application's parameters.
     *
     * @param apiKey  {@link String} API Key
     * @param appName {@link String} Application Name
     * @return If the key is allowed.
     */

    public boolean keyIsAllowed(final String apiKey, final String appName) {
        /**
         * "ALL"    = All Apps
         * - XYZ    = Blacklisted (All Except)
         * + XYZ    = Whitelisted (None Except)
         * "NO"     = No Apps (Key Revoked)
         */
        String keyPermissions = getAPIKey(apiKey).getPermissions();
        Logger.getLogger(getClass()).debug(String.format("Key: %s, has Permissions: \"%s\"", apiKey, keyPermissions));

        if (!keyPermissions.equals("NO")) {
            if (keyPermissions.equals("ALL")) {
                // All Access Key
                Logger.getLogger(getClass()).info(String.format("Full Permission Granted to Key: %s", apiKey));
                return true;
            } else if (keyPermissions.contains("+" + appName)) {
                // Whitelisted for App
                Logger.getLogger(getClass()).info(String.format("Key: %s, is whitelisted to access %s", apiKey, appName));
                return true;
            } else if (keyPermissions.contains("-" + appName)) {
                // Blacklisted for App
                Logger.getLogger(getClass()).info(String.format("Key: %s, is blacklisted to access %s", apiKey, appName));
                return false;
            } else {
                Logger.getLogger(getClass()).info(String.format("No Permissions defined for Key: %s, to use App: %s", apiKey, appName));
                return false;
            }
        } else {
            // Key Disabled for All Apps
            Logger.getLogger(getClass()).info(String.format("Key: %s, is Disabled", apiKey));
            return false;
        }
    }
}
