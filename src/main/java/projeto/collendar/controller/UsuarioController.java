package projeto.collendar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.collendar.dtos.request.UsuarioRequestDTO;
import projeto.collendar.dtos.response.UsuarioResponseDTO;
import projeto.collendar.service.UsuarioService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(
        name = "Usuários",
        description = "Endpoints para gerenciamento de usuários do sistema. " +
                "Permite criar, consultar, atualizar e gerenciar usuários e suas roles."
)
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria um novo usuário no sistema. Este é o único endpoint de usuário que não requer autenticação. " +
                    "Após o registro, utilize o endpoint /auth/login para obter o token de acesso.",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário criado com sucesso. Retorna os dados do usuário recém-criado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos. Verifique se: email é válido, senha tem mínimo 6 caracteres, email não está em uso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<UsuarioResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do usuário a ser registrado",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UsuarioRequestDTO.class),
                            mediaType = "application/json"
                    )
            )
            @RequestBody @Valid UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(dto));
    }

    @GetMapping
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista completa de todos os usuários cadastrados no sistema.",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado. Token JWT não fornecido ou inválido.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<UsuarioResponseDTO>> listAll() {
        return ResponseEntity.ok(usuarioService.listAll());
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os dados de um usuário específico através do seu identificador único.",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado com o ID fornecido.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<UsuarioResponseDTO> findById(
            @Parameter(
                    description = "ID único do usuário (UUID)",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @GetMapping("/email/{email}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Buscar usuário por email",
            description = "Retorna os dados de um usuário através do seu endereço de email.",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado com o email fornecido.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<UsuarioResponseDTO> findByEmail(
            @Parameter(
                    description = "Email do usuário",
                    required = true,
                    example = "usuario@email.com"
            )
            @PathVariable String email) {
        return ResponseEntity.ok(usuarioService.findByEmail(email));
    }

    @GetMapping("/ativos")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Listar usuários ativos",
            description = "Retorna apenas os usuários que estão com status ativo no sistema.",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários ativos retornada com sucesso.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<UsuarioResponseDTO>> listAtivos() {
        return ResponseEntity.ok(usuarioService.listAtivos());
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Atualizar dados do usuário",
            description = "Atualiza as informações de um usuário existente. " +
                    "Permite alterar nome, email e senha (se fornecida).",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou email já cadastrado para outro usuário.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<UsuarioResponseDTO> update(
            @Parameter(
                    description = "ID do usuário a ser atualizado",
                    required = true
            )
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados do usuário",
                    required = true
            )
            @RequestBody @Valid UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.update(id, dto));
    }

    @PatchMapping("/{id}/desativar")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Desativar usuário",
            description = "Desativa um usuário no sistema sem excluí-lo permanentemente. " +
                    "Usuários desativados não podem fazer login.",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário desativado com sucesso.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> deactivate(
            @Parameter(
                    description = "ID do usuário a ser desativado",
                    required = true
            )
            @PathVariable UUID id) {
        usuarioService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Excluir usuário permanentemente",
            description = "Remove permanentemente um usuário do sistema. " +
                    "Esta ação não pode ser desfeita. Use 'desativar' para manter o histórico.",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuário excluído com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "ID do usuário a ser excluído",
                    required = true
            )
            @PathVariable UUID id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/roles")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Adicionar role ao usuário",
            description = "Adiciona uma role (permissão) a um usuário específico. " +
                    "Roles disponíveis: USER, ADMIN.",
            tags = {"Usuários"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role adicionada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário ou role não encontrado.",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<UsuarioResponseDTO> addRole(
            @Parameter(
                    description = "ID do usuário",
                    required = true
            )
            @PathVariable UUID id,
            @Parameter(
                    description = "Nome da role a ser adicionada (USER, ADMIN)",
                    required = true,
                    example = "ADMIN"
            )
            @RequestParam String role) {
        return ResponseEntity.ok(usuarioService.addRole(id, role));
    }
}