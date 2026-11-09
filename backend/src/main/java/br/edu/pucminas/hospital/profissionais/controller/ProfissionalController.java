package br.edu.pucminas.hospital.profissionais.controller;

import br.edu.pucminas.hospital.profissionais.dto.ProfissionalPedido;
import br.edu.pucminas.hospital.profissionais.dto.ProfissionalResposta;
import br.edu.pucminas.hospital.profissionais.service.ProfissionalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profissionais")
@Validated
public class ProfissionalController {
    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProfissionalResposta> criar(@Valid @RequestBody ProfissionalPedido pedido) {
        var profissional = service.criar(pedido);
        return ResponseEntity.created(URI.create("/profissionais/" + profissional.id())).body(profissional);
    }

    @GetMapping
    public List<ProfissionalResposta> listar(
        @RequestParam(required = false) @Size(max = 120) String especialidade,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio) {
        return service.listar(especialidade, inicio);
    }

    @GetMapping("/{id}")
    public ProfissionalResposta consultar(@PathVariable @Positive Long id) {
        return service.consultar(id);
    }

    @PutMapping("/{id}")
    public ProfissionalResposta atualizar(@PathVariable @Positive Long id, @Valid @RequestBody ProfissionalPedido pedido) {
        return service.atualizar(id, pedido);
    }
}
