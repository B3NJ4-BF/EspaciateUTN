package com.utn.espacios.exception;

import com.utn.espacios.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNoEncontrado(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> manejarSolicitudInvalida(
            BadRequestException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidaciones(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.merge(error.getField(), error.getDefaultMessage(),
                    (anterior, nuevo) -> anterior + "; " + nuevo);
        }
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "La petición contiene campos inválidos.", errores, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarViolacionesDeParametros(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violacion ->
                errores.put(violacion.getPropertyPath().toString(), violacion.getMessage()));
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "Uno o más parámetros no cumplen las restricciones.", errores, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> manejarJsonInvalido(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "El cuerpo JSON no es válido o contiene un valor de enum desconocido.",
                Map.of(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> manejarParametroConTipoInvalido(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        Map<String, String> errores = Map.of(
                ex.getName(), "El valor '" + ex.getValue() + "' no tiene un formato válido.");
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "Uno de los parámetros de la ruta o consulta es inválido.", errores, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> manejarParametroFaltante(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "Falta un parámetro obligatorio: " + ex.getParameterName() + ".",
                Map.of(ex.getParameterName(), "Parámetro obligatorio."), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErrorGeneral(
            Exception ex, HttpServletRequest request) {
                                                                      
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno al procesar la solicitud.", Map.of(), request);
    }

    private ResponseEntity<ErrorResponseDTO> construirRespuesta(
            HttpStatus status,
            String mensaje,
            Map<String, String> detalles,
            HttpServletRequest request) {

        ErrorResponseDTO body = new ErrorResponseDTO(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                detalles,
                request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }
}
