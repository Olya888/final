package com.example.springsecurityapplication.controllers;
import com.example.springsecurityapplication.models.Image;
import com.example.springsecurityapplication.models.Order;
import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.CategoryRepository;
import com.example.springsecurityapplication.repositories.OrderRepository;
import com.example.springsecurityapplication.security.PersonDetails;
import com.example.springsecurityapplication.services.ProductService;
import com.example.springsecurityapplication.util.ProductValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.springsecurityapplication.enumm.Status;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Value("${upload.path}")
    private String uploadPath;

    private final ProductValidator productValidator;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public AdminController(ProductValidator productValidator, ProductService productService, CategoryRepository categoryRepository, OrderRepository orderRepository) {
        this.productValidator = productValidator;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
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

    // ?????????? ???? ?????????????????????? ?????????????? ???????????????? ???????????????????????????? ?? ?????????????? ??????????????
    @GetMapping()
    public String admin(Model model){
        if(this.getUserRole().equals("ROLE_USER")){
            return "redirect:/index";
        }
        model.addAttribute("role", this.getUserRole());
        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/admin";
    }

    // ?????????? ???? ?????????????????????? ?????????? ????????????????????
    @GetMapping("/product/add")
    public String addProduct(Model model){
        model.addAttribute("role", this.getUserRole());
        model.addAttribute("product", new Product());
        model.addAttribute("category", categoryRepository.findAll());
        return "product/addProduct";
    }

    // ?????????? ???? ???????????????????? ?????????????? ?? ?????????? ?? ?????????????? product
    @PostMapping("/product/add")
    public String addProduct(
            @ModelAttribute("product")
            @Valid Product product, BindingResult bindingResult,
            @RequestParam("file_one") MultipartFile file_one,
            @RequestParam("file_two") MultipartFile file_two,
            @RequestParam("file_three") MultipartFile file_three,
            @RequestParam("file_four") MultipartFile file_four,
            @RequestParam("file_five") MultipartFile file_five
    ) throws IOException {
        productValidator.validate(product, bindingResult);
        if(bindingResult.hasErrors()){
            return "product/addProduct";
        }

        File uploadDir = new File(uploadPath);
        if(!uploadDir.exists()){
            uploadDir.mkdir();
        }

        MultipartFile[] productImages = new MultipartFile[]{ file_one, file_two, file_three, file_four, file_five };

        for (MultipartFile productImage : productImages){
            if(productImage != null && !productImage.isEmpty()){
                String resultFileName = productImage.getOriginalFilename();
                try {
                    String fileName = uploadPath + "/" + productImage.getOriginalFilename();
                    String file = new File(fileName).getAbsolutePath();
                    productImage.transferTo(new File(file));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Image image = new Image();
                image.setProduct(product);
                image.setFileName(resultFileName);
                product.addImageProduct(image);
            }
        }

        productService.saveProduct(product);
        return "redirect:/admin";
    }

    // ?????????? ???? ???????????????? ???????????? ???? id
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") int id){
        productService.deleteProduct(id);
        return "redirect:/admin";
    }

    // ?????????? ???? ?????????????????? ???????????? ???? id ?? ?????????????????????? ?????????????? ????????????????????????????
    @GetMapping("/product/edit/{id}")
    public String editProduct(@PathVariable("id") int id, Model model){
        model.addAttribute("role", this.getUserRole());
        model.addAttribute("editProduct", productService.getProductId(id));
        model.addAttribute("category", categoryRepository.findAll());
        return "product/editProduct";
    }

    @PostMapping("/product/edit/{id}")
    public String editProduct(@ModelAttribute("editProduct") Product product, @PathVariable("id") int id){
        productService.updateProduct(id, product);
        return "redirect:/admin";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("role", this.getUserRole());
        return "admin/orders";
    }

    @GetMapping("/order/accept/{id}")
    public String setOrderAccept(Model model, @PathVariable("id") int id){
        Order order = orderRepository.getById(id);
        order.setStatus(Status.????????????);
        orderRepository.save(order);

        return "redirect:/admin/orders";
    }

    @GetMapping("/order/decline/{id}")
    public String setOrderDecline(Model model, @PathVariable("id") int id){
        Order order = orderRepository.getById(id);
        order.setStatus(Status.??????????????);
        orderRepository.save(order);

        return "redirect:/admin/orders";
    }

    @GetMapping("/order/wait/{id}")
    public String setOrderWait(Model model, @PathVariable("id") int id){
        Order order = orderRepository.getById(id);
        order.setStatus(Status.??????????????);
        orderRepository.save(order);

        return "redirect:/admin/orders";
    }

    @GetMapping("/order/received/{id}")
    public String setOrderReceived(Model model, @PathVariable("id") int id){
        Order order = orderRepository.getById(id);
        order.setStatus(Status.??????????????);
        orderRepository.save(order);

        return "redirect:/admin/orders";
    }
}
