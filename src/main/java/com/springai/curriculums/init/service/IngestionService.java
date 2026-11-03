package com.springai.curriculums.init.service;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.springai.curriculums.init.components.PdfLoaderComponent;

@Service
public class IngestionService {
	private PdfLoaderComponent pdfLoaderComponent;
	private VectorStore vectorStore;
	public IngestionService(PdfLoaderComponent pdfLoaderComponent, VectorStore vectorStore) {
		this.pdfLoaderComponent = pdfLoaderComponent;
		this.vectorStore = vectorStore;
	}
	
	public void ingestarDocumentos() {
		vectorStore.add(pdfLoaderComponent.obtenerChunks());
	}
	public void eliminarDocumentos() {
		vectorStore.delete("id!=''");
	}
}
