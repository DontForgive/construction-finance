package br.com.galsystem.construction.finance.controller.user;
import br.com.galsystem.construction.finance.dto.user.UpdatePasswordRequest;
import br.com.galsystem.construction.finance.dto.user.UserCreateDTO;
import br.com.galsystem.construction.finance.dto.user.UserDTO;
import br.com.galsystem.construction.finance.mapper.UserMapper;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.response.PasswordUpdateResponse;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


@Tag(name = "Users", description = "Usuários")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<Response<Page<UserDTO>>> list(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @RequestParam(defaultValue = "id") final String sort,
            @RequestParam(defaultValue = "ASC") final String dir,
            @RequestParam(required = false) final String username,
            @RequestParam(required = false) final String email
    ) {
        final int safePage = Math.max(page, 0);
        final int safeSize = Math.min(Math.max(size, 1), 100);
        final Sort.Direction direction = "DESC".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        final Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, sort));

        final Page<UserDTO> result = userService.findByFilters(username, email, pageable);

        final Response<Page<UserDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de Usuários");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }

    // GET /users/{id} -> busca por id
    @GetMapping("/{id}")
    public ResponseEntity<Response<UserDTO>> getById(@PathVariable final Long id) {
        final Optional<User> opt = userService.findById(id);
        final Response<UserDTO> resp = new Response<>();

        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.getErros().add("Usuário não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        final User u = opt.get();
        final UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        // dto.setFullName(...);

        resp.setStatus(200);
        resp.setMessage("Usuário encontrado");
        resp.setData(dto);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/password")
    public ResponseEntity<PasswordUpdateResponse> updatePassword(@RequestBody UpdatePasswordRequest request) {
        Long userId = userService.getAuthenticatedUserId();
        PasswordUpdateResponse response = userService.updatePassword(
                userId,
                request.getPassword(),
                request.getNewPassword(),
                request.getConfirmNewPassword()
        );
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Response<UserDTO>> profile() {
        Long userId = userService.getAuthenticatedUserId();
        Optional<User> opt = userService.findById(userId);

        Response<UserDTO> resp = new Response<>();

        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.setMessage("Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        UserDTO dto = userMapper.toDTO(opt.get());
        resp.setStatus(200);
        resp.setMessage("Usuário encontrado");
        resp.setData(dto);

        return ResponseEntity.ok(resp);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Response<UserDTO>> updateProfile(@RequestBody UserDTO dto) {
        Long userId = userService.getAuthenticatedUserId();
        Optional<User> opt = userService.findById(userId);

        Response<UserDTO> resp = new Response<>();

        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.setMessage("Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        User user = opt.get();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());

        UserCreateDTO userDTO = userMapper.toUserCreateDTO(user);
        Response<UserDTO> updatedUser = userService.save(userDTO);
        UserDTO updatedDto = updatedUser.getData();

        resp.setStatus(200);
        resp.setMessage("Perfil atualizado com sucesso");
        resp.setData(updatedDto);

        return ResponseEntity.ok(resp);
    }
}
