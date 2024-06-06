package FEC.Backend.controllers;

import FEC.Backend.models.DonorReceipt;
import FEC.Backend.services.DonorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("donors")
public class DonorsController {
    @Autowired
    private DonorsService donorsService;

    @GetMapping("/name/{name}/city/{city}")
    public List<DonorReceipt> getDonorReceiptList(@PathVariable String name, @PathVariable String city){
        System.out.println(name + " " + city);
        return this.donorsService.getDonorReceiptList(name, city);
    }
}
