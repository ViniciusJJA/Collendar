package projeto.collendar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projeto.collendar.dtos.request.CalendarioRequestDTO;
import projeto.collendar.dtos.response.CalendarioResponseDTO;
import projeto.collendar.exception.ResourceNotFoundException;
import projeto.collendar.mappers.CalendarioMapper;
import projeto.collendar.model.Calendario;
import projeto.collendar.model.Usuario;
import projeto.collendar.repository.CalendarioRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarioService {

    private final CalendarioRepository calendarioRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public CalendarioResponseDTO create(CalendarioRequestDTO dto, UUID usuarioId) {
        Usuario usuario = usuarioService.findEntityById(usuarioId);
        Calendario calendario = CalendarioMapper.toEntity(dto, usuario);
        calendarioRepository.save(calendario);
        return CalendarioMapper.toDTO(calendario, true, null);
    }

    public CalendarioResponseDTO findById(UUID id) {
        return calendarioRepository.findById(id)
                .map(CalendarioMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Calendário", id.toString()));
    }

    public List<CalendarioResponseDTO> listAll() {
        return calendarioRepository.findAll().stream()
                .map(CalendarioMapper::toDTO)
                .toList();
    }

    public List<CalendarioResponseDTO> listByUsuario(UUID usuarioId) {
        return calendarioRepository.findByUsuarioId(usuarioId).stream()
                .map(c -> CalendarioMapper.toDTO(c, true, null))
                .toList();
    }

    public Page<CalendarioResponseDTO> listByUsuarioPaginated(UUID usuarioId, Pageable pageable) {
        Usuario usuario = usuarioService.findEntityById(usuarioId);
        return calendarioRepository.findByUsuario(usuario, pageable)
                .map(c -> CalendarioMapper.toDTO(c, true, null));
    }

    public Page<CalendarioResponseDTO> searchByNome(String nome, Pageable pageable) {
        return calendarioRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(CalendarioMapper::toDTO);
    }

    @Transactional
    public CalendarioResponseDTO update(UUID id, CalendarioRequestDTO dto) {
        Calendario calendario = findEntityById(id);
        calendario.setNome(dto.nome());
        calendario.setDescricao(dto.descricao());
        calendario.setCor(dto.cor());
        return CalendarioMapper.toDTO(calendarioRepository.save(calendario), true, null);
    }

    @Transactional
    public void delete(UUID id) {
        if (!calendarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Calendário", id.toString());
        }
        calendarioRepository.deleteById(id);
    }

    public boolean isOwner(UUID calendarioId, UUID usuarioId) {
        Calendario calendario = findEntityById(calendarioId);
        return calendario.getUsuario().getId().equals(usuarioId);
    }

    public long countByUsuario(UUID usuarioId) {
        return calendarioRepository.findByUsuarioId(usuarioId).size();
    }

    public Calendario findEntityById(UUID id) {
        return calendarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calendário", id.toString()));
    }
}