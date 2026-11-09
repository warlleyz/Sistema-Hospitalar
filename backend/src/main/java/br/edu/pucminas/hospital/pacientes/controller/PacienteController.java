package br.edu.pucminas.hospital.pacientes.controller;

import br.edu.pucminas.hospital.pacientes.dto.PacientePedido;
import br.edu.pucminas.hospital.pacientes.dto.PacienteResposta;
import br.edu.pucminas.hospital.pacientes.service.PacienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pacientes")
@Validated
public class PacienteController {
    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PacienteResposta> criar(@Valid @RequestBody PacientePedido pedido) {
        var paciente = service.criar(pedido);
        return ResponseEntity.created(URI.create("/pacientes/" + paciente.id())).body(paciente);
    }

    @GetMapping
    public List<PacienteResposta> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PacienteResposta consultar(@PathVariable @Positive Long id) {
        return service.consultar(id);
    }

    @PutMapping("/{id}")
    public PacienteResposta atualizar(@PathVariable @Positive Long id, @Valid @RequestBody PacientePedido pedido) {
        return service.atualizar(id, pedido);
    }
}
