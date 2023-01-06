package org.solitaire.model;

import java.util.ArrayList;

public class Columns extends ArrayList<Column> {
    public Columns() {
        super();
    }

    public Columns(int initialCapacity) {
        super(initialCapacity);
    }

    public Columns(Columns that) {
        super(that.size());
        that.forEach(it -> add(new Column(it)));
    }

}
