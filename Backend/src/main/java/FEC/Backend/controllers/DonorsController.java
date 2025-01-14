package FEC.Backend.controllers;

import FEC.Backend.models.DonorReceipt;
import FEC.Backend.services.DonorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("donors")
public class DonorsController {
    @Autowired
    private DonorsService donorsService;

    @GetMapping("/name/{name}")
    public List<DonorReceipt> getDonorReceiptList(@PathVariable String name, @RequestParam(required = false)String city, @RequestParam(required = false) String state, @RequestParam(required = false) String zipcode){
        System.out.println(name + " " + city + " " + state + " " + zipcode);
        return this.donorsService.getDonorReceiptList(name, city, state, zipcode);
    }
}
