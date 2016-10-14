package com.adfonic.beans;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * A read-only List<Selectable<T>> that's backed by an actual List<T>.
 */
public class SelectableList<T> extends AbstractList<Selectable<T>> {
    private List<T> backing;
    private List<Boolean> selected;

    public SelectableList(List<T> backing) {
        selected = new ArrayList<Boolean>(backing.size());
        this.backing = backing;
        for (int i = 0; i < backing.size(); i++) {
			selected.add(Boolean.FALSE);
		}
    }

    public List<T> getBacking() { return backing; }

    public Selectable<T> get(final int pos) {
        return new Selectable<T>() {
            public T getObject() {
                return backing.get(pos);
            }
            public boolean isChecked() {
                return selected.get(pos);
            }
            public void setChecked(boolean checked) {
                selected.set(pos, checked);
            }
        };
    }

    public int size() { return backing.size(); }
}
