package io.github.mvillasono.loginsight.autoconfigure.store;

import io.github.mvillasono.loginsight.autoconfigure.event.LogInsightEvent;
import io.github.mvillasono.loginsight.core.model.LogAnalysis;
import org.springframework.context.event.EventListener;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class AnalysisStore {

    private final int maxHistory;
    private final Deque<LogAnalysis> store;

    public AnalysisStore(int maxHistory) {
        this.maxHistory = maxHistory;
        this.store = new ArrayDeque<>(maxHistory);
    }

    @EventListener
    public synchronized void onAnalysis(LogInsightEvent event) {
        if (store.size() >= maxHistory) store.pollFirst();
        store.addLast(event.getAnalysis());
    }

    /** Devuelve las análisis del más reciente al más antiguo. */
    public synchronized List<LogAnalysis> getAll() {
        List<LogAnalysis> list = new ArrayList<>(store);
        list = list.reversed();
        return List.copyOf(list);
    }

    public synchronized int size() {
        return store.size();
    }
}
