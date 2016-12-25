package pku.ss.cyy.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pku.ss.cyy.bean.City;

public class CityDB {
    private static final String TAG = "MyAPP";
    public static final String CITY_DB_NAME = "city2.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;

    public CityDB(Context context, String path) {
        db = context.openOrCreateDatabase(CITY_DB_NAME,Context.MODE_PRIVATE,null);
    }

    public List<City> getAllCity() {
        List<City> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME + " order by allpy", null);
        while (c.moveToNext()) {
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, firstPY, allPY, allFirstPY);
            list.add(item);
        }
        return list;
    }

    public List<City> getAllCityByName() {
        List<City> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME + " order by number", null);
        while (c.moveToNext()) {
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, firstPY, allPY, allFirstPY);
            list.add(item);
        }
        return list;
    }

    public List<City> searchCityByName(String cityName) {
        List<City> list = new ArrayList<>();
//        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME + " WHERE city LIKE '%"+cityName+"%' OR allpy LIKE '%"+cityName+"%' OR allfirstpy LIKE '%"+cityName+"%'",
//                null);
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME + " WHERE city LIKE ? OR allpy LIKE ? OR allfirstpy LIKE ?",
                new String[] { "%"+cityName+"%", cityName+"%", cityName+"%" });
        while (c.moveToNext()) {
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, firstPY, allPY, allFirstPY);
            list.add(item);
        }
        return list;
    }

    public City searchCityById(String id) {
        City city = null;
//        List<City> list = new ArrayList<>();
//        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME + " WHERE city LIKE '%"+cityName+"%' OR allpy LIKE '%"+cityName+"%' OR allfirstpy LIKE '%"+cityName+"%'",
//                null);
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME + " WHERE number = ?",
                new String[] { id });
        while (c.moveToNext()) {
            String province = c.getString(c.getColumnIndex("province"));
            String cityName = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            city = new City(province, cityName, number, firstPY, allPY, allFirstPY);
        }
        return city;
    }

    public List<City> getAllProvince() {
        List<City> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT DISTINCT province from " + CITY_TABLE_NAME , null);
        while (c.moveToNext()) {
            String province = c.getString(c.getColumnIndex("province"));
//            String city = c.getString(c.getColumnIndex("city"));
//            String number = c.getString(c.getColumnIndex("number"));
//            String allPY = c.getString(c.getColumnIndex("allpy"));
//            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
//            String firstPY = c.getString(c.getColumnIndex("firstpy"));
//            City item = new City(province, city, number, firstPY, allPY, allFirstPY);
            City item = new City(province, "", "", "", "", "");
            list.add(item);
        }
        return list;
    }

    public List<City> getCityByProvince(String province) {
        List<City> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME  + " WHERE province=?",
                new String[] { province });
        while (c.moveToNext()) {
//            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, firstPY, allPY, allFirstPY);
            list.add(item);
        }
        return list;
    }

    public City getCity(String province, String district) {
        List<City> list = new ArrayList<>();
        City item = null;
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME  + " WHERE province LIKE ? AND city LIKE ?",
                new String[] { "%"+province+"%", "%"+district+"%" });
        while (c.moveToNext()) {
//            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            item = new City(province, city, number, firstPY, allPY, allFirstPY);
            //list.add(item);
        }
        return item;
    }
}
