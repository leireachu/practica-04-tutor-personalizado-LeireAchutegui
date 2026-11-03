package com.springai.curriculums.init.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.springai.curriculums.init.components.PdfLoaderComponent;
import com.springai.curriculums.init.model.Resultado;

@Service
public class TutorialesService {
	private VectorStore vectorStore;
	private ChatClient chatClient;
	private PdfLoaderComponent pdfLoaderComponent;
	private final String system="""
            Eres un experto en tecnología buscando información en documentos.
            Tu tarea es proporcionar al usuario información con una explicación sobre ese concepto o tecnología proporcionado en la pregunta, básandote única y exclusivamente en el contexto proporcionado.
            Si hay varios documentos que cumplen los requisitos, indicalos todos. 
            Se utilizará como fuente de información única y exclusivamente los documentos proporcionados en el contexto.
            ENTREGA LA RESPUESTA SIEMPRE EN FORMATO JSON (un array de objetos JSON). Cada objeto debe tener la siguiente estructura exacta:
		    - nombreDocumento: ruta completa con el nombre del documento completo donde aparece la información sobre el concepto o tecnología de los proporcionados en el contexto.
		    - explicacion: Explicación del concepto o tecnología por la que se pregunta basándote en los documentos proporcionados.		    
		    IMPORTANTE: Si no hay contexto o ningún documento contiene información relacionada a lo que se pregunta, responde estrictamente con un array vacío: []. No agregues texto plano que no sea JSON válido ni explicaciones complementarias fuera del JSON.
            CONTEXTO:
            {contexto}
            """;
	
	public TutorialesService(VectorStore vectorStore, ChatClient chatClient, PdfLoaderComponent pdfLoaderComponent) {
		this.vectorStore = vectorStore;
		this.chatClient = chatClient;
		this.pdfLoaderComponent = pdfLoaderComponent;
	}

	public List<Resultado> obtenerResultados(String preguntaUser){
		List<Document> chunks=vectorStore.similaritySearch(SearchRequest.builder()
				.query(preguntaUser)
				.topK(20)
				.build()
				);
		String contexto=chunks.stream()
				.map(d -> {
					String path = d.getMetadata().getOrDefault("file_path", "desconocida").toString();
					String documento = pdfLoaderComponent.obtenerDocumentoTexto(path);
			        return """
			                ============================================================
			                TUTORIAL
			                Nombre documento: %s
			                Información:
			
			                %s
			                """.formatted(path, documento);
				})
                .collect(Collectors.joining("\n\n---\n\n"));


		/*String contexto = chunks.stream()
				.map(d -> "Nombre documento: " + d.getMetadata().getOrDefault("file_path", "desconocida") +
						"\n" + d.getText())
				.collect(Collectors.joining("\n\n---\n\n"));*/

		PromptTemplate template=new PromptTemplate(system);
		Message sysMessage=template.createMessage(Map.of("contexto", contexto));
		Message userMessage=new UserMessage("Pregunta usuario: " + preguntaUser);
		Prompt prompt=new Prompt(List.of(sysMessage,userMessage));
		
		return Arrays.asList(chatClient
				.prompt(prompt)
				.call()
				.entity(Resultado[].class));
	}
}
