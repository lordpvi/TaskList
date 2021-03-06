package easysales.tasklist.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import easysales.tasklist.ApplicationWrapper;
import easysales.tasklist.R;
import easysales.tasklist.model.Task;
import easysales.tasklist.model.repository.TaskRepository;
import easysales.tasklist.model.service.TaskService;
import easysales.tasklist.presenter.TaskListPresenter;
import easysales.tasklist.presenter.base.MvpPresenter;
import easysales.tasklist.view.adapter.TaskRecycleListAdapter;
import easysales.tasklist.view.base.MvpFragment;
import easysales.tasklist.view.dialog.TaskEditDialog;
import easysales.tasklist.view.loader.TaskListLoader;


/**
 * A simple {@link Fragment} subclass.
 */
public class TaskListFragment extends MvpFragment
        implements  TaskListView,
                    TaskRecycleListAdapter.TaskViewHolder.OnItemClickListener,
                    SwipeRefreshLayout.OnRefreshListener,{

    @Inject
    TaskListPresenter presenter;

    @BindView(R.id.task_list)
    RecyclerView taskRecyclerView;

    @BindView(R.id.add_task_button)
    Button addTaskButton;

    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeContainer;

    private TaskRecycleListAdapter adapter;

    public TaskListFragment() { }

    public static TaskListFragment newInstance() {
        TaskListFragment taskListFragment = new TaskListFragment();
        return taskListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        ApplicationWrapper.getAppComponent().injectTaskListFragment(this);
        ButterKnife.bind(this, view);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter = new TaskRecycleListAdapter(this);
        taskRecyclerView.setAdapter(adapter);
        swipeContainer.setOnRefreshListener(this);

        presenter.attachView(this);
        presenter.onViewLoaded();
        return view;
    }

    @Override
    public void onItemClick(Task task) {
        presenter.onTaskClick(task);
    }

    @OnClick(R.id.add_task_button)
    public void onAddTaskClick(View view) {
        Log.d(getUserTag(), "add new task");
        presenter.onAddTackClick();
    }

    @Override
    public void refreshList() {
        getLoaderManager().restartLoader(TaskListLoader.ID, null, this).forceLoad();
    }

    @Override
    public void showTaskEditDialog(Task task) {
        final TaskEditDialog taskEditDialog = TaskEditDialog.newInstance(task);

        taskEditDialog.setConfirmRunnable(new Runnable() {
            @Override
            public void run() {
                presenter.onTaskEdited(taskEditDialog.getTask());
            }
        });

        taskEditDialog.show(getFragmentManager(), TaskEditDialog.TAG);
    }

    private void showTasks(List<Task> tasks) {
        adapter.setTasks(tasks);
        adapter.notifyDataSetChanged();
        if(swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }
    }


    @Override
    public void onRefresh() {
        refreshList();
    }

    @Override
    protected MvpPresenter getPresenter() {
        return presenter;
    }
}
