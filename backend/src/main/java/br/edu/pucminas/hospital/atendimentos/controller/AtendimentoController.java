package br.edu.pucminas.hospital.atendimentos.controller;

import br.edu.pucminas.hospital.atendimentos.dto.*;
import br.edu.pucminas.hospital.atendimentos.dto.Pedidos.*;
import br.edu.pucminas.hospital.atendimentos.model.SituacaoAtendimento;
import br.edu.pucminas.hospital.atendimentos.service.AtendimentoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
public class AtendimentoController {
    private final AtendimentoService service;
    public AtendimentoController(AtendimentoService service) { this.service = service; }
    @PostMapping("/consultas")
    public ResponseEntity<AtendimentoResposta> criarConsulta(@Valid @RequestBody NovaConsulta p) {
        return criado("consultas", service.criarConsulta(p));
    }
    @PostMapping("/emergencias")
    public ResponseEntity<AtendimentoResposta> criarEmergencia(@Valid @RequestBody NovaEmergencia p) {
        return criado("emergencias", service.criarEmergencia(p));
    }
    @PostMapping("/cirurgias")
    public ResponseEntity<AtendimentoResposta> criarCirurgia(@Valid @RequestBody NovaCirurgia p) {
        return criado("cirurgias", service.criarCirurgia(p));
    }
    @GetMapping("/{recurso:consultas|emergencias|cirurgias}")
    public List<AtendimentoResposta> listar(@PathVariable String recurso, @RequestParam(required = false) @Positive Long pacienteId,
        @RequestParam(required = false) SituacaoAtendimento situacao) {
        return service.listar(recurso, pacienteId, situacao);
    }
    @GetMapping("/{recurso:consultas|emergencias|cirurgias}/{id}")
    public AtendimentoResposta consultar(@PathVariable String recurso, @PathVariable @Positive Long id) {
        return service.consultar(recurso, id);
    }
    @PatchMapping("/{recurso:consultas|emergencias|cirurgias}/{id}/situacao")
    public AtendimentoResposta transicionar(@PathVariable String recurso, @PathVariable @Positive Long id,
        @Valid @RequestBody Transicao p) { return service.transicionar(recurso, id, p); }
    @PatchMapping("/{recurso:consultas|emergencias|cirurgias}/{id}/registro")
    public AtendimentoResposta registrar(@PathVariable String recurso, @PathVariable @Positive Long id,
        @Valid @RequestBody RegistroClinico p) { return service.registrar(recurso, id, p); }
    public record Disponibilidade(Long profissionalId, LocalDateTime inicio, boolean disponivel) { }
    @GetMapping("/atendimentos/disponibilidade")
    public Disponibilidade disponibilidade(@RequestParam @Positive Long profissionalId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio) {
        return new Disponibilidade(profissionalId, inicio, service.disponivel(profissionalId, inicio));
    }
    private ResponseEntity<AtendimentoResposta> criado(String recurso, AtendimentoResposta a) {
        return ResponseEntity.created(URI.create("/" + recurso + "/" + a.id())).body(a);
    }
}
