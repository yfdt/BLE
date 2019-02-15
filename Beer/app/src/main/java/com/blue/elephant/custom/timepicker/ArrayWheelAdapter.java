package com.blue.elephant.custom.timepicker;

import java.util.List;

public class ArrayWheelAdapter<T> implements WheelAdapter {
    // items
    private List<T> items;

    /**
     * Constructor
     * @param items the items
     */
    public ArrayWheelAdapter(List<T> items) {
        this.items = items;
    }

    public void setData(List<T> items)
    {
        if(items!= null)
        {
            this.items = items;
        }
    }

    @Override
    public Object getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return "";
    }

    @Override
    public int getItemsCount() {
        return items.size();
    }

    @Override
    public int indexOf(Object o){
        return items.indexOf(o);
    }
}
