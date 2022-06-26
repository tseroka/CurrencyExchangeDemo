package pl.tseroka.currency_exchange_demo.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import pl.foltak.polishidnumbers.pesel.InvalidPeselException

@ControllerAdvice
class RestGlobalExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(RestGlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(InvalidPeselException::class, IllegalStateException::class, IllegalArgumentException::class)
    fun handleBadRequest(
        exception: Exception, request: WebRequest
    ): ResponseEntity<String> {
        log.error("An error occurred", exception)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.message)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(
        exception: Exception, request: WebRequest
    ): ResponseEntity<String> {
        log.error("An error occurred", exception)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.message)
    }
}
