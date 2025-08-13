package br.com.galsystem.construction.finance.service.user;

import br.com.galsystem.construction.finance.dto.user.UserCreateDTO;
import br.com.galsystem.construction.finance.dto.user.UserDTO;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.repository.UserRepository;
import br.com.galsystem.construction.finance.response.Response;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public Response<UserDTO> save(UserCreateDTO userCreateDTO) {
        Response<UserDTO> resp = new Response<>();

        // Normalização
        String email = userCreateDTO.getEmail().trim().toLowerCase();
        String username = userCreateDTO.getUsername().trim();
        String fullName = userCreateDTO.getFullName().trim().toLowerCase();

        // Verificações de unicidade (pré-checagem)
        boolean emailExists = userRepository.existsByEmail(email);
        boolean usernameExists = userRepository.existsByUsername(username);

        if (emailExists || usernameExists) {
            resp.setStatus(409);
            resp.setMessage("Conflito ao cadastrar usuário.");
            if (emailExists)  resp.getErros().add("Já existe um usuário com este e-mail.");
            if (usernameExists) resp.getErros().add("Já existe um usuário com este username.");
            return resp;
        }

        try {
            // Entidade
            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setFullName(fullName);
            user.setPasswordHash(passwordEncoder.encode(userCreateDTO.getPassword()));

            User saved = userRepository.save(user);
            // DTO de saída
            UserDTO dto = new UserDTO(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getFullName());
            resp.setStatus(201);
            resp.setMessage("Usuário cadastrado com sucesso!");
            resp.setData(dto);
            return resp;

        } catch (DataIntegrityViolationException e) {
            resp.setStatus(409);
            resp.setMessage("Conflito ao cadastrar usuário.");
            resp.getErros().add("E-mail ou username já cadastrado.");
            return resp;
        }
    }
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
