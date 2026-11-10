package MicReservas.exception;

import MicReservas.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

   
                                                        
                                                                                         
   
@RestControllerAdvice
public class GlobalExceptionHandler {
                                                                                    
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarRegla(ReglaNegocioException ex, HttpServletRequest req) {
        return respuesta(ex.getStatus(), ex.getMessage(), req, Map.of());
    }

                                                                                  
                                                                     
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> detalles.put(e.getField(), e.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos obligatorios inválidos.", req, detalles);
    }

                                                                        
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarParametro(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Uno de los parámetros de la URL tiene un formato o valor inválido.", req, Map.of());
    }

                                                                               
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "El cuerpo JSON no es válido o contiene una fecha con formato incorrecto.", req, Map.of());
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> manejarParametroFaltante(
            MissingServletRequestParameterException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Falta el parámetro obligatorio: " + ex.getParameterName(),
                req, Map.of(ex.getParameterName(), "Parámetro obligatorio."));
    }


                                                                                         
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarViolacionesDeParametros(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> detalles.put(v.getPropertyPath().toString(), v.getMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Uno o más parámetros no cumplen las restricciones.", req, detalles);
    }

                                                                                     
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> manejarConcurrencia(OptimisticLockingFailureException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT,
                "La reserva fue modificada por otra operación. Consultá nuevamente e intentá otra vez.", req, Map.of());
    }

                                                                                        
                                                                                          
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex, HttpServletRequest req) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno al procesar la solicitud.", req, Map.of());
    }

                                                                   
    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje,
                                                     HttpServletRequest req, Map<String, String> detalles) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensaje, req.getRequestURI(), detalles);
        return ResponseEntity.status(status).body(body);
    }
}
