package projeto.collendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.collendar.dtos.request.CompartilhamentoRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
import projeto.collendar.dtos.response.CompartilhamentoResponseDTO;
import projeto.collendar.dtos.response.PermissaoResponseDTO;
import projeto.collendar.enums.TipoPermissao;
import projeto.collendar.exception.BusinessException;
import projeto.collendar.exception.ResourceNotFoundException;
import projeto.collendar.mappers.CalendarioMapper;
import projeto.collendar.mappers.CompartilhamentoMapper;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Compartilhamento;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CompartilhamentoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompartilhamentoService {

    private final CompartilhamentoRepository compartilhamentoRepository;
    private final CalendarioService calendarioService;
    private final UsuarioService usuarioService;

    @Transactional
    public CompartilhamentoResponseDTO create(CompartilhamentoRequestDTO dto) {
        Calendario calendario = calendarioService.findEntityById(dto.calendarioId());
        Usuario destinatario = usuarioService.findEntityByEmail(dto.emailDestinatario());

        if (calendario.getUsuario().getId().equals(destinatario.getId())) {
            throw new BusinessException("Não é possível compartilhar o calendário consigo mesmo");
        }

        if (compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, destinatario)) {
            throw new BusinessException("Calendário já compartilhado com este usuário");
        }

        Compartilhamento compartilhamento = CompartilhamentoMapper.toEntity(calendario, destinatario, dto.permissao());
        compartilhamentoRepository.save(compartilhamento);
        return CompartilhamentoMapper.toDTO(compartilhamento);
    }

    public CompartilhamentoResponseDTO findById(UUID id) {
        return compartilhamentoRepository.findById(id)
                .map(CompartilhamentoMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Compartilhamento", id.toString()));
    }

    public List<CompartilhamentoResponseDTO> listByCalendario(UUID calendarioId) {
        return compartilhamentoRepository.findByCalendarioId(calendarioId).stream()
                .map(CompartilhamentoMapper::toDTO)
                .toList();
    }

    public List<CalendarioResponseDTO> listSharedWithUsuario(UUID usuarioId) {
        return compartilhamentoRepository.findCalendariosCompartilhadosComUsuario(usuarioId).stream()
                .map(c -> CalendarioMapper.toDTO(c, false, getPermissao(c.getId(), usuarioId)))
                .toList();
    }

    public List<CompartilhamentoResponseDTO> listReceivedByUsuario(UUID usuarioId) {
        return compartilhamentoRepository.findByUsuarioId(usuarioId).stream()
                .map(CompartilhamentoMapper::toDTO)
                .toList();
    }

    @Transactional
    public CompartilhamentoResponseDTO updatePermissao(UUID id, TipoPermissao novaPermissao) {
        Compartilhamento compartilhamento = findEntityById(id);
        compartilhamento.setPermissao(novaPermissao);
        return CompartilhamentoMapper.toDTO(compartilhamentoRepository.save(compartilhamento));
    }

    @Transactional
    public void delete(UUID id) {
        if (!compartilhamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Compartilhamento", id.toString());
        }
        compartilhamentoRepository.deleteById(id);
    }

    @Transactional
    public void deleteByCalendarioAndUsuario(UUID calendarioId, UUID usuarioId) {
        compartilhamentoRepository.deleteByCalendarioIdAndUsuarioId(calendarioId, usuarioId);
    }

    public boolean hasAccess(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = calendarioService.findEntityById(calendarioId);

        if (calendario.getUsuario().getId().equals(usuarioId)) {
            return true;
        }

        Usuario usuario = usuarioService.findEntityById(usuarioId);
        return compartilhamentoRepository.existsByCalendarioAndUsuario(calendario, usuario);
    }

    public boolean canEdit(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = calendarioService.findEntityById(calendarioId);

        if (calendario.getUsuario().getId().equals(usuarioId)) {
            return true;
        }

        Usuario usuario = usuarioService.findEntityById(usuarioId);
        Optional<Compartilhamento> compartilhamento =
                compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuario);

        return compartilhamento.isPresent() &&
                compartilhamento.get().getPermissao() == TipoPermissao.EDITAR;
    }

    public PermissaoResponseDTO getMyPermission(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = calendarioService.findEntityById(calendarioId);

        if (calendario.getUsuario().getId().equals(usuarioId)) {
            return new PermissaoResponseDTO(true, true, true, null);
        }

        Usuario usuario = usuarioService.findEntityById(usuarioId);
        Optional<Compartilhamento> compartilhamento =
                compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuario);

        if (compartilhamento.isPresent()) {
            TipoPermissao permissao = compartilhamento.get().getPermissao();
            return new PermissaoResponseDTO(
                    false,
                    true,
                    permissao == TipoPermissao.EDITAR,
                    permissao
            );
        }

        return new PermissaoResponseDTO(false, false, false, null);
    }

    public long countByCalendario(UUID calendarioId) {
        return compartilhamentoRepository.findByCalendarioId(calendarioId).size();
    }

    public UUID getCalendarioIdByCompartilhamento(UUID compartilhamentoId) {
        Compartilhamento c = findEntityById(compartilhamentoId);
        return c.getCalendario().getId();
    }

    public UUID getDestinatarioIdByCompartilhamento(UUID compartilhamentoId) {
        Compartilhamento c = findEntityById(compartilhamentoId);
        return c.getUsuario().getId();
    }

    private Compartilhamento findEntityById(UUID id) {
        return compartilhamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compartilhamento", id.toString()));
    }

    private TipoPermissao getPermissao(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = calendarioService.findEntityById(calendarioId);
        Usuario usuario = usuarioService.findEntityById(usuarioId);
        return compartilhamentoRepository.findByCalendarioAndUsuario(calendario, usuario)
                .map(Compartilhamento::getPermissao)
                .orElse(null);
    }
}