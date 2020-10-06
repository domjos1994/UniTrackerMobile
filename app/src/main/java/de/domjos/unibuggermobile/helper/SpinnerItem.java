package de.domjos.unibuggermobile.helper;

import androidx.annotation.NonNull;

public class SpinnerItem {
    private int id;
    private String item;

    public SpinnerItem(int id, String item) {
        this.id = id;
        this.item = item;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItem() {
        return this.item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    @Override
    @NonNull
    public String toString() {
        return this.item;
    }
}
