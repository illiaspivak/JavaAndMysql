package sk.kosickaakademia.illiaspivak.mysql;

import sk.kosickaakademia.illiaspivak.mysql.entity.City;
import sk.kosickaakademia.illiaspivak.mysql.entity.Country;
import sk.kosickaakademia.illiaspivak.mysql.entity.Monument;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(Connect.getUrl(),Connect.getUsername(),Connect.getPassword());
        return conn;
    }

    public List<City> getCities(String country){
        String query = "SELECT city.Name, JSON_EXTRACT(Info,'$.Population') AS Population " +
                "FROM city " +
                "INNER JOIN country ON country.code = city.CountryCode " +
                "WHERE country.name LIKE ? ORDER BY Population DESC";
        List<City> list = new ArrayList<>();
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = getConnection();
            if(connection!=null){
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, country);
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    String name = rs.getString("Name");
                    int pop = rs.getInt("Population");
                    City city = new City(name,pop);
                    list.add(city);
                }
                connection.close();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return list;
    }

    public Country getCountryInfo (String country){
        String query = "SELECT country.name, country.code, city.name, " +
                " JSON_UNQUOTE(JSON_EXTRACT(doc, '$.geography.Continent')) AS Continent, " +
                " JSON_EXTRACT(doc, '$.geography.SurfaceArea') AS Area" +
                " FROM country " +
                " INNER JOIN city ON country.Capital = city.ID " +
                " INNER JOIN countryinfo ON country.code = countryinfo._id " +
                " WHERE country.name like ?";

        Country countryInfo = null;

        try {
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, country);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String code3=rs.getString("country.code");
                String capitalCity=rs.getString("city.name");
                String continent=rs.getString("continent");
                int area=rs.getInt("Area");
                countryInfo = new Country(country, code3, capitalCity,area, continent);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return countryInfo;
    }

    public String getCountryCode(String name)  {
        if(name==null || name.equalsIgnoreCase(""))
            return null;
        try {
            Connection con = getConnection();
            String query = "SELECT Code FROM country WHERE Name LIKE ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String code = rs.getString("Code");
                con.close();
                return code;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public void insertCity(City newCity) {

        String country = newCity.getCountry();
        String code3 = getCountryCode(country);
        if(code3==null){
            System.out.println("Warning! Country "+country+" does not exist! ");
        }
        else{
            newCity.setCode3(code3);
            String query = "INSERT INTO city (Name, CountryCode, District, Info) " +
                    "VALUES( ?,?,?,?) ";
            try {
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1,newCity.getName());
                ps.setString(2,newCity.getCode3());
                ps.setString(3,newCity.getDistrict());
                String json="{ \"Population\":"+newCity.getPopulation()+"}";
                ps.setString(4,json);
                boolean result= ps.execute();
                System.out.println("Result: "+result);
                con.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public boolean existCityInCountry(String country, String city){
        String query = "SELECT Name, CountryCode from city " +
                "WHERE CountryCode LIKE ?";
        try {
            Connection connection = getConnection();
            String code = getCountryCode(country);
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                if (city.equals(rs.getString("Name")))
                    return true;
            }
            connection.close();
        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("Such a city does not exist in this country");
        return false;
    }

    public void updatePopulation(String country, String city, int population){
        if(existCityInCountry(country, city)) {
            String query = "UPDATE city SET Info  = ? WHERE Name = ?";
            try {
                Connection con = getConnection();
                if (population <= 0) {
                    System.out.println("Error");
                    return;
                }
                String json = "{\"Population\": " + population + "}";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, json);
                ps.setString(2, city);
                ps.executeUpdate();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getCountryCodeInContinent(String continent){
        List<String> countries = new ArrayList<>();
        String query = "SELECT JSON_EXTRACT(doc, '$._id') AS Code, JSON_EXTRACT(doc, '$.geography.Continent') AS Con " +
                "FROM countryinfo " +
                "WHERE JSON_EXTRACT(doc, '$.geography.Continent') = ?";
        try {
            Connection connection = getConnection();
            if (connection != null){
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1 , continent);
                ResultSet rs = ps.executeQuery();
                while (rs.next()){
                    countries.add(rs.getString("Code"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return countries;
    }

    public Country getCapitalCity(String countryCode){
        String query = "SELECT Capital, country.Name, city.Name, JSON_EXTRACT(Info, '$.Population') AS Population " +
                "FROM country " +
                "INNER JOIN city ON country.Capital = city.ID " +
                "WHERE country.Code LIKE ?";
        Country continentInfo = null;
        try {
            Connection connection = getConnection();
            if (connection != null){
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, countryCode);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String name = rs.getString("city.Name");
                    String country = rs.getString("country.Name");
                    int pop = rs.getInt("Population");
                    continentInfo = new Country(country, name, pop);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return continentInfo;
    }

    public List<Country> getCapitalCities(String continent){
        List<Country> capitals = new ArrayList<>();
        for (String country : getCountryCodeInContinent(continent)){
            capitals.add(getCapitalCity(country.replace("\"", "")));
        }
        return capitals;
    }

    public int existCity(String code3, String cityName){
        if(code3==null || cityName == null || code3.equals("") || cityName.equals(""))
            return -1;

        String query = "SELECT id FROM city WHERE CountryCode LIKE ? AND name LIKE ? ";
        try {
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, code3);
            ps.setString(2, cityName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int id = rs.getInt("id");
                con.close();
                return id;
            }else{
                con.close();
                return -1;
            }

        }catch(Exception e){
        }
        return -1;
    }

    public boolean insertNewMonument( String code3, String city, String name ){
        if(name==null || name.equals(""))
            return false;
        int cityId=existCity(code3, city);
        if(cityId==-1) {
            System.out.println("Such a city does not exist in this country");
            return false;
        }
        String query = "INSERT INTO monument(name, city) VALUES (?, ?)";
        try {
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1,name);
            ps.setInt(2,cityId);
            ps.executeUpdate();
            con.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public List<Monument>  getMonuments() {
        String query= "SELECT monument.id AS id, monument.name AS name, city.Name AS city, country.Name AS country " +
                "       FROM monument " +
                "        INNER JOIN city ON city.ID = monument.city " +
                "         INNER JOIN country ON country.Code = city.CountryCode ";
        ArrayList<Monument> monuments = new ArrayList<>();
        try {
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String city = rs.getString("city");
                String country = rs.getString("country");
                Monument monument = new Monument(country, city, name, id);
                monuments.add(monument);
            }
            con.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return monuments;
    }
}
