package ru.kulikovman.tasklist;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.Task;
import ru.kulikovman.tasklist.adapters.TaskAdapter;

public class FinishedListActivity extends AppCompatActivity {

    private Realm mRealm;
    public RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;
    private String LOG = "log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_list);

        // Подключаем базу данных
        mRealm = Realm.getDefaultInstance();

        // Инициализируем RecyclerView
        mRecyclerView = findViewById(R.id.finished_recycler_view);

        // Создаем и запускаем список
        setUpRecyclerView();

        Log.d(LOG, "Завершен onCreate в FinishedListActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.setAdapter(null);
        mRealm.close();
    }

    private void setUpRecyclerView() {
        mAdapter = new TaskAdapter(this, loadFinishedTasks());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        // Обработчик свайпов
        initSwipe();
    }

    private OrderedRealmCollection<Task> loadFinishedTasks() {
        return mRealm.where(Task.class)
                .equalTo(Task.DONE, true)
                .findAll()
                .sort(Task.COMPLETION_DATE, Sort.DESCENDING);
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Получаем позицию и задачу
                int position = viewHolder.getAdapterPosition();
                Task task = mAdapter.getTaskByPosition(position);

                // Открываем транзакцию
                mRealm.beginTransaction();

                // Действие при свайпе
                if (direction == ItemTouchHelper.RIGHT) {
                    // Удаляем задачу
                    task.deleteFromRealm();
                    Log.d(LOG, "Задача удалена");
                } else {
                    // Восстанавливаем задачу
                    task.setCompletionDate(0);
                    task.setDone(false);

                    // Если есть группа, то повышаем счетчик
                    Group group = task.getGroup();
                    if (group != null) {
                        group.increaseCountTask();
                    }
                    Log.d(LOG, "Задача восстановлена");
                }

                // Закрываем транзакцию
                mRealm.commitTransaction();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;

                    float height = itemView.getBottom() - itemView.getTop();

                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {
                        // Цвет и размер фона
                        p.setColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);

                        // Иконка и ее расположение
                        Drawable d = getResources().getDrawable(R.drawable.ic_delete_white_24dp);
                        icon = drawableToBitmap(d);

                        int iconWidth = icon.getWidth();
                        int iconHeight = icon.getHeight();

                        float leftPosition = iconWidth;
                        float rightPosition = leftPosition + iconWidth;
                        float topPosition = itemView.getTop() + ((height - iconHeight) / 2);
                        float bottomPosition = topPosition + iconHeight;

                        RectF iconDest = new RectF(leftPosition, topPosition, rightPosition, bottomPosition);
                        c.drawBitmap(icon, null, iconDest, p);

                    } else {
                        // Цвет и размер фона
                        p.setColor(ResourcesCompat.getColor(getResources(), R.color.blue, null));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);

                        // Иконка и ее расположение
                        Drawable d = getResources().getDrawable(R.drawable.ic_replay_white_24dp);
                        icon = drawableToBitmap(d);

                        int iconWidth = icon.getWidth();
                        int iconHeight = icon.getHeight();

                        float rightPosition = itemView.getRight() - iconWidth;
                        float leftPosition = rightPosition - iconWidth;
                        float topPosition = itemView.getTop() + ((height - iconHeight) / 2);
                        float bottomPosition = topPosition + iconHeight;

                        RectF iconDest = new RectF(leftPosition, topPosition, rightPosition, bottomPosition);
                        c.drawBitmap(icon, null, iconDest, p);
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
            }

            Bitmap drawableToBitmap(Drawable drawable) {
                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable) drawable).getBitmap();
                }

                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                return bitmap;
            }
        };

        // Присоединяем всю эту конструкцию к нашему mTaskRecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }
}
