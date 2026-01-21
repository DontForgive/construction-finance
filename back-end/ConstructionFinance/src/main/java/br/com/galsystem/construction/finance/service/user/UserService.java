package br.com.galsystem.construction.finance.service.user;

import br.com.galsystem.construction.finance.dto.user.UserCreateDTO;
import br.com.galsystem.construction.finance.dto.user.UserDTO;
import br.com.galsystem.construction.finance.files.UploadArea;
import br.com.galsystem.construction.finance.mapper.UserMapper;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.repository.UserRepository;
import br.com.galsystem.construction.finance.response.PasswordUpdateResponse;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;


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
            final UserDTO dto = new UserDTO(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getFullName(), saved.getPhoneNumber(), saved.getProfilePictureUrl(), saved.getBannerUrl());
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

    @Transactional
    public PasswordUpdateResponse updatePassword(Long userId, String password, String newPassword, String confirmPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new PasswordUpdateResponse(404, "Usuário não encontrado");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return new PasswordUpdateResponse(401, "Senha atual incorreta");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            return new PasswordUpdateResponse(400, "A nova senha não pode ser igual à senha atual");
        }

        if (!newPassword.equals(confirmPassword)) {
            return new PasswordUpdateResponse(400, "As novas senhas não coincidem");
        }

        if (newPassword.length() < 6) {
            return new PasswordUpdateResponse(422, "A nova senha deve ter pelo menos 6 caracteres");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new PasswordUpdateResponse(200, "Senha alterada com sucesso");
    }

    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Usuário não autenticado");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User userDetails) {
            return userDetails.getId();
        } else if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            return userRepository.findByUsername(springUser.getUsername())
                    .map(User::getId)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        }
        throw new UsernameNotFoundException("Tipo de autenticação desconhecido");
    }

    @Transactional
    public Response<UserDTO> updateProfilePicture(final MultipartFile file) {
        final Long userId = getAuthenticatedUserId();
        final Response<UserDTO> resp = new Response<>();

        final User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            resp.setStatus(404);
            resp.setMessage("Usuário não encontrado");
            return resp;
        }

        final String oldUrl = user.getProfilePictureUrl();
        final String newUrl = fileStorageService.store(UploadArea.USERS, file);

        user.setProfilePictureUrl(newUrl);
        userRepository.save(user);

        if (oldUrl != null && !oldUrl.isBlank() && !oldUrl.equals(newUrl)) {
            fileStorageService.deleteByPublicUrl(oldUrl);
        }

        resp.setStatus(200);
        resp.setMessage("Foto de perfil atualizada com sucesso");
        resp.setData(userMapper.toDTO(user));
        return resp;
    }

    @Transactional
    public Response<UserDTO> updateBanner(final MultipartFile file) {
        final Long userId = getAuthenticatedUserId();
        final Response<UserDTO> resp = new Response<>();

        final User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            resp.setStatus(404);
            resp.setMessage("Usuário não encontrado");
            return resp;
        }

        final String oldUrl = user.getBannerUrl();
        final String newUrl = fileStorageService.store(UploadArea.USERS, file);

        user.setBannerUrl(newUrl);
        userRepository.save(user);

        if (oldUrl != null && !oldUrl.isBlank() && !oldUrl.equals(newUrl)) {
            fileStorageService.deleteByPublicUrl(oldUrl);
        }

        resp.setStatus(200);
        resp.setMessage("Banner atualizado com sucesso");
        resp.setData(userMapper.toDTO(user));
        return resp;
    }


}
