package ru.kulikovman.tasklist;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    /*public TouchHelperCallback(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
    }*/

    TouchHelperCallback() {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
