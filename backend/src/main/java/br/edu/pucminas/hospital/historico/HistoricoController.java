package br.edu.pucminas.hospital.historico;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/pacientes/{pacienteId}/historico")
public class HistoricoController {
    private final HistoricoService service;
    public HistoricoController(HistoricoService service) { this.service = service; }

    @GetMapping
    public HistoricoMedico consultar(@PathVariable @Positive Long pacienteId) {
        return service.consultar(pacienteId);
    }
}
