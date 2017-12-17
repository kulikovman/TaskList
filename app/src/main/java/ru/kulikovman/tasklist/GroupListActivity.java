package ru.kulikovman.tasklist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import ru.kulikovman.tasklist.dialogs.ColorDialog;
import ru.kulikovman.tasklist.dialogs.DateDialog;
import ru.kulikovman.tasklist.dialogs.DescriptionDialog;
import ru.kulikovman.tasklist.dialogs.GroupDialog;
import ru.kulikovman.tasklist.dialogs.PriorityDialog;
import ru.kulikovman.tasklist.dialogs.RepeatDialog;
import ru.kulikovman.tasklist.messages.GroupIsExist;
import ru.kulikovman.tasklist.models.Group;
import ru.kulikovman.tasklist.models.GroupAdapter;
import ru.kulikovman.tasklist.models.Task;

public class GroupListActivity extends AppCompatActivity implements GroupAdapter.OnItemClickListener,
        CallbackDialogFragment.CallbackDialogListener {

    private Realm mRealm;
    public RecyclerView mRecyclerView;
    private GroupAdapter mAdapter;
    private String LOG = "log";

    private Group mGroup;
    private int mPosition = -1;

    private EditText mGroupField;
    private ImageButton mAddGroup;

    private LinearLayout mGroupOptionsPanel;
    private ImageButton mSetDescription, mSetColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        // Подключаем базу данных
        mRealm = Realm.getDefaultInstance();

        // Инициализируем базовые вью элементы
        mRecyclerView = findViewById(R.id.group_recycler_view);
        mGroupField = findViewById(R.id.group_field);
        mAddGroup = findViewById(R.id.add_group_button);

        // Инициализируем вью элементы панели инструментов
        mGroupOptionsPanel = findViewById(R.id.group_options_panel);
        mSetDescription = findViewById(R.id.group_set_description);
        mSetColor = findViewById(R.id.group_set_color);

        // Создаем и запускаем список
        setUpRecyclerView();

        // Смена фокуса поля ввода
        mGroupField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                resetItemSelection();
            }
        });

        Log.d(LOG, "Завершен onCreate в GroupListActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.setAdapter(null);
        mRealm.close();
    }

    private void setUpRecyclerView() {
        mAdapter = new GroupAdapter(this, loadGroupList());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Слушатель для адаптера списка
        mAdapter.setOnItemClickListener(this);

        // Обработчик свайпов
        initSwipe();
    }

    private OrderedRealmCollection<Group> loadGroupList() {
        return mRealm.where(Group.class).findAll();
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (viewHolder != null) {
                    // Получаем сдвигаемый элемент
                    View itemView = viewHolder.itemView;

                    // Снимаем выделение, если оно есть
                    if (itemView.isSelected()) {
                        itemView.setSelected(false);
                        mPosition = RecyclerView.NO_POSITION;
                    } else if (mPosition != -1) {
                        resetItemSelection();
                    }

                    // Скрываем панель инструментов
                    mGroupOptionsPanel.setVisibility(View.INVISIBLE);
                }

                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Получаем позицию и группу
                mPosition = viewHolder.getAdapterPosition();
                mGroup = mAdapter.getGroupByPosition(mPosition);

                // Удаление группы
                if (direction == ItemTouchHelper.RIGHT) {
                    // TODO: 17.12.2017 сделать возможность удаления задач связанных с группой
                    // для этого нужно менять структуру данных группы
                    // добавить массив со списком связаных задач

                    // Пока просто удаляем группу
                    mRealm.beginTransaction();
                    mGroup.deleteFromRealm();
                    mRealm.commitTransaction();
                    Log.d(LOG, "Группа удалена");
                }

                // Обнуляем переменные
                mPosition = RecyclerView.NO_POSITION;
                mGroup = null;
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

                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
            }

            public Bitmap drawableToBitmap(Drawable drawable) {
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

        // Присоединяем всю эту конструкцию к нашему mRecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onItemClick(int position, Group group) {
        // Прячем клавиатуру
        hideKeyboard();

        // Определяем повторное нажатие по элементу
        if (position == mPosition) {
            resetItemSelection();
        } else {
            mGroup = group;
            selectItem(position);
        }
    }

    private void hideKeyboard() {
        // Прячем клавиатуру
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        // Снимаем фокус с поля ввода
        mGroupField.clearFocus();
    }

    private void resetItemSelection() {
        if (mPosition != RecyclerView.NO_POSITION) {
            mAdapter.resetSelection();
            mGroupOptionsPanel.setVisibility(View.INVISIBLE);
            mPosition = RecyclerView.NO_POSITION;
            mGroup = null;
        }
    }

    private void selectItem(int position) {
        mAdapter.selectItem(position);
        mPosition = position;

        // Показываем панель инструментов
        mGroupOptionsPanel.setVisibility(View.VISIBLE);
    }

    public void groupOptionsButtons(View view) {
        int id = view.getId();

        // Сохраняем id группы для передачи в диалог
        Bundle args = new Bundle();
        args.putLong("groupId", mGroup.getId());

        // Обрабатываем нажатие
        switch (id) {
            case R.id.group_set_description:
                DialogFragment descriptionDialog = new DescriptionDialog();
                descriptionDialog.setArguments(args);
                descriptionDialog.show(getSupportFragmentManager(), "descriptionDialog");
                break;
            case R.id.group_set_color:
                DialogFragment colorDialog = new ColorDialog();
                colorDialog.setArguments(args);
                colorDialog.show(getSupportFragmentManager(), "colorDialog");
                break;
        }
    }

    @Override
    public void onDialogFinish(DialogFragment dialog) {
        Log.d("log", "Запущен onDialogFinish в GroupListActivity");

        // Сбрасываем выделение и позицию
        mAdapter.resetSelection();
        mPosition = RecyclerView.NO_POSITION;

        // Получаем группу или null
        mGroup = mAdapter.getGroupById(mGroup.getId());

        if (mGroup != null) {
            // Получаем позицию, скролим к группе и выделяем ее
            mPosition = mAdapter.getPosition(mGroup.getId());
            mRecyclerView.scrollToPosition(mPosition);
            selectItem(mPosition);
        } else {
            // Скрываем панель инструментов
            mGroupOptionsPanel.setVisibility(View.INVISIBLE);
        }
    }

    public void addGroup(View view) {
        String groupName = mGroupField.getText().toString().trim();

        if (groupName.length() > 0) {
            //RealmResults<Group> existGroups = mRealm.where(Group.class).equalTo(Group.NAME, groupName).findAll();
            Group existGroup = mRealm.where(Group.class).equalTo(Group.NAME, groupName).findFirst();

            if (existGroup == null) {
                // Создаем группу и добавляем в базу
                Group group = new Group(groupName);
                mRealm.beginTransaction();
                mRealm.insert(group);
                mRealm.commitTransaction();

                // Очищаем поле
                mGroupField.setText(null);

                // Получаем группу или null
                mGroup = mAdapter.getGroupById(group.getId());

                if (mGroup != null) {
                    // Получаем позицию и скролим к группе
                    mPosition = mAdapter.getPosition(mGroup.getId());
                    mRecyclerView.scrollToPosition(mPosition);
                }
            } else {
                // Показываем сообщение об ошибке
                DialogFragment groupIsExist = new GroupIsExist();
                groupIsExist.show(getSupportFragmentManager(), "groupIsExist");
            }
        }
    }
}
