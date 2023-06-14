package cadonuno.pipelinescanautotrigger.exceptions;

import cadonuno.pipelinescanautotrigger.ui.MessageHandler;

public class VeracodePipelineScanException extends Exception {
    private final Exception exception;
    private final String title;
    private final String message;

    public VeracodePipelineScanException(String title, Exception exception) {
        this.title = title;
        this.exception = exception;
        this.message = null;
    }

    public VeracodePipelineScanException(String title, String message) {
        this.title = title;
        this.exception = null;
        this.message = message;
    }

    public void showErrorMessage() {
        MessageHandler.showErrorPopup(title, getFullMessage());
    }

    public void showCombinedError(VeracodePipelineScanException other) {
        MessageHandler.showErrorPopup("2 errors identified during process",
                getFullMessage() + "\n\n" + other.getFullMessage());
    }

    private String getFullMessage() {
        return exception == null
                ? message
                : exception.getClass().getName() + ": " + exception.getMessage();
    }
}
