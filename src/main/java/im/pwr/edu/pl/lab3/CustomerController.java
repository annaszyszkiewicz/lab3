package im.pwr.edu.pl.lab3;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    public Iterable<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("/customers/{customerId}")
    public Customer getUserById(@PathVariable Long customerId) {
        return customerRepository.findById(customerId).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "entity not found"
                ));
    }
}
