@RestControllerAdvice
public class GlobalExceptionHandler {

    // Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity < ApiResponse > handleResourceNotFound(
        ResourceNotFoundException ex) {

        ApiResponse response = new ApiResponse(false, ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Duplicate Resource
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity < ApiResponse > handleDuplicateResource(
        DuplicateResourceException ex) {

        ApiResponse response = new ApiResponse(false, ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Insufficient Funds
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity < ApiResponse > handleInsufficientFunds(
        InsufficientFundsException ex) {

        ApiResponse response =new ApiResponse(false, ex.getMessage());

        return new ResponseEntity < > ( response, HttpStatus.BAD_REQUEST);
    }

    // Account Blocked
    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity < ApiResponse > handleAccountBlocked(
        AccountBlockedException ex) {

        ApiResponse response = new ApiResponse(false, ex.getMessage());

        return new ResponseEntity<>( response, HttpStatus.FORBIDDEN);
    }

    // Validation Exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity < ApiResponse > handleValidation(
        MethodArgumentNotValidException ex) {

        Map < String, String > errors = new HashMap < > ();

        ex.getBindingResult().getAllErrors()
            .forEach(error -> {

                String field = ((FieldError) error).getField();

                String message =
                error.getDefaultMessage();

                errors.put(field, message);
            });

        ApiResponse response =new ApiResponse( false, errors.toString());

        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

    // General Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity < ApiResponse > handleGeneralException(
        Exception ex) {

        ApiResponse response =
      new ApiResponse(false, "Internal Server Error : " +ex.getMessage());

        return new ResponseEntity < > ( response,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
