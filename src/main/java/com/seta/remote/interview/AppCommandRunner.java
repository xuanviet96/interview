package com.seta.remote.interview;

import com.seta.remote.interview.models.City;
import com.seta.remote.interview.models.Country;
import com.seta.remote.interview.models.Director;
import com.seta.remote.interview.models.Movie;
import com.seta.remote.interview.models.entity.Customer;
import com.seta.remote.interview.models.entity.Order;
import com.seta.remote.interview.models.entity.Product;
import com.seta.remote.interview.repos.CustomerRepo;
import com.seta.remote.interview.repos.OrderRepo;
import com.seta.remote.interview.repos.ProductRepo;
import com.seta.remote.interview.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AppCommandRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("running runner");
        basicStreamApi();
        advanceStreamApi();

    }
    @Autowired
    ProductRepo productRepo;
    @Autowired
    OrderRepo orderRepo;
    @Autowired
    CustomerRepo customerRepo;
    private static final CountryDao countryDao = InMemoryWorldDao.getInstance();
    private static final CityDao cityDao = InMemoryWorldDao.getInstance();
    private static final MovieService movieService = InMemoryMovieService.getInstance();


    private void basicStreamApi() {

        List<Product> productsResult = new ArrayList<>();
        List<Order> ordersResult = new ArrayList<>();
        List<Customer> customersResult = new ArrayList<>();

//        1 — Obtain a list of products belongs to category “Books” with price > 100

        productsResult = productRepo.findAll().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase("Books"))
                .filter(product -> product.getPrice() > 100)
                .collect(Collectors.toList());
        System.out.println(productsResult.size());

//        2 — Obtain a list of order with products belong to category “Baby”
        ordersResult = orderRepo.findAll()
                .stream()
                .filter(order ->
                    order.getProducts()
                    .stream()
                    .anyMatch(product -> product.getCategory().equalsIgnoreCase("Baby"))
                ).collect(Collectors.toList());
        System.out.println(ordersResult.size());

//        3 — Obtain a list of product with category = “Toys” and then apply 10% discount
        productsResult = productRepo.findAll()
                .stream()
                .filter(product -> product.getCategory().equalsIgnoreCase("Toys"))
                .map(product -> product.withPrice(product.getPrice() * 0.9))
                .collect(Collectors.toList());

//        4 — Obtain a list of products ordered by customer of tier 2 between 01-Feb-2021 and 01-Apr-2021
        productsResult = orderRepo.findAll()
                .stream()
                .filter(o -> o.getCustomer().getTier() == 2)
                .filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 2, 1)) >= 0)
                .filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 4, 1)) <= 0)
                .flatMap(o -> o.getProducts().stream())
                .distinct()
                .collect(Collectors.toList());

//        5 — Get the cheapest products of “Books” category
        Optional<Product> result = productRepo.findAll()
                .stream()
                .filter(p -> p.getCategory().equalsIgnoreCase("Books"))
                .min(Comparator.comparing(Product::getPrice));
//        6 — Get the 3 most recent placed order
        ordersResult = orderRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(3)
                .collect(Collectors.toList());
//        7 — Get a list of orders which were ordered on 15-Mar-2021, log the order records to the console and then return its product list
//
        productsResult = orderRepo.findAll()
                .stream()
                .filter(o -> o.getOrderDate().isEqual(LocalDate.of(2021, 3, 15)))
                .peek(o -> System.out.println(o.toString()))
                .flatMap(o -> o.getProducts().stream())
                .distinct()
                .collect(Collectors.toList());
//        8 — Calculate total lump sum of all orders placed in Feb 2021
        Double result_8 = orderRepo.findAll()
                .stream()
                .filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 2, 1)) >= 0)
                .filter(o -> o.getOrderDate().compareTo(LocalDate.of(2021, 3, 1)) < 0)
                .flatMap(o -> o.getProducts().stream())
                .mapToDouble(p -> p.getPrice())
                .sum();
//        9 — Calculate order average payment placed on 14-Mar-2021
        Double result_9 = orderRepo.findAll()
                .stream()
                .filter(o -> o.getOrderDate().isEqual(LocalDate.of(2021, 3, 15)))
                .flatMap(o -> o.getProducts().stream())
                .mapToDouble(p -> p.getPrice())
                .average().getAsDouble();

//        10 — Obtain a collection of statistic figures (i.e. sum, average, max, min, count) for all products of category “Books”
        DoubleSummaryStatistics statistics = productRepo.findAll()
                .stream()
                .filter(p -> p.getCategory().equalsIgnoreCase("Books"))
                .mapToDouble(p -> p.getPrice())
                .summaryStatistics();

        System.out.println(String.format("count = %1$d, average = %2$f, max = %3$f, min = %4$f, sum = %5$f",
                statistics.getCount(), statistics.getAverage(), statistics.getMax(), statistics.getMin(), statistics.getSum()));

//        11 — Obtain a data map with order id and order’s product count
        Map<Long, Integer> result_11 = orderRepo.findAll()
                .stream()
                .collect(
                        Collectors.toMap(
                                order -> order.getId(),
                                order -> order.getProducts().size()
                        )
                );
//        12 — Produce a data map with order records grouped by customer
        Map<Customer, List<Order>> result_12 = orderRepo.findAll()
                .stream()
                .collect(
                        Collectors.groupingBy(Order::getCustomer)
                );
//        13 — Produce a data map with order record and product total sum
        Map<Order, Double> result_13 = orderRepo.findAll()
                .stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                order -> order.getProducts().stream()
                                        .mapToDouble(p -> p.getPrice()).sum()
                        )
                );
//        14 — Obtain a data map with list of product name by category
        Map<String, List<String>> result_14 = productRepo.findAll()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                Product::getCategory,
                                Collectors.mapping(product -> product.getName(), Collectors.toList()))
                );
//        15 — Get the most expensive product by category
        Map<String, Optional<Product>> result_15 = productRepo.findAll()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                Product::getCategory,
                                Collectors.maxBy(Comparator.comparing(Product::getPrice))));
    }

    private void advanceStreamApi() {
//        Find the highest populated city of each country:
        List<Optional<City>> maxPopolatedCityEachCountry= countryDao.findAllCountries()
                .stream()
                .map(c -> c.getCities().stream().max(Comparator.comparing(City::getPopulation)))
                .collect(Collectors.toList());

//        Find the most populated city of each continent

//        Find the number of movies of each director: Try to solve this problem by assuming that Director class has not the member movies.
        final Collection<Movie> movies = movieService.findAllMovies();
        Map<String, Long> dirMovCounts =
                movies.stream().map(Movie::getDirectors)
                        .flatMap(Collection::stream)
                        .collect(Collectors.groupingBy(
                                        Director::getName,
                                        Collectors.counting()
                                )
                        );
        dirMovCounts.forEach(
                (name, count) -> System.out.printf("%20s: %3d\n", name, count));
//        Find the number of genres of each director's movies:

//        Find the highest populated capital city:
        var highPopulatedCapitalCity =
                countryDao.findAllCountries()
                        .stream()
                        .map(Country::getCapital)
                        .filter(Objects::nonNull)
                        .map(cityDao::findCityById)
                        .filter(Objects::nonNull)
                        .max(Comparator.comparing(City::getPopulation));
        highPopulatedCapitalCity.ifPresent(System.out::println);

    }
}

//        Find the highest populated capital city of each continent:


//        Sort the countries by number of their cities in desending order:

//        Find the list of movies having the genres "Drama" and "Comedy" only:

//        Group the movies by the year and list them:

//        Sort the countries by their population densities in descending order ignoring zero population countries:

