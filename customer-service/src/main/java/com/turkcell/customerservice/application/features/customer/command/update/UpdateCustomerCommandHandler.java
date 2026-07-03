package com.turkcell.customerservice.application.features.customer.command.update;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.commonlib.cqrs.CommandHandler;
import com.turkcell.commonlib.exception.ResourceNotFoundException;
import com.turkcell.customerservice.application.features.customer.mapper.CustomerMapper;
import com.turkcell.customerservice.dto.CustomerResponse;
import com.turkcell.customerservice.entity.Customer;
import com.turkcell.customerservice.repository.CustomerRepository;

@Component
public class UpdateCustomerCommandHandler implements CommandHandler<UpdateCustomerCommand, CustomerResponse> {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public UpdateCustomerCommandHandler(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    @CacheEvict(value = "customerById", key = "#command.id")
    public CustomerResponse handle(UpdateCustomerCommand command) {
        Customer customer = repository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", command.id().toString()));
        mapper.applyUpdate(customer, command);
        return mapper.toResponse(repository.save(customer));
    }
}
