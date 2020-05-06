package spendreport.v.bounded;

import org.apache.flink.streaming.api.functions.source.FromIteratorFunction;
import org.apache.flink.walkthrough.common.entity.Transaction;


import java.io.Serializable;
import java.util.Iterator;

public class TransactionSourceBounded  extends FromIteratorFunction<Transaction> {
    private static final long serialVersionUID = 1L;

    public TransactionSourceBounded() {
        super(new TransactionSourceBounded.RateLimitedIterator<>(TransactionIterator.bounded()));
    }

    private static class RateLimitedIterator<T> implements Iterator<T>, Serializable {

        private static final long serialVersionUID = 1L;

        private final Iterator<T> inner;

        private RateLimitedIterator(Iterator<T> inner) {
            this.inner = inner;
        }

        @Override
        public boolean hasNext() {
            return inner.hasNext();
        }

        @Override
        public T next() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return inner.next();
        }
    }
}
