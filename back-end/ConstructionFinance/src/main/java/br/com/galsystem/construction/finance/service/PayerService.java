package br.com.galsystem.construction.finance.service;

import br.com.galsystem.construction.finance.dto.payer.PayerCreateDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerDTO;
import br.com.galsystem.construction.finance.dto.payer.PayerUpdateDTO;
import br.com.galsystem.construction.finance.exception.BadRequestException;
import br.com.galsystem.construction.finance.exception.ConflictException;
import br.com.galsystem.construction.finance.exception.ResourceNotFoundException;
import br.com.galsystem.construction.finance.models.Payer;
import br.com.galsystem.construction.finance.repository.PayerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public PayerDTO create(PayerCreateDTO dto) {
        String name = dto.getName().trim();

        if (payerRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um pagador com o nome informado.");
        }

        Payer entity = new Payer();
        entity.setName(name);

        try {
            return toDTO(payerRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Já existe um pagador com o nome informado.");
        }
    }

    @Transactional
    public PayerDTO update(Long id, PayerUpdateDTO dto) {
        Payer entity = payerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagador não encontrado."));

        String name = dto.getName().trim();

        // opcional para mensagem amigável (constraint no banco continua sendo a defesa final)
        if (payerRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Já existe um pagador com o nome informado.");
        }

        entity.setName(name);

        try {
            return toDTO(payerRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Já existe um pagador com o nome informado.");
        }
    }

    private PayerDTO toDTO(Payer p) {
        PayerDTO out = new PayerDTO();
        out.setId(p.getId());
        out.setName(p.getName());
        return out;
    }

    public void deleteById(Long id) {
        payerRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCase(String name) {
        return payerRepository.existsByNameIgnoreCase(name == null ? null : name.trim());
    }

    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return payerRepository.existsByNameIgnoreCaseAndIdNot(name == null ? null : name.trim(), id);
    }

}
