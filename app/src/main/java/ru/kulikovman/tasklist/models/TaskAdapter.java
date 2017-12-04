package ru.kulikovman.tasklist.models;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ru.kulikovman.tasklist.Helper;
import ru.kulikovman.tasklist.R;

public class TaskAdapter extends RealmRecyclerViewAdapter<Task, TaskAdapter.TaskHolder> {
    private static OnItemClickListener mListener;
    private Context mContext;
    private OrderedRealmCollection<Task> mTasks;

    private int mPosition = RecyclerView.NO_POSITION;

    // This object helps you save/restore the open/close state of each view
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public TaskAdapter(Context context, OrderedRealmCollection<Task> results) {
        super(results, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        setHasStableIds(true);

        mTasks = results;
        mContext = context;

        // uncomment the line below if you want to open only one row at a time
        viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getId();
    }

    public class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTaskTitle, mTaskDate, mTaskPriority, mTaskRepeat;
        private ImageButton mTaskColor;
        private ImageView mTaskWarning;
        private Task mTask;

        private SwipeRevealLayout swipeLayout;

        public TaskHolder(View view) {
            super(view);

            // Слушатель нажатий по элементу
            //view.setOnClickListener(this);

            // Инициализируем вью элемента списка
            mTaskTitle = (TextView) view.findViewById(R.id.item_task_title);
            mTaskDate = (TextView) view.findViewById(R.id.item_task_date);
            mTaskPriority = (TextView) view.findViewById(R.id.item_task_priority);
            mTaskRepeat = (TextView) view.findViewById(R.id.item_task_repeat);
            mTaskColor = (ImageButton) view.findViewById(R.id.item_task_color);
            mTaskWarning = (ImageView) view.findViewById(R.id.item_task_warning);

            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
        }

        @Override
        public void onClick(View v) {
            /*// Обновляем айтем нажатый ранее
            notifyItemChanged(mPosition);

            // Сохраняем старую и получаем новую позицию
            int oldPosition = mPosition;
            mPosition = getLayoutPosition();

            // Если старая и новая позиции совпадают, то удаляем позицию
            if (mPosition == oldPosition) {
                resetSelection();
            }

            // Обновляем айтем нажатый сейчас
            notifyItemChanged(mPosition);*/

            // Код для проброса слушателя
            if (TaskAdapter.mListener != null) {
                TaskAdapter.mListener.onItemClick(v, getLayoutPosition(), mTask, mPosition);
            }
        }

        // Назначаем содержимое для текущего элемента списка
        public void bindTask(Task task) {
            mTask = task;

            // Устанавливаем состояние айтема по умолчанию
            defaultStateItem();

            // Устанавливаем название задачи
            mTaskTitle.setText(task.getTitle());

            // Если задача завершена, то делаем ее светло-серой
            if (task.isDone()) {
                mTaskTitle.setTextColor(ContextCompat.getColor(mContext, R.color.gray_4));
            }

            // Устанавливаем дату
            long targetDate = task.getTargetDate();

            if (targetDate != Long.MAX_VALUE) {
                // Получаем год даты задачи и текущий год
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(targetDate);
                int targetYear = calendar.get(Calendar.YEAR);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                // Сравниваем года и записываем дату в нужном формате
                if (targetYear == currentYear) {
                    mTaskDate.setText(Helper.convertLongToShortTextDate(targetDate));
                } else {
                    mTaskDate.setText(Helper.convertLongToLongTextDate(targetDate));
                }

                // Считаем количество дней до задачи
                Calendar taskDate = Helper.convertLongToCalendar(task.getTargetDate());
                Calendar todayDate = Helper.getTodayRoundCalendar();

                int daysBeforeTaskDate = (int) ((taskDate.getTimeInMillis() - todayDate.getTimeInMillis()) / 1000 / 60 / 60 / 24);
                Log.d("log", "Разница в днях: " + daysBeforeTaskDate);

                // Если задача просрочена, то выделяем дату
                if (daysBeforeTaskDate < 0) {
                    mTaskDate.setTextColor(mContext.getResources().getColor(R.color.red_date));
                }
            }

            // Устанавливаем приоритет
            int priority = task.getPriority();

            if (priority != 2) {
                if (priority == 0) {
                    mTaskPriority.setText(R.string.priority_emergency);
                } else if (priority == 1) {
                    mTaskPriority.setText(R.string.priority_high);
                } else if (priority == 3) {
                    mTaskPriority.setText(R.string.priority_low);
                } else if (priority == 4) {
                    mTaskPriority.setText(R.string.priority_lowest);
                }
            }

            // Устанавливаем повтор
            String repeat = task.getRepeat();

            if (repeat != null) {
                switch (repeat) {
                    case "day":
                        mTaskRepeat.setText(R.string.repeat_day);
                        break;
                    case "week":
                        mTaskRepeat.setText(R.string.repeat_week);
                        break;
                    case "month":
                        mTaskRepeat.setText(R.string.repeat_month);
                        break;
                    case "year":
                        mTaskRepeat.setText(R.string.repeat_year);
                        break;
                }
            }

            // Получаем название цвета и закрашиваем ярлычок
            if (task.getGroup() != null) {
                String color = task.getGroup().getColor();

                if (color != null) {
                    // Получаем id цвета из его названия
                    int colorId = mContext.getResources().getIdentifier(color, "color", mContext.getPackageName());
                    mTaskColor.setBackgroundResource(colorId);
                }
            }

            // Если напоминание включено, показываем иконку
            if (task.getReminder()) {
                // Делаем иконку видимой и двигаем вправо до начала заголовка
                mTaskWarning.setVisibility(View.VISIBLE);
                setMarginStartForView(mTaskWarning, 48);
            }
        }

        private void defaultStateItem() {
            // Обнуляем все поля
            mTaskTitle.setText(null);
            mTaskDate.setText(null);
            mTaskPriority.setText(null);
            mTaskRepeat.setText(null);

            // Цвет ярлычка по умолчанию
            mTaskColor.setBackgroundResource(R.color.gray_2);

            // Прячем и двигаем иконку предупреждения на место
            mTaskWarning.setVisibility(View.INVISIBLE);
            setMarginStartForView(mTaskWarning, 32);

            // Цвет даты по умолчанию
            mTaskDate.setTextColor(mContext.getResources().getColor(R.color.gray_4));
        }
    }

    private void setMarginStartForView(View view, int value) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMarginStart(Helper.convertDpToPx(mContext, value));
        view.setLayoutParams(params);
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_reveal_layout, parent, false);
        return new TaskHolder(item);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        Task task = mTasks.get(position);

        // Save/restore the open/close state.
        // You need to provide a String id which uniquely defines the data object.
        String tempId = String.valueOf(task.getId());
        viewBinderHelper.bind(holder.swipeLayout, tempId);

        // Мои бинды
        holder.bindTask(task);

        // Если установленная позиция равна текущей, то выделяем элемент
        //holder.itemView.setSelected(mPosition == position);
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public void setTasks(OrderedRealmCollection<Task> results) {
        mTasks = results;
    }

    // Интерфейс для проброса слушателя наружу
    public interface OnItemClickListener {
        void onItemClick(View itemView, int itemPosition, Task task, int selectedPosition);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void resetSelection() {
        mPosition = RecyclerView.NO_POSITION;   //NO_POSITION == -1
    }
}
