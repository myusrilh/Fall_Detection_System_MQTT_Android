package helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "FallDetectionUser.db";

    // User table name
    private static final String TABLE_USER = "user";

    // User Table Columns names
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_USER_PATIENT_NAME = "user_patient_name";
    private static final String COLUMN_USER_STREET = "user_street";
    private static final String COLUMN_USER_CITY = "user_city";
    private static final String COLUMN_USER_COUNTRY = "user_country";
    private static final String COLUMN_USER_POSTAL_CODE = "user_postalcode";
    private static final String COLUMN_USER_PASSWORD = "user_password";
    private static final String COLUMN_USER_ROLE = "user_role";

    // create table sql query
    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_USER_PATIENT_NAME + " TEXT," + COLUMN_USER_STREET + " TEXT,"
            + COLUMN_USER_COUNTRY + " TEXT," + COLUMN_USER_CITY + " TEXT,"
            + COLUMN_USER_PASSWORD + " TEXT," + COLUMN_USER_POSTAL_CODE + " TEXT,"
            + COLUMN_USER_ROLE + " TEXT" + ")";

    // drop table sql query
    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_USER;

    /**
     * Constructor
     */

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop User Table if exist
        db.execSQL(DROP_USER_TABLE);
        // Create tables again
        onCreate(db);
    }

    /**
     * This method is to create user record
     */
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_PATIENT_NAME, user.getPatientname());
        values.put(COLUMN_USER_STREET, user.getStreet());
        values.put(COLUMN_USER_CITY, user.getCity());
        values.put(COLUMN_USER_COUNTRY, user.getCountry());
        values.put(COLUMN_USER_POSTAL_CODE, user.getPostalCode());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());
        values.put(COLUMN_USER_ROLE, user.getRole());
        // Inserting Row
        db.insert(TABLE_USER, null, values);
        db.close();
    }


    public List<User> getAllPatient() {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_NAME,
                COLUMN_USER_PATIENT_NAME,
                COLUMN_USER_PASSWORD,
                COLUMN_USER_STREET,
                COLUMN_USER_CITY,
                COLUMN_USER_COUNTRY,
                COLUMN_USER_POSTAL_CODE,
                COLUMN_USER_ROLE
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_PATIENT_NAME + " ASC";
        List<User> userList = new ArrayList<User>();
        SQLiteDatabase db = this.getReadableDatabase();
        // query the user table
        String selection = COLUMN_USER_PATIENT_NAME + " != ?";
        String[] selectionArgs = {"NoPatient"};
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id,user_name,user_email,user_password FROM user ORDER BY user_name;
         */
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                selection,        //columns for the WHERE clause
                selectionArgs,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        // Traversing through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
                user.setPatientname(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PATIENT_NAME)));
                user.setStreet(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_STREET)));
                user.setCity(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_CITY)));
                user.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_COUNTRY)));
                user.setPostalCode(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_POSTAL_CODE)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE)));
                // Adding user record to list
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return user list
        return userList;
    }

    /**
     * This method to update user record
     */
    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_PATIENT_NAME, user.getPatientname());
        values.put(COLUMN_USER_STREET, user.getStreet());
        values.put(COLUMN_USER_CITY, user.getCity());
        values.put(COLUMN_USER_COUNTRY, user.getCountry());
        values.put(COLUMN_USER_POSTAL_CODE, user.getPostalCode());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());
        // updating row
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }

    public void deleteUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete user record by id
        db.delete(TABLE_USER, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }

    /**
     * This method to get user role
     *
     * @return user object
     */
    public User getUserRole(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_NAME,
                COLUMN_USER_PATIENT_NAME,
                COLUMN_USER_PASSWORD,
                COLUMN_USER_STREET,
                COLUMN_USER_CITY,
                COLUMN_USER_COUNTRY,
                COLUMN_USER_POSTAL_CODE,
                COLUMN_USER_ROLE
        };

        String selection = COLUMN_USER_NAME + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        User user = new User();

        Cursor cursor = db.query(
                TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
//        int cursorCount = cursor.getCount();

        // Traversing through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                user.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
                user.setPatientname(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PATIENT_NAME)));
                user.setStreet(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_STREET)));
                user.setCity(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_CITY)));
                user.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_COUNTRY)));
                user.setPostalCode(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_POSTAL_CODE)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE)));
            } while (cursor.moveToNext());
        } else {
            return null;
        }
        cursor.close();
        db.close();
        return user;

    }

    /**
     * This method to check user exist or not
     *
     * @return true/false
     */
    public boolean checkUser(String username, String role) {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getReadableDatabase();
        // selection criteria
        String selection = COLUMN_USER_NAME + " = ?" + " AND " + COLUMN_USER_ROLE + " = ?";
        // selection argument
        String[] selectionArgs = {username, role};
        // query user table with condition
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id FROM user WHERE user_email = 'jack@androidtutorialshub.com';
         */
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,                    //columns to return
                selection,                  //columns for the WHERE clause
                selectionArgs,              //The values for the WHERE clause
                null,                       //group the rows
                null,                      //filter by row groups
                null);                      //The sort order
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        return cursorCount > 0;
    }

    /**
     * This method to check user exist or not
     *
     * @return true/false
     */
    public boolean checkUser(String street, String city, String country, String postalcode) {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID
        };
        SQLiteDatabase db = this.getReadableDatabase();
        // selection criteria
        String selection = COLUMN_USER_STREET + " = ?" + " AND " + COLUMN_USER_CITY + " = ?" + " AND " + COLUMN_USER_COUNTRY + " = ?" + " AND " + COLUMN_USER_POSTAL_CODE + " = ?";
        // selection arguments
        String[] selectionArgs = {street, city, country, postalcode};
        // query user table with conditions
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id FROM user WHERE user_email = 'jack@androidtutorialshub.com' AND user_password = 'qwerty';
         */
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,                    //columns to return
                selection,                  //columns for the WHERE clause
                selectionArgs,              //The values for the WHERE clause
                null,                       //group the rows
                null,                       //filter by row groups
                null);                      //The sort order
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        return cursorCount > 0;
    }

}
