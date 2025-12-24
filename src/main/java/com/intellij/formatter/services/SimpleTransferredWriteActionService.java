package com.intellij.formatter.services;

import com.intellij.util.concurrency.TransferredWriteActionService;

/**
 * Minimal implementation of {@link TransferredWriteActionService} for standalone formatting.
 *
 * <p>This implementation executes write actions directly since thread coordination
 * is simplified in standalone formatting mode.</p>
 *
 * @see TransferredWriteActionService
 */
public class SimpleTransferredWriteActionService implements TransferredWriteActionService {

    @Override
    public void runOnEdtWithTransferredWriteActionAndWait(Runnable runnable) {
        runnable.run();
    }
}
