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

import java.util.Calendar;
import java.util.GregorianCalendar;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ru.kulikovman.tasklist.Helper;
import ru.kulikovman.tasklist.R;

public class TaskAdapter extends RealmRecyclerViewAdapter<Task, TaskAdapter.TaskHolder> {
    private OnItemClickListener mOnItemClickListener;
    private OrderedRealmCollection<Task> mResults;
    private Context mContext;

    private int mRowIndex = -1;
    private boolean mIsSelected = false;

    public TaskAdapter(Context context, OrderedRealmCollection<Task> results) {
        super(results, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        setHasStableIds(true);

        mResults = results;
        mContext = context;
    }

    public void setResults(OrderedRealmCollection<Task> results) {
        mResults = results;
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getId();
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View item = inflater.inflate(R.layout.item_task, parent, false);
        return new TaskHolder(item);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        Task task = mResults.get(position);
        holder.bindTask(task);

        // Если установленная позиция равна текущей, то выделяем элемент
        holder.itemView.setSelected(mRowIndex == position);
        mIsSelected = holder.itemView.isSelected();
    }

    public class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTaskTitle, mTaskDate, mTaskPriority, mTaskRepeat;
        private ImageButton mTaskColor;
        private ImageView mTaskWarning;

        public TaskHolder(View view) {
            super(view);

            // Инициализируем вью элемента списка
            mTaskTitle = (TextView) view.findViewById(R.id.item_task_title);
            mTaskDate = (TextView) view.findViewById(R.id.item_task_date);
            mTaskPriority = (TextView) view.findViewById(R.id.item_task_priority);
            mTaskRepeat = (TextView) view.findViewById(R.id.item_task_repeat);
            mTaskColor = (ImageButton) view.findViewById(R.id.item_task_color);
            mTaskWarning = (ImageView) view.findViewById(R.id.item_task_warning);

            // Слушатель нажатий по элементу
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Обновляем айтем нажатый ранее
            notifyItemChanged(mRowIndex);

            // Сохраняем старую и получаем новую позицию
            int oldPosition = mRowIndex;
            mRowIndex = getLayoutPosition();

            // Если старая и новая позиции совпадают, то удаляем позицию
            if (mRowIndex == oldPosition) {
                resetSelection();
            }

            // Обновляем айтем нажатый сейчас
            notifyItemChanged(mRowIndex);

            // Код для проброса слушателя
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, getLayoutPosition(), mIsSelected);
            }
        }

        // Назначаем содержимое для текущего элемента списка
        public void bindTask(Task task) {
            //mTask = task;

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

        private void setMarginStartForView(View view, int value) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.setMarginStart(Helper.convertDpToPx(mContext, value));
            view.setLayoutParams(params);
        }
    }

    // Интерфейс для проброса слушателя наружу
    public interface OnItemClickListener {
        void onItemClick(View itemView, int itemPosition, boolean isSelected);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void resetSelection() {
        mRowIndex = RecyclerView.NO_POSITION;
        mIsSelected = false;
    }
}
