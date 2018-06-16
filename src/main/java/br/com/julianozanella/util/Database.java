package br.com.julianozanella.util;

import br.com.julianozanella.util.exception.ConnectionNotFoundException;
import br.com.julianozanella.util.exception.InvalidTypeArgsException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Juliano Zanella
 * <p>
 * Contains database utilities
 */
public class Database {

    private static Connection connection;
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static String driver;

    /**
     * Create the database connection and maintain, use only once. ****Be sure
     * to connect before using any method.****
     *
     * @param url      The database url. EX:
     *                 "jdbc:mysql://localhost:3306/databaseName"
     * @param user     The database user. Ex: "root"
     * @param password The database password
     * @throws SQLException           SqlException
     * @throws ClassNotFoundException Class not found
     */
    public static void createConnection(String url, String user, String password) throws SQLException, ClassNotFoundException {
        makeConnection(url, user, password, DRIVER);
    }

    /**
     * @param url      The database url. EX:
     *                 "jdbc:mysql://localhost:3306/databaseName"
     * @param user     The database user. Ex: "root"
     * @param password The database password
     * @param driver   The connection driver. Ex: "com.mysql.jdbc.Driver"
     * @throws SQLException
     * @throws ClassNotFoundException //deprecated For no mysql databases Create the database connection and
     *                                maintain, use only once. ****Be sure to connect before using any
     *                                method.****
     */
    public static void makeConnection(String url, String user, String password, String driver) throws SQLException, ClassNotFoundException {
        if (connection == null) {
            Database.driver = driver;
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    /**
     * @return The connection with database.
     */
    public static Connection getConnection() {
        return connection;
    }

    public static String getDriver() {
        return driver;
    }

    /**
     * Insert into database
     *
     * @param tableName       Table name in database. Ex: person
     * @param fieldsAndValues Pairs of fields and values ​​to enter, the fields
     *                        must have the same name in the database table. EX: <b>"name", "Test"</b>,
     *                        <b>"age", 18</b>
     * @throws InvalidTypeArgsException
     * @throws SQLException
     * @throws ConnectionNotFoundException
     */
    public static void insert(String tableName, HashMap<String, Object> fieldsAndValues) throws InvalidTypeArgsException, SQLException, ConnectionNotFoundException {
        if (connection == null) {
            throw new ConnectionNotFoundException();
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder valuesString = new StringBuilder(" VALUES (");
        for (String field : fieldsAndValues.keySet()) {
            sql.append(field);
            sql.append(", ");
            valuesString.append("?, ");
        }
        sql.delete(sql.length() - 2, sql.length());
        valuesString.delete(valuesString.length() - 2, valuesString.length());
        sql.append(") ");
        sql.append(valuesString);
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (Object object : fieldsAndValues.values()) {
                if (object instanceof Integer) {
                    stmt.setInt(index, (int) object);
                } else if (object instanceof String) {
                    stmt.setString(index, (String) object);
                } else if (object instanceof Double) {
                    stmt.setDouble(index, (double) object);
                } else if (object instanceof Character) {
                    stmt.setString(index, "" + ((Character) object));
                } else if (object instanceof Date) {
                    stmt.setDate(index, (Date) object);
                } else if (object instanceof LocalDate) {
                    stmt.setDate(index, DateUtil.getSQLDate((LocalDate) object));
                } else {
                    throw new InvalidTypeArgsException(object.getClass().getSimpleName());
                }
                index++;
            }
            stmt.execute();
        } catch (SQLException ex) {
            throw ex;
        }
    }

    /**
     * Insert into database the object. <b>The attributes must have the same
     * name, as well as the name of the class that is the name of the table.</b>
     *
     * @param object The fill object to insert into database.
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConnectionNotFoundException
     */
    public static void insert(Object object) throws IllegalAccessException, SQLException, ClassNotFoundException, ConnectionNotFoundException {
        insert(object, true);
    }

    /**
     * Insert into database the object. <b>The attributes must have the same
     * name, as well as the name of the class that is the name of the table.</b>
     *
     * @param object        The fill object to insert into database.
     * @param autoIncrement if false, insert also the primary key code.
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConnectionNotFoundException
     */
    public static void insert(Object object, boolean autoIncrement) throws SQLException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, ConnectionNotFoundException {
        if (connection == null) {
            throw new ConnectionNotFoundException();
        }
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Class clazz = Class.forName(object.getClass().getName());
        String pk = getPK(clazz);
        Field declaredFields[] = clazz.getDeclaredFields();
        for (Field fld : declaredFields) {
            fld.setAccessible(true);
            if (autoIncrement && fld.getName().equalsIgnoreCase(pk)) {
                continue;
            }
            if (fld.get(object) != null) {
                fields.append(fld.getName());
                fields.append(", ");
                values.append("'");
                values.append(fld.get(object));
                values.append("' ,");
            }
        }
        fields.delete(fields.length() - 2, fields.length());
        String table = clazz.getSimpleName();
        values.delete(values.length() - 1, values.length());
        String sql = "INSERT INTO " + table + " ("
                + fields
                + ") VALUES ("
                + values
                + ")";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    /**
     * Selects all fields in the table.
     *
     * @param clazz The class of objects that will be populated with the result of the query.
     * @return The list of these objects. Convert each one in turn.
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws ConnectionNotFoundException
     */
    public static List<Object> select(Class clazz) throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, ConnectionNotFoundException, InvalidTypeArgsException {
        return select(clazz, 0, "");
    }

    /**
     * Selects the field in the table that has the code as the primary key.
     *
     * @param clazz  The class of objects that will be populated with the result of the query.
     * @param codeId The primary code.
     * @return The list of these objects. <b>Convert each one in turn.</b>
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws ConnectionNotFoundException
     */
    public static List<Object> select(Class clazz, int codeId) throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, ConnectionNotFoundException, InvalidTypeArgsException {
        return select(clazz, codeId, "");
    }

    /**
     * Selects the fields in the table that meet the condition.
     *
     * @param clazz       The class of objects that will be populated with the result of the query.
     * @param whereClause The condition. <b>Ex: "name LIKE J%"</b>
     * @return The list of these objects. <b>Convert each one in turn.</b>
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws ConnectionNotFoundException
     */
    public static List<Object> select(Class clazz, String whereClause) throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, ConnectionNotFoundException, InvalidTypeArgsException {
        return select(clazz, 0, whereClause);
    }

    /**
     * Selects all fields in the table.
     *
     * @param c
     * @param codeId
     * @param whereClause
     * @return
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws ConnectionNotFoundException
     */
    private static List<Object> select(Class c, int codeId, String whereClause) throws SQLException,
            InstantiationException,
            IllegalAccessException,
            NoSuchMethodException,
            SecurityException,
            InvocationTargetException, ConnectionNotFoundException, InvalidTypeArgsException {
        if (connection == null) {
            throw new ConnectionNotFoundException();
        }
        List<Object> list = new ArrayList<>();
        String table = c.getSimpleName();
        String pk = getPK(c);
        String sql = "SELECT * FROM " + table;
        if (codeId > 0) {
            sql += " WHERE " + pk + " = ?";
        }
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (codeId > 0) {
                stmt.setInt(1, codeId);
            }
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    Object obj = c.newInstance();
                    for (Method m : c.getMethods()) {
                        if (m.getName().substring(0, 3).equals("set")) {
                            Class[] args1 = new Class[1];
                            Class parameterTypes[] = m.getParameterTypes();
                            String field = m.getName().substring(3, m.getName().length());

                            switch (parameterTypes[0].getName()) {
                                case "java.lang.String":
                                    obj.getClass().getMethod(m.getName(), String.class
                                    ).invoke(obj, resultSet.getString(field));
                                    break;

                                case "int":
                                    obj.getClass().getMethod(m.getName(), int.class
                                    ).invoke(obj, resultSet.getInt(field));
                                    break;

                                case "double":
                                    obj.getClass().getMethod(m.getName(), double.class
                                    ).invoke(obj, resultSet.getDouble(field));
                                    break;

                                case "boolean":
                                    obj.getClass().getMethod(m.getName(), boolean.class
                                    ).invoke(obj, resultSet.getBoolean(field));
                                    break;

                                case "char":
                                    obj.getClass().getMethod(m.getName(), char.class
                                    ).invoke(obj, resultSet.getString(field).charAt(0));
                                    break;
                                case "java.sql.Date":
                                    args1[0] = Date.class;
                                    obj.getClass().getMethod(m.getName(),
                                            args1).invoke(obj, resultSet.getDate(field));
                                    break;
                                case   "java.time.LocalDate":
                                    args1[0] = LocalDate.class;
                                    obj.getClass().getMethod(m.getName(),
                                            args1).invoke(obj, (resultSet.getDate(field)).toLocalDate());
                                default:
                                    throw new InvalidTypeArgsException(parameterTypes[0].getName());
                            }
                        }
                    }
                    list.add(obj);
                }
            }

        }
        return list;
    }


    public static ResultSet select(String tableName) throws ConnectionNotFoundException, SQLException {
        return select(tableName, 0);
    }

    public static ResultSet select(String tableName, int id) throws ConnectionNotFoundException, SQLException {
        String sql = "SELECT * FROM " + tableName;
        if (id > 0) {
            sql += " WHERE " + getPK(tableName) + " = " + id;
        }
        PreparedStatement stmt = connection.prepareStatement(sql);
        return stmt.executeQuery();
    }

    private static String getPK(String tableName) throws SQLException, ConnectionNotFoundException {
        if (connection == null) {
            throw new ConnectionNotFoundException();
        }
        String pK = "";
        String database;
        database = connection.getCatalog();
        String sql = "SELECT information_schema.KEY_COLUMN_USAGE.COLUMN_NAME as \"chave\" \n"
                + "FROM information_schema.KEY_COLUMN_USAGE \n"
                + "WHERE information_schema.KEY_COLUMN_USAGE.CONSTRAINT_NAME LIKE \"PRIMARY\" \n"
                + "AND information_schema.KEY_COLUMN_USAGE.TABLE_SCHEMA LIKE \"" + database + "\""
                + " AND information_schema.KEY_COLUMN_USAGE.TABLE_NAME LIKE \"" + tableName + "\"";
        PreparedStatement stmt = connection.prepareCall(sql);
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            pK = resultSet.getString("chave");
        }
        return pK;
    }

    private static String getPK(Class cls) throws SQLException, ConnectionNotFoundException {
        return getPK(cls.getSimpleName());
    }

    /**
     * Update the object in the database, by the primary key.
     * <b>The attributes must have the same
     * name, as well as the name of the class that is the name of the table.</b>
     *
     * @param object The fill object to update into database.
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConnectionNotFoundException
     */
    public static void update(Object object) throws IllegalAccessException, SQLException, ClassNotFoundException, ConnectionNotFoundException {
        update(object, "");
    }

    /**
     * Update the object in the database, by the where clause.
     * <b>The attributes must have the same
     * name, as well as the name of the class that is the name of the table.</b>
     *
     * @param object      The fill object to update into database.
     * @param whereClause The where clause to update the object. <b>Ex: "city LIKE 'Alabama'".</b>
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConnectionNotFoundException
     */
    public static void update(Object object, String whereClause) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, ConnectionNotFoundException {
        if (connection == null) {
            throw new ConnectionNotFoundException();
        }
        String className = object.getClass().getName();
        Class clazz;
        clazz = Class.forName(className);
        String pK = getPK(clazz);
        StringBuilder fields = new StringBuilder();
        String where = "";
        String table = clazz.getSimpleName();
        Field declaredFields[] = clazz.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field fld = declaredFields[i];
            fld.setAccessible(true);
            if (fld.get(object) != null) {
                if (fld.getType().toString().equals("int") && fld.getName().equalsIgnoreCase(pK)) {
                    where = (whereClause.isEmpty()) ? fld.getName() + " = '" + fld.get(object) + "'" : whereClause;
                } else if (!fld.getName().equalsIgnoreCase(pK) && !fld.get(object).equals(0)) {
                    fields.append(fld.getName()).append(" = '").append(fld.get(object)).append("'");
                    if (i != (declaredFields.length - 1)) {
                        fields.append(", ");
                    }
                }
            }
        }
        if (fields.toString().endsWith(", ")) {
            fields = new StringBuilder(fields.substring(0, fields.length() - 2));
        }
        String sql = "UPDATE " + table + " SET " + fields + " WHERE " + where + "";
        try (PreparedStatement stmt = connection.prepareCall(sql)) {
            stmt.execute();
        }

    }

    /**
     * Update in the database, by the primary key code.
     *
     * @param tableName       The name of table.
     * @param fieldsAndValues Pairs of fields and values ​​to enter, the fields
     *                        must have the same name in the database table. EX: <b>"name", "Test"; "age", 18</b>
     * @param codeId          The primary key code.
     * @throws InvalidTypeArgsException
     * @throws SQLException
     * @throws ConnectionNotFoundException
     */
    public static void update(String tableName, HashMap<String, Object> fieldsAndValues, int codeId) throws InvalidTypeArgsException, SQLException, ConnectionNotFoundException {
        update(tableName, fieldsAndValues, codeId, "");
    }

    /**
     * Update in the database, by the where clause.
     *
     * @param tableName       The name of table.
     * @param fieldsAndValues Pairs of fields and values ​​to enter, the fields
     *                        must have the same name in the database table. EX: <b>"name", "Test"; "age", 18</b>
     * @param whereClause     The condition. <b>Ex: "name LIKE J%"</b>
     * @throws SQLException
     * @throws InvalidTypeArgsException
     * @throws ConnectionNotFoundException
     */
    public static void update(String tableName, HashMap<String, Object> fieldsAndValues, String whereClause) throws SQLException, InvalidTypeArgsException, ConnectionNotFoundException {
        update(tableName, fieldsAndValues, 0, whereClause);
    }

    private static void update(String tableName, HashMap<String, Object> fieldsAndValues, int codeId, String whereClause) throws SQLException, InvalidTypeArgsException, ConnectionNotFoundException {
        if (connection == null) {
            throw new ConnectionNotFoundException();
        }
        String pk = getPK(tableName);
        String sql = "UPDATE " + tableName + " SET ";
        String where = " WHERE ";
        for (String field : fieldsAndValues.keySet()) {
            if (!field.equalsIgnoreCase(pk)) {
                sql = sql + "" + field + " = ? ,";
            }
        }
        sql = sql.substring(0, sql.lastIndexOf(","));
        where += (whereClause.isEmpty()) ? pk + " = ?" : whereClause;
        sql = sql + where;
        PreparedStatement stmt = connection.prepareStatement(sql);
        int index = 1;
        for (Object value : fieldsAndValues.values()) {
            if (value instanceof Integer) {
                stmt.setInt(index, (Integer) value);
            } else if (value instanceof String) {
                stmt.setString(index, (String) value);
            } else if (value instanceof Date) {
                stmt.setDate(index, (Date) value);
            } else if (value instanceof Character) {
                stmt.setString(index, "" + ((Character) value));
            } else if (value instanceof Double) {
                stmt.setDouble(index, (Double) value);
            } else if (value instanceof LocalDate) {
                stmt.setDate(index, DateUtil.getSQLDate((LocalDate) value));
            } else {
                throw new InvalidTypeArgsException(value.getClass().getSimpleName());
            }
            index++;
        }
        if (whereClause.isEmpty()) {
            stmt.setInt(fieldsAndValues.size() + 1, codeId);
        }
        stmt.execute();
        stmt.close();
    }

    /**
     * Delete the object in database.
     * <b>The attributes must have the same
     * name, as well as the name of the class that is the name of the table.</b>
     *
     * @param object The fill object to delete into database.
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConnectionNotFoundException
     */
    public static void delete(Object object) throws IllegalAccessException, SQLException, ClassNotFoundException, ConnectionNotFoundException {
        delete(object, "");
    }

    /**
     * Delete the object in database by the where clause.
     * <b>The attributes must have the same
     * name, as well as the name of the class that is the name of the table.</b>
     *
     * @param object      The fill object to delete into database.
     * @param whereClause The where clause to delete. <b>Ex: "name LIKE J%"</b>
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConnectionNotFoundException
     */
    public static void delete(Object object, String whereClause) throws SQLException, ClassNotFoundException, IllegalAccessException, ConnectionNotFoundException {
        if (connection == null) {
            throw new ConnectionNotFoundException();
        }
        Class cls = Class.forName(object.getClass().getName());
        String table = cls.getSimpleName();
        String sql = "DELETE FROM " + table + " WHERE ";
        int idCode = 0;
        if (whereClause.isEmpty()) {
            String pk = getPK(cls);
            Field declaredFields[] = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                if (field.getName().equalsIgnoreCase(pk)) {
                    idCode = (int) field.get(object);
                    break;
                }
            }
            sql += pk + " = ?";
        } else {
            sql += whereClause;
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (whereClause.isEmpty()) {
                stmt.setInt(1, idCode);
            }
            stmt.execute();
        }
    }

    /**
     * Delete in database by primary key.
     *
     * @param tableName The name of table.
     * @param codeId    The code from primary key.
     * @throws SQLException
     * @throws ConnectionNotFoundException
     * @throws InvalidTypeArgsException
     */
    public static void delete(String tableName, int codeId) throws SQLException, ConnectionNotFoundException, InvalidTypeArgsException {
        delete(tableName, codeId, "");
    }

    /**
     * Delete in database by where clause.
     *
     * @param tableName   The name of table.
     * @param whereClause The where clause to delete. <b>Ex: "name LIKE J%"</b>
     * @throws SQLException
     * @throws ConnectionNotFoundException
     * @throws InvalidTypeArgsException
     */
    public static void delete(String tableName, String whereClause) throws SQLException, ConnectionNotFoundException, InvalidTypeArgsException {
        delete(tableName, 0, whereClause);
    }

    private static void delete(String tableName, int codeId, String whereClause) throws SQLException, ConnectionNotFoundException, InvalidTypeArgsException {
        if (connection == null) {
            throw new ConnectionNotFoundException();
        }
        if (codeId <= 0) {
            if (whereClause.isEmpty()) {
                throw new InvalidTypeArgsException();
            }
        }
        String pk = getPK(tableName);
        String sql = "DELETE FROM " + tableName;
        String where = " WHERE ";
        where += (whereClause.isEmpty()) ? pk + " = ?" : whereClause;
        sql += where;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (whereClause.isEmpty()) {
                stmt.setInt(1, codeId);
            }
            stmt.execute();
        }
    }
}

