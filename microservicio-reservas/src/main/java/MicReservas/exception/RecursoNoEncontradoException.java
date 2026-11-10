package MicReservas.exception;

import org.springframework.http.HttpStatus;

                                                                                     
public class RecursoNoEncontradoException extends ReglaNegocioException {
    public RecursoNoEncontradoException(String message) { super(message, HttpStatus.NOT_FOUND); }
}
