package br.com.galsystem.construction.finance.service.user;

import br.com.galsystem.construction.finance.dto.user.UserCreateDTO;
import br.com.galsystem.construction.finance.dto.user.UserDTO;
import br.com.galsystem.construction.finance.mapper.UserMapper;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.repository.UserRepository;
import br.com.galsystem.construction.finance.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    public Page<UserDTO> findByFilters(final String username, final String email, final Pageable pageable) {
        return userRepository.findByFilters(username, email, pageable).map(userMapper::toDTO);
    }

    public Optional<User> findById(final Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public Response<UserDTO> save(final UserCreateDTO userCreateDTO) {
        final Response<UserDTO> resp = new Response<>();

        // Normalização
        final String email = userCreateDTO.getEmail().trim().toLowerCase();
        final String username = userCreateDTO.getUsername().trim();
        final String fullName = userCreateDTO.getFullName().trim().toLowerCase();

        // Verificações de unicidade (pré-checagem)
        final boolean emailExists = userRepository.existsByEmail(email);
        final boolean usernameExists = userRepository.existsByUsername(username);

        if (emailExists || usernameExists) {
            resp.setStatus(409);
            resp.setMessage("Conflito ao cadastrar usuário.");
            if (emailExists) resp.getErros().add("Já existe um usuário com este e-mail.");
            if (usernameExists) resp.getErros().add("Já existe um usuário com este username.");
            return resp;
        }

        try {
            // Entidade
            final User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setFullName(fullName);
            user.setPasswordHash(passwordEncoder.encode(userCreateDTO.getPassword()));

            final User saved = userRepository.save(user);
            // DTO de saída
            final UserDTO dto = new UserDTO(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getFullName());
            resp.setStatus(201);
            resp.setMessage("Usuário cadastrado com sucesso!");
            resp.setData(dto);
            return resp;

        } catch (final DataIntegrityViolationException e) {
            resp.setStatus(409);
            resp.setMessage("Conflito ao cadastrar usuário.");
            resp.getErros().add("E-mail ou username já cadastrado.");
            return resp;
        }
    }

    public void deleteById(final Long id) {
        userRepository.deleteById(id);
    }
}
