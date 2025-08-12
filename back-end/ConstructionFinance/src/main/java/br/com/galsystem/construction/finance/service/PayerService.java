package br.com.galsystem.construction.finance.service;

import br.com.galsystem.construction.finance.models.Payer;
import br.com.galsystem.construction.finance.repository.PayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PayerService {

    private final PayerRepository payerRepository;

    public PayerService(PayerRepository payerRepository) {
        this.payerRepository = payerRepository;
    }

    public List<Payer> findAll() {
        return payerRepository.findAll();
    }

    public Optional<Payer> findById(Long id) {
        return payerRepository.findById(id);
    }

    public Payer save(Payer payer) {
        return payerRepository.save(payer);
    }

    public void deleteById(Long id) {
        payerRepository.deleteById(id);
    }
}
