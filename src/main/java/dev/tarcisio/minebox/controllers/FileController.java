package dev.tarcisio.minebox.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.tarcisio.minebox.advice.ArgumentValidateMessage;
import dev.tarcisio.minebox.exception.FileAccessNotAllowed;
import dev.tarcisio.minebox.exception.FileEmptyException;
import dev.tarcisio.minebox.exception.FileNotFoundException;
import dev.tarcisio.minebox.exception.FileUploadException;
import dev.tarcisio.minebox.exception.S3Exception;
import dev.tarcisio.minebox.payload.request.FileRenameRequest;
import dev.tarcisio.minebox.payload.response.FileDownloadResponse;
import dev.tarcisio.minebox.payload.response.FileListResponse;
import dev.tarcisio.minebox.payload.response.FileResponse;
import dev.tarcisio.minebox.payload.response.FileUploadResponse;
import dev.tarcisio.minebox.services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Rota file", description = "Rotas referente a arquivos")
@RestController
@RequestMapping(value = "/api/file", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
    MediaType.APPLICATION_JSON_VALUE })
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

  @Operation(summary = "Rota de listagem de arquivos do usuario", description = "É necessario estar autenticado e não precisar enviar parametros")
  @ApiResponse(responseCode = "200", description = "Retorna um FileListResponse no body", content = {
      @Content(schema = @Schema(implementation = FileListResponse.class), mediaType = "application/json")
  })
  @GetMapping("/list")
  public ResponseEntity<?> list() {
    try {
      List<FileListResponse> files = fileService.list();
      return ResponseEntity.status(200).body(files);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error interno no servidor, tente novamente mais tarde!");
    }
  }

  @Operation(summary = "Rota de download de arquivo do usuario", description = "É necessario estar autenticado e enivar o id do arquivo no path")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Retorna os bytes do arquivo no body", content = {
          @Content(mediaType = "depende do arquivo")
      }),
      @ApiResponse(responseCode = "400", description = "Retorna um erro do tipo S3Exception, quando acontece um erro no S3 na hora de baixar o arquivo", content = {
          @Content(schema = @Schema(implementation = S3Exception.class), mediaType = "application/json")
      }),
      @ApiResponse(responseCode = "404", description = "Retorna um erro do tipo FileNotFoundException, quando arquivo não é encontrado", content = {
          @Content(schema = @Schema(implementation = FileNotFoundException.class), mediaType = "application/json")
      })
  })
  @GetMapping("/download/{id}")
  public ResponseEntity<?> upload(@PathVariable String id) {
    try {
      FileDownloadResponse result = fileService.download(id);

      HttpHeaders header = new HttpHeaders();
      header.setContentType(MediaType.valueOf(result.getContentType()));
      header.setContentLength(result.getSize());

      return ResponseEntity.status(200).headers(header).body(result.getFilebytes());
    } catch (FileNotFoundException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (S3Exception e) {
      return ResponseEntity.status(400).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error interno no servidor, tente novamente mais tarde!");

    }
  }

  @Operation(summary = "Rota para renomear o arquivo do usuario", description = "É necessario estar autenticado e enviar o id do arquivo no path")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Retorna um JSON do tipo FileResponse", content = {
          @Content(mediaType = "application/json")
      }),
      @ApiResponse(responseCode = "400", description = "Retorna um erro do tipo ArgumentValidateMessage, quando o valor que veio no body é invalido ou mal formatado", content = {
          @Content(schema = @Schema(implementation = ArgumentValidateMessage.class), mediaType = "application/json")
      }),
      @ApiResponse(responseCode = "404", description = "Retorna um erro do tipo FileNotFoundException, quando arquivo não é encontrado", content = {
          @Content(schema = @Schema(implementation = FileNotFoundException.class), mediaType = "application/json")
      }),
      @ApiResponse(responseCode = "403", description = "Retorna um erro do tipo FileAccessNotAllowed, quando o usuário tenta acessar um arquivo que não o pertence", content = {
          @Content(schema = @Schema(implementation = FileAccessNotAllowed.class), mediaType = "application/json")
      })
  })
  @PutMapping("/rename/{id}")
  public ResponseEntity<?> rename(@PathVariable String id, @Valid @RequestBody FileRenameRequest fileRenameRequest)
      throws FileAccessNotAllowed, FileNotFoundException {
    try {
      FileResponse result = fileService.rename(id, fileRenameRequest);

      return ResponseEntity.status(200).body(result);
    } catch (FileNotFoundException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (FileAccessNotAllowed e) {
      return ResponseEntity.status(403).body(e.getMessage());
    }
  }

  @Operation(summary = "Rota para deletar o arquivo do usuario", description = "É necessario estar autenticado e enviar o id do arquivo no path")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Retorna um plain text", content = {
          @Content(mediaType = "application/json")
      }),
      @ApiResponse(responseCode = "404", description = "Retorna um erro do tipo FileNotFoundException, quando arquivo não é encontrado", content = {
          @Content(schema = @Schema(implementation = FileNotFoundException.class), mediaType = "application/json")
      }),
      @ApiResponse(responseCode = "403", description = "Retorna um erro do tipo FileAccessNotAllowed, quando o usuário tenta acessar um arquivo que não o pertence", content = {
          @Content(schema = @Schema(implementation = FileAccessNotAllowed.class), mediaType = "application/json")
      })
  })
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> rename(@PathVariable String id)
      throws FileAccessNotAllowed, FileNotFoundException {
    try {
      fileService.delete(id);

      return ResponseEntity.status(200).body("Arquivo deletado com sucesso!");
    } catch (FileNotFoundException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (FileAccessNotAllowed e) {
      return ResponseEntity.status(403).body(e.getMessage());
    }
  }
}
