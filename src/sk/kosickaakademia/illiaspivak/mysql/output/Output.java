package sk.kosickaakademia.illiaspivak.mysql.output;

import sk.kosickaakademia.illiaspivak.mysql.entity.City;
import sk.kosickaakademia.illiaspivak.mysql.entity.Country;
import sk.kosickaakademia.illiaspivak.mysql.entity.Monument;

import java.util.List;

public class Output {
    public void printCities(List<City> cities){
        System.out.println("List of cities: ");
        for(City c : cities){
            System.out.println("   - "+c.getName()+ " ( "+c.getPopulation()+" ) ");
        }
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println();
    }

    public void printCountryInfo(Country country){
        if(country==null){
            System.out.println("Country does not exist !");
        }
        else {
            System.out.println("Name: " + country.getName() + " ( " + country.getCode3() + " )");
            System.out.println("Capital city: " + country.getCapitalCity());
            System.out.println("Continent: " + country.getContinent());
            System.out.println("Surface area : " + country.getArea());
        }
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println();
    }

    public void printCapitalCity(Country country){
        if(country==null){
            System.out.println("Country does not exist !");
        }
        else {
            System.out.println(country.getName());
            System.out.println(country.getCapitalCity());
            System.out.println(country.getPop());
        }
    }

    public void printCountryInContinent(List<String> country){
        System.out.println("List of country: ");
        for(String c : country){
            System.out.println("  - " + c);
        }
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println();
    }

    public void printCapitalCities(List <Country> list){
        for (Country cap : list){
            System.out.println("Country: " + cap.getName() + "; Capital city: " + cap.getCapitalCity() + "; Population: " + cap.getPop());
        }
    }

    public void printMonuments(List <Monument> list){
        for (Monument monument : list){
            System.out.println("   - " + monument.getId() + " " + monument.getName() + " " + monument.getCountry() + " " + monument.getCity());
        }
    }


}
