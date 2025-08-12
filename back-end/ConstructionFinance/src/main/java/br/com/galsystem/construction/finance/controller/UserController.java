package br.com.galsystem.construction.finance.controller;

import br.com.galsystem.construction.finance.dto.UserDTO;
import br.com.galsystem.construction.finance.models.User;
import br.com.galsystem.construction.finance.response.Response;
import br.com.galsystem.construction.finance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /users -> lista todos (pode paginar depois)
    @GetMapping
    public ResponseEntity<Response<List<UserDTO>>> listAll() {
        List<User> users = userService.findAll();
        List<UserDTO> dtos = new ArrayList<>(users.size());
        for (User u : users) {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            // dto.setFullName(...); // se/quando existir na entidade
            dtos.add(dto);
        }
        Response<List<UserDTO>> resp = new Response<>();
        resp.setStatus(200);
        resp.setMessage("Lista de usuários");
        resp.setData(dtos);
        return ResponseEntity.ok(resp);
    }

    // GET /users/{id} -> busca por id
    @GetMapping("/{id}")
    public ResponseEntity<Response<UserDTO>> getById(@PathVariable Long id) {
        Optional<User> opt = userService.findById(id);
        Response<UserDTO> resp = new Response<>();

        if (opt.isEmpty()) {
            resp.setStatus(404);
            resp.getErros().add("Usuário não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        User u = opt.get();
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        // dto.setFullName(...);

        resp.setStatus(200);
        resp.setMessage("Usuário encontrado");
        resp.setData(dto);
        return ResponseEntity.ok(resp);
    }

    // DELETE /users/{id} -> remove por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // opcional: verificar existência antes e retornar 404 se não existir
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
