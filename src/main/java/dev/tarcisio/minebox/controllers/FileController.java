package dev.tarcisio.minebox.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.tarcisio.minebox.exception.FileEmptyException;
import dev.tarcisio.minebox.exception.FileUploadException;
import dev.tarcisio.minebox.payload.response.FileUploadResponse;
import dev.tarcisio.minebox.services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Rota file", description = "Rotas referente a arquivos")
@RestController
@RequestMapping(value = "/api/file", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
public class FileController {
  @Autowired
  private FileService fileService;

  @Operation(summary = "Rota de upload de arquivos", description = "Recebe arquivos com parametro 'file' no body multipart/form-data")
  @ApiResponses({
      @ApiResponse(responseCode = "201", content = {
          @Content(schema = @Schema(implementation = FileUploadResponse.class), mediaType = "multipart/form-data")
      }),
      @ApiResponse(responseCode = "400", description = "Pode ser um erro no upload lançando FileUploadException ou o arquivo é maior do que o permitido lançando MaxUploadSizeExceededException", content = {
          @Content(schema = @Schema(implementation = FileUploadException.class), mediaType = "application/json")
      })
  })
  @PostMapping("/upload")
  public ResponseEntity<?> upload(@RequestPart("file") List<MultipartFile> files)
      throws IOException, FileUploadException, FileEmptyException {
    try {
      List<FileUploadResponse> result = fileService.upload(files);

      return ResponseEntity.status(201).body(result);

    } catch (FileEmptyException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    } catch (FileUploadException e) {
      return ResponseEntity.status(400).body(e.getMessage());
    }

  }
}
