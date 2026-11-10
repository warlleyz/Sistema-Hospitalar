package br.edu.pucminas.hospital.internacoes.controller;

import br.edu.pucminas.hospital.internacoes.dto.*;
import br.edu.pucminas.hospital.internacoes.dto.PedidosInternacao.*;
import br.edu.pucminas.hospital.internacoes.service.InternacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internacoes")
@Validated
public class InternacaoController {
    private final InternacaoService service;
    public InternacaoController(InternacaoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<InternacaoResposta> registrarEntrada(@Valid @RequestBody NovaInternacao pedido) {
        var criada = service.registrarEntrada(pedido);
        return ResponseEntity.created(URI.create("/internacoes/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<InternacaoResposta> listar(@RequestParam(required = false) @Positive Long pacienteId) {
        return service.listar(pacienteId);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<InternacaoResposta> historicoPaciente(@PathVariable @Positive Long pacienteId) {
        return service.listar(pacienteId);
    }

    @GetMapping("/{id}")
    public InternacaoResposta consultar(@PathVariable @Positive Long id) { return service.consultar(id); }

    @PutMapping("/{id}")
    public InternacaoResposta atualizar(@PathVariable @Positive Long id, @Valid @RequestBody AtualizarInternacao pedido) {
        return service.atualizar(id, pedido);
    }

    @PatchMapping("/{id}/alta")
    public InternacaoResposta registrarAlta(@PathVariable @Positive Long id, @Valid @RequestBody AltaInternacao pedido) {
        return service.registrarAlta(id, pedido);
    }
}
