package com.springai.curriculums.init.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springai.curriculums.init.model.Resultado;
import com.springai.curriculums.init.service.TutorialesService;
import com.springai.curriculums.init.service.IngestionService;

@RestController
@RequestMapping("rag")
public class TutorPersonalizadoController {
	private IngestionService ingestionService;
	private TutorialesService tutorialesService;


	public TutorPersonalizadoController(IngestionService ingestionService, TutorialesService tutorialesService) {
		this.ingestionService = ingestionService;
		this.tutorialesService = tutorialesService;
	}
	@PostMapping("ingest")
	public ResponseEntity<String> ingest(){
		ingestionService.ingestarDocumentos();
		return ResponseEntity.ok("Ingesta completada");
	}
	@DeleteMapping("clear")
	public ResponseEntity<String> clear(){
		ingestionService.eliminarDocumentos();
		return ResponseEntity.ok("Documentos eliminados");
	}
	@GetMapping("consultar")
	public ResponseEntity<List<Resultado>> resultados(@RequestParam String mensajeUser){
		return ResponseEntity.ok(tutorialesService.obtenerResultados(mensajeUser));
	}
}
