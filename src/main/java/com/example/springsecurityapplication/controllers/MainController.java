package com.example.springsecurityapplication.controllers;

import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.CategoryRepository;
import com.example.springsecurityapplication.repositories.ProductRepository;
import com.example.springsecurityapplication.security.PersonDetails;
import com.example.springsecurityapplication.services.ProductService;
import groovy.transform.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/")
public class MainController {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public MainController(ProductRepository productRepository, ProductService productService, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    public String getUserRole(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
            return personDetails.getPerson().getRole();
        } catch(Exception e) {
            return null;
        }
    }

    // Данный метод предназначен для отображении товаров без прохождения аутентификации и авторизации
    @GetMapping("/")
    public String index(Model model){
        if(this.getUserRole() != null){
            return "redirect:/index";
        }

        model.addAttribute("role", this.getUserRole());
        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("categories", categoryRepository.findAll());
        return "product/product";
    }

    // Данный метод предназначен для отображении товаров без прохождения аутентификации и авторизации
    @GetMapping("/product")
    public String getAllProduct(Model model){
        model.addAttribute("role", this.getUserRole());
        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("categories", categoryRepository.findAll());
        return "product/product";
    }

    @GetMapping("/product/info/{id}")
    public String infoProduct(@PathVariable("id") int id, Model model){
        model.addAttribute("role", this.getUserRole());
        model.addAttribute("product", productService.getProductId(id));
        return "product/infoProduct";
    }

    @GetMapping("error")
    public String error(Model model){
        return "error";
    }

    @PostMapping("/product/search")
    public String productSearch(@RequestParam("search") String search, @RequestParam(name = "ot", required = false, defaultValue = "0") Integer ot, @RequestParam(name = "do", required = false, defaultValue = "999999") Integer Do, @RequestParam(value = "price", required = false, defaultValue = "") String price, @RequestParam(value = "category", required = false, defaultValue = "") String category, Model model) {
        System.out.println("search: " + search);
        System.out.println("ot: " + ot);
        System.out.println("Do: " + Do);
        System.out.println("price: " + price);
        System.out.println("category: " + category);

        if (!price.isEmpty()) {
            System.out.println("ПЕРВЫЙ ИФЧИК");
            if (price.equals("sorted_by_ascending_price")) {
                System.out.println("ПЕРВЫЙ ИФЧИК");
                if (!category.isEmpty()) {
                    System.out.println("ПЕРВЫЙ ИФЧИК");
                    model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPrice(search.toLowerCase(), ot, Do, Integer.parseInt(category)));
                } else {
                    System.out.println("ВТОРОЙ ИФЧИК");
                    model.addAttribute("search_product", productRepository.findByTitleOrderByPrice(search.toLowerCase(), ot, Do));
                }
            } else if (price.equals("sorted_by_descending_price")) {
                System.out.println("ВТОРОЙ ИФЧИК");
                if (!category.isEmpty()) {
                    System.out.println("ПЕРВЫЙ ИФЧИК");
                    model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), ot, Do, Integer.parseInt(category)));
                } else {
                    System.out.println("ВТОРОЙ ИФЧИК");
                    model.addAttribute("search_product", productRepository.findByTitleOrderByPriceDesc(search.toLowerCase(), ot, Do));
                }
            }
        } else {
            System.out.println("ВТОРОЙ ИФЧИК");
            if (!category.isEmpty()) {
                System.out.println("ПЕРВЫЙ ИФЧИК");
                model.addAttribute("search_product", productRepository.findByTitleAndPriceGreaterThanEqualAndPriceLessThan(search.toLowerCase(), ot, Do, Integer.parseInt(category)));
            } else {
                System.out.println("ВТОРОЙ ИФЧИК");
                System.out.println("ОТ: " + ot);
                System.out.println("DO: " + Do);
                System.out.println("search: " + search);

                model.addAttribute("search_product", productRepository.findByTitleAndFromToWithoutSort(search.toLowerCase(), ot, Do));
            }
        }

        int filter_category = -1;
        if (!category.isEmpty()) {
            filter_category = Integer.parseInt(category);
        }

        model.addAttribute("role", this.getUserRole());
        model.addAttribute("filter_name", search);
        model.addAttribute("filter_category", filter_category);
        model.addAttribute("filter_price_from", ot);
        model.addAttribute("filter_price_to", Do);
        model.addAttribute("filter_sort", price);

        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("categories", categoryRepository.findAll());
        return "/product/product";
    }
}
