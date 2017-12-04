package ru.kulikovman.tasklist;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    public TouchHelperCallback() {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
        //DataHelper.deleteItemAsync(realm, viewHolder.getItemId());
        //String taskId = String.valueOf(viewHolder.getItemId());
        //Toast.makeText(getApplicationContext(), taskId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

}
